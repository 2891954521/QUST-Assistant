package com.university.assistant.ui.school;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.university.assistant.R;
import com.university.assistant.util.LogUtil;
import com.university.assistant.util.WebUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;

public class AutoEvaluationActivity extends BaseSchoolActivity{
	
	private ArrayList<EvaluationLesson> lessons;
	
	private String session;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		lessons = new ArrayList<>();
		
		initList(new EvaluationAdapter());
	}
	
	@Override
	protected String getName(){
		return "自动教评";
	}
	
	@Override
	protected int getLayout(){
		return R.layout.activity_auto_evaluation;
	}
	
	@Override
	protected void doQuery(String _session){
		Message message = new Message();
		message.obj = "正在查询教评";
		handler.sendMessage(message);
		
		session = _session;
		lessons = new ArrayList<>();
		try{
			String response = WebUtil.doPost(
					"http://jwglxt.qust.edu.cn/jwglxt/xspjgl/xspj_cxXspjIndex.html?doType=query&gnmkdm=0",
					"JSESSIONID=" + session,
					"queryModel.showCount=30"
			);
			if(!TextUtils.isEmpty(response)){
				
				Matcher table = Pattern.compile("<tbody(.*?)</tbody>", Pattern.DOTALL).matcher(response);
				if(table.find()){
					Matcher tr = Pattern.compile("<tr(.*?)</tr>", Pattern.DOTALL).matcher(table.group(1));
					Pattern tjzt = Pattern.compile("tjzt=\"([0-9a-zA-Z]*?)\"");
					Pattern xsdm = Pattern.compile("xsdm=\"([0-9a-zA-Z]*?)\"");
					Pattern jxb_id = Pattern.compile("jxb_id=\"([0-9a-zA-Z]*?)\"");
					Pattern kch_id = Pattern.compile("kch_id=\"([0-9a-zA-Z]*?)\"");
					Pattern jgh_id = Pattern.compile("jgh_id=\"([0-9a-zA-Z]*?)\"");
					Pattern td = Pattern.compile("<td.*?>(.*?)</td>", Pattern.DOTALL);
					while(tr.find()){
						String text = tr.group(1);
						EvaluationLesson lesson = new EvaluationLesson();
						Matcher tmp = tjzt.matcher(text);
						if(tmp.find()) lesson.tjzt = tmp.group(1);
						tmp = xsdm.matcher(text);
						if(tmp.find()) lesson.xsdm = tmp.group(1);
						tmp = jxb_id.matcher(text);
						if(tmp.find()) lesson.jxb_id = tmp.group(1);
						tmp = kch_id.matcher(text);
						if(tmp.find()) lesson.kch_id = tmp.group(1);
						tmp = jgh_id.matcher(text);
						if(tmp.find()) lesson.jgh_id = tmp.group(1);
						tmp = td.matcher(text);
						tmp.find();
						if(tmp.find()) lesson.name = tmp.group(1).trim();
						tmp.find();
						if(tmp.find()) lesson.teacher = tmp.group(1).trim();
						lessons.add(lesson);
					}
				}else{
					runOnUiThread(() -> {
						dialog.dismiss();
						toast("查询失败！");
					});
				}
				
				/* SB教务系统
				JSONArray array = new JSONObject(response).getJSONArray("items");
				
				for(int i=0;i<array.length();i++){
					JSONObject js = array.getJSONObject(i);
					EvaluationLesson lesson = new EvaluationLesson();
					lesson.name = js.getString("kcmc");
					lesson.teacher = js.getString("jzgmc");
					lesson.tjzt = js.getString("tjzt");
					lesson.xsdm = js.getString("xsdm");
					lesson.jxb_id = js.getString("jxb_id");
					lesson.kch_id = js.getString("kch_id");
					lesson.jgh_id = js.getString("jgh_id");
					lesson.pjzt = js.getString("pjzt");
					lessons.add(lesson);
				}*/
				
				runOnUiThread(() -> {
					dialog.dismiss();
					toast("查询成功！");
					adapter.notifyDataSetChanged();
				});
			}
		}catch(IOException e){
			LogUtil.Log(e);
			runOnUiThread(() -> {
				dialog.dismiss();
				toast("查询失败！");
			});
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
							"http://jwglxt.qust.edu.cn/jwglxt/xspjgl/xspj_cxXspjDisplay.html?gnmkdm=0",
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
						
						StringBuilder postData = fillEvaluation(response);
						postData.append("tjzt=").append(lesson.tjzt);
						postData.append("&xsdm=").append(lesson.xsdm);
						postData.append("&jxb_id=").append(lesson.jxb_id);
						postData.append("&kch_id=").append(lesson.kch_id);
						postData.append("&jgh_id=").append(lesson.jgh_id);
						//postData.append("&modelList[0].pjzt=").append(lesson.pjzt);
						postData.append("modelList[0].py=&ztpjbl=100&jszdpjbl=0&xykzpjbl=0");
						
						response = WebUtil.doPost(
								"http://jwglxt.qust.edu.cn/jwglxt/xspjgl/xspj_tjXspj.html?gnmkdm=0",
								"JSESSIONID=" + session,
								postData.toString()
						);
						
						if(!TextUtils.isEmpty(response) && "\"提交成功!\"".equals(response)){
							runOnUiThread(() -> {
								lesson.tjzt = "1";
								dialog.dismiss();
								toast("提交成功！");
								adapter.notifyDataSetChanged();
							});
						}else{
							runOnUiThread(() -> {
								dialog.dismiss();
								toast("提交失败！");
							});
						}
					}
				}catch(Exception e){
					LogUtil.Log(e);
					runOnUiThread(() -> {
						dialog.dismiss();
						toast("提交失败！");
					});
				}
			}
		}.start();
	}
	
	private StringBuilder fillEvaluation(String html){
		
		StringBuilder postData = new StringBuilder();
		
		Matcher tmp;
		Matcher form = Pattern.compile("<form(.*?)</form>",Pattern.DOTALL).matcher(html);
		if(form.find()){
			// 找到要提交的表单
			html = form.group(1);
			// 提交的基本参数
			for(String p : new String[]{"pjdxdm","fxzgf","xspfb_id"}){
				tmp = Pattern.compile(p + "=\"(.*?)\"").matcher(html);
				if(tmp.find()){
					postData.append("modelList[0].").append(p).append("=").append(tmp.group(1)).append("&");
				}
			}
			// 查找要提交的表
			Matcher table = Pattern.compile("<table(.*?)</table>",Pattern.DOTALL).matcher(html);
			int index = 0;
			while(table.find()){
				String t = table.group(1);
				// 查找表的基本数据
				tmp = Pattern.compile("pjzbxm_id=\"(.*?)\"").matcher(t);
				if(tmp.find()){
					postData.append("modelList[0].xspjList[").append(index).append("].pjzbxm_id=").append(tmp.group(1)).append("&");
				}
				// 查找单个条目
				Matcher tr = Pattern.compile("<tr(.*?)</tr>",Pattern.DOTALL).matcher(t);
				tr.find();
				
				String r = tr.group(1);
				// 先提取几个一模一样的参数
				// 选择的评价
				tmp = Pattern.compile("pfdjdmxmb_id=\"(.*?)\"").matcher(r);
				tmp.find();
				String pfdjdmxmb_id = tmp.group(1);
				
				tmp = Pattern.compile("pfdjdmb_id=\"(.*?)\"").matcher(r);
				tmp.find();
				String pfdjdmb_id = tmp.group(1);
				tmp = Pattern.compile("zsmbmcb_id=\"(.*?)\"").matcher(r);
				tmp.find();
				String zsmbmcb_id = tmp.group(1);
				
				int chIndex = 0;
				
				Pattern p = Pattern.compile("pjzbxm_id=\"(.*?)\"");
				do{
					r = tr.group(1);
					tmp = p.matcher(r);
					tmp.find();
					postData.append("modelList[0].xspjList[").append(index).append("].childXspjList[").append(chIndex).append("].pfdjdmxmb_id=").append(pfdjdmxmb_id).append("&");
					postData.append("modelList[0].xspjList[").append(index).append("].childXspjList[").append(chIndex).append("].pjzbxm_id=").append(tmp.group(1)).append("&");
					postData.append("modelList[0].xspjList[").append(index).append("].childXspjList[").append(chIndex).append("].pfdjdmb_id=").append(pfdjdmb_id).append("&");
					postData.append("modelList[0].xspjList[").append(index).append("].childXspjList[").append(chIndex).append("].zsmbmcb_id=").append(zsmbmcb_id).append("&");
					chIndex++;
				}while(tr.find());
				index++;
			}
		}
		return postData;
	}
	
	
	private class EvaluationAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){ return lessons.size(); }
		
		@Override
		public Object getItem(int position){ return null; }
		
		@Override
		public long getItemId(int position){ return 0; }
		
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			if(convertView == null){
				convertView = LayoutInflater.from(AutoEvaluationActivity.this).inflate(R.layout.item_evaluation,null);
			}
			EvaluationLesson lesson = lessons.get(position);
			((TextView)convertView.findViewById(R.id.item_evaluation_name)).setText(lesson.name);
			((TextView)convertView.findViewById(R.id.item_evaluation_teacher)).setText(lesson.teacher);
			if("1".equals(lesson.tjzt)){
				convertView.findViewById(R.id.item_evaluation_handled).setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.item_evaluation_submit).setVisibility(View.GONE);
			}else{
				convertView.findViewById(R.id.item_evaluation_handled).setVisibility(View.GONE);
				Button button = convertView.findViewById(R.id.item_evaluation_submit);
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(v -> autoSubmitEvaluation(position));
			}
			return convertView;
		}
	}
	
	private static class EvaluationLesson implements Serializable{
		
		public String name;
		
		public String teacher;
		
		public String tjzt;
		
		public String xsdm;
		
		public String jxb_id;
		
		public String kch_id;
		
		public String jgh_id;
		
		//public String pjzt;

	}
	
}
