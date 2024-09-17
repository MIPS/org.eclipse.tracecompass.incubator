package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

public class ShinroProfilingTrace extends TmfTrace {

    @Override
    public IStatus validate(IProject project, String path) {
        // TODO Auto-generated method stub
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
