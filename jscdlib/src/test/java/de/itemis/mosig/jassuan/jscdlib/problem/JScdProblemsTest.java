package de.itemis.mosig.jassuan.jscdlib.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class JScdProblemsTest {

    @EnumSource(JScdProblems.class)
    @ParameterizedTest
    public void from_known_error_creates_correct_instance(JScdProblems problem) {
        assertThat(JScdProblems.fromError(problem.errorCode())).isSameAs(problem);
    }

    @EnumSource(JScdProblems.class)
    @ParameterizedTest
    public void all_problems_have_names(JScdProblems problem) {
        assertThat(problem.name()).isNotNull();
    }

    @EnumSource(JScdProblems.class)
    @ParameterizedTest
    public void all_problems_have_descriptions(JScdProblems problem) {
        assertThat(problem.description()).isNotNull();
    }

    @Test
    public void test_error_codes_are_unique() {
        Set<Long> errorCodes = new HashSet<>();

        JScdProblems[] knownProblems = JScdProblems.values();
        for (JScdProblems problem : knownProblems) {
            errorCodes.add(problem.errorCode());
        }

        assertThat(errorCodes.size()).as("At least one problem error code is a duplicate.").isEqualTo(knownProblems.length);
    }

    @Test
    public void fromError_with_unknown_error_throws() {
        long unknownErrorCode = -1;
        assertThatThrownBy(() -> JScdProblems.fromError(unknownErrorCode)).isInstanceOf(JScdException.class).hasFieldOrPropertyWithValue("problem",
            JScdProblems.UNKNOWN_ERROR_CODE).hasMessageContaining("0x" + Long.toHexString(unknownErrorCode).toUpperCase());
    }

    @EnumSource(JScdProblems.class)
    @ParameterizedTest
    public void test_toString(JScdProblems problem) {
        assertThat(problem.toString()).as("toString: Encountered unexpected result.")
            .isEqualTo(problem.errorName() + " (0x" + Long.toHexString(problem.errorCode()).toUpperCase() + ")");
    }
}


