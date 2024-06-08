package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

class ShinroFakeTreeDataModel extends TimeGraphEntryModel {
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    static long makeId() {
        return ENTRY_ID.getAndIncrement();
    }

    public ShinroFakeTreeDataModel(String name, long startTime, long endTime) {
        //super(makeId(), -1, name);
        super(makeId(), -1, name, startTime, endTime);
    }

}