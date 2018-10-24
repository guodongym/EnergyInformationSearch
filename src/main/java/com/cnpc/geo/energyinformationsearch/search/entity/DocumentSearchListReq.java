package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;

public class DocumentSearchListReq implements Serializable {

	private static final long serialVersionUID = -8608850252290026481L;

	/** 文档名称 **/
	private String fileName;

	/** 文档名称条件( 1：等于 2：包含 ) **/
	private String fileNameTerm;

	/** 文档简介 **/
	private String summary;

	/** 文档简介条件( 1：等于 2：包含 ) **/
	private String summaryTerm;

	/** 文档类型 **/
	private List<String> fileTypeList;

	/** 文档来源类型 **/
	private List<String> sourceOriginalTypeList;

	/** 入库时间开始 **/
	private long createTimeBegin;

	/** 入库时间结束 **/
	private long createTimeEnd;

	/** 文档锁定状态 **/
	private List<String> lockStatusList;

	/** 文档锁定人 **/
	private String lockUser;

	/** 文档锁定人条件( 1：等于 2：包含 ) **/
	private String lockUserTerm;

	/** 当前页 **/
	private long currentPage;

	/** 每页条数 **/
	private long pageSize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileNameTerm() {
		return fileNameTerm;
	}

	public void setFileNameTerm(String fileNameTerm) {
		this.fileNameTerm = fileNameTerm;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummaryTerm() {
		return summaryTerm;
	}

	public void setSummaryTerm(String summaryTerm) {
		this.summaryTerm = summaryTerm;
	}

	public List<String> getFileTypeList() {
		return fileTypeList;
	}

	public void setFileTypeList(List<String> fileTypeList) {
		this.fileTypeList = fileTypeList;
	}

	public List<String> getSourceOriginalTypeList() {
		return sourceOriginalTypeList;
	}

	public void setSourceOriginalTypeList(List<String> sourceOriginalTypeList) {
		this.sourceOriginalTypeList = sourceOriginalTypeList;
	}

	public long getCreateTimeBegin() {
		return createTimeBegin;
	}

	public void setCreateTimeBegin(long createTimeBegin) {
		this.createTimeBegin = createTimeBegin;
	}

	public long getCreateTimeEnd() {
		return createTimeEnd;
	}

	public void setCreateTimeEnd(long createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public List<String> getLockStatusList() {
		return lockStatusList;
	}

	public void setLockStatusList(List<String> lockStatusList) {
		this.lockStatusList = lockStatusList;
	}

	public String getLockUser() {
		return lockUser;
	}

	public void setLockUser(String lockUser) {
		this.lockUser = lockUser;
	}

	public String getLockUserTerm() {
		return lockUserTerm;
	}

	public void setLockUserTerm(String lockUserTerm) {
		this.lockUserTerm = lockUserTerm;
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
