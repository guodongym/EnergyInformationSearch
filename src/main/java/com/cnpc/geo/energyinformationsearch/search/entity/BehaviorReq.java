package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

public class BehaviorReq implements Serializable {

	private static final long serialVersionUID = -4264122610217135242L;

	/** ElasticSearchId **/
	private String eid;

	/** 关键字 **/
	private String keyWord;

	/** 每页条数 **/
	private int pageSize;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
}