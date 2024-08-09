package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.base.FlameDefaultPalette;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.TmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTableDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;


/**
 *
 */
public class ExampleTableDataProvider extends AbstractTmfTableDataProvider implements ITmfVirtualTableDataProvider<@NonNull ExampleTableColumnDataModel, @NonNull ExampleTableLine> {
    public static final String ID = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider.ExampleTableDataProvider"; //$NON-NLS-1$

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



    static private OutputElementStyle getStyle(Object callsite) {
        var style = FlameDefaultPalette.getStyleFor(callsite);
        return style;
    }

    /**
     * @param parameters  - currently unused for the fake data scenario
     */
    TimeGraphModel getFakeData(@NonNull Map<@NonNull String, @NonNull Object> parameters) {
        @NonNull List<ITimeGraphRowModel> rows = new ArrayList<>();

        for (int id = 0; id < 3; id++) {
            List<ITimeGraphState> states = new ArrayList<>();
            Object callsite = new Object();
            var hashcode = callsite.hashCode();
            System.out.println(hashcode);
            OutputElementStyle style = getStyle(callsite);
            TimeGraphState state = new TimeGraphState(0, 11209985, "label", style);
            states.add(state);
            TimeGraphRowModel row = new TimeGraphRowModel(id, states);
            rows.add(row);
        }
        return new TimeGraphModel(rows);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TmfTreeModel<@NonNull ExampleTableColumnDataModel>> fetchTree(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<ExampleTableColumnDataModel> model = new ArrayList<>();
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
        List<Long> columnsIds = new ArrayList<>();



        ArrayList<ExampleTableLine> lines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ArrayList<VirtualTableCell> cells = new ArrayList<>();
            VirtualTableCell cell = new VirtualTableCell("dummy");
            cells.add(cell);
            ExampleTableLine line = new ExampleTableLine(i, cells);
            lines.add(line);
        }
        TmfVirtualTableModel<@NonNull ExampleTableLine> model = new TmfVirtualTableModel<>(columnsIds, lines, queryFilter.getIndex(), getTrace().getNbEvents());
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
