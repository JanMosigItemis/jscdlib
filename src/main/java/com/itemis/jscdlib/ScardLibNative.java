package com.itemis.jscdlib;

import jdk.incubator.foreign.MemoryAddress;

/**
 * Implementations of this interface know how to call native smart card functions via OS libraries.
 * It abstracts away the knowledge how exactly this is done (e. g. JNI, JNA, FLA).
 */
public interface ScardLibNative {

    /**
     * According to the spec, this is a null pointer.
     */
    public static final MemoryAddress SCARD_ALL_READERS = MemoryAddress.NULL;

    /**
     * Use this value to signal that a lib should automatically allocate memory for lists or arrays.
     */
    public static final int SCARD_AUTOALLOCATE = -1;

    /**
     * Database operations are performed within the domain of the user.
     */
    public static final long PCSC_SCOPE_SYSTEM = 2;

    /**
     * Database operations are performed within the domain of the system. The calling application
     * must have appropriate access permissions for any database actions.
     */
    public static final long PCSC_SCOPE_USER = 0;

    /**
     * See <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext">https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext</a>
     *
     * @param dwScope - {@link #PCSC_SCOPE_SYSTEM} or {@link #PCSC_SCOPE_USER}.
     * @param pvReserved1 - Reserved for future use and must be {@link MemoryAddress#NULL}.
     * @param pvReserved2 - Reserved for future use and must be {@link MemoryAddress#NULL}.
     * @param phContext
     * @return {@link #SCARD_S_SUCCESS} if success, else error code according to <a href=
     *         "https://docs.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values">SmartCard
     *         return values</a>.
     */
    long sCardEstablishContext(long dwScope, MemoryAddress pvReserved1, MemoryAddress pvReserved2, MemoryAddress phContext);

    /**
     * See <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa">https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa</a>
     *
     * @param hContext
     * @param mszGroups
     * @param mszReaders
     * @param pcchReaders
     * @return {@link #SCARD_S_SUCCESS} if success, else error code according to <a href=
     *         "https://docs.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values">SmartCard
     *         return values</a>.
     */
    long sCardListReadersA(MemoryAddress hContext, MemoryAddress mszGroups, MemoryAddress mszReaders, MemoryAddress pcchReaders);

    /**
     * See <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardfreememory">https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardfreememory</a>
     *
     * @param hContext
     * @param pvMem - Memory block to be released.
     * @return {@link #SCARD_S_SUCCESS} if success, else error code according to <a href=
     *         "https://docs.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values">SmartCard
     *         return values</a>.
     */
    long sCardFreeMemory(MemoryAddress hContext, MemoryAddress pvMem);

    /**
     * See <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext">https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext</a>
     *
     * @param hContext
     * @return {@link #SCARD_S_SUCCESS} if success, else error code according to <a href=
     *         "https://docs.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values">SmartCard
     *         return values</a>.
     */
    long sCardReleaseContext(MemoryAddress hContext);
}
