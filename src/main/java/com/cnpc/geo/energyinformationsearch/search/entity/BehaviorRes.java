package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;

public class BehaviorRes implements Serializable {

	private static final long serialVersionUID = 5219229467749634889L;

	/** 索引列表 **/
	private List<FileResult> data;

	public List<FileResult> getData() {
		return data;
	}

	public void setData(List<FileResult> data) {
		this.data = data;
	}

}
