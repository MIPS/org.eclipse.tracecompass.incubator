package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage;

import java.util.List;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;


public class CpuUsageEntryModel extends TmfTreeDataModel {
    public CpuUsageEntryModel(long id, long parentId, List<String> labels) {
        super(id, parentId, labels);
    }
}
