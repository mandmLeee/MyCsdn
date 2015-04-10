package com.model;

/**
 * 博客页面的组成元素
 * */
public class ArticleElement {
	public static final int TITLE = 1;
	public static final int CONTENT = 2;
	public static final int IMAGE = 3;
	public static final int CODE = 5;

	private String mTitle; // 标题
	private String mContent; // 内容
	private String mImageLink; // 图片链接
	private String mCode; // 代码
	private int mStyle; // 类别

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.setStyle(this.TITLE);
		this.mTitle = mTitle;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String mContent) {
		this.setStyle(this.CONTENT);
		this.mContent = mContent;
	}

	public String getImageLink() {
		return mImageLink;
	}

	public void setImageLink(String mImageLink) {
		this.setStyle(ArticleElement.IMAGE);
		this.mImageLink = mImageLink;
	}

	public int getStyle() {
		return mStyle;
	}

	public void setStyle(int mStyle) {
		this.mStyle = mStyle;
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String mCode) {
		this.mStyle = this.CODE;
		this.mCode = mCode;
	}

}
