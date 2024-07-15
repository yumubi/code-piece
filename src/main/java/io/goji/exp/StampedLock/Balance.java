package io.goji.exp.StampedLock;

public class Balance {
    private long amount;

    public Balance(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
