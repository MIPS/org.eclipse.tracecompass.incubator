package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.symbols.IMappingFile;
import org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Symbol provider for Shinro traces
 */
public class ShinroSymbolProvider implements ISymbolProvider {
    private final @NonNull ITmfTrace fTrace;

    private final @NonNull List<@NonNull IMappingFile> fMappingFiles = new ArrayList<>();



    /**
     * Constructor that is parameter-wise consistent with superclass
     *
     * @param trace
     */
    public ShinroSymbolProvider(ShinroProfilingTrace trace) {
        fTrace = trace;
        // TODO: initialize fMappingFiles to refer to an ELF file that is in a known place
        //  relative to the trace
        IResource resourceTrace = fTrace.getResource();
        String pathTrace = fTrace.getPath();
        System.out.println(resourceTrace);
        System.out.println(pathTrace);
    }

    @Override
    public @NonNull ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void loadConfiguration(@Nullable IProgressMonitor monitor) {
        // Do nothing because the resolved symbols are already stored in
        // fMappingFiles
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(long address) {
        return getSymbol(address, fMappingFiles);
    }

    private static @Nullable TmfResolvedSymbol getSymbol(long address, List<IMappingFile> mappingFiles) {
        TmfResolvedSymbol currentFloorEntry = null;
        for (IMappingFile mf : mappingFiles) {
            TmfResolvedSymbol floorEntry = mf.getSymbolEntry(address);
            if (floorEntry == null) {
                continue;
            }
            long floorValue = floorEntry.getBaseAddress();
            // A symbol may come from different file, prioritize the symbol
            // closest to value
            if (floorValue == address) {
                return floorEntry;
            }
            if (currentFloorEntry == null) {
                currentFloorEntry = floorEntry;
            } else {
                if (address - floorValue < address - currentFloorEntry.getBaseAddress()) {
                    currentFloorEntry = floorEntry;
                }
            }
        }
        if (currentFloorEntry == null) {
            return null;
        }
        return currentFloorEntry;
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(int pid, long timestamp, long address) {
        // First look at the mapping files that are specific for the process
        TmfResolvedSymbol symbol = getSymbol(address, fMappingFiles.stream()
                .filter(mf -> mf.getPid() == pid)
                .collect(Collectors.toList()));
        if (symbol != null) {
            return symbol;
        }

        // The symbol was not found, look in global mapping files
        symbol = getSymbol(address, fMappingFiles.stream()
                .filter(mf -> mf.getPid() < 0)
                .collect(Collectors.toList()));

        return symbol;


        /* This was the original logic borrowed from DefaultSymbolProvider:

        String resourceName = getTrace().getResource().getName();
        String name = resourceName + " " + Long.toHexString(address);
        return new TmfResolvedSymbol(address, name);
        */
    }




}
