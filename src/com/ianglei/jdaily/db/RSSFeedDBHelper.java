package com.ianglei.jdaily.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ianglei.jdaily.model.RssFeed;

public class RSSFeedDBHelper
{
	public static final String TABLE_RSSFEED = "t_rssfeed";
	
    // Contacts Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LINK = "feedUrl";
    public static final String KEY_DESCRIPTION = "feedDescription";
	
    public static final String CREATE_TABLE_RSS_FEED = "CREATE TABLE " + TABLE_RSSFEED + "(" 
                                 + KEY_ID  + " INTEGER PRIMARY KEY," 
    		                     + KEY_TITLE + " TEXT," 
                                 + KEY_LINK  + " TEXT," 
    		                     + KEY_DESCRIPTION + " TEXT" + ")";
    
    public static List<String> firstTimeInitSql()
    {
    	ArrayList<String> list = new ArrayList<String>();
    	
    	list.add("INSERT INTO trssfeed VALUES (101, 'VOA Latest','http://www.51voa.com/voa.xml','Voa latest')");
    	list.add("INSERT INTO trssfeed VALUES (102, 'VOA Special','http://www.51voa.com/sp.xml','VOA Special English')");
    	list.add("INSERT INTO trssfeed VALUES (103, 'VOA Standard','http://www.51voa.com/st.xml','VOA Standard English')");
    	list.add("INSERT INTO trssfeed VALUES (201, '6 Minute English','http://www.bbc.co.uk/learningenglish/english/features/6-minute-english','6-minute-english')");
    	return list;     	
    }
    
    /**
     * Reading all rows from database
     * */
    public static List<RssFeed> getAllFeeds() {
        List<RssFeed> feedList = new ArrayList<RssFeed>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RSSFEED  + " ORDER BY id DESC"; 
        SQLiteDatabase db = DBAgent.getInstance().getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {           	
            	RssFeed feed = new RssFeed(Integer.parseInt(cursor.getString(0))
                		                		   ,cursor.getString(1)
                		                		   ,cursor.getString(2)
                		                		   ,cursor.getString(3));
                feedList.add(feed);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return feedList;
    }
}
