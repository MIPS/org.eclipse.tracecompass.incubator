/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.perf.profiling.core.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.incubator.callstack.core.base.CallStackElement;
import org.eclipse.tracecompass.incubator.callstack.core.base.CallStackGroupDescriptor;
import org.eclipse.tracecompass.incubator.callstack.core.base.ICallStackElement;
import org.eclipse.tracecompass.incubator.callstack.core.base.ICallStackGroupDescriptor;
import org.eclipse.tracecompass.incubator.callstack.core.sampled.callgraph.ProfilingCallGraphAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * An analysis module for the sampled callchains from a perf trace. It
 * aggregates the data from the sampling events per pid/tid.
 *
 * @author Geneviève Bastien
 */
public class PerfCallchainAnalysisModule extends ProfilingCallGraphAnalysisModule {

    private static final String EVENT_SAMPLING = "cycles"; //$NON-NLS-1$
    private static final String FIELD_PERF_CALLCHAIN = "perf_callchain"; //$NON-NLS-1$
    private static final String FIELD_PERF_PID = "perf_pid"; //$NON-NLS-1$
    private static final String FIELD_PERF_TID = "perf_tid"; //$NON-NLS-1$

    private final CallStackGroupDescriptor fThreadDescriptor;
    private final CallStackGroupDescriptor fProcessDescriptor;
    // private final ProfilingGroup fGroupNode = new ProfilingGroup("Data",
    // CallGraphAllGroupDescriptor.getInstance());

    /**
     * Constructor
     */
    public PerfCallchainAnalysisModule() {
        // Create group descriptors
        fThreadDescriptor = new CallStackGroupDescriptor("Threads", null, false);
        fProcessDescriptor = new CallStackGroupDescriptor("Process", fThreadDescriptor, true);
    }

    @Override
    protected void processEvent(ITmfEvent event) {
        if (!event.getName().startsWith(EVENT_SAMPLING)) {
            return;
        }
        // Get the callchain if available
        ITmfEventField field = event.getContent().getField(FIELD_PERF_CALLCHAIN);
        if (field == null) {
            return;
        }
        long[] value = (long[]) field.getValue();
        int size = value.length;
        long tmp;
        // Reverse the stack so that element at position 0 is the bottom
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            tmp = value[i];
            value[i] = value[j];
            value[j] = tmp;
        }
        ICallStackElement element = getElement(event);
        addStackTrace(element, value);
    }

    /**
     * @param event
     */
    private ICallStackElement getElement(ITmfEvent event) {
        // Find a root elements with the same PID
        Collection<ICallStackElement> rootElements = getRootElements();
        Long pidField = event.getContent().getFieldValue(Long.class, FIELD_PERF_PID);
        Long pid = pidField == null ? -1 : pidField;
        Long tidField = event.getContent().getFieldValue(Long.class, FIELD_PERF_TID);
        Long tid = tidField == null ? -1 : tidField;
        Optional<ICallStackElement> process = rootElements.stream()
                .filter(e -> e.getName().equals(String.valueOf(pid)))
                .findFirst();
        if (!process.isPresent()) {
            // Process is null, create both process and thread elements and return
            ICallStackElement processEl = new CallStackElement(String.valueOf(pid), fProcessDescriptor, fThreadDescriptor, null);
            ICallStackElement threadEl = new CallStackElement(String.valueOf(tid), fThreadDescriptor, null, processEl);
            processEl.addChild(threadEl);
            addRootElement(processEl);
            return threadEl;
        }
        ICallStackElement processEl = process.get();

        // Process exists, find a thread element under it or create it
        Optional<ICallStackElement> thread = processEl.getChildren().stream()
                .filter(e -> e.getName().equals(String.valueOf(tid)))
                .findFirst();

        if (thread.isPresent()) {
            return thread.get();
        }
        ICallStackElement threadEl = new CallStackElement(String.valueOf(tid), fThreadDescriptor, null, processEl);
        processEl.addChild(threadEl);
        return threadEl;

    }

    @Override
    public Collection<ICallStackGroupDescriptor> getGroupDescriptors() {
        return ImmutableList.of(fProcessDescriptor);
    }

    @Override
    public Map<String, Collection<Object>> getCallStack(@NonNull ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(FIELD_PERF_CALLCHAIN);
        if (field == null) {
            return Collections.emptyMap();
        }
        Object value = field.getValue();
        if (!(value instanceof long[])) {
            return Collections.emptyMap();
        }
        long[] callstack = (long[]) value;
        List<Object> longList = new ArrayList<>();
        for (long callsite : callstack) {
            longList.add(callsite);
        }
        Collections.reverse(longList);
        return ImmutableMap.of("Callchain", longList);

    }

}
