package com.qust.assistant.ui.note;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qust.assistant.R;
import com.qust.assistant.sql.NoteData;
import com.qust.assistant.sql.PictureData;
import com.qust.assistant.ui.BaseAnimActivity;
import com.qust.assistant.util.DateUtil;
import com.qust.assistant.util.LogUtil;
import com.qust.assistant.widget.BigImage;
import com.qust.assistant.widget.PictureGrid;
import com.qust.assistant.widget.TimePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

public class NoteEditActivity extends BaseAnimActivity{
    // 拍照
    private static final int TAKE_PHOTO = 1;
    // 从相册
    private static final int CHOOSE_PHOTO = 2;
    
    private static final int REQUEST_PERMISSION = 3;
    // 如果勾选了不再询问
    private static final int NOT_NOTICE = 4;
    
    private TextView title;
    
    private TextView text;
    
    private TextView date,deadline;
    
    private BigImage bigImage;
    
    private LinearLayout content;

    private PictureGrid pictureGrid;
    
    private Calendar c_date,c_deadline;
    
    private Note data;

    private ArrayList<EditItem> child;

    private ArrayList<String> pictures;
    
    private File file;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        child = new ArrayList<>();
    
        title = findViewById(R.id.activity_note_edit_title);
        text = findViewById(R.id.activity_note_edit_text);
        
        data = NoteData.getInstance().getEditingNote();

        if(data==null){
            data = new Note();
            NoteData.getInstance().setEditingNote(data);
            c_date = Calendar.getInstance();
            data.date = DateUtil.getDateString(c_date);
            pictures = new ArrayList<>();
        }else{
            if(!"".equals(data.date)){
                c_date = Calendar.getInstance();
                c_date.setTime(DateUtil.getDateString(data.date));
            }
            if(!"".equals(data.deadline)){
                c_deadline = Calendar.getInstance();
                c_deadline.setTime(DateUtil.getDateString(data.deadline));
            }
            
            pictures = data.pictures;
            title.setText(data.title);
            text.setText(data.text);
        }

        findViewById(R.id.activity_note_edit_back).setOnClickListener(view -> finish());

        findViewById(R.id.activity_note_edit_done).setOnClickListener(view -> {
            
            data.title = title.getText().toString();
            data.text = text.getText().toString();
            
            Note.Item[] item = new Note.Item[child.size()];
            for(int i=0;i<item.length;i++)item[i] = child.get(i).getItem();
            data.items = item;
            
            data.pictures = pictures;
            setResult(RESULT_OK,getIntent().putExtra("class",NoteActivity.class.getName()));
            finish();
        });

        findViewById(R.id.activity_note_edit_add).setOnClickListener(view -> addChild(new EditItem(new Note.Item(false,""))));

        findViewById(R.id.activity_note_edit_shoot).setOnClickListener(v -> takePicture());
        
        findViewById(R.id.activity_note_edit_picture).setOnClickListener(v -> choosePicture());
        
        date = findViewById(R.id.activity_note_edit_date);
        deadline = findViewById(R.id.activity_note_edit_deadline);
        
        content = findViewById(R.id.activity_note_edit_items);

        date.setOnClickListener(view -> showDate(R.id.activity_note_edit_date));
        deadline.setOnClickListener(view -> showDate(R.id.activity_note_edit_deadline));

        date.setText(getString(R.string.str_date,data.date));
        
        if(c_deadline!=null)deadline.setText(getString(R.string.str_commit,data.deadline));

        for(int i=0;i<data.items.length;i++) addChild(new EditItem(data.items[i]));
    
        bigImage = findViewById(R.id.activity_note_edit_image);
        
