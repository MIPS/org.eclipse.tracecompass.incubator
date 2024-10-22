package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.Activator;
import org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.aspects.ShinroProfilingSourceAspect;
import org.eclipse.tracecompass.internal.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

import com.google.common.collect.ImmutableSet;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.api.NodeType;
import io.jhdf.exceptions.HdfException;
import io.jhdf.object.datatype.CompoundDataType;
import io.jhdf.object.datatype.CompoundDataType.CompoundDataMember;
import io.jhdf.object.datatype.DataType;

public class ShinroProfilingTrace extends TmfTrace {

    Map<String, Object> f_instProfData = null;
    Map<Long, String> f_opcodeDasmMap = new HashMap<Long, String>();
    long f_instProfDataNumElements;
    long f_rank;
    Map<String, DatasetMetadata> f_instProfMetadata = new HashMap<>();
    ITmfEventType shinroProfilingEventType;

    private final String TIMESTAMP_FIELD_NAME = "cycle_first_seen";
    // TODO: update above line to "cycle_first_retired" when that
    // corresponding change gets merged to master branch of shinro

    private static final @NonNull Collection<ITmfEventAspect<?>> SHINRO_ASPECTS;
    /** Default collections of aspects */
    private @NonNull Collection<ITmfEventAspect<?>> fShinroTraceAspects = ImmutableSet.copyOf(SHINRO_ASPECTS);

    private Addr2line addr2line;
    private static final String ADDR2LINE_EXECUTABLE = "riscv64-mti-elf-addr2line";

    static {
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(TmfTrace.BASE_ASPECTS);

        // these were attempts to get source linkage to work from events table,
        // but that looks like it doesn't work in the VSCode/Theia based trace UIs,
        // in which case there may be no point.
        builder.add(ShinroProfilingSourceAspect.INSTANCE);
        //builder.add(UstDebugInfoSourceAspect.INSTANCE);
        //builder.add(UstDebugInfoBinaryAspect.INSTANCE);
        //builder.add(UstDebugInfoFunctionAspect.INSTANCE);



        SHINRO_ASPECTS = builder.build();
    }

    class DatasetMetadata {
        DatasetMetadata(CompoundDataMember member) {
            this.member = member;
            DataType mdt = member.getDataType();
            cls = mdt.getJavaType();
        }
        CompoundDataMember member;
        Class<?> cls;
    }

    private static final int CONFIDENCE = 100;

    public BigInteger getCallInfo(long rank) {
        BigInteger result = null;
        BigInteger [] arr = (BigInteger[])f_instProfData.get("is_call");
        if (arr != null) {
            result = arr[(int)rank];
        }
        return result;
    }

    public BigInteger getReturnInfo(long rank) {
        BigInteger result = null;
        BigInteger [] arr = (BigInteger[])f_instProfData.get("is_return");
        if (arr != null) {
            result = arr[(int)rank];
        }
        return result;
    }

    static private boolean isShinroProfilingHdf5File(String strPath) {
        // a file is deemed to be a Shinro profiling HDF5 file if it can be opened
        // successfully with the HDF5 library, and if it has a group /inst_prof_data.
        boolean valid = false;
        Path path = Paths.get(strPath);
        try (HdfFile hdfFile = new HdfFile(path)) {
            // consider the answer being yes iff the file opens successfully as an HDF5 file,
            // and if the group /inst_prof_data is present
            Node nodeProfData = hdfFile.getByPath("/inst_prof_data");
            valid = (nodeProfData != null && nodeProfData.getType() == NodeType.GROUP);
        } catch (HdfException e) {

        }
        return valid;
    }

    @Override
    public void initTrace(IResource resource, String strPath, Class<? extends ITmfEvent> type, String name, String traceTypeId) throws TmfTraceException {
        Path path = Paths.get(strPath);
        try (HdfFile file = new HdfFile(path)) {
            loadInstDisasmData(file);
            loadInstProfData(file);
        } catch (HdfException e) {

        }
        super.initTrace(resource, strPath, type, name, traceTypeId);
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(SHINRO_ASPECTS);
        fShinroTraceAspects = builder.build();
        Path elfPath = ShinroSymbolProvider.lookForElf(path);
        if (elfPath != null) {
            String [] params = {"-C", "-f", "-e", elfPath.toString()};
            try {
                addr2line = new Addr2line(ADDR2LINE_EXECUTABLE, params, elfPath.toString(), null);
            } catch (IOException e) {
                // addr2line will stay null
                e.printStackTrace();
            }
        }
    }

