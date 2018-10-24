package com.cnpc.geo.energyinformationsearch.test;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.util.HttpUtil;
import com.cnpc.geo.energyinformationsearch.search.entity.SearchListReq;

public class Test {
	public static void main(String [] args) {
//		RelativeSearchReq paramsSub = new RelativeSearchReq();
//		paramsSub.setKeyWord("");
//		paramsSub.setPageSize(10);
		
		SearchListReq searchListReq = new SearchListReq();
		searchListReq.setKeyWord("中国石油");
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("sid", "111");
		params.put("reqData", JSON.toJSONString(searchListReq));
		
		HttpUtil.connectPostHttps("http://localhost:8080/EnergyInformationSearch/searchIface/searchList.do", params);
	}
}

//	MatchQueryBuilder boolQuery = null;
//	if (relativeSearchReq.getKeyWord() != null && !("").equals(relativeSearchReq.getKeyWord())) {
//		boolQuery = QueryBuilders.matchQuery(StringUtil.getColumnString("keyWord"), relativeSearchReq.getKeyWord());
//	}
//	
//	List<String> fieldList = new ArrayList<String>();
//	fieldList.add(StringUtil.getColumnString("keyWord"));
//
//	SignificantTermsBuilder group = AggregationBuilders.significantTerms("searchresults").field("c_title");
//	SearchResponse sr = esClientService.searchAggsIndex(new String[] { "doc" }, new String[] { "doc-*" }, null, boolQuery, group, null, false);
//	if (sr != null) {
//		SignificantTerms titleTerms = sr.getAggregations().get("searchresults");
//		for (Bucket b : titleTerms.getBuckets()) {
//			System.out.println(b.getKey());
//			System.out.println(b.getDocCount());
//		}
//	}
