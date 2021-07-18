package de.itemis.jassuan.jscdlib;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

public interface JAssuanNative {

    /**
     * If you don’t know the server’s process ID (PID), pass ASSUAN_INVALID_PID.
     * 
     * @see https://www.gnupg.org/documentation/manuals/assuan/Client-code.html
     */
    public static final int ASSUAN_INVALID_PID = 0;
    /**
     * With flags set to ASSUAN_SOCKET_CONNECT_FDPASSING, sendmsg and recvmesg are used for input
     * and output and thereby enable the use of descriptor passing.
     * 
     * @see https://www.gnupg.org/documentation/manuals/assuan/Client-code.html
     */
    public static final int ASSUAN_SOCKET_CONNECT_FDPASSING = 1;

    /**
     * Create a new assuan context with default arguments.
     *
     * @param p_ctx Pointer of pointer segment. Will hold the pointer to the created ctx upon
     *        successful return.
     * @return gpg_error_t - 0 for success, error code else.
     */
    long assuanNew(MemoryAddress p_ctx);

    /**
     * Release all resources associated with {@code ctx}.
     *
     * @param ctx Pointer to ctx created with {@link #assuanNew(MemoryAddress)}.
     */
    void assuanRelease(MemoryAddress ctx);

    /**
     * Connect to an assuan socket (to the scdaemon).
     *
     * @param ctx Pointer to ctx created with {@link #assuanNew(MemoryAddress)}.
     * @param name Unix domain socket to connect to. May be a URI to a file.
     * @param server_pid Currently unused. Use ASSUAN_INVALID_PID (-1)
     * @param flags Undocumented. Use ASSUAN_SOCKET_CONNECT_FDPASSING (1)
     * @return gpg_error_t - 0 for success, error code else.
     */
    long assuanSocketConnect(MemoryAddress ctx, MemoryAddress name, int server_pid, int flags);

    /**
     * <p>
     * Send a command to the smart card daemon (scdaemon).
     * </p>
     * <p>
     * Use
     * {@link CLinker#upcallStub(java.lang.invoke.MethodHandle, jdk.incubator.foreign.FunctionDescriptor)}
     * to provide proper callbacks.
     * </p>
     * <p>
     * Required callback signatures are:
     * <ul>
     * <li>gpg_error_t (*data_cb)(void *, const void *, size_t)</li>
     * <li>gpg_error_t (*inquire_cb)(void*, const char *)</li>
     * <li>gpg_error_t (*status_cb)(void*, const char *)</li>
     * </ul>
     * </p>
     *
     * @see https://www.gnupg.org/documentation/manuals/assuan/Client-code.html
     *
     * @param ctx Pointer to Assuan ctx
     * @param command Pointer to command string
     * @param data_cb Callback method for returned data.
     * @param data_cb_arg Will be passed to the callback along other things. May be
     *        {@link MemoryAddress#NULL}.
     * @param inquire_cb Callback method if the daemon needs more information from the client.
     * @param inquire_cb_arg Will be passed to the callback along other things. May be
     *        {@link MemoryAddress#NULL}.
     * @param status_cb Called for status lines returned from the daemon.
     * @param status_cb_arg Will be passed to the callback along other things. May be
     *        {@link MemoryAddress#NULL}.
     * @return gpg_error_t
     */
    long assuanTransact(MemoryAddress ctx, MemoryAddress command, MemoryAddress data_cb, MemoryAddress data_cb_arg,
            MemoryAddress inquire_cb, MemoryAddress inquire_cb_arg, MemoryAddress status_cb, MemoryAddress status_cb_arg);
}
