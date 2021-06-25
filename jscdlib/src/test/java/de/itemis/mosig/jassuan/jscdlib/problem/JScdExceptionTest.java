package de.itemis.mosig.jassuan.jscdlib.problem;

import static de.itemis.mosig.fluffy.tests.java.FluffyTestHelper.assertSerialVersionUid;
import static de.itemis.mosig.jassuan.jscdlib.problem.JScdProblems.SCARD_F_UNKNOWN_ERROR;
import static java.lang.Long.toHexString;
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
        assertThat(new JScdException().problem()).as("Default problem must be unknown error").isEqualTo(SCARD_F_UNKNOWN_ERROR);
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
            .isEqualTo(EXPECTED_PROBLEM.errorName() + "(0x" + toHexString(EXPECTED_PROBLEM.errorCode()).toUpperCase() + "): " + EXPECTED_PROBLEM.description());
    }

    @Test
    public void constructor_does_not_accept_null_problem() {
        assertThatThrownBy(() -> new JScdException(null)).isInstanceOf(NullPointerException.class).hasMessage("problem");
    }

    @Test
    public void two_args_constructor_appends_string_to_message() {
        var expectedAppendix = "expectedAppendix";
        var underTest = new JScdException(EXPECTED_PROBLEM, expectedAppendix);

        assertThat(underTest.getMessage())
            .isEqualTo(EXPECTED_PROBLEM.errorName() + "(0x" + toHexString(EXPECTED_PROBLEM.errorCode()).toUpperCase() + "): " + EXPECTED_PROBLEM.description()
                + " - " + expectedAppendix);
    }
}
