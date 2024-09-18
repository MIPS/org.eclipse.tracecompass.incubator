package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

public class ShinroProfilingEventField extends TmfEventField {
    public ShinroProfilingEventField(String name, Object value, ITmfEventField [] fields) {
        super(name, value, fields);
    }
}
