package com.cnpc.geo.energyinformationsearch.file.model;

import com.alibaba.fastjson.annotation.JSONField;

public class LogChnHot {

	@JSONField(name = "c_eid")
	private String eid;
	@JSONField(name = "c_count")
	private String count;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
}