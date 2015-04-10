package com.custom;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mycsdn.R;

public class MyListView extends ListView implements OnScrollListener {

	private final static int RELEASE_To_REFRESH = 0;// ����ˢ��
	private final static int PULL_To_REFRESH = 1;// ����ˢ��
	private final static int REFRESHING = 2;// ����ˢ��
	private final static int DONE = 3; // �ص�δ����״̬
	private final static int LOADING = 4;// ����
	// ʵ�ʵ�padding�ľ����������ƫ�Ƶľ���ı���
	private final static int RATIO = 3;
	private static final String TAG = "MyListView";

	private LinearLayout mHeadView;// �����ListView�Ϸ���ͷ����
	private TextView mTipsTextview;
	private TextView mLastUpdatedTextView;
	private ImageView mArrowImageView;
	private ProgressBar mHeaderProgressBar;

	private LinearLayout mFooterView; // β������
	private TextView mShowText; // ��ʾ״̬�ַ���
	private ProgressBar mFooterProgressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	private boolean isRecored; // ���ڱ�֤startY��ֵ��һ��������touch�¼���ֻ����¼һ��
	private int headContentHeight;
	private int startY;
	private int mFirstItemIndex;// �б���ʱ�������Ļ�ڿɼ��ĵ�һ��Item���
	private int mState;
	private boolean isBack; // ��ͷ�Ƿ�Ҫ����
	private OnRefreshListener refreshListener;
	private boolean isRefreshable;
	private OnLoadListener loadListener;
	private boolean mLoadEnable;
	private boolean isLoading; // �ж��Ƿ����ڼ���
	private LayoutInflater inflater;

	public MyListView(Context context) {
		super(context);
		init(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * ��ʼ��View
	 * 
	 * @param context
	 */
	@SuppressLint("InflateParams")
	private void init(Context context) {

		inflater = LayoutInflater.from(context);
		// -----��ʼ��ͷ�����������-------------------------
		mHeadView = (LinearLayout) inflater.inflate(R.layout.mylistview_header,
				null);
		mArrowImageView = (ImageView) mHeadView
				.findViewById(R.id.head_arrowImageView);
		mArrowImageView.setMinimumWidth(70);
		mArrowImageView.setMinimumHeight(50);
		mHeaderProgressBar = (ProgressBar) mHeadView
				.findViewById(R.id.head_progressBar);
		mTipsTextview = (TextView) mHeadView
				.findViewById(R.id.head_tipsTextView);
		mLastUpdatedTextView = (TextView) mHeadView
				.findViewById(R.id.head_lastUpdatedTextView);
		// -----------------------------------------------
		measureView(mHeadView);
		setOnScrollListener(this); // ��ListView���ù���������

		headContentHeight = mHeadView.getMeasuredHeight();
		mHeadView.setPadding(0, -1 * headContentHeight, 0, 0);
		// ˢ��view
		mHeadView.invalidate();
		// ���ͷ�ļ����� selected
		addHeaderView(mHeadView, null, false);
		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250); // ����ʱ��
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator()); // ���ü�������
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		// β����ʼ��
		mFooterView = (LinearLayout) inflater.inflate(
				R.layout.mylistview_footer, null);
		mShowText = (TextView) mFooterView.findViewById(R.id.showText_footer);
		mFooterProgressBar = (ProgressBar) mFooterView
				.findViewById(R.id.ProgressBar_footer);
		addFooterView(mFooterView); // ���β��

		mState = DONE;
		isRefreshable = false;
		mLoadEnable = false;
	}

	/**
	 * ����mHeaderView�ĸߺͿ�
	 */
	@SuppressWarnings("deprecation")
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		// spec �����ڴ��ݸ��Ӵ��ڵĴ�С��ģʽ
		// padding �����ڵı߾࣬�൱��android:padding
		// childDimension �Ӵ�����Ҫ���Ƶ�׼ȷ��С�����ղ�һ���������ֵ

		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			// MeasureSpec.UNSPECIFIED ֵ=0
			// δָ���ߴ�����������࣬һ�㶼�Ǹ��ؼ���AdapterView��ͨ��measure���������ģʽ
			// MeasureSpec.EXACTLY,��ȷ�ߴ�
			// MeasureSpec.AT_MOST���ߴ�
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// Log.i(TAG, "DOWN");
				if (mFirstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					// Log.v("this is", "��downʱ���¼��ǰλ��");
				}
				break;
			case MotionEvent.ACTION_UP:
				// Log.i(TAG, "up");
				if (mState != REFRESHING && mState != LOADING) {
					if (mState == DONE) {
					}
					if (mState == PULL_To_REFRESH) {
						mState = DONE;
						changeHeaderViewByState();
						// Log.v("this is", "������ˢ��״̬����done״̬");
					}
					if (mState == RELEASE_To_REFRESH) {
						mState = REFRESHING;
						changeHeaderViewByState();
						onRefresh();
						// Log.v("this is", "���ɿ�ˢ��״̬����done״̬");
					}
				}
				isRecored = false;
				isBack = false;
				break;
			case MotionEvent.ACTION_MOVE:
				// Log.i(TAG, "MOVE");
				int tempY = (int) event.getY();
				if (!isRecored && mFirstItemIndex == 0) {
					isRecored = true;
					startY = tempY;
					// Log.v("this is", "��moveʱ���¼��λ��");
				}
				// ��֤������padding�Ĺ����У���ǰ��λ��һֱ����head������������б�����Ļ�Ļ����������Ƶ�ʱ���б��ͬʱ���й���
				if (mState != REFRESHING && isRecored && mState != LOADING) {

					// ��������ȥˢ����
					if (mState == RELEASE_To_REFRESH) {
						setSelection(0);
						// �������ˣ��Ƶ�����Ļ�㹻�ڸ�head�ĳ̶ȣ����ǻ�û���Ƶ�ȫ���ڸǵĵز�
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							mState = PULL_To_REFRESH;
							changeHeaderViewByState();
							// Log.v("this is", "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");

						}
						// һ�����Ƶ�����
						else if (tempY - startY <= 0) {
							mState = DONE;
							changeHeaderViewByState();
							// Log.v("this is", "���ɿ�ˢ��״̬ת�䵽done״̬");

						}
						// �������ˣ����߻�û�����Ƶ���Ļ�����ڸ�head�ĵز�
						else {
							// ���ý����ر�Ĳ�����ֻ�ø���paddingTop��ֵ������
						}
					}
					// ��û�е�����ʾ�ɿ�ˢ�µ�ʱ��,DONE������PULL_To_REFRESH״̬
					if (mState == PULL_To_REFRESH) {
						setSelection(0);
						// ���������Խ���RELEASE_TO_REFRESH��״̬
						if ((tempY - startY) / RATIO >= headContentHeight) {
							mState = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();
							// Log.v("this is", "��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");

						}
						// ���Ƶ�����
						else if (tempY - startY <= 0) {
							mState = DONE;
							changeHeaderViewByState();
							// Log.v("this is", "��done��������ˢ��״̬ת�䵽done״̬");

						}
					}
					// done״̬��

