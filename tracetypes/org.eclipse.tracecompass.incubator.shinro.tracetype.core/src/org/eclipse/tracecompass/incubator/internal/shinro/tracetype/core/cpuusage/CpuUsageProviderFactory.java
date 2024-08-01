package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public class CpuUsageProviderFactory implements IDataProviderFactory {

    private static final Predicate<? super ITmfTrace> PREDICATE = t -> TmfTraceUtils.getAnalysisModuleOfClass(t, CpuUsageAnalysis.class, CpuUsageAnalysis.ID) != null;

    private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
            .setId(CpuUsageDataProvider.ID)
            .setName(Objects.requireNonNull(Messages.CpuUsageDataProvider_title))
            .setDescription(Objects.requireNonNull(Messages.CpuUsageProviderFactory_DescriptionText))
            .setProviderType(ProviderType.TREE_TIME_XY)
            .build();


    public CpuUsageProviderFactory() {

    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return CpuUsageDataProvider.create(trace);
        }
        return TmfTreeXYCompositeDataProvider.create(traces, Objects.requireNonNull(Messages.CpuUsageDataProvider_title), CpuUsageDataProvider.ID);

    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        return Iterables.any(traces, PREDICATE) ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }



}
