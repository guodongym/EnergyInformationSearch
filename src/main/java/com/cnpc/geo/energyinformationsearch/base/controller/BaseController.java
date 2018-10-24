package com.cnpc.geo.energyinformationsearch.base.controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cnpc.geo.energyinformationsearch.base.util.BeanUtil;

/**
 * 
 * @Package: com.cnpc.geo.crawlmaster.base.controller
 * @ClassName: BaseController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author quwu
 * @date 2015年4月10日 下午5:44:35
 * @version V1.0
 * @ChangeHistoryList version modifier date description V1.0 quwu 2015年4月10日
 *                    下午5:44:35
 */
public class BaseController {

	public Logger logger = Logger.getLogger(BaseController.class);

	public void resultResponse(String resultMsg, String sid, String resultCode, Object resData, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		// 设置返回response的消息内容
		jsonMap.put("resultMsg", resultMsg);
		jsonMap.put("sid", sid);
		jsonMap.put("resultCode", resultCode);
		jsonMap.put("resData", resData);
		// 返回接口应答
		writeRepnonseJson(jsonMap, response);
	}

	public void writeRepnonseJson(Object obj, HttpServletResponse response) {
		try {
			String result = JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
			writeRepnonse(result, response);
		} catch (Exception e) {
			logger.error("/BaseController/writeRepnonseJson()-->", e);
		}
	}

	public void writeRepnonse(String message, HttpServletResponse response) {
		try {
			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();

			out.print(message);
			out.flush();
			out.close();
		} catch (Exception e) {
			logger.error("/BaseController/writeRepnonse()-->", e);
		}
	}

	public Map<String, Object> parserToMap(Object obj) {
		Map<String, Object> map = BeanUtil.transBean2Map(obj);
		return map;
	}
}