package com.ianglei.jdaily.util;

public class DealSecondClickUtil
{
	private static long lastClickTime;
    
    private static boolean hasMutex = false;
    
    private DealSecondClickUtil()
    {
        
    }
    
    /**
     * �ж��Ƿ���ٵ��
     * 
     * @param time ���ʱ��
     * @return TRUE ���ٵ��
     * @see [�ࡢ��#��������#��Ա]
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
     * �����Ƿ���ڻ�����
     * 
     * @param flag �Ƿ���ڻ�����
     * @return void
     * @see [�ࡢ��#��������#��Ա]
     */
    public static void setMutexClick(boolean flag)
    {
        hasMutex = flag;
    }
    
    /**
     * ��ȡ�Ƿ���ڻ�����
     * 
     * @return TRUE ���ڻ�����
     * @see [�ࡢ��#��������#��Ա]
     */
    public static boolean isMutexClick()
    {
        return hasMutex;
    }
}
