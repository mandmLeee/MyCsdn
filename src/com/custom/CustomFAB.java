package com.custom;

import com.example.mycsdn.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class CustomFAB extends ImageButton {
	private Context context;
	private int mBgColor; // 普通状态背景色
	private int mBgColorPressed; // 点击状态背景色

	public CustomFAB(Context context) {
		super(context);
		this.context = context;
	}

	public CustomFAB(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs);
	}

	/**
	 * 初始化
	 * 
	 * @param attrs
	 */
	@SuppressLint("NewApi")
	private void init(AttributeSet attrs) {
		Theme theme = this.context.getTheme();
		TypedArray arr = theme.obtainStyledAttributes(attrs,
				R.styleable.CustomFAB, 0, 0);
		// 返回属性参数列表
		// 参数一：属性值列表
		// 参数二：需要检索的属性
		this.mBgColor = arr
				.getColor(R.styleable.CustomFAB_bg_color, Color.BLUE);
		// 获取从xml解析的常态背景色
		this.mBgColorPressed = arr.getColor(
				R.styleable.CustomFAB_bg_color_pressed, Color.GRAY);
		try {
			StateListDrawable sld = new StateListDrawable();
			sld.addState(new int[] { android.R.attr.state_pressed },
					createButton(this.mBgColorPressed));
			sld.addState(new int[] {}, createButton(this.mBgColor));
			setBackground(sld);
		} catch (Throwable t) {
		} finally {
			arr.recycle();
		}
	}

	private Drawable createButton(int color) {
		OvalShape oShape = new OvalShape(); // 椭圆
		ShapeDrawable sd = new ShapeDrawable(oShape);
		setWillNotDraw(true); // 设置机制不绘制ImageButton
		sd.getPaint().setColor(color);
		return sd;
	}

}
