package com.cnpc.geo.energyinformationsearch.base.util;

import java.util.ArrayList;
import java.util.List;

import com.cnpc.geo.energyinformationsearch.base.entity.Types;
import com.cnpc.geo.energyinformationsearch.base.entity.Types.docTypes;

public class GeneratorIndexAndType {

	/** 文档主索引Index前缀 **/
	private final static String DOC_INDEX_PREFIX = "doc";

	/** 索引名称分隔符 **/
	private final static String SEPARATOR = "-";

	/** 通配符 **/
	private final static String WILDCARD = "*";

	/**
	 * 
	 * @Title: generatorDocIndex
	 * @Description: 根据语言生成文档主索引IndexName
	 * @author zhaogd
	 * @param languages
	 * @return
	 */
	public static String[] generatorDocIndex(List<String> languages) {
		List<String> indexs = new ArrayList<String>();
		
		if(languages == null || languages.size()<= 0){
			return new String[] {generatorAllIndex()};
		}
		
		for (String language : languages) {
			StringBuffer index = new StringBuffer();
			index.append(DOC_INDEX_PREFIX);
			index.append(SEPARATOR);
			index.append(language);
			index.append(SEPARATOR);
			index.append(WILDCARD);
			indexs.add(index.toString());
		}
		return indexs.toArray(new String[0]);
	}

	public static String generatorAllIndex() {
		StringBuffer index = new StringBuffer();
		index.append(DOC_INDEX_PREFIX);
		index.append(SEPARATOR);
		index.append(WILDCARD);
		return index.toString();
	}

	/**
	 * 
	 * @Title: generatorDocType
	 * @Description: 生成文档主索引Type
	 * @author zhaogd
	 * @return
	 */
	public static String[] generatorDocType() {
		List<String> types = new ArrayList<String>();
		for (docTypes type : Types.docTypes.values()) {
			types.add(type.toString());
		}
		return types.toArray(new String[0]);
	}

	
	public static void main(String[] args) {
		System.out.println(generatorAllIndex());
	}
}
