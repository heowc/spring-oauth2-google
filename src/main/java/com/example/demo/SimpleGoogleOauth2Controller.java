package com.example.demo;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class SimpleGoogleOauth2Controller {

	private RestTemplate restTemplate;

	private GoogleOauth2Properties properties;

	public SimpleGoogleOauth2Controller(RestTemplate restTemplate, GoogleOauth2Properties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	@RequestMapping("/oauth2/google")
	public View auth() {
		UriComponents googleOauthUrl = UriComponentsBuilder.fromHttpUrl(properties.getAuth().getUrl())
				.queryParam("client_id", properties.getClientId())
				.queryParam("response_type", "code")
				.queryParam("scope", "openid email profile")
				.queryParam("redirect_uri", properties.getAuth().getCallbackUrl())
				.queryParam("state", UUID.randomUUID().toString())
				.queryParam("access_type", "offline")
				.queryParam("approval_prompt", "force")
				.build();

		return new RedirectView(googleOauthUrl.toUriString());
	}

	@ResponseBody
	@RequestMapping("/oauth2/google/callback")
	public ResponseEntity<String> authCallback(HttpSession session, @RequestParam String state, @RequestParam String code) {

		UriComponents googleTokenUrl = UriComponentsBuilder.fromHttpUrl(properties.getToken().getUrl()).build();

		Map<String, String> body = new HashMap<>();
		body.put("code", code);
		body.put("client_id", properties.getClientId());
		body.put("client_secret", properties.getClientSecret());
		body.put("redirect_uri", properties.getAuth().getCallbackUrl());
		body.put("grant_type", "authorization_code");

		try {
			return restTemplate.postForEntity(googleTokenUrl.toUri(), body, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	// Google + API 추가해야 함(https://console.developers.google.com/apis/api/plus.googleapis.com/overview)
	@ResponseBody
	@RequestMapping("/me")
	public ResponseEntity<String> getGoogleUser(@RequestHeader("Authorization") String authorization) {

		if (!authorization.startsWith("Bearer")) {
			return ResponseEntity.badRequest().build();
		}

		String accessToken = authorization.split(" ")[1];
		UriComponents googleUserUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/oauth2/v2/userinfo").build();

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken);
			return restTemplate.exchange(googleUserUrl.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@ResponseBody
	@RequestMapping("/refresh")
	public ResponseEntity<String> refresh(@RequestParam(name = "refresh_token") String refreshToken) {

		UriComponents googleTokenUrl = UriComponentsBuilder.fromHttpUrl(properties.getToken().getUrl()).build();

		Map<String, String> body = new HashMap<>();
		body.put("client_id", properties.getClientId());
		body.put("client_secret", properties.getClientSecret());
		body.put("refresh_token", refreshToken);
		body.put("grant_type", "refresh_token");

		try {
			return restTemplate.postForEntity(googleTokenUrl.toUri(), body, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}
