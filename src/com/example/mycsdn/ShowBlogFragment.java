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
	private String mBlogUrl; // 博客链接
	private ArrayList<ArticleElement> mElements; // 博客页面内元素
	private ListView mListView; // 承载页面元素的容器
	private ProgressBar mProgressBar;
	private ImageView mBackImageView; // back键
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
	 * 异步线程获取博客页面
	 */
	private void ShowBlog(final String urlSpec) {
		if (!NetworkState.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), "网络异常，请检查设置", Toast.LENGTH_LONG)
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
		ShowBlog(mBlogUrl); // 获取博客页面
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
				// 显示图片
				final String imageUrl = element.getImageLink();
				imageView.setTag(imageUrl);
				// 给imageView设置一个标签，用于存取于Cache和防止图片错位
				Bitmap bitmap = null;
				if ((bitmap = MainActivity.mThumbnailDownloader
						.getCacheImage(imageUrl)) != null) {
					// 如果在缓存中存在
					imageView.setImageBitmap(bitmap);
				} else {
					// 设置默认头像，在下载完毕前使用此头像占位
					imageView.setImageResource(R.drawable.ic_default);

					// 发送下载图片消息
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
				textView.setHorizontallyScrolling(true); // 不让超出屏幕的字体自动换行，使用滚动条
				textView.setFocusable(true);
				Log.v(TAG, "代码-------------->" + element.getCode());
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
		case R.id.back_showblog: // 回到BlogPagerActivity
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
