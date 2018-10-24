package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: RelativeSearchReq
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author louyujie
 * @date 2015年7月9日 下午5:42:22
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        louyujie     2015年7月9日 下午5:42:22
 */
public class RelativeSearchReq implements Serializable{
	
	private static final long serialVersionUID = -5291882980045605359L;
	
	private String keyWord;
	
	private int pageSize;
	
	public RelativeSearchReq(){
		keyWord="石油";
	}
	
	public String getKeyWord() {
		return keyWord;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
}
