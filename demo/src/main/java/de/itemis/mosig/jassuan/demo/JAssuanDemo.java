package de.itemis.mosig.jassuan.demo;

import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_LONG_LONG;
import static jdk.incubator.foreign.CLinker.C_POINTER;
import static jdk.incubator.foreign.MemoryAddress.NULL;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import de.itemis.mosig.jassuan.assuan_h;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

public class JAssuanDemo {

    public static int assuan_generated() {
        var linker = CLinker.getInstance();
        int errorCode = -1;
        var ctxScope = ResourceScope.newConfinedScope();
        var ctxPtrSeg = MemorySegment.allocateNative(MemoryLayouts.ADDRESS, ctxScope);
        MemoryAddress ctxPtr = null;

        MemoryAddress data_cbC = null;
        MemoryAddress inquire_cbC = null;
        MemoryAddress status_cbC = null;

        try {
            errorCode = assuan_h.assuan_new(ctxPtrSeg.address());
            printErrorCode(errorCode, "assuan_new");
            long addr = ctxPtrSeg.asByteBuffer().order(ByteOrder.nativeOrder()).getLong();
            ctxPtr = MemoryAddress.ofLong(addr);

            assuan_h.assuan_socket_connect(ctxPtr, CLinker.toCString("C:\\Users\\mosig_user\\.gnupg\\S.scdaemon", StandardCharsets.UTF_8, ctxScope).address(),
                -1, 0);
            printErrorCode(errorCode, "assuan_socket_connect");

            var data_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "data_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, long.class));
            data_cbC = linker.upcallStub(data_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_LONG_LONG), ctxScope);

            var inquire_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "inquire_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
            inquire_cbC = linker.upcallStub(inquire_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER), ctxScope);

