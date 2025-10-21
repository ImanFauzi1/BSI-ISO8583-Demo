package com.app.edcpoc.utils.IsoManager;

public class Model8583Bit {
    public String name;
    public String function;
    public int number;
    public int len;
    public int lenFixed;
    public String value;
    public Model8583Bit(int number, String name, int len, String value) {
        this.number = number;
        this.name = name;
        this.len = len;
        this.value = value;
    }
    public Model8583Bit(int number, String name, int len, int lenFixed) {
        this.number = number;
        this.name = name;
        this.len = len;
        this.lenFixed = lenFixed;
    }
    public Model8583Bit(int number, String name, int len) {
        this(number, name, len, 0);
    }
    public Model8583Bit setFunction(String function) {
        this.function = function;
        return this;
    }
}
