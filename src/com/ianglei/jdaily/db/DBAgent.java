package com.ianglei.jdaily.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAgent extends SQLiteOpenHelper
{
	private static final String TAG = "DBAgent";
	
	private static final int DB_VERSION = 1;

	private static String db_root;
	private static final String DB_PATH = "/databases/";
	private static final String DB_NAME = "jdaily.db";
	
	public DBAgent(Context context)
	{
		super(context, context.getFilesDir().getParent() + DB_PATH + DB_NAME, null, DB_VERSION);
	}

	public static DBAgent dbAgent;
	
	public static synchronized void init(Context context)
	{
		if(null == dbAgent)
		{
			dbAgent = new DBAgent(context);
		}
		
		//b_root = context.getFilesDir().getParent();
		
		//Log.d(TAG, "DB_ROOT: " + db_root);
	}
	
	public static DBAgent getInstance()
	{
		return dbAgent;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
	    db.execSQL(ListeningDBHelper.CREATE_TABLE_BBC6MIN);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void close() {
		Log.d(TAG,"Close DataBase");
		super.close();

	}

}
