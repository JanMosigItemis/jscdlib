package de.itemis.mosig.jassuan.jscdlib.problem;

import static de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems.SCARD_F_INTERNAL_ERROR;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;

public final class JScdException extends RuntimeException {

    private static final long serialVersionUID = -233131473028185622L;

    private static final Function<JScdProblem, String> MESSAGE_FROM_PROBLEM_SUPPLIER = problem -> problem + ": " + problem.description();
    private static final JScdProblem DEFAULT_PROBLEM = SCARD_F_INTERNAL_ERROR;
    private static final String DEFAULT_MESSAGE = MESSAGE_FROM_PROBLEM_SUPPLIER.apply(DEFAULT_PROBLEM);

    private final JScdProblem problem;

    public JScdException() {
        super(DEFAULT_MESSAGE);
        this.problem = DEFAULT_PROBLEM;
    }

    public JScdException(Throwable cause) {
        super(
            DEFAULT_MESSAGE + " - " + requireNonNull(cause, "cause").getClass().getSimpleName() + ": " + (cause.getMessage() == null ? "No further information"
                : cause.getMessage()),
            cause);
        this.problem = DEFAULT_PROBLEM;
    }

    public JScdException(JScdProblem problem) {
        super(MESSAGE_FROM_PROBLEM_SUPPLIER.apply(requireNonNull(problem, "problem")));
        this.problem = problem;
    }

    public JScdException(JScdProblem problem, String appendix) {
        super(MESSAGE_FROM_PROBLEM_SUPPLIER.apply(requireNonNull(problem, "problem")) + " - " + appendix);
        this.problem = problem;
    }



    public JScdProblem problem() {
        return problem;
    }
}
