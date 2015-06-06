package com.ianglei.jdaily;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.ianglei.jdaily.db.DBAgent;
import com.ianglei.jdaily.fragment.ListFragment;
import com.ianglei.jdaily.fragment.MenuFragment;
import com.ianglei.jdaily.menu.OnMenuItemClickListener;
import com.ianglei.jdaily.util.ToastUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity implements
		OnMenuItemClickListener, ActionBar.TabListener, OnPageChangeListener
{
	private static final String TAG = "MainActivity";

	private String[] tabTitles;

	private ViewPager viewPager;

	private List<Fragment> fragmentList;

	private ActionBar actionBar;

	private Toast toast;
	private boolean isBackExit;

	SlidingMenu sm;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Crittercism.initialize(getApplicationContext(),
				"54c4aebb51de5e9f042ed078");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		DBAgent.init(this);

		toast = new ToastUtil().showToast(this);
		// set the behind view
		setBehindContentView(R.layout.frame_menu);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();

		MenuFragment menuFragment = new MenuFragment();
		menuFragment.setOnMenuItemClickListener(this);
		fragmentTransaction.replace(R.id.menu, menuFragment);
		fragmentTransaction.commit();

		// customize the slidingmenu
		sm = getSlidingMenu();
		sm.setShadowWidth(50);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffset(100);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		tabTitles = getResources().getStringArray(R.array.tab_title);
		fragmentList = new ArrayList<Fragment>();
		viewPager = (ViewPager) findViewById(R.id.viewPager);

		actionBar = getSupportActionBar();

		actionBar.setDisplayShowTitleEnabled(true);

		actionBar.setDisplayShowHomeEnabled(true);

		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_gradient));

		for (int i = 0; i < tabTitles.length; i++)
		{
			ActionBar.Tab tab = actionBar.newTab();
			tab.setText(tabTitles[i]);
			tab.setTabListener(this);
			actionBar.addTab(tab, i);
		}

		for (int i = 0; i < tabTitles.length; i++)
		{
			Fragment fragment = new ListFragment();
			Bundle args = new Bundle();
			args.putString("arg", tabTitles[i]);
			fragment.setArguments(args);

			fragmentList.add(fragment);
		}

		viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(),
				fragmentList));

		viewPager.setOnPageChangeListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId())
		{
		case android.R.id.home:
			// handle clicking the app icon/logo
			if (sm.isMenuShowing())
			{
				sm.showContent(true);
			} else
			{
				sm.showMenu(true);
			}
			return true;
		case R.id.menu_settings:
			toast.setText("settings");
			toast.show();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0)
	{
		actionBar.setSelectedNavigationItem(arg0);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChange(int id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onBackPressed()
	{
		if (isBackExit)
		{
			finish();
		} else
		{
			isBackExit = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run()
				{
					isBackExit = false;
				}
			}, 2000);
			toast.setText(getResources().getString(R.string.back_again_to_exit));
			toast.show();
		}
	}
}
