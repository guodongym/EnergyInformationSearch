package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

/***
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: FileContentRes
 * @Description: 详情页展示写出实体
 * @author zhaoguodong
 * @date 2015-7-3 下午1:17:32
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 zhaoguodong
 *                    2015-7-3 下午1:17:32
 **/
public class FileContentRes implements Serializable {

	private static final long serialVersionUID = -477091320021818557L;

	/** HbaseID **/
	private String hid;
	/** 文件名 **/
	private String fileName;
	/** 文件大小byte单位 **/
	private long fileSize;
	/** 文件类型 **/
	private String fileType;
	/** 标题 **/
	private String title;
	/** 正文 **/
	private String content;
	/** 文档来源 **/
	private String sourceOriginalType;
	/** 查看数 **/
	private int viewCount;
	/** 下载数 **/
	private int downloadCount;
	/** 平均评分 **/
	private Double scoreAvg;
	/** 评分人数 **/
	private int scoreCount;
	/** 入库时间 **/
	private long createTime;
	/** 链接 **/
	private String uri;
	/** 数据源类别 **/
	private String sourceFormatType;
	/** 创建者 **/
	private String creatorId;
	/** 收藏次数 **/
	private Integer collectCount;
	/** 简介 **/
	private String description;

	public Integer getCollectCount() {
		return collectCount;
	}

	public void setCollectCount(Integer collectCount) {
		this.collectCount = collectCount;
	}

	public String getHid() {
		return hid;
	}

	public void setHid(String hid) {
		this.hid = hid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSourceOriginalType() {
		return sourceOriginalType;
	}

	public void setSourceOriginalType(String sourceOriginalType) {
		this.sourceOriginalType = sourceOriginalType;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	public Double getScoreAvg() {
		return scoreAvg;
	}

	public void setScoreAvg(Double scoreAvg) {
		this.scoreAvg = scoreAvg;
	}

	public int getScoreCount() {
		return scoreCount;
	}

	public void setScoreCount(int scoreCount) {
		this.scoreCount = scoreCount;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSourceFormatType() {
		return sourceFormatType;
	}

	public void setSourceFormatType(String sourceFormatType) {
		this.sourceFormatType = sourceFormatType;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}