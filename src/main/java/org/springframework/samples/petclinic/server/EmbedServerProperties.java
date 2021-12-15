package org.springframework.samples.petclinic.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = EmbedServerProperties.PREFIX)
public class EmbedServerProperties {

	public static final String PREFIX = "embed.server";

	private Integer port;

	private String address;

	private String accessToken;

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
