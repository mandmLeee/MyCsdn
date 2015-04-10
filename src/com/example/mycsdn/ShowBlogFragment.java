package com.example.mycsdn;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.model.ArticleElement;
import com.utils.HtmlFetchr;
import com.utils.NetworkState;

@SuppressLint("ViewHolder")
public class ShowBlogFragment extends Fragment implements OnClickListener {
	public static final String EXTRA_BLOG_URL = "blog_url";
	protected static final String TAG = "ShowBlogFragment";
	private String mBlogUrl; // ��������
	private ArrayList<ArticleElement> mElements; // ����ҳ����Ԫ��
	private ListView mListView; // ����ҳ��Ԫ�ص�����
	private ProgressBar mProgressBar;
	private ImageView mBackImageView; // back��
	private MyAdapter mMyAdapter;
	private HtmlFetchr fetchr;

	public static ShowBlogFragment newInstance(String blogUrl) {

		Bundle args = new Bundle();
		args.putString(EXTRA_BLOG_URL, blogUrl);
		ShowBlogFragment fragment = new ShowBlogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * �첽�̻߳�ȡ����ҳ��
	 */
	private void ShowBlog(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "�����쳣����������", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mElements = fetchr.downloadBlogPageElements(urlSpec);
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				mProgressBar.setVisibility(View.INVISIBLE);
				mMyAdapter = new MyAdapter(getActivity(), mElements);
				for (ArticleElement element : mElements) {
					Log.v(TAG, "element style:" + element.getStyle());
				}
				mListView.setAdapter(mMyAdapter);
			}
		}.execute();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fetchr = new HtmlFetchr();
		mBlogUrl = (String) getArguments().getSerializable(EXTRA_BLOG_URL);
		mElements = new ArrayList<ArticleElement>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_showblog, null);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.ProgressBar_showBlog);
		mProgressBar.setVisibility(View.VISIBLE);
		mListView = (ListView) view.findViewById(R.id.listView_showBlog);
		mBackImageView = (ImageView) view.findViewById(R.id.back_showblog);
		mBackImageView.setOnClickListener(this);
		ShowBlog(mBlogUrl); // ��ȡ����ҳ��
		return view;
	}

	@SuppressLint("ViewHolder")
	private class MyAdapter extends ArrayAdapter<ArticleElement> {

		public MyAdapter(Context context, ArrayList<ArticleElement> elements) {
			super(context, 0, elements);
		}

		@Override
		public boolean isEnabled(int position) {
			if (mElements.get(position).getStyle() == ArticleElement.CODE)
				return true;
			return false;
		}

		@SuppressLint({ "ViewHolder", "InflateParams" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView;
			ImageView imageView;
			ArticleElement element = mElements.get(position);
			switch (element.getStyle()) {
			case ArticleElement.TITLE:
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.article_content_title, null);
				textView = (TextView) convertView
						.findViewById(R.id.article_content_title_TextView);
				textView.setText(element.getTitle());
				break;

			case ArticleElement.CONTENT:
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.article_content_item, null);
				textView = (TextView) convertView
						.findViewById(R.id.article_content_item_TextView);
				textView.setText(Html.fromHtml(element.getContent()));
				break;

			case ArticleElement.IMAGE:
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.article_content_image, null);
				imageView = (ImageView) convertView
						.findViewById(R.id.article_content_ImageView);
				// ��ʾͼƬ
				final String imageUrl = element.getImageLink();
				imageView.setTag(imageUrl);
				// ��imageView����һ����ǩ�����ڴ�ȡ��Cache�ͷ�ֹͼƬ��λ
				Bitmap bitmap = null;
				if ((bitmap = MainActivity.mThumbnailDownloader
						.getCacheImage(imageUrl)) != null) {
					// ����ڻ����д���
					imageView.setImageBitmap(bitmap);
				} else {
					// ����Ĭ��ͷ�����������ǰʹ�ô�ͷ��ռλ
					imageView.setImageResource(R.drawable.ic_default);

					// ��������ͼƬ��Ϣ
					MainActivity.mThumbnailDownloader.queueThumbnail(imageView,
							imageUrl);
				}

				break;

			case ArticleElement.CODE:
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.article_content_code, null);
				textView = (TextView) convertView
						.findViewById(R.id.article_content_code_TextView);
				textView.setText(element.getCode());
				textView.setMovementMethod(ScrollingMovementMethod
						.getInstance());
				textView.setHorizontallyScrolling(true); // ���ó�����Ļ�������Զ����У�ʹ�ù�����
				textView.setFocusable(true);
				Log.v(TAG, "����-------------->" + element.getCode());
				break;
			default:
				break;

			}

			return convertView;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_showblog: // �ص�BlogPagerActivity
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
			} catch (IOException e) {
			}
			break;

		default:
			break;
		}

	}

}
