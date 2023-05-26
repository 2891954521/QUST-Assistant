package com.qust.assistant.vo;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 教务系统消息
 */
public class Notice implements Serializable{
	
	private static final long serialVersionUID = 4865392800886435761L;
	
	/**
	 * ID
	 */
	public String id;
	
	/**
	 * 创建时间
	 */
	public String cjsj;
	
	/**
	 * 消息内容
	 */
	public String xxnr;
	
	public Notice(){ }
	
	public Notice(@NonNull JSONObject js) throws JSONException{
		id = js.getString("id");
		cjsj = js.getString("cjsj");
		xxnr = js.getString("xxnr");
	}
	
//	{
//		"cjsj":"2022-11-30 16:08:53",
//		"clzt":"0",
//		"id":"EEAC263CF59D7E64E055000000000001",
//		"jgpxzd":"1",
//		"jsdm":"xs",
//		"jsmc":"学生",
//		"listnav":"false",
//		"localeKey":"zh_CN",
//		"modelList": [],
//		"pageable":true,
//		"rangeable":true,
//		"row_id":"31",
//		"rsdzjs":0,
//		"totalResult":"95",
//		"w_id":"EEAAE64053F973A9E055000000000001",
//		"xxbt":"调课提醒:徐凌伟老师于第17周星期五第5-6节在明-241上的计算机网络技术课程调课到由徐凌伟老师在第16周星期五第9-10节明-241上课，请各位同学相互告知！",
//		"xxbtjc":"调课提醒:徐凌伟老师于第17周星期五第5-6节在明...",
//		"xxnr":"调课提醒:徐凌伟老师于第17周星期五第5-6节在明-241上的计算机网络技术课程调课到由徐凌伟老师在第16周星期五第9-10节明-241上课，请各位同学相互告知！",
//		"yhm":"2029740103",
//		"zjxx":"EEAC263CF59D7E64E055000000000001"
//	}
}
