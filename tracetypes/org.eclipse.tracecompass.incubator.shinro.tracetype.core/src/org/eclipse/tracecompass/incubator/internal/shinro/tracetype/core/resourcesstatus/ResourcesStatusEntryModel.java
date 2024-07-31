package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus;

import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;


public class ResourcesStatusEntryModel extends TimeGraphEntryModel {
    /**
     * @param id
     * @param parentId
     * @param name
     * @param startTime
     * @param endTime
     * @param hasRowModel
     */
    public ResourcesStatusEntryModel(long id, long parentId, String name, long startTime, long endTime, boolean hasRowModel) {
        super(id, parentId, name, startTime, endTime, hasRowModel);
    }
}
