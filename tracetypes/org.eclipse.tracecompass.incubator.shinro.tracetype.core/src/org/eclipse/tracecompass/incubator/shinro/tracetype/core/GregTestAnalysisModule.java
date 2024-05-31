package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 *
 */
public class GregTestAnalysisModule extends TmfStateSystemAnalysisModule {



    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        return super.executeAnalysis(monitor);
    }

    @Override
    protected void canceling() {
        // TODO Auto-generated method stub
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new StateProvider(getTrace(), "id");
    }


}

class StateProvider extends AbstractTmfStateProvider {

    String id;

    public StateProvider(ITmfTrace trace, @NonNull String id) {
        super(trace, id);
        this.id = id;
    }

    @Override
    public int getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new StateProvider(getTrace(), id);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        // TODO Auto-generated method stub

    }

}
