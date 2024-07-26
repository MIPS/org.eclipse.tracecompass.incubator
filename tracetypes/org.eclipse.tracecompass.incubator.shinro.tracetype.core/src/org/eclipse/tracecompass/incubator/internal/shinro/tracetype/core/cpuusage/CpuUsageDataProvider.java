package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 *
 */
public class CpuUsageDataProvider extends AbstractTreeCommonXDataProvider<@NonNull CpuUsageAnalysis, @NonNull CpuUsageEntryModel> {
    /**
     * @param trace
     * @param analysis
     */
    public CpuUsageDataProvider(ITmfTrace trace, CpuUsageAnalysis analysis) {
        super(trace, analysis);
    }

    @Override
    public @NonNull String getId() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    protected @Nullable Collection<@NonNull IYModel> getYSeriesModels(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected @NonNull String getTitle() {
        // TODO Auto-generated method stub
        return "title";
    }

    @Override
    protected boolean isCacheable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull CpuUsageEntryModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // TODO Auto-generated method stub
        return (TmfTreeModel<@NonNull CpuUsageEntryModel>)new Object();
    }
}
