package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class ResourcesStatusDataProvider extends AbstractTreeCommonXDataProvider<@NonNull ResourcesStatusAnalysis, @NonNull ResourcesStatusEntryModel> {
    public static final String ID = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.resourcesstatus.ResourcesStatusDataProvider"; //$NON-NLS-1$

    private static final AtomicLong TRACE_IDS = new AtomicLong();
    private final long fBogusId = TRACE_IDS.getAndIncrement();

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
    protected @Nullable Collection<@NonNull IYModel> getYSeriesModels(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        return makeFakeData();
    }

    private Collection<@NonNull IYModel> makeFakeData() {
        ArrayList<IYModel> list = new ArrayList<>();
        list.add(new YModel(this.fBogusId, new double[] {1.2, 2.3, 3.4}));
        return list;
    }


    @Override
    protected @NonNull String getTitle() {
        return Objects.requireNonNull(Messages.ResourcesStatusDataProvider_title);
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

        List<ResourcesStatusEntryModel> entryList = new ArrayList<>();
        var builder = new ImmutableList.Builder<String>();
        builder.add("tree item");
        var entry = new ResourcesStatusEntryModel(0, -1, builder.build());
        entryList.add(entry);

        return new TmfTreeModel<>(ImmutableList.of("Column 1", "Column 2", "Column 3"), entryList);
    }

}
