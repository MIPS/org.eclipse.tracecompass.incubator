package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 *
 */
public class ShinroFakeTreeDataProviderFactory implements IDataProviderFactory {

    public ShinroFakeTreeDataProviderFactory() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        if (trace instanceof TmfExperiment) {
            return createProviderLocal(trace);
        }
        return null;
    }

    private static @Nullable ShinroFakeTreeDataProvider createProviderLocal (@NonNull ITmfTrace trace) {
        Iterator<ShinroFakeAnalysisModule> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, ShinroFakeAnalysisModule.class).iterator();
        while (modules.hasNext()) {
            ShinroFakeAnalysisModule first = modules.next();
            first.schedule();
            return new ShinroFakeTreeDataProvider(trace, first);
        }
        return null;
    }

    @Override
    public @NonNull Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        var descriptorList = new ArrayList<IDataProviderDescriptor>();
        var builder = new DataProviderDescriptor.Builder();
        builder.setId(ShinroFakeTreeDataProvider.ID);
        builder.setDescription("This would be a description for the Shinro trace custom view");
        builder.setName("Shinro trace custom view");
        builder.setProviderType(ProviderType.TIME_GRAPH);
        descriptorList.add(builder.build());
        return descriptorList;
    }
}
