package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

public class ShinroProfilingEvent extends TmfEvent implements ITmfSourceLookup {

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

    @Override
    public @Nullable ITmfCallsite getCallsite() {
        ITmfEventField rootField = getContent();
        ITmfEventField srcFileField = rootField.getField("srcfile");
        ITmfEventField srcLineField = rootField.getField("srcline");
        int lineNumber = -1;
        if (srcFileField != null && srcLineField != null) {
            String strFile = (String)srcFileField.getValue();
            lineNumber = (Integer)srcLineField.getValue();
            TmfCallsite cs = new TmfCallsite(strFile, (long)lineNumber);
            return cs;
        }
        return null;
    }

}
