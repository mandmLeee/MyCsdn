package com.model;

import java.io.Serializable;

public class Blog implements Serializable {
	private String mBlogUrl; // ��������
	private String mBlogTitle; // ������������
	private String mBlogType; // ������������
	private String mBlogContent; // ��������
	private String mBlogAuthor; // ��������
	private String mAgo; // ��ȥʱ��
	private String mReadNumb; // �Ķ�����
	private String mBloggerIcon; // ����ͷ��
	private String mCommentNumb; // ���۴���

	public Blog() {
	}

	public void setBlogTitle(String blogTitle) {
		mBlogTitle = blogTitle;
	}

	public String getBlogTitle() {
		return mBlogTitle;
	}

	public void setBlogType(String blogType) {
		mBlogType = blogType;
	}

	public String getBlogType() {
		return mBlogType;
	}

	public void setBlogContent(String blogContent) {
		mBlogContent = blogContent;
	}

	public String getBlogContent() {
		return mBlogContent;
	}

	public String getBlogAuthor() {
		return mBlogAuthor;
	}

	public void setBlogAuthor(String mBlogAuthor) {
		this.mBlogAuthor = mBlogAuthor;
	}

	public String getAgo() {
		return mAgo;
	}

	public void setAgo(String mAgo) {
		this.mAgo = mAgo;
	}

	public String getReadNum() {
		return mReadNumb;
	}

	public void setReadNum(String mReadNum) {
		this.mReadNumb = mReadNum;
	}

	public String getBloggerIcon() {
		return mBloggerIcon;
	}

	public void setBloggerIcon(String mBloggerIcon) {
		this.mBloggerIcon = mBloggerIcon;
	}

	public String getCommentNumb() {
		return mCommentNumb;
	}

	public void setCommentNumb(String mCommentNumb) {
		this.mCommentNumb = mCommentNumb;
	}

	public String getBlogUrl() {
		return mBlogUrl;
	}

	public void setBlogUrl(String mBlogUrl) {
		this.mBlogUrl = mBlogUrl;
	}

}
