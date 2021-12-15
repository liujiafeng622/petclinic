package org.springframework.samples.petclinic.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.server.PetClinicResponse;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	public static PetClinicResponse postBody(String url, String accessToken, int timeout, Object requestObj) {
		HttpURLConnection connection = null;
		BufferedReader bufferedReader = null;
		try {
			URL realUrl = new URL(url);
			connection = (HttpURLConnection) realUrl.openConnection();

			boolean useHttps = url.startsWith("https");
			if (useHttps) {
				HttpsURLConnection https = (HttpsURLConnection) connection;
				trustAllHosts(https);
			}

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setReadTimeout(timeout * 1000);
			connection.setConnectTimeout(3 * 1000);
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

			if (accessToken != null && accessToken.trim().length() > 0) {
				connection.setRequestProperty("ACCESS-TOKEN", accessToken);
			}

			connection.connect();

			if (requestObj != null) {
				String requestBody = JSONUtil.toJsonStr(requestObj);

				DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
				dataOutputStream.write(requestBody.getBytes("UTF-8"));
				dataOutputStream.flush();
				dataOutputStream.close();
			}

			int statusCode = connection.getResponseCode();
			if (statusCode != 200) {
				return new PetClinicResponse(PetClinicResponse.FAIL,
						"call remoting fail, StatusCode(" + statusCode + ") invalid. for url : " + url);
			}

			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				result.append(line);
			}
			String resultJson = result.toString();

			try {
				PetClinicResponse<List<String>> response = JSONUtil.toBean(resultJson,
						new TypeReference<PetClinicResponse<List<String>>>() {
						}, true);
				return response;
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
				return new PetClinicResponse(PetClinicResponse.FAIL,
						"remoting (url=" + url + ") response content invalid(" + resultJson + ").");
			}

		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new PetClinicResponse(PetClinicResponse.FAIL,
					"remoting error(" + e.getMessage() + "), for url : " + url);
		}
		finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (connection != null) {
					connection.disconnect();
				}
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	// trust-https start
	private static void trustAllHosts(HttpsURLConnection connection) {
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory newFactory = sc.getSocketFactory();

			connection.setSSLSocketFactory(newFactory);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		connection.setHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

	private static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	} };

}
