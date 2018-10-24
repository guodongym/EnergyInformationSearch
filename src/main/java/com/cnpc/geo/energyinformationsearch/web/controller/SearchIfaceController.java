package com.cnpc.geo.energyinformationsearch.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cnpc.geo.energyinformationsearch.base.controller.BaseController;
import com.cnpc.geo.energyinformationsearch.base.entity.CommonParameters;
import com.cnpc.geo.energyinformationsearch.base.entity.Contants;
import com.cnpc.geo.energyinformationsearch.base.util.GeneratorIndexAndType;
import com.cnpc.geo.energyinformationsearch.base.util.MD5Util;
import com.cnpc.geo.energyinformationsearch.base.util.StringUtil;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.file.model.CollctionFolderAndDoc;
import com.cnpc.geo.energyinformationsearch.file.model.CollctionFolderModel;
import com.cnpc.geo.energyinformationsearch.file.model.ResponseCollctionFolderModel;
import com.cnpc.geo.energyinformationsearch.search.entity.BehaviorReq;
import com.cnpc.geo.energyinformationsearch.search.entity.BehaviorRes;
import com.cnpc.geo.energyinformationsearch.search.entity.FileResult;
import com.cnpc.geo.energyinformationsearch.search.entity.RelativeSearchReq;
import com.cnpc.geo.energyinformationsearch.search.entity.RelativeSearchRes;
import com.cnpc.geo.energyinformationsearch.search.entity.SearchListReq;
import com.cnpc.geo.energyinformationsearch.search.entity.SearchListRes;
import com.cnpc.geo.energyinformationsearch.search.entity.SuperSearchListReq;
import com.cnpc.geo.energyinformationsearch.search.entity.SynergyRecommendRes;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.search.controller
 * @ClassName: SearchIfaceController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年6月29日 上午9:26:07
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年6月29日
 *                    上午9:26:07
 */

@Controller
@RequestMapping("/searchIface")
public class SearchIfaceController extends BaseController {

	Logger logger = Logger.getLogger(SearchIfaceController.class);
	@Autowired
	ElasticSearchClientService esClientService;

