package de.itemis.mosig.jassuan.jscdlib.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntSegmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(IntSegmentTest.class);

    private IntSegment underTest;

    @BeforeEach
    public void setUp() {
        underTest = new IntSegment();
    }

    @AfterEach
    public void tearDown() {
        if (underTest.isAlive()) {
            try {
                underTest.close();
            } catch (Exception e) {
                LOG.warn("Possible ressource leak. Could not close segment handle.", e);
            }
        }
    }

    @Test
    public void initial_value_is_minus_one() {
        try (var cut = new IntSegment()) {
            assertThat(cut.getValue()).isEqualTo(-1L);
        }
    }

    @Test
    public void wrap_wraps_provided_segment() {
        var expectedVal = 123;
        underTest.setValue(expectedVal);
        try (var wrappingSeg = new IntSegment(underTest)) {
            assertThat(wrappingSeg.getSegment()).isSameAs(underTest);
        }
    }

    @Test
    public void wrap_wraps_provided_addr() {
        var expectedVal = 123;
        underTest.setValue(expectedVal);
        try (var wrappingSeg = new IntSegment(underTest.address())) {
            assertThat(wrappingSeg.getValue()).isEqualTo(expectedVal);
            assertThat(wrappingSeg.address()).isEqualTo(underTest.address());
        }
    }

    @Test
    public void getValue_getsValue_setValue_setsValue() {
        var expectedVal = Integer.MAX_VALUE;
        underTest.setValue(expectedVal);
        assertThat(underTest.getValue()).isEqualTo(expectedVal);

        expectedVal = Integer.MIN_VALUE;
        underTest.setValue(expectedVal);
        assertThat(underTest.getValue()).isEqualTo(expectedVal);
    }

    @Test
    public void setValue_does_not_accept_null() {
        assertThatThrownBy(() -> underTest.setValue(null)).isInstanceOf(NullPointerException.class).hasMessage("newValue");
    }

    @Test
    public void setValue_returns_old_value() {
        var oldValue = 123;
        var newValue = 321;
        underTest.setValue(oldValue);
        assertThat(underTest.setValue(newValue)).isEqualTo(oldValue);
    }

    @Test
    public void constructor_accepts_initial_value() {
        var expectedVal = 123;
        underTest = new IntSegment(expectedVal);
        assertThat(underTest.getValue()).isEqualTo(expectedVal);
    }
}
