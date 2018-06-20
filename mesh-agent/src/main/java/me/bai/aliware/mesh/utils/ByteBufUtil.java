package me.bai.aliware.mesh.utils;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class ByteBufUtil {

	public static void print(ByteBuf in) {
		System.out.println(toByteString(in));
		System.out.println(in);
	}

	public static String toByteString(ByteBuf in) {
		byte[] t = read(in);
		return Arrays.toString(t);
	}

	public static void printAsChar(ByteBuf in) {
		byte[] t = read(in);
		for (int i = 0; i < t.length; i++) {
			System.out.print((char) t[i]);
		}
		System.out.println();
	}

	private static byte[] read(ByteBuf in) {
		byte[] t = new byte[in.readableBytes()];
		in.getBytes(in.readerIndex(), t);
		return t;
	}

	public static String getString(ByteBuf in) {
		byte[] t = read(in);
		return new String(t);
	}

	public static int strHash(ByteBuf in) {
		return getString(in).hashCode();
	}

	public static int toInt(ByteBuf in) {
		return Integer.valueOf(getString(in));
	}
}
