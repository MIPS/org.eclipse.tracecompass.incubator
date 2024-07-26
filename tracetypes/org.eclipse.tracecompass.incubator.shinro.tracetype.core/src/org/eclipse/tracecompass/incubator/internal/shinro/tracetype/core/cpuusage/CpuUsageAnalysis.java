package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

public class CpuUsageAnalysis extends TmfStateSystemAnalysisModule {

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfStateProvider bogus = (ITmfStateProvider)new Object();
        return bogus;
    }

}
