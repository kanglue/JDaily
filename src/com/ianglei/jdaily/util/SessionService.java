package com.ianglei.jdaily.util;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.service.textservice.SpellCheckerService.Session;

public class SessionService
{
	private static final String TAG = "SessionService";
    
    private Session session = null;
    
    private static SessionService gInstance = null;
    
    private Context myContext = null;

    
    public Context getMyContext()
    {
        return myContext;
    }
    
    public void setMyContext(Context context)
    {
        myContext = context;
    }
    
    private static ComponentName loginComponent = null;
    
    public static synchronized void setAppContext(Context appContext)
    {
        getInstance().setMyContext(appContext);
    }
    
    public static synchronized void setLoginComponent(String packageName, String activityName)
    {
        loginComponent = new ComponentName(packageName, activityName);
    }
    
    public static synchronized ComponentName getLoginComponent()
    {
        return loginComponent;
    }
    
    
    public static synchronized Context getAppContext()
    {
        Context context = getInstance().getMyContext();
        if (context == null)
        {
        	//throw new Exception("MemService appContext is null!");
        	DebugLog.error(TAG, "context is null");
        	return null;
        }
        else
        {
            return context;
        }
    }
    
    public static synchronized SessionService getInstance()
    {
        if (gInstance == null)
        {
            gInstance = new SessionService();
            
        }
        return gInstance;
    }
    
    
}
