package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

public class DocumentSimpleSearchListReq implements Serializable {

	private static final long serialVersionUID = 2548576918392495845L;

	/** 关键字 **/
	private String keyWord;

	/** 当前页 **/
	private long currentPage;

	/** 每页条数 **/
	private long pageSize;

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public long getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(long currentPage) {
		this.currentPage = currentPage;
	}

	public long getPageSize() {
		return pageSize;
	}

	public void setPageSize(long pageSize) {
		this.pageSize = pageSize;
	}

}
