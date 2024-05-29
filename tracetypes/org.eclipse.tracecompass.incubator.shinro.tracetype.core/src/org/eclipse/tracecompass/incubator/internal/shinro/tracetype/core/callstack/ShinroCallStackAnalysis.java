package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroTrace;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class ShinroCallStackAnalysis extends CallStackAnalysis {

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof ShinroTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    public ShinroTrace getTrace() {
        return (ShinroTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new ShinroCallStackProvider(checkNotNull(getTrace()));
    }

}

class ShinroCallStackProvider extends CallStackStateProvider {

    public ShinroCallStackProvider(ITmfTrace trace) {
        super(trace);
    }

    @Override
    public int getVersion() {
        // TODO: figure out what's best; 0 is probably OK to start
        return 0;
    }

    @Override
    public @NonNull CallStackStateProvider getNewInstance() {
        // TODO Auto-generated method stub
        return new ShinroCallStackProvider(getTrace());
    }

    @Override
    protected boolean considerEvent(@NonNull ITmfEvent event) {
        // TODO: if possible, make the return value more discriminating (for performance)
        return true;
    }

    @Override
    protected @Nullable ITmfStateValue functionEntry(@NonNull ITmfEvent event) {
        // TODO: Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        return null;
    }

    @Override
    protected @Nullable ITmfStateValue functionExit(@NonNull ITmfEvent event) {
        // TODO: Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        return null;
    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        // TODO: this probably isn't relevant for Shinro traces; 0 is probably fine
        return 0;
    }

    @Override
    protected long getThreadId(@NonNull ITmfEvent event) {
        // TODO: this probably isn't relevant for Shinro traces; 0 is probably fine
        return 0;
    }

}
