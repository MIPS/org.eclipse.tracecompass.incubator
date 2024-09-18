package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5FileInterfaceException;

public class ShinroProfilingTrace extends TmfTrace {

    private static final int CONFIDENCE = 100;

    long f_file_id = HDF5Constants.H5I_INVALID_HID;
    long f_dataset_id = HDF5Constants.H5I_INVALID_HID;
    long f_dataspace_id = HDF5Constants.H5I_INVALID_HID;
    long f_num_elements = 0;
    long f_rank = 0;


    static private boolean isHdf5File(String path) {
        try {
            boolean valid = false;
            long file_id = H5.H5Fopen(path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            boolean opened = file_id != HDF5Constants.H5I_INVALID_HID;
            if (opened) {
                valid = true;  // TODO - expand the logic to check for expected groups/datasets
                H5.H5Fclose(file_id);
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
            // anything need to be done at this point?
            String [] nameOut = new String[1];
            int [] typeOut = new int[1];
            int indexOfExpectedDataset = 0;  // We know this statically because we know the general structure of what Shinro outputs
            H5.H5Gget_obj_info_idx(f_file_id, "/inst_prof_data", indexOfExpectedDataset, nameOut, typeOut);
            f_dataset_id = H5.H5Dopen(f_file_id, "/inst_prof_data/" + nameOut[0], HDF5Constants.H5P_DEFAULT);
            f_dataspace_id = H5.H5Dget_space(f_dataset_id);
            f_num_elements = H5.H5Sget_simple_extent_npoints(f_dataspace_id);
        }
        super.initTrace(resource, path, type, name, traceTypeId);
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
        if (isHdf5File(path)) {
            return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        }
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ShinroTrace_DomainError);
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        if (context.getRank() != f_rank) {
            System.out.println("Unexpected; figure out an explanation.");
        }
        // TODO Auto-generated method stub
        return null;
    }

}
