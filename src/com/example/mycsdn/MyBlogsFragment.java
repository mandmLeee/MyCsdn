package com.example.mycsdn;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class MyBlogsFragment extends Fragment implements OnItemClickListener,
		OnClickListener {
	private static final String URL_MY_BLOGS = "http://blog.csdn.net/";
	private static final String TAG = "MyBlogsFragment";
	private String mIdString; // ����ID

	private MyListView mMyBlogsListView; // ��ʾ�����еĲ���
	private ProgressBar mProgressBar;
	private EditText mIdEditText;
	private Button mGoButton;
	private ImageView mDeleteImageView;
	private ArrayList<Blog> mMyBlogs; // ���в���
	private HtmlFetchr fetchr; // ���ز�����htmlҳ��Ķ���
	private MyAdapter adapter; // ListView��������
	private Integer mPages;// ��ҳ��
	private int mCurrentPage; // ��ǰҳ��

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fetchr = new HtmlFetchr();
		mMyBlogs = new ArrayList<Blog>();
		mCurrentPage = 1;
	}

	/**
	 * ˢ��ҳ��
	 */
	private void updateMyBlogs(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		Log.i(TAG, "��������:" + urlSpec);
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mMyBlogs = fetchr.downloadMyBlogs(mMyBlogs,
						HtmlFetchr.DROP_UPDATE, urlSpec); // ���������еĲ���
				mPages = fetchr.downloadMyBlogPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				Log.i(TAG, "onPostExecute");
				if (getActivity() != null) {
					mProgressBar.setVisibility(View.INVISIBLE);
					// ����ListView
					adapter = new MyAdapter(mMyBlogs);
					mMyBlogsListView.setAdapter(adapter);
					mMyBlogsListView.onRefreshComplete(); // ���½���
				}
			}
		}.execute();

	}

	/**
	 * ��������
	 */
	private void loadMyBlogs(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ArrayList<Blog> result = new ArrayList<>();
				result = fetchr.downloadMyBlogs(result, HtmlFetchr.UP_LOAD,
						urlSpec); // ���ز���ר���б�
				mMyBlogs.addAll(result);
				mPages = fetchr.downloadMyBlogPages();
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				// ����ListView
				adapter.notifyDataSetChanged();
				mMyBlogsListView.onLoadComplete();
			}
		}.execute();

	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.fragment_myblogs, null);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.ProgressBar_myBlogs);
		mProgressBar.setVisibility(View.INVISIBLE);
		mIdEditText = (EditText) view.findViewById(R.id.myBlogs_ID_EditText);
		// ��ȡ����õ�ID
		SharedPreferences spId = getActivity().getSharedPreferences("spId", 0);
		String id = spId.getString("id", "");
		if (id.equals(""))
			id = "u012964281";
		mIdEditText.setText(id);
		mIdEditText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					mDeleteImageView.setVisibility(View.GONE);
				} else {
					mDeleteImageView.setVisibility(View.VISIBLE);
				}
			}
		});

		mGoButton = (Button) view.findViewById(R.id.myBlogs_GO_Button);
		mGoButton.setOnClickListener(this);

		mDeleteImageView = (ImageView) view
				.findViewById(R.id.myBlogs_Delete_ImagerView);
		mDeleteImageView.setOnClickListener(this);

		mMyBlogsListView = (MyListView) view
				.findViewById(R.id.myBlogs_myListView);
		mMyBlogsListView.setOnItemClickListener(this);
		mMyBlogsListView.setonRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				mCurrentPage = 1;
				String urlSpec = URL_MY_BLOGS + mIdString + "/article/list/"
						+ mCurrentPage;
				updateMyBlogs(urlSpec);
			}
		});

		mMyBlogsListView.setonLoadListener(new OnLoadListener() {

			@Override
			public void onLoad() {
				if (mCurrentPage == mPages) { // ����������һҳ
					mMyBlogsListView.onLoadNoData();
				} else {
					++mCurrentPage;
					String urlSpec = URL_MY_BLOGS + mIdString
							+ "/article/list/" + mCurrentPage;
					loadMyBlogs(urlSpec);
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
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_myblogs_listview, null);
				holder = new ViewHolder();
				holder.titleText = (TextView) convertView
						.findViewById(R.id.my_blog_title);
				holder.contentText = (TextView) convertView
						.findViewById(R.id.my_blog_content);
				holder.agoText = (TextView) convertView
						.findViewById(R.id.my_blog_ago);
				holder.commentnumbText = (TextView) convertView
						.findViewById(R.id.my_blog_commentNum);
				holder.readnumbText = (TextView) convertView
						.findViewById(R.id.my_blog_readNum);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// ��ʾ����
			holder.titleText.setText(getItem(position).getBlogTitle());
			// ��ʾ����˵��
			holder.contentText.setText(getItem(position).getBlogContent());
			holder.agoText.setText(getItem(position).getAgo());
			holder.commentnumbText.setText(getItem(position).getCommentNumb());
			holder.readnumbText.setText(getItem(position).getReadNum());

			return convertView;
		}
	}

	private static class ViewHolder {
		public TextView titleText;
		public TextView contentText;
		public TextView readnumbText;
		public TextView commentnumbText;
		public TextView agoText;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), BlogPagerActivity.class);
		// ����ǰ����index�Ͳ��������ݹ�ȥ
		intent.putExtra(BlogPagerActivity.EXTRA_CURRENT_BLOG_INDEX,
				position - 1);
		Bundle bundle = new Bundle();
		bundle.putSerializable(BlogPagerActivity.EXTRA_BLOGS, mMyBlogs);
		intent.putExtras(bundle);
		startActivity(intent);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.myBlogs_GO_Button:
			// ����б�
			mMyBlogs.clear();
			adapter = new MyAdapter(mMyBlogs);
			mMyBlogsListView.setAdapter(adapter);
			// ��ʾ������
			mProgressBar.setVisibility(View.VISIBLE);
			mCurrentPage = 1;
			mIdString = mIdEditText.getText().toString();
			String urlSpec = URL_MY_BLOGS + mIdString + "/article/list/"
					+ mCurrentPage;
			updateMyBlogs(urlSpec);
			// ����ñ�������ID
			SharedPreferences spId = getActivity().getSharedPreferences("spId",
					0);
			Editor editor = spId.edit();
			editor.putString("id", mIdString);
			editor.commit();
			break;
		case R.id.myBlogs_Delete_ImagerView:
			mIdEditText.setText("");
			break;
		default:
			break;
		}

	}
}
