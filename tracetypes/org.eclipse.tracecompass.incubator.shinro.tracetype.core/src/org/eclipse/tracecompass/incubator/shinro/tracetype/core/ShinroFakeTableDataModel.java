package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.IVirtualTableLine;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

@SuppressWarnings("restriction")
class ShinroFakeTableDataModel extends TmfTreeDataModel implements IVirtualTableLine {
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    static long makeId() {
        return ENTRY_ID.getAndIncrement();
    }

    public ShinroFakeTableDataModel() {
        super(makeId(), -1, "Shinro tree data model");
    }

    @Override
    public long getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NonNull List<@NonNull VirtualTableCell> getCells() {
        // TODO Auto-generated method stub
        return new ArrayList<VirtualTableCell>();
    }

}