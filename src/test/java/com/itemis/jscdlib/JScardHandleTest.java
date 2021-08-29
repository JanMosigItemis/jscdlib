package com.itemis.jscdlib;

import static ch.qos.logback.classic.Level.WARN;
import static com.itemis.jscdlib.JScardNative.PCSC_SCOPE_SYSTEM;
import static com.itemis.jscdlib.JScardNative.SCARD_ALL_READERS;
import static com.itemis.jscdlib.JScardNative.SCARD_AUTOALLOCATE;
import static com.itemis.jscdlib.problem.JScdProblems.SCARD_E_NO_MEMORY;
import static com.itemis.jscdlib.problem.JScdProblems.SCARD_E_NO_READERS_AVAILABLE;
import static com.itemis.jscdlib.problem.JScdProblems.SCARD_S_SUCCESS;
import static jdk.incubator.foreign.MemoryAddress.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itemis.fluffyj.tests.FluffyTestHelper;
import com.itemis.fluffyj.tests.logging.FluffyTestAppender;
import com.itemis.jscdlib.internal.memory.IntSegment;
import com.itemis.jscdlib.internal.memory.LongPointerSegment;
import com.itemis.jscdlib.internal.memory.LongSegment;
import com.itemis.jscdlib.internal.memory.StringPointerSegment;
import com.itemis.jscdlib.internal.memory.StringSegment;
import com.itemis.jscdlib.problem.JScdException;
import com.itemis.jscdlib.problem.JScdProblem;
import com.itemis.jscdlib.problem.JScdProblems;

import jdk.incubator.foreign.MemoryAddress;

public class JScardHandleTest {

    private static final Logger LOG = LoggerFactory.getLogger(JScardHandleTest.class);

    private static final String READER_ONE = "readerOne";
    private static final String READER_TWO = "readerTwo";

    @RegisterExtension
    FluffyTestAppender logAssert = new FluffyTestAppender();

    private SCardMethodInvocations invocations;
    private JScardNative nativeMock;
    private JSCardHandle underTest;

    @BeforeEach
    public void setUp() {
        nativeMock = mock(JScardNative.class);
        invocations = new SCardMethodInvocations();
        setupAllMethodsSuccess();

        underTest = new JSCardHandle(nativeMock);
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
        FluffyTestHelper.assertFinal(JSCardHandle.class);
    }

