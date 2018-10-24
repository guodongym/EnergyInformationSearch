package com.cnpc.geo.energyinformationsearch.thrift.infaceimpl;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cnpc.geo.energyinformationsearch.base.entity.CommonParameters;
import com.cnpc.geo.energyinformationsearch.base.util.GeneratorIndexAndType;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;
import com.cnpc.geo.energyinformationsearch.hbase.client.HbaseClientService;
import com.cnpc.geo.energyinformationsearch.thrift.inface.DReqData;
import com.cnpc.geo.energyinformationsearch.thrift.inface.DResData;
import com.cnpc.geo.energyinformationsearch.thrift.inface.SearchInfoService;
import com.cnpc.geo.energyinformationsearch.thrift.model.FileEntity;

import kafka.javaapi.producer.Producer;

@Component("searchInfo")
public class SearchInfoServiceImpl implements SearchInfoService.Iface {

	Logger logger = Logger.getLogger(SearchInfoServiceImpl.class);

	@Resource(name = "hbaseClientService")
	private HbaseClientService hbaseClientService;
	@Resource(name = "producer")
	private Producer<String, String> producer;
	@Resource(name = "elasticSearchClientService")
	private ElasticSearchClientService elasticSearchClientService;

	private String oriFile = "c_original_file";// 源文件列
	private String swfFile = "c_swf_file";// swf文件列
	private String tableName = "INTERNET_DATA";// 表名
	private String columnFamily = "cf_file_bin_info";// 列族
	private String indexType = "doc";// 索引类型
	private String indexName = GeneratorIndexAndType.generatorAllIndex();// 索引名称
	private String downCountField = "c_download_count";// 下载数量
	private String EHId = "c_hid";// HbaseId
//	private String FileMD5 = "c_file_md5";

	/*
	 * zhangshuai 下载文件接口
	 */
	@SuppressWarnings("deprecation")
	@Override
	public DResData download(DReqData request) throws TException {
		logger.info("SearchInfoServiceImpl中的download----------------");
		String sequenceID = request.getSequenceID();
		String eID = request.getEID();
		DResData response = null;
		Map<String, Object> json = null;

		try {
			response = new DResData();
			json = new HashMap<String, Object>();
			if ("".equals(eID) && eID == null) {
				response.setResultCode(CommonParameters.ERRORCODE);
				response.setResultMsg(CommonParameters.ERRORMEG + "----原因是:eID=null或者为空");
				response.setResType(CommonParameters.download);

				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:eID=null或者为空" + "ResType:" + CommonParameters.download);
			} else {
				indexName = elasticSearchClientService.getIndexById(eID);
				GetResponse GetResEs = elasticSearchClientService.get(indexName, indexType, eID, new String[] { downCountField, EHId });
				if (!GetResEs.isExists()) {
					response.setResultCode(CommonParameters.ERRORCODE);
					response.setResultMsg(CommonParameters.ERRORMEG + "----原因是:GetResEs=null或者为空");
					response.setResType(CommonParameters.download);
					logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:GetResEs=null或者为空" + "ResType:" + CommonParameters.download);
				} else {
					Long downCount = Long.parseLong(GetResEs.getField(downCountField) == null ? "0" : GetResEs.getField(downCountField).getValue().toString());
					logger.info("------downCount =-----------" + downCount + "---------------");
					String hID = (String) GetResEs.getField(EHId).getValue();
					long countDown = downCount + 1;
					logger.info("----------------countDown= " + countDown + "------------");
					json.put(downCountField, countDown);
					indexName = elasticSearchClientService.getIndexById(eID);
					elasticSearchClientService.updateIndex(indexName, indexType, json, eID);
					hbaseClientService.updateHbase(tableName, hID, "cf_user_info", "c_download_count", countDown);
					response = new DResData();
					if ("".equals(sequenceID) && sequenceID == null) {
						response.setResultCode(CommonParameters.ERRORCODE);
						response.setResultMsg(CommonParameters.ERRORMEG + "----原因是:sequenceId=null或者为空");
						response.setResType(CommonParameters.download);
						logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:sequenceId=null或者为空" + "ResType:" + CommonParameters.download);
					} else if ("".equals(hID) && hID == null) {
						response.setResultCode(CommonParameters.ERRORCODE);
						response.setResultMsg(CommonParameters.ERRORMEG + "----原因是:hId=null或者为空");
						response.setResType(CommonParameters.download);
						logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:hId=null或者为空" + "ResType:" + CommonParameters.download);
					} else {
						byte fc[] = hbaseClientService.selectRow(tableName, hID).getColumnLatest(Bytes.toBytes(columnFamily), Bytes.toBytes(oriFile)).getValue();
						if (fc != null) {
							response.setOriginalFile(ByteBuffer.wrap(fc));
							response.setResType(CommonParameters.download);
							response.setResultCode(CommonParameters.SUCCESSCODE);
							response.setResultMsg(CommonParameters.SUCCESSMEG);
							logger.info("ResultCode :" + CommonParameters.SUCCESSCODE + "ResultMsg:" + CommonParameters.SUCCESSMEG + "ResType:" + CommonParameters.download);
						} else {
							response.setResType(CommonParameters.download);
							response.setResultCode(CommonParameters.ERRORCODE);
							response.setResultMsg(CommonParameters.ERRORMEG + "原因是获取数据失败");
							logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:hId=null或者为空" + "ResType:" + CommonParameters.download);
						}
						response.setSid(sequenceID);
					}
				}
			}
		} catch (Exception e) {
			logger.error("/SearchInfoServiceImpl/download()-->");
			e.printStackTrace();
		} finally {
			json = null;
		}
		return response;
	}

