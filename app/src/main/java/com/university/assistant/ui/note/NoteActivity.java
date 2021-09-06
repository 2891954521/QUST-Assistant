package com.university.assistant.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.sql.NoteData;
import com.university.assistant.sql.PictureData;
import com.university.assistant.ui.BaseAnimActivity;
import com.university.assistant.widget.BigImage;

import androidx.annotation.Nullable;

public class NoteActivity extends BaseAnimActivity{
	
	private BigImage bigImage;
	
	private NoteAdapter adapter;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		PictureData.init(this);
		NoteData.init(this);
		
		NoteData.getInstance().initData();
		
		setContentView(R.layout.activity_note);
		
		findViewById(R.id.fragment_note_add).setOnClickListener(v -> {
			NoteData.getInstance().setEditingNote(null);
			startActivityForResult(new Intent(this, NoteEditActivity.class), App.APP_REQUEST_CODE);
		});
		
		bigImage = findViewById(R.id.fragment_note_image);
		
		ListView list = findViewById(R.id.fragment_note_list);
		
		adapter = new NoteAdapter(this, NoteData.getInstance().getData());
		adapter.setOnImageClickListener((imageView, path) -> bigImage.setImage(imageView, path));
		
		list.setAdapter(adapter);

		initToolBar(null);
		initSliding(null, null);
	}
	

		//view.setVisibility(View.VISIBLE);
		//view.setImageResource(R.drawable.ic_add);
//		view.setOnClickListener(v -> new MaterialDialog.Builder(getContext()).title("新建")
//                .input("请输入科目名","",(dialog,input) -> { })
//                .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
//                .positiveText("确定").onPositive((dialog,which) -> {
//			try{
//				String s = dialog.getInputEditText().getText().toString();
//				ArrayList<String> header = NoteData.getInstance().getHeader();
//				if(header.indexOf(s)==-1){
//					Toast.makeText(getContext(),"标签名已存在!",Toast.LENGTH_LONG).show();
//					return;
//				}
//				header.add(s);
//				addLabel(s);
//			}catch(NullPointerException e){
//				LogUtil.Log(e);
//				Toast.makeText(getContext(),"新建标签失败!" + e.getClass().getName(),Toast.LENGTH_LONG).show();
//			}
//		}).show());
	
	@Override
	public void onResume(){
		super.onResume();
		
		Note note = NoteData.getInstance().getEditingNote();
		
		if(note == null) return;
		
		if(note.id == -1){
			int id = NoteData.getInstance().insertData();
			if(id == -1) toast("新建笔记失败！写入数据库失败");
			adapter.notifyDataSetChanged();
		}else{
			int id = NoteData.getInstance().update();
			if(id == -1) toast("修改笔记失败！写入数据库失败");
			adapter.notifyDataSetChanged();
		}
		NoteData.getInstance().setEditingNote(null);
		//		JSONObject js;
		//		try{
		//			js = new JSONObject(data.getStringExtra("data"));
		//			Note d = new Note(js.getString("type"),js.getString("date"),js.getString("commit"),js.getJSONArray("data"));
		//			if(onEdit==-1) NoteData.getInstance().getData().add(d);
		//			else NoteData.getInstance().getData().set(onEdit,d);
		//		}catch(JSONException e){
		//			LogUtil.Log(e);
		//			Toast.makeText(getContext(),"编辑失败！",Toast.LENGTH_SHORT).show();
		//			return;
		//		}
		//		Toast.makeText(getContext(),"添加成功",Toast.LENGTH_SHORT).show();
	}
	
}
