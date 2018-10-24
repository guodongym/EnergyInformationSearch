package com.cnpc.geo.energyinformationsearch.thrift.infaceimpl;

import com.cnpc.geo.realtime.base.bolt.upload.UploadTask;

public class UploadThread extends Thread {

	private String jsondata;

	@Override
	public void run() {
		UploadTask.execute(jsondata);
	}

	public String getJsondata() {
		return jsondata;
	}

	public void setJsondata(String jsondata) {
		this.jsondata = jsondata;
	}
}
