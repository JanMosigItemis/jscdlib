package com.itemis.jassuan.jscdlib.problem;

/**
 * Replacement for C-like error return codes of native library functions.
 */
public interface JScdProblem {

    /**
     * @return A unique {@link Long} that identifies this problem.
     */
    long errorCode();

    /**
     * @return This problem's name.
     */
    String errorName();

    /**
     * @return A meaningful description that may provide users with details on the meaning of this
     *         problem.
     */
    String description();
}
