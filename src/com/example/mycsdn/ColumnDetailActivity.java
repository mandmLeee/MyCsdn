package com.example.mycsdn;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.model.Blog;
import com.model.Column;
import com.utils.HtmlFetchr;
import com.utils.NetworkState;
import com.utils.ThumbnailDownloader;

public class ColumnDetailActivity extends Activity implements
		OnItemClickListener {
	private static final String TAG = "ColumnDetailActivity";
	private static final String CSDN = "http://blog.csdn.net";
	private Column mColumn;
	private ArrayList<Blog> mBlogs; // 专栏的所有博客
	private ProgressBar mProgressBar;
	private TextView mColumnContentText; // 专栏说明
	private TextView mColumnReadNumbText; // 浏览次数
	private TextView mColumnBlogNumbText; // 文章数量
	private TextView mColumnBuildDateText; // 创建时间
	private ImageView mColumnImageView; // 专栏头像
	private ListView mBlogListView; // 博客列表
	private View mHeaderView; // listView的头部布局
	private HtmlFetchr fetchr; // 下载并解析html页面的对象

	@SuppressLint({ "InflateParams", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activtiy_columndetail);
		getActionBar().setDisplayHomeAsUpEnabled(true); // 后退导航键
		mHeaderView = getLayoutInflater().inflate(R.layout.columndetail_header,
				null);
		fetchr = new HtmlFetchr();
		Bundle bundle = getIntent().getExtras();
		// 获取BlogColumnsFragment传递过来的Column对象
		mColumn = (Column) bundle
				.getSerializable(BlogColumnsFragment.EXTRA_COLUMN_DETAIL);
		getActionBar().setTitle(mColumn.getColumnTitle());

		// 初始化控件
		mColumnContentText = (TextView) mHeaderView
				.findViewById(R.id.detail_columnContentText);
		mColumnReadNumbText = (TextView) mHeaderView
				.findViewById(R.id.detail_columnReadNumbText);
		mColumnBlogNumbText = (TextView) mHeaderView
				.findViewById(R.id.detail_columnBlogNumbText);
		mColumnBuildDateText = (TextView) mHeaderView
				.findViewById(R.id.detail_columnBuildDateText);
		mColumnImageView = (ImageView) mHeaderView
				.findViewById(R.id.detail_imageView);
		mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar_columnDetail);
		mProgressBar.setVisibility(View.VISIBLE);
		mBlogListView = (ListView) findViewById(R.id.listview_columndetail);
		mBlogListView.addHeaderView(mHeaderView, null, false); // 给listview添加头部布局
		mBlogListView.setOnItemClickListener(this);
		mColumnContentText.setText(mColumn.getColumnContent());
		// --------------------------------------------------
		String imageUrl = mColumn.getImageUrl();
		mColumnImageView.setTag(imageUrl);
		Bitmap bitmap = null;
		if ((bitmap = MainActivity.mThumbnailDownloader.getCacheImage(imageUrl)) != null) {
			// 如果在缓存中存在
			mColumnImageView.setImageBitmap(bitmap);
			Log.i(TAG, "image from Cache");
		} else {
			// 发送下载图片消息
			Log.i(TAG, "imageUrl:" + imageUrl);
			// 解决convertView复用引起的图片错位
			MainActivity.mThumbnailDownloader.queueThumbnail(mColumnImageView,
					imageUrl);
		}
		initUI(); // 初始化需要下载的UI

	}

	/**
	 * 初始化界面
	 */
	private void initUI() {
		if (!NetworkState.isNetworkConnected(this)) {
			Toast.makeText(this, "网络异常，请检查设置", Toast.LENGTH_LONG).show();
			return;
		}
		// 下载
		new AsyncTask<Void, Void, Bundle>() {

			@Override
			protected Bundle doInBackground(Void... params) {
				// 下载html for columnInfo 浏览次数、文章数量、创建时间
				Bundle result = fetchr.downloadColumnInfo(CSDN
						+ mColumn.getColumnUrl());
				mBlogs = fetchr.downloadColumnBlogs(CSDN
						+ mColumn.getColumnUrl());
				return result;
			};

			@Override
			protected void onPostExecute(Bundle result) {
				mProgressBar.setVisibility(View.INVISIBLE);
				// 更新界面
				mColumnReadNumbText.setText(result
						.getString(HtmlFetchr.EXTRA_COLUMN_READ_NUMB));
				mColumnBlogNumbText.setText(result
						.getString(HtmlFetchr.EXTRA_COLUMN_BLOG_NUMB));
				mColumnBuildDateText.setText(result
						.getString(HtmlFetchr.EXTRA_COLUMN_BUILD_DATE));

				MyAdapter adapter = new MyAdapter(mBlogs);
				mBlogListView.setAdapter(adapter);
			}
		}.execute();

	}

	private class MyAdapter extends ArrayAdapter<Blog> {

		public MyAdapter(ArrayList<Blog> items) {
			super(ColumnDetailActivity.this, 0, items);
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null; // 使用ViewHolder缓存convertView的组件，实习ListView优化
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.item_columndetail_listview, null);
				holder = new ViewHolder();
				holder.typeText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_type);
				holder.titleText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_title);
				holder.contentText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_content);
				holder.authorText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_author);
				holder.agoText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_ago);
				holder.readNumText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_readNum);
				holder.commentNumText = (TextView) convertView
						.findViewById(R.id.columndetail_blog_commentNum);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 博客Type
			if (getItem(position).getBlogType() != null)
				holder.typeText.setText(getItem(position).getBlogType());
			// 博客Title
			holder.titleText.setText(getItem(position).getBlogTitle());
			// 博客Content
			holder.contentText.setText(getItem(position).getBlogContent());
			// 博客Author
			holder.authorText.setText(mColumn.getColumnOwner());
			// 过去时间
			holder.agoText.setText(getItem(position).getAgo());
			// 阅读次数
			holder.readNumText.setText(getItem(position).getReadNum());
			// 评论次数
			holder.commentNumText.setText(getItem(position).getCommentNumb());
			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView typeText;// 博客Type
		public TextView titleText;// 博客Title
		public TextView contentText;
		public TextView authorText;
		public TextView agoText;
		public TextView readNumText;
		public TextView commentNumText;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // 后退导航键
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		Intent intent = new Intent(this, BlogPagerActivity.class);
		// 将当前博客index和博客链表传递过去
		intent.putExtra(BlogPagerActivity.EXTRA_CURRENT_BLOG_INDEX,
				position - 1);
		Bundle bundle = new Bundle();
		bundle.putSerializable(BlogPagerActivity.EXTRA_BLOGS, mBlogs);
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
