package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: FileContentReq
 * @Description: 详情页展示接入实体
 * @author zhaoguodong
 * @date 2015-7-3 下午1:16:32
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 zhaoguodong
 *                    2015-7-3 下午1:16:32
 */
public class FileContentReq implements Serializable {

	private static final long serialVersionUID = 8932216134008858877L;

	/* ESID */
	private String eid;

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}


}
