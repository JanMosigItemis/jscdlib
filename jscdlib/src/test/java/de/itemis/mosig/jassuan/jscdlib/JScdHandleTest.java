package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_S_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.itemis.mosig.fluffy.tests.java.FluffyTestHelper;
import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

public class JScdHandleTest {

    private static final Logger LOG = LoggerFactory.getLogger(JScdHandleTest.class);

    private JAssuanNative nativeMock;
    private JScdHandle underTest;

    @BeforeEach
    public void setUp() {
        nativeMock = Mockito.mock(JAssuanNative.class);
        underTest = JScdLib.createHandle(nativeMock);
    }

    @AfterEach
    public void tearDown() {
        if (underTest != null) {
            try {
                underTest.close();
            } catch (Exception e) {
                LOG.warn("Possible ressource leak. Could not close JScd test handle.", e);
            }
        }
    }

    @Test
    public void test_handle_is_final() {
        FluffyTestHelper.assertFinal(JScdHandle.class);
    }

    @Test
    public void test_handle_is_autoclosable() {
        assertThat(AutoCloseable.class).as("JScd handles must be autoclossable").isAssignableFrom(JScdHandle.class);
    }

    @Test
    public void test_listReaders_returns_list() {
        assertThat(underTest.listReaders()).as("Method must return a list").isInstanceOf(List.class);
    }

    @Test
    public void test_listReaders_returns_immutable_list() {
        assertThatThrownBy(() -> underTest.listReaders().add("testString")).as("list of readers must be immutable")
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void when_one_reader_available_return_its_name_as_list() {
        String expectedReaderName = "testReaderName";
        when(nativeMock.sCardListReadersA(any(Addressable.class), any(Addressable.class), any(Addressable.class),
            any(Addressable.class))).then(invocation -> {
                var readerListPtrSegPtr = invocation.getArgument(2, MemoryAddress.class);
                byte[] expectedReaderListBytes = expectedReaderName.getBytes(StandardCharsets.UTF_8);
                var readerListPtrSeg = readerListPtrSegPtr.asSegmentRestricted(expectedReaderListBytes.length);
                var readerList = CLinker.toCString(expectedReaderName, StandardCharsets.UTF_8);
                var addr = readerList.address().toRawLongValue();
                readerListPtrSeg.asByteBuffer().order(ByteOrder.nativeOrder()).putLong(addr);
                return SCARD_S_SUCCESS;
            });


        assertThat(underTest.listReaders()).containsExactly(expectedReaderName);

    }
}
