package com.example.mycsdn;

import java.util.ArrayList;

import com.model.Blog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class BlogPagerActivity extends FragmentActivity {
	public static final String EXTRA_CURRENT_BLOG_INDEX = "current_blog_index";
	public static final String EXTRA_BLOGS = "blogs";
	private ViewPager mViewPager; // ����Fragment������
	private ArrayList<Blog> mBlogs; 

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager); 
		setContentView(mViewPager); // �������ݲ���
		// ��ȡ��ǰ����index�Ͳ�������
		int currentBlogIndex;
		currentBlogIndex = getIntent().getIntExtra(EXTRA_CURRENT_BLOG_INDEX, 0);
		mBlogs = (ArrayList<Blog>) getIntent().getExtras().getSerializable(EXTRA_BLOGS);
		
		FragmentManager fm = getSupportFragmentManager();
		// ����Adapter�У���getItem()���ص�BlogFragment�����ύ�����������
		mViewPager.setAdapter(new FragmentPagerAdapter(fm) {
			
			@Override
			public int getCount() {
				return mBlogs.size();
			}
			
			@Override
			public Fragment getItem(int position) {
				return ShowBlogFragment.newInstance(mBlogs.get(position).getBlogUrl());
			}
		});
		
		mViewPager.setCurrentItem(currentBlogIndex); //����ָ����ҳ��
	}
	

}
