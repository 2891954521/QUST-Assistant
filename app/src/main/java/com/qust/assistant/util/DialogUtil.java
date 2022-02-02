package com.qust.assistant.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;

public class DialogUtil{
	
	// 基础Dialog 取消 确定
	public static MaterialDialog.Builder getBaseDialog(Context context){
		return new MaterialDialog.Builder(context).positiveText(R.string.text_ok).negativeText(R.string.text_cancel).onNegative((dialog,which) -> dialog.dismiss());
	}
	
	// 无限加载条
	public static MaterialDialog.Builder getIndeterminateProgressDialog(Context context, String message){
		return new MaterialDialog.Builder(context).progress(true,0).content(message);
	}
	
	// 基础ListDialog 仅取消
	public static MaterialDialog.Builder getListDialog(Context context,String title,String[] items,MaterialDialog.ListCallback callback){
		return new MaterialDialog.Builder(context).title(title).items(items).itemsCallback(callback).positiveText(R.string.text_cancel).onPositive((dialog,which) -> dialog.dismiss());
	}
	
}