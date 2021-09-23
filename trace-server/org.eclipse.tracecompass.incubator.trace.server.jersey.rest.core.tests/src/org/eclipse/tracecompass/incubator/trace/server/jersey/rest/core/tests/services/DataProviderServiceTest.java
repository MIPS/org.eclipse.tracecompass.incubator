/*******************************************************************************
 * Copyright (c) 2018, 2021 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model.views.QueryParameters;
import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.DataProviderService;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.ColumnHeaderEntryStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.DataProviderDescriptorStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.EntryHeaderStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.EntryModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.EntryStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.ExperimentModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.LineModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TableColumnsOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TableLinesOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TableModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TgEntryModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TgStatesOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TgTooltipOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TgTreeOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TimeGraphEntryStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TimeGraphModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TimeGraphRowStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TimeGraphStateStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TreeOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.XyModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.XyOutputResponseStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.XySeriesStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.utils.RestServerTest;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

/**
 * Test the {@link DataProviderService}
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 */
public class DataProviderServiceTest extends RestServerTest {
    private static final int MAX_ITER = 40;
    private static final String CALL_STACK_DATAPROVIDER_ID = "org.eclipse.tracecompass.internal.analysis.profiling.callstack.provider.CallStackDataProvider";
    private static final String XY_DATAPROVIDER_ID = "org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider";
    private static final String EVENTS_TABLE_DATAPROVIDER_ID = "org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableDataProvider";
    private static final String REQUESTED_TIMES_KEY = "requested_times";
    private static final String REQUESTED_ITEMS_KEY = "requested_items";
    private static final String REQUESTED_ELEMENT_KEY = "requested_element";
    private static final String REQUESTED_COLUMN_IDS_KEY = "requested_table_column_ids";
    private static final String REQUESTED_TABLE_INDEX_KEY = "requested_table_index";
    private static final String REQUESTED_TABLE_COUNT_KEY = "requested_table_count";
    private static final String ELEMENT_TYPE = "elementType";
    private static final String STATE = "state";
    private static final String ANNOTATION = "annotation";
    private static final String ARROW = "arrow";
    private static final String TIME = "time";
    private static final String DURATION = "duration";
    private static final String ENTRY_ID = "entryId";
    private static final String DESTINATION_ID = "destinationId";
    private static final long TABLE_INDEX = 0L;
    private static final long TABLE_COUNT = 100L;

    private static final  List<EntryHeaderStub> EXPECTED_XY_TREE_HEADERS = ImmutableList.of(new EntryHeaderStub("Process", ""), new EntryHeaderStub("TID", ""), new EntryHeaderStub("%", ""), new EntryHeaderStub("Time", ""));

    /**
     * Test getting the data provider descriptors
     */
    @Test
    public void testProviders() {
        ExperimentModelStub exp = assertPostExperiment(CONTEXT_SWITCHES_UST_STUB.getName(), CONTEXT_SWITCHES_UST_STUB);

        WebTarget experiments = getApplicationEndpoint().path(EXPERIMENTS);
        WebTarget providers = experiments.path(exp.getUUID().toString())
                .path(OUTPUTS_PATH);

        Set<DataProviderDescriptorStub> descriptors = getDataProviderDescriptors(providers);
        for (DataProviderDescriptorStub desc : EXPECTED_DATA_PROVIDER_DESCRIPTOR) {
            assertTrue(desc.getName(), descriptors.contains(desc));
        }
    }

