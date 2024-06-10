package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.AbstractTimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

class ShinroFakeTreeDataProvider extends AbstractTimeGraphDataProvider<@NonNull ShinroFakeAnalysisModule, @NonNull TimeGraphEntryModel> {
    static final String ID = "org.eclipse.tracecompass.incubator.shinro.dataprovider.tree.fake";

    public ShinroFakeTreeDataProvider(ITmfTrace trace, ShinroFakeAnalysisModule module) {
        super(trace, module);
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    protected boolean isCacheable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected @NonNull TmfTreeModel<@NonNull TimeGraphEntryModel> getTree(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {

        //ImmutableList<String> header = ImmutableList.of("col1", "col2");

        /* this was all wrong; only the metadata is represented here; not data */
        /*
        ImmutableList<String> header = ImmutableList.of();
        var builder = new Builder<ShinroFakeTreeDataModel>();
        @NonNull List<@NonNull ITmfStateInterval> intervalList;
        long ts = ss.getStartTime();
        int quarkPicoseconds = ss.optQuarkAbsolute("picoseconds");
        while (ts < ss.getCurrentEndTime()) {
            intervalList = ss.queryFullState(ts);
            ITmfStateInterval interval = intervalList.get(quarkPicoseconds);
            long val = interval.getValueLong();
            var element = new ShinroFakeTreeDataModel(val);
            builder.add(element);
            ts = interval.getEndTime()+1;
        }
    */
        ImmutableList<String> header = ImmutableList.of();
        var builder = new Builder<TimeGraphEntryModel>();
        var element = new TimeGraphEntryModel(0, -1, "picoseconds", ss.getStartTime(), ss.getCurrentEndTime());
        builder.add(element);
        return new TmfTreeModel<@NonNull TimeGraphEntryModel>(header, builder.build());
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // empty list for now
        ImmutableList<ITimeGraphArrow> list = ImmutableList.of();
        var retval = new TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>>(list, ITmfResponse.Status.COMPLETED, "OK");
        return retval;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // empty map for now
        var map = new HashMap<String, String>();
        var retval = new TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>>(map, ITmfResponse.Status.COMPLETED, "OK");
        return retval;

    }

    @Override
    protected @Nullable TimeGraphModel getRowModel(@NonNull ITmfStateSystem ss, @NonNull Map<@NonNull String, @NonNull Object> parameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        // TimeGraphRowModel, TimeGraphState
        @SuppressWarnings("restriction")
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(parameters);
        if (filter == null) {
            return null;
        }
        int quark = ss.optQuarkAbsolute("picoseconds");
        getId(quark);  // this seems to be a necessary step, for stuff below to work
        Map<@NonNull Long, @NonNull Integer> entries = getSelectedEntries(filter);
        Collection<Long> times = getTimes(filter, ss.getStartTime(), ss.getCurrentEndTime());
        ArrayListMultimap<Integer, ITmfStateInterval> intervals = ArrayListMultimap.create();
        System.out.println("in getRowModel:");
        for (ITmfStateInterval interval : ss.query2D(entries.values(), times)) {
            System.out.println(ss);
            System.out.println(interval);
            intervals.put(interval.getAttribute(), interval);
        }
        List<@NonNull ITimeGraphRowModel> rows = new ArrayList<>();

        // TODO: figure out why the time graph state values don't show up graphically in the UI
        for (Map.Entry<Long, Integer> entry : entries.entrySet()) {
            Collection<ITmfStateInterval> states = intervals.get(entry.getValue());
            List<ITimeGraphState> eventList = new ArrayList<>(states.size());
            states.forEach(state -> {
                if (state.getValue() != null) {
                    Long val = state.getValueLong();
                    ITimeGraphState timeGraphState = createTimeGraphState(state, val);
                    System.out.println("adding timeGraphState: " + timeGraphState);
                    eventList.add(timeGraphState);
                }
            });
            eventList.sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
            rows.add(new TimeGraphRowModel(entry.getKey(), eventList));
        }
        return new TimeGraphModel(rows);
    }

    private static ITimeGraphState createTimeGraphState(ITmfStateInterval interval, long val) {
        long startTime = interval.getStartTime();
        long duration = interval.getEndTime() - startTime + 1;
        return new TimeGraphState(startTime, duration, Integer.MIN_VALUE, Long.toString(val));
    }


}