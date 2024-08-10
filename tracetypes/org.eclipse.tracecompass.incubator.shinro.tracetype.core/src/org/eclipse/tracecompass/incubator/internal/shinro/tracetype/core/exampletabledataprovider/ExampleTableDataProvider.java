package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.TmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTableDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;


/**
 *
 */
public class ExampleTableDataProvider extends AbstractTmfTableDataProvider implements ITmfVirtualTableDataProvider<@NonNull ExampleTableColumnDataModel, @NonNull ExampleTableLine> {
    public static final String ID = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider"; //$NON-NLS-1$

    ExampleTableDataAnalysis fModule;

    //private static final AtomicLong TRACE_IDS = new AtomicLong();
    // private final long fBogusId = TRACE_IDS.getAndIncrement();

    public static @Nullable ExampleTableDataProvider create(ITmfTrace trace) {
        ExampleTableDataAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ExampleTableDataAnalysis.class, ExampleTableDataAnalysis.ID);
        if (module != null) {
            module.schedule();
            return new ExampleTableDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private ExampleTableDataProvider(ITmfTrace trace, ExampleTableDataAnalysis module) {
        super(trace);
        fModule = module;
    }


    /*
    static private OutputElementStyle getStyle(Object callsite) {
        var style = FlameDefaultPalette.getStyleFor(callsite);
        return style;
    }
    */

     TmfVirtualTableModel<@NonNull ExampleTableLine> getFakeData(VirtualTableQueryFilter queryFilter) {
        List<Long> columnIds = queryFilter.getColumnsId();

        ArrayList<ExampleTableLine> lines = new ArrayList<>();

        long startRow = queryFilter.getIndex();
        for (long row = startRow; row < startRow + queryFilter.getCount(); row++) {
            ArrayList<VirtualTableCell> cells = new ArrayList<>();
            var it = queryFilter.getColumnsId().iterator();
            while (it.hasNext()) {
                long col = it.next();
                VirtualTableCell cell = new VirtualTableCell(String.format("cell(%d,%d)", row, col));
                cells.add(cell);
            }
            ExampleTableLine line = new ExampleTableLine(row, cells);
            lines.add(line);
        }
        TmfVirtualTableModel<@NonNull ExampleTableLine> model = new TmfVirtualTableModel<>(columnIds, lines, queryFilter.getIndex(), getTrace().getNbEvents());
        return model;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TmfTreeModel<@NonNull ExampleTableColumnDataModel>> fetchTree(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<ExampleTableColumnDataModel> model = new ArrayList<>();
        for (int idx = 0; idx < 5; idx++) {
            var labels = new ArrayList<String>();
            labels.add("tree model col " + idx);
            ExampleTableColumnDataModel entry = new ExampleTableColumnDataModel(idx, idx == 0 ? -1 : 0, labels);
            model.add(entry);
        }
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), model), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull ITmfVirtualTableModel<@NonNull ExampleTableLine>> fetchLines(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        VirtualTableQueryFilter queryFilter = FetchParametersUtils.createVirtualTableQueryFilter(fetchParameters);
        if (queryFilter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        TmfVirtualTableModel<@NonNull ExampleTableLine> model = getFakeData(queryFilter);
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
