package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: SearchListRequest
 * @author quwu
 * @date 2015年6月29日 上午9:40:09
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年6月29日
 *                    上午9:40:09
 */
public class SearchListReq implements Serializable {

	private static final long serialVersionUID = 637328915561347640L;

	private String oldKeyWord;
	private String keyWord;
	private List<String> languageList;
	private List<String> sourceOriginalTypeList;
	private List<String> fileTypeList;
	private List<String> sourceCategoryTypeList;
	private Long createTimeBegin;
	private Long createTimeEnd;
	private List<Map<String, Integer>> sortMapList;
	private int currentPage;
	private int pageSize;
	private String eid;

	public String getOldKeyWord() {
		return oldKeyWord;
	}

	public void setOldKeyWord(String oldKeyWord) {
		this.oldKeyWord = oldKeyWord;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public List<String> getLanguageList() {
		return languageList;
	}

	public void setLanguageList(List<String> languageList) {
		this.languageList = languageList;
	}

	public List<String> getSourceOriginalTypeList() {
		return sourceOriginalTypeList;
	}

	public void setSourceOriginalTypeList(List<String> sourceOriginalTypeList) {
		this.sourceOriginalTypeList = sourceOriginalTypeList;
	}

	public List<String> getFileTypeList() {
		return fileTypeList;
	}

	public void setFileTypeList(List<String> fileTypeList) {
		this.fileTypeList = fileTypeList;
	}

	public List<String> getSourceCategoryTypeList() {
		return sourceCategoryTypeList;
	}

	public void setSourceCategoryTypeList(List<String> sourceCategoryTypeList) {
		this.sourceCategoryTypeList = sourceCategoryTypeList;
	}

	public Long getCreateTimeBegin() {
		return createTimeBegin;
	}

	public void setCreateTimeBegin(Long createTimeBegin) {
		this.createTimeBegin = createTimeBegin;
	}

	public Long getCreateTimeEnd() {
		return createTimeEnd;
	}

	public void setCreateTimeEnd(Long createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public List<Map<String, Integer>> getSortMapList() {
		return sortMapList;
	}

	public void setSortMapList(List<Map<String, Integer>> sortMapList) {
		this.sortMapList = sortMapList;
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

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}
}