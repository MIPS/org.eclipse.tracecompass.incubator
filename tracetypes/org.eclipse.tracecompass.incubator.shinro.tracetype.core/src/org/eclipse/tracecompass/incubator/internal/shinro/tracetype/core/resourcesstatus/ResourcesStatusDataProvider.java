package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class ResourcesStatusDataProvider extends AbstractTimeGraphDataProvider<@NonNull ResourcesStatusAnalysis, @NonNull ResourcesStatusEntryModel> {
    public static final String ID = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus.ResourcesStatusDataProvider"; //$NON-NLS-1$

    //private static final AtomicLong TRACE_IDS = new AtomicLong();
    // private final long fBogusId = TRACE_IDS.getAndIncrement();

    public static @Nullable ResourcesStatusDataProvider create(ITmfTrace trace) {
        ResourcesStatusAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ResourcesStatusAnalysis.class, ResourcesStatusAnalysis.ID);
        if (module != null) {
            module.schedule();
            return new ResourcesStatusDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private ResourcesStatusDataProvider(ITmfTrace trace, ResourcesStatusAnalysis module) {
        super(trace, module);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }


    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull ResourcesStatusEntryModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfTreeModel<>(Collections.emptyList(), Collections.emptyList());
        }

        //long end = filter.getEnd();

        // TODO: figure out what start and end times should be.  Should they be the values from the
        // filter?  Or should they be from the earliest and latest timestamps in the trace?
        List<ResourcesStatusEntryModel> entryList = new ArrayList<>();
        var entry = new ResourcesStatusEntryModel(0, -1, "model", filter.getStart(), filter.getEnd(), false);
        entryList.add(entry);

        return new TmfTreeModel<>(ImmutableList.of("Column 1", "Column 2", "Column 3"), entryList);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // TODO: consider whether something else should be done here
        return new TmfModelResponse<>(null, Status.COMPLETED, "Not supported"); //$NON-NLS-1$
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // TODO: consider whether something else should be done here
        return new TmfModelResponse<>(null, Status.COMPLETED, "Not supported"); //$NON-NLS-1$
    }

    @Override
    protected @Nullable TimeGraphModel getRowModel(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> parameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        List<ITimeGraphRowModel> rows = new ArrayList<>();
        return new TimeGraphModel(rows);
    }

}
