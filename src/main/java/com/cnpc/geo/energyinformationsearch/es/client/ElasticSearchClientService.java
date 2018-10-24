package com.cnpc.geo.energyinformationsearch.es.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;

import com.cnpc.geo.energyinformationsearch.base.util.GeneratorIndexAndType;
import com.cnpc.geo.energyinformationsearch.base.util.StringUtil;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.es
 * @ClassName: ElasticSearchClientService
 * @author quwu
 * @date 2015年7月1日 上午10:08:09
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年7月1日
 *                    上午10:08:09
 */
public class ElasticSearchClientService {

	Logger logger = Logger.getLogger(ElasticSearchClientService.class);
	private TransportClient client;

	/**
	 * 
	 * @Title: getClient
	 * @author quwu
	 * @param ipAddress
	 * @param port
	 * @param clusterName
	 * @return
	 */
	private synchronized TransportClient getClient(ElasticSearchClientConfig clientConfig) {
		if (client == null) {
			Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", clientConfig.getSniff()).put("discovery.zen.ping.timeout", clientConfig.getPingTimeOut()).put("cluster.name", clientConfig.getClusterName()).build();
			client = new TransportClient(settings);
			String[] nodeList = clientConfig.getNodeList().split(",");
			for (String node : nodeList) {
				String tmp[] = node.split(":");
				client.addTransportAddress(new InetSocketTransportAddress(tmp[0], Integer.valueOf(tmp[1])));
			}
		}
		return client;
	}

	public TransportClient getClient() {
		return client;
	}

	/**
	 * 
	 * @author quwu
	 * @param clientConfig
	 */
	public ElasticSearchClientService(ElasticSearchClientConfig clientConfig) {
		client = getClient(clientConfig);
	}

	/**
	 * 
	 * @param indexname
	 * @param type
	 * @param id
	 * @return
	 */
	public GetResponse get(String indexName, String type, String id, String[] fields) {
		GetRequestBuilder getRequestBuilder = client.prepareGet(indexName, type, id);
		if (fields != null && fields.length > 0)
			getRequestBuilder.setFields(fields);
		GetResponse getResponse = getRequestBuilder.execute().actionGet();
		return getResponse;
	}

