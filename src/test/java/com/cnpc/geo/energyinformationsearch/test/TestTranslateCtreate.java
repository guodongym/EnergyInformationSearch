package com.cnpc.geo.energyinformationsearch.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientConfig;
import com.cnpc.geo.energyinformationsearch.es.client.ElasticSearchClientService;

public class TestTranslateCtreate {
	static ElasticSearchClientService esclientService;
	private static BufferedReader bufferedReader;

	static {
		ElasticSearchClientConfig clientConfig = new ElasticSearchClientConfig();
		clientConfig.setClusterName("elasticsearchdemo");
		clientConfig.setNodeList("hdpdemo12.cnpc.hdp.com:9300");
		clientConfig.setPingTimeOut("10s");
		clientConfig.setSniff(true);
		esclientService = new ElasticSearchClientService(clientConfig);
	}

	public static void main(String[] args) {
		String path = "F:\\ES词典\\英中词典\\英中词典;F:\\ES词典\\中英词典\\中英词典";
		// String path =
		// "C:\\Users\\lau\\Desktop\\ES词典\\英中词典\\英中词典;C:\\Users\\lau\\Desktop\\ES词典\\中英词典\\中英词典";
		String[] urls = path.split("\\;");

		for (int i = 0; i < urls.length; i++) {
			List<File> filelist = getFileList(urls[i]);
			Iterator<File> iter = filelist.iterator();

			while (iter.hasNext()) {
				File file = (File) iter.next();
				try {
					bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
					// StringBuilder stringBuilder = new StringBuilder();
					String content;
					int count = 1;

					while ((content = bufferedReader.readLine()) != null) {
						// stringBuilder.append(content);
						if (count > 1) {
							// System.out.println(content);
							int sum = 1;
							String[] results = content.split(",");
							// System.out.println("原词汇为 "+results[0]);

							// 处理解释
							for (int k = 1; k < results.length; k++) {
								String[] res = results[k].split("\\|");
								for (int j = 0; j < res.length; j++) {
									if (sum < 3) {
										String a = res[j].replaceAll("[\"]", "");
										a = a.replaceAll("[a-z]+:", "");
										// 在此处添加存储ES代码----------------------------------------------------------
										Map<String, String> map = new HashMap<String, String>();
										System.out.println("key=" + results[0]);
										System.out.println("value=" + a);
										map.put("key", results[0]);
										map.put("value", a);
										sum++;
										esclientService.createIndex("translation-chn-2015", "translation", JSON.toJSONString(map));
										// ES存储结束----------------------------------------------------------------
									}
								}

							}

						}
						count++;
					}

					// System.out.println(stringBuilder.toString());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			// if(iter.hasNext()){}
		}
	}

	/**
	 * 使用递归的方法读取文件夹
	 * 
	 * @param strPath
	 * @return
	 */
	public static List<File> getFileList(String strPath) {
		List<File> filelist = new ArrayList<File>();
		File dir = new File(strPath);
		File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
			System.out.println("不为空");
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) { // 判断是文件还是文件夹
					System.out.println("是文件夹");
					getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
				} else if (fileName.endsWith("csv")) { // 判断文件名是否以.avi结尾
					// String strFileName = files[i].getAbsolutePath();
					// System.out.println("---" + strFileName);
					filelist.add(files[i]);
				} else {
					continue;
				}
			}

		}
		return filelist;
	}

}
