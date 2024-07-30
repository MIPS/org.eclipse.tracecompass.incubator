package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus;

import java.util.List;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;


public class ResourcesStatusEntryModel extends TmfTreeDataModel {
    public ResourcesStatusEntryModel(long id, long parentId, List<String> labels) {
        super(id, parentId, labels);
    }
}
