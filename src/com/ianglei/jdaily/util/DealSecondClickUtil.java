package com.ianglei.jdaily.util;

public class DealSecondClickUtil
{
	private static long lastClickTime;
    
    private static boolean hasMutex = false;
    
    private DealSecondClickUtil()
    {
        
    }

    public static boolean isFastDoubleClick(long time)
    {
        long curTime = System.currentTimeMillis();
        long timeSpace = curTime - lastClickTime;
        lastClickTime = curTime;
        if (0 < timeSpace && timeSpace < time)
        {
            return true;
        }
        return false;
    }
    

    public static void setMutexClick(boolean flag)
    {
        hasMutex = flag;
    }
    

    public static boolean isMutexClick()
    {
        return hasMutex;
    }
}
