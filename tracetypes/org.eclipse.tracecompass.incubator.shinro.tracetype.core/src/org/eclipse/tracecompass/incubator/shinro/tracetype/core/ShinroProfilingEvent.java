package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class ShinroProfilingEvent extends TmfEvent {

    public ShinroProfilingEvent() {
        // TODO - it might necessary to assign fType field here.  I observed Trace Compass
        // guts calling this constructor and then seeing null for that field and panicking.
        //  But maybe that was because I didn't have the other constructor defined.  Just leaving this
        //  as a note if problems of that sort re-arise.
        super();
    }

    /**
     * @param trace
     * @param rank
     * @param timestamp
     * @param type
     * @param content
     */
    public ShinroProfilingEvent(ITmfTrace trace, long rank, ITmfTimestamp timestamp,
            ITmfEventType type, ITmfEventField content) {
        super(trace, rank, timestamp, type, content);
    }

    @Override
    public ITmfEventField getContent() {
        // TODO Auto-generated method stub
        return super.getContent();
    }

    @Override
    public @NonNull String getName() {
        // TODO Auto-generated method stub
        return super.getName();
    }

    @Override
    public ITmfEventType getType() {
        // TODO Auto-generated method stub
        return super.getType();
    }

    @Override
    public long getRank() {
        // TODO Auto-generated method stub
        return super.getRank();
    }

    @Override
    public @NonNull ITmfTrace getTrace() {
        // TODO Auto-generated method stub
        return super.getTrace();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /*
    @Override
    public @Nullable ITmfCallsite getCallsite() {
        // temporary scaffolding
        ITmfCallsite cs = new TmfCallsite("/home/gsavin/1.c", 2L);
        return cs;
    }
    */

}
