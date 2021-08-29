package com.itemis.jscdlib.problem;

import static com.itemis.jscdlib.problem.JScdProblems.SCARD_F_INTERNAL_ERROR;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * <p>
 * An (unchecked) {@link RuntimeException} that is thrown by many of JScdLib's methods. Instances of
 * this class usually hold a meaningful error message and a {@link JScdProblem} object with further
 * error information.
 * </p>
 */
public final class JScdException extends RuntimeException {

    private static final long serialVersionUID = -233131473028185622L;

    private static final Function<JScdProblem, String> MESSAGE_FROM_PROBLEM_SUPPLIER = problem -> problem + ": " + problem.description();

    /**
     * If no other {@link JScdProblem problem} applies, use this.
     */
    public static final JScdProblem DEFAULT_PROBLEM = SCARD_F_INTERNAL_ERROR;

    /**
     * Message of {@link JScdException#DEFAULT_PROBLEM}.
     */
    public static final String DEFAULT_MESSAGE = MESSAGE_FROM_PROBLEM_SUPPLIER.apply(DEFAULT_PROBLEM);

    private final JScdProblem problem;

    /**
     * Uses {@link #DEFAULT_PROBLEM} and {@link #DEFAULT_MESSAGE}.
     */
    public JScdException() {
        super(DEFAULT_MESSAGE);
        this.problem = DEFAULT_PROBLEM;
    }

    /**
     * Uses {@link #DEFAULT_PROBLEM} and a combination of {@link #DEFAULT_MESSAGE} and
     * {@code cause's} message.
     * 
     * @param cause
     */
    public JScdException(Throwable cause) {
        super(
            DEFAULT_MESSAGE + " - " + requireNonNull(cause, "cause").getClass().getSimpleName() + ": " + (cause.getMessage() == null ? "No further information"
                : cause.getMessage()),
            cause);
        this.problem = DEFAULT_PROBLEM;
    }

    /**
     * Construct an exception out of {@code problem} and {@code problem's} message.
     * 
     * @param problem
     */
    public JScdException(JScdProblem problem) {
        super(MESSAGE_FROM_PROBLEM_SUPPLIER.apply(requireNonNull(problem, "problem")));
        this.problem = problem;
    }

    /**
     * Like {@link #JScdException(JScdProblem)} but appends {@code appendix} to {@code problem's}
     * message.
     * 
     * @param problem
     * @param appendix
     */
    public JScdException(JScdProblem problem, String appendix) {
        super(MESSAGE_FROM_PROBLEM_SUPPLIER.apply(requireNonNull(problem, "problem")) + " - " + appendix);
        this.problem = problem;
    }


    /**
     * @return The problem encapsulated by this exception.
     */
    public JScdProblem problem() {
        return problem;
    }
}
