package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.exampletabledataprovider.messages"; //$NON-NLS-1$

    /**
     *
     */
    public static @Nullable String ExampleTableDataProvider_title;
    /**
     *
     */
    public static @Nullable String ExampleTableDataProviderFactory_DescriptionText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