    /**
     * Ensure that the Call Stack data provider exists for the trace.
     */
    @Test
    public void testCallStackDataProvider() {
        ExperimentModelStub exp = assertPostExperiment(CONTEXT_SWITCHES_UST_STUB.getName(), CONTEXT_SWITCHES_UST_STUB);

        WebTarget callstackTree = getTimeGraphTreeEndpoint(exp.getUUID().toString(), CALL_STACK_DATAPROVIDER_ID);

        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0L, Long.MAX_VALUE, 2));
        Response tree = callstackTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
        assertEquals("There should be a positive response for the data provider", 200, tree.getStatus());

        parameters = new HashMap<>();
        parameters.put(REQUESTED_TIMES_KEY, Collections.emptyList());
        Response defaults = callstackTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
        assertEquals("Default values should return OK code", 200, defaults.getStatus());
    }

    /**
     * Ensure that an XY data provider exists and returns correct data. It does
     * not test the data itself, simply that the serialized fields are the
     * expected ones according to the protocol.
     *
     * @throws InterruptedException
     *             Exception thrown while waiting to execute again
     */
    @Test
    public void testXYDataProvider() throws InterruptedException {
        long start = 1412670961211260539L;
        long end = 1412670967217750839L;
        try {
            ExperimentModelStub exp = assertPostExperiment(ARM_64_KERNEL_STUB.getName(), ARM_64_KERNEL_STUB);

            // Test getting the tree endpoint for an XY chart
            WebTarget xyTree = getXYTreeEndpoint(exp.getUUID().toString(), XY_DATAPROVIDER_ID);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(REQUESTED_TIMES_KEY, ImmutableList.of(start, end));
            TreeOutputResponseStub responseModel;
            Response tree = xyTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, tree.getStatus());
            responseModel = tree.readEntity(TreeOutputResponseStub.class);
            assertNotNull(responseModel);
            tree.close();

            // Make sure the analysis ran enough and we have a model
            int iteration = 0;
            while (responseModel.isRunning() && responseModel.getModel() == null && iteration < MAX_ITER) {
                Thread.sleep(100);
                Response xyResponse = xyTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
                assertEquals("There should be a positive response for the data provider", 200, xyResponse.getStatus());
                responseModel = xyResponse.readEntity(TreeOutputResponseStub.class);
                assertNotNull(responseModel);
                iteration++;
                xyResponse.close();
            }

            // Verify tree model
            EntryModelStub model = responseModel.getModel();
            assertNotNull(model);
            List<EntryHeaderStub> headers = model.getHeaders();
            assertNotNull(headers);
            assertEquals(EXPECTED_XY_TREE_HEADERS.size(), headers.size());
            // Verify tree headers
            for (int i = 0; i < headers.size(); i++ ) {
                EntryHeaderStub header = headers.get(i);
                EntryHeaderStub expHeader = EXPECTED_XY_TREE_HEADERS.get(i);
                assertTrue(expHeader.getName().equals(header.getName()) && expHeader.getTooltip().equals(header.getTooltip()));
            }
            // Verify Entries
            assertNotNull("The model is null, maybe the analysis did not run long enough?" + responseModel, model);
            List<EntryStub> entries = model.getEntries();
            assertFalse(entries.isEmpty());

            // Test getting the XY series endpoint
            WebTarget xySeriesEnpoint = getXYSeriesEndpoint(exp.getUUID().toString(), XY_DATAPROVIDER_ID);
            List<Integer> items = new ArrayList<>();
            for (EntryStub entry : entries) {
                items.add(entry.getId());
            }
            parameters.put(REQUESTED_ITEMS_KEY, items);
            Response series = xySeriesEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, series.getStatus());
            XyOutputResponseStub xyModelResponse = series.readEntity(XyOutputResponseStub.class);
            assertNotNull(xyModelResponse);

            XyModelStub xyModel = xyModelResponse.getModel();
            Set<XySeriesStub> xySeries = xyModel.getSeries();
            assertFalse(xySeries.isEmpty());
            series.close();

        } catch (ProcessingException e) {
            // The failure from this exception alone is not helpful. Use the
            // suppressed exception's message be the failure message for more
            // help debugging failed tests.
            fail(e.getCause().getMessage());
        }
    }

    /**
     * Ensure that a time graph data provider exists and returns correct data.
     * It does not test the data itself, simply that the serialized fields are
     * the expected ones.
     *
     * @throws InterruptedException
     *             Exception thrown while waiting to execute again
     */
    @Test
    public void testTimeGraphDataProvider() throws InterruptedException {
        long start = 1450193697034689597L;
        long end = 1450193745774189602L;
        try {
            ExperimentModelStub exp = assertPostExperiment(CONTEXT_SWITCHES_UST_STUB.getName(), CONTEXT_SWITCHES_UST_STUB);

            // Test getting the time graph tree
            WebTarget callstackTree = getTimeGraphTreeEndpoint(exp.getUUID().toString(), CALL_STACK_DATAPROVIDER_ID);

            Map<String, Object> parameters = new HashMap<>();
            TgTreeOutputResponseStub responseModel;
            parameters.put(REQUESTED_TIMES_KEY, ImmutableList.of(start, end));
            Response treeResponse = callstackTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, treeResponse.getStatus());
            responseModel = treeResponse.readEntity(TgTreeOutputResponseStub.class);
            assertNotNull(responseModel);
            treeResponse.close();

            // Make sure the analysis ran enough and we have a model
            int iteration = 0;
            while ((responseModel.isRunning() || responseModel.getModel() == null) && iteration < MAX_ITER) {
                Thread.sleep(100);
                treeResponse = callstackTree.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
                assertEquals("There should be a positive response for the data provider", 200, treeResponse.getStatus());
                responseModel = treeResponse.readEntity(TgTreeOutputResponseStub.class);
                assertNotNull(responseModel);
                iteration++;
                treeResponse.close();
            }

            TgEntryModelStub model = responseModel.getModel();
            assertNotNull("The model is null, maybe the analysis did not run long enough?" + responseModel, model);
            Set<TimeGraphEntryStub> entries = model.getEntries();
            assertFalse(entries.isEmpty());
            // add entries for the states query, and make sure they don't have
            // extra time fields
            List<Integer> items = new ArrayList<>();
            // Find a specific call stack entry
            for (TimeGraphEntryStub entry : entries) {
                items.add(entry.getId());
            }

            // Test getting the time graph row data
            WebTarget tgStatesEnpoint = getTimeGraphStatesEndpoint(exp.getUUID().toString(), CALL_STACK_DATAPROVIDER_ID);
            parameters.put(REQUESTED_TIMES_KEY, ImmutableList.of(1450193697034689597L, 1450193697118480368L));
            parameters.put(REQUESTED_ITEMS_KEY, items);
            Response statesResponse = tgStatesEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, statesResponse.getStatus());

            TgStatesOutputResponseStub tgStateModelResponse = statesResponse.readEntity(TgStatesOutputResponseStub.class);
            assertNotNull(tgStateModelResponse);

            TimeGraphModelStub tgModel = tgStateModelResponse.getModel();
            Set<TimeGraphRowStub> rows = tgModel.getRows();
            assertFalse(rows.isEmpty());
            statesResponse.close();

            // Test getting the time graph tooltip for a state
            int callstackEntryIdForTooltip = findCallStackEntry(entries);
            TimeGraphRowStub row = null;
            for (Iterator<TimeGraphRowStub> iterator = rows.iterator(); iterator.hasNext();) {
                TimeGraphRowStub timeGraphRowStub = iterator.next();
                if (timeGraphRowStub.getEntryId() == callstackEntryIdForTooltip) {
                    row = timeGraphRowStub;
                }
            }
            assertNotNull(row);
            TimeGraphStateStub state = row.getStates().get(0);
            WebTarget tgTooltipEnpoint = getTimeGraphTooltipEndpoint(exp.getUUID().toString(), CALL_STACK_DATAPROVIDER_ID);
            parameters.put(REQUESTED_ITEMS_KEY, Collections.singletonList(row.getEntryId()));
            parameters.put(REQUESTED_TIMES_KEY, Collections.singletonList(state.getStartTime()));
            parameters.put(REQUESTED_ELEMENT_KEY, ImmutableMap.of(
                    ELEMENT_TYPE, STATE,
                    TIME, state.getStartTime(),
                    DURATION, (state.getEndTime() - state.getStartTime())));
            Response tooltipResponse = tgTooltipEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, tooltipResponse.getStatus());

            TgTooltipOutputResponseStub timegraphTooltipResponse = tooltipResponse.readEntity(TgTooltipOutputResponseStub.class);
            assertNotNull(timegraphTooltipResponse);
            Map<String, String> expectedTooltip = new HashMap<>();
            expectedTooltip.put("Address", "0x804a291");
            expectedTooltip.put("State", "000000000804a291");
            assertEquals(expectedTooltip, timegraphTooltipResponse.getModel());
            tooltipResponse.close();

            // Test getting the time graph tooltip for an annotation
            long time = state.getStartTime();
            parameters.put(REQUESTED_ITEMS_KEY, Collections.singletonList(row.getEntryId()));
            parameters.put(REQUESTED_TIMES_KEY, Collections.singletonList(state.getStartTime()));
            parameters.put(REQUESTED_ELEMENT_KEY, ImmutableMap.of(
                    ELEMENT_TYPE, ANNOTATION,
                    TIME, time,
                    DURATION, 0L,
                    ENTRY_ID, row.getEntryId()));
            tooltipResponse = tgTooltipEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, tooltipResponse.getStatus());

            // Test getting the time graph tooltip for an arrow
            TimeGraphRowStub destinationRow = Iterators.get(rows.iterator(), rows.size() - 2);
            parameters.put(REQUESTED_ITEMS_KEY, Collections.singletonList(row.getEntryId()));
            parameters.put(REQUESTED_TIMES_KEY, Collections.singletonList(state.getStartTime()));
            parameters.put(REQUESTED_ELEMENT_KEY, ImmutableMap.of(
                    ELEMENT_TYPE, ARROW,
                    TIME, time,
                    DURATION, 0L,
                    ENTRY_ID, row.getEntryId(),
                    DESTINATION_ID, destinationRow.getEntryId()));
            tooltipResponse = tgTooltipEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, tooltipResponse.getStatus());

            timegraphTooltipResponse = tooltipResponse.readEntity(TgTooltipOutputResponseStub.class);
            assertNotNull(timegraphTooltipResponse);
            assertEquals(Collections.emptyMap(), timegraphTooltipResponse.getModel());
            tooltipResponse.close();

        } catch (ProcessingException e) {
            // The failure from this exception alone is not helpful. Use the
            // suppressed exception's message be the failure message for more
            // help debugging failed tests.
            fail(e.getCause().getMessage());
        }
    }

    private static int findCallStackEntry(Set<TimeGraphEntryStub> entries) {
        // Find trace entry
        Optional<TimeGraphEntryStub> traceOptional = entries.stream().filter(entry -> entry.getParentId() == -1).findFirst();
        assertTrue(traceOptional.isPresent());
        int traceId = traceOptional.get().getId();

        // Find process
        final String processNameForTooltip = "UNKNOWN";
        Optional<TimeGraphEntryStub> processOptional = entries.stream().filter(
                entry -> processNameForTooltip.equals(entry.getLabels().get(0)) && entry.getParentId() == traceId).findFirst();
        assertTrue(processOptional.isPresent());
        int processEntryId = processOptional.get().getId();

        // Find specific thread entry
        final String threadNameForTooltip = "lemon_server-589";
        Optional<TimeGraphEntryStub> threadOptional = entries.stream().filter(
                entry -> threadNameForTooltip.equals(entry.getLabels().get(0)) && entry.getParentId() == processEntryId).findFirst();
        assertTrue(threadOptional.isPresent());
        int threadId = threadOptional.get().getId();

        // Find first callstack entry under the thread entry
        Optional<TimeGraphEntryStub> callstackOptional = entries.stream().filter(
                entry -> threadNameForTooltip.equals(entry.getLabels().get(0)) && entry.getParentId() == threadId).findFirst();
        assertTrue(callstackOptional.isPresent());
        return callstackOptional.get().getId();
    }

    /**
     * Ensure that a table data provider exists and returns correct data. It
     * does not test the data itself, simply that the serialized fields are the
     * expected ones.
     *
     * @throws InterruptedException
     *             Exception thrown while waiting to execute again
     */
    @Test
    public void testTableDataProvider() throws InterruptedException {
        long start = 1412670961211260539L;
        long end = 1412670967217750839L;
        try {
            ExperimentModelStub exp = assertPostExperiment(ARM_64_KERNEL_STUB.getName(), ARM_64_KERNEL_STUB);

            // Test getting the tree endpoint for an XY chart
            WebTarget tableColumns = getTableColumnsEndpoint(exp.getUUID().toString(), EVENTS_TABLE_DATAPROVIDER_ID);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put(REQUESTED_TIMES_KEY, ImmutableList.of(start, end));
            TableColumnsOutputResponseStub responseModel;
            Response tree = tableColumns.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, tree.getStatus());
            responseModel = tree.readEntity(TableColumnsOutputResponseStub.class);
            assertNotNull(responseModel);
            tree.close();

            // Make sure the analysis ran enough and we have a model
            int iteration = 0;
            while (responseModel.isRunning() && responseModel.getModel() == null && iteration < MAX_ITER) {
                Thread.sleep(100);
                Response xyResponse = tableColumns.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
                assertEquals("There should be a positive response for the data provider", 200, xyResponse.getStatus());
                responseModel = xyResponse.readEntity(TableColumnsOutputResponseStub.class);
                assertNotNull(responseModel);
                iteration++;
                xyResponse.close();
            }

            List<ColumnHeaderEntryStub> columns = responseModel.getModel();
            assertNotNull("The model is null, maybe the analysis did not run long enough?" + responseModel, columns);
            assertFalse(columns.isEmpty());

            // Test getting the XY series endpoint
            WebTarget tableLinesEnpoint = getTableLinesEndpoint(exp.getUUID().toString(), EVENTS_TABLE_DATAPROVIDER_ID);
            List<Long> requestedColumnsIds = new ArrayList<>();
            for (int i = 0; i <= columns.size() / 2; i++) {
                requestedColumnsIds.add(columns.get(i).getId());
            }
            parameters.put(REQUESTED_COLUMN_IDS_KEY, requestedColumnsIds);
            parameters.put(REQUESTED_TABLE_INDEX_KEY, TABLE_INDEX);
            parameters.put(REQUESTED_TABLE_COUNT_KEY, TABLE_COUNT);
            Response linesResponse = tableLinesEnpoint.request().post(Entity.json(new QueryParameters(parameters, Collections.emptyList())));
            assertEquals("There should be a positive response for the data provider", 200, linesResponse.getStatus());
            TableLinesOutputResponseStub lineModelResponse = linesResponse.readEntity(TableLinesOutputResponseStub.class);
            assertNotNull(lineModelResponse);

            TableModelStub tableModel = lineModelResponse.getModel();
            assertNotNull("Table model", tableModel);
            List<LineModelStub> lines = tableModel.getLines();
            // FIXME This assert does not work with current implementation
            // assertEquals("Sizes match", tableModel.getSize(), lines.size());
            assertFalse(lines.isEmpty());
            for (LineModelStub line : lines) {
                assertEquals("Number of returned cells", requestedColumnsIds.size(), line.getCells().size());
            }
            linesResponse.close();

        } catch (ProcessingException e) {
            // The failure from this exception alone is not helpful. Use the
            // suppressed exception's message be the failure message for more
            // help debugging failed tests.
            fail(e.getCause().getMessage());
        }
    }

}
