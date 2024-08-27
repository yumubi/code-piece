//package io.goji.exp.wc;
//
//import java.io.BufferedOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.nio.charset.StandardCharsets;
//import net.openhft.hashing.LongHashFunction;
//
///** Highly optimized word count algorithm for java incorporating multiple optimizations
// *  Key tricks:
// *    - Buffer I/O, either using Buffered I/O classes or by explicitly reading chunks to an array
// *    - Process recurring tokens separately from singletons (big performance gain)
// *    - Work directly with raw bytes (safe for UTF-8), to reduce memory and avoid text decoding cost
// *    - Sort strings using byte order (fudging the sort order slightly, faster than decoding to sort)
// *    - Use a radix byte sort to sort large lists of bytestrings (small performance gain)
// *    - Faster hashing
// *
// *  Authors:
// *   - Sam Van Oort <samvanoort@gmail.com> (svanoort on GitHub)
// *   - Rick Hendricksen (xupwup on GitHub)
// *   - sgwerder on GitHub
// *   - Roman Leventov (leventov on Github)
// */
//class WordCountOptimized {
//
//    public static final LongHashFunction XX_HASH = LongHashFunction.xx_r39();
//
//    private static class CountForWord implements Comparable<CountForWord>{
//        byte[] word;
//        int count = 1;
//
//
//        public CountForWord(byte[] word, int count) {
//            this.word = word;
//            this.count = count;
//        }
//
//        @Override
//        public int compareTo(CountForWord t) {
//            if(count < t.count){
//                return 1;
//            } else if(count > t.count) {
//                return -1;
//            } else {
//                return BYTE_COMPARATOR_INSTANCE.compare(this.word, t.word);
//            }
//        }
//
//        public StringBuilder toStringBuilder() {
//            StringBuilder retval = new StringBuilder(word.length+6);
//            retval.append(new String(word, StandardCharsets.UTF_8))
//                .append('\t')
//                .append(count);
//            return retval;
//        }
//    }
//
//    // Compares pairs of byte arrays by unsigned byte values, then by array length
//    // Allows for creating an instance with an offset
//    static class ByteArrayComparator implements Comparator<byte[]> {
//        int offset=0;
//
//        @Override
//        public int compare(byte[] b1, byte[] b2) {
//            // Sorting by unsigned byte value is less correct
//            // but avoids very expensive UTF-8 decoding
//            int limit = Math.min(b1.length, b2.length);
//            for(int i=offset; i<limit; i++) {
//                int diff = (b1[i] & 0xFF) - (b2[i] & 0xFF);
//                if (diff != 0) {
//                    return diff;
//                }
//            }
//            return b1.length-b2.length;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return o == this;
//        }
//
//        ByteArrayComparator() {
//
//        }
//
//        // Only start comparing at byte X
//        ByteArrayComparator(int offset){
//            this.offset = offset;
//        }
//    }
//
//    private static final ByteArrayComparator BYTE_COMPARATOR_INSTANCE = new ByteArrayComparator();
//
//    /** Sorts a list of byte arrays, switching between builtin sort & radix sort on bytes, depending on size of list
//     *  Arrays are sorted by the unsigned byte value of each element, and if identical then longest wins
//     *  Properties: recursive, not in-place, not null-safe, generates a bit of garbage
//     *  @param byteArrayList List of byte arrays to sort
//     *  @param byteOffset Offset of first byte in arryays to use in sorting, use 0 for the initial sorting
//     */
//    static void fastRadixSort(ArrayList<byte[]> byteArrayList, int byteOffset) {
//        if (byteArrayList.size() < 512) {
//            Collections.sort(byteArrayList, new ByteArrayComparator(byteOffset));
//        } else if (byteArrayList.size() < 4096) { // Overheads not justified for small arrays, bigger ones use first 2 bytes
//            ArrayList<byte[]>[] buckets = new ArrayList[256];
//            final int slotSize = Math.max(byteArrayList.size()>>8, 4);
//            ArrayList<byte[]> prefix = new ArrayList<byte[]>(); //Values not long enough to have this byte
//            int len = byteArrayList.size();
//            for(int i=0; i<len; i++) {
//                byte[] val = byteArrayList.get(i);
//                if (byteOffset >= val.length) {
//                    prefix.add(val);
//                } else {
//                    int index = val[byteOffset] & 0xFF;
//                    ArrayList<byte[]> slot = buckets[index];
//                    if (slot == null) {
//                        slot = new ArrayList<byte[]>(slotSize);
//                        buckets[index] = slot;
//                    }
//                    slot.add(val);
//                }
//            }
//            // Prefix is already sorted
//            byteArrayList.clear();
//            byteArrayList.addAll(prefix);
//            for (int i=0; i<buckets.length; i++) {
//                ArrayList<byte[]> slot = buckets[i];
//                if (slot != null) {
//                    fastRadixSort(slot, byteOffset+1);
//                    byteArrayList.addAll(slot);
//                }
//            }
//        } else { // Huge array sort, using first 2 bytes
//            ArrayList<byte[]>[] buckets = new ArrayList[65536];
//            ArrayList<byte[]> prefix = new ArrayList<byte[]>();
//            int len = byteArrayList.size();
//            for(int i=0; i<len; i++) {
//                byte[] val = byteArrayList.get(i);
//                if (byteOffset+1 >= val.length) {
//                    prefix.add(val);
//                } else {
//                    int index = (val[byteOffset] & 0xFF) << 8 | (val[byteOffset+1] & 0xFF);
//                    ArrayList<byte[]> slot = buckets[index];
//                    if (slot == null) {
//                        slot = new ArrayList<byte[]>();
//                        buckets[index] = slot;
//                    }
//                    slot.add(val);
//                }
//            }
//            // Prefix is already sorted
//            byteArrayList.clear();
//            Collections.sort(prefix, new ByteArrayComparator(byteOffset));
//            byteArrayList.addAll(prefix);
//            for (int i=0; i<buckets.length; i++) {
//                ArrayList<byte[]> slot = buckets[i];
//                if (slot != null) {
//                    fastRadixSort(slot, byteOffset+2);
//                    byteArrayList.addAll(slot);
//                }
//            }
//        }
//    }
//
//    // Token separators
//    static final byte BYTE_SPACE = (byte)' ';
//    static final byte BYTE_TAB = (byte)'\t';
//    static final byte BYTE_NEWLINE = (byte)'\n';
//
//    /** Tokenizes UTF-8 input and submits tokens, operating on raw bytes
//    *   This is safe because all separators are 1-byte encodings...
//    *   and all successive bytes of multibyte encodings have the high bit set
//    *   @return Length of residual at beginning of array, that is part of next token
//    */
//    public static int tokenizeAndSubmitBlock(WordMap map,
//        final byte[] inputBuffer, int startIndex, int length) {
//
//        int tokenStartIndex = 0;
//        int endIndex = startIndex + length;
//
//        map.rehash();
//
//        byte[][] keys = map.keys;
//        int[] values = map.values;
//        int size = map.size;
//        int tableMask = map.tableMask;
//
//        // Go from start to end of current read
//        for (int i=startIndex; i<endIndex; i++) {
//            byte b = inputBuffer[i];
//
//            if (b == BYTE_SPACE || b == BYTE_NEWLINE || b == BYTE_TAB) { // Token end
//                if (i != tokenStartIndex) {
//                    size += submitWord(keys, values, tableMask,
//                            inputBuffer, tokenStartIndex, i - tokenStartIndex);
//                    tokenStartIndex = i;
//                }
//                tokenStartIndex++;
//            }
//        }
//
//        map.size = size;
//
//        // Copy residual token content to beginning of the array, so we can read in more data
//        int residualSize =  endIndex - tokenStartIndex;
//        if (residualSize > 0) {
//            System.arraycopy(inputBuffer, tokenStartIndex, inputBuffer, 0, residualSize);
//            return residualSize;
//        }
//        return 0;
//    }
//
//    static class WordMap {
//        int tableSize = 64 * 1024 * 1024;
//        int tableMask = tableSize - 1;
//        byte[][] keys = new byte[tableSize][];
//        int[] values = new int[tableSize];
//        int size = 0;
//
//        void rehash() {
//            if (size <= (int) (tableSize * 0.6))
//                return;
//            tableSize *= 2;
//            int tableMask = this.tableMask = tableSize - 1;
//            byte[][] oldKeys = this.keys;
//            int[] oldValues = this.values;
//            byte[][] keys = this.keys = new byte[tableSize][];
//            int[] values = this.values = new int[tableSize];
//            for (int i = 0; i < oldKeys.length; i++) {
//                byte[] key = oldKeys[i];
//                if (key != null) {
//                    int hashCode = (int) XX_HASH.hashBytes(key);
//                    int index = hashCode & tableMask;
//                    while (keys[index] != null) {
//                        index = (index + 1) & tableMask;
//                    }
//                    keys[index] = key;
//                    values[index] = oldValues[i];
//                }
//            }
//        }
//    }
//
//    /**
//     * Returns number of new entries in the table (0 or 1)
//     */
//    private static int submitWord(
//            byte[][] keys, int[] values, int tableMask,
//            byte[] word, int off, int len) {
//        int hashCode = (int) XX_HASH.hashBytes(word, off, len);
//        int index = hashCode & tableMask;
//        while (true) {
//            byte[] key = keys[index];
//            if (key != null) {
//                if (equal(key, word, off, len)) {
//                    values[index]++;
//                    return 0;
//                }
//                index = (index + 1) & tableMask;
//            } else {
//                keys[index] = Arrays.copyOfRange(word, off, off + len);
//                values[index] = 1;
//                return 1;
//            }
//        }
//    }
//
//    private static boolean equal(byte[] key, byte[] word, int off, int len) {
//        if (key.length != len)
//            return false;
//        for (int i = 0; i < len; i++) {
//            if (key[i] != word[off + i])
//                return false;
//        }
//        return true;
//    }
//
//    public static void main(String[] args) throws IOException {
//        // System.err.println("Parsing and adding to map");
//        long startTime = System.currentTimeMillis();
//        InputStream stdin = System.in;
//        WordMap map = new WordMap();
//
//        byte[] buff = new byte[16384];
//
//        // Read through chunks, carrying over content that is part of a token
//        int readAmount;
//        int offset = 0;
//        while ((readAmount = stdin.read(buff, offset, buff.length-offset)) > 0 ) {
//            offset = tokenizeAndSubmitBlock(map, buff, offset, readAmount);
//            if (offset == buff.length) {
//                // Token longer than buffer, double buffer size and keep reading
//                buff = Arrays.copyOf(buff, buff.length<<1);
//            }
//        }
//        if (offset > 0) {
//            map.size += submitWord(map.keys, map.values, map.tableMask, buff, 0, offset);
//        }
//        long endTime = System.currentTimeMillis();
//        // System.err.println("Parsing/map addition time (ms): "+(endTime-startTime));
//
//        // System.err.println("Creating Count objects for sorting");
//        startTime = System.currentTimeMillis();
//
//        // Separating singleton tokens from multiples lets us store & sort them more efficiently
//        // TODO allocate arrays more wisely not just size/2 and size/2
//        final ArrayList<CountForWord> multiples = new ArrayList<CountForWord>(map.size/2);
//        final ArrayList<byte[]> singles = new ArrayList<byte[]>(map.size/2);
//        byte[][] keys = map.keys;
//        int[] values = map.values;
//        for (int i = 0; i < keys.length; i++) {
//            byte[] key = keys[i];
//            if (key != null) {
//                int count = values[i];
//                if (count > 1) {
//                    multiples.add(new CountForWord(key, count));
//                } else {
//                    singles.add(key);
//                }
//            }
//        }
//        map = null; // Just-in-case, this allows for GC
//        endTime = System.currentTimeMillis();
//        // System.err.println("Count object creation time (ms): "+(endTime-startTime));
//
//        // System.err.println("sorting...");
//        startTime = System.currentTimeMillis();
//        Collections.sort(multiples);
//        endTime = System.currentTimeMillis();
//        // System.err.println("Sorting multiples time (ms): "+(endTime-startTime)+ " with count: "+multiples.size());
//
//        startTime = System.currentTimeMillis();
//        fastRadixSort(singles, 0);
//        endTime = System.currentTimeMillis();
//        // System.err.println("Sorting single time (ms): "+(endTime-startTime) + " with count: "+singles.size());
//
//        // System.err.println("output...");
//        startTime = System.currentTimeMillis();
//        // Output is buffered to reduce stream I/O overhead
//        BufferedOutputStream out = new BufferedOutputStream(System.out);
//        for (CountForWord c : multiples) {
//            out.write(c.word);
//            out.write(("\t"+c.count+'\n').getBytes(StandardCharsets.UTF_8));
//        }
//        // Finally we write singletons out
//        byte[] simpleSuffix = "\t1\n".getBytes(StandardCharsets.UTF_8);
//        for (byte[] b : singles) {
//            out.write(b);
//            out.write(simpleSuffix);
//        }
//        out.close();
//        endTime = System.currentTimeMillis();
//        // System.err.println("Output time (ms): "+(endTime-startTime));
//    }
//}
