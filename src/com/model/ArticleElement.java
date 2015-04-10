package com.model;

/**
 * ����ҳ������Ԫ��
 * */
public class ArticleElement {
	public static final int TITLE = 1;
	public static final int CONTENT = 2;
	public static final int IMAGE = 3;
	public static final int CODE = 5;

	private String mTitle; // ����
	private String mContent; // ����
	private String mImageLink; // ͼƬ����
	private String mCode; // ����
	private int mStyle; // ���

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
