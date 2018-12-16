package com.example.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.oauth2")
@Data
public class GoogleOauth2Properties {

	private String clientId;
	private String clientSecret;
	private Auth auth = new Auth();
	private Token token = new Token();

	@Data
	public static class Auth {
		private String url;
		private String callbackUrl;
	}

	@Data
	public static class Token {
		private String url;
	}
}
