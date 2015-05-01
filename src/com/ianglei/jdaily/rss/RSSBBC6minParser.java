package com.ianglei.jdaily.rss;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.ianglei.jdaily.model.ListeningItem;

import android.util.Log;
import android.util.Xml;

public class RSSBBC6minParser
{
	private static final String TAG = "RSSBBC6minParser";
	
	public static List<ListeningItem> getRssItems(InputStream is) {
		List<ListeningItem> list = null;

		ListeningItem item = null;

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(is, "utf-8");
			int type = parser.getEventType();

			while (type != XmlPullParser.END_DOCUMENT) {

				switch (type) {

				case XmlPullParser.START_DOCUMENT:
					list = new ArrayList<ListeningItem>();
					break;
				case XmlPullParser.START_TAG:
					if ("entry".equals(parser.getName())) {
						item = new ListeningItem();
					}

					if (item != null) {
						if ("id".equals(parser.getName())) {
							item.setId(parser.nextText());
						} else if ("title".equals(parser.getName())) {
							item.setTitle(parser.nextText());
						} else if ("link".equals(parser.getName())) {
							item.setLink(parser.getAttributeValue(1));
						} else if ("updated".equals(parser.getName())) {
							item.setUpdated(parser.nextText());
						} else if ("summary".equals(parser.getName())) {
							item.setDescribe(parser.nextText());
						}
					}

					break;

				case XmlPullParser.END_TAG:
					if ("entry".equals(parser.getName())) {
						
						Log.d("", item.toString());
						list.add(item);
						item = null;
					}

					break;

				}

				type = parser.next();
			}
			return list;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, e.toString());
			return null;
		}

	}
}
