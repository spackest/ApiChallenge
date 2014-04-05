package com.apichallenge.common;

import com.apichallenge.common.espn.*;
import org.apache.commons.logging.*;
import org.jsoup.*;

import java.io.*;
import java.util.*;

public class MyConnection {
	private Connection connection;
	private Connection.Response response;
	private Map<String, String> cookies;
	private List<EspnEntry> espnEntries;
	private int spid;

	private static final Log LOG = LogFactory.getLog(MyConnection.class);

	public MyConnection() {
	}

	public void setEspnEntries(List<EspnEntry> espnEntries) {
		this.espnEntries = espnEntries;
	}

	public List<EspnEntry> getEspnEntries() {
		return espnEntries;
	}

	public Connection.Response get(String url, Map<String, String> form) throws IOException {
		Connection.Response thisResponse = request(Connection.Method.GET, url, form);
		return thisResponse;
	}

	public void post(String url, Map<String, String> form) throws IOException {
		request(Connection.Method.POST, url, form);
	}

	private Connection.Response request(Connection.Method method, String url, Map<String, String> form) throws IOException {
		if (connection == null) {
			connection = Jsoup.connect(url);
		}

		if (cookies != null && cookies.size() > 0) {
			connection = connection.cookies(cookies);
		}

		if (form != null) {
			connection = connection.data(form);
		}

		connection.followRedirects(true);

		for (int i = 1; i <= 3; i++) {
			try {
				response = connection.url(url).method(method).execute();
				break;
			} catch (Exception e) {
				LOG.debug("trying to " + method + " " + url + " again");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}

		Map<String, String> theseCookies = response.cookies();

		if (theseCookies != null && theseCookies.size() > 0) {
			cookies = response.cookies();
		}

		return response;
	}

	public int getSpid() {
		return spid;
	}

	public void setSpid(int spid) {
		this.spid = spid;
	}
}

