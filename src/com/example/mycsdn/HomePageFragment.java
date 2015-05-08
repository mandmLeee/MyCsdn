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
 * ��ʾ����ר��ҳ��
 */
public class HomePageFragment extends Fragment implements OnItemClickListener,
		OnClickListener, OnDismissListener {
	private static final String URL_HOME_HOTS = "http://blog.csdn.net/index.html?&page="; // ����
	private static final String URL_HOME_NEWS = "http://blog.csdn.net/newest.html?&page="; // ����
	private static final int STATE_HOME_HOTS = 1;
	private static final int STATE_HOME_NEWS = 2;
	private static final String TAG = "HomePageFragment";
	private static final int NUM_OF_VISIBLE_LIST_ROWS = 2;

	private MyListView mHomeListView; // ��ʾ�������ĵ��Զ������������MyListView
	private CustomFAB mCustomFAB; // ������ť
	private ProgressBar mProgressBar; // ������
	private ArrayList<Blog> mBlogs; // ���в���
	private HtmlFetchr fetchr; // ���ز�����htmlҳ��Ķ���
	private MyAdapter adapter; // ListView��������
	private Integer mPages;// ��ҳ��
	private int mCurrentPage; // ��ǰҳ��
	private RotateAnimation animation;
	private PopupWindow mPop; // ѡ������/�������µ���
	private ListView mNorHListView; // �����������
	private int mState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "HomePageFragment onCreate");
		animation = new RotateAnimation(0, 90,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250); // ����ʱ��
		animation.setFillAfter(true);

		fetchr = new HtmlFetchr();
		// ��ȡ����ר���б�
		mBlogs = new ArrayList<Blog>();
		mCurrentPage = 1;
		mState = STATE_HOME_HOTS;
		// ��ʼ״̬Ϊ�������Ȳ���
		updateHomeBlogs(URL_HOME_HOTS + mCurrentPage);
	}

	/**
	 * ˢ��ҳ��
	 */
	private void updateHomeBlogs(final String urlSpec) {
		Log.i(TAG, "��������:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Blog> result = fetchr.downloadBlogs(mBlogs,
						HtmlFetchr.DROP_UPDATE, urlSpec); // ������ҳ����
				if (result == null) {
					Toast.makeText(getActivity(), "���ӷ�����ʧ��", Toast.LENGTH_SHORT)
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
					// ���ؽ�����
					mProgressBar.setVisibility(View.INVISIBLE);
					mCustomFAB.setVisibility(View.VISIBLE);
					// ����ListView
					adapter = new MyAdapter(mBlogs);
					mHomeListView.setAdapter(adapter);
					mHomeListView.onRefreshComplete(); // ���½���
				}
			}
		}.execute();

	}

	/**
	 * ��������
	 */
	private void loadHomeBlogs(final String urlSpec) {

		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Blog> result = new ArrayList<>();
				result = fetchr.downloadBlogs(result, HtmlFetchr.UP_LOAD,
						urlSpec); // ���ز���ר����
				if (result == null) {
					Toast.makeText(getActivity(), "���ӷ�����ʧ��", Toast.LENGTH_SHORT)
							.show();
				} else {
					mBlogs.addAll(result);
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				
				// ����ListView
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
				if (mCurrentPage == mPages) { // ����������һҳ
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
	 * ��ʼ��������
	 */
	@SuppressLint("InflateParams")
	private void initPop() {
		View layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
				R.layout.listview_neworhot, null);
		mNorHListView = (ListView) layout.findViewById(R.id.NOH_listView);
		mPop = new PopupWindow(layout);
		mPop.setFocusable(true); // popupwindow�е�ListView�ſ��Խ��յ���¼�

		ArrayList<String> items = new ArrayList<>();
		items.add("��������");
		items.add("��������");
		ArrayAdapter<String> nohAdapter = new ArrayAdapter<>(getActivity(),
				R.layout.item_neworhot_listview, items);
		mNorHListView.setAdapter(nohAdapter);
		mNorHListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) { // ��������
					Toast.makeText(getActivity(), "��������", Toast.LENGTH_SHORT)
							.show();
					mPop.dismiss();
					mCurrentPage = 1;
					mState = STATE_HOME_NEWS;
					updateHomeBlogs(URL_HOME_NEWS + mCurrentPage);
				} else { // ��������
					Toast.makeText(getActivity(), "��������", Toast.LENGTH_SHORT)
							.show();
					mPop.dismiss();
					mCurrentPage = 1;
					mState = STATE_HOME_HOTS;
					updateHomeBlogs(URL_HOME_HOTS + mCurrentPage);
				}
			}

		});

		// ����popupwindow�Ŀ�Ⱥ͸߶�����Ӧ
		mNorHListView.measure(View.MeasureSpec.UNSPECIFIED,
				View.MeasureSpec.UNSPECIFIED);
		// ����ListView�ĳߴ�
		// ������View.MeasureSpec.UNSPECIFIED ��ʾ�����ؼ����޶��ӿؼ��Ĵ�С

		mPop.setWidth(mNorHListView.getMeasuredWidth());
		mPop.setHeight(mNorHListView.getMeasuredHeight()
				* NUM_OF_VISIBLE_LIST_ROWS);

		mPop.setOnDismissListener(this);
		// ע��Ҫ�������룬�������������������Ż��ô�����ʧ
		mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));
		mPop.setOutsideTouchable(true);
		// ����popupwindow�ⲿ��popupwindow��ʧ�����Ҫ�����popupwindowҪ�б���ͼƬ�ſ��Գɹ�������
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

			// ��ʾר��ͷ��
			String imageUrl = getItem(position).getBloggerIcon();
			holder.bloggerIcon.setTag(imageUrl);
			// ��imageView����һ����ǩ�����ڴ�ȡ��Cache�ͷ�ֹͼƬ��λ
			Bitmap bitmap = null;
			GifDrawable gifDrawable = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// ����ھ�̬ͼ�����д���
				holder.bloggerIcon.setImageBitmap(bitmap);
			} else if ((gifDrawable = MainActivity.mThumbnailDownloader
					.getGifCacheImage(imageUrl)) != null) {
				// ����ڶ�̬ͼ�����д���
				holder.bloggerIcon.setImageDrawable(gifDrawable);
			} else {
				// ����Ĭ��ͷ�����������ǰʹ�ô�ͷ��ռλ
				holder.bloggerIcon.setImageResource(R.drawable.ic_default);

				// ��������ͼƬ��Ϣ
				MainActivity.mThumbnailDownloader.queueThumbnail(
						holder.bloggerIcon, imageUrl);
			}

			// ��ʾ����
			holder.authorText.setText(getItem(position).getBlogAuthor());
			// ��ʾ����
			holder.titleText.setText(getItem(position).getBlogTitle());
			// ��ʾ����˵��
			holder.contentText.setText(getItem(position).getBlogContent());
			// ��ʾ���
			holder.typeText.setText(getItem(position).getBlogType());
			// ��ʾ�Ķ�����
			holder.readnumText.setText(getItem(position).getReadNum());
			// ��ʾ����ʱ��
			holder.agoText.setText(getItem(position).getAgo());
			// ��ʾ���۴���
			holder.commentnumText.setText(getItem(position).getCommentNumb());
			return convertView;
		}
	}

	private static class ViewHolder {
		public TextView typeText; // �������
		public TextView titleText; // ���±���
		public GifImageView bloggerIcon;// ����ͷ��
		public TextView contentText; // ���¼��
		public TextView authorText; // ��������
		public TextView agoText; // ���֮ǰ
		public TextView readnumText; // �Ķ�����
		public TextView commentnumText; // ���۴���
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), BlogPagerActivity.class);
		// ����ǰ����index�Ͳ��������ݹ�ȥ
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
	 * ��dpת����px
	 */
	private int dip2px(float dpValue) {
		float scale = getActivity().getResources().getDisplayMetrics().density; // ���ص��ܶ�
		return (int) (dpValue * scale + 0.5f);
	}

	@Override
	public void onDismiss() {
		mCustomFAB.startAnimation(animation);
	}

}
