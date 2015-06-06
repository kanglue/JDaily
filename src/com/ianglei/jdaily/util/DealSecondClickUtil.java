package com.ianglei.jdaily.util;

public class DealSecondClickUtil
{
	private static long lastClickTime;
    
    private static boolean hasMutex = false;
    
    private DealSecondClickUtil()
    {
        
    }
    
    /**
     * 判断是否快速点击
     * 
     * @param time 间隔时间
     * @return TRUE 快速点击
     * @see [类、类#方法、类#成员]
     */
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
    
    /**
     * 设置是否存在互斥点击
     * 
     * @param flag 是否存在互斥点击
     * @return void
     * @see [类、类#方法、类#成员]
     */
    public static void setMutexClick(boolean flag)
    {
        hasMutex = flag;
    }
    
    /**
     * 获取是否存在互斥点击
     * 
     * @return TRUE 存在互斥点击
     * @see [类、类#方法、类#成员]
     */
    public static boolean isMutexClick()
    {
        return hasMutex;
    }
}
