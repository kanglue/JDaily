package com.ianglei.jdaily.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DownPlayImageView extends ImageView {

	private int index = -1;
	private boolean downloaded = false;
	
	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public DownPlayImageView(Context context) {
		super(context);

	}
	
    public DownPlayImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    
    public DownPlayImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
