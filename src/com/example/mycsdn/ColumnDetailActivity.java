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
	private ArrayList<Blog> mBlogs; // ר�������в���
	private ProgressBar mProgressBar;
	private TextView mColumnContentText; // ר��˵��
	private TextView mColumnReadNumbText; // �������
	private TextView mColumnBlogNumbText; // ��������
	private TextView mColumnBuildDateText; // ����ʱ��
	private ImageView mColumnImageView; // ר��ͷ��
	private ListView mBlogListView; // �����б�
	private View mHeaderView; // listView��ͷ������
	private HtmlFetchr fetchr; // ���ز�����htmlҳ��Ķ���

	@SuppressLint({ "InflateParams", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activtiy_columndetail);
		getActionBar().setDisplayHomeAsUpEnabled(true); // ���˵�����
		mHeaderView = getLayoutInflater().inflate(R.layout.columndetail_header,
				null);
		fetchr = new HtmlFetchr();
		Bundle bundle = getIntent().getExtras();
		// ��ȡBlogColumnsFragment���ݹ�����Column����
		mColumn = (Column) bundle
				.getSerializable(BlogColumnsFragment.EXTRA_COLUMN_DETAIL);
		getActionBar().setTitle(mColumn.getColumnTitle());

		// ��ʼ���ؼ�
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
		mBlogListView.addHeaderView(mHeaderView, null, false); // ��listview���ͷ������
		mBlogListView.setOnItemClickListener(this);
		mColumnContentText.setText(mColumn.getColumnContent());
		// --------------------------------------------------
		String imageUrl = mColumn.getImageUrl();
		mColumnImageView.setTag(imageUrl);
		Bitmap bitmap = null;
		if ((bitmap = MainActivity.mThumbnailDownloader.getCacheImage(imageUrl)) != null) {
			// ����ڻ����д���
			mColumnImageView.setImageBitmap(bitmap);
			Log.i(TAG, "image from Cache");
		} else {
			// ��������ͼƬ��Ϣ
			Log.i(TAG, "imageUrl:" + imageUrl);
			// ���convertView���������ͼƬ��λ
			MainActivity.mThumbnailDownloader.queueThumbnail(mColumnImageView,
					imageUrl);
		}
		initUI(); // ��ʼ����Ҫ���ص�UI

	}

	/**
	 * ��ʼ������
	 */
	private void initUI() {
		if (!NetworkState.isNetworkConnected(this)) {
			Toast.makeText(this, "�����쳣����������", Toast.LENGTH_LONG).show();
			return;
		}
		// ����
		new AsyncTask<Void, Void, Bundle>() {

			@Override
			protected Bundle doInBackground(Void... params) {
				// ����html for columnInfo �����������������������ʱ��
				Bundle result = fetchr.downloadColumnInfo(CSDN
						+ mColumn.getColumnUrl());
				mBlogs = fetchr.downloadColumnBlogs(CSDN
						+ mColumn.getColumnUrl());
				return result;
			};

			@Override
			protected void onPostExecute(Bundle result) {
				mProgressBar.setVisibility(View.INVISIBLE);
				// ���½���
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
			ViewHolder holder = null; // ʹ��ViewHolder����convertView�������ʵϰListView�Ż�
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

			// ����Type
			if (getItem(position).getBlogType() != null)
				holder.typeText.setText(getItem(position).getBlogType());
			// ����Title
			holder.titleText.setText(getItem(position).getBlogTitle());
			// ����Content
			holder.contentText.setText(getItem(position).getBlogContent());
			// ����Author
			holder.authorText.setText(mColumn.getColumnOwner());
			// ��ȥʱ��
			holder.agoText.setText(getItem(position).getAgo());
			// �Ķ�����
			holder.readNumText.setText(getItem(position).getReadNum());
			// ���۴���
			holder.commentNumText.setText(getItem(position).getCommentNumb());
			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView typeText;// ����Type
		public TextView titleText;// ����Title
		public TextView contentText;
		public TextView authorText;
		public TextView agoText;
		public TextView readNumText;
		public TextView commentNumText;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: // ���˵�����
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
		// ����ǰ����index�Ͳ��������ݹ�ȥ
		intent.putExtra(BlogPagerActivity.EXTRA_CURRENT_BLOG_INDEX,
				position - 1);
		Bundle bundle = new Bundle();
		bundle.putSerializable(BlogPagerActivity.EXTRA_BLOGS, mBlogs);
		intent.putExtras(bundle);
		startActivity(intent);
	}

}
