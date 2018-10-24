package com.cnpc.geo.energyinformationsearch.file.model;

import java.io.Serializable;
import java.util.List;

public class ResponseCollctionFolderModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7889031208730788677L;
	private List<CollctionFolderModel> collctionFolderModel;
	private long totalCount;//总条数
	private long totalPage;//总页数
	
	
	public List<CollctionFolderModel> getCollctionFolderModel() {
		return collctionFolderModel;
	}
	public void setCollctionFolderModel(
			List<CollctionFolderModel> collctionFolderModel) {
		this.collctionFolderModel = collctionFolderModel;
	}
	public long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
	public long getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}
}
