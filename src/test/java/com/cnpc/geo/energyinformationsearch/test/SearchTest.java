package com.cnpc.geo.energyinformationsearch.test;

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;

import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientConfig;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;


public class SearchTest {

	public void createIndex() {
		// ElasticSearchClientConfig clientConfig = new
		// ElasticSearchClientConfig();
		// clientConfig.setClusterName("elasticsearch");
		// clientConfig.setNodeList("192.168.47.140:9300");
		// clientConfig.setPingTimeOut("10s");
		// clientConfig.setSniff(true);
		// ElasticSearchClientService esclientService = new
		// ElasticSearchClientService(clientConfig);
		// Client client = esclientService.getClient();
		// List<String> suggestions = null;
		// try {
		// suggestions = new SuggestRequestBuilder(client).field("name")//
		// 查询field
		// .term("张")// 提示关键字
		// .size(10)// 返回结果数
		// .similarity(0.5f)// 相似度
		// .execute().actionGet().suggestions();
		// System.out.println(suggestions.get(0));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void searchTest() {
		ElasticSearchClientConfig clientConfig = new ElasticSearchClientConfig();
		clientConfig.setClusterName("elasticsearch-cluster2");
		clientConfig.setNodeList("10.2.45.204:9300");
		clientConfig.setPingTimeOut("10s");
		clientConfig.setSniff(true);
		ElasticSearchClientService esclientService = new ElasticSearchClientService(clientConfig);
		Client client = esclientService.getClient();
		CompletionSuggestionBuilder suggest = new CompletionSuggestionBuilder("song");
		suggest.field("suggest").size(5).text("张");
		SearchRequestBuilder request = client.prepareSearch("music").setTypes("song").setSearchType(SearchType.COUNT).addSuggestion(suggest);
		SearchResponse response = request.execute().actionGet();
		System.out.println(request);
		Suggest suggest1 = response.getSuggest();
		List<? extends Entry<? extends Option>> list = suggest1.getSuggestion("song").getEntries();
		for (int i = 0; i < list.size(); i++) {
			List<?> options = list.get(i).getOptions();
			for (int j = 0; j < options.size(); j++) {
				if (options.get(j) instanceof Option) {
					Option op = (Option) options.get(j);
					System.out.println(op.getScore());
					System.out.println(op.getText());
				}
			}
			System.out.println(list.get(i).getText() + "-------------");
		}
	}

	public static void main(String args[]) {
		ElasticSearchClientConfig clientConfig = new ElasticSearchClientConfig();
		clientConfig.setClusterName("elasticsearchtest");
		clientConfig.setNodeList("10.2.45.63:9300");
		clientConfig.setPingTimeOut("10s");
		clientConfig.setSniff(true);
		ElasticSearchClientService esclientService = new ElasticSearchClientService(clientConfig);
		TransportClient client = esclientService.getClient();
		SearchResponse actionGet = client.prepareSearch("query-chn-test").setTypes("query").setQuery(QueryBuilders.prefixQuery("c_key_word", "中国石")).addField("c_key_word").execute().actionGet();
		SearchHits hits = actionGet.getHits();
		for (SearchHit searchHit : hits) {
			System.out.println(searchHit.field("c_key_word").getValue().toString());
		}
		
//		TransportClient client = esclientService.getClient();
//		for(int i=0;i<Integer.MAX_VALUE;i=i+100){
//			SearchResponse actionGet = client.prepareSearch("doc-*").setTypes("doc").setQuery(QueryBuilders.matchAllQuery()).addField("c_file_type").setFrom(0).setSize(100).execute().actionGet();
//			SearchHits hits = actionGet.getHits();
//			for (SearchHit searchHit : hits) {
//				if("image".equals(searchHit.field("c_file_type").getValue().toString())){
//					Map<String, Object> hashMap = new HashMap<String, Object>();
//					hashMap.put("c_backup1", "2");
//					esclientService.updateIndex(searchHit.getIndex(), "doc", hashMap, searchHit.getId());
//				}else{
//					Map<String, Object> hashMap = new HashMap<String, Object>();
//					hashMap.put("c_backup1", "1");
//					esclientService.updateIndex(searchHit.getIndex(), "doc", hashMap, searchHit.getId());
//				}
//			}
//		}
		
	}
}