package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
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

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.callbacks.H5D_iterate_cb;
import hdf.hdf5lib.callbacks.H5D_iterate_t;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.hdf5lib.exceptions.HDF5FileInterfaceException;

public class ShinroProfilingTrace extends TmfTrace {

    private static final int CONFIDENCE = 100;

    long f_file_id = HDF5Constants.H5I_INVALID_HID;
    long f_dataset_id = HDF5Constants.H5I_INVALID_HID;
    long f_dataspace_id = HDF5Constants.H5I_INVALID_HID;
    long f_num_elements = 0;
    long f_rank = 0;
    Hdf5libProfilingDataSlicedAccessor f_accessor = null;


    static private boolean isShinroProfilingHdf5File(String path) {
        // a file is deemed to be a Shinro profiling HDF5 file if it can be opened
        // successfully with the HDF5 library, and if it has a group /inst_prof_data.
        try {
            boolean valid = false;
            long file_id = H5.H5Fopen(path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            boolean opened = file_id != HDF5Constants.H5I_INVALID_HID;
            if (opened) {
                try {
                    long group_id = H5.H5Gopen(file_id, "/inst_prof_data", HDF5Constants.H5P_DEFAULT);
                    valid = group_id >= 0;
                    if (valid) {
                        H5.H5Gclose(group_id);
                    }
                } catch (HDF5Exception e) {
                    // This means that the HDF5 file doesn't have the expected group, so it's
                    // OK to swallow this exception, and assume that this is not a Shinro profiling HDF5 file
                } finally {
                    H5.H5Fclose(file_id);
                }
            }
            return valid;
        } catch (HDF5FileInterfaceException e) {
            return false;
        }
    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type, String name, String traceTypeId) throws TmfTraceException {
        f_file_id = H5.H5Fopen(path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
        if (f_file_id != HDF5Constants.H5I_INVALID_HID) {
            // We can't statically know the *name* of the expected dataset, but we do statically know the position (0).
            // The HDF5Dopen API wants a name, so we have an extra step of looking up the name from the known index.
            String [] nameOut = new String[1];
            int [] typeOut = new int[1];
            int indexOfExpectedDataset = 0;
            H5.H5Gget_obj_info_idx(f_file_id, "/inst_prof_data", indexOfExpectedDataset, nameOut, typeOut);

            // Now the we have the name of the dataset we're interested in, we can open it and query for
            // additional information we need
            f_dataset_id = H5.H5Dopen(f_file_id, "/inst_prof_data/" + nameOut[0], HDF5Constants.H5P_DEFAULT);
            f_dataspace_id = H5.H5Dget_space(f_dataset_id);
            f_num_elements = H5.H5Sget_simple_extent_npoints(f_dataspace_id);

            loadDasmData(f_file_id);

            // TODO - CONSIDER MULTI-CORE!  Currently getting single core working, but there will probably be one
            // dataset per core for multi-core.  So this metehod, and class member data structures will probably need
            // to be adjusted to be per-core.
        }
        super.initTrace(resource, path, type, name, traceTypeId);
    }

    static class IterateCallback implements H5D_iterate_cb {
        /**
         * @param elem
         * @param elem_type
         * @param ndim
         * @param point
         * @param op_data
         */
        @Override
        public int callback (byte[] elem, long elem_type, int ndim, long[] point, H5D_iterate_t op_data) {
            return 0;
        }
    }

    static class IterateData implements H5D_iterate_t {

    }
    private static void loadDasmData(long file_id) {
        /*
        String [] nameOut = new String[1];
        int [] typeOut = new int[1];
        int indexOfExpectedDataset = 1;
        H5.H5Gget_obj_info_idx(file_id, "/", indexOfExpectedDataset, nameOut, typeOut);
        long dasm_dataset_id = H5.H5Dopen(file_id, "/" + nameOut[0], HDF5Constants.H5P_DEFAULT);
        */

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

    @Override
    public synchronized void dispose() {
        if (f_dataspace_id != HDF5Constants.H5I_INVALID_HID) {
            H5.H5Sclose(f_dataspace_id);
            f_dataspace_id = HDF5Constants.H5I_INVALID_HID;
        }
        if (f_dataset_id != HDF5Constants.H5I_INVALID_HID) {
            H5.H5Dclose(f_dataset_id);
            f_dataset_id = HDF5Constants.H5I_INVALID_HID;
        }
        if (f_file_id != HDF5Constants.H5I_INVALID_HID) {
            H5.H5Fclose(f_file_id);
            f_file_id = HDF5Constants.H5I_INVALID_HID;
        }
        // These assignments aren't strictly necessary, but might make it easier when debugging if
        // things have gone wrong
        f_num_elements = 0;
        f_rank = 0;
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
        invalidateAccessor();
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

    private Hdf5libProfilingDataSlicedAccessor getAccessor() {
        if (f_accessor == null) {
            f_accessor = new Hdf5libProfilingDataSlicedAccessor(f_dataset_id, f_num_elements, f_rank, 1, 1);
        }
        return f_accessor;
    }

    private void invalidateAccessor() {
        if (f_accessor != null) {
            f_accessor.dispose();
            f_accessor = null;
        }
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        if (context.getRank() != f_rank) {
            System.out.println("Unexpected; figure out an explanation.");
        }

        Hdf5libProfilingDataSlicedAccessor accessor = getAccessor();
        boolean gotNext = accessor.next();
        if (!gotNext) {
            return null;
        }


        ITmfEventField content = getFieldContent(accessor);
        ITmfEvent event = new ShinroProfilingEvent(this, f_rank, null, shinroProfilingEventType, content);

        // advance rank so that when getCurrentLocation gets called next time, we return
        // a location that references the incremented rank
        f_rank++;

        // return the event we constructed
        return event;
    }

    private static ITmfEventField getFieldContent(Hdf5libProfilingDataSlicedAccessor accessor) {
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
        TmfEventField rootField = new TmfEventField(":root:", null, children.toArray(new ITmfEventField[0]));
        return rootField;
    }

    static class Hdf5libProfilingDataSlicedAccessor {
        long [] stride = new long[1];
        long [] count = new long[1];
        long [] start = new long[1];
        long dataset_id;
        long num_elements;
        long memspace_id;
        long filespace_id;
        long datatype_id;
        byte [] bytes;

        Map<String, ProfilingDataMemberInfo> members = new HashMap<>();


        public Hdf5libProfilingDataSlicedAccessor(long dataset_id, long num_elements, long start, long count, long stride) {
            this.dataset_id = dataset_id;
            this.num_elements = num_elements;
            this.start[0] = start;
            this.count[0] = count;
            this.stride[0] = stride;
            memspace_id = H5.H5Screate_simple(1, this.count, null);
            filespace_id = H5.H5Dget_space(dataset_id);
            datatype_id = H5.H5Dget_type(dataset_id);

            // Determine how big of a byte buffer we need, and allocate it
            int datatype_size = (int)H5.H5Tget_size(datatype_id);
            bytes = new byte[(int)count * datatype_size];

            // Pre-calculate (one time) the offsets and sizes of members within the raw bytes representation
            int numMembers = H5.H5Tget_nmembers(datatype_id);
            for (int memberIdx = 0; memberIdx < numMembers; memberIdx++) {
                String memberName = H5.H5Tget_member_name(datatype_id, memberIdx);
                //System.out.println("member name is: " + field);
                long memberOffset = H5.H5Tget_member_offset(datatype_id, memberIdx);
                //System.out.println("member offset is: " + memberOffset);
                long memberType_id = H5.H5Tget_member_type(datatype_id, memberIdx);
                long memberSize = H5.H5Tget_size(memberType_id);
                //System.out.println("member size is: " + memberSize);

                members.put(memberName, new ProfilingDataMemberInfo(memberName, memberType_id, memberOffset, memberSize));
                H5.H5Tclose(memberType_id);
            }

        }

        public void dispose() {
            H5.H5Tclose(datatype_id);
            H5.H5Sclose(filespace_id);
            H5.H5Sclose(memspace_id);
        }

        public boolean next() {
            if (this.start[0] >= num_elements) {
                return false;
            }
            int selectResult = H5.H5Sselect_hyperslab(filespace_id, HDF5Constants.H5S_SELECT_SET, this.start, this.stride, this.count, null);
            if (selectResult < 0) {
                return false;
            }

            int readResult = H5.H5Dread(dataset_id, datatype_id, memspace_id, filespace_id,
                    HDF5Constants.H5P_DEFAULT, bytes);
            if (readResult >= 0) {
                members.forEach((name, info) -> {
                    if (info.type_class == HDF5Constants.H5T_INTEGER) {
                        info.longVal = bytesToIntLittleEndian(bytes, (int)info.offset, (int)info.size);
                        // System.out.println(String.format("%s = 0x%x", name, info.longVal));
                    } else if (info.type_class == HDF5Constants.H5T_FLOAT) {
                        info.doubleVal = bytesToDoubleLittleEndian(bytes, (int)info.offset, (int)info.size);
                        // System.out.println(String.format("%s = %f", name, info.doubleVal));
                    }
                });

                // increment position for the next access (in case there is a next access)
                this.start[0] = this.start[0]+(this.count[0] * this.stride[0]);
            }
            return readResult >= 0;
        }
    }

    static class ProfilingDataMemberInfo {
        public String name;
        public long type_id;
        public int type_class;
        public long offset;
        public long size;
        public double doubleVal;
        public long longVal;

        public ProfilingDataMemberInfo(String name, long type_id, long offset, long size) {
            this.name = name;
            this.type_id = type_id;
            this.type_class = H5.H5Tget_class(type_id);
            this.offset = offset;
            this.size = size;
        }
    }

    static long bytesToIntLittleEndian(byte [] bytes, int offset, int numbytes) {
        long val = 0;

        for (int i = 0; i < numbytes; i++) {
            val |= ((bytes[offset+i] & 0xFFL) << (8*i));
        }

        return val;
    }

    static double bytesToDoubleLittleEndian(byte [] bytes, int offset, int numbytes) {
        double d = ByteBuffer.wrap(bytes, offset, numbytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        return d;
    }

}
