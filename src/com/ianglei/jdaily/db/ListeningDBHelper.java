package com.ianglei.jdaily.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.util.Log;

import com.ianglei.jdaily.model.ListeningItem;



public class ListeningDBHelper
{
	public static final String TAG = "ListeningDBHelper";
	
	public static final String TABLE_NAME = "t_listening";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_CATEGORY = "category";
	public static final String COLUMN_UPDATED = "updated";
	public static final String COLUMN_DESCRIBE = "describe";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_COVERPATH = "coverpath";
	public static final String COLUMN_MP3PATH = "mp3path";
	public static final String COLUMN_PDFPATH = "pdfpath";
	public static final String COLUMN_TRANSCRIPT = "transcript";
	public static final String COLUMN_LEARNEDTIMES = "learnedtimes";
	
	public static final String CREATE_TABLE_BBC6MIN = "create table " + TABLE_NAME +"("
			+ COLUMN_ID + " TEXT PRIMARY KEY, "
			+ COLUMN_TITLE + " TEXT, "
			+ COLUMN_CATEGORY + " INTEGER, "
			+ COLUMN_UPDATED + " TEXT, "
			+ COLUMN_DESCRIBE + " TEXT, "
			+ COLUMN_LINK + " TEXT, "
			+ COLUMN_COVERPATH + " TEXT, "
			+ COLUMN_MP3PATH + " TEXT, "
			+ COLUMN_PDFPATH + " TEXT, "
			+ COLUMN_TRANSCRIPT + " TEXT, "
			+ COLUMN_LEARNEDTIMES + " INTEGER)";
	
	public static void insertListeningInfo(ListeningItem item)
	{
		Log.i(TAG, "Insert Listening item: id="+item.getId() +", title="+item.getTitle()); 
		
		List<ListeningItem> list = getItemDetail(item.getId());
		if(0 == list.size())
		{
		
			SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
			
			ContentValues values = new ContentValues();
			values.put(COLUMN_ID, item.getId());
			values.put(COLUMN_TITLE, item.getTitle());
			values.put(COLUMN_CATEGORY, item.getCategory());
			values.put(COLUMN_UPDATED, item.getUpdated());
			values.put(COLUMN_DESCRIBE, item.getDescribe());
			values.put(COLUMN_LINK, item.getLink());
			values.put(COLUMN_COVERPATH, item.getCoverpath());
			values.put(COLUMN_MP3PATH, item.getMp3path());
			values.put(COLUMN_PDFPATH, item.getPdfpath());
			values.put(COLUMN_TRANSCRIPT, item.getTranscript());
			values.put(COLUMN_LEARNEDTIMES, 0);
			
	        long result =db.insert(TABLE_NAME, null, values);
	        Log.i(TAG, "Insert Listening item result "+ result);
	        
	        //db.close();
		}
		else {
			Log.i(TAG, "item: id="+item.getId() +", title="+item.getTitle() + " is already exist."); 
		}
	}
	
	public static List<ListeningItem> getItemDetail(String id) {
        List<ListeningItem> itemList = new ArrayList<ListeningItem>();
        
        String selectQuery = "SELECT * FROM " + TABLE_NAME; 
        selectQuery =  selectQuery + " where id='" + id + "'";
        Log.i(TAG, selectQuery);
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {           	
                ListeningItem item = new ListeningItem(cursor.getString(0)
                		                   		   ,cursor.getString(1)
                		                		   ,Integer.parseInt(cursor.getString(2))
                		                		   ,cursor.getString(3)
                		                		   ,cursor.getString(4)
                		                		   ,cursor.getString(5)
                		                		   ,cursor.getString(6)
                		                		   ,cursor.getString(7)
                		                		   ,cursor.getString(8)
                		                		   ,cursor.getString(9)
                		                		   ,Integer.parseInt(cursor.getString(10)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
               
        //db.close();
        
        return itemList;
    }
	
	public static ArrayList<ListeningItem> getItemList(int category) {
        ArrayList<ListeningItem> itemList = new ArrayList<ListeningItem>();
        
        String selectQuery = "SELECT " + COLUMN_ID + "," + COLUMN_TITLE + "," 
        					+ COLUMN_UPDATED + "," + COLUMN_DESCRIBE + "," + COLUMN_LINK + ","
        					+ COLUMN_COVERPATH + "," + COLUMN_MP3PATH + "," + COLUMN_TRANSCRIPT
        					+ "," + COLUMN_LEARNEDTIMES
        					+ " FROM " + TABLE_NAME + " where " + COLUMN_CATEGORY + "=" + category; 

        Log.i(TAG, selectQuery);
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {           	
            	ListeningItem item = new ListeningItem(cursor.getString(0)
                		                   		   ,cursor.getString(1)
                		                		   ,cursor.getString(2)
                		                		   ,cursor.getString(3)
                		                		   ,cursor.getString(4)
                		                		   ,cursor.getString(5)
                		                		   ,cursor.getString(6)
                		                		   ,cursor.getString(7)
                		                		   ,Integer.parseInt(cursor.getString(8)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        Collections.sort(itemList,new Comparator<ListeningItem>(){  
//            @Override  
//            public int compare(ListeningItem b1, ListeningItem b2) { 
//            
//            	SimpleDateFormat fmt =new SimpleDateFormat("dd mm yyyy");
//            	try {
//					Date date1 = fmt.parse(b1.getUpdated().split("/")[1]);
//					Date date2 = fmt.parse(b2.getUpdated().split("/")[1]);
//					return date1.compareTo(date2);
//					
//				} catch (ParseException e) {
//
//					e.printStackTrace();
//				}            	
//            	return b1.getUpdated().compareTo(b2.getUpdated());  
//            }  
//              
//        });    
        
        //db.close();
        
        Collections.reverse(itemList);
        return itemList;
    }
	
	public synchronized static void updateCoverImgPath(String urlPath, String localPath)
	{
		Log.i(TAG, "Update url path:"+urlPath + ", local path:" + localPath); 
		
		SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_COVERPATH, localPath);
		int resultrow = db.update(TABLE_NAME, values, "coverpath=?", new String[]{urlPath});
		Log.i(TAG, "Update row " + resultrow);
		//db.close();
	}
	
	public static void updateTranscript(ListeningItem item)
	{
		Log.i(TAG, "Update Listening item: id="+item.getId() +"'s transcript"); 
		
		SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_TRANSCRIPT, item.getTranscript());
		int resultrow = db.update(TABLE_NAME, values, "id=?", new String[]{item.getId()});
		Log.i(TAG, "Update row " + resultrow);
		//db.close();
	}
	
	public static ListeningItem queryDetailById(ListeningItem item)
	{
		Log.i(TAG, "Update Listening item: id="+item.getId() +"'s transcript");
		
		String selectQuery = "SELECT " + COLUMN_TRANSCRIPT + ","+ COLUMN_MP3PATH
		+ " FROM " + TABLE_NAME; 
        selectQuery =  selectQuery + " where id='" + item.getId() + "'";
        Log.i(TAG, selectQuery);
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                item.setTranscript(cursor.getString(0));
                item.setMp3path(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
               
        //db.close();
        
        return item;
	}
	
	public static void updateLearnedTimes()
	{
		

	}
}
