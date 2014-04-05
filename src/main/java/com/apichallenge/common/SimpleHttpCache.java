package com.apichallenge.common;

import org.apache.commons.logging.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

import java.io.*;
import java.util.*;

public class SimpleHttpCache {
	private static final String baseDirectory = File.separator + File.separator + "httpcache";

	static {
		File file = new File(baseDirectory);
		if (!file.exists() || !file.isDirectory() || !file.canRead() || !file.canWrite()) {
			throw new IllegalStateException("please create directory '" + baseDirectory + "' and make sure it is readable and writable");
		}
	}

	private static final Log LOG = LogFactory.getLog(SimpleHttpCache.class);

	private static String url2Filename(String url) {
		String filename = url;
		filename = filename.replaceAll("https?://", baseDirectory + File.separator + File.separator);
		filename = filename.replaceAll("\\?", File.separator + File.separator);
		filename = filename.replaceAll("/", File.separator + File.separator);
		filename = filename.replaceAll(File.separator + File.separator + "+$", "");
		return filename;
	}

	private static String getHtmlProper(String url) {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(url);

		StringBuilder stringBuilder = null;
		try {
			HttpResponse response = httpclient.execute(httpGet);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			stringBuilder = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			LOG.error(e);
		} finally {
			httpGet.releaseConnection();
		}

		return (stringBuilder == null) ? null : stringBuilder.toString();
	}

	public static String getHtmlFromUrl(String url) {
		return getHtmlFromUrl(url, false);
	}

	public static void wipeCache(String url) {
		File file = new File(url2Filename(url));

		if (file.exists()) {
			file.delete();
		}
	}

	public static String getHtmlFromUrl(String url, boolean fresh) {
		String filename = url2Filename(url);
		File file = new File(filename);

		String html = null;

		if (file.exists() && fresh) {
			file.delete();
		}

		if (file.exists()) {
			try {
				html = new Scanner(file).useDelimiter("\\Z").next();
			} catch (FileNotFoundException e) {
				LOG.error(e);
			}
		} else {
			for (int i = 1; i <= 3; i++) {
				html = getHtmlProper(url);
				if (html != null) {
					break;
				}
				try {
					LOG.debug("going to sleep, likely to be kind to espn");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOG.error(e);
				}
			}

			String parentDirectory = filename;
			parentDirectory = parentDirectory.replaceAll("[^\\\\/]+$", "");

			File parentDirectoryFile = new File(parentDirectory);
			if (!parentDirectoryFile.exists()) {
				parentDirectoryFile.mkdirs();
			}

			try {
				FileWriter fstream = new FileWriter(filename);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(html);
				out.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}

		return html;
	}
}
