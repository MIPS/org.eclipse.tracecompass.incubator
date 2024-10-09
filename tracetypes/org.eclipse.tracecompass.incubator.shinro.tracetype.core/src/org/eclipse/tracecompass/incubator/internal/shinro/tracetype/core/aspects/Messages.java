package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.aspects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @author
 * @since
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.aspects.messages"; //$NON-NLS-1$

    public static @Nullable String ShinroProfilingTrace_BinaryAspectName;
    public static @Nullable String ShinroProfilingTrace_BinaryAspectHelpText;
    public static @Nullable String ShinroProfilingTrace_FunctionAspectName;
    public static @Nullable String ShinroProfilingTrace_FunctionAspectHelpText;
    public static @Nullable String ShinroProfilingTrace_SourceAspectName;
    public static @Nullable String ShinroProfilingTrace_SourceAspectHelpText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
