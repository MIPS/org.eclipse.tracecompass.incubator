package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import java.util.List;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;


public class ExampleTableColumnDataModel extends TmfTreeDataModel {
    public ExampleTableColumnDataModel(long id, long parentId, List<String> labels) {
        super(id, parentId, labels);
    }
}