					if (mState == DONE) {
						if (tempY - startY > 0) {
							mState = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}
					// ����headView��size

					if (mState == PULL_To_REFRESH) {
						mHeadView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);
					}
					// ����headView��paddingTop

					if (mState == RELEASE_To_REFRESH) {
						mHeadView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}
				}
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * ����ͷ�����ֵ�״̬
	 */
	private void changeHeaderViewByState() {
		switch (mState) {
		case RELEASE_To_REFRESH:
			mArrowImageView.setVisibility(View.VISIBLE);
			mHeaderProgressBar.setVisibility(View.GONE);
			mTipsTextview.setVisibility(View.VISIBLE);
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.startAnimation(animation);
			mTipsTextview.setText("�ɿ�ˢ��");
			break;
		case PULL_To_REFRESH:
			mHeaderProgressBar.setVisibility(View.GONE);
			mTipsTextview.setVisibility(View.VISIBLE);
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.VISIBLE);
			// ����RELEASE_To_REFRESH״̬ת������

			if (isBack) {
				isBack = false;
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(reverseAnimation);
				mTipsTextview.setText("����ˢ��");
			} else {
				mTipsTextview.setText("����ˢ��");
			}
			break;

		case REFRESHING:
			mHeadView.setPadding(0, 0, 0, 0);
			mHeaderProgressBar.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
			mTipsTextview.setText("����ˢ�� ...");
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case DONE:
			mHeadView.setPadding(0, -1 * headContentHeight, 0, 0);
			mHeaderProgressBar.setVisibility(View.GONE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setImageResource(R.drawable.arrow);
			mTipsTextview.setText("ˢ�³ɹ�");
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		}

	}

	
	
	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	// �Զ����������½ӿ�
	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void setonLoadListener(OnLoadListener loadListener) {
		this.loadListener = loadListener;
		mLoadEnable = true;
	}

	// ���ؽӿ�
	public interface OnLoadListener {
		public void onLoad();
	}

	/**
	 * �������*/
	public void onLoadComplete() {
		isLoading = false;
		mShowText.setVisibility(View.VISIBLE);
		mShowText.setText("�������");
		mFooterProgressBar.setVisibility(View.INVISIBLE); // ��ʾ���������ɼ�
	}
	
	/**
	 * �����ݿɼ���*/
	public void onLoadNoData(){
		isLoading = false;
		mShowText.setVisibility(View.VISIBLE);
		mShowText.setText("��������");
		mFooterProgressBar.setVisibility(View.INVISIBLE); // ��ʾ���������ɼ�
	}

	// ע��ӿ�
	@SuppressWarnings("deprecation")
	public void onRefreshComplete() {
		mState = DONE;
		mLastUpdatedTextView.setText("�������: " + new Date().toLocaleString());
		Log.i(TAG,"onRefreshComplete");
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		ifNeedLoad(view, scrollState);
	}

	private void ifNeedLoad(AbsListView view, int scrollState) {
		if (!mLoadEnable) {
			return;
		}
		try {
			// OnScrollListener.SCROLL_STATE_IDLE��ʾֹͣ����
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& !isLoading
					&& view.getLastVisiblePosition() == view
							.getPositionForView(mFooterView)) {
				// ���ڼ���
				isLoading = true;
				mShowText.setVisibility(View.VISIBLE);
				mShowText.setText("���ڼ���");
				mFooterProgressBar.setVisibility(View.VISIBLE); // ��ʾ�������ɼ� 
				loadListener.onLoad();
			}
		} catch (Exception e) {
		}

	}
	

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstItemIndex = firstVisibleItem;
	}

}
