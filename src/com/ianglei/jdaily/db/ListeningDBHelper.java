package com.ianglei.jdaily.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ianglei.jdaily.model.ListeningItem;



public class ListeningDBHelper
{
	private static final String TAG = "ListeningDBHelper";
	
	private static final String TABLE_NAME = "Listening";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_TITLE = "title";
	private static final String COLUMN_CATEGORY = "category";
	private static final String COLUMN_UPDATED = "updated";
	private static final String COLUMN_DESCRIBE = "describe";
	private static final String COLUMN_LINK = "link";
	private static final String COLUMN_COVERPATH = "coverpath";
	private static final String COLUMN_MP3PATH = "mp3path";
	private static final String COLUMN_PDFPATH = "pdfpath";
	private static final String COLUMN_TRANSCRIPT = "transcript";
	private static final String COLUMN_LEARNEDTIMES = "learnedtimes";
	
	private static final String STEP = "10";
	
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
	
	public static boolean isListeningExist(String id)
	{
		boolean result;
		String selectQuery = "SELECT * FROM " + TABLE_NAME; 
        selectQuery =  selectQuery + " where id='" + id + "'";
        Log.i(TAG, selectQuery);
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToNext())
        {
        	result = true;
        }
        else
        {
        	result = false;
        }
        
        cursor.close();
        return result;
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
	
	public static int getCountByCategory(int category)
	{
		int count = 0;
		String selectQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " where "
				+ COLUMN_CATEGORY + "=" + category;
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	count = cursor.getInt(0);
            }while (cursor.moveToNext());
        }
        cursor.close();
        Log.i(TAG, "The count of category " + category + " is " + count);
        
        return count;
	}
	
	public static ArrayList<ListeningItem> getItemListByCategory(int category, int startPos) {
        ArrayList<ListeningItem> itemList = new ArrayList<ListeningItem>();
        
        String selectQuery = "SELECT " + COLUMN_ID + "," + COLUMN_TITLE + "," 
        					+ COLUMN_UPDATED + "," + COLUMN_DESCRIBE + "," + COLUMN_LINK + ","
        					+ COLUMN_COVERPATH + "," + COLUMN_MP3PATH + "," + COLUMN_TRANSCRIPT
        					+ "," + COLUMN_LEARNEDTIMES
        					+ " FROM " + TABLE_NAME + " where " + COLUMN_CATEGORY + "=" + category
        					+ " order by " + COLUMN_UPDATED + " DESC limit " + STEP + " offset " + startPos; 

        Log.i(TAG, selectQuery);
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        Log.i(TAG, cursor.getCount() + " records");
 
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
        
        //Collections.reverse(itemList);
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
	
	public synchronized static void updateCoverImgPath(ListeningItem item)
	{
		String localPath = item.getCoverpath();
		Log.i(TAG, "Update url path:"+localPath); 
		
		SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_COVERPATH, localPath);
		int resultrow = db.update(TABLE_NAME, values, "id=?", new String[]{item.getId()});
		Log.i(TAG, "Update row " + resultrow);
		//db.close();
	}

//	public static void updateTranscript(ListeningItem item)
//	{
//		Log.i(TAG, "Update Listening item: id="+item.getId() +"'s transcript"); 
//		
//		SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
//		ContentValues values = new ContentValues();
//		values.put(COLUMN_TRANSCRIPT, item.getTranscript());
//		int resultrow = db.update(TABLE_NAME, values, "id=?", new String[]{item.getId()});
//		Log.i(TAG, "Update row " + resultrow);
//		//db.close();
//	}
	
	public static void updateInfoPath(ListeningItem item)
	{
		Log.i(TAG, "Update Listening item: id="+item.getId() +"'s mp3 path"); 
		
		SQLiteDatabase db = DBAgent.getInstance().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_MP3PATH, item.getMp3path());
		values.put(COLUMN_TRANSCRIPT, item.getTranscript());
		values.put(COLUMN_PDFPATH, item.getPdfpath());
		int resultrow = db.update(TABLE_NAME, values, "id=?", new String[]{item.getId()});
		Log.i(TAG, "Update row " + resultrow);
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
	
}
