package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

public class ShinroProfilingEventType extends TmfEventType {
    /**
     * @param eventName
     */
    public ShinroProfilingEventType(@NonNull String eventName) {
        super(eventName, new ShinroProfilingEventField("rootfield", Long.valueOf(0x12345678L), null));
    }
}
