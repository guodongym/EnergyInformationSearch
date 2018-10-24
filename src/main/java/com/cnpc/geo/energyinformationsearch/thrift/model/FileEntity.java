package com.cnpc.geo.energyinformationsearch.thrift.model;

public class FileEntity {
	
	private String title;//标题
	private String fileName;//文件名
	private String tags;//标签
	private String description;//描述
	private String fileExtention;//文件后缀
	private String sequenceID;//消息ID
	private byte[] fileContent;//文件内容
	private String userID;//用户ID
	private String uri;//uri 他是userID+sequenceID
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFileExtention() {
		return fileExtention;
	}
	public void setFileExtention(String fileExtention) {
		this.fileExtention = fileExtention;
	}
	public String getSequenceID() {
		return sequenceID;
	}
	public void setSequenceID(String sequenceID) {
		this.sequenceID = sequenceID;
	}
	public byte[] getFileContent() {
		return fileContent;
	}
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
}

