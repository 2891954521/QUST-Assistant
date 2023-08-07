package com.qust.account;

import java.io.IOException;

import okhttp3.Response;

/**
 * 请求成功时的回调
 */
public interface RequestCallback{
	void onSuccess(Response response) throws IOException;
}