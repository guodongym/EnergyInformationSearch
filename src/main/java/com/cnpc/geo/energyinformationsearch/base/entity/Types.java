package com.cnpc.geo.energyinformationsearch.base.entity;

import java.util.ArrayList;
import java.util.List;

public class Types {
	
	public enum Languages {
		/** 中文  **/
		chn,
		/** 英文  **/
		eng,
		/** 俄文  **/
		rus;
		
		public static List<String> getAll(){
			List<String> languages = new ArrayList<String>();
			for (Languages language : Languages.values()) {
				languages.add(language.toString());
			}
			return languages;
		}
	}
	
	public enum docTypes {
		doc
	}
}

