package io.extremum.sharedmodels.spacetime;

public class FrameCode {
    private int num;
    private int base;
    private int rate;

    public FrameCode(int num, int base, int rate) {
        this.num = num;
        this.base = base;
        this.rate = rate;
    }

    public FrameCode() {
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
