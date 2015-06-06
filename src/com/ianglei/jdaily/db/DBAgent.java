package com.ianglei.jdaily.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ianglei.jdaily.util.CommonUtil;

public class DBAgent extends SQLiteOpenHelper
{
	private static final String TAG = "DBAgent";
	
	private static final int DB_VERSION = 1;

	//private static final String DB_PATH = "db/";
	private static final String DB_NAME = "jdaily.db";
	
    /** 
     * 在SQLiteOpenHelper的子类当中，必须有该构造函数 
     * @param context   上下文对象 
     * @param name      数据库名称 
     * @param factory 
     * @param version   当前数据库的版本，值必须是整数并且是递增的状态 
     */  
	public DBAgent(Context context, String name, CursorFactory factory,  int version)
	{
		//必须通过super调用父类当中的构造函数  
        super(context, name, factory, version); 
		//super(context, CommonUtil.getRootFilePath() + DB_PATH + DB_NAME, null, DB_VERSION);
	}
	
	public DBAgent(Context context, String name, int version){  
        this(context,name,null,version);  
    }  
  
    public DBAgent(Context context, String name){  
        this(context,name,DB_VERSION);  
    }  
    
    public DBAgent(Context context){  
        this(context,CommonUtil.getRootFilePath() + DB_NAME, DB_VERSION);  
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

	}

	@Override
	public synchronized void close() {
		Log.d(TAG,"Close DataBase");
		super.close();

	}

}
