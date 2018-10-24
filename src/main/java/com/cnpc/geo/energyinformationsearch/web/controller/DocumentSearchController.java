package com.cnpc.geo.energyinformationsearch.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.controller.BaseController;
import com.cnpc.geo.energyinformationsearch.base.entity.CommonParameters;
import com.cnpc.geo.energyinformationsearch.base.util.GeneratorIndexAndType;
import com.cnpc.geo.energyinformationsearch.base.util.StringUtil;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.hbase.client.HbaseClientService;
import com.cnpc.geo.energyinformationsearch.search.entity.Document;
import com.cnpc.geo.energyinformationsearch.search.entity.DocumentSearchListReq;
import com.cnpc.geo.energyinformationsearch.search.entity.DocumentSearchListRes;
import com.cnpc.geo.energyinformationsearch.search.entity.DocumentSimpleSearchListReq;
import com.cnpc.geo.energyinformationsearch.search.entity.DocumentlockReq;

@Controller
@RequestMapping("/documentSearch")
public class DocumentSearchController extends BaseController {

	Logger logger = Logger.getLogger(DocumentSearchController.class);

	@Autowired
	ElasticSearchClientService esClientService;

	@Autowired
	HbaseClientService hbaseClientService;

	/**
	 * 
	 * @Title: simpleSearchList
	 * @Description: 文档快速查询接口
	 * @author zhaogd
	 * @param request
	 * @param response
	 */
	@RequestMapping("/simpleSearchList.do")
	public void simpleSearchList(HttpServletRequest request, HttpServletResponse response) {
		logger.info("execute--->文档快速查询：/documentSearch/simpleSearchList.do");
		Date startDate = new Date();
		String resultMsg = "simpleSearchList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = null;
		DocumentSearchListRes documentListRes = null;
		String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
		String[] sourceOriginalTypes = { "open", "own", "trade", "upload" };
		logger.info("id 是：" + sid + ", 文档快速查询接口的开始时间为：" + StringUtil.format(startDate));
		try {
			sid = request.getParameter("sid");
			String reqData = request.getParameter("reqData");
			if (sid == null || reqData == null) {
				resultMsg = "操作失败：参数不完整";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.info("resultMsg =操作失败;resultCode = 201");
				logger.error("/documentSearch/simpleSearchList.do--->参数不完整");
			} else {
				logger.info("reqData=" + reqData);
				DocumentSimpleSearchListReq simpleSearchListReq = JSON.parseObject(reqData, DocumentSimpleSearchListReq.class);
				String keyWord = null;
				Long currentPage = simpleSearchListReq.getCurrentPage();
				Long pageSize = simpleSearchListReq.getPageSize();
				if (currentPage == null || pageSize == null) {
					resultMsg = "操作失败：字段中含有null或者空值";
					resultCode = CommonParameters.resultCode_ERROR;
					logger.info("resultMsg =操作失败;resultCode = 201");
					logger.error("/documentSearch/simpleSearchList.do--->字段中含有null或者空值");
				} else {
					BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
					// 文件类型过滤
					BoolQueryBuilder queryFileTypeBuilderBool = QueryBuilders.boolQuery();
					for (String type : fileTypes) {
						if (StringUtils.isNotEmpty(type)) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery("c_file_type", type);
							queryFileTypeBuilderBool.should(termQuery);
						}
					}
					if (queryFileTypeBuilderBool != null) {
						boolQuery.must(queryFileTypeBuilderBool);
					}

					// 来源类型过滤
					BoolQueryBuilder querySourceTypeBuilderBool = QueryBuilders.boolQuery();

					for (String type : sourceOriginalTypes) {
						if (StringUtils.isNotEmpty(type)) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery("c_source_original_type", type);
							querySourceTypeBuilderBool.should(termQuery);
						}
					}
					if (querySourceTypeBuilderBool != null) {
						boolQuery.must(querySourceTypeBuilderBool);
					}

					if (StringUtils.isNotEmpty(simpleSearchListReq.getKeyWord())) {
						keyWord = simpleSearchListReq.getKeyWord().trim();
						if (keyWord.lastIndexOf(" ") == -1) {
							BoolQueryBuilder boolQueryKeyWord = QueryBuilders.boolQuery();
							MatchQueryBuilder keyWordQueryFileName = new MatchQueryBuilder("c_title", keyWord);
							boolQueryKeyWord.should(keyWordQueryFileName);
							TermQueryBuilder keyWordQueryCount = new TermQueryBuilder("c_content", keyWord);
							boolQueryKeyWord.should(keyWordQueryCount);
							if (boolQueryKeyWord != null) {
								boolQuery.must(boolQueryKeyWord);
							}
						} else {
							String[] keyWords = StringUtils.split(keyWord, " ");
							for (int i = 0; i < keyWords.length; i++) {
								if (StringUtils.isNotEmpty(keyWords[i])) {
									QueryStringQueryBuilder keyWordQueryTitle = new QueryStringQueryBuilder(keyWords[i]);
									keyWordQueryTitle.field("c_title").field("c_content");
									if (keyWordQueryTitle != null) {
										boolQuery.must(keyWordQueryTitle);
									}
								}
							}
						}
					}
					List<String> fieldList = new ArrayList<String>();
					fieldList.add("eid");
					fieldList.add("c_hid");
					fieldList.add("c_title");
					fieldList.add("c_file_type");
					fieldList.add("c_lock_status");
					fieldList.add("c_lock_user");
					fieldList.add("c_source_original_type");
					fieldList.add("c_creator_id");
					fieldList.add("c_source_id");
					fieldList.add("c_create_time");
					List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
					Map<String, Integer> sort = new HashMap<String, Integer>();
					sort.put("createTime", 1);
					sortMapList.add(sort);
					SearchHits hits = esClientService.searchIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, boolQuery, true, sortMapList, false, "template", Integer.parseInt(currentPage.toString()), Integer.parseInt(pageSize.toString()));
					documentListRes = new DocumentSearchListRes();
					documentListRes.setCurrentPage(currentPage);
					documentListRes.setTotalCount(hits.getTotalHits());
					documentListRes.setTotalPage(esClientService.getTotalPage(hits.totalHits(), Integer.parseInt(pageSize.toString())));
					List<Document> documentList = new ArrayList<Document>();
					if (hits != null && hits.totalHits() > 0) {
						for (SearchHit hit : hits) {
							if (!hit.fields().keySet().isEmpty()) {
								Document document = new Document();
								document.setEid(hit.getId());
								if (hit.field("c_title") != null) {
									document.setFileName(StringUtil.replaceBlank(hit.field("c_title").getValue().toString()));
								} else {
									document.setFileName(null);
								}
								if (hit.field("c_hid") != null) {
									document.setHid(hit.field("c_hid").getValue().toString());
								} else {
									document.setHid(null);
								}
								if (hit.field("c_file_type") != null) {
									document.setFileType(hit.field("c_file_type").getValue().toString());
								} else {
									document.setFileType(null);
								}
								if (hit.field("c_lock_status") != null) {
									document.setLockStatus(hit.field("c_lock_status").getValue().toString());
								} else {
									document.setLockStatus(CommonParameters.UN_LOCK);
								}
								if (hit.field("c_lock_user") != null) {
									document.setLockUser(hit.field("c_lock_user").getValue().toString());
								} else {
									document.setLockUser(null);
								}
								if (hit.field("c_source_original_type") != null) {
									document.setSourceOriginalType(hit.field("c_source_original_type").getValue().toString());
								} else {
									document.setSourceOriginalType(null);
								}
								if (hit.field("c_creator_id") != null) {
									document.setCreatorId(hit.field("c_creator_id").getValue().toString());
								} else {
									document.setCreatorId(null);
								}
								if (hit.field("c_source_id") != null) {
									document.setSourceId(hit.field("c_source_id").getValue().toString());
								} else {
									document.setSourceId(null);
								}
								if (hit.field("c_create_time") != null) {
									document.setCreateTime(Long.valueOf(hit.field("c_create_time").getValue().toString()));
								} else {
									document.setCreateTime(0);
								}
								documentList.add(document);
							}
						}
					}
					documentListRes.setDocumentList(documentList);
					resultMsg = "操作成功";
					resultCode = CommonParameters.resultCode_OK;
//					logger.info("resultMsg =操作成功;resultCode = 200");
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("resultMsg =操作失败;resultCode = 201");
			logger.error("/documentSearch/simpleSearchList.do--->" + e);
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 文档快速查询接口的结束时间为：" + StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 文档快速查询接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, documentListRes, request, response);
		}
	}

	@RequestMapping("/advancedSearchList.do")
	public void advancedSearchList(HttpServletRequest request, HttpServletResponse response) {
		logger.info("execute--->文档高级查询：/documentSearch/advancedSearchList.do");
		Date startDate = new Date();
		String resultMsg = "simpleSearchList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		DocumentSearchListRes documentListRes = null;
		String[] fileTypes = { "txt", "data", "doc", "xls", "ppt", "pdf" };
		String[] sourceOriginalTypes = { "open", "own", "trade", "upload" };
		String sid = null;
		logger.info("id是：" + sid + ", 文档高级查询接口的开始时间为：" + StringUtil.format(startDate));
		try {
			sid = request.getParameter("sid");
			String reqData = request.getParameter("reqData");
			if (sid == null || reqData == null) {
				resultMsg = "操作失败:参数不完整";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.info("resultMsg =操作失败;resultCode = 201");
				logger.error("/documentSearch/advancedSearchList.do--->参数不完整");
			} else {
				DocumentSearchListReq searchListReq = JSON.parseObject(reqData, DocumentSearchListReq.class);
				String fileName = searchListReq.getFileName();
				String fileNameTerm = searchListReq.getFileNameTerm();
				String summary = searchListReq.getSummary();
				String summaryTerm = searchListReq.getSummaryTerm();
				List<String> fileTypeList = searchListReq.getFileTypeList();
				List<String> sourceOriginalTypeList = searchListReq.getSourceOriginalTypeList();
				Long createTimeBegin = searchListReq.getCreateTimeBegin();
				Long createTimeEnd = searchListReq.getCreateTimeEnd();
				List<String> lockStatusList = searchListReq.getLockStatusList();
				String lockUser = searchListReq.getLockUser();
				String lockUserTerm = searchListReq.getLockUserTerm();
				Long currentPage = searchListReq.getCurrentPage();
				Long pageSize = searchListReq.getPageSize();

				BoolQueryBuilder queryBuilderBool = QueryBuilders.boolQuery();

				// 筛选锁定人
				if (StringUtils.isNotEmpty(lockUser)) {
					BoolQueryBuilder queryLockUserBuilderBool = QueryBuilders.boolQuery();

					if (StringUtils.equals(lockUserTerm, CommonParameters.EQUAL)) {
						TermQueryBuilder queryLockUser = QueryBuilders.termQuery("c_lock_user", lockUser);
						queryLockUserBuilderBool.must(queryLockUser);
					} else if (StringUtils.equals(lockUserTerm, CommonParameters.UN_EQUAL)) {
						MatchQueryBuilder queryLockUser = new MatchQueryBuilder("c_lock_user", lockUser);
						queryLockUserBuilderBool.must(queryLockUser);
					}

					if (queryLockUserBuilderBool != null) {
						queryBuilderBool.must(queryLockUserBuilderBool);
					}
				}

				// 筛选文件名称
				if (StringUtils.isNotEmpty(fileName)) {
					BoolQueryBuilder queryFileNameBuilderBool = QueryBuilders.boolQuery();

					if (StringUtils.equals(fileNameTerm, CommonParameters.EQUAL)) {
						QueryStringQueryBuilder queryFileName = new QueryStringQueryBuilder(fileName);
						queryFileName.field("c_title");
						// TermQueryBuilder queryFileName =
						// QueryBuilders.termQuery("c_title", fileName);
						queryFileNameBuilderBool.must(queryFileName);
					} else if (StringUtils.equals(fileNameTerm, CommonParameters.UN_EQUAL)) {
						MatchQueryBuilder queryFileName = QueryBuilders.matchQuery("c_title", fileName);
						queryFileNameBuilderBool.must(queryFileName);
					}

					if (queryFileNameBuilderBool != null) {
						queryBuilderBool.must(queryFileNameBuilderBool);
					}
				}

				// 筛选文档简介
				if (StringUtils.isNotEmpty(summary)) {
					BoolQueryBuilder querySummaryBuilderBool = QueryBuilders.boolQuery();

					if (StringUtils.equals(summaryTerm, CommonParameters.EQUAL)) {
						QueryBuilder querySummary = QueryBuilders.termQuery("c_content", summary);
						querySummaryBuilderBool.must(querySummary);
					} else if (StringUtils.equals(summaryTerm, CommonParameters.UN_EQUAL)) {
						MatchQueryBuilder querySummary = QueryBuilders.matchQuery("c_content", summary);
						querySummaryBuilderBool.must(querySummary);
					}

					if (querySummaryBuilderBool != null) {
						queryBuilderBool.must(querySummaryBuilderBool);
					}
				}

				// 筛选文档类型
				if (!fileTypeList.isEmpty()) {
					BoolQueryBuilder queryFileTypeBuilderBool = QueryBuilders.boolQuery();

					for (String type : fileTypeList) {
						if (StringUtils.isNotEmpty(type)) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery("c_file_type", type);
							queryFileTypeBuilderBool.should(termQuery);
						} else {
							// 文件类型过滤
							BoolQueryBuilder queryFileTypeBuilder = QueryBuilders.boolQuery();
							for (String fielType : fileTypes) {
								if (StringUtils.isNotEmpty(fielType)) {
									TermQueryBuilder termQuery = QueryBuilders.termQuery("c_file_type", fielType);
									queryFileTypeBuilder.should(termQuery);
								}
							}
							if (queryFileTypeBuilder != null) {
								queryBuilderBool.must(queryFileTypeBuilder);
							}
						}
					}

					if (queryFileTypeBuilderBool != null) {
						queryBuilderBool.must(queryFileTypeBuilderBool);
					}
				}

				// 筛选文档来源
				if (!sourceOriginalTypeList.isEmpty()) {
					BoolQueryBuilder querySourceBuilderBool = QueryBuilders.boolQuery();

					for (String source : sourceOriginalTypeList) {
						if (StringUtils.isNotEmpty(source)) {
							TermQueryBuilder termQuery = QueryBuilders.termQuery("c_source_original_type", source);
							querySourceBuilderBool.should(termQuery);
						} else {
							// 来源类型过滤
							BoolQueryBuilder querySourceTypeBuilderBool = QueryBuilders.boolQuery();

							for (String type : sourceOriginalTypes) {
								if (StringUtils.isNotEmpty(type)) {
									TermQueryBuilder termQuery = QueryBuilders.termQuery("c_source_original_type", type);
									querySourceTypeBuilderBool.should(termQuery);
								}
							}
							if (querySourceTypeBuilderBool != null) {
								queryBuilderBool.must(querySourceTypeBuilderBool);
							}
						}
					}

					if (querySourceBuilderBool != null) {
						queryBuilderBool.must(querySourceBuilderBool);
					}
				}

				// 筛选时间段数据
				if ((createTimeBegin != null && createTimeBegin > 0) || (createTimeEnd != null && createTimeEnd > 0)) {
					RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("c_create_time");
					if (createTimeBegin != null && createTimeBegin > 0) {
						rangeQueryBuilder.from(createTimeBegin);
					}
					if (createTimeEnd != null && createTimeEnd > 0) {
						rangeQueryBuilder.to(createTimeEnd);
					}
					queryBuilderBool.must(rangeQueryBuilder);
				}

				// 筛选锁定状态
				if (!lockStatusList.isEmpty()) {
					BoolQueryBuilder queryLockStatusBuilderBool = QueryBuilders.boolQuery();

					if (lockStatusList.size() == 1) {
						for (String status : lockStatusList) {
							if (StringUtils.isNotEmpty(status)) {
								if (StringUtils.equals(status, CommonParameters.LOCK)) {
									TermQueryBuilder termQuery = QueryBuilders.termQuery("c_lock_status", status);
									queryLockStatusBuilderBool.must(termQuery);
								} else {
									TermQueryBuilder termQuery = QueryBuilders.termQuery("c_lock_status", CommonParameters.LOCK);
									queryLockStatusBuilderBool.mustNot(termQuery);
								}
							}
						}
					}

					if (queryLockStatusBuilderBool != null) {
						queryBuilderBool.must(queryLockStatusBuilderBool);
					}
				}

				List<String> fieldList = new ArrayList<String>();
				fieldList.add("eid");
				fieldList.add("c_hid");
				fieldList.add("c_title");
				fieldList.add("c_content");
				fieldList.add("c_file_type");
				fieldList.add("c_lock_status");
				fieldList.add("c_lock_user");
				fieldList.add("c_source_original_type");
				fieldList.add("c_creator_id");
				fieldList.add("c_source_id");
				fieldList.add("c_create_time");
				List<Map<String, Integer>> sortMapList = new ArrayList<Map<String, Integer>>();
				Map<String, Integer> sort = new HashMap<String, Integer>();
				sort.put("createTime", 1);
				sortMapList.add(sort);
				SearchHits hits = esClientService.searchIndex(GeneratorIndexAndType.generatorDocType(), new String[] { GeneratorIndexAndType.generatorAllIndex() }, fieldList, queryBuilderBool, true, sortMapList, false, "template", Integer.parseInt(currentPage.toString()), Integer.parseInt(pageSize.toString()));
				documentListRes = new DocumentSearchListRes();
				documentListRes.setCurrentPage(currentPage);
				documentListRes.setTotalCount(hits.getTotalHits());
				documentListRes.setTotalPage(esClientService.getTotalPage(hits.totalHits(), Integer.parseInt(pageSize.toString())));
				List<Document> documentList = new ArrayList<Document>();
				if (hits != null && hits.totalHits() > 0) {
					for (SearchHit hit : hits) {
						if (!hit.fields().keySet().isEmpty()) {
							Document document = new Document();
							document.setEid(hit.getId());
							if (hit.field("c_title") != null) {
								document.setFileName(StringUtil.replaceBlank(hit.field("c_title").getValue().toString()));
							} else {
								document.setFileName(null);
							}
							if (hit.field("c_hid") != null) {
								document.setHid(hit.field("c_hid").getValue().toString());
							} else {
								document.setHid(null);
							}
							if (hit.field("c_file_type") != null) {
								document.setFileType(hit.field("c_file_type").getValue().toString());
							} else {
								document.setFileType(null);
							}
							if (hit.field("c_lock_status") != null) {
								document.setLockStatus(hit.field("c_lock_status").getValue().toString());
							} else {
								document.setLockStatus(CommonParameters.UN_LOCK);
							}
							if (hit.field("c_lock_user") != null) {
								document.setLockUser(hit.field("c_lock_user").getValue().toString());
							} else {
								document.setLockUser(null);
							}
							if (hit.field("c_source_original_type") != null) {
								document.setSourceOriginalType(hit.field("c_source_original_type").getValue().toString());
							} else {
								document.setSourceOriginalType(null);
							}
							if (hit.field("c_creator_id") != null) {
								document.setCreatorId(hit.field("c_creator_id").getValue().toString());
							} else {
								document.setCreatorId(null);
							}
							if (hit.field("c_source_id") != null) {
								document.setSourceId(hit.field("c_source_id").getValue().toString());
							} else {
								document.setSourceId(null);
							}
							// document.setSourceId(hit.field("c_source_id").getValue().toString());
							document.setSourceId(null);
							if (hit.field("c_create_time") != null) {
								document.setCreateTime(Long.valueOf(hit.field("c_create_time").getValue().toString()));
							} else {
								document.setCreateTime(0);
							}
							documentList.add(document);
						}
					}
				}
				documentListRes.setDocumentList(documentList);
				resultMsg = "操作成功";
				resultCode = CommonParameters.resultCode_OK;
//				logger.info("resultMsg =操作成功;resultCode = 200");
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("resultMsg =操作失败;resultCode = 201");
			logger.error("/documentSearch/advancedSearchList.do--->" + e);
		} finally {
			Date endDate = new Date();
			logger.info("id是：" + sid + ", 文档高级查询接口的结束时间为：" +  StringUtil.format(endDate));
			logger.info("id是：" + sid + ", 文档高级查询接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, documentListRes, request, response);
		}
	}

	@RequestMapping("/lockDocuments.do")
	public void lockDocument(HttpServletRequest request, HttpServletResponse response) {
		logger.info("execute--->文档批量锁定：/documentSearch/lockDocuments.do");
		Date startDate = new Date();
		String resultMsg = "simpleSearchList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = null;
		logger.info("id 是：" + sid + ", 文档批量锁定接口的开始时间为：" +  StringUtil.format(startDate));
		try {
			sid = request.getParameter("sid");
			String reqData = request.getParameter("reqData");
			if (sid == null || reqData == null) {
				resultMsg = "操作失败:参数不完整";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.info("resultMsg =操作失败;resultCode = 201");
				logger.error("/documentSearch/lockDocuments.do--->参数不完整");
			} else {
				logger.info("reqData" + reqData);
				DocumentlockReq documentlockReq = JSON.parseObject(reqData, DocumentlockReq.class);
				List<String> eidList = documentlockReq.getEidList();
				String lockUser = documentlockReq.getLockUser();
				if (eidList.isEmpty() || !StringUtils.isNotEmpty(lockUser)) {
					resultMsg = "操作失败:锁定人或者文件为空";
					resultCode = CommonParameters.resultCode_ERROR;
					logger.info("resultMsg =操作失败;resultCode = 201");
					logger.error("/documentSearch/lockDocuments.do--->锁定人或者文件为空");
				} else {
					Map<String, Object> json = new HashMap<String, Object>();
					json.put(StringUtil.getColumnString("lockUser"), lockUser);
					json.put(StringUtil.getColumnString("lockStatus"), CommonParameters.LOCK);
					for (String eid : eidList) {
						String indexName = esClientService.getIndexById(eid);
						esClientService.updateIndex(indexName, "doc", json, eid);
					}
					resultMsg = "操作成功";
					resultCode = CommonParameters.resultCode_OK;
//					logger.info("resultMsg =操作成功;resultCode = 200");
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("resultMsg =操作失败;resultCode = 201");
			logger.error("/documentSearch/lockDocuments.do--->" + e);
		} finally {
			Date endDate = new Date();
			logger.info("id 是：" + sid + ", 文档批量锁定接口的结束时间为：" +   StringUtil.format(endDate));
			logger.info("id 是：" + sid + ", 文档批量锁定接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, null, request, response);
		}
	}

	@RequestMapping("/unLockDocuments.do")
	public void unLockDocument(HttpServletRequest request, HttpServletResponse response) {
		logger.info("execute--->文档批量解锁：/documentSearch/unLockDocuments.do");
		Date startDate = new Date();
		String resultMsg = "simpleSearchList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = null;
		logger.info("id是：" + sid + "文档批量解锁接口的开始时间为：" + StringUtil.format(startDate));
		try {
			sid = request.getParameter("sid");
			String reqData = request.getParameter("reqData");
			if (sid == null || reqData == null) {
				resultMsg = "操作失败：参数不完整";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.info("resultMsg =操作失败;resultCode = 201");
				logger.error("/documentSearch/unLockDocuments.do--->参数不完整");
			} else {
				logger.info("reqData" + reqData);
				DocumentlockReq documentlockReq = JSON.parseObject(reqData, DocumentlockReq.class);
				List<String> eidList = documentlockReq.getEidList();
				String lockUser = documentlockReq.getLockUser();
				if (eidList.isEmpty()) {
					resultMsg = "操作失败:文件为空";
					resultCode = CommonParameters.resultCode_ERROR;
					logger.info("resultMsg =操作失败;resultCode = 201");
					logger.error("/documentSearch/unLockDocuments.do--->文件为空");
				} else {
					Map<String, Object> json = new HashMap<String, Object>();
					json.put(StringUtil.getColumnString("lockUser"), lockUser);
					json.put(StringUtil.getColumnString("lockStatus"), CommonParameters.UN_LOCK);
					for (String eid : eidList) {
						String indexName = esClientService.getIndexById(eid);
						esClientService.updateIndex(indexName, "doc", json, eid);
					}
					resultMsg = "操作成功";
					resultCode = CommonParameters.resultCode_OK;
//					logger.info("resultMsg =操作成功;resultCode = 200");
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("resultMsg =操作失败;resultCode = 201");
			logger.error("/documentSearch/unLockDocuments.do--->" + e);
		} finally {
			Date endDate = new Date();
			logger.info("id是：" + sid + "文档批量解锁接口的结束时间为：" +  StringUtil.format(endDate));
			logger.info("id是：" + sid + "文档批量解锁接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, null, request, response);
		}
	}

	@RequestMapping("/deleteDocuments.do")
	public void deleteDocument(HttpServletRequest request, HttpServletResponse response) {
		logger.info("execute--->文档批量删除：/documentSearch/unLockDocuments.do");
		Date startDate = new Date();
		String resultMsg = "simpleSearchList.do";
		String resultCode = CommonParameters.resultCode_ERROR;
		String sid = null;
		logger.info("id是：" + sid + ", 文档批量删除接口的开始时间为：" + StringUtil.format(startDate));
		try {
			sid = request.getParameter("sid");
			String reqData = request.getParameter("reqData");
			if (sid == null || reqData == null) {
				resultMsg = "参数不完整";
			} else {
				logger.info("reqData" + reqData);
				DocumentlockReq documentlockReq = JSON.parseObject(reqData, DocumentlockReq.class);
				List<String> eidList = documentlockReq.getEidList();
				if (eidList.isEmpty()) {
					logger.info("resultMsg =操作失败: eidList为null或为空;  resultCode = 201;");
					resultMsg = "操作失败: eidList为null或为空";
					resultCode = CommonParameters.resultCode_ERROR;
				} else {
					for (String eid : eidList) {
						String indexName = esClientService.getIndexById(eid);
						esClientService.deleteIndex(indexName, "doc", eid);
					}
					resultMsg = "操作成功";
					resultCode = CommonParameters.resultCode_OK;
//					logger.info("resultMsg =操作成功;resultCode = 200");
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = CommonParameters.resultCode_ERROR;
			logger.error("resultMsg =操作失败;resultCode = 201");
			logger.error("/documentSearch/deleteDocuments.do--->" + e);
		} finally {
			Date endDate = new Date();
			logger.info("id是：" + sid + ", 文档批量删除接口的结束时间为：" +  StringUtil.format(endDate));
			logger.info("id是：" + sid + ", 文档批量删除接口的执行时间为：" + StringUtil.pastTime(startDate, endDate));
			resultResponse(resultMsg, sid, resultCode, null, request, response);
		}
	}
}