/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mixare.data.XMLHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.util.Log;


public class DownloadManager implements Runnable {

	private boolean stop = false;
	private boolean  pause = false;
	private boolean proceed = false;

	public static int NOT_STARTED = 0;
	public static int CONNECTING = 1;
	public static int CONNECTED = 2;
	public static int PAUSED = 3;
	public static int STOPPED = 4;
	
	private int state = NOT_STARTED;

	private int id = 0;
	private HashMap<String, DownloadRequest> todoList = new HashMap<String, DownloadRequest>();
	private HashMap<String, DownloadResult> doneList = new HashMap<String, DownloadResult>();
	InputStream is;
	public boolean isLauncherStarted=false;
	
	int retry = 0;
	DownloadResult dRes;
	private String currJobId = null;
	
	private MixContext ctx;
	private XMLHandler xml;
	

	public boolean isStop() {
		return stop;
	}
	
	/**
	 * @return the xml
	 */
	public XMLHandler getXml() {
		return xml;
	}

	/**
	 * @param xml the xml to set
	 */
	public void setXml(XMLHandler xml) {
		this.xml = xml;
	}

	public DownloadManager(MixContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		String jobId;
		DownloadRequest request;
		DownloadResult result;

		stop = false;
		pause = false;
		proceed = false;
		state = CONNECTING;

		while (!stop) {
			jobId = null;
			request = null;
			result = null;

			while (!stop && !pause) {
				
				checkState();
				synchronized (this) {
					if (todoList.size() > 0) {
						jobId = getNextReqId();
						request = todoList.get(jobId);
						proceed = true;
					}
				}
				if (proceed) {
					state = CONNECTED;
					currJobId = jobId;
					result = processRequest(request);

					synchronized (this) {
						todoList.remove(jobId);
						doneList.put(jobId, result);
						proceed = false;
					}
				}
				state = CONNECTING;

				if (!stop && !pause)
					sleep(100);
				
				
			}

			while (!stop && pause) {
				state = PAUSED;
				sleep(100);
			}
			state = CONNECTING;
			
		}
		state = STOPPED;
	}
	public int checkForConnection(){
		return state;
	}

	private void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (java.lang.InterruptedException ex) {

		}
	}

	private String getNextReqId() {
		return todoList.keySet().iterator().next();
	}

	/**
	 * process the xml data from server
	 * @param request
	 * @return
	 */
	private DownloadResult processRequest(DownloadRequest request) {
		DownloadResult result = new DownloadResult();
		try {
			if(ctx.getHttpGETInputStream(request.url)!=null){
				is = ctx.getHttpGETInputStream(request.url);
				String tmp = ctx.getHttpInputString(is);
				
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		        //Document doc = builder.parse(is);
				Document doc = builder.parse(new InputSource(new StringReader(tmp)));

				//Document doc = builder.parse(is);
		        
				xml = new XMLHandler(ctx, this);

				Log.i(MixView.TAG, "loading XML data");	
				xml.load(doc);

				result.obj = xml;
				result.format = request.format;
				result.error = false;
				result.errorMsg = null;				
				//}

			ctx.returnHttpInputStream(is);
			is = null;
			}


		} catch (Exception ex) {
			Log.i("DownloadManager", "Exception");
			ex.printStackTrace();
			//ex.printStackTrace();
			result.obj = null;
			result.error = true;
			result.errorMsg = ex.getMessage();
			result.errorRequest = request;

			try {
				ctx.returnHttpInputStream(is);
			} catch (Exception ignore) {
			}

		}

		currJobId = null;

		return result;
	}


	public synchronized void purgeLists() {
		todoList.clear();
		doneList.clear();
	}

	public synchronized String submitJob(DownloadRequest job) {
		String jobId = "ID_" + (id++);
		todoList.put(jobId, job);

		return jobId;
	}

	public synchronized boolean isReqComplete(String jobId) {
		return doneList.containsKey(jobId);
	}

	public synchronized DownloadResult getReqResult(String jobId) {
		DownloadResult result = doneList.get(jobId);
		doneList.remove(jobId);

		return result;
	}

	public String getActiveReqId() {
		return currJobId;
	}
	
	public void pause() {
		pause = true;
	}

	public void restart() {
		pause = false;
	}

	public void stop() {
		this.stop = true;
	}
	
	/**
	 * state of downloading data, attempts data download 10 times
	 */
	public void checkState() {
		MixState mixState = ctx.getDataView().getState();
		
		if (MixState.nextLStatus == MixState.NOT_STARTED ) {

			DownloadRequest request1 = new DownloadRequest();

			if (!ctx.getStartUrl().equals("")){
				request1.url = ctx.getStartUrl();
				isLauncherStarted=true;
			}
			else
//				request1.url = MixView.AR_WEBSERVER_URL;
			mixState.downloadId = ctx.getDownloader().submitJob(request1);

			MixState.nextLStatus = MixState.PROCESSING;

		} else if (MixState.nextLStatus == MixState.PROCESSING) {
			if (ctx.getDownloader().isReqComplete(mixState.downloadId)) {
				dRes = ctx.getDownloader().getReqResult(mixState.downloadId);

				if (dRes.error && retry < 10) {
					retry++;
					MixState.nextLStatus = MixState.NOT_STARTED;
				} else {
					retry = 0;
					if (dRes.obj != null) {
						MixState.nextLStatus = MixState.DONE;
						MixView.downloadFailed = false;
					}else {
						MixState.nextLStatus = MixState.NOT_STARTED;
						if (MixView.isOnScreen)
							MixView.handler.sendEmptyMessage(MixView.WIFI_3G_TOAST);
					}
					stop();
				}	
			}
		} 
	}
}

class DownloadRequest {
	int format;
	String url;
	String params;
}

class DownloadResult {
	int format;
	Object obj;

	boolean error;
	String errorMsg;
	DownloadRequest errorRequest;
}
