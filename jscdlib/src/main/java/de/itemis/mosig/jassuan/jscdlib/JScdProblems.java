package de.itemis.mosig.jassuan.jscdlib;

import static java.util.Objects.requireNonNull;

public enum JScdProblems
        implements
        JScdProblem {

    /**
     * 0x0 - Success
     */
    SCARD_S_SUCCESS(
            0x0,
            "Success"),

    /**
     * 0x8010002E - Group contains no readers
     */
    SCARD_E_NO_READERS_AVAILABLE(
            0x8010002E,
            "Group contains no readers"),

    /**
     * 0x80100017 - Specified reader is not currently available for use
     */
    SCARD_E_READER_UNAVAILABLE(
            0x80100017,
            "Specified reader is not currently available for use"),

    /**
     * 0x80100006 - Not enough memory available to complete this command.
     */
    SCARD_E_NO_MEMORY(
            0x80100006,
            "Not enough memory available to complete this command."),

    /**
     * 0x80100014 - An internal error has been detected, but the source is unknown.
     */
    SCARD_F_UNKNOWN_ERROR(
            0x80100014,
            "An internal error has been detected, but the source is unknown.");

    private final long errorCode;
    private final String description;

    private JScdProblems(long errorCode, String description) {
        this.errorCode = requireNonNull(errorCode);
        this.description = requireNonNull(description);
    }

    @Override
    public long errorCode() {
        return errorCode;
    }

    @Override
    public String errorName() {
        return name();
    }

    @Override
    public String description() {
        return description;
    }
}
