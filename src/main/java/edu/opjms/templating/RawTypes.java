package edu.opjms.templating;

public enum RawTypes {
    AUTO_DETECT,
    NUMBER,
    TEXT;

    @Override
    public String toString() {
        return switch (this) {
            case AUTO_DETECT:
                yield "Auto Detect";
            case NUMBER:
                yield "Number";
            case TEXT:
                yield "Text";
        };
    }
}
