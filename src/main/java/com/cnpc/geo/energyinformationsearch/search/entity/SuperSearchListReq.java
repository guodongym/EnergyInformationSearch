package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SuperSearchListReq implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8222459038397988701L;
	private Map<String, Integer> titleBool;
	private Map<String, Integer> contentBool;
	private Map<String, Integer> publishDateRangeBool;
	private Map<String, Integer> authorBool;
	private Map<List<String>, Integer> fileTypeListBool;
	private Map<List<String>, Integer> sourceOriginalTypeListBool;
	private Map<String, Integer> createTimeRangeBool;
	private int currentPage;
	private int pageSize;
	private String oldKeyWord;
	private String sortWay;

	public String getSortWay() {
		return sortWay;
	}

	public void setSortWay(String sortWay) {
		this.sortWay = sortWay;
	}

	public Map<String, Integer> getTitleBool() {
		return titleBool;
	}

	public void setTitleBool(Map<String, Integer> titleBool) {
		this.titleBool = titleBool;
	}

	public Map<String, Integer> getContentBool() {
		return contentBool;
	}

	public void setContentBool(Map<String, Integer> contentBool) {
		this.contentBool = contentBool;
	}

	public Map<String, Integer> getPublishDateRangeBool() {
		return publishDateRangeBool;
	}

	public void setPublishDateRangeBool(Map<String, Integer> publishDateRangeBool) {
		this.publishDateRangeBool = publishDateRangeBool;
	}

	public Map<String, Integer> getAuthorBool() {
		return authorBool;
	}

	public void setAuthorBool(Map<String, Integer> authorBool) {
		this.authorBool = authorBool;
	}

	public Map<List<String>, Integer> getFileTypeListBool() {
		return fileTypeListBool;
	}

	public void setFileTypeListBool(Map<List<String>, Integer> fileTypeListBool) {
		this.fileTypeListBool = fileTypeListBool;
	}

	public Map<List<String>, Integer> getSourceOriginalTypeListBool() {
		return sourceOriginalTypeListBool;
	}

	public void setSourceOriginalTypeListBool(Map<List<String>, Integer> sourceOriginalTypeListBool) {
		this.sourceOriginalTypeListBool = sourceOriginalTypeListBool;
	}

	public Map<String, Integer> getCreateTimeRangeBool() {
		return createTimeRangeBool;
	}

	public void setCreateTimeRangeBool(Map<String, Integer> createTimeRangeBool) {
		this.createTimeRangeBool = createTimeRangeBool;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getOldKeyWord() {
		return oldKeyWord;
	}

	public void setOldKeyWord(String oldKeyWord) {
		this.oldKeyWord = oldKeyWord;
	}
}