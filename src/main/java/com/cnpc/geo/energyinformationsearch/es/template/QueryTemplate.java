package com.cnpc.geo.energyinformationsearch.es.template;

import com.alibaba.fastjson.annotation.JSONField;

public class QueryTemplate {
	
	@JSONField(name="c_key_word")
	private String keyWord;
	
	@JSONField(name="c_create_time")
	private Long createTime;
	
	@JSONField(name="c_user_id")
	private String userId;
	
	@JSONField(name="c_session_id")
	private String sessionId;
	
	public String getKeyWord() {
		return keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
}
