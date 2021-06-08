package de.itemis.mosig.jassuan.jscdlib.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import jdk.incubator.foreign.CLinker;

public class StringPointerSegmentTest {

    private StringPointerSegment underTest;

    @BeforeEach
    public void setUp() {
        underTest = new StringPointerSegment();
    }

    @AfterEach
    public void tearDown() {
        if (underTest != null) {
            underTest.close();
        }
    }

    @Test
    public void dereference_returns_string_the_segment_points_to() {
        var testString = "testString";
        var seg = CLinker.toCString(testString, StandardCharsets.UTF_8);
        underTest.pointTo(seg.address());

        assertThat(underTest.dereference()).isEqualTo(testString);
    }
}
