package de.itemis.mosig.jassuan.jscdlib.problem;

import static de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems.SCARD_F_UNKNOWN_ERROR;
import static java.lang.Long.toHexString;
import static java.util.Objects.requireNonNull;

public final class JScdException extends RuntimeException {

    private static final long serialVersionUID = -233131473028185622L;

    private final JScdProblem problem;

    public JScdException() {
        super(SCARD_F_UNKNOWN_ERROR.errorName() + "(0x" + toHexString(SCARD_F_UNKNOWN_ERROR.errorCode()).toUpperCase() + "): "
            + SCARD_F_UNKNOWN_ERROR.description());
        this.problem = SCARD_F_UNKNOWN_ERROR;
    }

    public JScdException(JScdProblem problem) {
        super(requireNonNull(problem, "problem").errorName() + "(0x" + toHexString(problem.errorCode()).toUpperCase() + "): "
            + problem.description());
        this.problem = problem;
    }

    public JScdException(JScdProblem problem, String appendix) {
        super(requireNonNull(problem, "problem").errorName() + "(0x" + toHexString(problem.errorCode()).toUpperCase() + "): "
            + problem.description() + " - " + appendix);
        this.problem = problem;
    }

    public JScdProblem problem() {
        return problem;
    }
}
