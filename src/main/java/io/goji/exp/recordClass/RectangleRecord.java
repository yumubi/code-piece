package io.goji.exp.recordClass;

import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.StringBuilder;

public record RectangleRecord(double length, double width) implements Serializable {

    public RectangleRecord {
        StringBuilder builder = new StringBuilder();
        if (length <= 0) {
            builder.append("\nLength must be greater than zero: ").append(length);
        }
        if (width <= 0) {
            builder.append("\nWidth must be greater than zero: ").append(width);
        }
        if (builder.length() > 0) {
            throw new IllegalArgumentException(builder.toString());
        }
    }

}
