package com.qust.account.vpn;

import androidx.annotation.NonNull;

import java.math.BigInteger;

public class VpnEncodeUtil{
	
	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
	
	public static final int[] table = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
	
	public static final int[] table2 = {14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2,
			41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};
	
	public static final int[] table3 = {58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
			57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7};
	
	public static final int[] table4 = {40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
			36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25};
	
	public static final int[] tableE = {32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1};
	
	public static final int[] tableP = {16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
	
	public static final long[][][] SBox = new long[][][]{ {
			{14L, 4L, 13L, 1L, 2L, 15L, 11L, 8L, 3L, 10L, 6L, 12L, 5L, 9L, 0L, 7}, {0L, 15L, 7L, 4L, 14L, 2L, 13L, 1L, 10L, 6L, 12L, 11L, 9L, 5L, 3L, 8},
			{4L, 1L, 14L, 8L, 13L, 6L, 2L, 11L, 15L, 12L, 9L, 7L, 3L, 10L, 5L, 0}, {15L, 12L, 8L, 2L, 4L, 9L, 1L, 7L, 5L, 11L, 3L, 14L, 10L, 0L, 6L, 13}
	}, {
			{15L, 1L, 8L, 14L, 6L, 11L, 3L, 4L, 9L, 7L, 2L, 13L, 12L, 0L, 5L, 10}, {3L, 13L, 4L, 7L, 15L, 2L, 8L, 14L, 12L, 0L, 1L, 10L, 6L, 9L, 11L, 5},
			{0L, 14L, 7L, 11L, 10L, 4L, 13L, 1L, 5L, 8L, 12L, 6L, 9L, 3L, 2L, 15}, {13L, 8L, 10L, 1L, 3L, 15L, 4L, 2L, 11L, 6L, 7L, 12L, 0L, 5L, 14L, 9}
	}, {
			{10L, 0L, 9L, 14L, 6L, 3L, 15L, 5L, 1L, 13L, 12L, 7L, 11L, 4L, 2L, 8}, {13L, 7L, 0L, 9L, 3L, 4L, 6L, 10L, 2L, 8L, 5L, 14L, 12L, 11L, 15L, 1},
			{13L, 6L, 4L, 9L, 8L, 15L, 3L, 0L, 11L, 1L, 2L, 12L, 5L, 10L, 14L, 7}, {1L, 10L, 13L, 0L, 6L, 9L, 8L, 7L, 4L, 15L, 14L, 3L, 11L, 5L, 2L, 12}
	}, {
			{7L, 13L, 14L, 3L, 0L, 6L, 9L, 10L, 1L, 2L, 8L, 5L, 11L, 12L, 4L, 15}, {13L, 8L, 11L, 5L, 6L, 15L, 0L, 3L, 4L, 7L, 2L, 12L, 1L, 10L, 14L, 9},
			{10L, 6L, 9L, 0L, 12L, 11L, 7L, 13L, 15L, 1L, 3L, 14L, 5L, 2L, 8L, 4}, {3L, 15L, 0L, 6L, 10L, 1L, 13L, 8L, 9L, 4L, 5L, 11L, 12L, 7L, 2L, 14}
	}, {
			{2L, 12L, 4L, 1L, 7L, 10L, 11L, 6L, 8L, 5L, 3L, 15L, 13L, 0L, 14L, 9}, {14L, 11L, 2L, 12L, 4L, 7L, 13L, 1L, 5L, 0L, 15L, 10L, 3L, 9L, 8L, 6},
			{4L, 2L, 1L, 11L, 10L, 13L, 7L, 8L, 15L, 9L, 12L, 5L, 6L, 3L, 0L, 14}, {11L, 8L, 12L, 7L, 1L, 14L, 2L, 13L, 6L, 15L, 0L, 9L, 10L, 4L, 5L, 3}
	}, {
			{12L, 1L, 10L, 15L, 9L, 2L, 6L, 8L, 0L, 13L, 3L, 4L, 14L, 7L, 5L, 11}, {10L, 15L, 4L, 2L, 7L, 12L, 9L, 5L, 6L, 1L, 13L, 14L, 0L, 11L, 3L, 8},
			{9L, 14L, 15L, 5L, 2L, 8L, 12L, 3L, 7L, 0L, 4L, 10L, 1L, 13L, 11L, 6}, {4L, 3L, 2L, 12L, 9L, 5L, 15L, 10L, 11L, 14L, 1L, 7L, 6L, 0L, 8L, 13}
	}, {
			{4L, 11L, 2L, 14L, 15L, 0L, 8L, 13L, 3L, 12L, 9L, 7L, 5L, 10L, 6L, 1}, {13L, 0L, 11L, 7L, 4L, 9L, 1L, 10L, 14L, 3L, 5L, 12L, 2L, 15L, 8L, 6},
			{1L, 4L, 11L, 13L, 12L, 3L, 7L, 14L, 10L, 15L, 6L, 8L, 0L, 5L, 9L, 2}, {6L, 11L, 13L, 8L, 1L, 4L, 10L, 7L, 9L, 5L, 0L, 15L, 14L, 2L, 3L, 12}
	}, {
			{13L, 2L, 8L, 4L, 6L, 15L, 11L, 1L, 10L, 9L, 3L, 14L, 5L, 0L, 12L, 7}, {1L, 15L, 13L, 8L, 10L, 3L, 7L, 4L, 12L, 5L, 6L, 11L, 0L, 14L, 9L, 2},
			{7L, 11L, 4L, 1L, 9L, 12L, 14L, 2L, 0L, 6L, 10L, 13L, 15L, 3L, 5L, 8}, {2L, 1L, 14L, 7L, 4L, 10L, 8L, 13L, 15L, 12L, 9L, 0L, 3L, 5L, 6L, 11}
	}};
	

	@NonNull
	public static String encode(String userName, String password, String lt){
		return Encrypt(userName + password + lt, "1", "2", "3");
	}
	
	@NonNull
	public static String Encrypt(String msg, String key1, String key2, String key3){
		byte[] msgByte = str2bytes(msg);
		byte[] key1Byte = str2bytes(key1);
		byte[] key2Byte = str2bytes(key2);
		byte[] key3Byte = str2bytes(key3);
		
		StringBuilder sb = new StringBuilder();
		
		for(int m = 0; m < msgByte.length; m += 8){
			byte[] tmpMsg = new byte[8];
			System.arraycopy(msgByte, m, tmpMsg, 0, 8);
			
			for(int k = 0; k < key1Byte.length; k += 8){
				byte[] tmpKey = new byte[8];
				System.arraycopy(key1Byte, k, tmpKey, 0, 8);
				tmpMsg = enc(tmpMsg, tmpKey);
			}

			for(int k = 0; k < key2Byte.length; k += 8){
				byte[] tmpKey = new byte[8];
				System.arraycopy(key2Byte, k, tmpKey, 0, 8);
				tmpMsg = enc(tmpMsg, tmpKey);
			}

			for(int k = 0; k < key3Byte.length; k += 8){
				byte[] tmpKey = new byte[8];
				System.arraycopy(key3Byte, k, tmpKey, 0, 8);
				tmpMsg = enc(tmpMsg, tmpKey);
			}
			
			char[] hexChars = new char[16];
			for (int i = 0; i < 8; i++) {
				int value = tmpMsg[i] & 0xFF;
				hexChars[i * 2] = HEX_DIGITS[value >>> 4];
				hexChars[i * 2 + 1] = HEX_DIGITS[value & 0x0F];
			}
			sb.append(hexChars);
		}
		
		return sb.toString();
	}
	
	@NonNull
	public static byte[] enc(byte[] msg, byte[] key){
		long keyLong = KeyTo56(byte2long(key));
		
		long c = keyLong >> 28;
		long d = keyLong & 268435455L;
		long[] kList = new long[16];
		for(int i = 0; i < 16; i++){
			c = ((c >> (28 - table[i])) + (c << table[i])) & 268435455L;
			d = ((d >> (28 - table[i])) + (d << table[i])) & 268435455L;
			long t = (c << 28) + d;
			for(int j = 0; j < 48; j++){
				kList[i] += (((1L << (56 - table2[j])) & t) >> (56 - table2[j])) << (47 - j);
			}
		}
		
		BigInteger msgBig = new BigInteger(1, msg);
		
		BigInteger n = BigInteger.valueOf(0);
		for(int i = 0; i < 64; i++){
			n = n.add(BigInteger.valueOf(1L << (64 - table3[i])).and(msgBig).shiftRight(64 - table3[i]).shiftLeft(63 - i));
		}
		
		BigInteger tmp;
		BigInteger l = n.shiftRight(32);
		BigInteger r = n.and(BigInteger.valueOf(0xFFFFFFFFL));
		
		for(int j = 0; j < 16; j++){
			tmp = l.xor(F(r, BigInteger.valueOf(kList[j])));
			l = r;
			r = tmp;
		}
		
		tmp = r.shiftLeft(32).add(l);
		
		BigInteger res = BigInteger.ZERO;
		for(int i = 0; i < 64; i++){
			res = res.add(BigInteger.valueOf(1L << (64 - table4[i])).and(tmp).shiftRight(64 - table4[i]).shiftLeft(63 - i));
		}
		
		return long2byte(res.longValue());
	}
	
	public static long KeyTo56(long k){
		byte[] keyByte = new byte[64];
		for(int x = 63; x >= 0; x--){
			keyByte[63 - x] = (byte)((k >> x) & 1);
		}
		byte[] key = new byte[56];
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 8; j++){
				int kIndex = 7 - j;
				key[i * 8 + j] = keyByte[8 * kIndex + i];
			}
		}
		long keyInt = 0;
		for(byte b : key){
			keyInt = (keyInt << 1) + b;
		}
		return keyInt;
	}
	
	public static BigInteger F(BigInteger r, BigInteger k){
		BigInteger r2 = BigInteger.ZERO;
		for(int i = 0; i < 48; i++){
			r2 = r2.add(BigInteger.valueOf(1L << (32 - tableE[i])).and(r).shiftRight(32 - tableE[i]).shiftLeft(47 - i));
		}
		
		BigInteger r3 = r2.xor(k);
		BigInteger r4 = BigInteger.ZERO;
		for(int i = 0; i < 8; i++){
			long s = BigInteger.valueOf(63L << ((7 - i) * 6)).and(r3).shiftRight((7 - i) * 6).longValue();
			int x = (int)((30 & s) >> 1);
			int y = (int)(((32 & s) >> 4) + (1 & s));
			r4 = r4.add(BigInteger.valueOf(SBox[i][y][x] << ((7 - i) * 4)));
		}
		
		BigInteger r5 = BigInteger.ZERO;
		for(int i = 0; i < 32; i++){
			r5 = r5.add(BigInteger.valueOf(1L << (32 - tableP[i])).and(r4).shiftRight(32 - tableP[i]).shiftLeft(31 - i));
		}
		return r5;
	}
	
	@NonNull
	public static byte[] long2byte(long value){
		byte[] result = new byte[8];
		for(int i = 0; i < 8; i++){
			result[i] = (byte)((value >>> ((8 - i - 1) * 8)) & 0xFF);
		}
		return result;
	}
	
	public static long byte2long(@NonNull byte[] bytes){
		long result = 0;
		for(byte b : bytes){
			result = (result << 8) + (b & 0xFF);
		}
		return result;
	}
	
	@NonNull
	public static byte[] str2bytes(@NonNull String string){
		int length = string.length() * 2;
		int paddingLength = (8 - length % 8) % 8;
		byte[] bts = new byte[length + paddingLength];
		int index = 0;
		for(char x : string.toCharArray()){
			bts[index++] = (byte)(x >> 8);
			bts[index++] = (byte)x;
		}
		return bts;
	}
}
