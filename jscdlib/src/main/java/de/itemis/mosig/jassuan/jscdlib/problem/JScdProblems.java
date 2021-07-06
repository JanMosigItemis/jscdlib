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
            "An operation resulted in an error code unknown to the library"),

    /**
     * 0x7FFFFFFFFFFFFFFF - An operation encountered an error in its implementation.
     */
    IMPLEMENTATION_ERROR(
            Long.MAX_VALUE,
            "Encountered an internal implementation error"),

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
            "Not enough memory available to complete this command"),

    /**
     * 0x80100014 - An internal error has been detected.
     */
    SCARD_F_INTERNAL_ERROR(
            0x80100014L,
            "An internal error has been detected"),

    /**
     * 0x20000103 - Connect to assuan server failed.
     */
    GPG_ERR_ASS_CONNECT_FAILED(
            0x20000103L,
            "Connect to assuan server failed."),

    /**
     * 0x2000002E - Encountered a bad URI
     */
    GPG_ERR_BAD_URI(
            0x2000002EL,
            "Encountered a bad URI"),

    /**
     * 0x80100004 - One or more of the supplied parameters could not be properly interpreted.
     */
    SCARD_E_INVALID_PARAMETER(
            0x80100004L,
            "One or more of the supplied parameters could not be properly interpreted"),
    /**
     * 0x6008050 - No device attached to the system.
     */
    GPG_ERR_ENODEV(
            0x6008050L,
            "No device attached to the system");

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
            throw new JScdException(IMPLEMENTATION_ERROR, "Encountered more than one problem with the same error code.");
        } else {
            return problemCandidates.get(0);
        }
    }

    @Override
    public String toString() {
        return this.errorName() + " (0x" + Long.toHexString(errorCode).toUpperCase() + ")";
    }
}
