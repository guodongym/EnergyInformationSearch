package com.cnpc.geo.energyinformationsearch.web.timingTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.controller.BaseController;
import com.cnpc.geo.energyinformationsearch.base.entity.Contants;
import com.cnpc.geo.energyinformationsearch.base.util.GeneratorIndexAndType;
import com.cnpc.geo.energyinformationsearch.base.util.RedisUtil;
import com.cnpc.geo.energyinformationsearch.base.util.StringUtil;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.file.model.LogChnHot;
import com.cnpc.geo.energyinformationsearch.file.model.SynergyRecommend;

/**
 * @Description：智能推荐接口自动任务
 * @author chl
 * 
 */

@Controller
@Component
public class ZntjTask extends BaseController {

	@Autowired
	ElasticSearchClientService esClientService;

	private static Properties properties;
	static {
		InputStream resourceAsStream = ZntjTask.class.getResourceAsStream("/timingTask.properties");
		properties = new Properties();
		try {
			properties.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// @Scheduled(cron = "0/5 * * * * ? ")
	// 间隔5秒执行
	// public void taskCycle() {
	// for (int i = 0; i < 10; i++) {
	// System.out.println(i);
	// }
	// System.out.println("定时任务已经启动，每隔5秒打印！");
	// }

	// 热门文档定时任务（30分钟执行一次）
	// @Scheduled(cron = "0/10 * * * * ? ")
	 @Scheduled(cron = "0 0/30 * * * ? ")
	public void hotDocuments() {
		try {
			logger.info("热门文档定时任务启动");
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			// 时间筛选(一个月之内的数据)
			Long pubDateBegin = new Date().getTime() - 7 * 24 * 60 * 60 * 1000L;
			RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("c_time");
			rangeQueryBuilder.from(pubDateBegin);
			rangeQueryBuilder.to(new Date().getTime());
			boolQuery.must(rangeQueryBuilder);

			// 筛选下载和收藏的数据
			String[] operatings = { "DOWNLOAD", "COLLECT" };
			BoolQueryBuilder boolQueryOpearting = QueryBuilders.boolQuery();
			for (String type : operatings) {
				MatchQueryBuilder queryOperating = QueryBuilders.matchQuery("c_operating", type);
				boolQueryOpearting.should(queryOperating);
			}
			if (boolQueryOpearting != null) {
				boolQuery.must(boolQueryOpearting);
			}

			// 将eid分组
			TermsBuilder group = AggregationBuilders.terms("eidAgg").field("c_eid");
			group.size(Integer.MAX_VALUE);
			SearchResponse sr = esClientService.searchAggsIndex(new String[] { "log" }, new String[] { "log-chn-*" }, null, boolQuery, group, null, false);

			if (sr != null) {
				if (sr.getAggregations() != null) {
					Map<String, Aggregation> aggMap = sr.getAggregations().asMap();
					StringTerms eidTerms = (StringTerms) aggMap.get("eidAgg");
					Iterator<Bucket> eidBucketIt = eidTerms.getBuckets().iterator();
					if (eidBucketIt != null) {
						if (eidBucketIt.hasNext()) {
							while (eidBucketIt.hasNext()) {
								Bucket gradeBucket = eidBucketIt.next();
								LogChnHot lch = new LogChnHot();
								lch.setEid(gradeBucket.getKey().toUpperCase());
								lch.setCount(gradeBucket.getDocCount() + "");
								String indexJson = JSON.toJSONString(lch);
								String indexById = esClientService.getIndexById(gradeBucket.getKey().toUpperCase());
								if(StringUtils.isNotEmpty(indexById)){
									esClientService.createIndex(Contants.HOT_INDEX, "hot", indexJson, gradeBucket.getKey().toUpperCase());
								}
							}
							logger.info("检索成功,热门文档定时任务结束！");
						} else {
							logger.info("未检索到数据,热门文档定时任务结束！");
						}
					} else {
						logger.info("未检索到数据,热门文档定时任务结束！");
					}
				} else {
					logger.info("未检索到数据,热门文档定时任务结束！");
				}
			} else {
				logger.info("未检索到数据,热门文档定时任务结束！");
			}
		} catch (Exception e) {
			logger.info("热门文档定时任务出现异常：");
			e.printStackTrace();
		}
	}

	// 喜欢该文档的还喜欢定时任务
	// @Scheduled(cron = "0/10 * * * * ? ")
		@Scheduled(cron = "0/30 * * * * ? ")
	public void synergyRecommend() {
		logger.info("喜欢该文档的还喜欢定时任务启动");

		Calendar instance = Calendar.getInstance();
		int hour = instance.get(Calendar.HOUR_OF_DAY);

		String start = properties.getProperty("start");
		String end = properties.getProperty("end");

		if (hour < Integer.parseInt(start) || hour >= Integer.parseInt(end)) {
			return;
		}

		try {
			// 取出redis中一条数据，以下成为 主eid
			String eid = RedisUtil.pop("synergyRecommend");
			// 填充redis队列
			if (StringUtils.isEmpty(eid)) {
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				// 筛选下载和收藏的数据
				String[] operatings = { "DOWNLOAD", "COLLECT" };
				BoolQueryBuilder boolQueryOpearting = QueryBuilders.boolQuery();
				for (String type : operatings) {
					MatchQueryBuilder queryOperating = QueryBuilders.matchQuery("c_operating", type);
					boolQueryOpearting.should(queryOperating);
				}
				if (boolQueryOpearting != null) {
					boolQuery.must(boolQueryOpearting);
				}
				// 将eid分组
				TermsBuilder group = AggregationBuilders.terms("eidAgg").field("c_eid");
				group.size(Integer.MAX_VALUE);
				SearchResponse sr = esClientService.searchAggsIndex(new String[] { "log" }, new String[] { "log-chn-*" }, null, boolQuery, group, null, false);
				if (sr != null) {
					if (sr.getAggregations() != null) {
						Map<String, Aggregation> aggMap = sr.getAggregations().asMap();
						StringTerms eidTerms = (StringTerms) aggMap.get("eidAgg");
						Iterator<Bucket> eidBucketIt = eidTerms.getBuckets().iterator();
						if (eidBucketIt != null) {
							while (eidBucketIt.hasNext()) {
								Bucket gradeBucket = eidBucketIt.next();
								RedisUtil.push("synergyRecommend", gradeBucket.getKey());
							}
						}
					}
				}
				eid = RedisUtil.pop("synergyRecommend");
			}

			// 生成新的索引
			if (StringUtils.isNotEmpty(eid)) {
				// 查询改eid下的标题和内容
				String title = "";
				String content = "";
				String[] fields = { "c_title", "c_content" };
				String indexName = esClientService.getIndexById(eid.toUpperCase());
				if (StringUtils.isEmpty(indexName)) {
					return;
				}
				GetResponse getResponse = esClientService.get(indexName, "doc", eid.toUpperCase(), fields);
				if (getResponse.getField("c_title") != null)
					title = getResponse.getField("c_title").getValue().toString();
				if (getResponse.getField("c_content") != null)
					content = getResponse.getField("c_content").getValue().toString();
				// 结合title和content，以下做相关度使用
				String eidTitleContent = StringUtil.replaceBlank(title + content);

				if (eid != null) {
					// 定义用户组变量
					List<String> userList = new ArrayList<String>();
					// 通过eid查询所有收藏和下载这个eid的所有用户
					BoolQueryBuilder boolQueryUserByEid = QueryBuilders.boolQuery();
					// 筛选eid
					QueryStringQueryBuilder queryEid = new QueryStringQueryBuilder(eid);
					queryEid.field(StringUtil.getColumnString("eid"));
					boolQueryUserByEid.must(queryEid);
					// 筛选下载和收藏的数据
					String[] operatings = { "DOWNLOAD", "COLLECT" };
					BoolQueryBuilder boolQueryOpearting = QueryBuilders.boolQuery();
					for (String type : operatings) {
						MatchQueryBuilder queryOperating = QueryBuilders.matchQuery("c_operating", type);
						boolQueryOpearting.should(queryOperating);
					}
					if (boolQueryOpearting != null) {
						boolQueryUserByEid.must(boolQueryOpearting);
					}
					// 将user分组
					TermsBuilder groupUser = AggregationBuilders.terms("userAgg").field("c_user");
					groupUser.size(Integer.MAX_VALUE);
					SearchResponse srUserByEid = esClientService.searchAggsIndex(new String[] { "log" }, new String[] { "log-chn-*" }, null, boolQueryUserByEid, groupUser, null, false);
					// 将所用用户放到用户list中
					if (srUserByEid != null) {
						if (srUserByEid.getAggregations() != null) {
							Map<String, Aggregation> aggMap = srUserByEid.getAggregations().asMap();
							StringTerms userTerms = (StringTerms) aggMap.get("userAgg");
							Iterator<Bucket> userBucketIt = userTerms.getBuckets().iterator();
							if (userBucketIt != null) {
								while (userBucketIt.hasNext()) {
									Bucket gradeBucket = userBucketIt.next();
									userList.add(gradeBucket.getKey());
								}
							}
						}
					}

					// 找到所有用户下载和收藏过的eid
					if (!userList.isEmpty()) {
						// 定义eid组变量
						List<String> eidList = new ArrayList<String>();
						for (String user : userList) {
							// 通过单个user查询所有被该用户下载和收藏过的eid
							BoolQueryBuilder boolQueryEidsByUser = QueryBuilders.boolQuery();
							QueryStringQueryBuilder queryUser = new QueryStringQueryBuilder(user);
							queryUser.field("c_user");
							boolQueryEidsByUser.must(queryUser);
							// 筛选下载和收藏的数据
							String[] operatings1 = { "DOWNLOAD", "COLLECT" };
							BoolQueryBuilder boolQueryOpearting1 = QueryBuilders.boolQuery();
							for (String type : operatings1) {
								MatchQueryBuilder queryOperating = QueryBuilders.matchQuery("c_operating", type);
								boolQueryOpearting1.should(queryOperating);
							}
							if (boolQueryOpearting1 != null) {
								boolQueryEidsByUser.must(boolQueryOpearting1);
							}
							// 将eid分组
							TermsBuilder groupEid = AggregationBuilders.terms("eidAgg").field("c_eid");
							SearchResponse srEidByUser = esClientService.searchAggsIndex(new String[] { "log" }, new String[] { "log-chn-*" }, null, boolQueryEidsByUser, groupEid, null, false);
							if (srEidByUser != null) {
								if (srEidByUser.getAggregations() != null) {
									Map<String, Aggregation> aggMap = srEidByUser.getAggregations().asMap();
									StringTerms eidTerms = (StringTerms) aggMap.get("eidAgg");
									Iterator<Bucket> eidBucketIt = eidTerms.getBuckets().iterator();
									if (eidBucketIt != null) {
										while (eidBucketIt.hasNext()) {
											Bucket gradeBucket = eidBucketIt.next();
											eidList.add(gradeBucket.getKey());
										}
									}
								}
							}
						}
						// 删除eidList中的主Eid
						eidList.remove(eid);

						// 同过所有的Eid找出与主Eid相关的5条数据并创建索引
						if (!eidList.isEmpty()) {
							BoolQueryBuilder boolQueryTop5 = QueryBuilders.boolQuery();
							// 筛选EidList中的eid
							BoolQueryBuilder queryEids = QueryBuilders.boolQuery();
							for (String _eid : eidList) {
								MatchQueryBuilder queryRangeEid = QueryBuilders.matchQuery("_id", _eid.toUpperCase());
								queryEids.should(queryRangeEid);
							}
							if (queryEids != null) {
								boolQueryTop5.must(queryEids);
							}
							// 筛选与主Eid相关
							MoreLikeThisQueryBuilder more = new MoreLikeThisQueryBuilder("c_content", "c_title");
							more.likeText(eidTitleContent).analyzer("ik").minTermFreq(1).maxQueryTerms(100);
							boolQueryTop5.must(more);

							String[] fileTypes = { "data", "txt", "doc", "xls", "ppt", "pdf" };
							// 文件类型过滤
							BoolQueryBuilder boolQueryType = QueryBuilders.boolQuery();
							for (String type : fileTypes) {
								if (StringUtils.isNotEmpty(type)) {
									MatchQueryBuilder queryTypes = QueryBuilders.matchQuery("c_file_type", type);
									boolQueryType.should(queryTypes);
								}
							}
							if (boolQueryType != null) {
								boolQueryTop5.must(boolQueryType);
							}

							// 通过平均分排序
							List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
							Map<String, Integer> sortMap = new HashMap<String, Integer>();
							sortMap.put("scoreAvg", 0);
							sortMapList.add(sortMap);
							SearchHits hits = esClientService.searchRangeIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, null, boolQueryTop5, sortMapList, false, 0, 5);
							if (hits != null && hits.totalHits() > 0) {
								// 设置索引对象及索引数据
								SynergyRecommend srd = new SynergyRecommend();
								List<String> eidsTop5 = new ArrayList<String>();
								for (SearchHit hit : hits) {
									eidsTop5.add(hit.getId().toUpperCase());
								}
								srd.setEid(eid.toUpperCase());
								srd.setEids(StringUtils.join(eidsTop5, ","));
								String indexJson = JSON.toJSONString(srd);
								esClientService.createIndex(Contants.MORELIKE_INDEX, "like", indexJson, eid.toUpperCase());
							}
						}
					}
				}
				logger.info("检索成功,喜欢该文档的还喜欢定时任务结束！");
			} else {
				logger.info("检索完毕，未在redis里检索到数据！");
			}
		} catch (Exception e) {
			logger.info("喜欢该文档的还喜欢定时任务出现异常：");
			e.printStackTrace();
		}
	}
}