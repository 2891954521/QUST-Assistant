package com.university.assistant.fragment.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.university.assistant.App;
import com.university.assistant.R;
import com.university.assistant.sql.NoteData;
import com.university.assistant.ui.note.NoteEditActivity;
import com.university.assistant.widget.PictureGrid;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends BaseAdapter{

    private Context context;

    private ArrayList<Note> data;

    private PictureGrid.OnImageClickListener listener;
    
    private OnItemClickListener onItemClickListener;

    public NoteAdapter(Context _context,ArrayList<Note> _data){
        context = _context;
        data = _data;
    }
    
    @Override
    public int getCount(){
        return data.size();
    }
    
    @Override
    public Object getItem(int position){
        return null;
    }
    
    @Override
    public long getItemId(int position){
        return 0;
    }
    
    @Override
    public View getView(int position,View view,ViewGroup parent){
        if(view==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_note,null);
        }
        TextView title = view.findViewById(R.id.item_note_title);
        TextView text = view.findViewById(R.id.item_note_text);
        TextView date = view.findViewById(R.id.item_note_date);
        TextView deadline = view.findViewById(R.id.item_note_deadline);
    
        LinearLayout content = view.findViewById(R.id.item_note_items);
    
        PictureGrid pictureGrid = view.findViewById(R.id.item_note_photo);
        
        final Note data = NoteAdapter.this.data.get(position);
        
        if(TextUtils.isEmpty(data.title))title.setVisibility(View.GONE);
        else{
            title.setVisibility(View.VISIBLE);
            title.setText(data.title);
        }
        
        if(TextUtils.isEmpty(data.text))text.setVisibility(View.GONE);
        else{
            text.setVisibility(View.VISIBLE);
            text.setText(data.text);
        }
        
        date.setText(data.date);
        deadline.setText(data.deadline);
    
        content.removeAllViews();
        
        for(int i = 0;i<data.items.length;i++){
            LinearLayout layout = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.item_note_item,null);
            CheckBox checkBox = layout.findViewById(R.id.item_check);
            checkBox.setChecked(data.items[i].isFinish);
            checkBox.setOnCheckedChangeListener(createCheckListener(data,i));
            ((TextView)layout.findViewById(R.id.item_text)).setText(data.items[i].text);
            content.addView(layout);
        }
        
        pictureGrid.setPictures(data.pictures);
        pictureGrid.setClickListener(listener);
        
        view.setOnLongClickListener(v -> {
            new MaterialDialog.Builder(context)
                    .title("操作")
                    .items(new String[]{"编辑","删除"})
                    .itemsCallback((dialog,itemView,index,charSequence)->{
                        if(index==0){
                            NoteData.getInstance().setEditingNote(data);
                            Intent intent = new Intent(context,NoteEditActivity.class);
                            ((Activity)context).startActivityForResult(intent,App.APP_REQUEST_CODE);
                        }else if(index==1){
                            new MaterialDialog.Builder(context)
                                    .title("是否删除笔记？")
                                    .content("笔记中的图片不会被删除")
                                    .positiveText("确定").onPositive((d,which)->{
                                        NoteData.getInstance().delete(data.id);
                                        NoteAdapter.this.data.remove(data);
                                        notifyDataSetChanged();
                                    })
                                    .negativeText("取消").onNegative((d,which)->d.dismiss())
                                    .show();
                        }
                    })
                    .positiveText("取消").onPositive((dialog,which)->dialog.dismiss())
                    .show();
            //if(onItemClickListener!=null) onItemClickListener.onItemClick(v,data.get(getLayoutPosition()));
            return true;
        });
        
        return view;
    }
    
    private CompoundButton.OnCheckedChangeListener createCheckListener(final Note data,final int i){
        return (compoundButton,b) -> {
            data.items[i].isFinish = b;
            NoteData.getInstance().updateIsFinish(data);
        };
    }
    
    public void setData(ArrayList<Note> _data){
        data = _data;
        notifyDataSetChanged();
    }
    
    public void setOnImageClickListener(PictureGrid.OnImageClickListener listener){
        this.listener = listener;
    }
    
    public void setOnItemClickListener(OnItemClickListener _onItemClickListener){
        onItemClickListener = _onItemClickListener;
    }
    
    private interface OnItemClickListener{
        /**
         * @param view 点击的item的视图
         * @param data 点击的item的数据
         */
        void onItemClick(View view,Note data);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder{
        
        private TextView title;
        private TextView text;
        private TextView date,deadline;
        private LinearLayout content;
        
        private ImageView p1,p2,p3;
        
        public DataViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.item_note_title);
            text = view.findViewById(R.id.item_note_text);
            date = view.findViewById(R.id.item_note_date);
            deadline = view.findViewById(R.id.item_note_deadline);

            content = view.findViewById(R.id.item_note_items);
            
            view.setOnLongClickListener(v -> {
                if(onItemClickListener!=null) onItemClickListener.onItemClick(v,data.get(getLayoutPosition()));
                return true;
            });
        }

        public void initData(final Note data){
            title.setText(data.title);
            text.setText(data.text);
            date.setText(data.date);
            deadline.setText(data.deadline);
            
            content.removeAllViews();
            for(int i = 0;i<data.items.length;i++){
                LinearLayout layout = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.item_note_item,null);
                CheckBox checkBox = layout.findViewById(R.id.item_check);
                checkBox.setChecked(data.items[i].isFinish);
                checkBox.setOnCheckedChangeListener(createCheckListener(data,i));
                ((TextView)layout.findViewById(R.id.item_text)).setText(data.items[i].text);
                content.addView(layout);
            }
        }

        private CompoundButton.OnCheckedChangeListener createCheckListener(final Note data,final int i){
            return (compoundButton,b) -> data.items[i].isFinish = b;
        }

    }
}