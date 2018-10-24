package com.cnpc.geo.energyinformationsearch.search.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.entity
 * @ClassName: SearchListResponse
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年6月29日 上午9:55:54
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0        quwu     2015年6月29日 上午9:55:54
 */
public class SearchListRes implements Serializable {

	private static final long serialVersionUID = -6651824295086074273L;
	
	private List<FileResult> data;
	
	private long currentPage;
	
	private long totalCount;
	
	private long totalPage;
	
	public SearchListRes(){
		data = new ArrayList<FileResult>();
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}

	public long getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(long currentPage) {
		this.currentPage = currentPage;
	}


	public void setData(List<FileResult> data) {
		this.data = data;
	}

	public List<FileResult> getData() {
		return data;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
	
}
