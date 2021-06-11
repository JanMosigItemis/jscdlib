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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import de.itemis.mosig.fluffy.tests.java.FluffyTestHelper;
import de.itemis.mosig.jassuan.jscdlib.internal.IntSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringSegment;
import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.MemoryAddress;

public class JScdHandleTest {

    private static final Logger LOG = LoggerFactory.getLogger(JScdHandleTest.class);

    private static final String READER_ONE = "readerOne";
    private static final String READER_TWO = "readerTwo";

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
        setupAvailableReaders(READER_ONE);
        assertThat(underTest.listReaders()).as("Method must return a list").isInstanceOf(List.class);
    }

    @Test
    public void test_listReaders_returns_immutable_list() {
        setupAvailableReaders(READER_ONE);
        assertThatThrownBy(() -> underTest.listReaders().add("testString")).as("list of readers must be immutable")
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void when_one_reader_is_available_return_its_name_as_list() {
        setupAvailableReaders(READER_ONE);
        assertThat(underTest.listReaders()).containsExactly(READER_ONE);
    }

    @Test
    public void when_multiple_readers_are_available_return_their_names_as_list() {
        setupAvailableReaders(READER_ONE, READER_TWO);
        assertThat(underTest.listReaders()).containsExactly(READER_ONE, READER_TWO);
        // System.out.println(CLinker.toJavaStringRestricted(CLinker.toCString("eins\0zwei\0\0",
        // StandardCharsets.UTF_8).address().addOffset(5)));
    }

    private void setupAvailableReaders(String... readerNames) {

        when(nativeMock.sCardListReadersA(any(Addressable.class), any(Addressable.class), any(Addressable.class),
            any(Addressable.class))).then(invocation -> {
                var addrOfReaderListPtr = invocation.getArgument(2, MemoryAddress.class);
                var addrOfReaderListLength = invocation.getArgument(3, MemoryAddress.class);

                var readerList = new StringSegment();
                var ptrToReaderList = new StringPointerSegment(addrOfReaderListPtr);
                var readerListLength = new IntSegment(addrOfReaderListLength);
                var readerListMultiStringBuilder = new StringBuilder("");
                Arrays.stream(readerNames).forEach(reader -> {
                    readerListMultiStringBuilder.append(reader);
                    readerListMultiStringBuilder.append('\0');
                });
                readerListMultiStringBuilder.append('\0');
                String readerListMultiString = readerListMultiStringBuilder.toString();
                readerList.setValue(readerListMultiString);
                readerListLength.setValue(readerListMultiString.getBytes(StandardCharsets.UTF_8).length);
                ptrToReaderList.pointTo(readerList);
                return SCARD_S_SUCCESS;
            });
    }
}