    private void loadInstDisasmData(HdfFile file) {
        Node nodeInstDisasmData = file.getByPath("/inst_disasm_data");
        if (nodeInstDisasmData != null && nodeInstDisasmData.getType() == NodeType.DATASET) {
            Dataset dataset = (Dataset)nodeInstDisasmData;
            Map<String, Object> map = (Map<String, Object>)dataset.getData();
            long size = dataset.getSize();
            for (int idx = 0; idx < (int)size; idx++) {
                long [] aryLong = (long[])map.get("opcode");
                String [] aryString =  (String[])map.get("disasm");
                if (aryLong != null && aryString != null) {
                    f_opcodeDasmMap.put(aryLong[idx], aryString[idx]);
                }
            }
        }
    }

    /**
     * @param fieldname
     */
    private static boolean shouldFieldBeDisplayedInHex(String fieldname) {
        return fieldname.equals("inst_addr") ||
                fieldname.equals("opcode");
    }


    static final String CORE_INDEX_SENTINEL_SUBSTRING = "_core";

    private static int getCoreIndexFromDatasetName(String datasetName) {
        // dataset name is something like "dhry_mips_core0", so the heuristic is to
        // look for a trailing "_core" substring and then scrape the index from the remaining characters
        int coreIndex = 0;
        int pos = datasetName.lastIndexOf(CORE_INDEX_SENTINEL_SUBSTRING);
        if (pos != -1) {
            String strIndex = datasetName.substring(pos+CORE_INDEX_SENTINEL_SUBSTRING.length());
            coreIndex = Integer.valueOf(strIndex);
        }
        return coreIndex;
    }

    private void loadInstProfData(HdfFile file) {
        Node nodeProfData = file.getByPath("/inst_prof_data");

        if (nodeProfData != null && nodeProfData.getType() == NodeType.GROUP) {
            Group groupProfData = (Group)nodeProfData;
            // find first dataset (we don't statically know the name at this point)
            Dataset firstDataset = null;
            var children = groupProfData.getChildren();
            var childIterator = children.values().iterator();
            while (childIterator.hasNext()) {
                var child = childIterator.next();
                if (child.getType() == NodeType.DATASET) {
                    firstDataset = (Dataset)child;
                    break;
                }
            }
            if (firstDataset != null) {
                String datasetName = firstDataset.getName();
                int coreIdx = getCoreIndexFromDatasetName(datasetName);
                f_instProfDataNumElements = firstDataset.getDimensions()[0];
                DataType dt = firstDataset.getDataType();
                CompoundDataType cdt = (CompoundDataType)dt;
                List<CompoundDataMember> members = cdt.getMembers();
                for (int idx = 0; idx < members.size(); idx++) {
                    CompoundDataMember member = members.get(idx);
                    f_instProfMetadata.put(member.getName(), new DatasetMetadata(member));
                }
                Object obj = firstDataset.getData();
                if (obj instanceof Map<?, ?>) {
                    f_instProfData = (Map<String, Object>)firstDataset.getData();
                    // compute call/return pseudo attributes
                    assignCoreIndexField(f_instProfData, (int)f_instProfDataNumElements, coreIdx);
                    computeCallReturnPseudoAttributes(f_instProfData, (int)f_instProfDataNumElements);
                }

                shinroProfilingEventType = buildShinroProfilingEventType();

            }
            // TODO - CONSIDER MULTI-CORE!  Currently getting single core working, but there will probably be one
            // dataset per core for multi-core.  So this metehod, and class member data structures will probably need
            // to be adjusted to be per-core.
        }
    }

    static private void assignCoreIndexField(Map<String, Object> map, int numElements, int coreIndex) {
        int [] core = new int [numElements];
        Arrays.fill(core, coreIndex);
        map.put("core", core);
    }

    static final int MATCH_JAL = 0x6f;
    static final int MASK_JAL = 0x7f;
    static final int MATCH_JALR = 0x67;
    static final int MASK_JALR = 0x707f;
    static final int MATCH_C_JAL = 0x2001;
    static final int MASK_C_JAL = 0xe003;
    static final int MATCH_C_JALR = 0x9002;
    static final int MASK_C_JALR = 0xf07f;
    static final int MATCH_C_JR = 0x8002;
    static final int MASK_C_JR = 0xf07f;

