package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroProfilingEvent;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroProfilingTrace;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 *
 */
public class ShinroProfilingCallStackAnalysis extends CallStackAnalysis {

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof ShinroProfilingTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    public ShinroProfilingTrace getTrace() {
        return (ShinroProfilingTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new ShinroProfilingCallStackProvider(checkNotNull(getTrace()));
    }

}

class ShinroProfilingCallStackProvider extends CallStackStateProvider {
    public ShinroProfilingCallStackProvider(ITmfTrace trace) {
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
        return new ShinroProfilingCallStackProvider(getTrace());
    }

    @Override
    protected boolean considerEvent(@NonNull ITmfEvent event) {
        return event instanceof ShinroProfilingEvent;
    }


    @Override
    protected @Nullable ITmfStateValue functionEntry(@NonNull ITmfEvent event) {
        // Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        ITmfStateValue stateValue = null;
        if (!(event instanceof ShinroProfilingEvent)) {
            return null;
        }
        long rank = event.getRank();
        ShinroProfilingTrace ourTrace = (ShinroProfilingTrace)event.getTrace();
        BigInteger val = ourTrace.getCallInfo(rank);
        if (val != null) {
            // System.out.println(String.format("Event is a call: %s", event.toString()));
            stateValue = TmfStateValue.newValueLong(val.longValue());
        }
        return stateValue;
    }

    @Override
    protected @Nullable ITmfStateValue functionExit(@NonNull ITmfEvent event) {
        // Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        ITmfStateValue stateValue = null;
        if (!(event instanceof ShinroProfilingEvent)) {
            return null;
        }
        long rank = event.getRank();
        ShinroProfilingTrace ourTrace = (ShinroProfilingTrace)event.getTrace();
        BigInteger val = ourTrace.getReturnInfo(rank);
        if (val != null) {
            // System.out.println(String.format("Event is a return: %s", event.toString()));
            stateValue = TmfStateValue.newValueLong(val.longValue());
        }
        return stateValue;

    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        // TODO: make this the core index
        return 0;
    }

    @Override
    protected long getThreadId(@NonNull ITmfEvent event) {
        // TODO: when hw threads are modeled, make this hw thread ID
        return 0;
    }

}
