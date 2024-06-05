package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.model.tree.AbstractTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

class ShinroFakeTreeDataProvider extends AbstractTreeDataProvider<@NonNull ShinroFakeAnalysisModule, @NonNull ShinroFakeTreeDataModel> {
    static final String ID = "org.eclipse.tracecompass.incubator.shinro.dataprovider.tree.fake";

    public ShinroFakeTreeDataProvider(ITmfTrace trace, ShinroFakeAnalysisModule module) {
        super(trace, module);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    protected boolean isCacheable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull ShinroFakeTreeDataModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {

        ImmutableList<String> header = ImmutableList.of("col1", "col2");
        ShinroFakeTreeDataModel dm = new ShinroFakeTreeDataModel();
        ImmutableList<ShinroFakeTreeDataModel> dmList = ImmutableList.of(dm);
        return new TmfTreeModel<@NonNull ShinroFakeTreeDataModel>(header, dmList);
    }

}