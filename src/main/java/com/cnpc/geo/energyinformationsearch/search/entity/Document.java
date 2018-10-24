package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

public class Document implements Serializable {

	private static final long serialVersionUID = -5689745551858108352L;

	/** ES id **/
	private String eid;

	/** H id **/
	private String hid;

	/** 文档名称 **/
	private String fileName;

	/** 文档简介 **/
	private String summary;

	/** 文档类型 **/
	private String fileType;

	/** 文档锁定状态( 1：锁定 、2：未锁定 ) **/
	private String lockStatus;

	/** 文档锁定人 **/
	private String lockUser;

	/** 文档来源类型 **/
	private String sourceOriginalType;

	/** 创建者ID（上传文档为用户ID，摄取文档默认为robot） **/
	private String creatorId;

	/** 文档来源ID（数据源编号） **/
	private String sourceId;

	/** 文档入库时间 **/
	private long createTime;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(String lockStatus) {
		this.lockStatus = lockStatus;
	}

	public String getLockUser() {
		return lockUser;
	}

	public void setLockUser(String lockUser) {
		this.lockUser = lockUser;
	}

	public String getSourceOriginalType() {
		return sourceOriginalType;
	}

	public void setSourceOriginalType(String sourceOriginalType) {
		this.sourceOriginalType = sourceOriginalType;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getHid() {
		return hid;
	}

	public void setHid(String hid) {
		this.hid = hid;
	}
}