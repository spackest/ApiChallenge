package com.apichallenge.common;

public class EspnEntry {
	private int id;
	private String name;
	private String url;

	public EspnEntry(int id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
}
