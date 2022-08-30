package com.qust.assistant.ui.fragment.school;

import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qust.assistant.App;
import com.qust.assistant.R;
import com.qust.assistant.ui.MainActivity;
import com.qust.assistant.util.DialogUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.util.QustUtil.LoginUtil;
import com.qust.assistant.util.WebUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoEvaluationFragment extends BaseSchoolFragment{
	
	/**
	 * 匹配form标签
	 */
	private static final Pattern FORM = Pattern.compile("<form(.*?)</form>", Pattern.DOTALL);
	
	/**
	 * 匹配table标签
	 */
	private static final Pattern TABLE = Pattern.compile("<table(.*?)</table>", Pattern.DOTALL);
	
	/**
	 * 匹配tr标签
	 */
	private static final Pattern TR = Pattern.compile("<tr(.*?)</tr>", Pattern.DOTALL);
	
	/**
	 * 提交的基本参数
	 */
	private static final Object[] BASE_PARAM = {
			"fxzgf=",       Pattern.compile("fxzgf=\"(.*?)\""),
			"pjdxdm=",      Pattern.compile("pjdxdm=\"(.*?)\""),
			"xspfb_id=",    Pattern.compile("xspfb_id=\"(.*?)\""),
			"pjmbmcb_id=",  Pattern.compile("pjmbmcb_id=\"(.*?)\"")
	};
	
	private static final Pattern JGH_ID = Pattern.compile("jgh_id=\"(.*?)\"");
	
	/**
	 * 标识一个表的参数
 	 */
	private static final Pattern PJZBXM_ID = Pattern.compile("pjzbxm_id=\"(.*?)\"");
	
	/**
	 * 每个选项的参数
 	 */
	private static final Pattern PFDJDMB_ID = Pattern.compile("pfdjdmb_id=\"(.*?)\"");
	/**
	 * 每个选项的参数
	 */
	private static final Pattern ZSMBMCB_ID = Pattern.compile("zsmbmcb_id=\"(.*?)\"");
	
	/**
	 * 匹配一个具体的选项和他的文本
 	 */
	private static final Pattern PFDJDMXMB_ID = Pattern.compile("pfdjdmxmb_id=\"(.*?)\".*?>(.*?)<", Pattern.DOTALL);
	
	
	private ArrayList<EvaluationLesson> lessons;
	
	private String session;
	
	public AutoEvaluationFragment(MainActivity activity){
		super(activity);
	}
	
	@Override
	protected void initLayout(LayoutInflater inflater){
		super.initLayout(inflater);
		
		addMenuItem(inflater, R.drawable.ic_refresh, v -> doLogin());
		
		lessons = new ArrayList<>();
		
		adapter = new EvaluationAdapter();
		
		((ListView)findViewById(R.id.view_list)).setAdapter(adapter);
		
		doLogin();
	}
	
	@Override
	protected void doQuery(String JSESSIONID){
		
		sendMessage(App.UPDATE_DIALOG, "正在查询教评");
		
		session = JSESSIONID;
		
		lessons = new ArrayList<>();
		
		try{
			String response = WebUtil.doPost(
					LoginUtil.HOST + "/jwglxt/xspjgl/xspj_cxXspjIndex.html?doType=query&gnmkdm=0",
					"JSESSIONID=" + JSESSIONID,
					"queryModel.showCount=30"
			);
			if(!TextUtils.isEmpty(response)){

				JSONArray array = new JSONObject(response).getJSONArray("items");
				
				for(int i=0;i<array.length();i++){
					JSONObject js = array.getJSONObject(i);
					EvaluationLesson lesson = new EvaluationLesson();
					lesson.name = js.getString("kcmc").trim();
					lesson.teacher = js.getString("jzgmc");
					lesson.tjztmc = js.getString("tjztmc");
					lesson.tjzt = "已评完".equals(lesson.tjztmc) ? "1" : js.getString("tjzt");
					lesson.xsdm = js.getString("xsdm");
					lesson.xsmc = js.getString("xsmc");
					lesson.jxb_id = js.getString("jxb_id");
					lesson.kch_id = js.getString("kch_id");
					lesson.jgh_id = js.getString("jgh_id");
					lesson.pjzt = js.getString("pjzt");
					lessons.add(lesson);
				}
				sendMessage(App.NOTIFY_TOAST, "查询成功！");
			}
		}catch(IOException | JSONException e){
			LogUtil.Log(e);
			sendMessage(App.DISMISS_TOAST, "查询失败！");
		}
	}
	
	private void autoSubmitEvaluation(int position){
		dialog.setContent("正在查询教评详情");
		dialog.show();
		
		new Thread(){
			@Override
			public void run(){
				EvaluationLesson lesson = lessons.get(position);
				try{
					String response = WebUtil.doPost(
							LoginUtil.HOST + "/jwglxt/xspjgl/xspj_cxXspjDisplay.html?gnmkdm=0",
							"JSESSIONID=" + session,
							"jxb_id=" + lesson.jxb_id +
									"&kch_id=" + lesson.kch_id +
									"&xsdm=" + lesson.xsdm +
									"&jgh_id=" + lesson.jgh_id +
									"&tjzt=" + lesson.tjzt
					);
					if(!TextUtils.isEmpty(response)){
						
						Message message = new Message();
						message.obj = "正在自动提交教评";
						handler.sendMessage(message);
						
						StringBuilder postData = new StringBuilder().append("ztpjbl=100&jszdpjbl=0&xykzpjbl=0");
						postData.append("&jxb_id=").append(lesson.jxb_id);
						postData.append("&kch_id=").append(lesson.kch_id);
						postData.append("&xsdm=").append(lesson.xsdm);
						fillEvaluation(response, postData.append("&"));
						postData.append("modelList%5B0%5D.pjzt=1");
						postData.append("&modelList%5B0%5D.py="); // 评语
						postData.append("&tjzt=").append(lesson.tjzt);
						
						response = WebUtil.doPost(
								LoginUtil.HOST + "/jwglxt/xspjgl/xspj_bcXspj.html?gnmkdm=0",
								"JSESSIONID=" + session,
								postData.toString()
						);
						
						if(!TextUtils.isEmpty(response) && "\"评价保存成功！\"".equals(response)){
							lesson.tjzt = "1";
							sendMessage(App.NOTIFY_TOAST, "提交成功！");
						}else{
							sendMessage(App.DISMISS_TOAST, "提交失败！" + response);
						}
					}
				}catch(Exception e){
					LogUtil.Log(e);
					sendMessage(App.DISMISS_TOAST, "提交失败！");
				}
			}
		}.start();
	}
	
	private void fillEvaluation(String html, StringBuilder postData){
		Matcher tmp;
		
		// 找到要提交的表单
		String form = match(FORM, html);
		
		if(form == null) return;
		
		String jgh_id = match(JGH_ID, form);
		
		if(jgh_id == null) return;
		
		postData.append("jgh_id=").append(jgh_id).append("&");
		
		// 提交的基本参数
		for(int i = 0; i < BASE_PARAM.length; i += 2){
			tmp = ((Pattern)BASE_PARAM[i + 1]).matcher(form);
			if(tmp.find()){
				postData.append("modelList%5B0%5D.").append(BASE_PARAM[i]).append(tmp.group(1)).append("&");
			}
		}
		
		int tableIndex = 0;
		Matcher tableMatcher = TABLE.matcher(form);
		
		// 查找要提交的表
		while(tableMatcher.find()){
			String table = tableMatcher.group(1);
			
			// 查找表的基本数据
			String t = match(PJZBXM_ID, table);
			if(t != null){
				postData.append("modelList%5B0%5D.xspjList%5B").append(tableIndex).append("%5D.pjzbxm_id=").append(t).append("&");
			}else{
				continue;
			}
			
			
			int chIndex = 0;
			
			// 查找单个条目
			Matcher itemMatcher = TR.matcher(table);
			while(itemMatcher.find()){
				String item = itemMatcher.group(1);
				
				// 每个选项的参数
				String pjzbxm_id = match(PJZBXM_ID, item);
				String pfdjdmb_id = match(PFDJDMB_ID, item);
				String zsmbmcb_id = match(ZSMBMCB_ID, item);
				
				if(pjzbxm_id == null || pfdjdmb_id == null || zsmbmcb_id == null) continue;
				
				// 选择的选项
				tmp = PFDJDMXMB_ID.matcher(item);
				if(!tmp.find()) continue;
				
				// 找选项，默认是第一个
				String pfdjdmxmb_id = tmp.group(1);
				
				while(tmp.find()){
					String message = tmp.group(2).trim().replaceAll("&nbsp;", "");
					// 看到"无"无脑选, 开头为'A'无脑选
					if("无".equals(message) || message.startsWith("A")){
						pfdjdmxmb_id = tmp.group(1);
						break;
					}
				}
				
				postData.append("modelList%5B0%5D.xspjList%5B").append(tableIndex).append("%5D.childXspjList%5B").append(chIndex).append("%5D.pfdjdmxmb_id=").append(pfdjdmxmb_id).append("&");
				postData.append("modelList%5B0%5D.xspjList%5B").append(tableIndex).append("%5D.childXspjList%5B").append(chIndex).append("%5D.pjzbxm_id=").append(pjzbxm_id).append("&");
				postData.append("modelList%5B0%5D.xspjList%5B").append(tableIndex).append("%5D.childXspjList%5B").append(chIndex).append("%5D.pfdjdmb_id=").append(pfdjdmb_id).append("&");
				postData.append("modelList%5B0%5D.xspjList%5B").append(tableIndex).append("%5D.childXspjList%5B").append(chIndex).append("%5D.zsmbmcb_id=").append(zsmbmcb_id).append("&");
				chIndex++;
			}
			tableIndex++;
		}
	}
	
	@Nullable
	private String match(@NonNull Pattern pattern, String str){
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()){
			return matcher.group(1);
		}else{
			return null;
		}
	}
	
	@Override
	protected String getName(){
		return "自动教评";
	}
	
	@Override
	protected int getLayoutId(){
		return R.layout.view_list;
	}
	
	
	private class EvaluationAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return lessons.size(); }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(activity).inflate(R.layout.item_evaluation, parent, false);
			}
			
			EvaluationLesson lesson = lessons.get(position);
			
			((TextView)convertView.findViewById(R.id.item_evaluation_name)).setText(lesson.name);
			((TextView)convertView.findViewById(R.id.item_evaluation_teacher)).setText(lesson.teacher);
			((TextView)convertView.findViewById(R.id.item_evaluation_type)).setText(lesson.xsmc);
			
			((TextView)convertView.findViewById(R.id.item_evaluation_handled)).setText(lesson.tjztmc);
			
			if("1".equals(lesson.tjzt)){
				convertView.findViewById(R.id.item_evaluation_submit).setVisibility(View.GONE);
				convertView.findViewById(R.id.item_evaluation_wtf).setVisibility(View.GONE);
			}else{
				Button button = convertView.findViewById(R.id.item_evaluation_submit);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(v -> autoSubmitEvaluation(position));
				button = convertView.findViewById(R.id.item_evaluation_wtf);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(v -> DialogUtil.getBaseDialog(activity).content("然而没有这个功能/doge").onPositive((dialog, which) -> dialog.dismiss()).show());
			}
			return convertView;
		}
	}
	
	
	private static class EvaluationLesson implements Serializable{
		
		private static final long serialVersionUID = 6435785622350990450L;
		
		/**
		 * 课程名称
		 */
		public String name;
		
		/**
		 * 教师名称
		 */
		public String teacher;
		
		/**
		 * 提交状态 1 已提交， 0 未填完， -1 未填
		 */
		public String tjzt;
		
		public String tjztmc;
		
		/**
		 * 类型
		 */
		public String xsmc;
		
		public String xsdm;
		
		public String jxb_id;
		
		public String kch_id;
		
		public String jgh_id;
		
		public String pjzt;

	}
	
}
