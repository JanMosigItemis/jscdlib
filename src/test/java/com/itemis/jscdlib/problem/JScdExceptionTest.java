package com.itemis.jscdlib.problem;

import static com.itemis.fluffyj.tests.FluffyTestHelper.assertSerialVersionUid;
import static com.itemis.fluffyj.tests.exceptions.ExpectedExceptions.EXPECTED_CHECKED_EXCEPTION;
import static com.itemis.jscdlib.problem.JScdProblems.SCARD_F_INTERNAL_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class JScdExceptionTest {

    private static final JScdProblem EXPECTED_PROBLEM = new JScdProblem() {

        @Override
        public String errorName() {
            return "expectedErrorName";
        }

        @Override
        public long errorCode() {
            return 123L;
        }

        @Override
        public String description() {
            return "expectedDescritpion";
        }
    };

    @Test
    public void no_args_constructor_sets_problem_to_unknown() {
        assertThat(new JScdException().problem()).as("Default problem must be unknown error").isEqualTo(SCARD_F_INTERNAL_ERROR);
    }

    @Test
    public void hasSerialVersionUid() {
        assertSerialVersionUid(JScdException.class);
    }

    @Test
    public void problem_returns_problem() {
        assertThat(new JScdException(EXPECTED_PROBLEM).problem()).isEqualTo(EXPECTED_PROBLEM);
    }

    @Test
    public void message_matches_problem() {
        var underTest = new JScdException(EXPECTED_PROBLEM);

        assertThat(underTest.getMessage())
            .isEqualTo(EXPECTED_PROBLEM + ": " + EXPECTED_PROBLEM.description());
    }

    @Test
    public void constructor_does_not_accept_null_problem() {
        assertThatThrownBy(() -> new JScdException((JScdProblem) null)).isInstanceOf(NullPointerException.class).hasMessage("problem");
    }

    @Test
    public void two_args_constructor_appends_string_to_message() {
        var expectedAppendix = "expectedAppendix";
        var underTest = new JScdException(EXPECTED_PROBLEM, expectedAppendix);

        assertThat(underTest.getMessage())
            .isEqualTo(EXPECTED_PROBLEM + ": " + EXPECTED_PROBLEM.description() + " - " + expectedAppendix);
    }

    @Test
    public void constructor_sets_cause() {
        var underTest = new JScdException(EXPECTED_CHECKED_EXCEPTION);
        assertThat(underTest.getCause()).isSameAs(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void constructor_does_not_accept_null_cause() {
        assertThatThrownBy(() -> new JScdException((Throwable) null)).isInstanceOf(NullPointerException.class).hasMessage("cause");
    }

    @Test
    public void constructor_sets_cause_message_correctly() {
        var underTest = new JScdException(EXPECTED_CHECKED_EXCEPTION);
        assertThat(underTest.getMessage()).isEqualTo(SCARD_F_INTERNAL_ERROR + ": " + SCARD_F_INTERNAL_ERROR.description() + " - "
            + EXPECTED_CHECKED_EXCEPTION.getClass().getSimpleName() + ": " + EXPECTED_CHECKED_EXCEPTION.getMessage());

        NullPointerException npe = new NullPointerException();
        underTest = new JScdException(npe);
        assertThat(underTest.getMessage()).isEqualTo(SCARD_F_INTERNAL_ERROR + ": " + SCARD_F_INTERNAL_ERROR.description() + " - "
            + npe.getClass().getSimpleName() + ": No further information");
    }
}
