package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;

public class DocumentlockReq implements Serializable {

	private static final long serialVersionUID = -6572382271365337172L;

	/** ElasticSearchId **/
	private List<String> eidList;

	/** 文档锁定人 **/
	private String lockUser;

	public List<String> getEidList() {
		return eidList;
	}

	public void setEidList(List<String> eidList) {
		this.eidList = eidList;
	}

	public String getLockUser() {
		return lockUser;
	}

	public void setLockUser(String lockUser) {
		this.lockUser = lockUser;
	}

}
