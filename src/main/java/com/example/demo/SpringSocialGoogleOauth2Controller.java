package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.api.oauth2.UserInfo;
import org.springframework.social.google.connect.GoogleConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/social")
public class SpringSocialGoogleOauth2Controller {

    private GoogleConnectionFactory connectionFactory;

    private OAuth2Parameters parameters;

    public SpringSocialGoogleOauth2Controller(GoogleConnectionFactory connectionFactory, OAuth2Parameters parameters) {
        this.connectionFactory = connectionFactory;
        this.parameters = parameters;
    }

    @RequestMapping("/oauth2/google")
    public View auth() {
        return new RedirectView(connectionFactory.getOAuthOperations().buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE, parameters));
    }

    @ResponseBody
    @RequestMapping("/oauth2/google/callback")
    public ResponseEntity<String> authCallback(HttpSession session, @RequestParam String code) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.set("grant_type", "authorization_code");
        AccessGrant accessGrant = connectionFactory.getOAuthOperations().exchangeForAccess(code, parameters.getRedirectUri(), map);

        Map<String, String> body = new HashMap<>();
        body.put("access_token", accessGrant.getAccessToken());
        body.put("refresh_token", accessGrant.getRefreshToken());
        body.put("scope", accessGrant.getScope());
        body.put("expire_time", accessGrant.getExpireTime().toString());
        return ResponseEntity.ok(body.toString());
    }

    @ResponseBody
    @RequestMapping("/me")
    public ResponseEntity<UserInfo> getGoogleUser(@RequestHeader("Authorization") String authorization) {

        if (!authorization.startsWith("Bearer")) {
            return ResponseEntity.badRequest().build();
        }

        String accessToken = authorization.split(" ")[1];
        GoogleTemplate google = new GoogleTemplate(accessToken);
        UserInfo user = google.oauth2Operations().getUserinfo();

        return ResponseEntity.ok(user);
    }

}
