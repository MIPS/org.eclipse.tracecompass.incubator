package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @SuppressWarnings("null")
    public ShinroSymbolProvider(ShinroProfilingTrace trace) {
        fTrace = trace;
        // initialize fMappingFiles to refer to an ELF file that is in a known place
        //  relative to the trace
        String strPathTrace = fTrace.getPath();
        Path pathTrace = Path.of(strPathTrace);
        Path pathElf = lookForElf(pathTrace);
        if (pathElf != null) {
            IMappingFile elfFile = IMappingFile.create(pathElf.toString(), true);
            if (elfFile != null) {
                fMappingFiles.add(elfFile);
            }
        }
    }

    static private String withoutFileExtension(String filename) {
        String result = filename;
        int pos = result.lastIndexOf('.');
        if (pos != -1) {
            result = result.substring(0, pos);
        }
        return result;
    }

    static private Path lookForElf(Path pathTrace) {
        // in the same directory as pathTrace, look for a file with the same name but with the .elf extension
        String strFileNameTrace = pathTrace.getFileName().toString();
        String strFileNameElf = withoutFileExtension(strFileNameTrace) + ".elf";
        Path pathElf = pathTrace.resolveSibling(strFileNameElf);
        if (pathElf.toFile().exists()) {
            return pathElf;
        }
        return null;
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
