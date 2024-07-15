package io.goji.jav.jvm;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Crash {
  public static void main(String... args) throws Exception {
    /*
        * This code will crash the JVM with the following error:
        * which is a SIGSEGV (Segmentation fault) error.
        * equal C code: void main() {  *((int*)(0xDEADBEEF)) = 0; // Accessing memory via broken ptr}
     */
    Field f = Unsafe.class.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Unsafe u = (Unsafe) f.get(null);
    u.getInt(0xDEADBEEF); // Accessing memory via broken ptr
  }
}
