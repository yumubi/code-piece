//
//
//public class App {
//
//    public static volatile int count;
//
//    public static void main(String[] args) {
//        new OddThread().start();
//        new EvenThread().start();
//
//
//        }
//    }
//
//    public static void print(int i) {
//        System.out.printf("%s - %d%n", Thread.currentThread().getName(), i);
//    }
//
//    public static class OddThread extends Thread {
//        public OddThread() {
//            super("Odd");
//        }
//
//        @Override
//        public void run() {
//            for (; ; ) {
//                int c = count;
//                if (c >= 100) break;
//
//                if (++c % 2 == 1) {
//                    print(c);
//                    count = c;
//                }
//            }
//        }
//    }
//
//    public static class EvenThread extends Thread {
//        public EvenThread() {
//            super("Even");
//        }
//
//        @Override
//        public void run() {
//            for (; ; ) {
//                int c = count;
//                if (c >= 100) break;
//
//                if (++c % 2 == 0) {
//                    print(c);
//                    count = c;
//                }
//            }
//        }
//    }
//}
//
//public void main() {
//}
