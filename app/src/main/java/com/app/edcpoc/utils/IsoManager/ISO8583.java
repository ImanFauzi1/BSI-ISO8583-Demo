package com.app.edcpoc.utils.IsoManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ISO8583 {
    public final static int LEN_0 = 0;
    public final static int LEN_1HALF = 11;
    public final static int LEN_1FULL = 12;
    public final static int LEN_2HALF = 21;
    public final static int LEN_2FULL = 22;
    public final static int LEN_3HALF = 31;
    public final static int LEN_3FULL = 32;
    public final static int LEN_4HALF = 41;
    public final static int LEN_4FULL = 42;
    public final static int LENHEX_1HALF = 111;
    public final static int LENHEX_1FULL = 112;
    public final static int LENHEX_2HALF = 121;
    public final static int LENHEX_2FULL = 122;
    public final static int LENHEX_3HALF = 131;
    public final static int LENHEX_3FULL = 132;
    public final static int LENHEX_4HALF = 141;
    public final static int LENHEX_4FULL = 142;

    public final static int FORMAT_ORIGINAL = 0;

    public static String packToHex(Model8583Request model8583Request) throws Exception {
//        Complete ISO8583 packing logic should be implemented here
//        complete = data length (2B) + TPDU (5B) + MTI (2B) + Bitmap (8B) + Data Elements
        String tpdu = model8583Request.getTPDU();
        if(!isAllHex(tpdu)){
            throw new Exception("TPDU must be in hex format");
        }
        String mti = model8583Request.getMTI();
        if(!isAllHex(mti)){
            throw new Exception("MTI must be in hex format");
        }
        String bitmap = bitmapGenerator(model8583Request);
        Log.d("NPLOG","Generated bitmap="+bitmap);
        if(bitmap.length()%16!=0){
            throw new Exception("Bitmap length must be multiple of 16 hex characters");
        }
        if(model8583Request.bits_sending.isEmpty()){
            throw new Exception("No data elements to send");
        }
        StringBuilder data = new StringBuilder();
        for(Model8583Bit bit:model8583Request.bits_sending){
            Log.d("NPLOG","Validating data element number="+bit.number);
            Log.d("NPLOG","Data element details: name="+bit.name+", len="+bit.len+", value="+bit.value);
            if(!model8583Request.specs.containsKey(bit.number)){
                throw new Exception("Data element specification for bit number "+bit.number+" not found");
            }
            Model8583Bit specBit = model8583Request.specs.get(bit.number);
            assert specBit != null;
            if(specBit.len!=bit.len||!specBit.name.equals(bit.name)){
                throw new Exception("Data element specification length format / name for bit number "+bit.number+" does not match with sending data element. Name: "+specBit.name+":"+bit.name+", Len: "+specBit.len+":"+bit.len);
            }
            if(specBit.number!=bit.number){
                throw new Exception("Data element specification number for bit number "+bit.number+" does not match with sending data element");
            }
            if(bit.number==1){
                throw new Exception("Data element number 1 is reserved for bitmap and cannot be set manually");
            }
            Log.d("NPLOG","Processing data element number="+bit.number);
            if(bit.function!=null && bit.function.equals("STAN")){
                //generate STAN
                long stan = System.currentTimeMillis()%1000000;
                bit.value = String.format("%06d",stan);
            }
            if(!isAllHex(bit.value)){
                throw new Exception("Data element number "+bit.number+" must be in hex format");
            }
            if(bit.value.equals("")){
                throw new Exception("Data element number "+bit.number+" value is empty");
            }
            switch (bit.len){
                case LEN_0:
                    //fixed length, no length prefix
                    data.append(bit.value);
                    break;
                case LENHEX_1HALF:
                    processPackHexHalf(data,1,""+bit.number,bit.value);
                    break;
                case LENHEX_1FULL:
                    processPackHexFull(data,1,bit.value);
                    break;
                case LEN_1HALF:
                    processPackHalf(data,1,""+bit.number,bit.value);
                    break;
                case LEN_1FULL:
                    processPackFull(data,1,bit.value);
                    break;
                case LENHEX_2HALF:
                    processPackHexHalf(data,2,""+bit.number,bit.value);
                    break;
                case LENHEX_2FULL:
                    processPackHexFull(data,2,bit.value);
                    break;
                case LEN_2HALF:
                    processPackHalf(data,2,""+bit.number,bit.value);
                    break;
                case LEN_2FULL:
                    processPackFull(data,2,bit.value);
                    break;
                case LENHEX_3HALF:
                    processPackHexHalf(data,3,""+bit.number,bit.value);
                    break;
                case LENHEX_3FULL:
                    processPackHexFull(data,3,bit.value);
                    break;
                case LEN_3HALF:
                    processPackHalf(data,3,""+bit.number,bit.value);
                    break;
                case LEN_3FULL:
                    processPackFull(data,3,bit.value);
                    break;
                case LENHEX_4HALF:
                    processPackHexHalf(data,4,""+bit.number,bit.value);
                    break;
                case LENHEX_4FULL:
                    processPackHexFull(data,4,bit.value);
                    break;
                case LEN_4HALF:
                    processPackHalf(data,4,""+bit.number,bit.value);
                    break;
                case LEN_4FULL:
                    processPackFull(data,4,bit.value);
                    break;
                default:
                    throw new Exception("Length format "+bit.len+" not implemented yet");
            }
        }
        if(data.length()%2!=0){
            data.append("0");
        }
        String hex = mti+bitmap+data;
        String semiCompleteHex = tpdu + hex;
        int dataLength = semiCompleteHex.length()/2;
        if(dataLength>9999){
            throw new Exception("Total data length exceeds maximum of 9999 bytes");
        }
        String dataLengthHex = String.format("%04X",dataLength);
        String completeHex = dataLengthHex+semiCompleteHex;
        Log.d("NPLOG","Complete packed hex="+completeHex);
        return completeHex;
    }

    private static void processPackFull(StringBuilder data,int lengthMode,String value) throws Exception {
        int len1 = value.length();
        if(len1>=Math.pow(10,lengthMode)){
            throw new Exception("Data element length exceeds maximum for length mode "+lengthMode);
        }
        data.append(String.format("%0"+lengthMode+"d", len1)).append(value);
    }
    private static void processPackHalf(StringBuilder data,int lengthMode,String bitNumber,String value) throws Exception {
        int len1half = value.length();
        if(len1half%2!=0){
            throw new Exception("Data element number "+bitNumber+" length must be even number of characters. It is "+value);
        }
        int halfLength = len1half/2;
        if(halfLength>=Math.pow(10,lengthMode)){
            throw new Exception("Data element length exceeds maximum for length mode "+lengthMode);
        }
        data.append(String.format("%0"+lengthMode+"d", halfLength)).append(value);
    }
    private static void processPackHexFull(StringBuilder data,int lengthMode,String value) throws Exception {
        int len1 = value.length();
        if(len1>=Math.pow(16,lengthMode)){
            throw new Exception("Data element length exceeds maximum for length mode "+lengthMode);
        }
        data.append(String.format("%0"+lengthMode+"X", len1)).append(value);
    }
    private static void processPackHexHalf(StringBuilder data,int lengthMode,String bitNumber,String value) throws Exception {
        int len1half = value.length();
        if(len1half%2!=0){
            throw new Exception("Data element number "+bitNumber+" length must be even number of characters. It is "+value);
        }
        int halfLength = len1half/2;
        if(halfLength>=Math.pow(16,lengthMode)){
            throw new Exception("Data element length exceeds maximum for length mode "+lengthMode);
        }
        data.append(String.format("%0"+lengthMode+"X", halfLength)).append(value);
    }

    private static boolean isAllHex(String str) {
        return str.matches("[0-9A-Fa-f]+");
    }
    private static boolean isAllNumber(String str){
        return str.matches("[0-9]+");
    }

    private static String hexToBinary(String hex){
        StringBuilder binary = new StringBuilder();
        for(int i=0;i<hex.length();i++){
            char c = hex.charAt(i);
            int decimal = Integer.parseInt(String.valueOf(c),16);
            String binChunk = String.format("%04d",Integer.parseInt(Integer.toBinaryString(decimal)));
            binary.append(binChunk);
        }
        return binary.toString();
    }

    private static String bitmapGenerator(Model8583Request model8583Request) {
        ArrayList<String> bitmapStack = new ArrayList<>();
        for(Model8583Bit bit:model8583Request.bits_sending){
            Log.d("NPLOG","Processing bit number="+bit.number);
            checkBitmap(bitmapStack,bit);
            Log.d("NPLOG","bitmapStack.length="+bitmapStack.size());
            int bitmapBlockActive = (int)Math.ceil((double)bit.number/64.0)-1;
            Log.d("NPLOG","bitmapBlockActive="+bitmapBlockActive);
            int indexInBlock = (bit.number - 1) % 64;
            char[] tb = bitmapStack.get(bitmapBlockActive).toCharArray();
            tb[indexInBlock] = '1';
            bitmapStack.set(bitmapBlockActive,String.valueOf(tb));
        }
        return binaryToHex(String.join("",bitmapStack));
    }
    private static String binaryToHex(String binary){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<binary.length();i+=4){
            String chunk = binary.substring(i,i+4);
            int decimal = Integer.parseInt(chunk,2);
            sb.append(Integer.toHexString(decimal).toUpperCase());
        }
        return sb.toString();
    }
    private static String generateSetBitmap(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<64;i++) {
            sb.append('0');
        }
        return sb.toString();
    }
    private static String checkBitmap(ArrayList<String> bitmapStack,Model8583Bit bit){
        int bitmapBlock = (int)Math.ceil((double)bit.number/64.0);
        for(int i=bitmapStack.size();i<bitmapBlock;i++) {
            bitmapStack.add(generateSetBitmap());
            if(i>0){
                char[] tb = bitmapStack.get(i-1).toCharArray();
                tb[0] = '1';
                bitmapStack.set(i-1,String.valueOf(tb));
            }
        }
        return "";
    }

    public static HashMap<String,String> unpackFromHex(String hex,Model8583Request model8583Request) throws Exception {
        HashMap<String, String> unpackedData = new HashMap<>();
        if (hex.length() < (4 + 10 + 4 + 16)) {
            throw new Exception("Hex data length is too short to be a valid ISO8583 message");
        }
        String length = hex.substring(0, 4);
        int len = Integer.parseInt(length, 16);
        if (hex.length() - 4 != len * 2) {
            throw new Exception("Hex data length does not match with length prefix");
        }
        if(!isAllHex(hex)){
            throw new Exception("Hex data contains non-hex characters");
        }
        String tpdu = hex.substring(4, 14);
        String mti = hex.substring(14, 18);
        String remainingContent = hex.substring(18);
        String firstBit = "1";
        String bitmap = "";
        int i = 0;
        while (firstBit.equals("1")) {
            if (remainingContent.length() < 16) {
                throw new Exception("Hex data length is too short to contain complete bitmap");
            }
            String currentBitmapHex = remainingContent.substring(i * 16, (i + 1) * 16);
            String currentBitmapBinary = hexToBinary(currentBitmapHex);
            bitmap += currentBitmapBinary;
            firstBit = String.valueOf(currentBitmapBinary.charAt(0));
            i += 1;
        }
        ArrayList<String> activeBitmaps = activeBitmaps(bitmap);
        String bitmapHex = remainingContent.substring(0, i * 16);
        String dataElementsHex = remainingContent.substring(i * 16);
        parseData(activeBitmaps, model8583Request, unpackedData, dataElementsHex);
        unpackedData.put("tpdu", tpdu);
        unpackedData.put("mti", mti);
        unpackedData.put("bitmapHex", bitmapHex);
        unpackedData.put("bitmap", bitmap);
        unpackedData.put("data", dataElementsHex);
        unpackedData.put("activeBitmaps", String.join(",", activeBitmaps));
        Log.d("NPLOG", "Unpacking ISO8583 message:");
        return unpackedData;
    }
    private static void parseData(ArrayList<String> activeBitmap, Model8583Request model8583Request,HashMap<String, String> unpackedData, String dataElementsHex) throws Exception {
        for (String bitNumberStr : activeBitmap) {
            int bitNumber = Integer.parseInt(bitNumberStr);
            if (!model8583Request.specs.containsKey(bitNumber)) {
                throw new Exception("Data element specification for bit number " + bitNumber + " not found");
            }
            Model8583Bit bitSpec = model8583Request.specs.get(bitNumber);
            switch (bitSpec.len) {
                case LEN_0:
                    if(dataElementsHex.length()<bitSpec.lenFixed){
                        throw new Exception("Data element number "+bitNumber+" length ("+dataElementsHex.length()+") is less than expected fixed length "+bitSpec.lenFixed);
                    }
                    unpackedData.put(bitNumberStr,dataElementsHex.substring(0,bitSpec.lenFixed));
                    dataElementsHex = dataElementsHex.substring(bitSpec.lenFixed);
                    break;
                case LENHEX_1HALF:
                    dataElementsHex = processUnpackHalfHex(unpackedData,bitNumberStr,1,dataElementsHex);
                    break;
                case LEN_1HALF:
                    dataElementsHex = processUnpackHalf(unpackedData,bitNumberStr,1,dataElementsHex);
                    break;
                case LENHEX_1FULL:
                    dataElementsHex = processUnpackFullHex(unpackedData,bitNumberStr,1,dataElementsHex);
                    break;
                case LEN_1FULL:
                    dataElementsHex = processUnpackFull(unpackedData,bitNumberStr,1,dataElementsHex);
                    break;
                case LENHEX_2HALF:
                    dataElementsHex = processUnpackHalfHex(unpackedData,bitNumberStr,2,dataElementsHex);
                    break;
                case LEN_2HALF:
                    dataElementsHex = processUnpackHalf(unpackedData,bitNumberStr,2,dataElementsHex);
                    break;
                case LENHEX_2FULL:
                    dataElementsHex = processUnpackFullHex(unpackedData,bitNumberStr,2,dataElementsHex);
                    break;
                case LEN_2FULL:
                    dataElementsHex = processUnpackFull(unpackedData,bitNumberStr,2,dataElementsHex);
                    break;
                case LENHEX_3HALF:
                    dataElementsHex = processUnpackHalfHex(unpackedData,bitNumberStr,3,dataElementsHex);
                    break;
                case LEN_3HALF:
                    dataElementsHex = processUnpackHalf(unpackedData,bitNumberStr,3,dataElementsHex);
                    break;
                case LENHEX_3FULL:
                    dataElementsHex = processUnpackFullHex(unpackedData,bitNumberStr,3,dataElementsHex);
                    break;
                case LEN_3FULL:
                    dataElementsHex = processUnpackFull(unpackedData,bitNumberStr,3,dataElementsHex);
                    break;
                case LENHEX_4HALF:
                    dataElementsHex = processUnpackHalfHex(unpackedData,bitNumberStr,4,dataElementsHex);
                    break;
                case LEN_4HALF:
                    dataElementsHex = processUnpackHalf(unpackedData,bitNumberStr,4,dataElementsHex);
                    break;
                case LENHEX_4FULL:
                    dataElementsHex = processUnpackFullHex(unpackedData,bitNumberStr,4,dataElementsHex);
                    break;
                case LEN_4FULL:
                    dataElementsHex = processUnpackFull(unpackedData,bitNumberStr,4,dataElementsHex);
                    break;
                default:
                    throw new Exception("Length format " + bitSpec.len + " not implemented yet");
            }
        }
        if(dataElementsHex.length()>1){
            throw new Exception("Remaining data elements hex has too many leftovers, remaining="+dataElementsHex);
        }
    }
    private static String processUnpackHalfHex(HashMap<String,String> unpackedData,String bitNumberStr,int templateLength,String dataElementsHex) throws Exception {
        int lenHex = Integer.parseInt(dataElementsHex.substring(0, templateLength), 16)*2;
        if(dataElementsHex.length()<templateLength+lenHex){
            throw new Exception("Data element length ("+dataElementsHex.length()+") is less than expected length "+(templateLength+lenHex));
        }
        unpackedData.put(bitNumberStr,dataElementsHex.substring(templateLength,templateLength+lenHex));
        return dataElementsHex.substring(templateLength+lenHex);
    }
    private static String processUnpackFullHex(HashMap<String,String> unpackedData,String bitNumberStr,int templateLength,String dataElementsHex) throws Exception {
        int lenHex = Integer.parseInt(dataElementsHex.substring(0, templateLength), 16);
        if(dataElementsHex.length()<templateLength+lenHex){
            throw new Exception("Data element length ("+dataElementsHex.length()+") is less than expected length "+(templateLength+lenHex));
        }
        unpackedData.put(bitNumberStr,dataElementsHex.substring(templateLength,templateLength+lenHex));
        return dataElementsHex.substring(templateLength+lenHex);
    }
    private static String processUnpackHalf(HashMap<String,String> unpackedData,String bitNumberStr,int templateLength,String dataElementsHex) throws Exception {
        String s1half = dataElementsHex.substring(0, templateLength);
        if(!isAllNumber(s1half)){
            throw new Exception("Data element number "+bitNumberStr+" length prefix is not numeric: "+s1half);
        }
        int len1half = Integer.parseInt(s1half)*2;
        if(dataElementsHex.length()<templateLength+len1half){
            throw new Exception("Data element number "+bitNumberStr+" length ("+dataElementsHex.length()+") is less than expected length "+(templateLength+len1half));
        }
        unpackedData.put(bitNumberStr,dataElementsHex.substring(templateLength,templateLength+len1half));
        return dataElementsHex.substring(templateLength+len1half);
    }
    private static String processUnpackFull(HashMap<String,String> unpackedData,String bitNumberStr,int templateLength,String dataElementsHex) throws Exception {
        String s1half = dataElementsHex.substring(0, templateLength);
        if(!isAllNumber(s1half)){
            throw new Exception("Data element number "+bitNumberStr+" length prefix is not numeric: "+s1half);
        }
        int len1half = Integer.parseInt(s1half);
        if(dataElementsHex.length()<templateLength+len1half){
            throw new Exception("Data element number "+bitNumberStr+" length ("+dataElementsHex.length()+") is less than expected length "+(templateLength+len1half));
        }
        unpackedData.put(bitNumberStr,dataElementsHex.substring(templateLength,templateLength+len1half));
        return dataElementsHex.substring(templateLength+len1half);
    }
    private static ArrayList<String> activeBitmaps(String binaryBitmap){
        ArrayList<String> activeBitmaps = new ArrayList<>();
        for(int i=0;i<binaryBitmap.length();i++){
            char c = binaryBitmap.charAt(i);
            if(c=='1'){
                activeBitmaps.add(""+(i+1));
            }
        }
        return activeBitmaps;
    }
}
