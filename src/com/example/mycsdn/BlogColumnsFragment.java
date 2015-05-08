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
 * ��ʾ����ר��ҳ��
 */
public class BlogColumnsFragment extends Fragment implements
		OnItemClickListener {
	private static final String URL_COLUMNS = "http://blog.csdn.net/all/column/list.html?&page=";
	private static final String TAG = "BlogColumnsFragment";
	public static final String EXTRA_COLUMN_DETAIL = "column_detail"; // KEY to
																		// ��ColumnDetailActivity����Column
	// ָ������ר��������
	private MyListView mColumnsListView; // ��ʾ����ר�����Զ������������MyListView
	private ProgressBar mProgressBar; // ������
	private ArrayList<Column> mColumns; // ����ר��
	private HtmlFetchr fetchr; // ���ز�����htmlҳ��Ķ���
	private MyAdapter adapter; // ListView��������
	private Integer mPages;// ��ҳ��
	private int mCurrentPage; // ��ǰҳ��

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "BlogColumnsFragment onCreate");
		fetchr = new HtmlFetchr();
		// ��ȡ����ר���б�
		mColumns = new ArrayList<Column>();
		mCurrentPage = 1;
		updateColumns(URL_COLUMNS + mCurrentPage);
	}

	/**
	 * ˢ��ҳ��
	 */
	private void updateColumns(final String urlSpec) {
		Log.i(TAG, "��������:" + urlSpec);
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Column> result = fetchr.downloadColumns(mColumns,
						HtmlFetchr.DROP_UPDATE, urlSpec); // ���ز���ר���б�
				if (result == null) { // ����ʧ��
					Toast.makeText(getActivity(), "���ӷ�����ʧ��", Toast.LENGTH_SHORT)
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
					// ����ListView
					adapter = new MyAdapter(mColumns);
					mColumnsListView.setAdapter(adapter);
					mColumnsListView.onRefreshComplete(); // ���½���
				}
			}
		}.execute();

	}

	/**
	 * ��������
	 */
	private void loadColumns(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Column> result = new ArrayList<>();
				result = fetchr.downloadColumns(result, HtmlFetchr.UP_LOAD,
						urlSpec); // ���ز���ר���б�
				if (result == null) { // ����ʧ��
					Toast.makeText(getActivity(), "���ӷ�����ʧ��", Toast.LENGTH_SHORT)
							.show();
				} else {
					mColumns.addAll(result);
					mPages = fetchr.downloadPages();
				}
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				// ����ListView
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
				if (mCurrentPage == mPages) { // ����������һҳ
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
			// ��ʾר��ͷ��
			String imageUrl = getItem(position).getImageUrl();
			holder.columnImageView.setTag(imageUrl);
			// ��imageView����һ����ǩ�����ڴ�ȡ��Cache�ͷ�ֹͼƬ��λ
			Bitmap bitmap = null;
			GifDrawable gifDrawable = null;
			if ((bitmap = MainActivity.mThumbnailDownloader
					.getCacheImage(imageUrl)) != null) {
				// ����ھ�̬ͼ�����д���
				holder.columnImageView.setImageBitmap(bitmap);
			} else if ((gifDrawable = MainActivity.mThumbnailDownloader
					.getGifCacheImage(imageUrl)) != null) {
				// ����ڶ�̬ͼ�����д���
				holder.columnImageView.setImageDrawable(gifDrawable);
			} else {
				// ����Ĭ��ͷ�����������ǰʹ�ô�ͷ��ռλ
				holder.columnImageView.setImageResource(R.drawable.ic_default);

				// ��������ͼƬ��Ϣ
				MainActivity.mThumbnailDownloader.queueThumbnail(
						holder.columnImageView, imageUrl);
			}

			// ��ʾ����id
			holder.ownerText.setText(getItem(position).getColumnOwner());
			// ��ʾ����
			holder.titleText.setText(getItem(position).getColumnTitle());
			// ��ʾ����˵��
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
		// ����ǰѡ�е�Column���󴫵ݵ� ColumnDetailActivity
		bundle.putSerializable(EXTRA_COLUMN_DETAIL, mColumns.get(position - 1));
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
