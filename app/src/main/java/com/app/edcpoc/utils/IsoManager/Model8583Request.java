package com.app.edcpoc.utils.IsoManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Model8583Request {
    public ArrayList<Model8583Bit> bits_sending;
    public String mti;
    public String tpdu;
    public HashMap<Integer,Model8583Bit> specs;
    public String getTPDU() throws Exception {
        if(tpdu==null){
            throw new Exception("TPDU is not set");
        }
        if(tpdu.length()!=10){
            throw new Exception("TPDU length must be 10 hex characters");
        }
        return tpdu;
    }
    public String getMTI() throws Exception {
        if(mti==null){
            throw new Exception("MTI is not set");
        }
        if(mti.length()!=4){
            throw new Exception("MTI length must be 4 characters");
        }
        return mti;
    }
    public Model8583Request() {
        this.specs = new HashMap<>();
        this.bits_sending = new ArrayList<>();
    }
    public Model8583Request setSpecs(HashMap<Integer,Model8583Bit> specs) {
        this.specs = specs;
        return this;
    }
    public Model8583Request setMTI(String mti) {
        this.mti = mti;
        return this;
    }
    public Model8583Request setTPDU(String tpdu) {
        this.tpdu = tpdu;
        return this;
    }
}
