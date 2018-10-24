package com.cnpc.geo.energyinformationsearch.file.model;

import com.alibaba.fastjson.annotation.JSONField;

public class SynergyRecommend {

	@JSONField(name = "c_eid")
	private String eid;
	@JSONField(name = "c_eids")
	private String eids;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getEids() {
		return eids;
	}

	public void setEids(String eids) {
		this.eids = eids;
	}
}