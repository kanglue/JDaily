package com.ianglei.jdaily.fragment;

import com.ianglei.jdaily.model.ListeningItem;


public interface UpdateCallback {
	public void startProgress(final ListeningItem item, final int position);
}
