package de.itemis.mosig.jassuan.jscdlib.problem;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

public enum JScdProblems
        implements
        JScdProblem {

    /**
     * 0x8000000000000000 - An operation resulted in an error code unknown to the library.
     */
    UNKNOWN_ERROR_CODE(
            Long.MIN_VALUE,
            "An operation resulted in an error code unknown to the library."),

    /**
     * 0x7FFFFFFFFFFFFFFF - An operation encountered an error in its implementation.
     */
    IMPLEMENTATION_ERROR(
            Long.MAX_VALUE,
            "Encountered an internal implementation error."),

    /**
     * 0x0 - Success
     */
    SCARD_S_SUCCESS(
            0x0L,
            "Success"),

    /**
     * 0x8010002E - Group contains no readers
     */
    SCARD_E_NO_READERS_AVAILABLE(
            0x8010002EL,
            "Group contains no readers"),

    /**
     * 0x80100017 - Specified reader is not currently available for use
     */
    SCARD_E_READER_UNAVAILABLE(
            0x80100017L,
            "Specified reader is not currently available for use"),

    /**
     * 0x80100006 - Not enough memory available to complete this command.
     */
    SCARD_E_NO_MEMORY(
            0x80100006L,
            "Not enough memory available to complete this command."),

    /**
     * 0x80100014 - An internal error has been detected, but the source is unknown.
     */
    SCARD_F_UNKNOWN_ERROR(
            0x80100014L,
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

    public static JScdProblem fromError(long errorCode) {
        var problemCandidates = Arrays.stream(JScdProblems.values()).filter(problem -> {
            return problem.errorCode == errorCode;
        }).toList();

        if (problemCandidates.isEmpty()) {
            throw new JScdException(UNKNOWN_ERROR_CODE, "0x" + Long.toHexString(errorCode).toUpperCase());
        } else if (problemCandidates.size() > 1) {
            throw new JScdException(IMPLEMENTATION_ERROR, "Encountered more than one problems with the same error code.");
        } else {
            return problemCandidates.get(0);
        }
    }

    @Override
    public String toString() {
        return this.errorName() + " (0x" + Long.toHexString(errorCode).toUpperCase() + ")";
    }
}
