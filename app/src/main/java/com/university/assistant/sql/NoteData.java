package com.university.assistant.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.university.assistant.fragment.note.Note;

import java.util.ArrayList;

public class NoteData{
    
    private static NoteData noteData;

    private ArrayList<Note> notes;
    
    private Note editingNote;
    
    private NoteData(Context context){
        initData();
    }

    public static void init(Context context){
        synchronized(NoteData.class){
            if(noteData == null){
                noteData = new NoteData(context);
            }
        }
    }

    public static NoteData getInstance(){
        return noteData;
    }

    public void initData(){
        notes = new ArrayList<>(20);
        SQLiteDatabase db = DataBase.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM `Note`;", null);
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()) notes.add(new Note(cursor));
        }
        cursor.close();
        db.close();
    }

    public int insertData(){
        if(editingNote==null)return -1;
        SQLiteDatabase write = DataBase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isFinish",editingNote.isFinish);
        values.put("title",editingNote.title);
        values.put("text",editingNote.text);
        values.put("date",editingNote.date);
        values.put("deadline",editingNote.deadline);
        values.put("items",editingNote.items2Json().toString());
        values.put("pictures",editingNote.picture2Json().toString());
        int id = (int)write.insert("Note","title",values);
        if(id!=-1){
            editingNote.id = id;
            notes.add(editingNote);
        }
        write.close();
        editingNote = null;
        return id;
    }
    
    public int update(){
        if(editingNote==null)return -1;
        SQLiteDatabase db = DataBase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isFinish",editingNote.isFinish);
        values.put("title",editingNote.title);
        values.put("text",editingNote.text);
        values.put("date",editingNote.date);
        values.put("deadline",editingNote.deadline);
        values.put("items",editingNote.items2Json().toString());
        values.put("pictures",editingNote.picture2Json().toString());
        // 返回值大于0代表修改更新成功
        int i = db.update("Note",values,"id = ?",new String[]{ String.valueOf(editingNote.id) });
        db.close();
        editingNote = null;
        return i;
    }
    
    public void updateIsFinish(Note note){
        SQLiteDatabase db = DataBase.getInstance().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("items", note.items2Json().toString());
        // 返回值大于0代表修改更新成功
        int i = db.update("Note",values,"id = ?",new String[]{ String.valueOf(note.id) });
        db.close();
    }
    
    public void delete(int id){
        SQLiteDatabase db = DataBase.getInstance().getWritableDatabase();
        // 返回值为受影响的行数，大于0代表成功
        int i = db.delete("Note", "id = ?",new String[]{ String.valueOf(id) });
        db.close();
    }
    
    public ArrayList<Note> getData(){ return notes; }
    
    public Note getEditingNote(){
        return editingNote;
    }
    
    public void setEditingNote(Note _editingNote){
        editingNote = _editingNote;
    }
    
}
