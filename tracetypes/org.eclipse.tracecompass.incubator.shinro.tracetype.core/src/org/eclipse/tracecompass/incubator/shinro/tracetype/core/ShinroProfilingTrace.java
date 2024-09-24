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
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
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

            }
            // TODO - CONSIDER MULTI-CORE!  Currently getting single core working, but there will probably be one
            // dataset per core for multi-core.  So this metehod, and class member data structures will probably need
            // to be adjusted to be per-core.
        }
    }

/*
    private static void loadDasmData(long file_id) {

        long dasm_dataset_id = H5.H5Dopen(file_id, "/inst_disasm_data", HDF5Constants.H5P_DEFAULT);
        long dasm_dataspace_id = H5.H5Dget_space(dasm_dataset_id);
        //long num_elements = H5.H5Sget_simple_extent_npoints(dasm_dataspace_id);

        long datatype_id = H5.H5Dget_type(dasm_dataset_id);
        long datatype_size = H5.H5Tget_size(datatype_id);
        System.out.println(datatype_size);



        long member_type_id = H5.H5Tget_member_type(datatype_id, 1);
        long member_size = H5.H5Tget_size(member_type_id);
        long member_cls = H5.H5Tget_class(member_type_id);
        String member_name = H5.H5Tget_class_name(member_cls);
        long temp3 = H5.H5Tget_member_offset(datatype_id, 1);
        System.out.println(temp3);
        System.out.println(member_size);
        System.out.println(member_name);
        //byte [] bytes = new byte[(int)(datatype_size * num_elements)];
        byte [] bytes = new byte[1024*1024*10];
        //long [] dims = { num_elements };


        long str_type = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
        H5.H5Tset_size(str_type, HDF5Constants.H5T_VARIABLE);
        long size_readback = H5.H5Tget_size(str_type);
        System.out.println(size_readback);
        //H5.H5Tset_strpad(str_type,  HDF5Constants.H5T_STR_NULLTERM);
        H5.H5Tset_cset(str_type, HDF5Constants.H5T_CSET_ASCII);
        int cls = H5.H5Tget_class(str_type);
        String temp = H5.H5Tget_class_name(cls);
        System.out.println(temp);


        long memtype_id = H5.H5Tcreate(HDF5Constants.H5T_COMPOUND, datatype_size);
        H5.H5Tinsert(memtype_id, "opcode", 0, HDF5Constants.H5T_NATIVE_INT);
        long temp2 = H5.H5Tget_size(memtype_id);
        System.out.println(temp2);
        H5.H5Tinsert(memtype_id, "disasm", 8, str_type);
        long temp4 = H5.H5Tget_size(memtype_id);
        System.out.println(temp4);


        //long filespace_id = H5.H5Dget_space(dasm_dataset_id);
        H5.H5Dread(dasm_dataset_id, memtype_id, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, bytes);

        IterateCallback cb = new IterateCallback();
        IterateData d = new IterateData();
        //long memspace_id = H5.H5Screate_simple(1, dims, null);
        int result = H5.H5Diterate (bytes, datatype_id, dasm_dataspace_id, cb, d);
        System.out.println(result);
        //H5.H5Sclose(filespace_id);
        //H5.H5Sclose(memspace_id);
        H5.H5Tclose(datatype_id);


        if (dasm_dataset_id >= 0) {
            H5.H5Dclose(dasm_dataset_id);
        }
    }
    */

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

    ITmfEventType shinroProfilingEventType = buildShinroProfilingEventType();

    static ITmfEventType buildShinroProfilingEventType() {
        ShinroProfilingEventType eventType = new ShinroProfilingEventType("Shinro Profiling Event");
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
        ITmfEvent event = new ShinroProfilingEvent(this, f_rank, null, shinroProfilingEventType, content);

        // advance rank so that when getCurrentLocation gets called next time, we return
        // a location that references the incremented rank
        f_rank++;

        // return the event we constructed
        return event;
    }

    /**
     * @param rank
     */
    private ITmfEventField getFieldContent(long rank) {
        /*
        var map = accessor.members;
        var children = new ArrayList<ITmfEventField>();
        map.forEach((name, info) -> {
            Object fieldVal = null;
            if (info.type_class == HDF5Constants.H5T_INTEGER) {
                fieldVal = Long.valueOf(info.longVal);
            } else if (info.type_class == HDF5Constants.H5T_FLOAT) {
                fieldVal = Double.valueOf(info.doubleVal);
            }
            TmfEventField child = new TmfEventField(name, fieldVal, null);
            children.add(child);
        });
        */
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
                    System.out.println("Oops, saw a data type we're not prepared to see");
                }
                if (fieldVal != null) {
                    TmfEventField child = new TmfEventField(fieldname, fieldVal, null);
                    children.add(child);
                }
                // There's one pseudo field that's not in /inst_prof_data but we have to look it up
                // at runtime from info gleaned from /inst_disasm_data: disassembly text.
                // So this case is treated specially:
                if (fieldname.equals("opcode")) {
                    // lookup in opcode map to get dasm text
                    String dasmString = f_opcodeDasmMap.get(fieldVal);
                    if (dasmString != null) {
                        TmfEventField child = new TmfEventField("disasm", dasmString, null);
                        children.add(child);
                    }
                }
            }
        });

        TmfEventField rootField = new TmfEventField(":root:", null, children.toArray(new ITmfEventField[0]));
        return rootField;
    }



}
