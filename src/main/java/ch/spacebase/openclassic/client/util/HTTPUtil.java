package ch.spacebase.openclassic.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class HTTPUtil {

	public static String fetchUrl(String url, String params) {
		return fetchUrl(url, params, url);
	}
	
	public static String fetchUrl(String url, String params, String referer) {
		try {
			URLConnection conn = makeConnection(url, params, referer, true);
			InputStream in = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			StringBuffer buffer = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}

			reader.close();

			return buffer.toString();
		} catch (IOException e) {
		}

		return "";
	}

	private static URLConnection makeConnection(String url, String params, String referer, boolean paramBoolean) throws IOException {
		URLConnection conn = new URL(url).openConnection();
		conn.addRequestProperty("Referer", referer);

		conn.setReadTimeout(40000);
		conn.setConnectTimeout(15000);
		conn.setDoInput(true);

		conn.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
		//conn.addRequestProperty("Accept-Encoding", "gzip, deflate, compress");
		conn.addRequestProperty("Connection", "keep-alive");

		if (params.length() > 0) {
			conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.addRequestProperty("Content-Length", Integer.toString(params.length()));
			conn.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(params);
			out.flush();
			out.close();
		}

		conn.connect();
		return conn;
	}

	public static String getParameterOffPage(String page, String param) {
		String str = "param name=\"" + param + "\" value=\"";
		int index = page.indexOf(str);
		
		if (index > 0) {
			index += str.length();
			int index2 = page.indexOf("\"", index);
			
			if (index2 > 0) {
				return page.substring(index, index2);
			}
		}

		return "";
	}
	

	
}
