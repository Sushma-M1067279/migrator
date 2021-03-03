package com.mindtree.validator.http;

import java.security.cert.CertificateException;

import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

/**
 * The Class MyTrustManager.
 */
public class MyTrustManager implements X509TrustManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return new java.security.cert.X509Certificate[0];
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType) {
		/*Empty block method */
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) {
		/*Empty block method */
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws CertificateException {
		/*Empty block method */
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
			throws CertificateException {
		/*Empty block method */
	}

}
