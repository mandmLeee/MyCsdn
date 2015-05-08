package com.example.mycsdn;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.MyListView;
import com.custom.MyListView.OnLoadListener;
import com.custom.MyListView.OnRefreshListener;
import com.model.Column;
import com.utils.HtmlFetchr;
import com.utils.NetworkState;

/**
 * 显示所有专栏页面
 */
public class BlogColumnsFragment extends Fragment implements
		OnItemClickListener {
	private static final String URL_COLUMNS = "http://blog.csdn.net/all/column/list.html?&page=";
	private static final String TAG = "BlogColumnsFragment";
	public static final String EXTRA_COLUMN_DETAIL = "column_detail"; // KEY to
																		// 向ColumnDetailActivity传递Column
	// 指向所有专栏的链接
	private MyListView mColumnsListView; // 显示所有专栏的自定义带下拉栏的MyListView
	private ProgressBar mProgressBar; // 进度条
	private ArrayList<Column> mColumns; // 所有专栏
	private HtmlFetchr fetchr; // 下载并解析html页面的对象
	private MyAdapter adapter; // ListView的设配器
	private Integer mPages;// 总页数
	private int mCurrentPage; // 当前页数

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "BlogColumnsFragment onCreate");
		fetchr = new HtmlFetchr();
		// 获取所有专栏列表
		mColumns = new ArrayList<Column>();
		mCurrentPage = 1;
		updateColumns(URL_COLUMNS + mCurrentPage);
	}

	/**
	 * 刷新页面
	 */
	private void updateColumns(final String urlSpec) {
		Log.i(TAG, "访问链接:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
					.show();
			return;
		}

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Column> result = fetchr.downloadColumns(mColumns,
						HtmlFetchr.DROP_UPDATE, urlSpec); // 下载博客专栏列表
				if (result == null) { // 加载失败
					Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT)
							.show();
				} else {
					mColumns = result;
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				Log.i(TAG, "onPostExecute");
				if (getActivity() != null) {
					mProgressBar.setVisibility(View.INVISIBLE);
					// 更新ListView
					adapter = new MyAdapter(mColumns);
					mColumnsListView.setAdapter(adapter);
					mColumnsListView.onRefreshComplete(); // 更新结束
				}
			}
		}.execute();

	}

	/**
	 * 加载数据
	 */
	private void loadColumns(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Column> result = new ArrayList<>();
				result = fetchr.downloadColumns(result, HtmlFetchr.UP_LOAD,
						urlSpec); // 下载博客专栏列表
				if (result == null) { // 加载失败
					Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT)
							.show();
				} else {
					mColumns.addAll(result);
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				// 更新ListView
				adapter.notifyDataSetChanged();
				mColumnsListView.onLoadComplete();
			}
		}.execute();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_blogcolumns, null);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.ProgressBar_blogcolumns);
		mProgressBar.setVisibility(View.VISIBLE);
		mColumnsListView = (MyListView) view
				.findViewById(R.id.listview_blogcolumns);
		mColumnsListView.setOnItemClickListener(this);
		mColumnsListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				updateColumns(URL_COLUMNS + "1");
			}
		});

		mColumnsListView.setonLoadListener(new OnLoadListener() {

			@Override
			public void onLoad() {
				if (mCurrentPage == mPages) { // 如果到了最后一页
					mColumnsListView.onLoadNoData();
				} else {
					++mCurrentPage;
					loadColumns(URL_COLUMNS + "" + mCurrentPage);
				}

			}
		});

		return view;
	}

	private class MyAdapter extends ArrayAdapter<Column> {

		public MyAdapter(ArrayList<Column> columns) {
			super(getActivity(), 0, columns);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Log.i(TAG, "getView position:" + position);
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_bcs_listview, null);
				holder = new ViewHolder();
				holder.columnImageView = (GifImageView) convertView
						.findViewById(R.id.column_imageView);
				holder.ownerText = (TextView) convertView
						.findViewById(R.id.column_owner);
				holder.titleText = (TextView) convertView
						.findViewById(R.id.column_title);
				holder.contentText = (TextView) convertView
						.findViewById(R.id.column_content);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 显示专栏头像
			String imageUrl = getItem(position).getImageUrl();
			holder.columnImageView.setTag(imageUrl);
			// 给imageView设置一个标签，用于存取于Cache和防止图片错位
			Bitmap bitmap = null;
			GifDrawable gifDrawable = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// 如果在静态图缓存中存在
				holder.columnImageView.setImageBitmap(bitmap);
			} else if ((gifDrawable = MainActivity.mThumbnailDownloader
					.getGifCacheImage(imageUrl)) != null) {
				// 如果在动态图缓存中存在
				holder.columnImageView.setImageDrawable(gifDrawable);
			} else {
				// 设置默认头像，在下载完毕前使用此头像占位
				holder.columnImageView.setImageResource(R.drawable.ic_default);

				// 发送下载图片消息
				MainActivity.mThumbnailDownloader.queueThumbnail(
						holder.columnImageView, imageUrl);
			}

			// 显示作者id
			holder.ownerText.setText(getItem(position).getColumnOwner());
			// 显示标题
			holder.titleText.setText(getItem(position).getColumnTitle());
			// 显示内容说明
			holder.contentText.setText(getItem(position).getColumnContent());

			return convertView;
		}
	}

	private static class ViewHolder {
		public GifImageView columnImageView;
		public TextView ownerText;
		public TextView titleText;
		public TextView contentText;

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ColumnDetailActivity.class);
		Bundle bundle = new Bundle();
		// 将当前选中的Column对象传递到 ColumnDetailActivity
		bundle.putSerializable(EXTRA_COLUMN_DETAIL, mColumns.get(position - 1));
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
