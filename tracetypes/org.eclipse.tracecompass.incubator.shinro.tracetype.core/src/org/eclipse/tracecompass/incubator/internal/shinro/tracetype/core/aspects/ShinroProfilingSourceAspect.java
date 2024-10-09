package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.aspects;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroProfilingTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 *
 */
public class ShinroProfilingSourceAspect implements ITmfEventAspect<TmfCallsite> {

    /** Singleton instance */
    public static final ShinroProfilingSourceAspect INSTANCE = new ShinroProfilingSourceAspect();

    @Override
    public @NonNull String getName() {
        return nullToEmptyString(Messages.ShinroProfilingTrace_SourceAspectName);
    }

    @Override
    public @NonNull String getHelpText() {
        return nullToEmptyString(Messages.ShinroProfilingTrace_SourceAspectHelpText);
    }

    @Override
    public @Nullable TmfCallsite resolve(@NonNull ITmfEvent event) {
        /* This aspect only supports Shinro profiling traces */
        if (!(event.getTrace() instanceof ShinroProfilingTrace)) {
            return null;
        }
        return null;
    }

}