    private void computeCallReturnPseudoAttributes(Map<String, Object> map, int numElements) {
        long [] opcodes = (long [])f_instProfData.get("opcode");
        if (opcodes == null) {
            return;
        }
        BigInteger [] addresses = (BigInteger[])f_instProfData.get("inst_addr");
        if (addresses == null) {
            return;
        }
        Stack<BigInteger> stack = new Stack<>();
        BigInteger [] isCallArray = new BigInteger[numElements];
        BigInteger [] isReturnArray = new BigInteger[numElements];
        map.put("is_call", isCallArray);
        map.put("is_return", isReturnArray);
        for (int idx = 0; idx < numElements; idx++) {
            boolean isCall = false;
            boolean isReturn = false;
            long opcode = opcodes[idx];
            if ((opcode & MASK_JAL) == MATCH_JAL) {
                int rd = ((int)opcode >> 7) & 0x1F;
                isCall = rd == 1 || rd == 5;
            } else if ((opcode & MASK_JALR) == MATCH_JALR) {
                int rd = ((int)opcode >> 7) & 0x1F;
                int rs1 = ((int)opcode >> 15) & 0x1F;
                boolean rd_is_link = rd == 1 || rd == 5;
                boolean rs1_is_link = rs1 == 1 || rs1 == 5;
                boolean rd_and_rs1_equal = rd == rs1;
                isCall = rd_is_link && (!rs1_is_link || rd_and_rs1_equal);
                isReturn = !rd_is_link && rs1_is_link;
            } else if ((opcode & MASK_C_JAL) == MATCH_C_JAL) {
                // rd is implicitly 1, so it's always a call
                isCall = true;
            } else if ((opcode & MASK_C_JALR) == MATCH_C_JALR) {
                int rd = 1;  // implicit
                int rs1 = ((int)opcode >> 7) & 0x1F;
                boolean rd_is_link = rd == 1 || rd == 5;
                boolean rs1_is_link = rs1 == 1 || rs1 == 5;
                boolean rd_and_rs1_equal = rd == rs1;
                isCall = rd_is_link && (!rs1_is_link || rd_and_rs1_equal);
                isReturn = !rd_is_link && rs1_is_link;
            } else if ((opcode & MASK_C_JR) == MATCH_C_JR) {
                int rs1 = ((int)opcode >> 7) & 0x1F;
                boolean rs1_is_link = rs1 == 1 || rs1 == 5;
                isReturn = !rs1_is_link && rs1_is_link;
            }
            if (isCall && idx < numElements-1) {
                BigInteger addr = addresses[idx+1];
                isCallArray[idx] = addr;
                stack.push(addr);
                BigInteger [] arr = (BigInteger[])map.get("is_call");
                if (arr != null) {
                    //System.out.println(String.format("Rank %d is a call to addr 0x%x", idx, addr.longValue()));
                    arr[idx] = addr;
                }
            }
            if (isReturn) {
                if (stack.empty()) {
                    // System.out.println("Warning: saw return without preceding call");
                } else {
                    BigInteger top = stack.pop();
                    isReturnArray[idx] = top;
                    BigInteger [] arr = (BigInteger[])map.get("is_return");
                    if (arr != null) {
                        arr[idx] = top;
                        // System.out.println(String.format("Rank %d is a return from function at addr 0x%x", idx, top.longValue()));
                    }

                }
            }
        }
    }


    @Override
    public synchronized void dispose() {
        if (addr2line != null) {
            addr2line.dispose();
        }
        super.dispose();
    }