	@RequestMapping("/test.do")
	public void test() {
		String[] hids = { "F3EC90456A9D1B70E8251E02F2D54817", "0E94B1C9D67F72909CB4D696AE68CDAB", "DFD63D7429351C0C11353DD89ED03933", "06A8A0FAFB83C2AA40FDE236D9D09F39" };
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		MoreLikeThisQueryBuilder more = new MoreLikeThisQueryBuilder("c_content", "c_title");
		more.likeText("Omari").analyzer("ik").minTermFreq(1).maxQueryTerms(10);
		boolQuery.must(more);
		BoolQueryBuilder boolQuery2 = QueryBuilders.boolQuery();
		for (String hid : hids) {
			TermQueryBuilder queryHids = QueryBuilders.termQuery("c_hid", hid);
			boolQuery2.should(queryHids);
		}
		boolQuery.must(boolQuery2);
		List<String> fieldList1 = new ArrayList<String>();
		fieldList1.add("c_hid");
		fieldList1.add("c_title");
		SearchHits hits1 = esClientService.searchAllIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList1, boolQuery, null, false);
		if (hits1 != null && hits1.totalHits() > 0) {
			for (SearchHit hit : hits1) {
				if (!hit.fields().keySet().isEmpty()) {
					System.out.println(hit.field("c_hid").value().toString());
				}
			}
		}
	}

	// /**
	// *
	// * @Title: getTypeList
	// * @Description: TODO 获取类型列表
	// * @author louyujie
	// * @return
	// */
	// public ArrayList<String> getTypeList() {
	// // type 数组
	// ArrayList<String> typeList = new ArrayList<String>();
	// typeList.add("doc");
	// return typeList;
	// }

	// /**
	// *
	// * @Title: getIndexList
	// * @Description: TODO(这里用一句话描述这个方法的作用)
	// * @author louyujie
	// * @return
	// */
	// public ArrayList<String> getIndexList() {
	// // type 数组
	// ArrayList<String> indexList = new ArrayList<String>();
	// indexList.add("doc-chn-test");
	// return indexList;
	// }

	/**
	 * 
	 * @Title: addFlagQuery
	 * @Description: TODO 给文档加标记，被标记文档不被搜索到
	 * @author louyujie
	 * @param boolQuery
	 */
	public void addFlagQuery(BoolQueryBuilder boolQuery) {
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(StringUtil.getColumnString("lockStatus"), CommonParameters.LOCK);
		boolQuery.mustNot(termQueryBuilder);
	}

	/**
	 * 
	 * @Title: addTitleQuery
	 * @Description: TODO 添加-通过关键字查询title的条件
	 * @author louyujie
	 * @param boolQuery
	 * @param keyWord
	 */
	public void addTitleAndContentQuery(BoolQueryBuilder boolQuery, String keyWord) {
		QueryStringQueryBuilder keyWordQueryTitle = new QueryStringQueryBuilder(keyWord);
		keyWordQueryTitle.analyzer("ik_smart").field(StringUtil.getColumnString("title")).field(StringUtil.getColumnString("content"));
		// keyWordQueryTitle.analyzer("ik").field(StringUtil.getColumnString("title"));
		boolQuery.must(keyWordQueryTitle);
	}

	/**
	 * 
	 * @Title: addKeyWordQuery
	 * @Description: TODO 相关词搜索专用
	 * @author louyujie
	 * @param boolQuery
	 * @param keyWord
	 */
	public void addRelativeKeyWord(BoolQueryBuilder boolQuery, String keyWord) {
		BoolQueryBuilder boolQuerySub = QueryBuilders.boolQuery();
		QueryStringQueryBuilder keyWordQuery = new QueryStringQueryBuilder(keyWord);
		keyWordQuery.analyzer("ik").field(StringUtil.getColumnString("keyWord"));
		boolQuerySub.should(keyWordQuery);
		boolQuery.should(boolQuerySub);
	}

	/**
	 * 
	 * @Title: userQueryIndex
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @author quwu
	 * @param userId
	 * @param sessionId
	 * @param keyWord
	 */
	// public void userQueryIndex(String userId, String sessionId, String
	// keyWord) {
	//
	// String indexName = "query-chn-test";
	// String type = "query";
	//
	// QueryTemplate queryTemplate = new QueryTemplate();
	// queryTemplate.setKeyWord(keyWord);
	// queryTemplate.setSessionId(sessionId);
	// queryTemplate.setUserId(userId);
	// queryTemplate.setCreateTime(System.currentTimeMillis());
	// String indexJson = JSON.toJSONString(queryTemplate);
	// String eid = MD5Util.MD5(keyWord);
	// esClientService.createIndex(indexName, type, indexJson, eid);
	//
	// }

	/**
	 * 
	 * @Title: searchList
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @author quwu
	 * @param request
	 * @param response
	 */
	@RequestMapping("/searchList.do")
	public void searchList(HttpServletRequest request, HttpServletResponse response) {

		logger.info("execute--->/serchIface/searchList.do");

		Date startDate = new Date();
		SearchListRes searchListRes = new SearchListRes();
		String resultMsg = "执行searchList.do成功";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = null;// 会话ID
		String reqData = null;
		// String userId = "123";
		// String sessionId = "456";
		sid = request.getParameter("sid");
		reqData = request.getParameter("reqData");
		// userId = request.getParameter("userId");
		// sessionId = request.getParameter("sessionId");
		// logger.info("sid=" + sid);
		// logger.info("reqData=" + reqData);
		logger.info("id 是： " + sid + ", searchList列表查询的开始时间是：" + StringUtil.format(startDate));

		// 判断参数是否完整
		if (sid == null || reqData == null) {
			resultMsg = "参数不完整";
			resultCode = CommonParameters.resultCode_ERROR;
		} else {
			try {
				SearchListReq searchListReq = JSON.parseObject(reqData, SearchListReq.class);

				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				BoolQueryBuilder highlighQuery = QueryBuilders.boolQuery();

				// 被标记文档不被搜索到
				addFlagQuery(boolQuery);
				addFlagQuery(highlighQuery);

				// 搜索关键字
				String keyWord = searchListReq.getKeyWord();
				if (keyWord != null && !keyWord.equals("")) {
					String[] split = keyWord.split("\\s");
					List<String> arrayList = new ArrayList<>();
					for (int i = 0; i < split.length; i++) {
						if (StringUtils.isNotEmpty(split[i])) {
							arrayList.add(split[i]);
						}
					}
					if (arrayList.size() > 1) {
						for (String string : arrayList) {
							addTitleAndContentQuery(boolQuery, string);
							addTitleAndContentQuery(highlighQuery, string);
						}
					} else {
						addTitleAndContentQuery(boolQuery, keyWord);
						addTitleAndContentQuery(highlighQuery, keyWord);
					}
					// userQueryIndex(userId, sessionId, keyWord);
				}

				MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("key", keyWord).analyzer("ik_smart");

				List<String> keywordFieldList = new ArrayList<String>();
				keywordFieldList.add("key");
				keywordFieldList.add("value");
				SearchHits keywordHits = esClientService.searchAllIndex(new String[] { "keyword" }, new String[] { "keyword-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
				for (SearchHit hit : keywordHits) {
					String value = hit.field("value").getValue().toString();
					QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
					valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content")).field(StringUtil.getColumnString("title"));
					boolQuery.should(valueQuery);
				}

				SearchHits translationHits = esClientService.searchAllIndex(new String[] { "translation" }, new String[] { "translation-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
				for (SearchHit hit : translationHits) {
					String value = hit.field("value").getValue().toString();
					QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
					valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content"));
					boolQuery.should(valueQuery);
				}

				// 在结果中搜索关键字
				if (searchListReq.getOldKeyWord() != null && !searchListReq.getOldKeyWord().equals("")) {
					addTitleAndContentQuery(boolQuery, searchListReq.getOldKeyWord());
					// userQueryIndex(userId, sessionId,
					// searchListReq.getOldKeyWord());
				}
				// 语言筛选
				// if (searchListReq.getLanguageList() != null &&
				// searchListReq.getLanguageList().size() > 0) {
				// for (String language : searchListReq.getLanguageList()) {
				// if (language != null && !language.equals("")) {
				// TermQueryBuilder termQuery =
				// QueryBuilders.termQuery(StringUtil.getColumnString("language"),
				// language);
				// boolQuery.must(termQuery);
				// }
				// }
				// }
				// 数据来源分类筛选
				if (searchListReq.getSourceOriginalTypeList() != null && searchListReq.getSourceOriginalTypeList().size() > 0) {
					BoolQueryBuilder SourceOriginalTypeQuery = QueryBuilders.boolQuery();
					for (String sourceOriginalType : searchListReq.getSourceOriginalTypeList()) {
						if (sourceOriginalType != null && !sourceOriginalType.equals("")) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("sourceOriginalType"), sourceOriginalType);
							SourceOriginalTypeQuery.should(termQuery);
						}
					}
					if (SourceOriginalTypeQuery != null) {
						boolQuery.must(SourceOriginalTypeQuery);
					}
				}
				// 文档类型筛选
				if (searchListReq.getFileTypeList() != null && searchListReq.getFileTypeList().size() > 0) {
					BoolQueryBuilder fileTypeQuery = QueryBuilders.boolQuery();

					for (String fileType : searchListReq.getFileTypeList()) {
						if (fileType != null && !fileType.equals("")) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("fileType"), fileType);
							fileTypeQuery.should(termQuery);
							termQuery = null;
						}
					}
					if (fileTypeQuery != null) {
						boolQuery.must(fileTypeQuery);
					}
				} else {
					String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf", "html" };
					BoolQueryBuilder fileTypeQuery = QueryBuilders.boolQuery();

					for (String fileType : fileTypes) {
						if (fileType != null && !fileType.equals("")) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("fileType"), fileType);
							fileTypeQuery.should(termQuery);
							termQuery = null;
						}
					}
					if (fileTypeQuery != null) {
						boolQuery.must(fileTypeQuery);
					}
				}

				// 业务类型筛选
				if (searchListReq.getSourceCategoryTypeList() != null && searchListReq.getSourceCategoryTypeList().size() > 0) {
					BoolQueryBuilder SourceCategoryType = QueryBuilders.boolQuery();
					for (String sourceCategoryType : searchListReq.getSourceCategoryTypeList()) {
						if (sourceCategoryType != null && !sourceCategoryType.equals("")) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("sourceCategoryType"), sourceCategoryType);
							SourceCategoryType.should(termQuery);
						}
					}
					if (SourceCategoryType != null) {
						boolQuery.must(SourceCategoryType);
					}
				}

				// 时间塞选
				if ((searchListReq.getCreateTimeBegin() != null && searchListReq.getCreateTimeBegin() > 0) || (searchListReq.getCreateTimeEnd() != null && searchListReq.getCreateTimeEnd() > 0)) {
					RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(StringUtil.getColumnString("createTime"));
					if (searchListReq.getCreateTimeBegin() != null && searchListReq.getCreateTimeBegin() > 0) {
						rangeQueryBuilder.from(searchListReq.getCreateTimeBegin());
					}
					if (searchListReq.getCreateTimeEnd() != null && searchListReq.getCreateTimeEnd() > 0) {
						rangeQueryBuilder.to(searchListReq.getCreateTimeEnd());
					}
					boolQuery.must(rangeQueryBuilder);
				}

				// Field list
				List<String> fieldList = new ArrayList<String>();

				fieldList.add(StringUtil.getColumnString("hid"));
				fieldList.add(StringUtil.getColumnString("title"));
				fieldList.add(StringUtil.getColumnString("content"));
				fieldList.add(StringUtil.getColumnString("createTime"));
				fieldList.add(StringUtil.getColumnString("downloadCount"));
				fieldList.add(StringUtil.getColumnString("viewCount"));
				fieldList.add(StringUtil.getColumnString("uri"));
				fieldList.add(StringUtil.getColumnString("fileType"));
				fieldList.add(StringUtil.getColumnString("collectCount"));

				// 排序
				List<Map<String, Integer>> sortMapList = searchListReq.getSortMapList();
				// HashMap<String, Integer> hashMap = new HashMap<String,
				// Integer>();
				// hashMap.put("backup1", 1);
				// sortMapList.add(hashMap);
				// 默认排序
				// boolean explainFlag = true;
				// if (sortMapList != null && sortMapList.size() > 0)
				// explainFlag = false;

				SearchHits hits = esClientService.searchHighlighIndex(GeneratorIndexAndType.generatorDocType(), GeneratorIndexAndType.generatorDocIndex(searchListReq.getLanguageList()), fieldList, boolQuery, true, sortMapList, true, "template", searchListReq.getCurrentPage(), searchListReq.getPageSize(), highlighQuery);
				// 总命中数
				long totalHits = hits.getTotalHits();

				searchListRes.setTotalCount(totalHits);
				searchListRes.setCurrentPage(searchListReq.getCurrentPage());
				searchListRes.setTotalPage(esClientService.getTotalPage(totalHits, searchListReq.getPageSize()));

				List<FileResult> data = searchListRes.getData();

				for (SearchHit hit : hits) {
					Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
					FileResult fileResult = new FileResult();

					HighlightField titleHighlightField = highlightFieldMap.get(StringUtil.getColumnString("title"));
					HighlightField contentHighlightField = highlightFieldMap.get(StringUtil.getColumnString("content"));

					String title = null;
					String summary = null;
					title = esClientService.getHighlightFieldString(titleHighlightField);

					// 如果高亮中没有关键字
					if (title == null) {
						SearchHitField titleField = esClientService.getHitField(hit, "title");

						if (titleField != null) {
							String titleNoHighlight = titleField.getValue();
							title = titleNoHighlight;
						}
					}

					summary = esClientService.getHighlightFieldString(contentHighlightField);

					fileResult.setEid(hit.getId());

					fileResult.setTitle(StringUtil.replaceBlank(title));
					fileResult.setSummary(summary);
					SearchHitField hidField = esClientService.getHitField(hit, "hid");
					if (hidField != null) {
						String hid = hidField.getValue();
						fileResult.setHid(hid);
					}

					SearchHitField fileTypeField = esClientService.getHitField(hit, "fileType");
					if (fileTypeField != null) {
						String fileType = fileTypeField.getValue();
						fileResult.setFileType(fileType);
					}

					SearchHitField downloadCountField = esClientService.getHitField(hit, "downloadCount");
					if (downloadCountField != null) {
						int downloadCount = downloadCountField.getValue();
						fileResult.setDownloadCount(downloadCount);
					}

					SearchHitField collectCountField = esClientService.getHitField(hit, "collectCount");
					if (collectCountField != null) {
						int collectCount = collectCountField.getValue();
						fileResult.setCollectCount(collectCount);
					}

					SearchHitField viewCountField = esClientService.getHitField(hit, "viewCount");
					if (viewCountField != null) {
						int viewCount = viewCountField.getValue();
						fileResult.setViewCount(viewCount);
					}

					SearchHitField uriField = esClientService.getHitField(hit, "uri");
					if (uriField != null) {
						String uri = uriField.getValue();
						fileResult.setUri(uri);
					}

					SearchHitField collectCount = esClientService.getHitField(hit, "collectCount");
					if (collectCount != null) {
						Integer collect = Integer.valueOf(collectCount.getValue().toString());
						fileResult.setCollectCount(collect);
					}

					SearchHitField createTimeField = esClientService.getHitField(hit, "createTime");
					if (createTimeField != null) {
						Long createTime = Long.parseLong(createTimeField.getValue().toString());
						fileResult.setCreateTime(createTime);
					}
					data.add(fileResult);
					searchListRes.setData(data);
				}
			} catch (Exception e) {
				resultMsg = "内部处理异常";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.error("searchList列表查询出现异常--->", e);
			}
		}

		Date endDate = new Date();
		logger.info("id 是：" + sid + ", searchList列表查询的结束时间是：" + StringUtil.format(endDate));
		logger.info("id 是：" + sid + ", searchList列表查询的执行时间是：" + StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);

	}

	// @RequestMapping("/testIndex.do")
	// public String testIndex() {
	// FileTemplate fileTemplate = new FileTemplate();
	// for (int i = 0; i < 10; i++) {
	// fileTemplate.setId(UUID.randomUUID().toString());
	// fileTemplate.setTitle("中国石油");
	// fileTemplate.setContent("中国石油 大庆油田 克拉玛依油田 天然气");
	// fileTemplate.setCreatetime(System.currentTimeMillis() + 86400000);
	// fileTemplate.setDowncount(1000);
	// fileTemplate.setViewcount(1000);
	// fileTemplate.setFileformat("word");
	// fileTemplate.setUri("http://www.baidu.com");
	// String indexJson = JSON.toJSONString(fileTemplate);
	// esClientService.createIndex("doc-chn-test", "doc", indexJson);
	// }
	// System.out.println("------");
	// return "ok";
	// }

	/**
	 * 
	 * @Title: getBrowseTOPTen
	 * @Description: TODO 相关浏览TOP10
	 * @author louyujie
	 * @param request
	 * @param response
	 */
	@RequestMapping("browseTopList.do")
	public void browseTopList(HttpServletRequest request, HttpServletResponse response) {
		logger.info("获取相关浏览TOP10");
		Date startDate = new Date();
		String resultMsg = "browseTopList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		logger.info("reqData=" + reqData);
		SearchListRes searchListRes = new SearchListRes();
		SearchListReq searchListReq = null;
		logger.info("id 是：" + sid + ", 获取相关浏览top10接口的开始时间是：" + StringUtil.format(startDate));
		try {
			searchListReq = JSON.parseObject(reqData, SearchListReq.class);
			String keyWord = searchListReq.getKeyWord();
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("viewCount"), 0);
			boolQuery.mustNot(termQuery);

			// 被标记文档不被搜索到
			addFlagQuery(boolQuery);

			// 搜索关键字
			// if (StringUtils.isNotEmpty(keyWord)) {
			// addTitleAndContentQuery(boolQuery, keyWord);
			// }

			// 搜索关键字
			if (keyWord != null && !keyWord.equals("")) {
				String[] split = keyWord.split("\\s");
				List<String> arrayList = new ArrayList<>();
				for (int i = 0; i < split.length; i++) {
					if (StringUtils.isNotEmpty(split[i])) {
						arrayList.add(split[i]);
					}
				}
				if (arrayList.size() > 1) {
					for (String string : arrayList) {
						addTitleAndContentQuery(boolQuery, string);
					}
				} else {
					addTitleAndContentQuery(boolQuery, keyWord);
				}
			}

			MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("key", keyWord).analyzer("ik_smart");

			List<String> keywordFieldList = new ArrayList<String>();
			keywordFieldList.add("key");
			keywordFieldList.add("value");
			SearchHits keywordHits = esClientService.searchAllIndex(new String[] { "keyword" }, new String[] { "keyword-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
			for (SearchHit hit : keywordHits) {
				String value = hit.field("value").getValue().toString();
				QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
				valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content")).field(StringUtil.getColumnString("title"));
				boolQuery.should(valueQuery);
			}

			SearchHits translationHits = esClientService.searchAllIndex(new String[] { "translation" }, new String[] { "translation-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
			for (SearchHit hit : translationHits) {
				String value = hit.field("value").getValue().toString();
				QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
				valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content"));
				boolQuery.should(valueQuery);
			}

			String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
			// 文件类型过滤
			BoolQueryBuilder boolQueryType = QueryBuilders.boolQuery();
			for (String type : fileTypes) {
				if (StringUtils.isNotEmpty(type)) {
					MatchQueryBuilder queryType = QueryBuilders.matchQuery("c_file_type", type);
					boolQueryType.should(queryType);
				}
			}
			if (boolQueryType != null) {
				boolQuery.must(boolQueryType);
			}
			// Field list
			List<String> fieldList = new ArrayList<String>();
			fieldList.add(StringUtil.getColumnString("hid"));
			fieldList.add(StringUtil.getColumnString("uri"));
			fieldList.add(StringUtil.getColumnString("title"));
			fieldList.add(StringUtil.getColumnString("content"));
			fieldList.add(StringUtil.getColumnString("viewCount"));
			fieldList.add(StringUtil.getColumnString("fileType"));
			List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
			Map<String, Integer> sort = new HashMap<String, Integer>();
			sort.put("viewCount", 0);
			sortMapList.add(sort);
			SearchHits hits = esClientService.searchRangeIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, boolQuery, sortMapList, false, 0, 10);

			List<FileResult> data = new ArrayList<FileResult>();
			if (hits != null && hits.totalHits() > 0) {
				for (SearchHit hit : hits) {
					FileResult fileResult = new FileResult();
					fileResult.setEid(hit.getId());
					fileResult.setHid(hit.field(StringUtil.getColumnString("hid")).getValue().toString());
					fileResult.setTitle(StringUtil.replaceBlank(hit.field(StringUtil.getColumnString("title")).getValue().toString()));
					fileResult.setUri(hit.field(StringUtil.getColumnString("uri")).getValue().toString());
					fileResult.setFileType(hit.field(StringUtil.getColumnString("fileType")).getValue().toString());
					int viewCount = Integer.valueOf("null".equals(hit.field(StringUtil.getColumnString("viewCount"))) || hit.field(StringUtil.getColumnString("viewCount")) == null ? "0" : hit.field(StringUtil.getColumnString("viewCount")).getValue().toString());
					fileResult.setViewCount(viewCount);
					data.add(fileResult);
				}
				searchListRes.setData(data);
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "检索成功";
				logger.info("检索结果共" + hits.getTotalHits() + "条");
			} else {
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "无检索结果";
				logger.info("无检索结果");
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索异常";
			logger.error("相关浏览TOP10检索失败：");
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 获取相关浏览top10接口的结束时间是：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ",获取相关浏览top10接口的执行时间是：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);
			searchListRes = null;
		}
	}

	/**
	 * 
	 * @Title: downloadTopList
	 * @Description: TODO 下载TOP10
	 * @author louyujie
	 * @param request
	 * @param response
	 */
	@RequestMapping("downloadTopList.do")
	public void downloadTopList(HttpServletRequest request, HttpServletResponse response) {
		logger.info("下载TOP10");
		Date startDate = new Date();
		String resultMsg = "downloadTopList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		logger.info("reqData=" + reqData);
		SearchListRes searchListRes = new SearchListRes();
		logger.info("id 是：" + sid + ", 获取相关下载top10接口的开始时间是：" + StringUtil.format(startDate));
		try {
			SearchListReq searchListReq = JSON.parseObject(reqData, SearchListReq.class);
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			TermQueryBuilder termQuery = QueryBuilders.termQuery(StringUtil.getColumnString("downloadCount"), 0);
			boolQuery.mustNot(termQuery);

			String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
			// 文件类型过滤
			BoolQueryBuilder boolQueryType = QueryBuilders.boolQuery();
			for (String type : fileTypes) {
				if (StringUtils.isNotEmpty(type)) {
					MatchQueryBuilder queryType = QueryBuilders.matchQuery("c_file_type", type);
					boolQueryType.should(queryType);
				}
			}
			if (boolQueryType != null) {
				boolQuery.must(boolQueryType);
			}

			// 搜索关键字
			// if (searchListReq.getKeyWord() != null &&
			// !searchListReq.getKeyWord().equals("")) {
			// addTitleAndContentQuery(boolQuery, searchListReq.getKeyWord());
			// logger.info("相关下载TOP10的关键词 :" + searchListReq.getKeyWord());
			// }

			String keyWord = searchListReq.getKeyWord();

			// 被标记文档不被搜索到
			addFlagQuery(boolQuery);

			// 搜索关键字
			// if (StringUtils.isNotEmpty(keyWord)) {
			// addTitleAndContentQuery(boolQuery, keyWord);
			// }

			// 搜索关键字
			if (keyWord != null && !keyWord.equals("")) {
				String[] split = keyWord.split("\\s");
				List<String> arrayList = new ArrayList<>();
				for (int i = 0; i < split.length; i++) {
					if (StringUtils.isNotEmpty(split[i])) {
						arrayList.add(split[i]);
					}
				}
				if (arrayList.size() > 1) {
					for (String string : arrayList) {
						addTitleAndContentQuery(boolQuery, string);
					}
				} else {
					addTitleAndContentQuery(boolQuery, keyWord);
				}
			}

			MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("key", keyWord).analyzer("ik_smart");

			List<String> keywordFieldList = new ArrayList<String>();
			keywordFieldList.add("key");
			keywordFieldList.add("value");
			SearchHits keywordHits = esClientService.searchAllIndex(new String[] { "keyword" }, new String[] { "keyword-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
			for (SearchHit hit : keywordHits) {
				String value = hit.field("value").getValue().toString();
				QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
				valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content")).field(StringUtil.getColumnString("title"));
				boolQuery.should(valueQuery);
			}

			SearchHits translationHits = esClientService.searchAllIndex(new String[] { "translation" }, new String[] { "translation-chn-2015" }, keywordFieldList, matchQueryBuilder, null, false);
			for (SearchHit hit : translationHits) {
				String value = hit.field("value").getValue().toString();
				QueryStringQueryBuilder valueQuery = new QueryStringQueryBuilder(value);
				valueQuery.analyzer("ik_smart").field(StringUtil.getColumnString("content"));
				boolQuery.should(valueQuery);
			}

			// Field list
			List<String> fieldList = new ArrayList<String>();
			fieldList.add(StringUtil.getColumnString("hid"));
			fieldList.add(StringUtil.getColumnString("uri"));
			fieldList.add(StringUtil.getColumnString("title"));
			fieldList.add(StringUtil.getColumnString("content"));
			fieldList.add(StringUtil.getColumnString("downloadCount"));
			fieldList.add(StringUtil.getColumnString("fileType"));
			List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
			Map<String, Integer> sort = new HashMap<String, Integer>();
			sort.put("downloadCount", 0);
			sortMapList.add(sort);
			SearchHits hits = esClientService.searchRangeIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, boolQuery, sortMapList, false, 0, 10);

			List<FileResult> data = new ArrayList<FileResult>();
			Integer i = 0;
			if (hits != null && hits.totalHits() > 0) {
				for (SearchHit hit : hits) {
					i++;
					FileResult fileResult = new FileResult();
					fileResult.setEid(hit.getId());
					fileResult.setHid(hit.field(StringUtil.getColumnString("hid")).getValue().toString());
					try {
						fileResult.setTitle(StringUtil.replaceBlank(hit.field(StringUtil.getColumnString("title")).getValue().toString()));
					} catch (Exception e) {
						fileResult.setTitle(hit.field(StringUtil.getColumnString("content")).getValue().toString().substring(0, 30));
					}
					int downloadCount = Integer.valueOf("null".equals(hit.field(StringUtil.getColumnString("downloadCount"))) || hit.field(StringUtil.getColumnString("downloadCount")) == null ? "0" : hit.field(StringUtil.getColumnString("downloadCount")).getValue().toString());
					fileResult.setDownloadCount(downloadCount);
					fileResult.setUri(hit.field(StringUtil.getColumnString("uri")).getValue().toString());
					fileResult.setFileType(hit.field(StringUtil.getColumnString("fileType")).getValue().toString());
					data.add(fileResult);
					if (i == 10) {
						break;
					}
				}
				searchListRes.setCurrentPage(searchListReq.getCurrentPage());
				searchListRes.setTotalCount(hits.getTotalHits());
				searchListRes.setTotalPage(esClientService.getTotalPage(hits.totalHits(), searchListReq.getPageSize()));
				searchListRes.setData(data);
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "检索成功";
				logger.info("检索结果共" + hits.getTotalHits() + "条");
			} else {
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "无检索结果";
				logger.info("无检索结果");
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索失败";
			logger.error("下载TOP10检索异常：");
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 获取相关下载top10接口的结束时间是：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 获取相关下载top10接口的执行时间是：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);
		}
	}

	/**
	 * 
	 * @Title: relativeSearch
	 * @Description: TODO 相关词搜索
	 * @author louyujie
	 * @param request
	 * @param response
	 */
	@RequestMapping("/relativeWord.do")
	public void relativeSearch(HttpServletRequest request, HttpServletResponse response) {
		logger.info("执行相关词搜索");
		Date startDate = new Date();
		String resultMsg = "relativeSearch.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		logger.info("reqData=" + reqData);
		RelativeSearchRes relativeSearchRes = new RelativeSearchRes();
		logger.info("id 是： " + sid + ", 相关词搜索的开始时间为：" + StringUtil.format(startDate));
		try {
			RelativeSearchReq relativeSearchReq = JSON.parseObject(reqData, RelativeSearchReq.class);
			// BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			// if (relativeSearchReq.getKeyWord() != null &&
			// !("").equals(relativeSearchReq.getKeyWord())) {
			// addRelativeKeyWord(boolQuery, relativeSearchReq.getKeyWord());
			// }
			// List<String> fieldList = new ArrayList<String>();
			// fieldList.add(StringUtil.getColumnString("keyWord"));
			// SearchHits hits = esClientService.searchIndex(new String[] {
			// "query" }, new String[] { "query-chn-test" }, fieldList,
			// boolQuery, true, null, false, "template", 1,
			// relativeSearchReq.getPageSize());

			List<String> data = relativeSearchRes.getRelativeList();
			Set<String> set = new HashSet<String>();
			MatchQueryBuilder boolQuery = null;
			if (relativeSearchReq.getKeyWord() != null && !("").equals(relativeSearchReq.getKeyWord())) {
				boolQuery = QueryBuilders.matchQuery(StringUtil.getColumnString("title"), relativeSearchReq.getKeyWord()).analyzer("ik_smart");
			}

			List<String> fieldList = new ArrayList<String>();
			fieldList.add(StringUtil.getColumnString("keyWord"));

			SignificantTermsBuilder group = AggregationBuilders.significantTerms("searchresults").field("c_title");
			group.size(relativeSearchReq.getPageSize());
			SearchResponse sr = esClientService.searchAggsIndex(new String[] { "doc" }, new String[] { "doc-*" }, null, boolQuery, group, null, false, 1, relativeSearchReq.getPageSize() - 1);
			if (sr != null) {
				if (sr.getAggregations() != null) {
					SignificantTerms titleTerms = sr.getAggregations().get("searchresults");
					if (titleTerms != null && titleTerms.getBuckets().size() > 0) {
						int count = 0;
						for (Bucket b : titleTerms.getBuckets()) {
							count++;
							if (count > relativeSearchReq.getPageSize()) {
								break;
							}
							String key = b.getKey().trim();
							if (key.length() != 1 && !key.equals(relativeSearchReq.getKeyWord())) {
								set.add(key);
							}
						}
						Iterator<String> iterator = set.iterator();
						while (iterator.hasNext()) {
							data.add(iterator.next().toString());
						}
						resultCode = CommonParameters.resultCode_OK;
						logger.info("检索结果共" + titleTerms.getBuckets().size() + "条");
					} else {
						resultMsg = "无检索结果";
						resultCode = CommonParameters.resultCode_OK;
						logger.info("无检索结果");
					}
				}
			}
			// List<String> data = relativeSearchRes.getRelativeList();
			// Set<String> set = new HashSet<String>();
			// if (hits != null && hits.totalHits() > 0) {
			// for (SearchHit hit : hits) {
			// set.add(hit.field(StringUtil.getColumnString("keyWord")).getValue().toString().trim());
			// }
			// Iterator<String> iterator = set.iterator();
			// while (iterator.hasNext()) {
			// data.add(iterator.next().toString());
			// }
			// resultCode = CommonParameters.resultCode_OK;
			// logger.info("检索结果共" + hits.getTotalHits() + "条");
			// } else {
			// resultMsg = "无检索结果";
			// resultCode = CommonParameters.resultCode_OK;
			// logger.info("无检索结果");
			// }
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索异常";
			logger.error("检索异常");
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是： " + sid + ", 相关词搜索的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是： " + sid + ", 相关词搜索执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, relativeSearchRes, request, response);
		}
	}

	/**
	 * @Title synergyRecommend
	 * @Description:喜欢该文档的还喜欢TOP5
	 * @param request
	 * @param response
	 */
	@RequestMapping("/synergyRecommend.do")
	public void synergyRecommend(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String resultMsg = "synergyRecommend.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		SynergyRecommendRes srRes = new SynergyRecommendRes();
		logger.info("id是 ：" + sid + ", 习惯该文档的还喜欢top5的开始时间为：" + StringUtil.format(startDate));
		try {
			String eid = request.getParameter("eid");

			String eidStr = "";
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			QueryStringQueryBuilder queryEid = new QueryStringQueryBuilder(eid);
			queryEid.field("c_eid");
			boolQuery.must(queryEid);
			List<String> fieldList = new ArrayList<String>();
			fieldList.add("c_eids");
			SearchHits hits = esClientService.searchAllIndex(new String[] { "like" }, new String[] { Contants.MORELIKE_INDEX }, fieldList, boolQuery, null, false);
			if (hits != null && hits.totalHits() > 0) {
				for (SearchHit hit : hits) {
					if (!hit.fields().keySet().isEmpty()) {
						if (hit.field("c_eids") != null) {
							eidStr = hit.field("c_eids").getValue().toString();
						}
					}
				}
			}

			if (StringUtils.isNotEmpty(eidStr)) {
				List<FileResult> data = new ArrayList<FileResult>();
				String[] eids = StringUtils.split(eidStr, ",");
				for (String _eid : eids) {
					FileResult fr = new FileResult();
					String[] fields = { "c_title", "c_view_count", "c_file_total_page", "c_score_avg", "c_hid", "c_file_type" };
					String indexName = esClientService.getIndexById(_eid.toUpperCase());
					if (StringUtils.isNotEmpty(indexName)) {
						GetResponse getResponse = esClientService.get(indexName, "doc", _eid.toUpperCase(), fields);
						fr.setEid(_eid.toUpperCase());
						if (getResponse.getField("c_title") != null) {
							fr.setTitle(StringUtil.replaceBlank(getResponse.getField("c_title").getValue().toString()));
						}
						if (getResponse.getField("c_view_count") != null) {
							fr.setViewCount(Integer.valueOf(getResponse.getField("c_view_count").getValue().toString()));
						}
						if (getResponse.getField("c_file_total_page") != null) {
							fr.setFileTotalPage(Integer.valueOf(getResponse.getField("c_file_total_page").getValue().toString()));
						}
						if (getResponse.getField("c_score_avg") != null) {
							fr.setScoreAvg(Double.valueOf(getResponse.getField("c_score_avg").getValue().toString()));
						}
						if (getResponse.getField("c_hid") != null) {
							fr.setHid(getResponse.getField("c_hid").getValue().toString());
						}
						if (getResponse.getField("c_file_type") != null) {
							fr.setFileType(getResponse.getField("c_file_type").getValue().toString());
						}
						data.add(fr);
					}
				}
				srRes.setData(data);
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "检索成功";
				logger.info("检索成功");
			} else {
				resultCode = CommonParameters.resultCode_ERROR;
				resultMsg = "喜欢该文档的还喜欢TOP5:未检索到数据";
				logger.info("检索成功,喜欢该文档的还喜欢TOP5未检索到数据");
			}

			// srReq = JSON.parseObject(reqData, SynergyRecommendReq.class);
			// String hid = srReq.getHid();
			//
			// // 查询该文档的信息
			// BoolQueryBuilder boolFileQuery = QueryBuilders.boolQuery();
			// QueryStringQueryBuilder queryThisHid = new
			// QueryStringQueryBuilder(hid);
			// queryThisHid.field(StringUtil.getColumnString("hid"));
			// boolFileQuery.must(queryThisHid);
			// List<String> fieldList = new ArrayList<String>();
			// fieldList.add("c_content");
			// SearchHits hits =
			// esClientService.searchAllIndex(GeneratorIndexAndType.generatorDocType(),
			// new String[] { GeneratorIndexAndType.generatorAllIndex() },
			// fieldList, boolFileQuery, null, false);
			//
			// if (hits != null && hits.totalHits() > 0) {
			// for (SearchHit hit : hits) {
			// if (!hit.fields().keySet().isEmpty()) {
			// hidContent = hit.field("c_content").getValue().toString();
			// }
			// }
			// }
			//
			// // 浏览过该文档的所有用户
			// BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			//
			// QueryStringQueryBuilder queryHid = new
			// QueryStringQueryBuilder(hid);
			// queryHid.field("hid");
			// boolQuery.must(queryHid);
			//
			// String[] operatings = { "PREVIEW_FILE" };
			// for (String type : operatings) {
			// MatchQueryBuilder queryOperating =
			// QueryBuilders.matchQuery("operating", type);
			// boolQuery.should(queryOperating);
			// }
			// TermsBuilder group =
			// AggregationBuilders.terms("userAgg").field("user");
			// SearchResponse sr = esClientService.searchAggsIndex(new String[]
			// { "log" }, new String[] { "log-chn-*" }, null, boolQuery, group,
			// null, false);
			// if (sr != null) {
			// List<String> users = new ArrayList<String>();
			// Map<String, Aggregation> userAgg = sr.getAggregations().asMap();
			// StringTerms userTerms = (StringTerms) userAgg.get("userAgg");
			// Iterator<Bucket> userBucketIt =
			// userTerms.getBuckets().iterator();
			// while (userBucketIt.hasNext()) {
			// Bucket gradeBucket = userBucketIt.next();
			// users.add(gradeBucket.getKey());
			// }
			// // 所有用户的收藏及下载过的所有文档
			// BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
			// for (String user : users) {
			// MatchQueryBuilder queryUsers = QueryBuilders.matchQuery("user",
			// user);
			// boolQuery.should(queryUsers);
			// }
			// String[] operatings1 = { "DOWNLOAD", "COLLECT" };
			// for (String type : operatings1) {
			// MatchQueryBuilder queryOperating =
			// QueryBuilders.matchQuery("operating", type);
			// boolQuery.should(queryOperating);
			// }
			// TermsBuilder group1 =
			// AggregationBuilders.terms("hidAgg").field("hid");
			// SearchResponse sr1 = esClientService.searchAggsIndex(new String[]
			// { "log" }, new String[] { "log-chn-*" }, null, boolQuery1,
			// group1, null, false);
			// if (sr1 != null) {
			// List<String> hids = new ArrayList<String>();
			// Map<String, Aggregation> hidAgg = sr.getAggregations().asMap();
			// StringTerms hidTerms = (StringTerms) hidAgg.get("hidAgg");
			// Iterator<Bucket> hidBucketIt = hidTerms.getBuckets().iterator();
			// while (hidBucketIt.hasNext()) {
			// Bucket gradeBucket = hidBucketIt.next();
			// hids.add(gradeBucket.getKey());
			// }
			// hids.remove(hid);
			// // 所有文档中与hid该文档的相关度最高的文档
			// BoolQueryBuilder boolQuery2 = QueryBuilders.boolQuery();
			// for (String hid1 : hids) {
			// MatchQueryBuilder queryHids = QueryBuilders.matchQuery("hid",
			// hid1);
			// boolQuery2.should(queryHids);
			// }
			// MoreLikeThisQueryBuilder more = new
			// MoreLikeThisQueryBuilder("c_title", "c_content");
			// more.likeText(hidContent).analyzer("ik").minTermFreq(1).maxQueryTerms(10);
			// boolQuery2.must(more);
			// List<String> fieldList1 = new ArrayList<String>();
			// fieldList1.add("c_hid");
			// fieldList1.add("c_title");
			// fieldList1.add("c_content");
			// fieldList1.add("c_summary");
			// fieldList1.add("c_download_count");
			// fieldList1.add("c_view_count");
			// fieldList1.add("c_uri");
			// fieldList1.add("c_file_type");
			// fieldList1.add("c_create_time");
			// fieldList.add(StringUtil.getColumnString("scoreCount"));
			// fieldList.add(StringUtil.getColumnString("scoreSum"));
			// SearchHits hits1 =
			// esClientService.searchAllIndex(GeneratorIndexAndType.generatorDocType(),
			// new String[] { GeneratorIndexAndType.generatorAllIndex() },
			// fieldList1, boolQuery2, null, false);
			// List<FileResult> data = new ArrayList<FileResult>();
			// if (hits1 != null && hits1.totalHits() > 0) {
			// for (SearchHit hit : hits1) {
			// if (!hit.fields().keySet().isEmpty()) {
			// FileResult fileResult = new FileResult();
			// if (hit.field("c_score_count") != null) {
			// fileResult.setScoreCount(Double.valueOf(hit.field("c_score_count").getValue().toString()));
			// }
			// if (hit.field("c_hid") != null) {
			// fileResult.setHid(hit.field("c_hid").getValue().toString());
			// }
			// if (hit.field("c_title") != null) {
			// fileResult.setTitle(hit.field("c_title").getValue().toString());
			// }
			// if (hit.field("c_content") != null) {
			// fileResult.setContent(hit.field("c_content").getValue().toString());
			// }
			// if (hit.field("c_summary") != null) {
			// fileResult.setSummary(hit.field("c_summary").getValue().toString());
			// }
			// if (hit.field("c_download_count") != null) {
			// fileResult.setDownloadCount(Integer.valueOf(hit.field("c_download_count").getValue().toString()));
			// }
			// if (hit.field("c_view_count") != null) {
			// fileResult.setViewCount(Integer.valueOf(hit.field("c_view_count").getValue().toString()));
			// }
			// if (hit.field("c_uri") != null) {
			// fileResult.setUri(hit.field("c_uri").getValue().toString());
			// }
			// if (hit.field("c_file_type") != null) {
			// fileResult.setFileType(hit.field("c_file_type").getValue().toString());
			// }
			// if (hit.field("c_create_time") != null) {
			// fileResult.setCreateTime(Long.valueOf(hit.field("c_create_time").getValue().toString()));
			// }
			// data.add(fileResult);
			// }
			// }
			// }
			// srRes.setData(getTopFive(data));
			// }
			// }
			// resultCode = CommonParameters.resultCode_OK;
			// resultMsg = "检索成功";
			// logger.info("检索成功");
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索异常：";
			logger.error("检索异常", e);
		} finally {
			Date endDate = new Date();
			logger.info("id是 ：" + sid + ", 习惯该文档的还喜欢top5的结束时间为：" + StringUtil.format(endDate));
			logger.info("id是 ：" + sid + ", 习惯该文档的还喜欢top5的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, srRes, request, response);
		}
	}

	/**
	 * 获取评分最高的5条记录
	 * 
	 * @param data
	 * @return
	 */
	public List<FileResult> getTopFive(List<FileResult> data) {
		List<FileResult> newData = new ArrayList<FileResult>();
		if (data.size() <= 5) {
			return data;
		} else {
			for (int i = 0; i < 5; i++) {
				newData.add(new FileResult());
			}
			for (int i = 0; i < data.size() - 1; i++) {
				for (int j = i + 1; j < data.size(); j++) {
					if (i >= 5) {
						break;
					}
					FileResult fr = new FileResult();
					if (data.get(i).getScoreCount() < data.get(j).getScoreCount()) {
						fr = data.get(i);
						data.set(i, data.get(j));
						data.set(j, fr);
						newData.set(i, data.get(i));
					}
					newData.set(j, data.get(j));
				}
			}
			return newData;
		}
	}

	/**
	 * 
	 * @Title: behavior
	 * @Description: 你可能还喜欢的文档TOP5
	 * @author zhaoguodong
	 * @param request
	 * @param response
	 */
	@RequestMapping("/behavior.do")
	public void behavior(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String resultMsg = "behavior.do";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		BehaviorReq behaviorReq = null;
		BehaviorRes behaviorRes = new BehaviorRes();

		String title = "";
		String content = "";
		logger.info("id 是：" + sid + "你可能还喜欢的文档TOP5接口的开始时间为：" + StringUtil.format(startDate));
		try {
			behaviorReq = JSON.parseObject(reqData, BehaviorReq.class);
			String eid = behaviorReq.getEid();
			String keyWord = behaviorReq.getKeyWord();

			// 获取文档的标题和内容
			String[] fields = { "c_title", "c_content" };
			String indexName = esClientService.getIndexById(eid);
			GetResponse getResponse = esClientService.get(indexName, "doc", eid, fields);
			title = getResponse.getField("c_title").getValue().toString();
			content = getResponse.getField("c_content").getValue().toString();

			// 查询关键字所匹配的文档
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			// 匹配关键字
			if (StringUtils.isNotEmpty(keyWord)) {
				QueryStringQueryBuilder keyWordQuery = new QueryStringQueryBuilder(keyWord);
				keyWordQuery.field("c_keywords");
				boolQuery.must(keyWordQuery);
			}

			// 筛选类型
			String[] operatings = { "PREVIEW_FILE" };
			BoolQueryBuilder queryTypes = QueryBuilders.boolQuery();
			for (String type : operatings) {
				MatchQueryBuilder queryOperating = QueryBuilders.matchQuery("c_operating", type);
				queryTypes.should(queryOperating);
			}
			if (queryTypes != null) {
				boolQuery.must(queryTypes);
			}

			TermsBuilder group = AggregationBuilders.terms("eidAgg").field("c_eid");
			SearchResponse sr = esClientService.searchAggsIndex(new String[] { "log" }, new String[] { "log-chn-*" }, null, boolQuery, group, null, false, 1, 40);
			if (sr != null) {
				if (sr.getAggregations() != null) {
					if (sr.getAggregations().get("eidAgg") != null) {
						StringTerms eidTerms = sr.getAggregations().get("eidAgg");
						// StringTerms eidTerms = eidAgg.get("eidAgg");
						Iterator<org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket> eidBucketIt = eidTerms.getBuckets().iterator();
						List<String> eids = new ArrayList<String>();
						while (eidBucketIt.hasNext()) {
							org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket gradeBucket = eidBucketIt.next();
							eids.add(gradeBucket.getKey().toUpperCase());
						}
						eids.remove(eid);

						if (!eids.isEmpty()) {
							BoolQueryBuilder boolQueryNew = QueryBuilders.boolQuery();

							BoolQueryBuilder boolQueryEid = QueryBuilders.boolQuery();
							for (String _eid : eids) {
								MatchQueryBuilder queryEid = QueryBuilders.matchQuery("_id", _eid);
								boolQueryEid.should(queryEid);
							}
							if (boolQueryEid != null) {
								boolQueryNew.must(boolQueryEid);
							}

							MoreLikeThisQueryBuilder more = new MoreLikeThisQueryBuilder("c_content", "c_title");
							more.likeText(StringUtil.replaceBlank(title + content)).analyzer("ik").minTermFreq(1);
							boolQuery.must(more);
							String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
							// 文件类型过滤
							BoolQueryBuilder boolQueryType = QueryBuilders.boolQuery();
							for (String type : fileTypes) {
								if (StringUtils.isNotEmpty(type)) {
									MatchQueryBuilder termQuery = QueryBuilders.matchQuery("c_file_type", type);
									boolQueryType.should(termQuery);
								}
							}
							if (boolQueryType != null) {
								boolQueryNew.must(boolQueryType);
							}
							List<String> fieldList1 = new ArrayList<String>();
							fieldList1.add("c_hid");
							fieldList1.add("c_title");
							fieldList1.add("c_file_total_page");
							fieldList1.add(StringUtil.getColumnString("fileType"));
							fieldList1.add(StringUtil.getColumnString("createTime"));
							fieldList1.add(StringUtil.getColumnString("scoreAvg"));
							List<Map<String, Integer>> sortMapList1 = new ArrayList<Map<String, Integer>>();
							Map<String, Integer> sortMap1 = new HashMap<String, Integer>();
							sortMap1.put("createTime", 0);
							sortMap1.put("scoreAvg", 0);
							sortMapList1.add(sortMap1);
							SearchHits hits1 = esClientService.searchRangeIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList1, boolQueryNew, sortMapList1, false, 0, 5);
							List<FileResult> data = new ArrayList<FileResult>();
							if (hits1 != null && hits1.totalHits() > 0) {
								for (SearchHit hit : hits1) {
									FileResult fr = new FileResult();
									if (!hit.fields().keySet().isEmpty()) {
										fr.setEid(hit.getId());
										if (hit.field("c_hid") != null) {
											fr.setHid(hit.field("c_hid").getValue().toString());
										}
										if (hit.field("c_title") != null) {
											fr.setTitle(StringUtil.replaceBlank(hit.field("c_title").getValue().toString()));
										}
										if (hit.field(StringUtil.getColumnString("fileType")) != null) {
											fr.setFileType(hit.field(StringUtil.getColumnString("fileType")).getValue().toString());
										}
										if (hit.field(StringUtil.getColumnString("createTime")) != null) {
											fr.setCreateTime(Long.valueOf(hit.field(StringUtil.getColumnString("createTime")).getValue().toString()));
										}
										if (hit.field(StringUtil.getColumnString("scoreAvg")) != null) {
											fr.setScoreAvg(Double.valueOf(hit.field(StringUtil.getColumnString("scoreAvg")).getValue().toString()));
										}
										if (hit.field("c_file_total_page") != null) {
											fr.setFileTotalPage(Integer.valueOf(hit.field("c_file_total_page").getValue().toString()));
										}
									}
									data.add(fr);
								}
							}
							behaviorRes.setData(data);
							resultCode = CommonParameters.resultCode_OK;
							resultMsg = "检索成功";
							logger.info("数据检索成功");
						} else {
							resultCode = CommonParameters.resultCode_ERROR;
							resultMsg = "检索完成，没有数据";
							logger.info("数据检索成功,没有数据");
						}
					}
				}
			}

			// behaviorReq = JSON.parseObject(reqData, BehaviorReq.class);
			//
			// BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			//
			// // 用户ID筛选
			// if (StringUtils.isNotBlank(userId)) {
			// TermQueryBuilder termQuery = QueryBuilders.termQuery("c_user_id",
			// userId);
			// boolQuery.must(termQuery);
			// } else {
			// TermQueryBuilder termQuery =
			// QueryBuilders.termQuery("c_session_id", sessionId);
			// boolQuery.must(termQuery);
			// }
			// // Field list
			// List<String> list = new ArrayList<String>();
			// list.add("id");
			// list.add("c_key_word");
			//
			// SearchHits hits = esClientService.searchIndex(new String[] {
			// "query" }, new String[] { "query-chn-test" }, list, boolQuery,
			// true, null, true, null, 1, 10);
			//
			// String likeKeyWord = "";
			// for (SearchHit hit : hits) {
			// likeKeyWord += hit.field("c_key_word").getValue();
			// }
			//
			// BoolQueryBuilder likeBoolQuery = QueryBuilders.boolQuery();
			//
			// if (StringUtils.isNotBlank(likeKeyWord)) {
			// QueryStringQueryBuilder keyWordQuery = new
			// QueryStringQueryBuilder(likeKeyWord);
			// keyWordQuery.analyzer("ik").field("c_title").field("c_content");
			// likeBoolQuery.must(keyWordQuery);
			// likeBoolQuery.mustNot(QueryBuilders.termQuery("id",
			// behaviorReq.getEid()));
			// }
			// likeBoolQuery.mustNot(QueryBuilders.termQuery("c_file_type",
			// "html"));
			// likeBoolQuery.mustNot(QueryBuilders.termQuery("c_file_type",
			// "image"));
			// likeBoolQuery.mustNot(QueryBuilders.termQuery("c_file_type",
			// "data"));
			//
			// // Field list
			// List<String> fieldList = new ArrayList<String>();
			// fieldList.add("id");
			// fieldList.add("c_hid");
			// fieldList.add("c_title");
			// fieldList.add("c_download_count");
			// fieldList.add("c_view_count");
			// fieldList.add("c_uri");
			// fieldList.add("c_file_type");
			// fieldList.add("c_create_time");
			// fieldList.add("c_score_sum");
			// fieldList.add("c_score_count");
			// fieldList.add("c_file_total_page");
			// int pageSize = behaviorReq.getPageSize() > 0 ?
			// behaviorReq.getPageSize() : 5;
			// SearchHits likeHits =
			// esClientService.searchIndex(GeneratorIndexAndType.generatorDocType(),
			// new String[] { GeneratorIndexAndType.generatorAllIndex() },
			// fieldList, likeBoolQuery, true, null, true, null, 1, pageSize);
			// ArrayList<FileResult> fileResultList = new
			// ArrayList<FileResult>();
			// for (SearchHit searchHit : likeHits) {
			// Map<String, HighlightField> highlightFieldMap =
			// searchHit.getHighlightFields();
			// HighlightField contentHighlightField =
			// highlightFieldMap.get("c_content");
			//
			// String summary =
			// esClientService.getHighlightFieldString(contentHighlightField);
			//
			// Double scoreSum =
			// Double.valueOf("null".equals(searchHit.field("c_score_sum")) ||
			// searchHit.field("c_score_sum") == null ? "0" :
			// searchHit.field("c_score_sum").getValue().toString());
			// Double scoreCount =
			// Double.valueOf("null".equals(searchHit.field("c_score_count")) ||
			// searchHit.field("c_score_count") == null ? "0" :
			// searchHit.field("c_score_count").getValue().toString());
			//
			// int downloadCount =
			// Integer.valueOf("null".equals(searchHit.field("c_download_count"))
			// || searchHit.field("c_download_count") == null ? "0" :
			// searchHit.field("c_download_count").getValue().toString());
			// int viewCount =
			// Integer.valueOf("null".equals(searchHit.field("c_view_count")) ||
			// searchHit.field("c_view_count") == null ? "0" :
			// searchHit.field("c_view_count").getValue().toString());
			//
			// int fileTotalPage =
			// Integer.valueOf("null".equals(searchHit.field("c_file_total_page"))
			// || searchHit.field("c_file_total_page") == null ? "0" :
			// searchHit.field("c_file_total_page").getValue().toString());
			//
			// FileResult fileResult = new FileResult();
			// fileResult.setEid(searchHit.getId());
			// fileResult.setHid(searchHit.field("c_hid").getValue().toString());
			// fileResult.setTitle(searchHit.field("c_title").getValue().toString());
			// fileResult.setSummary(summary);
			// fileResult.setDownloadCount(downloadCount);
			// fileResult.setViewCount(viewCount);
			// fileResult.setUri(searchHit.field("c_uri").getValue().toString());
			// fileResult.setFileType(searchHit.field("c_file_type").getValue().toString());
			// fileResult.setCreateTime(Long.valueOf(searchHit.field("c_create_time").getValue().toString()));
			// fileResult.setFileTotalPage(fileTotalPage);
			// Double scoreAvg = 0.00;
			// if (scoreCount != 0) {
			// scoreAvg = (double) (Math.round((scoreSum / scoreCount) * 10.0) /
			// 10.0);
			// }
			// fileResult.setScoreAvg(scoreAvg);
			// fileResultList.add(fileResult);
			// }
			// behaviorRes.setData(fileResultList);
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索异常：";
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + "你可能还喜欢的文档TOP5接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + "你可能还喜欢的文档TOP5接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, behaviorRes, request, response);
		}
	}

	/**
	 * 
	 * @Title: relativeFileList
	 * @Description: TODO 相关文档Top5
	 * @author louyujie
	 * @param request
	 * @param response
	 */
	@RequestMapping("/relativeFileList.do")
	public void relativeFileList(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info("执行相关文档搜索  relativeFileList---->>relativeFileList()");
		String resultMsg = "relativeFileList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		// logger.info("reqData=" + reqData);
		SearchListReq slReq = null;
		SearchListRes searchListRes = new SearchListRes();
		String title = null;
		// String content = null;
		logger.info("id 是：" + sid + ", 相关文档Top5开始的时间为：" + StringUtil.format(startDate));
		try {
			slReq = JSONObject.parseObject(reqData, SearchListReq.class);
			String eid = slReq.getEid();
			String keyWord = slReq.getKeyWord();
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			// 获取文档标题和内容
			String[] fields = { "c_title", "c_content" };
			String indexName = esClientService.getIndexById(eid);
			GetResponse getResponse = esClientService.get(indexName, "doc", eid, fields);
			if (getResponse.getField("c_title") != null) {
				title = getResponse.getField("c_title").getValue().toString();
				// content =
				// getResponse.getField("c_content").getValue().toString();
			}

			MoreLikeThisQueryBuilder more = new MoreLikeThisQueryBuilder("c_title");
			more.likeText(StringUtil.replaceBlank(title)).analyzer("ik_smart").minTermFreq(1).maxQueryTerms(100);
			if (more != null) {
				boolQuery.must(more);
			}

			boolQuery.mustNot(QueryBuilders.termQuery("_id", eid.toUpperCase()));
			// 关键字搜索
			if (keyWord != null && !keyWord.equals("")) {
				String[] split = keyWord.split("\\s");
				List<String> arrayList = new ArrayList<>();
				for (int i = 0; i < split.length; i++) {
					if (StringUtils.isNotEmpty(split[i])) {
						arrayList.add(split[i]);
					}
				}
				if (arrayList.size() > 1) {
					for (String string : arrayList) {
						// QueryStringQueryBuilder keyWordQueryTitle = new
						// QueryStringQueryBuilder(string);
						// keyWordQueryTitle.analyzer("ik_smart").field(StringUtil.getColumnString("title"));
						// boolQuery.must(keyWordQueryTitle);
						addTitleAndContentQuery(boolQuery, string);
					}
				} else {
					// QueryStringQueryBuilder keyWordQueryTitle = new
					// QueryStringQueryBuilder(keyWord);
					// keyWordQueryTitle.analyzer("ik_smart").field(StringUtil.getColumnString("title"));
					// boolQuery.must(keyWordQueryTitle);
					addTitleAndContentQuery(boolQuery, keyWord);
				}
			}

			// BoolQueryBuilder boolQuerySub = QueryBuilders.boolQuery();
			// if (StringUtils.isNotEmpty(keyWord)) {
			// MatchQueryBuilder keyWordQueryTitle =
			// QueryBuilders.matchQuery(StringUtil.getColumnString("title"),
			// keyWord);
			// MatchQueryBuilder keyWordQueryContent =
			// QueryBuilders.matchQuery(StringUtil.getColumnString("content"),
			// keyWord);
			// boolQuerySub.should(keyWordQueryTitle);
			// boolQuerySub.should(keyWordQueryContent);
			// }
			// if (boolQuerySub != null) {
			// boolQuery.must(boolQuerySub);
			// }

			String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
			// 文件类型过滤
			BoolQueryBuilder boolQueryType = QueryBuilders.boolQuery();
			for (String type : fileTypes) {
				if (StringUtils.isNotEmpty(type)) {
					MatchQueryBuilder termQuery = QueryBuilders.matchQuery("c_file_type", type);
					boolQueryType.should(termQuery);
				}
			}
			if (boolQueryType != null) {
				boolQuery.must(boolQueryType);
			}

			List<String> fieldList = new ArrayList<String>();
			fieldList.add(StringUtil.getColumnString("hid"));
			fieldList.add(StringUtil.getColumnString("title"));
			fieldList.add(StringUtil.getColumnString("content"));
			fieldList.add(StringUtil.getColumnString("downloadCount"));
			fieldList.add(StringUtil.getColumnString("viewCount"));
			fieldList.add(StringUtil.getColumnString("uri"));
			fieldList.add(StringUtil.getColumnString("fileType"));
			fieldList.add(StringUtil.getColumnString("createTime"));
			fieldList.add(StringUtil.getColumnString("scoreAvg"));
			
			// 被标记文档不被搜索到
			addFlagQuery(boolQuery);

			List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
			Map<String, Integer> mapList = new HashMap<String, Integer>();
			mapList.put("scoreAvg", 0);
			sortMapList.add(mapList);
			SearchHits hits = esClientService.searchRangeIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, boolQuery, sortMapList, false, 0, 10);
			int count = 0;
			List<FileResult> data = new ArrayList<FileResult>();
			if (hits != null && hits.totalHits() > 0) {
				for (SearchHit hit : hits) {
					count++;
					FileResult fr = new FileResult();
					if (!hit.fields().keySet().isEmpty()) {
						fr.setEid(hit.getId());
						if (hit.field("c_hid") != null) {
							fr.setHid(hit.field("c_hid").getValue().toString());
						}
						if (hit.field("c_title") != null) {
							fr.setTitle(StringUtil.replaceBlank(hit.field("c_title").getValue().toString()));
						}
						if (hit.field("c_content") != null) {
							fr.setContent(hit.field("c_content").getValue().toString());
						}
						if (hit.field("c_content") != null) {
							fr.setSummary(hit.field("c_content").getValue().toString());
						}
						if (hit.field(StringUtil.getColumnString("downloadCount")) != null) {
							fr.setDownloadCount(Integer.valueOf(hit.field(StringUtil.getColumnString("downloadCount")).getValue().toString()));
						}
						if (hit.field(StringUtil.getColumnString("viewCount")) != null) {
							fr.setViewCount(Integer.valueOf(hit.field(StringUtil.getColumnString("viewCount")).getValue().toString()));
						}
						if (hit.field("c_uri") != null) {
							fr.setUri(hit.field("c_uri").getValue().toString());
						}
						if (hit.field(StringUtil.getColumnString("fileType")) != null) {
							fr.setFileType(hit.field(StringUtil.getColumnString("fileType")).getValue().toString());
						}
						if (hit.field(StringUtil.getColumnString("createTime")) != null) {
							fr.setCreateTime(Long.valueOf(hit.field(StringUtil.getColumnString("createTime")).getValue().toString()));
						}
						if (hit.field(StringUtil.getColumnString("scoreAvg")) != null) {
							fr.setScoreAvg(Double.valueOf(hit.field(StringUtil.getColumnString("scoreAvg")).getValue().toString()));
						}
					}
					data.add(fr);
					if (count >= 5) {
						break;
					}
				}
				searchListRes.setData(data);
				resultCode = CommonParameters.resultCode_OK;
				// logger.info("检索成功！");
			} else {
				resultMsg = "无检索结果";
				logger.info("无检索结果");
			}
			// List<String> fieldList = new ArrayList<String>();
			// fieldList.add(StringUtil.getColumnString("hid"));
			// fieldList.add(StringUtil.getColumnString("title"));
			// fieldList.add(StringUtil.getColumnString("content"));
			// fieldList.add(StringUtil.getColumnString("uri"));
			// fieldList.add(StringUtil.getColumnString("fileType"));
			// fieldList.add(StringUtil.getColumnString("scoreCount"));
			// fieldList.add(StringUtil.getColumnString("scoreSum"));
			// SearchHits hits =
			// esClientService.searchMoreLikeThis(GeneratorIndexAndType.generatorAllIndex(),
			// "doc", fieldList, eid);
			// int count = 0;
			// Double scoreAvg = 0.00;
			// FileResult fileResult = null;
			// List<FileResult> data = new ArrayList<FileResult>();
			// if (hits != null && hits.totalHits() > 0) {
			// for (SearchHit hit : hits) {
			// String fileType =
			// hit.getSource().get(StringUtil.getColumnString("fileType")).toString();
			// if (StringUtils.equals(fileType, "html")) {
			// continue;
			// }
			// count = count + 1;
			// if (count <= pageSize) {
			// fileResult = new FileResult();
			// fileResult.setEid(hit.getId());
			// fileResult.setHid(hit.getSource().get(StringUtil.getColumnString("hid")).toString());
			// try {
			// fileResult.setTitle(StringUtil.replaceBlank(hit.getSource().get(StringUtil.getColumnString("title")).toString()));
			// } catch (Exception e) {
			// fileResult.setTitle(hit.getSource().get(StringUtil.getColumnString("content")).toString().substring(0,
			// 30));
			// }
			// fileResult.setUri(hit.getSource().get(StringUtil.getColumnString("uri")).toString());
			// fileResult.setFileType(fileType);
			// String scoreSum = "null".equals(hit.field("c_score_sum")) ||
			// hit.field("c_score_sum") == null ? "0" :
			// hit.field("c_score_sum").getValue().toString();
			// String scoreCount = "null".equals(hit.field("c_score_count")) ||
			// hit.field("c_score_count") == null ? "0" :
			// hit.field("c_score_count").getValue().toString();
			//
			// if (Integer.parseInt(scoreCount) != 0 &&
			// Integer.parseInt(scoreSum) != 0) {
			// scoreAvg = (double) (Math.round((Double.parseDouble(scoreSum) /
			// Double.parseDouble(scoreCount)) * 10.0) / 10.0);
			// }
			// fileResult.setScoreAvg(scoreAvg);
			// data.add(fileResult);
			// } else {
			// resultCode = CommonParameters.resultCode_OK;
			// logger.info("返回相关文档搜索");
			// }
			// }
			// searchListRes.setData(data);
			// } else {
			// resultMsg = "无检索结果";
			// logger.info("无检索结果");
			// }
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("检索异常：", e);
			resultMsg = "检索异常";
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 相关文档Top5结束的时间为：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 相关文档Top5执行的时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);
		}
	}

	/**
	 * 
	 * @Title: superSearchList
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @author quwu
	 * @param request
	 * @param response
	 */
	@RequestMapping("/superSearchList.do")
	public void superSearchList(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info("execute--->/serchIface/superSearchList.do");
		SearchListRes searchListRes = new SearchListRes();
		String resultMsg = "执行superSearchList.do成功";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");

		// logger.info("sid=" + sid);
		// logger.info("reqData=" + reqData);

		logger.info("id 是：" + sid + ",  高级查询开始的时间为：" + StringUtil.format(startDate));
		// 判断参数是否完整
		if (sid == null || reqData == null) {
			resultMsg = "参数不完整";
			resultCode = CommonParameters.resultCode_ERROR;
		} else {
			try {
				SuperSearchListReq superSearchListReq = JSON.parseObject(reqData, SuperSearchListReq.class);
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				// oldKeyWord二次搜索
				if (superSearchListReq.getOldKeyWord() != null && !"".equals(superSearchListReq.getOldKeyWord())) {
					QueryStringQueryBuilder keyWordQueryTitle = new QueryStringQueryBuilder(superSearchListReq.getOldKeyWord());
					keyWordQueryTitle.analyzer("ik").field(StringUtil.getColumnString("title"));

					QueryStringQueryBuilder keyWordQueryContent = new QueryStringQueryBuilder(superSearchListReq.getOldKeyWord());
					keyWordQueryTitle.analyzer("ik").field(StringUtil.getColumnString("content"));

					boolQuery.should(keyWordQueryTitle);
					boolQuery.should(keyWordQueryContent);
				}

				// 搜索title
				if (superSearchListReq.getTitleBool() != null && superSearchListReq.getTitleBool().size() > 0) {
					Iterator<Entry<String, Integer>> iterator = superSearchListReq.getTitleBool().entrySet().iterator();
					queryString(boolQuery, iterator, "title");
				}

				// 搜索内容
				if (superSearchListReq.getContentBool() != null && superSearchListReq.getContentBool().size() > 0) {
					Iterator<Entry<String, Integer>> iterator = superSearchListReq.getContentBool().entrySet().iterator();
					queryString(boolQuery, iterator, "content");
				}

				// 搜索开始时间与结束时间
				if (superSearchListReq.getPublishDateRangeBool() != null && superSearchListReq.getPublishDateRangeBool().size() > 0) {
					Iterator<Entry<String, Integer>> iterator = superSearchListReq.getPublishDateRangeBool().entrySet().iterator();
					rangeQuery(boolQuery, iterator, "publishDate");
				}
				// 搜索author
				if (superSearchListReq.getAuthorBool() != null && superSearchListReq.getAuthorBool().size() > 0) {
					Iterator<Entry<String, Integer>> iterator = superSearchListReq.getAuthorBool().entrySet().iterator();
					termQuery(boolQuery, iterator, "author");
				}
				// 搜索fileTypeListBool
				if (superSearchListReq.getFileTypeListBool() != null && superSearchListReq.getFileTypeListBool().size() > 0) {
					Iterator<Entry<List<String>, Integer>> iterator = superSearchListReq.getFileTypeListBool().entrySet().iterator();
					termQueryList(boolQuery, iterator, "fileType");
				}

				// 搜索sourceOriginalTypeListBool
				if (superSearchListReq.getSourceOriginalTypeListBool() != null && superSearchListReq.getSourceOriginalTypeListBool().size() > 0) {
					Iterator<Entry<List<String>, Integer>> iterator = superSearchListReq.getSourceOriginalTypeListBool().entrySet().iterator();
					termQueryList(boolQuery, iterator, "sourceOriginalType");
				}

				// 搜索createTimeRangeBool
				if (superSearchListReq.getCreateTimeRangeBool() != null && superSearchListReq.getCreateTimeRangeBool().size() > 0) {
					Iterator<Entry<String, Integer>> iterator = superSearchListReq.getCreateTimeRangeBool().entrySet().iterator();
					rangeQuery(boolQuery, iterator, "createTime");
				}

				List<String> fieldList = new ArrayList<String>();
				fieldList.add(StringUtil.getColumnString("hid"));
				fieldList.add(StringUtil.getColumnString("title"));
				fieldList.add(StringUtil.getColumnString("content"));
				fieldList.add(StringUtil.getColumnString("createTime"));
				fieldList.add(StringUtil.getColumnString("downloadCount"));
				fieldList.add(StringUtil.getColumnString("viewCount"));
				fieldList.add(StringUtil.getColumnString("uri"));
				fieldList.add(StringUtil.getColumnString("fileType"));
				fieldList.add(StringUtil.getColumnString("collectCount"));

				// 默认排序
				// boolean explainFlag = true;
				List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
				Map<String, Integer> sortMap = new HashMap<String, Integer>();
				String sortWay = superSearchListReq.getSortWay();
				if (StringUtils.isNotEmpty(sortWay)) {
					sortMap.put("createTime", 0);
					sortMapList.add(sortMap);
				}
				SearchHits hits = esClientService.searchIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, boolQuery, true, sortMapList, true, null, superSearchListReq.getCurrentPage(), superSearchListReq.getPageSize());
				// 总命中数
				long totalHits = hits.getTotalHits();
				searchListRes.setTotalCount(totalHits);
				searchListRes.setCurrentPage(superSearchListReq.getCurrentPage());
				searchListRes.setTotalPage(esClientService.getTotalPage(totalHits, superSearchListReq.getPageSize()));

				List<FileResult> data = searchListRes.getData();

				for (SearchHit hit : hits) {
					Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
					FileResult fileResult = new FileResult();
					HighlightField titleHighlightField = highlightFieldMap.get(StringUtil.getColumnString("title"));
					HighlightField contentHighlightField = highlightFieldMap.get(StringUtil.getColumnString("content"));
					String title = null;
					String summary = null;
					title = esClientService.getHighlightFieldString(titleHighlightField);
					// 如果高亮中没有关键字
					if (title == null) {
						SearchHitField titleField = esClientService.getHitField(hit, StringUtil.getColumnString("title"));

						if (titleField != null) {
							String titleNoHighlight = titleField.getValue();
							title = StringUtil.replaceBlank(titleNoHighlight);
						}
					}

					summary = esClientService.getHighlightFieldString(contentHighlightField);
					if (summary == null) {
						SearchHitField summaryField = esClientService.getHitField(hit, StringUtil.getColumnString("content"));

						if (summaryField != null) {
							String summaryNoHighlight = summaryField.getValue();
							summary = summaryNoHighlight;
						}
					}
					if (title == null) {
						fileResult.setTitle(StringUtil.replaceBlank(getFieldValue(hit, "title")));
					} else {
						fileResult.setTitle(StringUtil.replaceBlank(title));
					}
					if (summary == null) {
						String content = getFieldValue(hit, "content");
						if (content.length() <= 150) {
							fileResult.setSummary(content.substring(0, content.length()));
						} else {
							fileResult.setSummary(content.substring(0, 150));
						}

					} else {
						fileResult.setSummary(summary);
					}

					SearchHitField collectCount = esClientService.getHitField(hit, "collectCount");
					if (collectCount != null) {
						Integer collect = Integer.valueOf(collectCount.getValue().toString());
						fileResult.setCollectCount(collect);
					}

					fileResult.setEid(hit.getId());

					fileResult.setHid(getFieldValue(hit, "hid"));
					fileResult.setFileType(getFieldValue(hit, "fileType"));
					if (StringUtils.isNotEmpty(getFieldValue(hit, "downloadCount"))) {
						fileResult.setDownloadCount(Integer.valueOf(getFieldValue(hit, "downloadCount")));
					} else {
						fileResult.setDownloadCount(0);
					}
					if (StringUtils.isNotEmpty(getFieldValue(hit, "viewCount"))) {
						fileResult.setViewCount(Integer.valueOf(getFieldValue(hit, "viewCount")));
					} else {
						fileResult.setViewCount(0);
					}
					fileResult.setUri(getFieldValue(hit, "uri"));
					fileResult.setCreateTime(Long.valueOf(getFieldValue(hit, "createTime")));
					data.add(fileResult);
					searchListRes.setData(data);
				}
			} catch (Exception e) {
				resultMsg = "执行superSearchList.do失败";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.error("execute--->/serchIface/superSearchList.do" + e);
				e.printStackTrace();
			}
		}
		Date endDate = new Date();
		logger.info("id 是：" + sid + ",  高级查询结束的时间为：" + StringUtil.format(endDate));
		logger.info("id 是：" + sid + ",  高级查询执行的时间为：" + StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);
	}

	/**
	 * 
	 * @Title: hotDocuments
	 * @Description: TODO 热门文档TOP10
	 * @author louyujie
	 * @param request
	 * @param response
	 */
	@RequestMapping("hotDocuments.do")
	public void hotDocuments(HttpServletRequest request, HttpServletResponse response) {
		logger.info("获取热门文档");
		Date startDate = new Date();
		String resultMsg = "hotDocuments.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		logger.info("reqData=" + reqData);
		SearchListRes searchListRes = new SearchListRes();
		logger.info("id 是： " + sid + ", 热门文档top接口的开始时间为：" + StringUtil.format(startDate));
		try {
			// 检索热门索引中的数据
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			List<String> fieldList = new ArrayList<String>();
			fieldList.add("c_eid");
			fieldList.add("c_count");
			List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
			Map<String, Integer> sortMap = new HashMap<String, Integer>();
			sortMap.put("count", 0);
			sortMapList.add(sortMap);
			SearchHits hits = esClientService.searchAllIndex(new String[] { "hot" }, new String[] { Contants.HOT_INDEX }, fieldList, boolQuery, sortMapList, false);
			List<FileResult> data = new ArrayList<FileResult>();
			if (hits != null && hits.totalHits() > 0) {
				if (hits.totalHits() > 10) {
					int sum = 0;
					for (SearchHit hit : hits) {
						if (sum >= 10) {
							break;
						}
						if (!hit.fields().keySet().isEmpty()) {
							if (hit.field("c_eid") != null) {
								String _eid = hit.field("c_eid").value().toString();
								BoolQueryBuilder boolQueryFile = QueryBuilders.boolQuery();
								TermQueryBuilder queryEid = QueryBuilders.termQuery("_id", _eid.toUpperCase());
								boolQueryFile.must(queryEid);
								// 数据类型筛选
								String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
								BoolQueryBuilder queryFileTypeBuilder = QueryBuilders.boolQuery();
								for (String fielType : fileTypes) {
									if (StringUtils.isNotEmpty(fielType)) {
										TermQueryBuilder termQuery = QueryBuilders.termQuery("c_file_type", fielType);
										queryFileTypeBuilder.should(termQuery);
									}
								}
								if (queryFileTypeBuilder != null) {
									boolQueryFile.must(queryFileTypeBuilder);
								}
								List<String> fieldList1 = new ArrayList<String>();
								fieldList1.add(StringUtil.getColumnString("title"));
								fieldList1.add(StringUtil.getColumnString("viewCount"));
								fieldList1.add(StringUtil.getColumnString("hid"));
								List<Map<String, Integer>> sortMapList1 = new ArrayList<Map<String, Integer>>();
								Map<String, Integer> sortMap1 = new HashMap<String, Integer>();
								sortMap1.put("viewCount", 0);
								sortMapList1.add(sortMap1);
								SearchHits hits1 = esClientService.searchAllIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList1, boolQueryFile, sortMapList1, false);
								if (hits1 != null && hits1.totalHits() > 0) {
									FileResult fr = new FileResult();
									fr.setEid(hit.field("c_eid").value().toString());
									fr.setHotCount(hit.field("c_count").value().toString());
									for (SearchHit hit1 : hits1) {
										if (!hit1.fields().keySet().isEmpty()) {
											if (hit1.field(StringUtil.getColumnString("title")) != null) {
												fr.setTitle(StringUtil.replaceBlank(hit1.field(StringUtil.getColumnString("title")).value().toString()));
											}
											if (hit1.field(StringUtil.getColumnString("viewCount")) != null) {
												fr.setViewCount(Integer.valueOf(hit1.field(StringUtil.getColumnString("viewCount")).value().toString()));
											}
											if (hit1.field(StringUtil.getColumnString("hid")) != null) {
												fr.setHid(hit1.field(StringUtil.getColumnString("hid")).value().toString());
											}
										}
									}
									data.add(fr);
								}
							}
						}
						sum++;
					}
				} else {
					for (SearchHit hit : hits) {
						if (!hit.fields().keySet().isEmpty()) {
							if (hit.field("c_eid") != null) {
								String _eid = hit.field("c_eid").value().toString();
								BoolQueryBuilder boolQueryFile = QueryBuilders.boolQuery();
								TermQueryBuilder queryEid = QueryBuilders.termQuery("_id", _eid.toUpperCase());
								boolQueryFile.must(queryEid);
								// 数据类型筛选
								String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
								BoolQueryBuilder queryFileTypeBuilder = QueryBuilders.boolQuery();
								for (String fielType : fileTypes) {
									if (StringUtils.isNotEmpty(fielType)) {
										TermQueryBuilder termQuery = QueryBuilders.termQuery("c_file_type", fielType);
										queryFileTypeBuilder.should(termQuery);
									}
								}
								if (queryFileTypeBuilder != null) {
									boolQueryFile.must(queryFileTypeBuilder);
								}
								List<String> fieldList1 = new ArrayList<String>();
								fieldList1.add(StringUtil.getColumnString("title"));
								fieldList1.add(StringUtil.getColumnString("viewCount"));
								fieldList1.add(StringUtil.getColumnString("hid"));
								List<Map<String, Integer>> sortMapList1 = new ArrayList<Map<String, Integer>>();
								Map<String, Integer> sortMap1 = new HashMap<String, Integer>();
								sortMap1.put("viewCount", 0);
								sortMapList1.add(sortMap1);
								SearchHits hits1 = esClientService.searchAllIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList1, boolQueryFile, sortMapList1, false);
								if (hits1 != null && hits1.totalHits() > 0) {
									FileResult fr = new FileResult();
									fr.setEid(hit.field("c_eid").value().toString());
									fr.setHotCount(hit.field("c_count").value().toString());
									for (SearchHit hit1 : hits1) {
										if (!hit1.fields().keySet().isEmpty()) {
											if (hit1.field(StringUtil.getColumnString("title")) != null) {
												fr.setTitle(StringUtil.replaceBlank(hit1.field(StringUtil.getColumnString("title")).value().toString()));
											}
											if (hit1.field(StringUtil.getColumnString("viewCount")) != null) {
												fr.setViewCount(Integer.valueOf(hit1.field(StringUtil.getColumnString("viewCount")).value().toString()));
											}
											if (hit1.field(StringUtil.getColumnString("hid")) != null) {
												fr.setHid(hit1.field(StringUtil.getColumnString("hid")).value().toString());
											}
										}
									}
									data.add(fr);
								}
							}
						}
					}
				}
				searchListRes.setData(data);
			} else {
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "未检索到数据";
				logger.info("未检索到数据");
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索出现异常";
			logger.error("检索出现异常:");
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是： " + sid + ", 热门文档top接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是： " + sid + ", 热门文档top接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, searchListRes, request, response);
		}
	}

	/**
	 * 
	 * @Description: 公共方法 查询域的返回值
	 * @author zhangshuai
	 * @param hit
	 * @param field
	 * @param type
	 *            数据类型
	 */

	private String getFieldValue(SearchHit hit, String field) {
		logger.info("SearchIfaceController   private String getFieldValue(SearchHit hit,String field)");
		String fieldValue = "";
		SearchHitField fieldAttrbute = null;
		try {
			fieldAttrbute = esClientService.getHitField(hit, field);

			if (fieldAttrbute != null) {
				fieldValue = String.valueOf(fieldAttrbute.getValue());
			}
		} catch (Exception e) {
			logger.error("SearchIfaceController   private String getFieldValue(SearchHit hit,String field)" + e);
			e.printStackTrace();
		} finally {
			fieldAttrbute = null;
		}
		return fieldValue;
	}

	/**
	 * @Description queryString的高级
	 * @author zhangshuai
	 * @param boolQuery
	 * @param iterator
	 * @return BoolQueryBuilder
	 */
	private void queryString(BoolQueryBuilder boolQuery, Iterator<Entry<String, Integer>> iterator, String field) {
		logger.info("public BoolQueryBuilder queryString(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator){");
		Entry<String, Integer> entry = null;
		try {
			while (iterator.hasNext()) {
				entry = iterator.next();
				QueryStringQueryBuilder keyWordQueryTitle = new QueryStringQueryBuilder(entry.getKey());
				keyWordQueryTitle.analyzer("ik").field(StringUtil.getColumnString(field));
				if (entry.getValue() == 1) {
					boolQuery.must(keyWordQueryTitle);
				} else if (entry.getValue() == 2) {
					boolQuery.should(keyWordQueryTitle);
				} else if (entry.getValue() == 0) {
					boolQuery.mustNot(keyWordQueryTitle);
				}
			}
		} catch (Exception e) {
			logger.error("public BoolQueryBuilder queryString(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator){" + e);
			e.printStackTrace();
		} finally {
			iterator = null;
			entry = null;
		}
	}

	/**
	 * @Description rangeQuery的高级
	 * @author zhangshuai
	 * @param boolQuery
	 * @param iterator
	 * @return BoolQueryBuilder
	 */
	private void rangeQuery(BoolQueryBuilder boolQuery, Iterator<Entry<String, Integer>> iterator, String field) {
		logger.info("private void rangeQuery(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator,String field){");
		Entry<String, Integer> entry = null;
		try {
			while (iterator.hasNext()) {
				entry = iterator.next();
				String times[] = entry.getKey().split(":");
				QueryBuilder keyWordRangeQueryBool = QueryBuilders.rangeQuery(StringUtil.getColumnString(field)).from(times[0]).to(times[1]);
				if (entry.getValue() == 1) {
					boolQuery.must(keyWordRangeQueryBool);
				} else if (entry.getValue() == 2) {
					boolQuery.should(keyWordRangeQueryBool);
				} else if (entry.getValue() == 0) {
					boolQuery.mustNot(keyWordRangeQueryBool);
				}
			}
		} catch (Exception e) {
			logger.error("private void rangeQuery(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator,String field){" + e);
			e.printStackTrace();
		} finally {
			iterator = null;
			entry = null;
		}
	}

	/**
	 * @Description termQuery的高级
	 * @author zhangshuai
	 * @param boolQuery
	 * @param iterator
	 * @return BoolQueryBuilder
	 */
	private void termQuery(BoolQueryBuilder boolQuery, Iterator<Entry<String, Integer>> iterator, String field) {
		logger.info("private void termQuery(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator,String field){");
		Entry<String, Integer> entry = null;
		try {
			while (iterator.hasNext()) {
				entry = iterator.next();
				QueryBuilder keyWordTermQueryBool = QueryBuilders.termQuery(entry.getKey(), StringUtil.getColumnString("author"));
				if (entry.getValue() == 1) {
					boolQuery.must(keyWordTermQueryBool);
				} else if (entry.getValue() == 2) {
					boolQuery.should(keyWordTermQueryBool);
				} else if (entry.getValue() == 0) {
					boolQuery.mustNot(keyWordTermQueryBool);
				}
			}
		} catch (Exception e) {
			logger.error("private void termQuery(BoolQueryBuilder boolQuery,Iterator<Entry<String,Integer>> iterator,String field){" + e);
			e.printStackTrace();
		} finally {
			entry = null;
			iterator = null;
		}
	}

	/**
	 * @Description termQuery的高级
	 * @author zhangshuai
	 * @param boolQuery
	 * @param iterator
	 * @return BoolQueryBuilder
	 */
	private void termQueryList(BoolQueryBuilder boolQuery, Iterator<Entry<List<String>, Integer>> iterator, String field) {
		logger.info("private void termQueryList(BoolQueryBuilder boolQuery,Iterator<Entry<List<String>,Integer>> iterator,String field){");
		Entry<List<String>, Integer> entry = null;
		try {
			while (iterator.hasNext()) {
				entry = iterator.next();
				BoolQueryBuilder termQueryList = QueryBuilders.boolQuery();
				for (int i = 0; i < entry.getKey().size(); i++) {
					QueryBuilder keyWordtermQueryList = QueryBuilders.termQuery(StringUtil.getColumnString(field), entry.getKey().get(i));
					termQueryList.should(keyWordtermQueryList);
				}
				if (entry.getValue() == 1) {
					boolQuery.must(termQueryList);
				} else if (entry.getValue() == 2) {
					boolQuery.should(termQueryList);
				} else if (entry.getValue() == 0) {
					boolQuery.mustNot(termQueryList);
				}
			}
		} catch (Exception e) {
			logger.error("private void termQueryList(BoolQueryBuilder boolQuery,Iterator<Entry<List<String>,Integer>> iterator,String field){" + e);
			e.printStackTrace();
		} finally {
			entry = null;
			iterator = null;
		}
	}

	/**
	 * 智能提示
	 * 
	 * @param keyWord
	 * @param resultCount
	 * @return
	 */
	@RequestMapping("termSuggert.do")
	public void termSuggert(HttpServletRequest request, HttpServletResponse response) {
		logger.info("智能提示");
		Date startDate = new Date();
		String resultMsg = "termSuggert.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		// logger.info("reqData=" + reqData);
		logger.info("id 是：" + sid + ", 智能提示接口的 开始时间为：" + StringUtil.format(startDate));
		JSONObject json = JSONObject.parseObject(reqData);
		String keyWord = json.getString("keyWord");
		int resultCount = Integer.parseInt(json.getString("resultCount"));
		Integer i = 0;
		RelativeSearchRes res = new RelativeSearchRes();

		// TermSuggestionBuilder suggest = new
		// TermSuggestionBuilder("doc-chn-test");
		// suggest.analyzer("ik").field(StringUtil.getColumnString("title")).size(resultCount).text(keyWord).maxTermFreq(10).minDocFreq(0).suggestMode("always");
		// Client client = esClientService.getClient();
		// SearchResponse responses =
		// client.prepareSearch("doc-chn-test").setTypes("doc").setSuggestText(keyWord).addSuggestion(suggest).execute().actionGet();
		// System.out.println(responses + "---");
		// Suggest sugg = responses.getSuggest();
		// List<? extends
		// org.elasticsearch.search.suggest.Suggest.Suggestion.Entry<? extends
		// Option>> list = sugg.getSuggestion("doc-chn-test").getEntries();
		// for (int i = 0; i < list.size(); i++) {
		// List<?> options = list.get(i).getOptions();
		// for (int j = 0; j < options.size(); j++) {
		// if (options.get(j) instanceof Option) {
		// Option op = (Option) options.get(j);
		// System.out.println(op.getText());
		// System.out.println(op.getScore());
		// res.getRelativeList().add(op.getText().toString());
		// }
		// }
		// // System.out.println(list.get(i).getText());
		// }

		BoolQueryBuilder bool = QueryBuilders.boolQuery();
		QueryStringQueryBuilder key = new QueryStringQueryBuilder(keyWord);
		key.analyzer("ik").field("c_key_word");
		// MatchQueryBuilder keyQuery = QueryBuilders.matchQuery("c_key_word",
		// "\"" + keyWord + "\"");
		bool.should(key);
		List<String> _fieldList = new ArrayList<String>();
		_fieldList.add("c_key_word");
		SearchHits hits = esClientService.searchAllIndex(new String[] { "query" }, new String[] { "query-chn-test" }, _fieldList, bool, null, true);
		Map<String, String> map = new HashMap<String, String>();
		if (hits != null && hits.totalHits() > 0) {
			for (SearchHit hit : hits) {
				if (!hit.fields().keySet().isEmpty()) {
					if (hit.field("c_key_word") != null) {
						map.put(hit.field("c_key_word").getValue().toString(), hit.field("c_key_word").getValue().toString());
					}
				}
			}
		}
		List<String> relativeList = new ArrayList<String>();
		for (Object o : map.keySet()) {
			relativeList.add(map.get(o));
			if (i == resultCount - 1) {
				break;
			} else if (i == map.size()) {
				break;
			} else {
				i++;
			}
		}
		res.setRelativeList(relativeList);
		/**
		 * 插件 Client client = esClientService.getClient(); List <String>
		 * suggestions = null; try{ suggestions = new
		 * SuggestRequestBuilder(client) .setIndices("doc-chn-test")//索引
		 * .field(StringUtil.getColumnString("title"))//查询field
		 * .field(StringUtil.getColumnString("content"))
		 * .field(StringUtil.getColumnString("keyWord")) .term(keyWord)//提示关键字
		 * .size(resultCount)//返回结果数 .similarity(0.5f)//相似度
		 * .execute().actionGet().suggestions(); resultCode =
		 * CommonParameters.resultCode_OK; }catch(Exception e){
		 * resultMsg="无提示结果"; logger.info("无提示结果"); e.printStackTrace(); }
		 */
		Date endDate = new Date();
		logger.info("id 是：" + sid + ", 智能提示接口的结束时间为：" + StringUtil.format(endDate));
		logger.info("id 是：" + sid + ", 智能提示接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, res, request, response);
	}

	/**
	 * 功能： 查询收藏文件夹及收藏文件 参数： request response zhangshuai
	 */
	@RequestMapping("searchCollectionFolder.do")
	public void searchCollectionFolder(HttpServletRequest request, HttpServletResponse response) {
		logger.info(" 对收藏文件的文件夹操作 ：public void collectionFolder(HttpServletRequest request, HttpServletResponse response){");
		Date startDate = new Date();
		String sid = request.getParameter("sid");
		String jsonData = request.getParameter("reqData");
		logger.info("jsonData = " + jsonData);
		String resultCode = "";
		String resultMsg = "";

		CollctionFolderModel collctionFolderModel = null;
		ResponseCollctionFolderModel responseCollctionFolderModel = null;
		List<CollctionFolderModel> collctionFolderModelList = new ArrayList<CollctionFolderModel>();
		List<String> fieldList = null;
		logger.info("id 是：" + sid + ", 查询收藏文件夹及收藏文件接口的开始时间为" + StringUtil.format(startDate));
		try {
			collctionFolderModel = JSONObject.parseObject(jsonData, CollctionFolderModel.class);

			String userId = collctionFolderModel.getUserId();
			String folderName = collctionFolderModel.getFolderName();
			String folderId = collctionFolderModel.getFolderId();
			String isFolder = collctionFolderModel.getIsFolder();
			if (!StringUtils.isNotEmpty(userId) && !StringUtils.isNotEmpty(folderName) && !StringUtils.isNotEmpty(folderId) && !StringUtils.isNotEmpty(isFolder)) {
				resultCode = CommonParameters.resultCode_ERROR;
				resultMsg = "传过来的字段都为null或者为空";
			} else {
				String keyWord = request.getParameter("keyWord");
				int pageSize = Integer.valueOf(request.getParameter("pageSize"));
				int currentpage = Integer.valueOf(request.getParameter("currentpage"));
				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();

				if (StringUtils.isNotEmpty(keyWord)) {
					QueryStringQueryBuilder queryStringQueryFolder = new QueryStringQueryBuilder(keyWord);
					queryBuilderBool.should(queryStringQueryFolder);
				}

				if (StringUtils.isNotEmpty(isFolder)) {
					TermQueryBuilder queryIsFold = QueryBuilders.termQuery(StringUtil.getColumnString("isFolder"), isFolder);
					queryBuilderBool.must(queryIsFold);
				}

				if (StringUtils.isNotEmpty(folderId)) {
					TermQueryBuilder FolderIdtermQuery = QueryBuilders.termQuery(StringUtil.getColumnString("folderId"), folderId);
					queryBuilderBool.must(FolderIdtermQuery);
				}

				if (StringUtils.isNotEmpty(userId)) {
					QueryBuilder userIdtermQuery = QueryBuilders.termQuery(StringUtil.getColumnString("userId"), userId);
					queryBuilderBool.must(userIdtermQuery);
				}

				fieldList = new ArrayList<String>();
				fieldList.add(StringUtil.getColumnString("userId"));
				fieldList.add(StringUtil.getColumnString("folderName"));
				fieldList.add(StringUtil.getColumnString("folderId"));
				fieldList.add(StringUtil.getColumnString("isFolder"));
				fieldList.add(StringUtil.getColumnString("eid"));
				fieldList.add(StringUtil.getColumnString("hid"));
				fieldList.add(StringUtil.getColumnString("fileName"));
				fieldList.add(StringUtil.getColumnString("createTime"));
				List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
				Map<String, Integer> sortMap = new HashMap<String, Integer>();
				sortMap.put("createTime", 0);
				sortMapList.add(sortMap);
				SearchHits hits = esClientService.searchIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, fieldList, queryBuilderBool, false, sortMapList, false, "template", currentpage, pageSize);
				responseCollctionFolderModel = new ResponseCollctionFolderModel();
				responseCollctionFolderModel.setTotalCount(hits.totalHits());
				responseCollctionFolderModel.setTotalPage(esClientService.getTotalPage(hits.totalHits(), pageSize));
				if (hits != null && hits.totalHits() > 0) {
					for (SearchHit hit : hits) {
						if (!hit.fields().keySet().isEmpty()) {
							CollctionFolderModel collctionFolder = new CollctionFolderModel();
							collctionFolder.setFid(hit.getId());
							String _folderId = "";
							if (hit.field(StringUtil.getColumnString("createTime")) != null) {
								collctionFolder.setCreateTime(Long.valueOf(hit.field(StringUtil.getColumnString("createTime")).getValue().toString()));
							} else {
								collctionFolder.setCreateTime(0);
							}
							if (hit.field(StringUtil.getColumnString("eid")) != null) {
								collctionFolder.setEid(hit.field(StringUtil.getColumnString("eid")).getValue().toString());
							} else {
								collctionFolder.setEid(null);
							}
							if (hit.field(StringUtil.getColumnString("hid")) != null) {
								collctionFolder.setHid(hit.field(StringUtil.getColumnString("hid")).getValue().toString());
							} else {
								collctionFolder.setHid(null);
							}
							if (hit.field(StringUtil.getColumnString("userId")) != null) {
								collctionFolder.setUserId(hit.field(StringUtil.getColumnString("userId")).getValue().toString());
							} else {
								collctionFolder.setUserId(null);
							}
							if (hit.field(StringUtil.getColumnString("fileName")) != null) {
								collctionFolder.setFileName(hit.field(StringUtil.getColumnString("fileName")).getValue().toString());
							} else {
								collctionFolder.setFileName(null);
							}
							if (hit.field(StringUtil.getColumnString("folderName")) != null) {
								collctionFolder.setFolderName(hit.field(StringUtil.getColumnString("folderName")).getValue().toString());
							} else {
								collctionFolder.setFolderName(null);
							}
							if (hit.field(StringUtil.getColumnString("folderId")) != null) {
								_folderId = hit.field(StringUtil.getColumnString("folderId")).getValue().toString();
								collctionFolder.setFolderId(_folderId);
							} else {
								collctionFolder.setFolderId(null);
							}
							if (hit.field(StringUtil.getColumnString("isFolder")) != null) {
								String _isFolder = hit.field(StringUtil.getColumnString("isFolder")).getValue().toString();
								collctionFolder.setIsFolder(_isFolder);
								if (StringUtils.equals("true", _isFolder)) {
									BoolQueryBuilder queryBool = QueryBuilders.boolQuery();
									TermQueryBuilder queryFolderId = QueryBuilders.termQuery(StringUtil.getColumnString("folderId"), _folderId);
									queryBool.must(queryFolderId);
									TermQueryBuilder queryIsFolder = QueryBuilders.termQuery(StringUtil.getColumnString("isFolder"), "false");
									queryBool.must(queryIsFolder);
									SearchHits _hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, queryBool, null, false);
									if (_hits != null) {
										collctionFolder.setCount(_hits.totalHits());
									}
								}
							} else {
								collctionFolder.setIsFolder(null);
							}
							collctionFolderModelList.add(collctionFolder);
							responseCollctionFolderModel.setCollctionFolderModel(collctionFolderModelList);
						}
					}
					resultCode = CommonParameters.resultCode_OK;
					resultMsg = "检索成功";
					logger.info("检索结果共" + hits.getTotalHits() + "条");
				} else {
					resultMsg = "无检索结果";
					logger.info("无检索结果");
				}
			}
		} catch (Exception e) {
			logger.error("public void searchCollectionFolder(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索失败";
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 查询收藏文件夹及收藏文件接口的结束时间为" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 查询收藏文件夹及收藏文件接口的执行时间为" + StringUtil.pastTime(startDate, endDate));
		}
		resultResponse(resultMsg, sid, resultCode, responseCollctionFolderModel, request, response);
	}

	/**
	 * 功能： 对文件夹及文件添加 参数： request response zhangshuai
	 */
	@RequestMapping("createCollectionDocAndFolder.do")
	public void createCollectionDocAndFolder(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String sid = request.getParameter("sid");
		String jsonData = request.getParameter("reqData");
		// logger.info("jsonData = " + jsonData);
		String resultCode = "";
		String resultMsg = "";
		CollctionFolderModel collctionFolderModel = null;
		CollctionFolderAndDoc collctionFolderAndDoc = null;
		ResponseCollctionFolderModel responseCollctionFolderModel = new ResponseCollctionFolderModel();
		logger.info("id 是：" + sid + ", 对文件夹及文件添加 接口的开始时间为：" + StringUtil.format(startDate));
		try {
			collctionFolderModel = JSONObject.parseObject(jsonData, CollctionFolderModel.class);
			long createTime = collctionFolderModel.getCreateTime();
			String eid = collctionFolderModel.getEid();
			String hid = collctionFolderModel.getHid();
			String userId = collctionFolderModel.getUserId();
			String sessionId = collctionFolderModel.getSessionId();
			String fileName = collctionFolderModel.getFileName();
			String folderName = collctionFolderModel.getFolderName();
			String folderId = collctionFolderModel.getFolderId();
			String isFolder = collctionFolderModel.getIsFolder();
			String fileType = collctionFolderModel.getFileType();
			Boolean folderNameFlag = true;
			Boolean eidUserIdFlag = true;
			if (StringUtils.equals(isFolder, "true")) {
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				TermQueryBuilder queryFolderName = QueryBuilders.termQuery("c_folder_md5", MD5Util.MD5(folderName));
				boolQuery.must(queryFolderName);
				TermQueryBuilder queryUser = QueryBuilders.termQuery("c_user_id", userId);
				boolQuery.must(queryUser);
				SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, boolQuery, null, false);
				if (hits != null && hits.totalHits() > 0) {
					folderNameFlag = false;
				}
			} else {
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				TermQueryBuilder queryEid = QueryBuilders.termQuery("c_eid", eid);
				boolQuery.must(queryEid);
				TermQueryBuilder queryUserId = QueryBuilders.termQuery("c_user_id", userId);
				boolQuery.must(queryUserId);
				SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, boolQuery, null, false);
				if (hits != null && hits.totalHits() > 0) {
					eidUserIdFlag = false;
				}
			}
			if (folderNameFlag && eidUserIdFlag) {
				collctionFolderAndDoc = new CollctionFolderAndDoc();
				collctionFolderAndDoc.setCreateTime(createTime);
				collctionFolderAndDoc.setEid(eid);
				collctionFolderAndDoc.setHid(hid);
				collctionFolderAndDoc.setUserId(userId);
				collctionFolderAndDoc.setSessionId(sessionId);
				collctionFolderAndDoc.setFileName(fileName);
				collctionFolderAndDoc.setFolderName(folderName);
				collctionFolderAndDoc.setFolderId(folderId);
				collctionFolderAndDoc.setIsFolder(isFolder);
				if (StringUtils.equals(isFolder, "false")) {
					collctionFolderAndDoc.setFileType(fileType);
					// 将文档的收藏量字段加1
					String indexName = esClientService.getIndexById(eid.toUpperCase());
					String[] fields = { StringUtil.getColumnString("collectCount") };
					GetResponse getResponse = esClientService.get(indexName, "doc", eid.toUpperCase(), fields);
					if (getResponse != null) {
						String collectCount = "0";
						if (getResponse.getField(StringUtil.getColumnString("collectCount")) != null) {
							collectCount = StringUtils.isEmpty(getResponse.getField(StringUtil.getColumnString("collectCount")).getValue().toString()) ? "0" : getResponse.getField(StringUtil.getColumnString("collectCount")).getValue().toString();
						}
						int collectCounts = Integer.valueOf(collectCount);
						Map<String, Object> json = new HashMap<String, Object>();
						Integer co = collectCounts + 1;
						json.put(StringUtil.getColumnString("collectCount"), co);
						esClientService.updateIndex(indexName, "doc", json, eid.toUpperCase());
					}
				} else {
					collctionFolderAndDoc.setFolderMd5(MD5Util.MD5(folderName));
				}
				String indexJson = JSON.toJSONString(collctionFolderAndDoc);
				esClientService.createIndex("test-doc-chn", "test", indexJson);
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "操作成功！！！";
				logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
				List<CollctionFolderModel> list = new ArrayList<CollctionFolderModel>();
				CollctionFolderModel c = new CollctionFolderModel();
				c.setCreateTime(createTime);
				c.setEid(eid);
				c.setFid(folderId);
				c.setFileName(fileName);
				c.setFolderId(folderId);
				c.setFolderName(folderName);
				c.setHid(hid);
				c.setIsFolder(isFolder);
				c.setSessionId(sessionId);
				c.setUserId(userId);
				list.add(c);
				responseCollctionFolderModel.setCollctionFolderModel(list);
			} else {
				if (!eidUserIdFlag) {
					resultCode = CommonParameters.resultCode_ERROR;
					resultMsg = "该文件已经被你收藏！！！";
					logger.error("resultCode = " + resultCode + " resultMsg =" + resultMsg);
				}
				if (!folderNameFlag) {
					resultCode = CommonParameters.resultCode_ERROR;
					resultMsg = "该文件夹已经存在！！！";
					logger.error("resultCode = " + resultCode + " resultMsg =" + resultMsg);
				}
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "操作失败！！！";
			logger.error("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			logger.error("public void searchCollectionDoc(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 对文件夹及文件添加 接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 对文件夹及文件添加 接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
		}
		resultResponse(resultMsg, sid, resultCode, responseCollctionFolderModel, request, response);
	}

	/**
	 * 功能： 移动文件及其文件夹重新命名 参数： request response zhangshuai
	 */
	@RequestMapping("updateCollectionDoc.do")
	public void updateCollectionDoc(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String sid = request.getParameter("sid");
		String jsonData = request.getParameter("reqData");
		String resultCode = "";
		String resultMsg = "";
		logger.info("jsonData = " + jsonData);
		CollctionFolderModel collctionFolderModel = null;
		Map<String, Object> json = new HashMap<String, Object>();
		ResponseCollctionFolderModel responseCollctionFolderModel = null;
		logger.info("id 是：" + sid + "移动文件及其文件夹重新命名接口的开始时间为：" + StringUtil.format(startDate));
		try {
			collctionFolderModel = JSONObject.parseObject(jsonData, CollctionFolderModel.class);
			String folderId = collctionFolderModel.getFolderId();
			String isFolder = collctionFolderModel.getIsFolder();
			String eid = collctionFolderModel.getEid();
			String userId = collctionFolderModel.getUserId();
			String id = null;
			if ("true".equals(isFolder)) {
				String folderName = collctionFolderModel.getFolderName();
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				QueryStringQueryBuilder queryFolderName = new QueryStringQueryBuilder(folderName);
				queryFolderName.field("c_folder_name");
				boolQuery.must(queryFolderName);
				TermQueryBuilder termQuery = QueryBuilders.termQuery("c_user_id", userId);
				boolQuery.must(termQuery);
				SearchHits hitsFolderName = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, boolQuery, null, false);
				if (hitsFolderName != null && hitsFolderName.totalHits() > 0) {
					resultCode = CommonParameters.resultCode_ERROR;
					resultMsg = "该文件夹已经存在！！！";
					logger.error("resultCode = " + resultCode + " resultMsg =" + resultMsg);
				} else {
					json.put(StringUtil.getColumnString("folderName"), folderName);

					BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();
					QueryStringQueryBuilder query = new QueryStringQueryBuilder(folderId);
					query.field(StringUtil.getColumnString("folderId"));
					TermQueryBuilder queryIsForder = QueryBuilders.termQuery(StringUtil.getColumnString("isFolder"), "true");
					queryBuilderBool.must(queryIsForder);
					queryBuilderBool.must(query);
					List<String> fieldList = new ArrayList<String>();
					fieldList.add("eid");
					SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, fieldList, queryBuilderBool, null, false);
					if (hits != null && hits.totalHits() > 0) {
						for (SearchHit hit : hits) {
							if (hit != null) {
								id = hit.id();
							}
						}
					}
					esClientService.updateIndex("test-doc-chn", "test", json, id);
					resultCode = CommonParameters.resultCode_OK;
					resultMsg = "操作成功！！！";
					logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
				}
			} else if (StringUtils.equals(isFolder, "false")) {
				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();
				TermQueryBuilder queryEid = QueryBuilders.termQuery(StringUtil.getColumnString("eid"), eid);
				queryBuilderBool.must(queryEid);
				TermQueryBuilder termQuery = QueryBuilders.termQuery("c_user_id", userId);
				queryBuilderBool.must(termQuery);
				SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, queryBuilderBool, null, false);
				if (hits != null) {
					for (SearchHit hit : hits) {
						if (hit != null) {
							id = hit.getId();
						}
					}
				}
				json.put(StringUtil.getColumnString("folderId"), folderId);
				esClientService.updateIndex("test-doc-chn", "test", json, id);
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "操作成功！！！";
				logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "操作失败！！！";
			logger.error("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			logger.error("public void updateCollectionDoc(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + "移动文件及其文件夹重新命名接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + "移动文件及其文件夹重新命名接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, responseCollctionFolderModel, request, response);
			collctionFolderModel = null;
			json = null;
		}
	}

	/**
	 * 功能： 取消文件收藏及其文件夹删除 参数： request response zhangshuai
	 */
	@RequestMapping("deleteCollectionDoc.do")
	public void deleteCollectionDoc(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String sid = request.getParameter("sid");
		String jsonData = request.getParameter("reqData");
		logger.info("jsonData = " + jsonData);
		String resultCode = "";
		String resultMsg = "";
		CollctionFolderModel collctionFolderModel = null;
		ResponseCollctionFolderModel responseCollctionFolderModel = null;
		logger.info("id 是：" + sid + ", 取消文件收藏及其文件夹删除接口的开始时间为" + StringUtil.format(startDate));
		try {
			collctionFolderModel = JSONObject.parseObject(jsonData, CollctionFolderModel.class);
			String eid = collctionFolderModel.getEid();
			String isFolder = collctionFolderModel.getIsFolder();
			String folderId = collctionFolderModel.getFolderId();
			if ("true".equals(isFolder)) {
				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();
				QueryStringQueryBuilder query = new QueryStringQueryBuilder(folderId);
				query.field(StringUtil.getColumnString("folderId"));
				queryBuilderBool.must(query);
				SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, queryBuilderBool, null, false);
				if (hits != null && hits.totalHits() > 0) {
					for (SearchHit hit : hits) {
						if (hit != null) {
							esClientService.deleteIndex("test-doc-chn", "test", hit.getId());
						}
					}
				}
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "操作成功！！！";
				logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			} else {
				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();
				QueryStringQueryBuilder query = new QueryStringQueryBuilder(eid);
				query.field(StringUtil.getColumnString("eid"));
				queryBuilderBool.must(query);
				List<String> fieldList = new ArrayList<String>();
				fieldList.add("eid");
				SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, fieldList, queryBuilderBool, null, false);
				String id = null;
				if (hits != null && hits.totalHits() > 0) {
					for (SearchHit hit : hits) {
						if (hit != null) {
							id = hit.id();
						}
					}
				}
				esClientService.deleteIndex("test-doc-chn", "test", id);

				// 将文档的收藏量减1
				Integer collectCount = 0;
				String indexName = esClientService.getIndexById(eid.toUpperCase());
				String[] fields = { StringUtil.getColumnString("collectCount") };
				GetResponse getResponse = esClientService.get(indexName, "doc", eid.toUpperCase(), fields);
				if (getResponse != null) {
					if (getResponse.getField(StringUtil.getColumnString("collectCount")) != null) {
						collectCount = Integer.valueOf(getResponse.getField(StringUtil.getColumnString("collectCount")).getValue().toString());
					}
					if (collectCount != null) {
						if (collectCount > 0) {
							collectCount--;
						}
					}
					Map<String, Object> json = new HashMap<String, Object>();
					json.put(StringUtil.getColumnString("collectCount"), collectCount);
					esClientService.updateIndex(indexName, "doc", json, eid.toUpperCase());
				}
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "操作成功！！！";
				logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "操作失败！！！";
			logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			logger.error("public void deleteCollectionDoc(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 取消文件收藏及其文件夹删除接口的结束时间为" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 取消文件收藏及其文件夹删除接口的执行时间为" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, responseCollctionFolderModel, request, response);
			collctionFolderModel = null;
		}
	}

	/**
	 * 功能： 文档检查重复接口 参数： request response Dream
	 */
	@RequestMapping("noRepeatCollection.do")
	public void noRepeatCollection(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info(" ---- 文档检查重复接口 ---- ");
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		// logger.info("reqData = " + reqData);

		String resultCode = "";
		String resultMsg = "";

		CollctionFolderModel cfm = null;

		logger.info("id是：" + sid + ", 文档检查重复接口的开始时间为：" + StringUtil.format(startDate));
		try {
			cfm = JSONObject.parseObject(reqData, CollctionFolderModel.class);
			String eid = cfm.getEid();
			String userId = cfm.getUserId();

			BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();
			TermQueryBuilder queryEid = QueryBuilders.termQuery(StringUtil.getColumnString("eid"), eid);
			queryBuilderBool.must(queryEid);
			TermQueryBuilder queryUserId = QueryBuilders.termQuery("c_user_id", userId);
			queryBuilderBool.must(queryUserId);
			SearchHits hits = esClientService.searchAllIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, null, queryBuilderBool, null, false);
			if (hits != null && hits.totalHits() > 0) {
				resultCode = "202";
				resultMsg = "操作成功！";
			} else {
				resultCode = CommonParameters.resultCode_OK;
				resultMsg = "操作成功！";
			}
		} catch (Exception e) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "操作失败！！！";
			logger.info("resultCode = " + resultCode + " resultMsg =" + resultMsg);
			logger.error("public void noRepeatCollection(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
		} finally {
			Date endDate = new Date();
			logger.info("id是：" + sid + ", 文档检查重复接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id是：" + sid + ", 文档检查重复接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, null, request, response);
		}
	}

	/**
	 * 功能： 查询文件夹下的所有文件 参数： request response zhangshuai
	 */
	@RequestMapping("searchCollectionDoc.do")
	public void searchCollectionDoc(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info(" 查询文件夹下的所有文件 ：public void searchCollectionDoc(HttpServletRequest request, HttpServletResponse response){");
		String sid = request.getParameter("sid");
		String jsonData = request.getParameter("reqData");
		// logger.info("jsonData = " + jsonData);

		String resultCode = "";
		String resultMsg = "";

		CollctionFolderModel collctionFolderModel = null;
		ResponseCollctionFolderModel responseCollctionFolderModel = null;
		List<CollctionFolderModel> collctionFolderModelList = new ArrayList<CollctionFolderModel>();
		List<String> fieldList = null;
		logger.info("id是：" + sid + "查询文件夹下的所有文件接口的开始时间为：" + StringUtil.format(startDate));
		try {
			collctionFolderModel = JSONObject.parseObject(jsonData, CollctionFolderModel.class);

			String userId = collctionFolderModel.getUserId();
			String folderName = collctionFolderModel.getFolderName();
			String folderId = collctionFolderModel.getFolderId();
			String isFolder = collctionFolderModel.getIsFolder();
			if (StringUtils.isEmpty(userId) && StringUtils.isEmpty(folderId) && StringUtils.isEmpty(folderName) && StringUtils.isEmpty(isFolder)) {
				resultCode = CommonParameters.resultCode_ERROR;
				resultMsg = "传过来的字段都为null或者为空";
			} else {
				int pageSize = Integer.valueOf(request.getParameter("pageSize"));
				String current = request.getParameter("currentPage");
				Integer currentPage = Integer.valueOf(current);
				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();

				if (StringUtils.isNotEmpty(isFolder)) {
					QueryBuilder isFoldertermQuery = QueryBuilders.termQuery(StringUtil.getColumnString("isFolder"), isFolder);
					queryBuilderBool.must(isFoldertermQuery);
				}

				if (StringUtils.isNotEmpty(folderId)) {
					QueryBuilder FolderIdtermQuery = QueryBuilders.termQuery(StringUtil.getColumnString("folderId"), folderId);
					queryBuilderBool.must(FolderIdtermQuery);
				}

				if (StringUtils.isNotEmpty(userId)) {
					QueryBuilder userIdtermQuery = QueryBuilders.termQuery(StringUtil.getColumnString("userId"), userId);
					queryBuilderBool.must(userIdtermQuery);
				}

				fieldList = new ArrayList<String>();
				fieldList.add(StringUtil.getColumnString("userId"));
				fieldList.add(StringUtil.getColumnString("folderName"));
				fieldList.add(StringUtil.getColumnString("folderId"));
				fieldList.add(StringUtil.getColumnString("isFolder"));
				fieldList.add(StringUtil.getColumnString("eid"));
				fieldList.add(StringUtil.getColumnString("hid"));
				fieldList.add(StringUtil.getColumnString("fileName"));
				fieldList.add(StringUtil.getColumnString("createTime"));
				fieldList.add(StringUtil.getColumnString("fileType"));
				SearchHits hits = esClientService.searchIndex(new String[] { "test" }, new String[] { "test-doc-chn" }, fieldList, queryBuilderBool, false, null, false, "template", currentPage, pageSize);
				responseCollctionFolderModel = new ResponseCollctionFolderModel();
				responseCollctionFolderModel.setTotalCount(hits.totalHits());
				responseCollctionFolderModel.setTotalPage(esClientService.getTotalPage(hits.totalHits(), pageSize));
				if (hits != null && hits.totalHits() > 0) {
					for (SearchHit hit : hits) {
						if (!hit.fields().keySet().isEmpty()) {
							CollctionFolderModel collctionFolder = new CollctionFolderModel();
							collctionFolder.setFid(hit.getId());
							if (hit.field(StringUtil.getColumnString("createTime")) != null) {
								collctionFolder.setCreateTime(Long.valueOf(hit.field(StringUtil.getColumnString("createTime")).getValue().toString()));
							} else {
								collctionFolder.setCreateTime(0);
							}
							if (hit.field(StringUtil.getColumnString("eid")) != null) {
								collctionFolder.setEid(hit.field(StringUtil.getColumnString("eid")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("hid")) != null) {
								collctionFolder.setHid(hit.field(StringUtil.getColumnString("hid")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("userId")) != null) {
								collctionFolder.setUserId(hit.field(StringUtil.getColumnString("userId")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("fileName")) != null) {
								collctionFolder.setFileName(hit.field(StringUtil.getColumnString("fileName")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("folderName")) != null) {
								collctionFolder.setFolderName(hit.field(StringUtil.getColumnString("folderName")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("folderId")) != null) {
								collctionFolder.setFolderId(hit.field(StringUtil.getColumnString("folderId")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("isFolder")) != null) {
								collctionFolder.setIsFolder(hit.field(StringUtil.getColumnString("isFolder")).getValue().toString());
							}
							if (hit.field(StringUtil.getColumnString("fileType")) != null) {
								collctionFolder.setFileType(hit.field(StringUtil.getColumnString("fileType")).getValue().toString());
							}
							collctionFolderModelList.add(collctionFolder);
							responseCollctionFolderModel.setCollctionFolderModel(collctionFolderModelList);
						}
					}
					resultCode = CommonParameters.resultCode_OK;
					logger.info("检索结果共" + hits.getTotalHits() + "条");
				} else {
					resultMsg = "无检索结果";
					logger.info("无检索结果");
				}
			}
		} catch (Exception e) {
			logger.error("public void searchCollectionDoc(HttpServletRequest request, HttpServletResponse response){" + e);
			e.printStackTrace();
			resultCode = CommonParameters.resultCode_ERROR;
			resultMsg = "检索失败";
		} finally {
			collctionFolderModel = null;
			collctionFolderModelList = null;
			fieldList = null;
			Date endDate = new Date();
			logger.info("id是：" + sid + "查询文件夹下的所有文件接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id是：" + sid + "查询文件夹下的所有文件接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
		}
		resultResponse(resultMsg, sid, resultCode, responseCollctionFolderModel, request, response);
	}
}