package com.example.mycsdn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.PagerSlidingTabStrip;
import com.utils.ThumbnailDownloader;

public class MainActivity extends FragmentActivity implements
		android.view.View.OnClickListener {
	protected static final String TAG = "MainActivity";
	private PagerSlidingTabStrip mTabs; // 滑动Tabs
	private ViewPager mViewPager; // 存放Fragment的容器
	private DisplayMetrics mDm; // 当前屏幕的密度
	private MyPagerAdapter mAdapter; // 设配器

	private BlogColumnsFragment mBCSFragment; // 所有专栏
	private HomePageFragment mHPFragment; // 首页
	private HotBlogsFragment mHBFragment; // 热门文章
	private MyBlogsFragment mMBFraggment; // 我的博客
	public static ThumbnailDownloader<ImageView> mThumbnailDownloader; // 图片下载器

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 自定义ActionBars
		TextView localTextView = (TextView) findViewById(getResources()
				.getIdentifier("action_bar_title", "id", "android"));
		// 获取ActionBar中的TitleTextView
		// 第一个参数为ID名，第二个为资源属性是ID或者是Drawable，第三个为包名。
		localTextView.setTextSize(23.0F);
		localTextView.setTextColor(Color.parseColor("#ffffff"));

		// 开启响应下载图片消息的线程
		mThumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailDownloader
				.setListener(new ThumbnailDownloader.Listener<ImageView>() {
					@Override
					public void onThumbnailDownloaded(ImageView imageView,
							Bitmap thumbnail, String url) {
						if (imageView.getTag().equals(url)) {
							// Log.i(TAG, "tag与url对应");
							imageView.setImageBitmap(thumbnail); // 更新UI，上图
						} else {
							// Log.i(TAG, "tag与url不对应");
						}
					}
				});
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper(); // 必须要在start之后
		// -----------------------------------

		mDm = getResources().getDisplayMetrics(); // 获取当前屏幕的密度

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter); // 给ViewPager设配器
		mViewPager.setOffscreenPageLimit(3); // 设置viewPager缓存3个页面
		mTabs.setViewPager(mViewPager); // 将tabs和viewPager联系起来
		setTabsValue(); // 初始化tabs属性
	}

	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		mTabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		mTabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, mDm));
		// 设置Tab 滑动条的高度
		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, mDm));
		// 设置Tab标题文字的大小
		mTabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, mDm));
		// 设置Tab 滑动条的颜色
		mTabs.setIndicatorColor(Color.parseColor("#cc0000"));
		// 设置选中Tab文字的颜色 (这是我自定义的一个方法)
		mTabs.setSelectedTextColor(Color.parseColor("#cc0000"));
		// 取消点击Tab时的背景色
		mTabs.setTabBackground(0);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MyPagerAdapter";
		private final String[] titles = { "首页", "博客专栏", "热门文章", "我的博客" };

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: // 显示博客专家页面
				if (mHPFragment == null) {
					mHPFragment = new HomePageFragment();
				}
				return mHPFragment;
			case 1: // 显示所有博客专栏
				if (mBCSFragment == null) {
					mBCSFragment = new BlogColumnsFragment();
				}
				return mBCSFragment;
			case 2: // 显示热门文章页面
				if (mHBFragment == null) {
					mHBFragment = new HotBlogsFragment();
				}
				return mHBFragment;
			case 3: // 显示我的博客页面
				if (mMBFraggment == null) {
					mMBFraggment = new MyBlogsFragment();
				}
				return mMBFraggment;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return titles.length;
		}

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(TAG, "onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View aboutView = getLayoutInflater().inflate(R.layout.dialog_about,
				null);
		TextView goTextView = (TextView) aboutView.findViewById(R.id.textView6);
		goTextView.setOnClickListener(this);
		new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setView(
				aboutView).show();
		return super.onOptionsItemSelected(item);
	}

	/* 监听设备物理键，做出反应 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * keyCode: 被按下的键值即键盘码 event: 按键事件的对象，其中包括触发事件的详细信息。如事件发生时间等。
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")
					.setMessage("确认退出程序？")
					.setPositiveButton("确定", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton("取消", null).show();
		}
		return super.onKeyDown(keyCode, event);
		// false表示未处理此事件，它应该继续传播 相当于 return super.
		// true表示处理完此事件，不会继续传播
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.textView6) {
			// 启动浏览器
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri uri = Uri.parse("http://blog.csdn.net/u012964281");
			intent.setData(uri);
			startActivity(intent);
		}
	}

}
