package com.cnpc.geo.energyinformationsearch.file.model;

import com.alibaba.fastjson.annotation.JSONField;

public class CollctionFolderAndDoc {
	@JSONField(name = "c_eid")
	private String eid;// elasticsearch ID号
	@JSONField(name = "c_hid")
	private String hid;// hbase的RowKey
	@JSONField(name = "c_session_id")
	private String sessionId;// session的Id号
	@JSONField(name = "c_user_id")
	private String userId;// 用户的Id号
	@JSONField(name = "c_folder_id")
	private String folderId;// 收藏文件夹的Id号
	@JSONField(name = "c_folder_name")
	private String folderName;// 收藏文件夹的名字
	@JSONField(name = "c_create_time")
	private long createTime;// 文件收藏时间
	@JSONField(name = "c_file_name")
	private String fileName;// 收藏的文件名字
	@JSONField(name = "c_is_folder")
	private String isFolder;// 是否是文件夹
	@JSONField(name = "c_file_type")
	private String fileType;// 是否是文件夹
	@JSONField(name = "c_folder_md5")
	private String folderMd5;// 是否是文件夹

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getHid() {
		return hid;
	}

	public void setHid(String hid) {
		this.hid = hid;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getIsFolder() {
		return isFolder;
	}

	public void setIsFolder(String isFolder) {
		this.isFolder = isFolder;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFolderMd5() {
		return folderMd5;
	}

	public void setFolderMd5(String folderMd5) {
		this.folderMd5 = folderMd5;
	}
}