package com.itemis.jscdlib.internal.memory;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.LibraryLookup.Symbol;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;

public final class NativeMethodHandle<T> {

    private final MethodHandle method;

    private NativeMethodHandle(MethodHandle method) {
        this.method = requireNonNull(method, "method");
    }

    // Well the cast ought to be safe enough. It is only possible to make harm here in case the
    // implementation of the builder itself is wrong. We try to mitigate this with good test
    // coverage.
    @SuppressWarnings("unchecked")
    public T call(Object... args) {
        try {
            return (T) method.invokeWithArguments(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static ReturnTypeStage ofLib(LibraryLookup lib) {
        return new NativeMethodHandleBuilder<>(lib);
    }

    public static interface ReturnTypeStage {
        <T> NameStage<T> returnType(Class<T> returnType);
    }

    public static interface NameStage<T> {
        ArgsStage<T> func(String name);
    }

    public static interface ArgsStage<T> {
        CreateStage<T> args(MemoryLayout... args);
    }

    public static interface CreateStage<T> {
        NativeMethodHandle<T> create(CLinker linker);
    }

    private static final class NativeMethodHandleBuilder<T> implements ReturnTypeStage, NameStage<T>, ArgsStage<T>, CreateStage<T> {
        private static final Map<Class<?>, MemoryLayout> JAVA_TO_C_TYPE_MAPPING;
        private static final Map<MemoryLayout, Class<?>> C_TYPE_TO_JAVA_MAPPING;

        static {
            var longType = CLinker.C_LONG;
            var osName = nullToEmpty(System.getProperty("os.name")).toLowerCase().trim();
            if (osName.startsWith("windows")) {
                longType = CLinker.C_LONG_LONG;
            }

            JAVA_TO_C_TYPE_MAPPING = ImmutableMap.<Class<?>, MemoryLayout>builder()
                .put(Long.class, longType)
                .put(long.class, longType)
                .put(Character.class, CLinker.C_CHAR)
                .put(char.class, CLinker.C_CHAR)
                .put(Double.class, CLinker.C_DOUBLE)
                .put(double.class, CLinker.C_DOUBLE)
                .put(Float.class, CLinker.C_FLOAT)
                .put(float.class, CLinker.C_FLOAT)
                .put(Integer.class, CLinker.C_INT)
                .put(int.class, CLinker.C_INT)
                .put(Short.class, CLinker.C_SHORT)
                .put(short.class, CLinker.C_SHORT)
                .build();

            C_TYPE_TO_JAVA_MAPPING = ImmutableMap.<MemoryLayout, Class<?>>builder()
                .put(CLinker.C_LONG, long.class)
                .put(CLinker.C_LONG_LONG, long.class)
                .put(CLinker.C_CHAR, char.class)
                .put(CLinker.C_DOUBLE, double.class)
                .put(CLinker.C_FLOAT, float.class)
                .put(CLinker.C_INT, int.class)
                .put(CLinker.C_POINTER, MemoryAddress.class)
                .put(CLinker.C_SHORT, short.class)
                .build();
        }

        private LibraryLookup lib;
        private Symbol func;
        private Class<?> javaReturnType;
        private MemoryLayout cReturnType;
        private FunctionDescriptor funcDescr;
        private MethodType methodType;

        NativeMethodHandleBuilder(LibraryLookup lib) {
            this.lib = requireNonNull(lib, "lib");
        }

        // Well the cast ought to be safe enough. It is only possible to make harm here in case the
        // implementation itself is wrong. We try to mitigate this with good test coverage.
        @SuppressWarnings("unchecked")
        @Override
        public <K> NameStage<K> returnType(Class<K> returnType) {
            this.javaReturnType = requireNonNull(returnType, "returnType");
            this.cReturnType = safeGetCType(javaReturnType);
            return (NameStage<K>) this;
        }

        @Override
        public ArgsStage<T> func(String name) {
            this.func = loadSymbol(lib, requireNonNull(name, "name"));
            return this;
        }

        @Override
        public CreateStage<T> args(MemoryLayout... args) {
            funcDescr = FunctionDescriptor.of(cReturnType, requireNonNull(args, "args"));
            methodType = MethodType.methodType(javaReturnType, safeGetJavaTypes(args));

            return this;
        }

        @Override
        public NativeMethodHandle<T> create(CLinker linker) {
            var method = linker.downcallHandle(func, methodType, funcDescr);
            return new NativeMethodHandle<>(method);
        }

        private static Symbol loadSymbol(LibraryLookup lib, String symbolName) {
            return lib.lookup(symbolName)
                .orElseThrow(() -> new RuntimeException("Could not find symbol '" + symbolName + "' in library '" + lib.toString() + "'."));
        }

        private final MemoryLayout safeGetCType(Class<?> javaType) {
            var cType = JAVA_TO_C_TYPE_MAPPING.get(javaType);
            return cType;
        }

        private final Class<?> safeGetJavaType(MemoryLayout cType) {
            return C_TYPE_TO_JAVA_MAPPING.get(cType);
        }

        private Class<?>[] safeGetJavaTypes(MemoryLayout... args) {
            Class<?>[] result = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                result[i] = safeGetJavaType(args[i]);
            }

            return result;
        }
    }
}
