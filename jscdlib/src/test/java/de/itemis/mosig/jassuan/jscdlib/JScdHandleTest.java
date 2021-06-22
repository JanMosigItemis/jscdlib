package de.itemis.mosig.jassuan.jscdlib;

import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.PCSC_SCOPE_SYSTEM;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_ALL_READERS;
import static de.itemis.mosig.jassuan.jscdlib.JAssuanNative.SCARD_AUTOALLOCATE;
import static de.itemis.mosig.jassuan.jscdlib.JScdProblems.SCARD_E_NO_MEMORY;
import static de.itemis.mosig.jassuan.jscdlib.JScdProblems.SCARD_S_SUCCESS;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.itemis.mosig.fluffy.tests.java.FluffyTestHelper;
import de.itemis.mosig.jassuan.jscdlib.internal.IntSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.LongPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.LongSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringPointerSegment;
import de.itemis.mosig.jassuan.jscdlib.internal.StringSegment;
import jdk.incubator.foreign.MemoryAddress;

public class JScdHandleTest {

    private static final Logger LOG = LoggerFactory.getLogger(JScdHandleTest.class);

    private static final String READER_ONE = "readerOne";
    private static final String READER_TWO = "readerTwo";

    private SCardMethodInvocations invocations;
    private JAssuanNative nativeMock;
    private JScdHandle underTest;

    @BeforeEach
    public void setUp() {
        nativeMock = mock(JAssuanNative.class);
        invocations = new SCardMethodInvocations();
        setupAllMethodsSuccess();

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
    }

    /**
     * We cannot test against a real winscard.dll. Thus, we must make sure, the calls are correct
     * and in expected order.
     */
    @Test
    public void list_readers_happy_path_runs_expected_calls() {
        setupAvailableReaders(READER_ONE);

        assertThatNoException().isThrownBy(() -> underTest.listReaders());

        var inOrder = inOrder(nativeMock);
        inOrder.verify(nativeMock).sCardEstablishContext(eq(PCSC_SCOPE_SYSTEM), same(NULL), same(NULL), any(MemoryAddress.class));
        inOrder.verify(nativeMock)
            .sCardListReadersA(eq(invocations.hContext), eq(SCARD_ALL_READERS), any(MemoryAddress.class), any(MemoryAddress.class));
        inOrder.verify(nativeMock).sCardFreeMemory(eq(invocations.hContext), eq(invocations.readerListPtr));
        inOrder.verify(nativeMock).sCardReleaseContext(eq(invocations.hContext));
    }

    @Test
    public void list_readers_throws_jscdException_if_establish_context_fails() {
        establishContextReturns(SCARD_E_NO_MEMORY);
        assertThatThrownBy(() -> underTest.listReaders()).as("Expected exception in case of an error in smart card native code.")
            .isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem", SCARD_E_NO_MEMORY);
    }

    private void establishContextReturns(JScdProblem expectedValue) {
        when(nativeMock.sCardEstablishContext(anyLong(), any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class)))
            .thenReturn(expectedValue.errorCode());
    }

    private void setupAllMethodsSuccess() {
        when(nativeMock.sCardEstablishContext(anyLong(), any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class)))
            .thenAnswer(invocation -> {
                try (var ctxPtr = new LongPointerSegment(invocation.getArgument(3, MemoryAddress.class));
                        var ctx = new LongSegment()) {
                    ctxPtr.pointTo(ctx);
                    invocations.hContext = ctxPtr.getContainedAddress();
                    return SCARD_S_SUCCESS.errorCode();
                }
            });

        when(nativeMock.sCardListReadersA(any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class)))
            .thenReturn(SCARD_S_SUCCESS.errorCode());
        when(nativeMock.sCardFreeMemory(any(MemoryAddress.class), any(MemoryAddress.class))).thenReturn(SCARD_S_SUCCESS.errorCode());
        when(nativeMock.sCardReleaseContext(any(MemoryAddress.class))).thenReturn(SCARD_S_SUCCESS.errorCode());
    }

    private void setupAvailableReaders(String... readerNames) {
        when(nativeMock.sCardListReadersA(any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class),
            any(MemoryAddress.class))).then(invocation -> {
                var addrOfReaderListPtr = invocation.getArgument(2, MemoryAddress.class);
                var addrOfReaderListLength = invocation.getArgument(3, MemoryAddress.class);

                try (var readerList = new StringSegment();
                        var ptrToReaderList = new StringPointerSegment(addrOfReaderListPtr);
                        var readerListLength = new IntSegment(addrOfReaderListLength)) {
                    assertThat(readerListLength.getValue()).as("Provided reader list length must be unset.").isEqualTo(SCARD_AUTOALLOCATE);
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
                    invocations.readerListPtr = ptrToReaderList.getContainedAddress();
                    return SCARD_S_SUCCESS.errorCode();
                }
            });
    }

    private static final class SCardMethodInvocations {
        MemoryAddress hContext = null;
        MemoryAddress readerListPtr = null;
    }
}
