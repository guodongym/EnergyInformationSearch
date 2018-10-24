package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;

public class DocumentSearchListRes implements Serializable {

	private static final long serialVersionUID = -2148550501877160992L;

	/** 文档列表 **/
	private List<Document> documentList;
	
	/** 当前页 **/
	private long currentPage;
	
	/** 总条数 **/
	private long totalCount;
	
	/** 总页数 **/
	private long totalPage;

	public List<Document> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<Document> documentList) {
		this.documentList = documentList;
	}

	public long getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(long currentPage) {
		this.currentPage = currentPage;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

}
