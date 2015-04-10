package com.model;

import java.io.Serializable;

public class Blog implements Serializable {
	private String mBlogUrl; // 博客链接
	private String mBlogTitle; // 博客文章名称
	private String mBlogType; // 博客文章类型
	private String mBlogContent; // 博客内容
	private String mBlogAuthor; // 博客作者
	private String mAgo; // 过去时间
	private String mReadNumb; // 阅读次数
	private String mBloggerIcon; // 博主头像
	private String mCommentNumb; // 评论次数

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
