package io.goji.jav.mdc;

import java.util.concurrent.*;

public class Main {
    public static ThreadLocal<String> context = new ThreadLocal<>();
    public static void main(String[] args) {
        context.set("123456");
       var executor =  new ScheduledThreadPoolExecutor(1){
            @Override
            protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
                return new MDCRunnableScheduledFuture<>(task, context.get());
            }
        };
       executor.submit(()->{
           System.out.println(context.get());
       });
       executor.schedule(()->{
           System.out.println(context.get());
       }, 1, TimeUnit.SECONDS);
    }

}
class MDCRunnableScheduledFuture<V> implements RunnableScheduledFuture<V> {
    private RunnableScheduledFuture<V> origin;
    private String snap;

    MDCRunnableScheduledFuture(RunnableScheduledFuture<V> origin, String snap) {
        this.origin = origin;
        this.snap = snap;
    }

    @Override
    public boolean isPeriodic() {
        return origin.isPeriodic();
    }

    @Override
    public void run() {
        Main.context.set(snap);
        try {
            origin.run();
        }finally {
            Main.context.remove();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return origin.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return origin.isCancelled();
    }

    @Override
    public boolean isDone() {
        return origin.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return origin.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return origin.get(timeout, unit);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return origin.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return origin.compareTo(o);
    }
}
