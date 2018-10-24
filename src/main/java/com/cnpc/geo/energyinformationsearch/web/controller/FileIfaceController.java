package com.cnpc.geo.energyinformationsearch.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.index.get.GetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.base.controller.BaseController;
import com.cnpc.geo.energyinformationsearch.base.entity.CommonParameters;
import com.cnpc.geo.energyinformationsearch.base.util.StringUtil;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.file.model.DocumentOperateModel;
import com.cnpc.geo.energyinformationsearch.hbase.client.HbaseClientService;
import com.cnpc.geo.energyinformationsearch.search.entity.FileContentReq;
import com.cnpc.geo.energyinformationsearch.search.entity.FileContentRes;
import com.cnpc.geo.energyinformationsearch.search.entity.FileScoreReq;

/**
 * 
 * @Package: com.cnpc.geo.energyinformationsearch.web.controller
 * @ClassName: FileIfaceController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年6月29日 上午10:47:02
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年6月29日
 *                    上午10:47:02
 */

@Controller
@RequestMapping("/fileIface")
public class FileIfaceController extends BaseController {

	Logger logger = Logger.getLogger(FileIfaceController.class);

	@Autowired
	ElasticSearchClientService esClientservice;

	@Autowired
	HbaseClientService hbaseClientService;

	/**
	 * 
	 * @Title: fileContent
	 * @Description: 详情页展示，并更新hbase查看数 es索引
	 * @author zhaoguodong
	 * @param request
	 * @param response
	 */
	@RequestMapping("/fileContent.do")
	public void fileContent(HttpServletRequest request, HttpServletResponse response) {
		// hbaseClientService.createTable("INTERNET_DATA");
		// hbaseClientService.insertData("INTERNET_DATA");
		logger.info("FileIfaceController-------------------->>fileContent.do");
		Date startDate = new Date();
		String resultMsg = "fileContent.do";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");

		logger.info("id是：" + sid + ", 详情页展示，并更新hbase查看数 es索引的结束时间是：" +  StringUtil.format(startDate));
		
		FileContentReq fileContentReq = JSON.parseObject(reqData, FileContentReq.class);
		String eID = fileContentReq.getEid();
		String indexName = esClientservice.getIndexById(eID);
		GetResponse getResponse = esClientservice.get(indexName, "doc", eID, null);
		String hID = getResponse.getSource().get("c_hid").toString();
		String creatorId = getResponse.getSource().get("c_creator_id").toString();
		long createTime = Long.parseLong(getResponse.getSource().get("c_create_time").toString());
		// System.out.println(hID);
		FileContentRes fileContentRes = new FileContentRes();
		if(getResponse.getSource().get("c_collect_count") != null) {
			fileContentRes.setCollectCount(Integer.valueOf(getResponse.getSource().get("c_collect_count").toString()));
		} else {
			fileContentRes.setCollectCount(0);
		}
		if(getResponse.getSource().get("c_description") != null) {
			fileContentRes.setDescription(getResponse.getSource().get("c_description").toString());
		} else {
			fileContentRes.setDescription(null);
		}


		Set<String> set = new HashSet<String>();
		set.add("cf_file_info:c_file_name");
		set.add("cf_file_info:c_file_size");
		set.add("cf_file_info:c_file_extension");
		set.add("cf_file_bin_info:c_original_file");
		set.add("cf_content_info:c_title");
		set.add("cf_user_info:c_view_count");
		set.add("cf_user_info:c_download_count");
		set.add("cf_user_info:c_score_sum");
		set.add("cf_user_info:c_score_count");
		set.add("cf_admin_info:c_creator_id");
		set.add("cf_admin_info:c_create_time");
		set.add("cf_datasource_info:c_source_original_type");
		set.add("cf_datasource_info:c_uri");
		set.add("cf_datasource_info:c_source_format_type");

		Result rs = hbaseClientService.selectRowAddColumns("INTERNET_DATA", hID, set);

		if (rs.isEmpty()) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultResponse(resultMsg, sid, resultCode, "", request, response);
			return;
		}
		fileContentRes.setHid(hID);
		fileContentRes.setFileName(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_name"))));
		String fileSize = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_size")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_size")));
		fileContentRes.setFileSize(Long.valueOf(fileSize));
		String fileType = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_extension")))) ? "" : new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_extension")));
		fileContentRes.setFileType(fileType);
		if ("html".equals(fileType)) {
			fileContentRes.setContent(new String(rs.getValue(Bytes.toBytes("cf_file_bin_info"), Bytes.toBytes("c_original_file"))));
		} else {
			fileContentRes.setContent("");
		}
		fileContentRes.setTitle(StringUtil.replaceBlank(new String(rs.getValue(Bytes.toBytes("cf_content_info"), Bytes.toBytes("c_title")))));

		String sourceOriginalType = new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_source_original_type"))).trim();

		fileContentRes.setSourceOriginalType(sourceOriginalType);

		String viewCount = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_view_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_view_count")));
		fileContentRes.setViewCount(Integer.valueOf(viewCount));
		String downloadCount = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_download_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_download_count")));
		fileContentRes.setDownloadCount(Integer.valueOf(downloadCount));
		int scoreCount = Integer.valueOf(StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_count"))));
		Double scoreSum = Double.valueOf(StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_sum")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_sum"))));
		fileContentRes.setScoreCount(scoreCount);
		Double scoreAvg = 0.00;
		if (scoreCount != 0) {
			scoreAvg = (double) (Math.round((scoreSum / Double.valueOf(scoreCount)) * 10.0) / 10.0);
		}
		fileContentRes.setScoreAvg(scoreAvg);
		// String createTime = StringUtils.isBlank(new
		// String(rs.getValue(Bytes.toBytes("cf_admin_info"),
		// Bytes.toBytes("c_create_time"))))?"0":new
		// String(rs.getValue(Bytes.toBytes("cf_admin_info"),
		// Bytes.toBytes("c_create_time")));
		fileContentRes.setCreateTime(createTime);
		fileContentRes.setUri(new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_uri"))));
		fileContentRes.setSourceFormatType(new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_source_format_type"))));
		fileContentRes.setCreatorId(creatorId);
		Map<String, Object> hashMap = new HashMap<>();
		hashMap.put("cf_user_info:c_view_count", fileContentRes.getViewCount() + 1);
		// 更新Hbase查看数
		hbaseClientService.writeRow("INTERNET_DATA", hID, hashMap);

		// 更新索引
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("c_view_count", fileContentRes.getViewCount() + 1);
		esClientservice.updateIndex(indexName, "doc", jsonMap, eID);

		Date endDate = new Date();
		logger.info("id是：" + sid + ", 详情页展示，并更新hbase查看数 es索引的结束时间是：" + StringUtil.format(endDate));
		logger.info("id是：" + sid + ", 详情页展示，并更新hbase查看数 es索引的执行时间是：" + StringUtil.pastTime(startDate, endDate) );
		resultResponse(resultMsg, sid, resultCode, fileContentRes, request, response);

	}

	@RequestMapping("/fileContentNotLock.do")
	public void fileContentNotLock(HttpServletRequest request, HttpServletResponse response) {
		logger.info("FileIfaceController-------------------->>fileContent.do");
		Date startDate = new Date();
		String resultMsg = "fileContent.do";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		FileContentRes fileContentRes = new FileContentRes();
		
		logger.info("id是：" + sid + ",fileContentNotLock.do的开始时间是：" +  StringUtil.format(startDate));

		FileContentReq fileContentReq = JSON.parseObject(reqData, FileContentReq.class);
		String eID = fileContentReq.getEid();
		String indexName = esClientservice.getIndexById(eID);
		if (StringUtils.isEmpty(indexName)) {
			resultMsg = "delete";
			resultResponse(resultMsg, sid, resultCode, fileContentRes, request, response);
			return;
		}
		GetResponse getResponse = esClientservice.get(indexName, "doc", eID, null);
		// String object =
		// getResponse.getSource().get(StringUtil.getColumnString("lockStatus")).toString();
		String object = null;
		if (getResponse.getField(StringUtil.getColumnString("lockStatus")) != null) {
			object = getResponse.getField(StringUtil.getColumnString("lockStatus")).getValue().toString();
		}
		if (StringUtils.isNotEmpty(object)) {
			if (CommonParameters.LOCK.equals(object)) {
				resultMsg = "lock";
				resultResponse(resultMsg, sid, resultCode, fileContentRes, request, response);
				return;
			}
		}

		String hID = getResponse.getSource().get("c_hid").toString();
		String creatorId = getResponse.getSource().get("c_creator_id").toString();
		long createTime = Long.parseLong(getResponse.getSource().get("c_create_time").toString());
		if(getResponse.getSource().get("c_collect_count") != null) {
			fileContentRes.setCollectCount(Integer.valueOf(getResponse.getSource().get("c_collect_count").toString()));
		} else {
			fileContentRes.setCollectCount(0);
		}
		if(getResponse.getSource().get("c_description") != null) {
			fileContentRes.setDescription(getResponse.getSource().get("c_description").toString());
		} else {
			fileContentRes.setDescription(null);
		}

		Set<String> set = new HashSet<String>();
		set.add("cf_file_info:c_file_name");
		set.add("cf_file_info:c_file_size");
		set.add("cf_file_info:c_file_extension");
		set.add("cf_file_bin_info:c_original_file");
		set.add("cf_content_info:c_title");
		set.add("cf_user_info:c_view_count");
		set.add("cf_user_info:c_download_count");
		set.add("cf_user_info:c_score_sum");
		set.add("cf_user_info:c_score_count");
		set.add("cf_admin_info:c_creator_id");
		set.add("cf_admin_info:c_create_time");
		set.add("cf_datasource_info:c_source_original_type");
		set.add("cf_datasource_info:c_uri");
		set.add("cf_datasource_info:c_source_format_type");

		Result rs = hbaseClientService.selectRowAddColumns("INTERNET_DATA", hID, set);

		if (rs.isEmpty()) {
			resultCode = CommonParameters.resultCode_ERROR;
			resultResponse(resultMsg, sid, resultCode, "", request, response);
			return;
		}
		fileContentRes.setHid(hID);
		fileContentRes.setFileName(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_name"))));
		String fileSize = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_size")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_size")));
		fileContentRes.setFileSize(Long.valueOf(fileSize));
		String fileType = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_extension")))) ? "" : new String(rs.getValue(Bytes.toBytes("cf_file_info"), Bytes.toBytes("c_file_extension")));
		fileContentRes.setFileType(fileType);
		if ("html".equals(fileType)) {
			fileContentRes.setContent(new String(rs.getValue(Bytes.toBytes("cf_file_bin_info"), Bytes.toBytes("c_original_file"))));
		} else {
			fileContentRes.setContent("");
		}
		fileContentRes.setTitle(StringUtil.replaceBlank(new String(rs.getValue(Bytes.toBytes("cf_content_info"), Bytes.toBytes("c_title")))));

		String sourceOriginalType = new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_source_original_type"))).trim();

		fileContentRes.setSourceOriginalType(sourceOriginalType);

		String viewCount = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_view_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_view_count")));
		fileContentRes.setViewCount(Integer.valueOf(viewCount));
		String downloadCount = StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_download_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_download_count")));
		fileContentRes.setDownloadCount(Integer.valueOf(downloadCount));
		int scoreCount = Integer.valueOf(StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_count")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_count"))));
		Double scoreSum = Double.valueOf(StringUtils.isBlank(new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_sum")))) ? "0" : new String(rs.getValue(Bytes.toBytes("cf_user_info"), Bytes.toBytes("c_score_sum"))));
		fileContentRes.setScoreCount(scoreCount);
		Double scoreAvg = 0.00;
		if (scoreCount != 0) {
			scoreAvg = (double) (Math.round((scoreSum / Double.valueOf(scoreCount)) * 10.0) / 10.0);
		}
		fileContentRes.setScoreAvg(scoreAvg);
		// String createTime = StringUtils.isBlank(new
		// String(rs.getValue(Bytes.toBytes("cf_admin_info"),
		// Bytes.toBytes("c_create_time"))))?"0":new
		// String(rs.getValue(Bytes.toBytes("cf_admin_info"),
		// Bytes.toBytes("c_create_time")));
		fileContentRes.setCreateTime(createTime);
		fileContentRes.setUri(new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_uri"))));
		fileContentRes.setSourceFormatType(new String(rs.getValue(Bytes.toBytes("cf_datasource_info"), Bytes.toBytes("c_source_format_type"))));
		fileContentRes.setCreatorId(creatorId);
		Map<String, Object> hashMap = new HashMap<>();
		hashMap.put("cf_user_info:c_view_count", fileContentRes.getViewCount() + 1);
		// 更新Hbase查看数
		hbaseClientService.writeRow("INTERNET_DATA", hID, hashMap);

		// 更新索引
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("c_view_count", fileContentRes.getViewCount() + 1);
		esClientservice.updateIndex(indexName, "doc", jsonMap, eID);
		
		Date endDate = new Date();

		logger.info("id是：" + sid + ",fileContentNotLock.do的结束时间是：" +  StringUtil.format(endDate));
		logger.info("id是：" + sid + ",fileContentNotLock.do的执行时间是：" +  StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, fileContentRes, request, response);

	}

	/**
	 * 
	 * @Title: score
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @author quwu
	 * @param request
	 * @param response
	 */
	@RequestMapping("/score.do")
	public void score(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		String resultMsg = "执行score.do成功";
		String resultCode = CommonParameters.resultCode_OK;
		String sid = request.getParameter("sid");
		String reqData = request.getParameter("reqData");
		logger.info("id是：" + sid + ", 评分的开始时间是：" +  StringUtil.format(startDate));

		Double scoreAvg = 0.00;
		// 判断参数是否完整
		if (sid == null || reqData == null) {
			resultMsg = "参数不完整";
			resultCode = CommonParameters.resultCode_ERROR;
		} else {
			try {
				FileScoreReq fileScoreReq = JSON.parseObject(reqData, FileScoreReq.class);
				Map<String, Object> map = new HashMap<String, Object>();
				Map<String, Object> mapEs = new HashMap<String, Object>();

				List<String> fields = new ArrayList<String>();
				fields.add(StringUtil.getColumnString("hid"));
				fields.add(StringUtil.getColumnString("scoreCount"));
				fields.add(StringUtil.getColumnString("scoreSum"));

				String indexName = esClientservice.getIndexById(fileScoreReq.getEid());
				GetResponse esRes = esClientservice.get(indexName, "doc", fileScoreReq.getEid(), fields.toArray(new String[0]));

				if (!esRes.isExists()) {
					resultMsg = "索引ID不存在";
					resultCode = CommonParameters.resultCode_ERROR;
					logger.info("索引ID不存在");
				} else {
					String hid = null;
					GetField hidField = esClientservice.getField(esRes, "hid");
					if (hidField != null) {
						hid = (String) hidField.getValue();
					}
					logger.info("hid:" + hid);

					GetField scoreSumField = esClientservice.getField(esRes, "scoreSum");
					long scoreSum = 0;
					if (scoreSumField != null) {
						scoreSum = (long) scoreSumField.getValue();
					}

					GetField scoreCountField = esClientservice.getField(esRes, "scoreCount");
					long scoreCount = 0;
					if (scoreCountField != null) {
						scoreCount = (long) scoreCountField.getValue();
					}

					logger.info("scoreSumOld:" + scoreSum);
					logger.info("scoreCountOld:" + scoreCount);

					// 累加评分
					scoreSum = scoreSum + fileScoreReq.getScore();
					scoreCount = scoreCount + 1;

					// 平均分
					scoreAvg = (double) (Math.round((Double.valueOf(scoreSum) / Double.valueOf(scoreCount)) * 10.0) / 10.0);

					logger.info("scoreSum:" + scoreSum);
					logger.info("scoreCount:" + scoreCount);

					map.put(StringUtil.getColumnFamilyAndColumnString("userInfo", "scoreCount"), scoreCount);
					map.put(StringUtil.getColumnFamilyAndColumnString("userInfo", "scoreSum"), scoreSum);
					map.put(StringUtil.getColumnFamilyAndColumnString("userInfo", "scoreAvg"), scoreAvg);

					mapEs.put(StringUtil.getColumnString("scoreCount"), scoreCount);
					mapEs.put(StringUtil.getColumnString("scoreSum"), scoreSum);
					mapEs.put(StringUtil.getColumnString("scoreAvg"), scoreAvg);

					hbaseClientService.writeRow("INTERNET_DATA", hid, map);
					esClientservice.updateIndex(indexName, "doc", mapEs, fileScoreReq.getEid());
				}
			} catch (Exception e) {
				resultMsg = "内部处理异常";
				resultCode = CommonParameters.resultCode_ERROR;
				logger.error("/fileIface/score.do--->", e);
			}
		}
		Date  endDate = new Date();
		logger.info("id是：" + sid + ", 评分的结束时间是：" +  StringUtil.format(endDate));
		logger.info("id是：" + sid + ", 评分的执行时间是：" +  StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, null, request, response);
	}

	/**
	 * zhangshuai 文档做标记
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/fileDoMarker.do")
	public void FileDoMarker(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info("执行：FileIfaceController中FileDoMarker(HttpServletRequest request, HttpServletResponse response)的方法");
		String resultCode = "";
		String resultMsg = "";
		String eid = "";
		String flag = "";
		String sid = "";
		String json = "";
		List<DocumentOperateModel> DocumentOperateList = null;
		Map<String, Object> hashMap = null;
		logger.info("id是：" + sid + ",文档做标记的开始时间是：" + StringUtil.format(startDate));
		try {
			flag = request.getParameter("flag");
			sid = request.getParameter("sid");
			json = request.getParameter("documentOperateList");
			DocumentOperateList = JSON.parseArray(json, DocumentOperateModel.class);
			for (int i = 0; i < DocumentOperateList.size(); i++) {
				eid = DocumentOperateList.get(i).getEid();
				if ("".equals(sid.trim()) || sid == null) {
					resultMsg = "操作失败：sid=null或为空";
					resultCode = "201";
					logger.info("resultMsg = 操作失败：sid为null或为空,resultCode = 201");
				} else if ("".equals(eid.trim()) || eid == null) {
					resultMsg = "操作失败：eid=null或为空";
					resultCode = "201";
					logger.info("resultMsg = 操作失败：eid为null或为空,resultCode = 201");
				} else if ("".equals(flag.trim()) || flag == null) {
					resultMsg = "操作失败：flag=null或为空";
					resultCode = "201";
					logger.info("resultMsg = 操作失败：flag为null或为空,resultCode = 201");
				} else {
					hashMap = new HashMap<String, Object>();
					Boolean.valueOf(flag);
					hashMap.put(StringUtil.getColumnString("canBeSearch"), Boolean.valueOf(flag));
					logger.info(StringUtil.getColumnString("canBeSearch") + "---------------------");
					String indexName = esClientservice.getIndexById(eid);
					esClientservice.updateIndex(indexName, "doc", hashMap, eid);
					resultMsg = "操作成功";
					resultCode = "200";
					logger.info("resultMsg =操作成功,resultCode = 200");
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = "201";
			logger.info("resultMsg =操作失败,resultCode = 201");
			logger.error("/fileIface/fileDoMarker.do--->" + e);
		} finally {
			hashMap = null;
			DocumentOperateList = null;
		}
		Date endDate = new Date();
		logger.info("id是：" + sid + ",文档做标记的结束时间是：" + StringUtil.format(endDate));
		logger.info("id是：" + sid + ",文档做标记的执行时间是：" +  StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, null, request, response);
	}

	/**
	 * zhangshuai 删除文档
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/fileDelete.do")
	public void FileDelete(HttpServletRequest request, HttpServletResponse response) {
		Date startDate = new Date();
		logger.info("执行：FileIfaceController中FileDelete(HttpServletRequest request, HttpServletResponse response)的方法");
		String json = "";
		String eid = "";
		String hid = "";
		String resultMsg = "";
		String resultCode = "";
		String sid = "";
		List<DocumentOperateModel> DocumentOperateList = null;
		logger.info("id是：" + sid + ", 删除文档的开始时间是：" +  StringUtil.format(startDate));
		try {
			json = request.getParameter("documentOperateList");
			sid = request.getParameter("sid");
			DocumentOperateList = JSON.parseArray(json, DocumentOperateModel.class);
			for (int i = 0; i < DocumentOperateList.size(); i++) {
				eid = DocumentOperateList.get(i).getEid();
				hid = DocumentOperateList.get(i).getHid();
				if ("".equals(sid.trim()) || sid == null) {
					resultMsg = "操作失败：sid=null或为空";
					resultCode = "201";
					logger.info("resultMsg = 操作失败：sid为null或为空;resultCode = 201");
				} else if ("".equals(hid.trim()) || hid == null) {
					logger.info("resultMsg =操作失败: hid为null或为空;  resultCode = 201;");
					resultMsg = "操作失败: hid为null或为空";
					resultCode = "201";
				} else if ("".equals(eid.trim()) || eid == null) {
					logger.info("resultMsg =操作失败: eid为null或为空;  resultCode = 201;");
					resultMsg = "操作失败: eid为null或为空";
					resultCode = "201";
				} else {
					boolean isSuccess = true;
					// esClientservice.DeleteIndex(indexName, indexType, eid,
					// hid);
					if (isSuccess) {
						// hbaseClientService.deleteRow("INTERNET_DATA",hid);
						logger.info("操作成功:isSuccess=" + isSuccess + ";resultCode = 200;");
						resultMsg = "操作成功";
						resultCode = "200";
					} else {
						logger.info("操作失败:isSuccess=" + isSuccess + ";resultCode = 201;");
						resultMsg = "操作失败";
						resultCode = "201";
					}
				}
			}
		} catch (Exception e) {
			resultMsg = "操作失败";
			resultCode = "201";
			logger.info("resultMsg =操作失败;resultCode = 201");
			logger.error("/fileIface/fileDelete.do--->" + e);
		} finally {
			DocumentOperateList = null;
		}
		Date endDate = new Date();
		logger.info("id是：" + sid + ", 删除文档的结束时间是：" +  StringUtil.format(endDate));
		logger.info("id是：" + sid + ", 删除文档的执行时间是：" +  StringUtil.pastTime(startDate, endDate));
		resultResponse(resultMsg, sid, resultCode, null, request, response);
	}
}