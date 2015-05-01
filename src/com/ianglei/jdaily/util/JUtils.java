package com.ianglei.jdaily.util;

import java.io.File;

import android.os.Environment;

public class JUtils
{
	public static String sdRoot;
	
	public static String getStorageRoot()
	{
		sdRoot=Environment.getExternalStorageDirectory().getPath();
		
		return sdRoot;
	}
	
	public static String getAppRoot()
	{
		String appRoot = CommonUtil.getRootFilePath() + "JDaily/";
		
		File file = new File(appRoot);
		if(!file.exists())
		{
			file.mkdir();
		}
		
		return appRoot;
	}
	
	public static String getResourcePath(String url)
	{
		String resourcePath = "";
		if(url.contains("jpg"))
		{
			resourcePath = getAppRoot() + "jpg/";
		}
		else if(url.endsWith("mp3"))
		{
			resourcePath = getAppRoot() + "mp3/";
		}
		if(url.endsWith("pdf"))
		{
			resourcePath = getAppRoot() + "pdf/";
		}
		
		File file = new File(resourcePath);
		if(!file.exists())
		{
			file.mkdir();
		}
		
		
		String[] str = url.split("/");
		int size = str.length;
		String fileName = str[size-2];
		
//		int fileNameIndex;
//		fileNameIndex = url.lastIndexOf("/");
//		String fileName = url.substring(fileNameIndex);
		return resourcePath + fileName;
	}
}
