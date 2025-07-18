package client;

import java.io.*;
import java.nio.*;
import java.util.*;


class Utils{

    public static byte[] marshal(String s) throws UnsupportedEncodingException{
        byte[] ret = new byte[s.length()];
        for(int i = 0; i < s.length(); i++) {
            ret[i] = (byte)s.charAt(i);
        }
        return ret;
    }

    public static String unmarshalString(byte[] b, int start, int end){
        char[] c = new char[end - start];
        for(int i = start; i < end; i++) {
            c[i-start] = (char)(b[i]);
        }
        return new String(c);
    }

    public static String unmarshalMsgString(byte[] b, int start){
        int len = Utils.unmarshalInteger(b, start);
        int resStart = start + Constants.INT_SIZE;
        return Utils.unmarshalString(b, resStart, resStart+len);
    }

    public static byte[] marshal(int x){
        return new byte[]{
          (byte)(x >> 24),
          (byte)(x >> 16),
          (byte)(x >> 8),
          (byte)(x >> 0)
        };
    }

    public static int unmarshalInteger(byte[] b, int start){
        return b[start] << 24 | (b[start+1] & 0xFF) << 16 | (b[start+2] & 0xFF) << 8 | (b[start+3] & 0xFF);
    }

    public static int unmarshalMsgInteger(byte[] b, int start){
        return Utils.unmarshalInteger(b, start+Constants.INT_SIZE);
    }

    public static boolean unmarshalBool(byte[] b, int start){
        return b[start] == 1;
    }

    public static byte[] marshal(float f){
        return ByteBuffer.allocate(Constants.FLOAT_SIZE).putFloat(f).array();
    }

    public static float unmarshalFloat(byte[] b, int start){
        byte[] content = new byte[]{
            b[start], b[start+1], b[start+2], b[start+3]
        };
        return ByteBuffer.wrap(content).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    public static float unmarshalMsgFloat(byte[] b, int start){
        return Utils.unmarshalFloat(b, start+Constants.INT_SIZE);
    }

	public static Byte[] byteBoxing(byte[] b){
		Byte[] ret = new Byte[b.length];
		for(int i = 0; i < b.length; i++)
			ret[i] = Byte.valueOf(b[i]);
		return ret;
	}

	public static byte[] byteUnboxing(Byte[] b){
		byte[] ret = new byte[b.length];
		for(int i = 0; i < b.length; i++)
			ret[i] = b[i].byteValue();
		return ret;
    }

    public static byte[] byteUnboxing(List list){
        return Utils.byteUnboxing((Byte[])list.toArray(new Byte[list.size()]));
    }

    public static void appendMessage(List list, String s)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            s.length()
        ))));

        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            s
        ))));
    }

    public static void appendMessage(List list, int x)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            Constants.INT_SIZE
        ))));

        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            x
        ))));
    }

    public static void appendMessage(List list, float f)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            Constants.FLOAT_SIZE
        ))));

        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            f
        ))));
    }

    public static void appendMessage(List list, byte[] b)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            b.length
        ))));

        list.addAll(Arrays.asList(Utils.byteBoxing(
            b
        )));
    }

    public static void append(List list, String s)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            s
        ))));
    }

    public static void append(List list, int x)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            x
        ))));
    }

    public static void append(List list, float f)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(Utils.marshal(
            f
        ))));
    }

    public static void append(List list, byte[] b)throws UnsupportedEncodingException{
        list.addAll(Arrays.asList(Utils.byteBoxing(
            b
        )));
    }

}
