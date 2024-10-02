package org.eclipse.tracecompass.incubator.internal.shinro.tracetype.core.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroProfilingEvent;
import org.eclipse.tracecompass.incubator.shinro.tracetype.core.ShinroProfilingTrace;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 *
 */
public class ShinroProfilingCallStackAnalysis extends CallStackAnalysis {

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof ShinroProfilingTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    public ShinroProfilingTrace getTrace() {
        return (ShinroProfilingTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new ShinroProfilingCallStackProvider(checkNotNull(getTrace()));
    }

}

class ShinroProfilingCallStackProvider extends CallStackStateProvider {

    static final int MATCH_JAL = 0x6f;
    static final int MASK_JAL = 0x7f;
    static final int MATCH_JALR = 0x67;
    static final int MASK_JALR = 0x707f;
    static final int MATCH_C_JAL = 0x2001;
    static final int MASK_C_JAL = 0xe003;
    static final int MATCH_C_JALR = 0x9002;
    static final int MASK_C_JALR = 0xf07f;
    static final int MATCH_C_JR = 0x8002;
    static final int MASK_C_JR = 0xf07f;

    int depth = 0;
    int count = 0;
    boolean pushing = true;

    public ShinroProfilingCallStackProvider(ITmfTrace trace) {
        super(trace);
    }

    @Override
    public int getVersion() {
        // TODO: figure out what's best; 0 is probably OK to start
        return 0;
    }

    @Override
    public @NonNull CallStackStateProvider getNewInstance() {
        // TODO Auto-generated method stub
        return new ShinroProfilingCallStackProvider(getTrace());
    }

    @Override
    protected boolean considerEvent(@NonNull ITmfEvent event) {
        return event instanceof ShinroProfilingEvent;
    }

    void checkRiscvInstruction(long opcode, boolean [] isCall, boolean [] isReturn) {
        /*
        static final int MATCH_JAL = 0x6f;
        static final int MASK_JAL = 0x7f;
        static final int MATCH_JALR = 0x67;
        static final int MASK_JALR = 0x707f;
        static final int MATCH_C_JAL = 0x2001;
        static final int MASK_C_JAL = 0xe003;
        static final int MATCH_C_JALR = 0x9002;
        static final int MASK_C_JALR = 0xf07f;
        static final int MATCH_C_JR = 0x8002;
        static final int MASK_C_JR = 0xf07f;
        */
        isCall[0] = false;
        isReturn[0] = false;

        if ((opcode & MASK_JAL) == MATCH_JAL) {
            int rd = ((int)opcode >> 7) & 0x1F;
            isCall[0] = rd == 1 || rd == 5;
        } else if ((opcode & MASK_JALR) == MATCH_JALR) {
            int rd = ((int)opcode >> 7) & 0x1F;
            int rs1 = ((int)opcode >> 15) & 0x1F;
            boolean rd_is_link = rd == 1 || rd == 5;
            boolean rs1_is_link = rs1 == 1 || rs1 == 5;
            boolean rd_and_rs1_equal = rd == rs1;
            isCall[0] = rd_is_link && (!rs1_is_link || rd_and_rs1_equal);
            isReturn[0] = !rd_is_link && rs1_is_link;
        } else if ((opcode & MASK_C_JAL) == MATCH_C_JAL) {
            // rd is implicitly 1, so it's always a call
            isCall[0] = true;
        } else if ((opcode & MASK_C_JALR) == MATCH_C_JALR) {
            int rd = 1;  // implicit
            int rs1 = ((int)opcode >> 7) & 0x1F;
            boolean rd_is_link = rd == 1 || rd == 5;
            boolean rs1_is_link = rs1 == 1 || rs1 == 5;
            boolean rd_and_rs1_equal = rd == rs1;
            isCall[0] = rd_is_link && (!rs1_is_link || rd_and_rs1_equal);
            isReturn[0] = !rd_is_link && rs1_is_link;
        } else if ((opcode & MASK_C_JR) == MATCH_C_JR) {
            int rs1 = ((int)opcode >> 7) & 0x1F;
            boolean rs1_is_link = rs1 == 1 || rs1 == 5;
            isReturn[0] = !rs1_is_link && rs1_is_link;
        }
        System.out.println(String.format("opcode is 0x%x, isCall is %s, isReturn is %s", opcode, isCall[0] ? "true" : "false", isReturn[0] ? "true" : "false"));
    }

    @Override
    protected @Nullable ITmfStateValue functionEntry(@NonNull ITmfEvent event) {
        // Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        if (!(event instanceof ShinroProfilingEvent)) {
            return null;
        }
        long rank = event.getRank();
        TmfStateValue stateValue = null;
        ShinroProfilingEvent ourEvent = (ShinroProfilingEvent)event;
        ITmfEventField opcodeField = ourEvent.getContent().getField("opcode");
        long opcodeValue = (Long)opcodeField.getValue();
        boolean [] isCall = { false };
        boolean [] isReturn = { false };
        checkRiscvInstruction(opcodeValue, isCall, isReturn);
        if (isCall[0]) {
            ShinroProfilingTrace ourTrace = (ShinroProfilingTrace)ourEvent.getTrace();
            long destAddr = ourTrace.getEventAddress(rank+1);
            stateValue = TmfStateValue.newValueLong(destAddr);
        }

        return stateValue;
    }

    @Override
    protected @Nullable ITmfStateValue functionExit(@NonNull ITmfEvent event) {
        // Does the event parameter represent a function entry? If so, return a Long state
        // value representing an address
        TmfStateValue stateValue = null;
        ShinroProfilingEvent ourEvent = (ShinroProfilingEvent)event;
        ITmfEventField rootField = ourEvent.getContent();
        ITmfEventField opcodeField = rootField.getField("opcode");
        long opcodeValue = (Long)opcodeField.getValue();
        boolean [] isCall = { false };
        boolean [] isReturn = { false };
        checkRiscvInstruction(opcodeValue, isCall, isReturn);
        if (isReturn[0]) {
            ITmfEventField addrField = rootField.getField("inst_addr");
            BigInteger addrValue = (BigInteger)addrField.getValue();
            stateValue = TmfStateValue.newValueLong(addrValue.longValue());
        }
        return stateValue;
    }

    @Override
    protected int getProcessId(@NonNull ITmfEvent event) {
        // TODO: this probably isn't relevant for Shinro traces; 0 is probably fine
        return 0;
    }

    @Override
    protected long getThreadId(@NonNull ITmfEvent event) {
        // TODO: this probably isn't relevant for Shinro traces; 0 is probably fine
        return 0;
    }

}
