package com.qust.account;

import androidx.annotation.NonNull;

import com.qust.utils.CodeUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * VPN URL编码解码器
 */
public class VpnCodec{
	
	public static final String VPN_URL = "https://wvpn.qust.edu.cn/";
	
	private static String IVHex;
	
	private static  Cipher cipher;
	
	static{
		try{
			byte[] KeyBytes = "wrdvpnisthebest!".getBytes();
			IVHex = CodeUtils.byteToHexString(KeyBytes);
			cipher = Cipher.getInstance("AES/CFB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KeyBytes, "AES"), new IvParameterSpec(KeyBytes));
		}catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e){
			throw new RuntimeException(e);
		}
	}
	
	@NonNull
	private static String encrypt(@NonNull String text) throws Exception {
		byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
		return IVHex + CodeUtils.byteToHexString(encryptedBytes);
	}
	
	/**
	 * 将只能在校园网访问的URL转换为走VPN的URL
	 * @param url 原始URL
	 * @return VPN URL
	 * @throws Exception
	 */
	@NonNull
	public static String encryptUrl(@NonNull String url) throws Exception{
		String protocol;
		if (url.startsWith("http://")){
			url = url.substring(7);
			protocol = "http";
		}else if (url.startsWith("https://")){
			url = url.substring(8);
			protocol = "https";
		}else{
			throw new RuntimeException("Not a valid URL");
		}
		
		// 处理ipv6
		String v6 = null;
		Pattern pattern = java.util.regex.Pattern.compile("\\[[0-9a-fA-F:]+?\\]");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			v6 = matcher.group(0);
			url = url.substring(matcher.end());
		}
		
		// 提取端口
		String port = null;
		String[] parts = url.split("\\?")[0].split(":");
		if(parts.length > 1){
			port = parts[1].split("/")[0];
			url = url.substring(0, parts[0].length()) + url.substring(parts[0].length() + port.length() + 1);
		}
		
		// 只对host进行加密
		int i = url.indexOf('/');
		if(i == -1){
			url = encrypt(v6 == null ? url : v6);
		}else{
			url = encrypt(v6 == null ? url.substring(0, i): v6) + url.substring(i);
		}
		
		if(port != null){
			url = protocol + "-" + port + "/" + url;
		}else{
			url = protocol + "/" + url;
		}
		
		return VPN_URL + url;
	}
}
