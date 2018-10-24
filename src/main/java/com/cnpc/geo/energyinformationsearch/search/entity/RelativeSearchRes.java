package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: RelativeSearchRes
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author louyujie
 * @date 2015年7月9日 下午5:42:35
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        louyujie     2015年7月9日 下午5:42:35
 */
public class RelativeSearchRes implements Serializable {
	
	private static final long serialVersionUID = 2379301938368291375L;
	
	private List<String> relativeList ;
	
	public RelativeSearchRes(){
		relativeList = new ArrayList<String>();
	}

	public List<String> getRelativeList() {
		return relativeList;
	}

	public void setRelativeList(List<String> relativeList) {
		this.relativeList = relativeList;
	}
	
}
