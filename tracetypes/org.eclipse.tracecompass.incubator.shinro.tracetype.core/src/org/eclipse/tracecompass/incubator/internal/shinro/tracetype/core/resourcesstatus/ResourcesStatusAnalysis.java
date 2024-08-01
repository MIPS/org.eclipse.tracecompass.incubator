package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class ResourcesStatusAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis */
    public static final String ID = "org.eclipse.tracecompass.incubator.shinro.tracetype.core.analysis.resourcesstatus"; //$NON-NLS-1$

    /**
     *
     */
    public ResourcesStatusAnalysis() {
        setId(ID);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new ResourcesStatusStateProvider(Objects.requireNonNull(getTrace()), ResourcesStatusAnalysis.ID);
    }

}


class ResourcesStatusStateProvider extends AbstractTmfStateProvider {

    String moduleId;

    long tempEventsSeen = 0;

    public ResourcesStatusStateProvider(@NonNull ITmfTrace trace, @NonNull String moduleId) {
        super(trace, moduleId);
        this.moduleId = moduleId;
    }

    @Override
    public int getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new ResourcesStatusStateProvider(getTrace(), moduleId);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        ITmfStateSystemBuilder ss = getStateSystemBuilder();
        if (ss == null) {
            return;
        }
        tempEventsSeen++;
        int fooQuark = ss.getQuarkAbsoluteAndAdd("foo", "bar");
        ss.modifyAttribute(event.getTimestamp().toNanos(), Long.valueOf(tempEventsSeen), fooQuark);
    }
}