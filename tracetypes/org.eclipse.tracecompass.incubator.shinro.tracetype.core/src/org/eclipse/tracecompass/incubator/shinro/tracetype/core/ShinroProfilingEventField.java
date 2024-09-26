package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

import java.math.BigInteger;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

public class ShinroProfilingEventField extends TmfEventField {
    boolean displayInHex;
    public ShinroProfilingEventField(String name, Object value, boolean displayInHex, ITmfEventField [] fields) {
        super(name, value, fields);
        this.displayInHex = displayInHex;
    }

    private int getRadix() {
        return displayInHex ? 16 : 10;
    }

    @Override
    public String getFormattedValue() {
        if (displayInHex) {
            Object value = this.getValue();
            if (value instanceof Integer) {
                return "0x" + Integer.toString((Integer)value, getRadix()); //$NON-NLS-1$
            } else if (value instanceof Long) {
                return "0x" + Long.toString((Long)value, getRadix()); //$NON-NLS-1$
            } else if (value instanceof BigInteger) {
                return "0x" + ((BigInteger)value).toString(getRadix()); //$NON-NLS-1$
            }
        }
        return super.getFormattedValue();
    }

    @Override
    public String toString() {
        if (getName().equals(ITmfEventField.ROOT_FIELD_ID)) {
            return super.toString();
        }
        String strValue = null;
        Object value = this.getValue();
        if (value == null) {
            System.out.println("got here");
        }
        if (displayInHex) {
            if (value instanceof Integer) {
                strValue = "0x" + Integer.toString((Integer)value, getRadix()); //$NON-NLS-1$
            } else if (value instanceof Long) {
                strValue = "0x" + Long.toString((Long)value, getRadix()); //$NON-NLS-1$
            } else if (value instanceof BigInteger) {
                strValue = "0x" + ((BigInteger)value).toString(getRadix()); //$NON-NLS-1$
            }
        }
        if (strValue == null && value != null) {
            strValue = value.toString();
        }

        return getName() + "=" + strValue; //$NON-NLS-1$
    }
}
