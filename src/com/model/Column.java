package com.model;

import java.io.Serializable;

public class Column implements Serializable {
	private static final long serialVersionUID = -7060210544600464481L;  
	private String mColumnTitle; // ר����Ŀ
	private String mImageUrl; // ר��ͷ������
	private String mColumnOwner; // ר��ӵ����
	private String mColumnContent; // ר������˵��
	private String mColumnUrl; // ר������

	public String getColumnTitle() {
		return mColumnTitle;
	}

	public void setColumnTitle(String mColumnTitle) {
		this.mColumnTitle = mColumnTitle;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String mImageUrl) {
		this.mImageUrl = mImageUrl;
	}

	public String getColumnOwner() {
		return mColumnOwner;
	}

	public void setColumnOwner(String mColumnOwner) {
		this.mColumnOwner = mColumnOwner;
	}

	public String getColumnContent() {
		return mColumnContent;
	}

	public void setColumnContent(String mColumnContent) {
		this.mColumnContent = mColumnContent;
	}

	public String getColumnUrl() {
		return mColumnUrl;
	}

	public void setColumnUrl(String mColumnUrl) {
		this.mColumnUrl = mColumnUrl;
	}

}
