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
 * ��ʾ����ר��ҳ��
 */
public class HotBlogsFragment extends Fragment implements OnItemClickListener {
	private static final String URL_HOTBLOGS = "http://blog.csdn.net/hot.html?&page=";
	private static final String TAG = "HotBlogsFragment";

	private MyListView mHotBlogsListView; // ��ʾ�������ĵ��Զ������������MyListView
	private ProgressBar mProgressBar;
	private ArrayList<Blog> mBlogs; // ���в���
	private HtmlFetchr fetchr; // ���ز�����htmlҳ��Ķ���
	private MyAdapter adapter; // ListView��������
	private Integer mPages;// ��ҳ��
	private int mCurrentPage; // ��ǰҳ��

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "HotBlogFragment onCreate");
		fetchr = new HtmlFetchr();
		// ��ȡ����ר���б�
		mBlogs = new ArrayList<Blog>();
		mCurrentPage = 1;
		updateHotBlogs(URL_HOTBLOGS + mCurrentPage);
	}

	/**
	 * ˢ��ҳ��
	 */
	private void updateHotBlogs(final String urlSpec) {
		Log.i(TAG, "��������:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mBlogs = fetchr.downloadBlogs(mBlogs, HtmlFetchr.DROP_UPDATE,
						urlSpec); // ���ز���ר���б�
				mPages = fetchr.downloadPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				mProgressBar.setVisibility(View.INVISIBLE);
				// ����ListView
				adapter = new MyAdapter(mBlogs);
				mHotBlogsListView.setAdapter(adapter);
				mHotBlogsListView.onRefreshComplete(); // ���½���
			}
		}.execute();

	}

	/**
	 * ��������
	 */
	private void loadHotBlogs(final String urlSpec) {
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
				mBlogs.addAll(result);
				mPages = fetchr.downloadPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				// ����ListView
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
				if (mCurrentPage == mPages) { // ����������һҳ
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

			// ��ʾר��ͷ��
			String imageUrl = getItem(position).getBloggerIcon();
			holder.bloggerIcon.setTag(imageUrl);
			// ��imageView����һ����ǩ�����ڴ�ȡ��Cache�ͷ�ֹͼƬ��λ
			Bitmap bitmap = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// Log.i(TAG, "get image from cache!");
				// ����ڻ����д���
				holder.bloggerIcon.setImageBitmap(bitmap);
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
		public ImageView bloggerIcon;// ����ͷ��
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

}
