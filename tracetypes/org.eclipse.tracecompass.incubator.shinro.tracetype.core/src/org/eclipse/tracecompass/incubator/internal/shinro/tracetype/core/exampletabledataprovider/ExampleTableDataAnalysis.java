package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class ExampleTableDataAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis */
    public static final String ID = "org.eclipse.tracecompass.incubator.shinro.tracetype.core.analysis.exampletabledataprovider"; //$NON-NLS-1$

    /**
     *
     */
    public ExampleTableDataAnalysis() {
        setId(ID);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new ExampleTableStateProvider(Objects.requireNonNull(getTrace()), ExampleTableDataAnalysis.ID);
    }

}


class ExampleTableStateProvider extends AbstractTmfStateProvider {

    String moduleId;

    long tempEventsSeen = 0;

    public ExampleTableStateProvider(@NonNull ITmfTrace trace, @NonNull String moduleId) {
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
        return new ExampleTableStateProvider(getTrace(), moduleId);
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