package com.example.demo.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.OAuth2Parameters;

import com.example.demo.GoogleOauth2Properties;

@Configuration
public class GoogleOauthConfig {

	@Bean
	public GoogleConnectionFactory connectionFactory(GoogleOauth2Properties properties) {
		return new GoogleConnectionFactory(properties.getClientId(), properties.getClientSecret());
	}

	@Bean
	public OAuth2Parameters parameters(GoogleOauth2Properties properties) {
		Map<String, List<String>> map = new HashMap<>();
		map.put("scope", Arrays.asList("openid email profile"));
		map.put("redirect_uri", Arrays.asList(properties.getAuth().getCallbackUrl()));
		return new OAuth2Parameters(map);
	}
}
