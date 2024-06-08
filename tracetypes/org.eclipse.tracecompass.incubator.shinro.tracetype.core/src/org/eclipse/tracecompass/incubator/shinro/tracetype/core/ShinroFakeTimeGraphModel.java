package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.List;

import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;

public class ShinroFakeTimeGraphModel extends TimeGraphModel {
    /**
     * @param rows
     */
    public ShinroFakeTimeGraphModel(List<ITimeGraphRowModel> rows) {
        super(rows);
    }
}
