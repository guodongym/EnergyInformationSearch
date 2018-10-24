package com.cnpc.geo.energyinformationsearch.job;

import java.io.Serializable;

public class FeedbackMessage implements Serializable {

	/**
	 * 序列化使用的uid
	 */
	private static final long serialVersionUID = 1L;

	private String sid;
	private String eid;
	private String hid;
	private String checkTag;
	private String checkMsg;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
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

	public String getCheckTag() {
		return checkTag;
	}

	public void setCheckTag(String checkTag) {
		this.checkTag = checkTag;
	}

	public String getCheckMsg() {
		return checkMsg;
	}

	public void setCheckMsg(String checkMsg) {
		this.checkMsg = checkMsg;
	}
}