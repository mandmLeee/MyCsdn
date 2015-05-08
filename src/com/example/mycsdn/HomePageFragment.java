package com.example.mycsdn;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.CustomFAB;
import com.custom.MyListView;
import com.custom.MyListView.OnLoadListener;
import com.custom.MyListView.OnRefreshListener;
import com.model.Blog;
import com.utils.HtmlFetchr;
import com.utils.NetworkState;

/**
 * 显示所有专栏页面
 */
public class HomePageFragment extends Fragment implements OnItemClickListener,
		OnClickListener, OnDismissListener {
	private static final String URL_HOME_HOTS = "http://blog.csdn.net/index.html?&page="; // 最热
	private static final String URL_HOME_NEWS = "http://blog.csdn.net/newest.html?&page="; // 最新
	private static final int STATE_HOME_HOTS = 1;
	private static final int STATE_HOME_NEWS = 2;
	private static final String TAG = "HomePageFragment";
	private static final int NUM_OF_VISIBLE_LIST_ROWS = 2;

	private MyListView mHomeListView; // 显示所有热文的自定义带下拉栏的MyListView
	private CustomFAB mCustomFAB; // 悬浮按钮
	private ProgressBar mProgressBar; // 进度条
	private ArrayList<Blog> mBlogs; // 所有博客
	private HtmlFetchr fetchr; // 下载并解析html页面的对象
	private MyAdapter adapter; // ListView的设配器
	private Integer mPages;// 总页数
	private int mCurrentPage; // 当前页数
	private RotateAnimation animation;
	private PopupWindow mPop; // 选择最热/最新文章弹窗
	private ListView mNorHListView; // 弹窗内容组件
	private int mState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "HomePageFragment onCreate");
		animation = new RotateAnimation(0, 90,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250); // 持续时间
		animation.setFillAfter(true);

		fetchr = new HtmlFetchr();
		// 获取所有专栏列表
		mBlogs = new ArrayList<Blog>();
		mCurrentPage = 1;
		mState = STATE_HOME_HOTS;
		// 初始状态为加载最热博客
		updateHomeBlogs(URL_HOME_HOTS + mCurrentPage);
	}

	/**
	 * 刷新页面
	 */
	private void updateHomeBlogs(final String urlSpec) {
		Log.i(TAG, "访问链接:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Blog> result = fetchr.downloadBlogs(mBlogs,
						HtmlFetchr.DROP_UPDATE, urlSpec); // 下载主页博客
				if (result == null) {
					Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT)
							.show();
				} else {
					mBlogs = result;
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				if (getActivity() != null) {
					// 隐藏进度条
					mProgressBar.setVisibility(View.INVISIBLE);
					mCustomFAB.setVisibility(View.VISIBLE);
					// 更新ListView
					adapter = new MyAdapter(mBlogs);
					mHomeListView.setAdapter(adapter);
					mHomeListView.onRefreshComplete(); // 更新结束
				}
			}
		}.execute();

	}

	/**
	 * 加载数据
	 */
	private void loadHomeBlogs(final String urlSpec) {

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
				if (result == null) {
					Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT)
							.show();
				} else {
					mBlogs.addAll(result);
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				
				// 更新ListView
				adapter.notifyDataSetChanged();
				mHomeListView.onLoadComplete();
			}
		}.execute();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_homepage, null);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.ProgressBar_homepage);
		mProgressBar.setVisibility(View.VISIBLE);
		mCustomFAB = (CustomFAB) view.findViewById(R.id.menu_customFAB);
		mCustomFAB.setVisibility(View.INVISIBLE);
		mCustomFAB.setOnClickListener(this);
		mHomeListView = (MyListView) view.findViewById(R.id.listview_homeblogs);
		mHomeListView.setOnItemClickListener(this);
		mHomeListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (mState == STATE_HOME_HOTS) {
					updateHomeBlogs(URL_HOME_HOTS + "1");
				} else {
					updateHomeBlogs(URL_HOME_NEWS + "1");
				}
			}
		});

		mHomeListView.setonLoadListener(new OnLoadListener() {

			@Override
			public void onLoad() {
				if (mCurrentPage == mPages) { // 如果到了最后一页
					mHomeListView.onLoadNoData();
				} else {
					++mCurrentPage;
					loadHomeBlogs(URL_HOME_HOTS + "" + mCurrentPage);
				}

			}
		});
		return view;
	}

	/**
	 * 初始化弹出框
	 */
	@SuppressLint("InflateParams")
	private void initPop() {
		View layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.listview_neworhot, null);
		mNorHListView = (ListView) layout.findViewById(R.id.NOH_listView);
		mPop = new PopupWindow(layout);
		mPop.setFocusable(true); // popupwindow中的ListView才可以接收点击事件

		ArrayList<String> items = new ArrayList<>();
		items.add("最新文章");
		items.add("最热文章");
		ArrayAdapter<String> nohAdapter = new ArrayAdapter<>(getActivity(),
				R.layout.item_neworhot_listview, items);
		mNorHListView.setAdapter(nohAdapter);
		mNorHListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) { // 最新文章
					Toast.makeText(getActivity(), "最新文章", Toast.LENGTH_SHORT)
							.show();
					mPop.dismiss();
					mCurrentPage = 1;
					mState = STATE_HOME_NEWS;
					updateHomeBlogs(URL_HOME_NEWS + mCurrentPage);
				} else { // 最热文章
					Toast.makeText(getActivity(), "最热文章", Toast.LENGTH_SHORT)
							.show();
					mPop.dismiss();
					mCurrentPage = 1;
					mState = STATE_HOME_HOTS;
					updateHomeBlogs(URL_HOME_HOTS + mCurrentPage);
				}
			}

		});

		// 控制popupwindow的宽度和高度自适应
		mNorHListView.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		// 估算ListView的尺寸
		// 参数：View.MeasureSpec.UNSPECIFIED 表示：父控件不限定子控件的大小

		mPop.setWidth(mNorHListView.getMeasuredWidth());
		mPop.setHeight(mNorHListView.getMeasuredHeight()
				* NUM_OF_VISIBLE_LIST_ROWS);

		mPop.setOnDismissListener(this);
		// 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
		mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
		mPop.setOutsideTouchable(true);
		// 触摸popupwindow外部，popupwindow消失。这个要求你的popupwindow要有背景图片才可以成功，如上
	}

	private class MyAdapter extends ArrayAdapter<Blog> {

		public MyAdapter(ArrayList<Blog> blogs) {
			super(getActivity(), 0, blogs);
		}

		@SuppressLint("InflateParams")
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
				holder.bloggerIcon = (GifImageView) convertView
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
			GifDrawable gifDrawable = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// 如果在静态图缓存中存在
				holder.bloggerIcon.setImageBitmap(bitmap);
			} else if ((gifDrawable = MainActivity.mThumbnailDownloader
					.getGifCacheImage(imageUrl)) != null) {
				// 如果在动态图缓存中存在
				holder.bloggerIcon.setImageDrawable(gifDrawable);
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
		public GifImageView bloggerIcon;// 博主头像
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

	@Override
	public void onClick(View v) {
		mCustomFAB.startAnimation(animation);
		if (mPop == null) {
			initPop();
		}
		if (!mPop.isShowing()) {
			int x = mCustomFAB.getWidth() + dip2px(20);
			int y = dip2px(43);
			mPop.showAtLocation(mCustomFAB, Gravity.RIGHT | Gravity.BOTTOM, x,
					y);// show mPop
		} else {
			mPop.dismiss();
		}
	}

	/**
	 * 将dp转换成px
	 */
	private int dip2px(float dpValue) {
		float scale = getActivity().getResources().getDisplayMetrics().density; // 像素点密度
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	public void onDismiss() {
		mCustomFAB.startAnimation(animation);
	}

}
