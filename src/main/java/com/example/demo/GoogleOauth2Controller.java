package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class GoogleOauth2Controller {

	private RestTemplate restTemplate;

	private GoogleOauth2Properties properties;

	public GoogleOauth2Controller(RestTemplate restTemplate, GoogleOauth2Properties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	@RequestMapping("/oauth2/google")
	public String auth(HttpSession session) {

		session.setAttribute("state", UUID.randomUUID().toString());

		UriComponents googleOauthUrl = UriComponentsBuilder.fromHttpUrl(properties.getAuth().getUrl())
				.queryParam("client_id", properties.getClientId())
				.queryParam("response_type", "code")
				.queryParam("scope", "openid email profile")
				.queryParam("redirect_uri", properties.getAuth().getCallbackUrl())
				.queryParam("state", session.getAttribute("state"))
				.queryParam("access_type", "offline")
				.queryParam("approval_prompt", "force")
				.build();

		return "redirect:" + googleOauthUrl.toUriString();
	}

	@ResponseBody
	@RequestMapping("/oauth2/google/callback")
	public String authCallback(HttpSession session, @RequestParam String state, @RequestParam String code) {

		String sessionState = (String) session.getAttribute("state");

		if (!StringUtils.equals(sessionState, state)) {
			return "redirect:/error";
		}

		UriComponents googleTokenUrl = UriComponentsBuilder.fromHttpUrl(properties.getToken().getUrl()).build();

		Map<String, String> body = new HashMap<>();
		body.put("code", code);
		body.put("client_id", properties.getClientId());
		body.put("client_secret", properties.getClientSecret());
		body.put("redirect_uri", properties.getAuth().getCallbackUrl());
		body.put("grant_type", "authorization_code");

		try {
			ResponseEntity<Map> postForEntity = restTemplate.postForEntity(googleTokenUrl.toUri(), body, Map.class);

			return new StringBuilder()
					.append("status ===========================><br>" + postForEntity.getStatusCode().name() + "<br>")
					.append("header ===========================><br>" + postForEntity.getHeaders().toString() + "<br><br>")
					.append("body ===========================><br>" + postForEntity.getBody().toString() + "<br><br>")
					.toString();
		} catch (RestClientException e) {
			e.printStackTrace();
			return "redirect:/error";
		}
	}

	@ResponseBody
	@RequestMapping("/me")
	public String getEmail(@RequestHeader("Authorization") String authorization) {

		if (!authorization.startsWith("Bearer")) {
			return "redirect:/error";
		}

		String idToken = authorization.split(" ")[1];
		UriComponents googleEmailOfUserUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/oauth2/v3/tokeninfo")
				.queryParam("id_token", idToken)
				.build();

		try {
			ResponseEntity<String> postForEntity = restTemplate.getForEntity(googleEmailOfUserUrl.toUriString(), String.class);

			return new StringBuilder()
					.append("status ===========================><br>" + postForEntity.getStatusCode().name() + "<br>")
					.append("header ===========================><br>" + postForEntity.getHeaders().toString() + "<br><br>")
					.append("body ===========================><br>" + postForEntity.getBody() + "<br><br>")
					.toString();
		} catch (RestClientException e) {
			e.printStackTrace();
			return "redirect:/error";
		}
	}
}