	/*
	 * zhangshuai 上传文件接口
	 */
	@Override
	public DResData upload(DReqData res) {
		String sequenceID = res.getSequenceID();// 消息id
		String fileName = res.getFileName().substring(0, res.getFileName().lastIndexOf("."));// 文件名称
		String fileExtention = res.getFileExtention();// 文件后缀
		String tags = res.getTags();// 标签
		String description = res.getDescription();// 摘要
		String title = res.getTitle();// 标题
		SimpleDateFormat ff = new SimpleDateFormat("yyyy-mm-dd HH:MM:SS");
		String starttime = ff.format(new Date());
		System.out.println(starttime + "------------starttime--------------------------");
		byte[] fileContent = res.getOriginalFile();// 文件内容
		String endtime = ff.format(new Date());
		System.out.println(endtime + "------------endtime--------------------------");
		String userID = res.getUserID();// 上传者
		DResData response = null;
		FileEntity fileEntity = null;
		logger.info("传过来的数据值：sequenceID=" + sequenceID + " , fileName=" + fileName + " , fileExtention=" + fileExtention + " , " + "tags=" + tags + " , description = " + description + " , title=" + title + "  , userID= " + userID);
		try {
			response = new DResData();
			if ("".equals(sequenceID) || sequenceID == null) {
				response.setResultCode(CommonParameters.ERRORCODE);
				response.setResultMsg(CommonParameters.ERRORMEG);
				response.setResType(CommonParameters.upload);
				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:sequenceID==null或者为空" + "ResType:" + CommonParameters.upload);
				response.setSid(sequenceID);
			} else if ("".equals(fileName) || fileName == null) {
				response.setResultCode(CommonParameters.ERRORCODE);
				response.setResultMsg(CommonParameters.ERRORMEG);
				response.setResType(CommonParameters.upload);
				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:fileName=null或者为空" + "ResType:" + CommonParameters.upload);
				response.setSid(sequenceID);
			} else if ("".equals(title) || title == null) {
				response.setResultCode(CommonParameters.ERRORCODE);
				response.setResultMsg(CommonParameters.ERRORMEG);
				response.setResType(CommonParameters.upload);
				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:title=null或者为空" + "ResType:" + CommonParameters.upload);
				response.setSid(sequenceID);
			} else {
//				String meg = checkFileContent(fileContent);
//				if ("error".equals(meg)) {
//					response.setResultCode(CommonParameters.ERRORCODE);
//					response.setResultMsg(CommonParameters.ERRORMEG + "文件已经存在");
//					response.setResType(CommonParameters.upload);
//					logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----" + "ResType:" + CommonParameters.upload);
//				} else if (("success".equals(meg))) {
				fileEntity = new FileEntity();
				fileEntity.setDescription(description);
				fileEntity.setFileContent(fileContent);
				fileEntity.setFileName(fileName);
				fileEntity.setSequenceID(sequenceID);
				fileEntity.setFileExtention(fileExtention);
				fileEntity.setTags(tags);
				fileEntity.setTitle(title);
				fileEntity.setUri(sequenceID + "_" + userID);
				fileEntity.setUserID(userID);
				String jsondata = JSON.toJSONString(fileEntity, SerializerFeature.WriteMapNullValue);
				
				UploadThread uploadThread = new UploadThread();
				uploadThread.setJsondata(jsondata);
				uploadThread.start();
				
					// logger.info(jsondata+"--------------------------");
//					KeyedMessage<String, String> data = new KeyedMessage<String, String>("fileUploadTopic", jsondata);
//					String kakfstart = ff.format(new Date());
//					System.out.println(kakfstart + "------------kakfstart--------------------------");
//					producer.send(data);
//					String kafkaend = ff.format(new Date());
//					System.out.println(kafkaend + "------------kafkaend--------------------------");
//					logger.info("消息" + "发送kafka成功----------------");
					
				response.setResultCode(CommonParameters.SUCCESSCODE);
				response.setResultMsg(CommonParameters.SUCCESSMEG);
				response.setResType(CommonParameters.upload);
				logger.info("ResultCode :" + CommonParameters.SUCCESSCODE + "ResultMsg:" + CommonParameters.SUCCESSCODE + "----" + "ResType:" + CommonParameters.upload);
//				}
				response.setSid(sequenceID);
			}
		} catch (Exception e) {
			response.setResultCode(CommonParameters.ERRORCODE);
			response.setResultMsg(CommonParameters.ERRORMEG);
			response.setResType(CommonParameters.upload);
			logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----" + "ResType:" + CommonParameters.upload);
			logger.error("/SearchInfoServiceImpl/upload()-->", e);
		} finally {
			fileEntity = null;
		}
		return response;
	}

