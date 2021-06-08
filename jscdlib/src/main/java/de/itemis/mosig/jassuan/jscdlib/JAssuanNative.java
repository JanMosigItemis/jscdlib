package de.itemis.mosig.jassuan.jscdlib;

import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.MemoryAddress;

/**
 * Implementations of this interface know how to call the native assuan lib functions. It abstracts
 * away the knowledge how exactly this is done (e. g. JNI, JNA, FLA).
 */
public interface JAssuanNative {

    /**
     * According to the spec, this is a null pointer.
     */
    public static final Addressable SCARD_ALL_READERS = MemoryAddress.NULL;

    public static final int SCARD_AUTOALLOCATE = -1;

    /**
     * Success
     */
    public static final long SCARD_S_SUCCESS = 0x0;
    /**
     * Group contains no readers
     */
    public static final long SCARD_E_NO_READERS_AVAILABLE = 0x8010002E;
    /**
     * Specified reader is not currently available for use
     */
    public static final long SCARD_E_READER_UNAVAILABLE = 0x80100017;

    /**
     * See <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa">https://docs.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa</a>
     *
     * @param hContext
     * @param mszGroups
     * @param mszReaders
     * @param pcchReaders
     * @return error code according to <a href=
     *         "https://docs.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values">SmartCard
     *         return values</a>.
     */
    long sCardListReadersA(Addressable hContext, Addressable mszGroups, Addressable mszReaders, Addressable pcchReaders);

}
