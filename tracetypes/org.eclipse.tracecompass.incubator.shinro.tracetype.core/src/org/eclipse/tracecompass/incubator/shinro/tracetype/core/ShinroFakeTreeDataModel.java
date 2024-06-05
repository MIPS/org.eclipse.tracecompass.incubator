package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

class ShinroFakeTreeDataModel extends TmfTreeDataModel {
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    static long makeId() {
        return ENTRY_ID.getAndIncrement();
    }

    public ShinroFakeTreeDataModel() {
        super(makeId(), -1, "Shinro tree data model");
    }

}