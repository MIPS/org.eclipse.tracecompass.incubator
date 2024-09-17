package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5FileInterfaceException;

public class ShinroProfilingTrace extends TmfTrace {

    static private boolean isHdf5File(String path) {
        try {
            boolean valid = false;
            long file_id = H5.H5Fopen(path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
            boolean opened = file_id != HDF5Constants.H5I_INVALID_HID;
            if (opened) {
                valid = true;  // TODO - expand the logic to check for expected groups/datasets
            }
            return valid;
        } catch (HDF5FileInterfaceException e) {
            return false;
        }
    }

    @Override
    public IStatus validate(IProject project, String path) {
        if (!isHdf5File(path)) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ShinroTrace_DomainError);
        }

        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