	public SearchHits getByField(String[] typeList, String[] indexList, String fieldName, String fieldValue) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		TermQueryBuilder teamQuery = QueryBuilders.termQuery(fieldName, fieldValue);
		this.setQuery(searchRequestBuilder, teamQuery);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}

	/**
	 * 
	 * @Title: createIndex
	 * @author quwu
	 * @param indexname
	 * @param type
	 * @param json
	 * @return
	 */
	public IndexResponse createIndex(String indexname, String type, String json) {
		IndexResponse response = client.prepareIndex(indexname, type).setSource(json).execute().actionGet();
		return response;
	}

	public IndexResponse createIndex(String indexname, String type, String json, String id) {
		IndexResponse response = client.prepareIndex(indexname, type, id).setSource(json).execute().actionGet();
		return response;
	}

	/**
	 * 
	 * @Title: updateIndex
	 * @author quwu
	 * @param indexname
	 * @param type
	 * @param json
	 * @param id
	 */
	public void updateIndex(String indexname, String type, Map<String, Object> json, String id) {
		XContentBuilder contentBuilder;
		try {
			contentBuilder = XContentFactory.jsonBuilder().startObject();
			for (Map.Entry<String, Object> entry : json.entrySet()) {
				contentBuilder.field(entry.getKey(), entry.getValue());
			}
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index(indexname);
			updateRequest.type(type);
			updateRequest.id(id);
			updateRequest.doc(contentBuilder.endObject());
			client.update(updateRequest).get();
		} catch (IOException e) {
			logger.error("/ElasticSearchClientService/updateIndex()-->", e);
		} catch (InterruptedException e) {
			logger.error("/ElasticSearchClientService/updateIndex()-->", e);
		} catch (ExecutionException e) {
			logger.error("/ElasticSearchClientService/updateIndex()-->", e);
		}
	}

	/**
	 * 
	 * @Title: createSearchRequestBuilder
	 * @author quwu
	 * @param typeList
	 * @param indexList
	 * @return
	 */
	public SearchRequestBuilder createSearchRequestBuilder(String[] typeList, String[] indexList) {
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexList);
		searchRequestBuilder.setTypes(typeList);
		return searchRequestBuilder;
	}

	/**
	 * 
	 * @Title: setQuery
	 * @author quwu
	 * @param searchRequestBuilder
	 * @param query
	 */
	public void setQuery(SearchRequestBuilder searchRequestBuilder, QueryBuilder query) {
		searchRequestBuilder.setQuery(query);
	}

	/**
	 * 
	 * @Title: searchIndex
	 * @author quwu
	 * @param typeList
	 * @param indexList
	 * @param query
	 * @param explainFlag
	 * @param sortMapList
	 * @param highlightFlag
	 * @param template
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	public SearchHits searchIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, Boolean explainFlag, List<Map<String, Integer>> sortMapList, Boolean highlightFlag, String template, int currentPage, int pageSize) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		this.addPage(searchRequestBuilder, currentPage, pageSize);
		if (highlightFlag)
			this.addHighlight(searchRequestBuilder);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}
	
	public SearchHits searchHighlighIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, Boolean explainFlag, List<Map<String, Integer>> sortMapList, Boolean highlightFlag, String template, int currentPage, int pageSize,QueryBuilder highlighQuery) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		this.addPage(searchRequestBuilder, currentPage, pageSize);
		if (highlightFlag)
			searchRequestBuilder.addHighlightedField(StringUtil.getColumnString("title"), 30, 1)
								.addHighlightedField(StringUtil.getColumnString("content"), 50, 3)
								.setHighlighterOrder("score").setHighlighterBoundaryMaxScan(20)
								.setHighlighterQuery(highlighQuery);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}

	/**
	 * 检索索引全部数据
	 * 
	 * @param typeList
	 * @param indexList
	 * @param fieldList
	 * @param query
	 * @param sortMapList
	 * @param highlightFlag
	 * @return
	 */
	public SearchHits searchAllIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, List<Map<String, Integer>> sortMapList, Boolean highlightFlag) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		if (highlightFlag)
			this.addHighlight(searchRequestBuilder);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}

	/**
	 * 检索索引，带有返回数据数量限制
	 * 
	 * @param typeList
	 * @param indexList
	 * @param fieldList
	 * @param query
	 * @param sortMapList
	 * @param highlightFlag
	 * @param from
	 * @param size
	 * @return
	 */
	public SearchHits searchRangeIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, List<Map<String, Integer>> sortMapList, Boolean highlightFlag, Integer from, Integer size) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		if (highlightFlag)
			this.addHighlight(searchRequestBuilder);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		searchRequestBuilder.setFrom(from);
		searchRequestBuilder.setSize(size);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}

	/**
	 * 分组查询索引（带有返回数据数量限制）
	 * 
	 * @param typeList
	 * @param indexList
	 * @param fieldList
	 * @param query
	 * @param group
	 * @param sortMapList
	 * @param highlightFlag
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	public SearchResponse searchAggsIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, AggregationBuilder<?> group, List<Map<String, Integer>> sortMapList, Boolean highlightFlag, int currentPage, int pageSize) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		searchRequestBuilder.setSearchType(SearchType.COUNT);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		this.addPage(searchRequestBuilder, currentPage, pageSize);
		if (highlightFlag)
			this.addHighlight(searchRequestBuilder);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		if (group != null)
			searchRequestBuilder.addAggregation(group);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchResponse sr = execute.actionGet();
		return sr;
	}

	/**
	 * 分组检索索引
	 * 
	 * @param typeList
	 * @param indexList
	 * @param fieldList
	 * @param query
	 * @param group
	 * @param sortMapList
	 * @param highlightFlag
	 * @return
	 */
	public SearchResponse searchAggsIndex(String[] typeList, String[] indexList, List<String> fieldList, QueryBuilder query, AggregationBuilder<?> group, List<Map<String, Integer>> sortMapList, Boolean highlightFlag) {
		SearchRequestBuilder searchRequestBuilder = this.createSearchRequestBuilder(typeList, indexList);
		searchRequestBuilder.setSearchType(SearchType.COUNT);
		this.setQuery(searchRequestBuilder, query);
		this.addSort(searchRequestBuilder, false, sortMapList);
		if (highlightFlag)
			this.addHighlight(searchRequestBuilder);
		if (fieldList != null)
			this.addFieldList(searchRequestBuilder, fieldList);
		if (group != null)
			searchRequestBuilder.addAggregation(group);
		ListenableActionFuture<SearchResponse> execute = searchRequestBuilder.execute();
		logger.info("send request json: " + searchRequestBuilder + "-----------");
		SearchResponse sr = execute.actionGet();
		return sr;
	}

	/**
	 * 文档相关度查询
	 * 
	 * @param index
	 * @param type
	 * @param fieldList
	 * @param eid
	 * @return
	 */
	public SearchHits searchMoreLikeThis(String index, String type, List<String> fieldList, String eid) {
		MoreLikeThisRequestBuilder searchRequestBuilder = new MoreLikeThisRequestBuilder(client, index, type, eid);
		ActionFuture<SearchResponse> execute = client.moreLikeThis(searchRequestBuilder.request());
		SearchHits hits = execute.actionGet().getHits();
		return hits;
	}

	/**
	 * 通过Eid获取索引名称
	 * 
	 * @param eid
	 * @return
	 */
	public String getIndexById(String eid) {
		SearchResponse searchResponse = client.prepareSearch(GeneratorIndexAndType.generatorAllIndex()).setTypes("doc").setQuery(QueryBuilders.termQuery("_id", eid)).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		String index = "";
		for (SearchHit hit : hits) {
			index = hit.getIndex();
		}
		return index;
	}

	/**
	 * 
	 * @Title: addFieldList
	 * @author quwu
	 * @param searchRequestBuilder
	 * @param fieldList
	 */
	public void addFieldList(SearchRequestBuilder searchRequestBuilder, List<String> fieldList) {
		for (String field : fieldList) {
			searchRequestBuilder.addField(field);
		}
	}

	/**
	 * 
	 * @Title: addSort
	 * @author quwu
	 * @param searchRequestBuilder
	 * @param explainFlag
	 * @param sortMapList
	 */
	public void addSort(SearchRequestBuilder searchRequestBuilder, Boolean explainFlag, List<Map<String, Integer>> sortMapList) {
		if (explainFlag == true)
			searchRequestBuilder.setExplain(explainFlag);
		else if (explainFlag == false && sortMapList != null) {
			for (Map<String, Integer> map : sortMapList) {
				for (Map.Entry<String, Integer> entry : map.entrySet()) {
					String field = entry.getKey();
					SortOrder order = SortOrder.DESC;
					if (entry.getValue() == 0) {
						order = SortOrder.DESC;
					} else if (entry.getValue() != 0) {
						order = SortOrder.ASC;
					}
					// 排序在这转createTime -> c_create_time
					searchRequestBuilder.addSort(StringUtil.getColumnString(field), order);
				}
			}
		}
	}

	/**
	 * 
	 * @Title: addPage
	 * @author quwu
	 * @param searchRequestBuilder
	 * @param currentPage
	 * @param pageSize
	 */
	public void addPage(SearchRequestBuilder searchRequestBuilder, int currentPage, int pageSize) {
		searchRequestBuilder.setFrom((currentPage - 1) * pageSize).setSize(pageSize);
	}

	/**
	 * 
	 * @Title: addHighlight
	 * @author zhangshuai
	 * @param searchRequestBuilder
	 */
	public void addHighlight(SearchRequestBuilder searchRequestBuilder) {
		searchRequestBuilder.addHighlightedField(StringUtil.getColumnString("title"), 30, 1).addHighlightedField(StringUtil.getColumnString("content"), 50, 3).setHighlighterOrder("score").setHighlighterBoundaryMaxScan(20);
	}

	/**
	 * 
	 * @Title: getHighlightFieldString
	 * @author zhangshuai
	 * @param summaryField
	 * @return
	 */
	public String getHighlightFieldString(HighlightField summaryField) {
		if (summaryField == null) {
			return null;
		}
		Text[] titleTexts = summaryField.fragments();
		String title = "";
		for (Text text : titleTexts) {
			title += text;
		}
		return title;
	}

	public SearchHitField getHitField(SearchHit hit, String fieldName) {
		return hit.field(StringUtil.getColumnString(fieldName));
	}

	public GetField getField(GetResponse res, String fieldName) {
		return res.getField(StringUtil.getColumnString(fieldName));
	}

	/**
	 * 
	 * @Title: getTotalPage
	 * @author zhangshuai
	 * @param totalCount
	 * @param pageSize
	 * @return
	 */
	public long getTotalPage(long totalCount, int pageSize) {
		long x = totalCount / pageSize;
		long s = totalCount % pageSize;
		long count = 0;

		if (x > 0 && s == 0) {
			count = x;
		} else if (x > 0 && s > 0) {
			count = x + 1;
		} else if (x <= 0) {
			count = 1;
		}
		return count;
	}

	/**
	 * 
	 * @Title:
	 * @Description: 删除索引
	 * @author zhangshuai
	 * @param totalCount
	 * @param pageSize
	 * @return
	 */
	public void deleteIndex(String indexname, String indexType, String id) {
		client.prepareDelete(indexname, indexType, id).execute().actionGet();
	}

	/*
	 * public static void main(String [] args){ ElasticSearchClientConfig
	 * clientConfig = new ElasticSearchClientConfig();
	 * ElasticSearchClientService esclientService = new
	 * ElasticSearchClientService(clientConfig);
	 * 
	 * Map<String,Object> json = new HashMap<String,Object>();
	 * 
	 * esclientService.createIndex("", "", json);
	 * 
	 * }
	 */
}