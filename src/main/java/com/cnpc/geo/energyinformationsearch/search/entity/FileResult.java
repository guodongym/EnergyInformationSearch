package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: FileResult
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年6月29日 上午9:53:39
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年6月29日
 *                    上午9:53:39
 */
public class FileResult implements Serializable {

	private static final long serialVersionUID = -5853546178597393448L;

	private String eid;

	private String hid;

	private String title;

	private String summary;

	private int downloadCount;

	private int viewCount;
	
	private int collectCount;

	private String uri;

	private String fileType;

	private Long createTime;

	private Double scoreAvg;

	private Double scoreCount;

	private int fileTotalPage;

	private String content;

	private String hotCount;

	
	public int getCollectCount() {
		return collectCount;
	}

	public void setCollectCount(int collectCount) {
		this.collectCount = collectCount;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Double getScoreCount() {
		return scoreCount;
	}

	public void setScoreCount(Double scoreCount) {
		this.scoreCount = scoreCount;
	}

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Double getScoreAvg() {
		return scoreAvg;
	}

	public void setScoreAvg(Double scoreAvg) {
		this.scoreAvg = scoreAvg;
	}

	public int getFileTotalPage() {
		return fileTotalPage;
	}

	public void setFileTotalPage(int fileTotalPage) {
		this.fileTotalPage = fileTotalPage;
	}

	public String getHotCount() {
		return hotCount;
	}

	public void setHotCount(String hotCount) {
		this.hotCount = hotCount;
	}
}