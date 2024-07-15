package io.goji.jav.panama;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

public class Segment {

    public static void main(String[] args) {
        String s = "My string";
        try (Arena arena = Arena.ofConfined()) {

            // Allocate off-heap memory
            MemorySegment nativeText = arena.allocateUtf8String(s);

            // Access off-heap memory
            for (int i = 0; i < s.length(); i++ ) {
                System.out.print((char)nativeText.get(ValueLayout.JAVA_BYTE, i));
            }
        } // Off-heap memory is deallocated
    }


    static long invokeStrlen(String s) throws Throwable {

        try (Arena arena = Arena.ofConfined()) {

            // Allocate off-heap memory and
            // copy the argument, a Java string, into off-heap memory
            MemorySegment nativeString = arena.allocateUtf8String(s);

            // Link and call the C function strlen

            // Obtain an instance of the native linker
            Linker linker = Linker.nativeLinker();

            // Locate the address of the C function signature
            SymbolLookup stdLib = linker.defaultLookup();


            MemorySegment strlen_addr = stdLib.find("strlen").get();


            // Create a description of the C function
            FunctionDescriptor strlen_sig =
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);

            // Create a downcall handle for the C function
            MethodHandle strlen = linker.downcallHandle(strlen_addr, strlen_sig);

            // Call the C function directly from Java
            return (long)strlen.invokeExact(nativeString);
        }
    }
}
