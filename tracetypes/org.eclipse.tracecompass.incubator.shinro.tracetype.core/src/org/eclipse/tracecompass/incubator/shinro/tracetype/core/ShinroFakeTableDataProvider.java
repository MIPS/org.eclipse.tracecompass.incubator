package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.model.tree.AbstractTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
class ShinroFakeTableDataProvider extends AbstractTreeDataProvider<@NonNull ShinroFakeAnalysisModule, @NonNull ShinroFakeTableDataModel> implements ITmfVirtualTableDataProvider<@NonNull ShinroFakeTableDataModel, @NonNull ShinroFakeTableDataModel> {
    static final String ID = "org.eclipse.tracecompass.incubator.shinro.dataprovider.table.fake";

    public ShinroFakeTableDataProvider(ITmfTrace trace, ShinroFakeAnalysisModule module) {
        super(trace, module);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }


    @Override
    public @NonNull TmfModelResponse<@NonNull ITmfVirtualTableModel<@NonNull ShinroFakeTableDataModel>> fetchLines(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        var bogus = (TmfModelResponse<@NonNull ITmfVirtualTableModel<@NonNull ShinroFakeTableDataModel>>)(new Object());
        return bogus;
    }

    @Override
    protected boolean isCacheable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull ShinroFakeTableDataModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        ImmutableList<String> header = ImmutableList.of("col1", "col2");
        ShinroFakeTableDataModel dm = new ShinroFakeTableDataModel();
        ImmutableList<ShinroFakeTableDataModel> dmList = ImmutableList.of(dm);
        return new TmfTreeModel<@NonNull ShinroFakeTableDataModel>(header, dmList);
    }


}