package de.itemis.mosig.jassuan.jscdlib.internal;

import static java.nio.ByteOrder.nativeOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jdk.incubator.foreign.MemoryAddress;

public class PointerSegmentTest {

    private PointerSegment<String> underTest;

    @BeforeEach
    public void setUp() {
        underTest = new PointerSegment<>() {
            @Override
            public String dereference() {
                return null;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        if (underTest != null) {
            underTest.close();
        }
    }

    @Test
    public void getContainedAddress_returns_address() {
        long expectedContents = 123L;
        underTest.pointTo(expectedContents);
        assertThat(underTest.getContainedAddress().toRawLongValue()).isEqualTo(expectedContents);
    }

    @Test
    public void test_pointTo_with_long_sets_contents_correctly() {
        long expectedContents = 123L;
        underTest.pointTo(expectedContents);
        assertThat(underTest.asByteBuffer().order(nativeOrder()).getLong()).isEqualTo(expectedContents);
    }

    @Test
    public void test_pointTo_with_addr_sets_contents_correctly() {
        long expectedContents = 123L;
        MemoryAddress addr = MemoryAddress.ofLong(expectedContents);
        underTest.pointTo(addr);
        assertThat(underTest.asByteBuffer().order(nativeOrder()).getLong()).isEqualTo(expectedContents);
    }

    @Test
    public void test_pointTo_does_not_accept_null() {
        assertThatThrownBy(() -> underTest.pointTo(null)).isInstanceOf(NullPointerException.class).hasMessage("addr");
    }
}
