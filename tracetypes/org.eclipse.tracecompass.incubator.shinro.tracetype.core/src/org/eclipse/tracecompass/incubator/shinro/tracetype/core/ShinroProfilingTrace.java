package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

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
                }

                shinroProfilingEventType = buildShinroProfilingEventType();

            }
            // TODO - CONSIDER MULTI-CORE!  Currently getting single core working, but there will probably be one
            // dataset per core for multi-core.  So this metehod, and class member data structures will probably need
            // to be adjusted to be per-core.
        }
    }


    @Override
    public synchronized void dispose() {
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
        if (context.getRank() != f_rank) {
            System.out.println("Unexpected; figure out an explanation.");
        }

        // TODO: return null if f_rank is beyond bounds of what is still unconsumed
        if (f_rank >= f_instProfDataNumElements) {
            return null;
        }


        ITmfEventField content = getFieldContent(f_rank);

        // let's use the cycle_first_seen field as the timestamp, since there's no better option from what's available
        ITmfEventField fieldCycleFirstSeen = content.getField("cycle_first_seen");
        ITmfTimestamp eventTimestamp = null;
        if (fieldCycleFirstSeen != null) {
            BigInteger bigintVal = (BigInteger)fieldCycleFirstSeen.getValue();
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
        // for each field in data map, create a TmfEventField instance with the name of the data
        // map field and the [f_rank] offset of the array of data values for that field
        ArrayList<ITmfEventField> children = new ArrayList<>();
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
                    System.out.println("Shinro Profiling Trace internal error: unexpected data type returned from jhdf query.");
                }
                if (fieldVal != null) {
                    TmfEventField child = new ShinroProfilingEventField(fieldname, fieldVal, shouldFieldBeDisplayedInHex(fieldname), null);
                    children.add(child);
                }
                // There's one pseudo field that's not in /inst_prof_data but we have to look it up
                // at runtime from info gleaned from /inst_disasm_data: disassembly text.
                // So this case is treated specially:
                if (fieldname.equals("opcode")) {
                    // lookup in opcode map to get dasm text
                    String dasmString = f_opcodeDasmMap.get(fieldVal);
                    if (dasmString != null) {
                        QuotedString quotedString = new QuotedString(dasmString);
                        TmfEventField child = new ShinroProfilingEventField("disasm", quotedString, false, null);
                        children.add(child);
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


}