	/*
	 * zhangshuai 预览文件接口
	 */
	@Override
	public DResData preview(DReqData res) throws TException {
		logger.info("SearchInfoServiceImpl中的preview-------------");
		String sequenceID = res.getSequenceID();
		String hID = res.getHID();
		logger.info(JSON.toJSONString(res) + "========json=======");
		logger.info("-----------" + hID + "==============");
		DResData respose = null;
		try {
			respose = new DResData();
			if ("".equals(sequenceID) && sequenceID == null) {
				respose.setResultCode(CommonParameters.ERRORCODE);
				respose.setResultMsg(CommonParameters.ERRORMEG + "----原因是:sequenceId=null或者为空");
				respose.setResType(CommonParameters.previwe);
				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:sequenceId=null或者为空" + "ResType:" + CommonParameters.previwe);
			} else if ("".equals(hID) && hID == null) {
				respose.setResultCode(CommonParameters.ERRORCODE);
				respose.setResultMsg(CommonParameters.ERRORMEG + "----原因是:hId=null或者为空");
				respose.setResType(CommonParameters.previwe);
				logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "----原因是:hId=null或者为空" + "ResType:" + CommonParameters.previwe);
			} else {
				@SuppressWarnings("deprecation")
				byte fc[] = hbaseClientService.selectRow(tableName, hID).getColumnLatest(Bytes.toBytes(columnFamily), Bytes.toBytes(swfFile)).getValue();
				if (fc != null) {
					respose.setSwfFile(ByteBuffer.wrap(fc));
					respose.setResultCode(CommonParameters.SUCCESSCODE);
					respose.setResType(CommonParameters.previwe);
					respose.setResultMsg(CommonParameters.SUCCESSMEG);
					logger.info("ResultCode :" + CommonParameters.SUCCESSMEG + "ResultMsg:" + CommonParameters.SUCCESSMEG + "" + "ResType:" + CommonParameters.previwe);
				} else {
					respose.setResultCode(CommonParameters.ERRORCODE);
					respose.setResultMsg(CommonParameters.ERRORMEG + "原因是获取数据失败");
					respose.setResType(CommonParameters.previwe);
					logger.info("ResultCode :" + CommonParameters.ERRORCODE + "ResultMsg:" + CommonParameters.ERRORMEG + "原因是获取数据失败" + "ResType:" + CommonParameters.previwe);
				}
				respose.setSid(sequenceID);
			}
		} catch (Exception e) {
			logger.error("/SearchInfoServiceImpl/preview()-->", e);
		} finally {
		}
		return respose;
	}

	/**
	 * 
	 * 
	 * @param fileContent
	 *            文件内容
	 * @return String success 表示成功 error 表示失败
	 */

//	public String checkFileContent(byte[] fileContent) {
//
//		String fileContentMD5 = MD5Util.MD5(new String(fileContent));
//		SearchHits hits = elasticSearchClientService.getByField(new String[] { indexType }, new String[] { indexName }, FileMD5, fileContentMD5);
//		System.out.println(hits.getHits().length + "--------------------------------------");
//		if (hits.getHits().length > 0) {
//			logger.info("文件已经上传");
//			return "error";
//		} else {
//			logger.info("文件上传成功！！！");
//			return "success";
//		}
//	}

	public HbaseClientService getHbaseClientService() {
		return hbaseClientService;
	}

	public void setHbaseClientService(HbaseClientService hbaseClientService) {
		this.hbaseClientService = hbaseClientService;
	}

	public ElasticSearchClientService getElasticSearchClientService() {
		return elasticSearchClientService;
	}

	public void setElasticSearchClientService(ElasticSearchClientService elasticSearchClientService) {
		this.elasticSearchClientService = elasticSearchClientService;
	}

	public Producer<String, String> getProducer() {
		return producer;
	}

	public void setProducer(Producer<String, String> producer) {
		this.producer = producer;
	}
}