            var status_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "status_cb",
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
            status_cbC = linker.upcallStub(status_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER), ctxScope);

            assuan_h.assuan_transact(ctxPtr, CLinker.toCString("SERIALNO", StandardCharsets.UTF_8, ctxScope).address(), data_cbC.address(), NULL,
                inquire_cbC.address(), NULL,
                status_cbC.address(), NULL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ctxPtr != null) {
                try {
                    assuan_h.assuan_release(ctxPtr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ctxScope != null) {
                ctxScope.close();
            }
        }
        return 0;
    }

    // public static int assuan_manual() {
    // var linker = CLinker.getInstance();
    // var cLib =
    // LibraryLookup.ofPath(Path.of(URI.create("file:///C:/devtools/gnupg/bin_64/libassuan6-0.dll")));
    //
    // var cFuncRef = loadSymbol(cLib, "assuan_new");
    // FunctionDescriptor cFuncDescr = FunctionDescriptor.of(C_INT, C_POINTER);
    // MethodType javaMethodDescr = MethodType.methodType(int.class, MemoryAddress.class);
    // var javaMethodRef = linker.downcallHandle(cFuncRef, javaMethodDescr, cFuncDescr);
    //
    // MemoryAddress addrOfCtxPtr = null;
    // MemoryAddress ctxPtr = null;
    // MemorySegment ctxPtrSeg = null;
    //
    // MemorySegment data_cbC = null;
    // MemorySegment inquire_cbC = null;
    // MemorySegment status_cbC = null;
    //
    // int errorCode = -1;
    // try {
    // ctxPtrSeg = MemorySegment.allocateNative(MemoryLayouts.ADDRESS);
    // addrOfCtxPtr = ctxPtrSeg.address();
    //
    // errorCode = (int) javaMethodRef.invokeExact(addrOfCtxPtr);
    // System.out.println("assuan_new: " + (errorCode & 0xFFFF));
    //
    // long addr = ctxPtrSeg.asByteBuffer().order(nativeOrder()).getLong();
    // ctxPtr = MemoryAddress.ofLong(addr);
    //
    // var socketFuncRef = loadSymbol(cLib, "assuan_socket_connect");
    // var socketFuncDescr = FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT,
    // C_INT);
    // var socketMethodDescr = MethodType.methodType(int.class, MemoryAddress.class,
    // MemoryAddress.class, int.class, int.class);
    // var socketMethodRef = linker.downcallHandle(socketFuncRef, socketMethodDescr,
    // socketFuncDescr);
    // errorCode = (int) socketMethodRef.invokeExact(ctxPtr,
    // CLinker.toCString("C:\\Users\\mosig_user\\.gnupg\\S.scdaemon",
    // StandardCharsets.UTF_8).address(), -1, 0);
    // // Lowest 16 bits are error code, see gpg-error.h
    // System.out.println("assuan_socket_connect: " + (errorCode & 0xFFFF));
    //
    // var data_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "data_cb",
    // MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, long.class));
    // data_cbC = linker.upcallStub(data_cbJava, FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER,
    // C_LONG_LONG));
    //
    // var inquire_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "inquire_cb",
    // MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
    // inquire_cbC = linker.upcallStub(inquire_cbJava, FunctionDescriptor.of(C_INT, C_POINTER,
    // C_POINTER));
    //
    // var status_cbJava = MethodHandles.lookup().findStatic(JAssuanDemo.class, "status_cb",
    // MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class));
    // status_cbC = linker.upcallStub(status_cbJava, FunctionDescriptor.of(C_INT, C_POINTER,
    // C_POINTER));
    //
    // var transactFuncRef = loadSymbol(cLib, "assuan_transact");
    // var transactFuncDescr = FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_POINTER,
    // C_POINTER, C_POINTER, C_POINTER, C_POINTER, C_POINTER);
    // var transactMethodDescr = MethodType.methodType(int.class, MemoryAddress.class,
    // MemoryAddress.class, MemoryAddress.class, MemoryAddress.class,
    // MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class);
    // var transactMethodRef = linker.downcallHandle(transactFuncRef, transactMethodDescr,
    // transactFuncDescr);
    // errorCode = (int) transactMethodRef.invokeExact(ctxPtr,
    // CLinker.toCString("GETATTR LOGIN-DATA", StandardCharsets.UTF_8).address(),
    // data_cbC.address(), NULL, inquire_cbC.address(), NULL,
    // status_cbC.address(), NULL);
    // // Lowest 16 bits are error code, see gpg-error.h
    // System.out.println("assuan_transact: " + (errorCode & 0xFFFF));
    //
    // errorCode = (int) transactMethodRef.invokeExact(ctxPtr,
    // CLinker.toCString("SERIALNO", StandardCharsets.UTF_8).address(), data_cbC.address(), NULL,
    // inquire_cbC.address(), NULL,
    // status_cbC.address(), NULL);
    // // Lowest 16 bits are error code, see gpg-error.h
    // System.out.println("assuan_transact: " + (errorCode & 0xFFFF));
    // } catch (WrongMethodTypeException e) {
    // throw e;
    // } catch (Throwable t) {
    // var errMsg = t.getMessage() == null ? "No further information" : t.getMessage();
    // throw new RuntimeException("Calling the library function caused a problem: " +
    // t.getClass().getSimpleName() + ": " + errMsg, t);
    // } finally {
    // if (ctxPtr != null) {
    // var cReleaseFuncRef = loadSymbol(cLib, "assuan_release");
    // FunctionDescriptor cReleaseFuncDescr = FunctionDescriptor.ofVoid(C_POINTER);
    // MethodType javaReleaseMethodDescr = MethodType.methodType(void.class,
    // MemoryAddress.class);
    // var javaReleaseMethodRef = linker.downcallHandle(cReleaseFuncRef,
    // javaReleaseMethodDescr, cReleaseFuncDescr);
    // try {
    // javaReleaseMethodRef.invokeExact(ctxPtr);
    // } catch (Throwable e) {
    // e.printStackTrace();
    // } finally {
    // ctxPtrSeg.close();
    // }
    // }
    //
    // if (data_cbC != null) {
    // data_cbC.close();
    // }
    // if (inquire_cbC != null) {
    // inquire_cbC.close();
    // }
    // if (status_cbC != null) {
    // status_cbC.close();
    // }
    // }
    //
    // return errorCode;
    // }

    public static int data_cb(MemoryAddress allLines, MemoryAddress currentLine, long lineLength) {
        System.out.println("data_cb");
        System.out.println(CLinker.toJavaString(currentLine, StandardCharsets.UTF_8));
        return 0;
    }

    public static int inquire_cb(MemoryAddress allLines, MemoryAddress currentLine) {
        System.out.println("inquire_cb");
        System.out.println(CLinker.toJavaString(currentLine, StandardCharsets.UTF_8));
        return 0;
    }

    public static int status_cb(MemoryAddress allLines, MemoryAddress currentLine) {
        System.out.println("status_cb");
        System.out.println(CLinker.toJavaString(currentLine, StandardCharsets.UTF_8));
        return 0;
    }

    private static void printErrorCode(int errorCode, String opName) {
        // Lowest 16 bits are error code, see gpg-error.h
        System.out.println(opName + ": " + (errorCode & 0xFFFF));
    }
}
