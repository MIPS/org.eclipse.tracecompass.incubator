package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

public class ShinroProfilingEventType extends TmfEventType {
    public ShinroProfilingEventType(String eventName) {
        super(eventName, new ShinroProfilingEventField("rootfield", Long.valueOf(0x12345678L), null));
    }
}
