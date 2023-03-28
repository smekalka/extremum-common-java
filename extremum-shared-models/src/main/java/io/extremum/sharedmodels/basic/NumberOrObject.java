package io.extremum.sharedmodels.basic;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NumberOrObject<N extends Number, O> {

    private N number;
    private O object;


    public NumberOrObject(N number) {
        this.number = number;
    }

    public NumberOrObject(O object) {
        this.object = object;
    }

    public boolean isNumber() {
        return this.number != null;
    }

    public boolean isObject() {
        return this.object != null;
    }


    public N getNumber() {
        return number;
    }

    public void setNumber(N number) {
        this.number = number;
    }

    public O getObject() {
        return object;
    }

    public void setObject(O object) {
        this.object = object;
    }

}
