package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.List;

public class SynergyRecommendRes implements Serializable {

	private static final long serialVersionUID = -9120127359134343121L;
	private List<FileResult> data;

	public List<FileResult> getData() {
		return data;
	}

	public void setData(List<FileResult> data) {
		this.data = data;
	}
}