package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;

public class FileScoreReq  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5858815220639926228L;
	
	private String eid;
	
	private long score;
	
	public String getEid() {
		return eid;
	}
	public void setEid(String eid) {
		this.eid = eid;
	}
	public long getScore() {
		return score;
	}
	public void setScore(long score) {
		this.score = score;
	}
	
}