        pictureGrid = findViewById(R.id.activity_note_edit_photo);
        pictureGrid.setPictures(pictures);
        pictureGrid.setClickListener((imageView,path) -> {
            bigImage.setImage(imageView,path);
        });
        pictureGrid.setLongClickListener((index) -> {
            new MaterialDialog.Builder(NoteEditActivity.this).title("删除")
                    .content("是否删除这张图片(图片源文件不会被删除)")
                    .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
                    .positiveText("确定").onPositive((dialog,which) -> {
                        pictures.remove(index);
                        pictureGrid.setPictures(pictures);
            }).show();
        });
    }

    private void addChild(EditItem item){
        child.add(item);
        content.addView(item.getLayout());
    }

    private void removeChild(EditItem d){
        try{
            content.removeView(d.getLayout());
            child.remove(d);
        }catch(NullPointerException e){
            LogUtil.Log(e);
            Toast.makeText(this,"出错!"+e.getClass().getName(),Toast.LENGTH_LONG).show();
        }
    }

    private void showDate(final int id){
        TimePicker p = null;
        if(id==R.id.activity_note_edit_date){
            p = new TimePicker(this,c_date);
        }else if(id==R.id.activity_note_edit_deadline){
            p = new TimePicker(this,c_deadline);
        }
        final TimePicker P = p;
        new MaterialDialog.Builder(this)
                .customView(p.getLayout(),false)
                .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
                .positiveText("确定").onPositive((dialog,which) -> {
                    if(id==R.id.activity_note_edit_date){
                        c_date = P.getTime();
                        data.date = DateUtil.getDateString(c_date);
                        date.setText(getString(R.string.str_date,data.date));
                    }else if(id==R.id.activity_note_edit_deadline){
                        c_deadline = P.getTime();
                        data.deadline = DateUtil.getDateString(c_deadline);
                        deadline.setText(getString(R.string.str_commit,data.deadline));
                    }
        }).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==TAKE_PHOTO){
            if(resultCode==RESULT_OK){
                PictureData.getInstance().addPicture(file.toString(),DateUtil.getDateString(Calendar.getInstance()));
                pictures.add(file.toString());
                ((BaseAdapter)pictureGrid.getAdapter()).notifyDataSetChanged();
            }
        }else if(requestCode==CHOOSE_PHOTO){
            if(resultCode==RESULT_OK){
                String filePath = null;
                Uri uri = data.getData();
                if(DocumentsContract.isDocumentUri(NoteEditActivity.this,uri)){
                    // 如果是document类型的 uri, 则通过document id来进行处理
                    String documentId = DocumentsContract.getDocumentId(uri);
                    if("com.android.providers.media.documents".equals(uri.getAuthority())){ // MediaProvider
                        // 使用':'分割
                        String id = documentId.split(":")[1];
                        String selection = MediaStore.Images.Media._ID + "=?";
                        String[] selectionArgs = {id};
                        filePath = getDataColumn(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection,selectionArgs);
                    }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){ // DownloadsProvider
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(documentId));
                        filePath = getDataColumn(contentUri,null,null);
                    }
                }else if("content".equalsIgnoreCase(uri.getScheme())){
                    // 如果是 content 类型的 Uri
                    filePath = getDataColumn(uri,null,null);
                }else if("file".equals(uri.getScheme())){
                    // 如果是 file 类型的 Uri,直接获取图片对应的路径
                    filePath = uri.getPath();
                }
                PictureData.getInstance().addPicture(filePath,DateUtil.getDateString(Calendar.getInstance()));
                pictures.add(filePath);
                ((BaseAdapter)pictureGrid.getAdapter()).notifyDataSetChanged();
            }
        }else if(requestCode==NOT_NOTICE){
            // 由于不知道是否选择了允许所以需要再次判断
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION);
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQUEST_PERMISSION){
            for(int i = 0;i<permissions.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    // 选择了“始终允许”
                    takePicture();
                }else{
                    // 用户选择了禁止不再询问
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){
                        new MaterialDialog.Builder(this).title("权限").content("请允许应用访问相机以进行拍照！")
                                .positiveText("取消").onPositive((dialog,which) -> {
                            Toast.makeText(this,"没有相机权限，无法进行拍照",Toast.LENGTH_SHORT).show();
                            finish();
                        }).negativeText("确定").onNegative((dialog,which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",getPackageName(),null);//注意就是"package",不用改成自己的包名
                            intent.setData(uri);
                            startActivityForResult(intent,NOT_NOTICE);
                            dialog.dismiss();
                        }).show();
                    }else{
                        // 选择禁止
                        new MaterialDialog.Builder(this).title("权限").content("请允许应用访问相机以进行拍照！")
                                .positiveText("取消").onPositive((dialog,which) -> {
                            Toast.makeText(this,"没有相机权限，无法进行拍照",Toast.LENGTH_SHORT).show();
                            finish();
                        }).negativeText("确定").onNegative((dialog,which) -> {
                            ActivityCompat.requestPermissions(NoteEditActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
                            dialog.dismiss();
                        }).show();
                    }
                }
            }
        }
    }
    
    private void takePicture() {
        
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION);
            return;
        }
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imageUri;
        String name = "Note_" + System.currentTimeMillis() + ".jpg";
        file = new File(PictureData.picturePath,name);
        if(android.os.Build.VERSION.SDK_INT > 24){
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imageUri = FileProvider.getUriForFile(this, "com.university.assistant", file);
        }else{
            // 从文件中创建uri
            imageUri = Uri.fromFile(file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void choosePicture(){
        //开启选择呢绒界面
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        Uri imageUri;
        String name = "Note_" + System.currentTimeMillis() + ".jpg";
        file = new File(PictureData.picturePath,name);
        if(android.os.Build.VERSION.SDK_INT > 24){
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imageUri = FileProvider.getUriForFile(this, "com.university.assistant", file);
        }else{
            // 从文件中创建uri
            imageUri = Uri.fromFile(file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //设置可以缩放
        // intent.putExtra("scale", true);
        //设置可以裁剪
        // intent.putExtra("crop", true);
        intent.setType("image/*");
        //设置输出位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //开始选择
        startActivityForResult(intent, CHOOSE_PHOTO);
    }
    
    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     * @return
     */
    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        String path = null;
        
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }
    
    private class EditItem implements View.OnLongClickListener{

        private CheckBox checkBox;

        private EditText text;
        // 自身根布局
        private LinearLayout layout;

        public EditItem(Note.Item item){
            layout = (LinearLayout)LayoutInflater.from(NoteEditActivity.this).inflate(R.layout.item_note_item_editable,null);
            layout.setOnLongClickListener(this);
            
            checkBox = layout.findViewById(R.id.item_note_checkbox);
            checkBox.setChecked(item.isFinish);
            checkBox.setOnLongClickListener(this);
            
            text = layout.findViewById(R.id.item_note_item);
            text.setText(item.text);
            text.setOnLongClickListener(this);
        }

        public Note.Item getItem(){
            return new Note.Item(checkBox.isChecked(),text.getText().toString());
        }
        
        public final LinearLayout getLayout(){ return layout; }
    
        @Override
        public boolean onLongClick(View v){
            new MaterialDialog.Builder(NoteEditActivity.this).title("删除")
                    .content("是否删除")
                    .negativeText("取消").onNegative((dialog,which) -> dialog.dismiss())
                    .positiveText("确定").onPositive((dialog,which) -> removeChild(EditItem.this)).show();
            return true;
        }
        
    }
    
    //    public void showSystemKeyboard(EditText editText){
    //        hideKeyboard();
    //        inputManager.showSoftInput(editText, 0);
    //    }
    //
    //    public void hideSystemKeyboard(EditText editText){
    //        inputManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    //    }
    
    //    public void showKeyboard(){
    //        if(isKeyboardShowing)return;
    //        inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
    //        isKeyboardShowing = true;
    //        keyboard.show();
    //    }
}
