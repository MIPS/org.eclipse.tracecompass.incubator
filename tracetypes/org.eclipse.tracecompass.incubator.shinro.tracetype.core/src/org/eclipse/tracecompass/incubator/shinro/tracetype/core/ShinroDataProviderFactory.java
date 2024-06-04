package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.AbstractTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class ShinroDataProviderFactory implements IDataProviderFactory {

    public ShinroDataProviderFactory() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        if (trace instanceof TmfExperiment) {
            return createProviderLocal(trace);
        }
        return null;
    }

    private static @Nullable DataProvider createProviderLocal (@NonNull ITmfTrace trace) {
        Iterator<ShinroFakeAnalysisModule> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, ShinroFakeAnalysisModule.class).iterator();
        while (modules.hasNext()) {
            ShinroFakeAnalysisModule first = modules.next();
            first.schedule();
            return new DataProvider(trace, first);
        }
        return null;
    }

    @Override
    public @NonNull Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        var descriptorList = new ArrayList<IDataProviderDescriptor>();
        var builder = new DataProviderDescriptor.Builder();
        builder.setId(DataProvider.ID);
        builder.setDescription("This would be a description for the Shinro trace custom view");
        builder.setName("Shinro trace custom view");
        builder.setProviderType(ProviderType.DATA_TREE);
        descriptorList.add(builder.build());
        return descriptorList;
    }
}

class DataProvider extends AbstractTreeDataProvider<@NonNull ShinroFakeAnalysisModule, @NonNull DataModel> {
    static final String ID = "org.eclipse.tracecompass.incubator.shinro.dataprovider";

    public DataProvider(ITmfTrace trace, ShinroFakeAnalysisModule module) {
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
    protected @NonNull TmfTreeModel<@NonNull DataModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {

        ImmutableList<String> header = ImmutableList.of("col1", "col2");
        DataModel dm = new DataModel();
        ImmutableList<DataModel> dmList = ImmutableList.of(dm);
        return new TmfTreeModel<@NonNull DataModel>(header, dmList);
    }
}

class DataModel extends TmfTreeDataModel {
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    static long makeId() {
        return ENTRY_ID.getAndIncrement();
    }

    public DataModel() {
        super(makeId(), -1, "Shinro tree data model");
    }

}
