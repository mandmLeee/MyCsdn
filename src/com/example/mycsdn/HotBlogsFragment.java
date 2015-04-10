package com.example.mycsdn;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.MyListView;
import com.custom.MyListView.OnLoadListener;
import com.custom.MyListView.OnRefreshListener;
import com.model.Blog;
import com.utils.HtmlFetchr;
import com.utils.NetworkState;

/**
 * 显示所有专栏页面
 */
public class HotBlogsFragment extends Fragment implements OnItemClickListener {
	private static final String URL_HOTBLOGS = "http://blog.csdn.net/hot.html?&page=";
	private static final String TAG = "HotBlogsFragment";

	private MyListView mHotBlogsListView; // 显示所有热文的自定义带下拉栏的MyListView
	private ProgressBar mProgressBar;
	private ArrayList<Blog> mBlogs; // 所有博客
	private HtmlFetchr fetchr; // 下载并解析html页面的对象
	private MyAdapter adapter; // ListView的设配器
	private Integer mPages;// 总页数
	private int mCurrentPage; // 当前页数

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "HotBlogFragment onCreate");
		fetchr = new HtmlFetchr();
		// 获取所有专栏列表
		mBlogs = new ArrayList<Blog>();
		mCurrentPage = 1;
		updateHotBlogs(URL_HOTBLOGS + mCurrentPage);
	}

	/**
	 * 刷新页面
	 */
	private void updateHotBlogs(final String urlSpec) {
		Log.i(TAG, "访问链接:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mBlogs = fetchr.downloadBlogs(mBlogs, HtmlFetchr.DROP_UPDATE,
						urlSpec); // 下载博客专栏列表
				mPages = fetchr.downloadPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				mProgressBar.setVisibility(View.INVISIBLE);
				// 更新ListView
				adapter = new MyAdapter(mBlogs);
				mHotBlogsListView.setAdapter(adapter);
				mHotBlogsListView.onRefreshComplete(); // 更新结束
			}
		}.execute();

	}

	/**
	 * 加载数据
	 */
	private void loadHotBlogs(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Blog> result = new ArrayList<>();
				result = fetchr.downloadBlogs(result, HtmlFetchr.UP_LOAD,
						urlSpec); // 下载博客专栏列
				mBlogs.addAll(result);
				mPages = fetchr.downloadPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				// 更新ListView
				adapter.notifyDataSetChanged();
				mHotBlogsListView.onLoadComplete();
			}
		}.execute();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_hotblogs, null);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.ProgressBar_hotblogs);
		mProgressBar.setVisibility(View.VISIBLE);
		mHotBlogsListView = (MyListView) view
				.findViewById(R.id.listview_hotblogs);
		mHotBlogsListView.setOnItemClickListener(this);
		mHotBlogsListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				updateHotBlogs(URL_HOTBLOGS + "1");
			}
		});

		mHotBlogsListView.setonLoadListener(new OnLoadListener() {

			@Override
			public void onLoad() {
				if (mCurrentPage == mPages) { // 如果到了最后一页
					mHotBlogsListView.onLoadNoData();
				} else {
					++mCurrentPage;
					loadHotBlogs(URL_HOTBLOGS + "" + mCurrentPage);
				}

			}
		});

		return view;
	}

	private class MyAdapter extends ArrayAdapter<Blog> {

		public MyAdapter(ArrayList<Blog> blogs) {
			super(getActivity(), 0, blogs);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Log.i(TAG, "getView position:" + position);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_blog_listview, null);
				holder = new ViewHolder();

				holder.typeText = (TextView) convertView
						.findViewById(R.id.blog_typeTextView);
				holder.titleText = (TextView) convertView
						.findViewById(R.id.blog_titleTextView);
				holder.bloggerIcon = (ImageView) convertView
						.findViewById(R.id.blogger_iconImageView);
				holder.contentText = (TextView) convertView
						.findViewById(R.id.blog_contentTextView);
				holder.authorText = (TextView) convertView
						.findViewById(R.id.blog_authorTextView);
				holder.agoText = (TextView) convertView
						.findViewById(R.id.blog_ago_TextView);
				holder.readnumText = (TextView) convertView
						.findViewById(R.id.blog_readNum_TextView);
				holder.contentText = (TextView) convertView
						.findViewById(R.id.blog_contentTextView);
				holder.commentnumText = (TextView) convertView
						.findViewById(R.id.blog_commentNum_TextView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 显示专栏头像
			String imageUrl = getItem(position).getBloggerIcon();
			holder.bloggerIcon.setTag(imageUrl);
			// 给imageView设置一个标签，用于存取于Cache和防止图片错位
			Bitmap bitmap = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// Log.i(TAG, "get image from cache!");
				// 如果在缓存中存在
				holder.bloggerIcon.setImageBitmap(bitmap);
			} else {
				// 设置默认头像，在下载完毕前使用此头像占位
				holder.bloggerIcon.setImageResource(R.drawable.ic_default);

				// 发送下载图片消息
				MainActivity.mThumbnailDownloader.queueThumbnail(
						holder.bloggerIcon, imageUrl);
			}

			// 显示作者
			holder.authorText.setText(getItem(position).getBlogAuthor());
			// 显示标题
			holder.titleText.setText(getItem(position).getBlogTitle());
			// 显示内容说明
			holder.contentText.setText(getItem(position).getBlogContent());
			// 显示类别
			holder.typeText.setText(getItem(position).getBlogType());
			// 显示阅读次数
			holder.readnumText.setText(getItem(position).getReadNum());
			// 显示发表时间
			holder.agoText.setText(getItem(position).getAgo());
			// 显示评论次数
			holder.commentnumText.setText(getItem(position).getCommentNumb());
			return convertView;
		}
	}

	private static class ViewHolder {
		public TextView typeText; // 文章类别
		public TextView titleText; // 文章标题
		public ImageView bloggerIcon;// 博主头像
		public TextView contentText; // 文章简介
		public TextView authorText; // 文章作者
		public TextView agoText; // 多久之前
		public TextView readnumText; // 阅读次数
		public TextView commentnumText; // 评论次数
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), BlogPagerActivity.class);
		// 将当前博客index和博客链表传递过去
		intent.putExtra(BlogPagerActivity.EXTRA_CURRENT_BLOG_INDEX,
				position - 1);
		Bundle bundle = new Bundle();
		bundle.putSerializable(BlogPagerActivity.EXTRA_BLOGS, mBlogs);
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