    @Test
    public void test_handle_is_autoclosable() {
        assertThat(AutoCloseable.class).as("JScd handles must be autoclossable").isAssignableFrom(JSCardHandle.class);
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

    @Test
    public void when_no_readers_are_available_return_empty_list() {
        setupAvailableReaders(SCARD_E_NO_READERS_AVAILABLE);
        assertThat(underTest.listReaders()).isEmpty();
    }

    @Test
    public void when_no_readers_are_available_cleanup_correctly() {
        setupAvailableReaders(SCARD_E_NO_READERS_AVAILABLE);
        underTest.listReaders();
        assertThat(invocations.readerListPtr).describedAs("The readerListPtr must be set even if listReaders returns no readers.").isNotNull();
        verify(nativeMock).sCardFreeMemory(invocations.hContext, invocations.readerListPtr);
        verify(nativeMock).sCardReleaseContext(invocations.hContext);
    }

    @Test
    public void when_establish_ctx_fails_skip_cleanup() {
        establishContextReturns(SCARD_E_NO_MEMORY);
        assertThatThrownBy(() -> underTest.listReaders()).isInstanceOf(JScdException.class);
        verify(nativeMock, never()).sCardFreeMemory(nullable(MemoryAddress.class), nullable(MemoryAddress.class));
        verify(nativeMock, never()).sCardReleaseContext(nullable(MemoryAddress.class));
    }

    @Test
    public void when_establish_listReaders_fails_skip_freeMem() {
        setupAvailableReaders(SCARD_E_NO_MEMORY);
        assertThatThrownBy(() -> underTest.listReaders()).isInstanceOf(JScdException.class);
        verify(nativeMock, never()).sCardFreeMemory(nullable(MemoryAddress.class), nullable(MemoryAddress.class));
        verify(nativeMock).sCardReleaseContext(any(MemoryAddress.class));
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
        JScdProblems expectedProblem = SCARD_E_NO_MEMORY;
        establishContextReturns(expectedProblem);
        assertThatThrownBy(() -> underTest.listReaders()).as("Expected exception in case of an error in smart card native code.")
            .isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem", expectedProblem);
    }

    @Test
    public void list_readers_throws_jscdException_if_scardListReaders_fails() {
        JScdProblems expectedProblem = SCARD_E_NO_MEMORY;
        setupAvailableReaders(expectedProblem);
        assertThatThrownBy(() -> underTest.listReaders()).as("Expected exception in case of an error in smart card native code.")
            .isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem", expectedProblem);
    }

    @Test
    public void list_readers_throws_jscdException_if_an_unknown_error_code_is_encountered() {
        var errorWithUnknownErrorCode = new JScdProblem() {

            @Override
            public String errorName() {
                return "testError";
            }

            @Override
            public long errorCode() {
                return -1;
            }

            @Override
            public String description() {
                return "Expected test error";
            }
        };
        establishContextReturns(errorWithUnknownErrorCode);
        assertThatThrownBy(() -> underTest.listReaders()).as("Expected exception in case smart card native code returns an unknown error code.")
            .isInstanceOf(JScdException.class)
            .hasFieldOrPropertyWithValue("problem", JScdProblems.UNKNOWN_ERROR_CODE);
    }

    @Test
    public void errors_during_free_mem_are_logged_no_exception_is_thrown() {
        JScdProblems expectedProblem = SCARD_E_NO_MEMORY;
        freeMemReturns(expectedProblem);

        assertDoesNotThrow(() -> underTest.listReaders());

        logAssert.assertLogContains(WARN,
            "Possible ressource leak: Operation listReaders could not free memory. Reason: " + expectedProblem + ": " + expectedProblem.description());
    }

    @Test
    public void errors_during_release_ctx_are_logged_no_exception_is_thrown() {
        JScdProblems expectedProblem = SCARD_E_NO_MEMORY;
        releaseCtxReturns(expectedProblem);

        assertDoesNotThrow(() -> underTest.listReaders());

        logAssert.assertLogContains(WARN,
            "Possible ressource leak: Operation listReaders could not release scard context. Reason: " + expectedProblem + ": "
                + expectedProblem.description());
    }

    private void establishContextReturns(JScdProblem expectedProblem) {
        when(nativeMock.sCardEstablishContext(anyLong(), any(MemoryAddress.class), any(MemoryAddress.class), any(MemoryAddress.class)))
            .thenReturn(expectedProblem.errorCode());
    }

    private void freeMemReturns(JScdProblem expectedProblem) {
        when(nativeMock.sCardFreeMemory(any(MemoryAddress.class), any(MemoryAddress.class))).thenReturn(expectedProblem.errorCode());
    }

    private void releaseCtxReturns(JScdProblem expectedProblem) {
        when(nativeMock.sCardReleaseContext(any(MemoryAddress.class))).thenReturn(expectedProblem.errorCode());
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

        setupAvailableReaders(SCARD_S_SUCCESS);
        freeMemReturns(SCARD_S_SUCCESS);
        releaseCtxReturns(SCARD_S_SUCCESS);
    }

    private void setupAvailableReaders(JScdProblem expectedProblem, String... readerNames) {
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
                    return expectedProblem.errorCode();
                }
            });
    }

    private void setupAvailableReaders(String... readerNames) {
        setupAvailableReaders(SCARD_S_SUCCESS, readerNames);
    }

    private static final class SCardMethodInvocations {
        MemoryAddress hContext = null;
        MemoryAddress readerListPtr = null;
    }
}
