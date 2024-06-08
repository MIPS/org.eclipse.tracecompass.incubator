package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 *
 */
public class ShinroFakeAnalysisModule extends TmfStateSystemAnalysisModule {



    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) {
        // TODO Auto-generated method stub
        return super.executeAnalysis(monitor);
    }

    @Override
    protected void canceling() {
        // TODO Auto-generated method stub
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return new StateProvider(getTrace(), "id");
    }


}

class StateProvider extends AbstractTmfStateProvider {

    String id;
    long accum = 0;


    public StateProvider(ITmfTrace trace, @NonNull String id) {
        super(trace, id);
        this.id = id;
    }

    @Override
    public int getVersion() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new StateProvider(getTrace(), id);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        var ssb = getStateSystemBuilder();
        if (ssb == null) {
            return;
        }
        int picosecondsQuark = ssb.getQuarkAbsoluteAndAdd("picoseconds");
        if (event.getName().equals("scheduler_picoseconds")) {
            ITmfEventField field = event.getType().getRootField().getField("value");
            accum += (Long)field.getValue();
            ssb.modifyAttribute(event.getTimestamp().getValue(), accum, picosecondsQuark);
            System.out.println("Setting attribute value to " + accum + " at timestamp " + event.getTimestamp().getValue());

            // temporary readback code, to understand better how all this stuff works
            ArrayList<Integer> entries = new ArrayList<>();
            entries.add(0);
            ArrayList<Long> times = new ArrayList<>();
            times.add(event.getTimestamp().getValue());

            try {
                for (ITmfStateInterval interval : ssb.query2D(entries, times)) {
                    System.out.println("In eventHandle, ssb is " + ssb);
                    System.out.println(interval);
                }
            } catch (IndexOutOfBoundsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TimeRangeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (StateSystemDisposedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // ssb.modifyAttribute(event.getTimestamp().getValue(), Long.valueOf(1), quark);
    }

}
