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
	private PagerSlidingTabStrip mTabs; // ����Tabs
	private ViewPager mViewPager; // ���Fragment������
	private DisplayMetrics mDm; // ��ǰ��Ļ���ܶ�
	private MyPagerAdapter mAdapter; // ������

	private BlogColumnsFragment mBCSFragment; // ����ר��
	private HomePageFragment mHPFragment; // ��ҳ
	private HotBlogsFragment mHBFragment; // ��������
	private MyBlogsFragment mMBFraggment; // �ҵĲ���
	public static ThumbnailDownloader<ImageView> mThumbnailDownloader; // ͼƬ������

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// �Զ���ActionBars
		TextView localTextView = (TextView) findViewById(getResources()
				.getIdentifier("action_bar_title", "id", "android"));
		// ��ȡActionBar�е�TitleTextView
		// ��һ������ΪID�����ڶ���Ϊ��Դ������ID������Drawable��������Ϊ������
		localTextView.setTextSize(23.0F);
		localTextView.setTextColor(Color.parseColor("#ffffff"));

		// ������Ӧ����ͼƬ��Ϣ���߳�
		mThumbnailDownloader = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailDownloader
				.setListener(new ThumbnailDownloader.Listener<ImageView>() {
					@Override
					public void onThumbnailDownloaded(ImageView imageView,
							Bitmap thumbnail, String url) {
						if (imageView.getTag().equals(url)) {
							// Log.i(TAG, "tag��url��Ӧ");
							imageView.setImageBitmap(thumbnail); // ����UI����ͼ
						} else {
							// Log.i(TAG, "tag��url����Ӧ");
						}
					}
				});
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper(); // ����Ҫ��start֮��
		// -----------------------------------

		mDm = getResources().getDisplayMetrics(); // ��ȡ��ǰ��Ļ���ܶ�

		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new MyPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter); // ��ViewPager������
		mViewPager.setOffscreenPageLimit(3); // ����viewPager����3��ҳ��
		mTabs.setViewPager(mViewPager); // ��tabs��viewPager��ϵ����
		setTabsValue(); // ��ʼ��tabs����
	}

	/**
	 * ��PagerSlidingTabStrip�ĸ������Խ��и�ֵ��
	 */
	private void setTabsValue() {
		// ����Tab���Զ��������Ļ��
		mTabs.setShouldExpand(true);
		// ����Tab�ķָ�����͸����
		mTabs.setDividerColor(Color.TRANSPARENT);
		// ����Tab�ײ��ߵĸ߶�
		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, mDm));
		// ����Tab �������ĸ߶�
		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, mDm));
		// ����Tab�������ֵĴ�С
		mTabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, mDm));
		// ����Tab ����������ɫ
		mTabs.setIndicatorColor(Color.parseColor("#cc0000"));
		// ����ѡ��Tab���ֵ���ɫ (�������Զ����һ������)
		mTabs.setSelectedTextColor(Color.parseColor("#cc0000"));
		// ȡ�����Tabʱ�ı���ɫ
		mTabs.setTabBackground(0);
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MyPagerAdapter";
		private final String[] titles = { "��ҳ", "����ר��", "��������", "�ҵĲ���" };

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
			case 0: // ��ʾ����ר��ҳ��
				if (mHPFragment == null) {
					mHPFragment = new HomePageFragment();
				}
				return mHPFragment;
			case 1: // ��ʾ���в���ר��
				if (mBCSFragment == null) {
					mBCSFragment = new BlogColumnsFragment();
				}
				return mBCSFragment;
			case 2: // ��ʾ��������ҳ��
				if (mHBFragment == null) {
					mHBFragment = new HotBlogsFragment();
				}
				return mHBFragment;
			case 3: // ��ʾ�ҵĲ���ҳ��
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

	/* �����豸�������������Ӧ */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/*
		 * keyCode: �����µļ�ֵ�������� event: �����¼��Ķ������а��������¼�����ϸ��Ϣ�����¼�����ʱ��ȡ�
		 */
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(MainActivity.this).setTitle("ϵͳ��ʾ")
					.setMessage("ȷ���˳�����")
					.setPositiveButton("ȷ��", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setNegativeButton("ȡ��", null).show();
		}
		return super.onKeyDown(keyCode, event);
		// false��ʾδ������¼�����Ӧ�ü������� �൱�� return super.
		// true��ʾ��������¼��������������
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.textView6) {
			// ���������
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri uri = Uri.parse("http://blog.csdn.net/u012964281");
			intent.setData(uri);
			startActivity(intent);
		}
	}

}
