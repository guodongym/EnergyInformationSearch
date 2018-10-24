/*package com.cnpc.geo.energyinformationsearch.es.controller; 

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cnpc.geo.energyinformationsearch.base.util.JsonUtil;
import com.cnpc.geo.energyinformationsearch.es.ElasticSearchClientService;
*//**
 * @ClassName: ElasticSearchController
 * @Description:控制类
 * @author zhangshuai
 * @date 2015.4.16 11:13:50
 * @version V1.0
 * @ChangeHistoryList    version    modifier        date                description
 *                        V1.0     zhangshuai     2015.4.16 11:13:50
 *//*
@Controller
public class ElasticSearchController {
	Logger log = Logger.getLogger(ElasticSearchController.class.getClass());
	@Autowired
	private ElasticSearchClientService elasticSearchHandler;

	*//**
	 * 
	 * @Title: index 
	 * @Description: 测试用
	 * @author louyujie
	 * @param request
	 * @param response
	 * @return
	 *//*
	@RequestMapping("/index.do")
	@ResponseBody
	public String ceshi(HttpServletRequest request,HttpServletResponse response){
		return "index";
	}
	
	*//** 
	 * @Title: index
	 * @Description:创建索引
	 * @throws 
	 *//* 
	@ResponseBody
	@RequestMapping("/es")
	public void index(HttpServletRequest request, HttpServletResponse response) {
		log.error("执行ElasticSearchController类中的 index()方法");
		Map<String, String[]> parameterMap = null;
		PrintWriter writer = null;
		try {
			parameterMap = request.getParameterMap();
			IndexResponse createIndexResponse = elasticSearchHandler.createIndexResponse(parameterMap.get("index")[0], parameterMap.get("type")[0], parameterMap.get("data")[0]);
			boolean created = createIndexResponse.isCreated();
			writer = response.getWriter();
			writer.print(created);
		} catch (IOException e) {
			log.error("执行ElasticSearchController类中index()方法出错");
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}
	
	*//** 
	 * @Title: searchData
	 * @Description:接收平台组传来的信息
	 * @throws 
	 *//* 
	
	@ResponseBody
	@RequestMapping("/es/searchData")
	public void searchData(HttpServletResponse response, String keyWord, String currentPage,String indexType,String pageSize ) throws Exception {
		log.info("执行ElasticSearchController类中的 searchData()方法");
		PrintWriter writer = null;
		try {
			if (StringUtils.equals(currentPage, "") || currentPage == null) {
				currentPage = "1";
			}
			String jsonData = bulidJson(keyWord, currentPage, indexType, pageSize);
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			writer = response.getWriter();
			writer.print(jsonData);
		} catch (IOException e) {
			log.error("执行ElasticSearchController类中的searchData()方法失败");
			e.printStackTrace();
		} finally {
			if(writer!=null){
				writer.close();
			}
			
		}
	}
	*//** 
	 * @Title: bulidJson
	 * @Description: 执行搜索，并把结果转化为字符串
	 * @throws 
	 *//* 
	private String bulidJson(String keyWord, String currentPage,String indexType,String pageSize) throws Exception {
		Map<String, String> searchData = elasticSearchHandler.searchData(keyWord, Integer.parseInt(currentPage), indexType, pageSize);
		Map<String, String> map = new HashMap<String, String>();
		int totalCount = Integer.parseInt(searchData.get("totalCount"));
		int pages=Integer.parseInt(pageSize);
		map.put("data", searchData.get("data"));
		map.put("totalPage", getTotalPage(totalCount,pages));
		map.put("totalCount", searchData.get("totalCount"));
		String jsonData = JsonUtil.getJsonValueFromStrMap(map);
		return jsonData;
	}
	*//**
	 * 返回总页数
	 * @param totalCount
	 * @param pageSize
	 * @return
	 *//*
	public String getTotalPage(int totalCount,int pageSize){
		int x=totalCount/pageSize;
		int s=totalCount%pageSize;
		int count=0;
		if(x>0&&s==0){
			count=x;
		}else if(x>0&&s>0 ){
			count = x+1;
		}else if(x<=0){
			count =1;
		}
		return count+"";
	}
}*/
