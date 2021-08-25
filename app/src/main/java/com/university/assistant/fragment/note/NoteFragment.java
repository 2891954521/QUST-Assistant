package com.university.assistant.fragment.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.fragment.BaseFragment;
import com.university.assistant.sql.NoteData;
import com.university.assistant.sql.PictureData;
import com.university.assistant.ui.MainActivity;
import com.university.assistant.ui.note.NoteEditActivity;
import com.university.assistant.widget.BigImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NoteFragment extends BaseFragment{
	
	private BigImage bigImage;
	
	private NoteAdapter adapter;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
		if(activity==null) activity = (MainActivity)getContext();
		PictureData.init(getContext());
		NoteData.init(getContext());
		NoteData.getInstance().initData();
		
		ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_note,container,false);
		
		layout.findViewById(R.id.fragment_note_add).setOnClickListener(v -> {
			NoteData.getInstance().setEditingNote(null);
			Intent intent = new Intent(getContext(),NoteEditActivity.class);
			startActivityForResult(intent,App.APP_REQUEST_CODE);
		});
		
		bigImage = layout.findViewById(R.id.fragment_note_image);
		
		ListView list = layout.findViewById(R.id.fragment_note_list);
		
		adapter = new NoteAdapter(getContext(),NoteData.getInstance().getData());
		adapter.setOnImageClickListener((imageView,path) -> bigImage.setImage(imageView,path));
		
		list.setAdapter(adapter);
		
		isCreated = true;
		return layout;
	}
	
	@Override
	public void onCreateMenu(ImageView view){
		if(!isCreated)return;
		super.onCreateMenu(view);
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
	}
	
	@Override
	public void onResume(String className,Intent data){
		if(!className.equals(this.getClass().getName())) return;
		Note note = NoteData.getInstance().getEditingNote();
		if(note.id==-1){
			int id = NoteData.getInstance().insertData();
			if(id==-1)toast("新建笔记失败！写入数据库失败");
			adapter.notifyDataSetChanged();
		}else{
			int id = NoteData.getInstance().update();
			if(id==-1)toast("修改笔记失败！写入数据库失败");
			adapter.notifyDataSetChanged();
		}
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
	
	@Override
	public String getTitle(){
		return "笔记";
	}
}
