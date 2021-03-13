package com.mindtree.validator.http;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.mindtree.core.service.MigratorServiceException;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.validator.model.AemQueryBuilderResponse;
import com.mindtree.validator.model.AemRequest;

public class RestHttpClient {

	private RestTemplate restClient;

	public RestTemplate getRestClient() throws MigratorServiceException {

		try {
			SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();

			TrustManager[] trustAllCerts = new TrustManager[] { new MyTrustManager() };

			Proxy proxy = new Proxy(Proxy.Type.HTTP,
					new InetSocketAddress(MigratorConstants.LOCAL_PROXY_HOST, MigratorConstants.LOCAL_PROXY_PORT));
			clientHttpReq.setProxy(proxy);

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HostnameVerifier allHostsValid = (hostname, session) -> true;
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			restClient = new RestTemplate(clientHttpReq);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new MigratorServiceException(e);
		}

		return restClient;
	}

	public void setRestClient(RestTemplate restClient) {
		this.restClient = restClient;
	}

	public ResponseEntity<AemQueryBuilderResponse> post(String url, AemRequest aemRequestBody)
			throws MigratorServiceException {
		HttpHeaders requestHeader = setHeaders();
		HttpEntity<AemRequest> aemRequestEntity = new HttpEntity<>(aemRequestBody, requestHeader);
		RestTemplate rClient = this.getRestClient();
		return rClient.exchange(url, HttpMethod.POST, aemRequestEntity, AemQueryBuilderResponse.class);
	}

	/**
	 * @return
	 */
	private HttpHeaders setHeaders() {
		HttpHeaders requestHeader = new HttpHeaders();
		requestHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		requestHeader.setContentType(MediaType.APPLICATION_JSON);
		StringBuilder credentials = new StringBuilder(System.getProperty("username"));
		credentials.append(":").append(System.getProperty("password"));
		String plainCreds = credentials.toString();
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		StringBuilder authorizationHeader = new StringBuilder("Basic ");
		requestHeader.add("Authorization", authorizationHeader.append(base64Creds).toString());
		return requestHeader;
	}

}
