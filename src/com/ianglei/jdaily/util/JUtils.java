package com.ianglei.jdaily.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		String appRoot = CommonUtil.getRootFilePath();
		
		isDirExist(appRoot);
		
		return appRoot;
	}
	
	public static void isDirExist(String dir)
	{
		File file = new File(dir);
		if(!file.exists())
		{
			file.mkdir();
		}
	}
	
	public static String getResourcePath(String url)
	{
		String resourcePath = "";
		Pattern p = null;
		if(url.contains("jpg"))
		{
			resourcePath = getAppRoot() + "jpg/";			
			p = Pattern.compile("([^/]{3,})jpg");
		}
		else if(url.endsWith("mp3"))
		{
			resourcePath = getAppRoot() + "mp3/";			
			p = Pattern.compile("([^/]{3,})mp3");
		}
		if(url.endsWith("pdf"))
		{
			resourcePath = getAppRoot() + "pdf/";			
			p = Pattern.compile("([^/]{3,})pdf");
		}
		
		isDirExist(resourcePath);
		
		Matcher m = p.matcher(url);
		String name = null;
		while (m.find()) {
			name = m.group(0) == null ? "" : m.group(0);
			resourcePath += name;
		}
		
		return resourcePath;
	}
}
