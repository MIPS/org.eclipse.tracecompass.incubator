package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import java.util.List;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableLine;

public class ExampleTableLine extends VirtualTableLine {
    ExampleTableLine(long index, List<VirtualTableCell> cellData) {
        super(index, cellData);
    }
}
