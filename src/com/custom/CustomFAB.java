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
	private int mBgColor; // ��ͨ״̬����ɫ
	private int mBgColorPressed; // ���״̬����ɫ

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
	 * ��ʼ��
	 * 
	 * @param attrs
	 */
	@SuppressLint("NewApi")
	private void init(AttributeSet attrs) {
		Theme theme = this.context.getTheme();
		TypedArray arr = theme.obtainStyledAttributes(attrs,
				R.styleable.CustomFAB, 0, 0);
		// �������Բ����б�
		// ����һ������ֵ�б�
		// ����������Ҫ����������
		this.mBgColor = arr
				.getColor(R.styleable.CustomFAB_bg_color, Color.BLUE);
		// ��ȡ��xml�����ĳ�̬����ɫ
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
		OvalShape oShape = new OvalShape(); // ��Բ
		ShapeDrawable sd = new ShapeDrawable(oShape);
		setWillNotDraw(true); // ���û��Ʋ�����ImageButton
		sd.getPaint().setColor(color);
		return sd;
	}

}
