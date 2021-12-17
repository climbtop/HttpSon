package com.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConfigArgs {

	public static String CHARSET = "GBK";
	public static String ARGS_CONFIG = "./args.txt";

	public static String[] readArgs(String[] args) {
		if (!fileExists(ARGS_CONFIG)) {
			if (args.length > 0 && args[0].endsWith(ARGS_CONFIG.substring(2))) {
				if (fileExists(args[0])) {
					ARGS_CONFIG = args[0];
				}
			}
		}
		if (!fileExists(ARGS_CONFIG))
			return args;
		List<String> argsList = new ArrayList<String>();
		try {
			StringBuffer b = new StringBuffer();
			Stack<Character> s = new Stack<Character>();
			Boolean marks = false;
			Reader r = new InputStreamReader(getInputStream(ARGS_CONFIG), CHARSET);
			int t;
			while ((t = r.read()) != -1) {
				if (t == 10 || t == 13)
					break;
				if (t == 65279)
					continue;
				char c = (char) t;
				if (c == '"') {
					s.push('"');
					if (s.size() == 2) {
						s.pop();
						s.pop();
						b.append('"');
					}
					if (b.length() == 0) {
						marks = true;
					}
				} else if ((c == ' ' || c == '\t') && s.size() == 0) {
					if (s.size() == 2) {
						s.pop();
						s.pop();
					}
					if (marks)
						b.deleteCharAt(b.length() - 1);
					if (marks || b.length() > 0) {
						argsList.add(b.toString());
					}
					b.delete(0, b.length());
					marks = false;
				} else {
					b.append(c);
				}
			}
			if (b.length() > 0) {
				if (marks)
					b.deleteCharAt(b.length() - 1);
				if (marks || b.length() > 0) {
					argsList.add(b.toString());
				}
			}
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (argsList.size() == 0)
			return args;

		String[] newArgs = new String[argsList.size()];
		for (int i = 0; i < argsList.size(); i++) {
			newArgs[i] = argsList.get(i);
			System.out.print(newArgs[i] + " ");
		}
		System.out.println();
		return newArgs;
	}

	private static boolean fileExists(String confFile) {
		return confFile != null && (confFile.contains("://") || new File(confFile).exists());
	}

	private static InputStream getInputStream(String confFile) throws Exception {
		if (confFile != null && new File(confFile).exists()) {
			return new FileInputStream(confFile);
		} else if (confFile != null && confFile.contains("://")) {
			byte[] readData = readFromURL(confFile);
			if (readData != null && readData.length > 0) {
				return new ByteArrayInputStream(readData);
			}
		}
		return null;
	}

	private static void setHttpSSLNoVerifier(HttpsURLConnection connection) {
		try {
			TrustManager[] trustAllCerts = new TrustManager[1];
			trustAllCerts[0] = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
				}
			};
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
			connection.setSSLSocketFactory(sslSocketFactory);

			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			connection.setHostnameVerifier(hostnameVerifier);
		} catch (Exception e) {
		}
	}

	private static byte[] readFromURL(String urlStr) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HttpURLConnection connection = null;
		InputStream stream = null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			if (urlStr.toLowerCase().startsWith("https")) {
				setHttpSSLNoVerifier((HttpsURLConnection) connection);
			}
			connection.setReadTimeout(30000);
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			connection.connect();
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				return null;
			}
			stream = connection.getInputStream();
			if (stream != null) {
				byte[] b = new byte[1024];
				int z;
				while ((z = stream.read(b, 0, b.length)) != -1) {
					baos.write(b, 0, z);
				}
			}
		} catch (Exception e) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
				}
			}
		}
		return baos.toByteArray();
	}

}