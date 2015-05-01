package com.ianglei.jdaily.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.ianglei.jdaily.util.DebugLog;

public class HttpUrlDownloader implements UrlDownloader
{
    private static final String TAG = "HttpUrlDownloader";
    
    public static interface RequestPropertiesCallback
    {
        public List<NameValuePair> getHeadersForRequest(Context context, String url);
    }
    
    private RequestPropertiesCallback mRequestPropertiesCallback;
    
    public RequestPropertiesCallback getRequestPropertiesCallback()
    {
        return mRequestPropertiesCallback;
    }
    
    public void setRequestPropertiesCallback(final RequestPropertiesCallback callback)
    {
        mRequestPropertiesCallback = callback;
    }
    
    @Override
    public void download(final Context context, final String url, final String filename,
        final UrlDownloaderCallback callback, final UrlLoadCallback completion)
    {
        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(final Void... params)
            {
                InputStream is = null;
                try
                {
                    String thisUrl = url;
                    HttpURLConnection urlConnection;
                    int responseCode = 0;
                    while (true)
                    {
                        final URL u = new URL(thisUrl);
                        urlConnection = (HttpURLConnection)u.openConnection();
                        urlConnection.setInstanceFollowRedirects(true);
                        urlConnection.setUseCaches(true);
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setReadTimeout(5000);
                        
                        if (mRequestPropertiesCallback != null)
                        {
                            final List<NameValuePair> props =
                                mRequestPropertiesCallback.getHeadersForRequest(context, url);
                            if (props != null)
                            {
                                for (final NameValuePair pair : props)
                                {
                                    urlConnection.addRequestProperty(pair.getName(), pair.getValue());
                                }
                            }
                        }
                        responseCode = urlConnection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP
                            && responseCode != HttpURLConnection.HTTP_MOVED_PERM)
                        {
                            break;
                        }
                        thisUrl = urlConnection.getHeaderField("Location");
                    }
                    
                    if (responseCode != HttpURLConnection.HTTP_OK)
                    {
                        DebugLog.error(TAG, "Response Code: " + responseCode + "  at url:" + url);
                        return null;
                    }
                    is = urlConnection.getInputStream();
                    callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
                    
                    return null;
                }
                catch (final Exception e)
                {
                    DebugLog.printException(TAG, e);
                    return null;
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (IOException e)
                        {
                            DebugLog.printException(TAG, e);
                        }
                    }
                    
                }
            }
            
            @Override
            protected void onPostExecute(final Void result)
            {
                if (completion != null)
                {
                    completion.onLoadComplete();
                }
            }
        };
        
        try
        {
            UrlImageViewHelper.executeTask(downloader);
        }
        catch (RejectedExecutionException e)
        {
            DebugLog.printException(e);
            completion.onLoadComplete();
        }
        
    }
    
    @Override
    public boolean allowCache()
    {
        return true;
    }
    
    @Override
    public boolean canDownloadUrl(String url)
    {
        return url.startsWith("http");
    }
}
