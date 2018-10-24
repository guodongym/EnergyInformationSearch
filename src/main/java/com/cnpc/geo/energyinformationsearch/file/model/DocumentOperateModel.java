package com.cnpc.geo.energyinformationsearch.file.model;


import java.io.Serializable;

public class DocumentOperateModel implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String eid;
	private String hid;
	
	public DocumentOperateModel() {
		super();
	}

	public DocumentOperateModel(String eid, String hid) {
		super();
		this.eid = eid;
		this.hid = hid;
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
	
	
	
}