    @Override
    public IStatus validate(IProject project, String path) {
        if (isShinroProfilingHdf5File(path)) {
            return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        }
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ShinroTrace_DomainError);
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return new TmfLongLocation(f_rank);
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        if (location == null) {
            f_rank = 0;
        }
        TmfContext context = new TmfContext(location, f_rank);
        return context;
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        // TODO Auto-generated method stub
        return null;

    }



    private ITmfEventType buildShinroProfilingEventType() {
        ITmfEventField rootField = null;
        ArrayList<ITmfEventField> subFields = new ArrayList<>();
        // query f_instProfMetadata for the event fields
        var fields = f_instProfMetadata.values();
        var it = fields.iterator();
        while (it.hasNext()) {
            DatasetMetadata dm = it.next();
            String fieldName = dm.member.getName();
            ShinroProfilingEventField field = new ShinroProfilingEventField(fieldName, null, shouldFieldBeDisplayedInHex(fieldName), null);
            subFields.add(field);
        }
        rootField = new ShinroProfilingEventField(ITmfEventField.ROOT_FIELD_ID, null, false, subFields.toArray(new ITmfEventField[0]));
        ShinroProfilingEventType eventType = new ShinroProfilingEventType("inst_prof_data", rootField);
        return eventType;
    }



    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        // This shouldn't happen, but we'd like to know if it does happen
        if (context.getRank() != f_rank) {
            throw new RuntimeException("Internal error in ShinroProfilingTrace.parseEvent()");
        }

        if (f_rank >= f_instProfDataNumElements) {
            return null;
        }


        ITmfEventField content = getFieldContent(f_rank);

        ITmfEventField fieldTimestamp = content.getField(TIMESTAMP_FIELD_NAME);
        ITmfTimestamp eventTimestamp = null;
        if (fieldTimestamp != null) {
            BigInteger bigintVal = (BigInteger)fieldTimestamp.getValue();
            long longval = bigintVal.longValue();
            eventTimestamp = new TmfNanoTimestamp(longval);
        }

        ITmfEvent event = new ShinroProfilingEvent(this, f_rank, eventTimestamp, shinroProfilingEventType, content);

        // advance rank so that when getCurrentLocation gets called next time, we return
        // a location that references the incremented rank
        f_rank++;

        // return the event we constructed
        return event;
    }



    /**
     * @param rank
     */
    public ITmfEventField getFieldContent(long rank) {
        ArrayList<ITmfEventField> children = new ArrayList<>();

        // a "core" field isn't part of the inst_prof_data record in the HDF5 file, but
        //  we figured it out when loading from HDF5 anyway.  Here we are handling that field specially
        int [] coreIndexArray = (int[])f_instProfData.get("core");
        int coreIndex = 0;
        if (coreIndexArray != null) {
            coreIndex = coreIndexArray[(int)rank];
        }
        TmfEventField child = new ShinroProfilingEventField("core", coreIndex, shouldFieldBeDisplayedInHex("core"), null);
        children.add(child);

        // for each field in data map, create a TmfEventField instance with the name of the data
        // map field and the [f_rank] offset of the array of data values for that field

        f_instProfMetadata.forEach((fieldname, metadata) -> {
            Object field = f_instProfData.get(fieldname);
            if (field != null) {
                Object fieldVal = null;
                if (metadata.cls == BigInteger.class) {
                    BigInteger [] ary = (BigInteger[])field;
                    fieldVal = ary[(int)f_rank];
                } else if (metadata.cls == long.class) {
                    long [] ary = (long[])field;
                    fieldVal = ary[(int)f_rank];
                } else if (metadata.cls == int.class) {
                    int [] ary = (int[])field;
                    fieldVal = ary[(int)f_rank];
                } else if (metadata.cls == double.class) {
                    double [] ary = (double[])field;
                    fieldVal = ary[(int)f_rank];
                } else {
                    throw new RuntimeException("Shinro Profiling Trace internal error: unexpected data type returned from jhdf query.");
                }
                if (fieldVal != null) {
                    TmfEventField f = new ShinroProfilingEventField(fieldname, fieldVal, shouldFieldBeDisplayedInHex(fieldname), null);
                    children.add(f);
                }
                if (fieldname.equals("opcode")) {
                    // lookup in opcode map to get dasm text
                    String dasmString = f_opcodeDasmMap.get(fieldVal);
                    if (dasmString != null) {
                        QuotedString quotedString = new QuotedString(dasmString);
                        TmfEventField f = new ShinroProfilingEventField("disasm", quotedString, false, null);
                        children.add(f);
                    }
                } else if (fieldname.equals("inst_addr")) {
                    // try to get a file:line pair for this address
                    if (addr2line != null) {
                        BigInteger big = (BigInteger)fieldVal;
                        Addr64 addr = new Addr64(big);
                        try {
                            String strFileName = addr2line.getFileName(addr);
                            int lineNumber = addr2line.getLineNumber(addr);
                            if (lineNumber != -1) {
                                // TODO: store file/line in an appropriate place within the event structure
                                System.out.println(strFileName);  // just to quiet unused variable warning
                            }
                        } catch (IOException e) {
                            // not much we can do
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        TmfEventField rootField = new ShinroProfilingEventField(ITmfEventField.ROOT_FIELD_ID, null, false, children.toArray(new ITmfEventField[0]));
        return rootField;
    }

    /**
     * @param rank
     * @return
     */
    public long getEventAddress(long rank) {
        Object field = f_instProfData.get("inst_addr");
        BigInteger [] ary = (BigInteger[])field;
        if (ary != null) {
            return ary[(int)rank].longValue();
        }
        return 0L;
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return fShinroTraceAspects;
    }



}
