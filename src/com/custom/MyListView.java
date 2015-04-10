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

	private final static int RELEASE_To_REFRESH = 0;// 发布刷新
	private final static int PULL_To_REFRESH = 1;// 下拉刷新
	private final static int REFRESHING = 2;// 正在刷新
	private final static int DONE = 3; // 回到未操作状态
	private final static int LOADING = 4;// 加载
	// 实际的padding的距离与界面上偏移的距离的比例
	private final static int RATIO = 3;
	private static final String TAG = "MyListView";

	private LinearLayout mHeadView;// 添加在ListView上方的头布局
	private TextView mTipsTextview;
	private TextView mLastUpdatedTextView;
	private ImageView mArrowImageView;
	private ProgressBar mHeaderProgressBar;

	private LinearLayout mFooterView; // 尾部布局
	private TextView mShowText; // 显示状态字符串
	private ProgressBar mFooterProgressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;
	private boolean isRecored; // 用于保证startY的值在一个完整的touch事件中只被记录一次
	private int headContentHeight;
	private int startY;
	private int mFirstItemIndex;// 列表滑动时，标记屏幕内可见的第一个Item序号
	private int mState;
	private boolean isBack; // 箭头是否要往下
	private OnRefreshListener refreshListener;
	private boolean isRefreshable;
	private OnLoadListener loadListener;
	private boolean mLoadEnable;
	private boolean isLoading; // 判断是否正在加载
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
	 * 初始化View
	 * 
	 * @param context
	 */
	@SuppressLint("InflateParams")
	private void init(Context context) {

		inflater = LayoutInflater.from(context);
		// -----初始化头部和其子组件-------------------------
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
		setOnScrollListener(this); // 给ListView设置滚动监听器

		headContentHeight = mHeadView.getMeasuredHeight();
		mHeadView.setPadding(0, -1 * headContentHeight, 0, 0);
		// 刷新view
		mHeadView.invalidate();
		// 添加头文件不被 selected
		addHeaderView(mHeadView, null, false);
		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250); // 持续时间
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator()); // 设置加速曲线
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		// 尾部初始化
		mFooterView = (LinearLayout) inflater.inflate(
				R.layout.mylistview_footer, null);
		mShowText = (TextView) mFooterView.findViewById(R.id.showText_footer);
		mFooterProgressBar = (ProgressBar) mFooterView
				.findViewById(R.id.ProgressBar_footer);
		addFooterView(mFooterView); // 添加尾部

		mState = DONE;
		isRefreshable = false;
		mLoadEnable = false;
	}

	/**
	 * 计算mHeaderView的高和宽
	 */
	@SuppressWarnings("deprecation")
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		// spec 父窗口传递给子窗口的大小和模式
		// padding 父窗口的边距，相当于android:padding
		// childDimension 子窗口想要绘制的准确大小，最终不一定绘制这个值

		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			// MeasureSpec.UNSPECIFIED 值=0
			// 未指定尺寸这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式
			// MeasureSpec.EXACTLY,精确尺寸
			// MeasureSpec.AT_MOST最大尺寸
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
					// Log.v("this is", "在down时候记录当前位置");
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
						// Log.v("this is", "由下拉刷新状态，到done状态");
					}
					if (mState == RELEASE_To_REFRESH) {
						mState = REFRESHING;
						changeHeaderViewByState();
						onRefresh();
						// Log.v("this is", "由松开刷新状态，到done状态");
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
					// Log.v("this is", "在move时候记录下位置");
				}
				// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
				if (mState != REFRESHING && isRecored && mState != LOADING) {

					// 可以松手去刷新了
					if (mState == RELEASE_To_REFRESH) {
						setSelection(0);
						// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							mState = PULL_To_REFRESH;
							changeHeaderViewByState();
							// Log.v("this is", "由松开刷新状态转变到下拉刷新状态");

						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							mState = DONE;
							changeHeaderViewByState();
							// Log.v("this is", "由松开刷新状态转变到done状态");

						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (mState == PULL_To_REFRESH) {
						setSelection(0);
						// 下拉到可以进入RELEASE_TO_REFRESH的状态
						if ((tempY - startY) / RATIO >= headContentHeight) {
							mState = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();
							// Log.v("this is", "由done或者下拉刷新状态转变到松开刷新");

						}
						// 上推到顶了
						else if (tempY - startY <= 0) {
							mState = DONE;
							changeHeaderViewByState();
							// Log.v("this is", "由done或者下拉刷新状态转变到done状态");

						}
					}
					// done状态下

					if (mState == DONE) {
						if (tempY - startY > 0) {
							mState = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}
					// 更新headView的size

					if (mState == PULL_To_REFRESH) {
						mHeadView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);
					}
					// 更新headView的paddingTop

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
	 * 更改头部布局的状态
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
			mTipsTextview.setText("松开刷新");
			break;
		case PULL_To_REFRESH:
			mHeaderProgressBar.setVisibility(View.GONE);
			mTipsTextview.setVisibility(View.VISIBLE);
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的

			if (isBack) {
				isBack = false;
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(reverseAnimation);
				mTipsTextview.setText("下拉刷新");
			} else {
				mTipsTextview.setText("下拉刷新");
			}
			break;

		case REFRESHING:
			mHeadView.setPadding(0, 0, 0, 0);
			mHeaderProgressBar.setVisibility(View.VISIBLE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.GONE);
			mTipsTextview.setText("正在刷新 ...");
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case DONE:
			mHeadView.setPadding(0, -1 * headContentHeight, 0, 0);
			mHeaderProgressBar.setVisibility(View.GONE);
			mArrowImageView.clearAnimation();
			mArrowImageView.setImageResource(R.drawable.arrow);
			mTipsTextview.setText("刷新成功");
			mLastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		}

	}

	
	
	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	// 自定义下拉更新接口
	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void setonLoadListener(OnLoadListener loadListener) {
		this.loadListener = loadListener;
		mLoadEnable = true;
	}

	// 加载接口
	public interface OnLoadListener {
		public void onLoad();
	}

	/**
	 * 加载完毕*/
	public void onLoadComplete() {
		isLoading = false;
		mShowText.setVisibility(View.VISIBLE);
		mShowText.setText("加载完成");
		mFooterProgressBar.setVisibility(View.INVISIBLE); // 显示进度条不可见
	}
	
	/**
	 * 无数据可加载*/
	public void onLoadNoData(){
		isLoading = false;
		mShowText.setVisibility(View.VISIBLE);
		mShowText.setText("暂无数据");
		mFooterProgressBar.setVisibility(View.INVISIBLE); // 显示进度条不可见
	}

	// 注入接口
	@SuppressWarnings("deprecation")
	public void onRefreshComplete() {
		mState = DONE;
		mLastUpdatedTextView.setText("最近更新: " + new Date().toLocaleString());
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
			// OnScrollListener.SCROLL_STATE_IDLE表示停止滚动
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& !isLoading
					&& view.getLastVisiblePosition() == view
							.getPositionForView(mFooterView)) {
				// 正在加载
				isLoading = true;
				mShowText.setVisibility(View.VISIBLE);
				mShowText.setText("正在加载");
				mFooterProgressBar.setVisibility(View.VISIBLE); // 显示进度条可见 
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
