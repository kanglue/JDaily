package com.ianglei.jdaily.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public final class DebugLog
{
    
    private static final String TAG = "huaweiott";
    
    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String LOG_PATH = Environment.getExternalStorageDirectory().getPath() + "/OTT/LOG/";
    
    private static final String FILE_TYPE = ".txt";
    
    private static volatile File logfile;
    
    private static final long ERR_LOG_SIZE = 1024 * 1024;
    
    private DebugLog()
    {
    }
    
    private static boolean needDebug = true;
    
    private static boolean enabled()
    {
        return needDebug;
    }
    
    public static void switchDebug(boolean flag)
    {
        needDebug = flag;
    }
    
    private static synchronized String buildMsg(String msg)
    {
        StringBuilder buffer = new StringBuilder();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ms");
        String time = format.format(date);
        
        buffer.append(time);
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        buffer.append(" [");
        buffer.append(Thread.currentThread().getName());
        buffer.append(":");
        buffer.append(stackTraceElement.getFileName());
        buffer.append(":");
        buffer.append(stackTraceElement.getLineNumber());
        buffer.append(":");
        buffer.append(stackTraceElement.getMethodName());
        buffer.append("()] ");
        buffer.append(msg);
        buffer.append(NEW_LINE);
        return buffer.toString();
    }
    
    public static void verbose(String tag, String msg)
    {
        if (enabled())
        {
            Log.v(TAG + tag, msg);
        }
    }
    
    public static void debug(String tag, String msg)
    {
        if (enabled())
        {
            Log.d(TAG + tag, msg);
//            if (WRITE_SDCARD_LOG)
//            {
//                writeFileToSD(buildMsg(msg));
//            }
        }
    }
    
    public static void info(String tag, String msg)
    {
        if (enabled())
        {
            Log.i(TAG + tag, msg);
        }
    }
    
    public static void warn(String tag, String msg)
    {
        if (enabled())
        {
            String info = buildMsg(msg);
            Log.w(TAG + tag, info);
//            if (WRITE_SDCARD_LOG)
//            {
//                writeFileToSD(info);
//            }
        }
    }
    
    public static void error(String tag, String msg)
    {
        if (enabled())
        {
            String info = buildMsg(msg);
            Log.e(TAG + tag, info);
//            if (WRITE_SDCARD_LOG)
//            {
//                writeFileToSD(info);
//            }
        }
    }
    
    private static String printStackTraceAsCause(StackTraceElement[] causedTrace, Throwable e)
    {
        StackTraceElement[] trace = e.getStackTrace();
        int m = trace.length - 1, n = causedTrace.length - 1;
        while (m >= 0 && n >= 0 && trace[m].equals(causedTrace[n]))
        {
            m--;
            n--;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(e.toString());
        builder.append(NEW_LINE);
        for (int i = 0; i <= m; i++)
        {
            builder.append(trace[i].toString());
            builder.append(NEW_LINE);
        }
        String ret = builder.toString();
        
        Throwable ourCause = e.getCause();
        if (ourCause != null)
        {
            String temp = printStackTraceAsCause(trace, ourCause);
            ret = temp + ret;
        }
        return ret;
    }
    
    public static String getStackString(Throwable e)
    {
        StackTraceElement[] trace = e.getStackTrace();
        StringBuilder builder = new StringBuilder();
        builder.append(e.toString());
        builder.append(NEW_LINE);
        for (StackTraceElement temp : trace)
        {
            builder.append(temp.toString());
            builder.append(NEW_LINE);
        }
        
        String ret = builder.toString();
        Throwable ourCause = e.getCause();
        if (ourCause != null)
        {
            String child = printStackTraceAsCause(trace, ourCause);
            ret = child + ret;
        }
        return ret;
        
    }
    
    public static void printException(Exception e)
    {
        if (enabled())
        {
            Log.e(TAG, getStackString(e));
//            if (WRITE_SDCARD_LOG)
//            {
//                writeFileToSD(getStackString(e));
//            }
        }
    }
    
    public static void printException(String tag, Exception e)
    {
        if (enabled())
        {
            Log.e(tag, getStackString(e));
//            if (WRITE_SDCARD_LOG)
//            {
//                
//                writeFileToSD(getStackString(e));
//            }
        }
    }
    
    public static void printException(String tag, String secondLevelMsg, Exception e)
    {
        if (enabled())
        {
            Log.e(tag, getStackString(e));
//            if (WRITE_SDCARD_LOG)
//            {
//                writeFileToSD(getStackString(e));
//            }
        }
    }
    
    public static String exceptionMessageGenerator(Exception e)
    {
        return " :Exception cause: " + e.getCause() + " message: " + e.getMessage();
    }
    
    public static void addLog(String value, int endLines)
    {
        if (!isSdcardCanWrite())
        {
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ms");
        String timeString = format.format(new Date(System.currentTimeMillis()));
        StringBuilder builder = new StringBuilder(timeString);
        builder.append(" ");
        builder.append(value);
        builder.append(NEW_LINE);
        for (int iLoop = 0; iLoop < endLines; iLoop++)
        {
            builder.append(NEW_LINE);
        }
        
        appendToFile(builder.toString(), chooseFileName("_HeartBeat"));
    }
    
    private static void writeFileToSD(String context)
    {
        if (!isSdcardCanWrite())
        {
            return;
        }
        
        appendToFile(context, chooseFileName("_LOG"));
    }
    
    private static void appendToFile(String text, File file)
    {
        if (file == null)
        {
            return;
        }
        if (makeDirs(LOG_PATH))
        {
            RandomAccessFile raf = null;
            try
            {
                raf = new RandomAccessFile(file.getAbsolutePath(), "rw");
                raf.seek(file.length());
                raf.write(text.getBytes());
            }
            catch (Exception e)
            {
                printExecption(e);
            }
            finally
            {
                if (null != raf)
                {
                    try
                    {
                        raf.close();
                    }
                    catch (IOException e)
                    {
                        printExecption(e);
                    }
                }
            }
        }
    }
    
    private static void printExecption(Exception exception)
    {
        String ss = getStackString(exception);
        Log.e(TAG, ss);
    }
    
    private static boolean makeDirs(String path)
    {
        boolean isComplete = true;
        if (!isFileExists(path))
        {
            File file = new File(path);
            isComplete = file.mkdirs();
        }
        return isComplete;
    }
    
    private static boolean isFileExists(String path)
    {
        if (TextUtils.isEmpty(path))
        {
            return false;
        }
        return new File(path).exists();
    }
    
    private static boolean isSdcardCanWrite()
    {
        boolean ret = true;
        String sdStatus = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(sdStatus))
        {
            Log.d(TAG, "SD card is not avaiable right now.");
            ret = false;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdStatus))
        {
            Log.d(TAG, "Not allow write SD card!");
            ret = false;
        }
        return ret;
    }
    
    private static File chooseFileName(String type)
    {
        
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()) + type + FILE_TYPE;
        if (logfile == null)
        {
            deleteUnusedFile(type + FILE_TYPE);
            logfile = new File(LOG_PATH + fileName);
        }
        else
        {
            if (logfile.length() > ERR_LOG_SIZE)
            {
                logfile = new File(LOG_PATH + fileName);
            }
        }
        
        return logfile;
    }
    
    private static void deleteUnusedFile(String simpleName)
    {
        List<String> rawList = new ArrayList<String>();
        File file = new File(LOG_PATH);
        String[] files = file.list();
        if (files != null)
        {
            rawList = Arrays.asList(files);
        }
        
        List<String> names = new ArrayList<String>();
        for (String fileTemp : rawList)
        {
            if (fileTemp.matches("[0-9]{4}[0-9]{2}[0-9]{2}[0-9]{2}[0-9]{2}[0-9]{2}" + simpleName))
            {
                names.add(fileTemp);
            }
        }
        if (names.size() > 2)
        {
            Comparator<String> comparator = new Comparator<String>()
            {
                @Override
                public int compare(String arg1, String arg2)
                { //逆排�?                
                    String str1 = arg1.substring(0, 13);
                    String str2 = arg2.substring(0, 13);
                    return str2.compareTo(str1);
                }
                
            };
            Collections.sort(names, comparator);
            
            for (int i = 0; i < names.size(); i++)
            {
                if (i >= 2)
                {
                    File deletedFile = new File(LOG_PATH + names.get(i));
                    deletedFile.delete();
                }
            }
        }
        
    }
}
