package org.eclipse.tracecompass.incubator.shinro.tracetype.core;

public class QuotedString {
    String str;
    public QuotedString(String str) {
        this.str = str;
    }
    @Override
    public String toString() {
        return "\"" + str + "\"";
    }
}
