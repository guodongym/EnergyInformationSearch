package com.cnpc.geo.energyinformationsearch.es.template;

import java.util.Date;
import java.util.List;
/**
 * 非结构化数据Elasticsearch Schema
 * @author Administrator
 *
 */
public class FileTemplate {
	//标识
	private String id;
	//文件名
	private String fileName;
	//文件大小
	private String filesize;
	//文件格式
	private String fileformat;
	//最后更新时间
	private Date lastmodify;
	//指纹,SimHash与Hamming distance
	private String finger;
	//
	private String content;
	//标题
	private String title;
	//作者
	private String author;
	//关键字,在html 的meta 标签中
	private List<String> keywords;
	//摘要,html meta标签中的description
	private String description;
	//html meta标签中的copyright
	private String copyright;
	//tika获得的所有meta数据
	private String metadata;
	//创建者id
	private String creatorid;
	//创建时间
	private long createtime;
	//查看数
	private Integer viewcount;
	//下载数
	private Integer downcount;
	//地址
	private String uri;
	//来源名称
	private List<String> sourcename;
	//来源ID
	private List<String> sourceid;
	//四个坐标定义一个来源
	private List<String> sourcetypeA;
	private List<String> sourcetypeB;
	private List<String> sourcetypeC;
	private List<String> sourcetypeD;
	//标签
	private List<String> tags;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilesize() {
		return filesize;
	}
	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}
	public String getFileformat() {
		return fileformat;
	}
	public void setFileformat(String fileformat) {
		this.fileformat = fileformat;
	}
	public Date getLastmodify() {
		return lastmodify;
	}
	public void setLastmodify(Date lastmodify) {
		this.lastmodify = lastmodify;
	}
	public String getFinger() {
		return finger;
	}
	public void setFinger(String finger) {
		this.finger = finger;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public List<String> getKeywords() {
		return keywords;
	}
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public String getCreatorid() {
		return creatorid;
	}
	public void setCreatorid(String creatorid) {
		this.creatorid = creatorid;
	}
	
	public long getCreatetime() {
		return createtime;
	}
	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Integer getViewcount() {
		return viewcount;
	}
	public void setViewcount(Integer viewcount) {
		this.viewcount = viewcount;
	}
	public Integer getDowncount() {
		return downcount;
	}
	public void setDowncount(Integer downcount) {
		this.downcount = downcount;
	}
	public List<String> getSourcename() {
		return sourcename;
	}
	public void setSourcename(List<String> sourcename) {
		this.sourcename = sourcename;
	}
	public List<String> getSourceid() {
		return sourceid;
	}
	public void setSourceid(List<String> sourceid) {
		this.sourceid = sourceid;
	}
	public List<String> getSourcetypeA() {
		return sourcetypeA;
	}
	public void setSourcetypeA(List<String> sourcetypeA) {
		this.sourcetypeA = sourcetypeA;
	}
	public List<String> getSourcetypeB() {
		return sourcetypeB;
	}
	public void setSourcetypeB(List<String> sourcetypeB) {
		this.sourcetypeB = sourcetypeB;
	}
	public List<String> getSourcetypeC() {
		return sourcetypeC;
	}
	public void setSourcetypeC(List<String> sourcetypeC) {
		this.sourcetypeC = sourcetypeC;
	}
	public List<String> getSourcetypeD() {
		return sourcetypeD;
	}
	public void setSourcetypeD(List<String> sourcetypeD) {
		this.sourcetypeD = sourcetypeD;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}
