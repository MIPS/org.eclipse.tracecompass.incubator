package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.cpuusage.messages"; //$NON-NLS-1$

    public static @Nullable String CpuUsageDataProvider_title;
    public static @Nullable String CpuUsageProviderFactory_DescriptionText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
