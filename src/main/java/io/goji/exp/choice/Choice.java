package io.goji.exp.choice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class Choice {
    /**
     * 方法map
     */
    private Map<Object, Function> map;
    /**
     * 默认方法
     */
    private Function Default;
    /**
     * 表达式支持
     */
    private boolean expression;

    public Choice() {
        map = new ConcurrentHashMap<>();
    }

    public Choice(boolean expression) {
        this();
        this.expression = expression;
    }

    public Choice add(Object v, Function function) {
        if (v != null && function != null) {
            map.put(v, function);
        }
        return this;
    }

    public Choice add(Function function, Object... v) {
        if (function != null) {
            for (Object iv : v) {
                if (iv != null) {
                    map.put(iv, function);
                }
            }
        }
        return this;
    }

    /**
     * 不传值的执行
     */
    @SuppressWarnings("rawtypes, unchecked")
    public void execute() {
        AtomicBoolean flg = new AtomicBoolean(false);
        //遍历map
        for (Map.Entry<Object, Function> entry : map.entrySet()) {
            Object iv = entry.getKey();
            if (iv != null) {
                Function iFun = entry.getValue();
                //类型判断
                if (expression && iv.getClass().equals(Boolean.class)) {
                    if (iv.equals(true)) {
                        //类型一样eq为true 执行方法
                        iFun.apply(null);
                        flg.set(true);
                        return;
                    }
                }
            }
        }
        if (!flg.get() && Default != null) {
            Default.apply(null);
        }
    }

    /**
     * 传值的执行
     *
     * @param v 匹配值
     */
    @SuppressWarnings("unchecked, rawtypes")
    public void execute(Object v) {
        if (v == null) {
            return;
        }
        AtomicBoolean flg = new AtomicBoolean(false);
        Function fastTrue = null;
        for (Map.Entry<Object, Function> entry : map.entrySet()) {
            Object iv = entry.getKey();
            Function iFun = entry.getValue();
            //类型判断
            if (v.getClass().equals(iv.getClass())) {
                if (v.equals(iv)) {
                    iFun.apply(null);
                    flg.set(true);
                    return;
                }
            }
            //表达式
            if (expression && iv.getClass().equals(Boolean.class)) {
                if (v.equals(true)) {
                    fastTrue = iFun;
                    flg.set(true);
                }
            }
        }
        if (flg.get()) {
            if (expression && fastTrue != null) {
                fastTrue.apply(null);
            }
        } else if (Default != null) {
            Default.apply(null);
        }
    }

    @SuppressWarnings("rawtypes")
    public Choice Default(Function function) {
        Default = function;
        return this;
    }


}
