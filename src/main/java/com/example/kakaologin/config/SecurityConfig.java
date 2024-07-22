package com.example.kakaologin.config;

import com.example.kakaologin.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
                                .failureUrl("/login?error=true")
                                .userInfoEndpoint(userInfoEndpoint ->
                                        userInfoEndpoint.userService(oAuth2UserService())
                                )
                                .tokenEndpoint(tokenEndpoint ->
                                        tokenEndpoint.accessTokenResponseClient(authorizationCodeTokenResponseClient())
                                )
                )
                .logout(logout ->
                        logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/")
                                .permitAll()
                );
        return http.build();
    }

    @Bean
    public DefaultOAuth2UserService oAuth2UserService() {
        return new DefaultOAuth2UserService() {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                try {
                    OAuth2User oAuth2User = super.loadUser(userRequest);
                    Map<String, Object> attributes = oAuth2User.getAttributes();

                    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                    String username = (String) profile.get("nickname");
                    String email = (String) kakaoAccount.get("email");
                    String profileImageUrl = (String) profile.get("profile_image_url");

                    userService.registerOrUpdateUser(username, email, profileImageUrl);
                    return oAuth2User;
                } catch (Exception e) {
                    OAuth2Error oauth2Error = new OAuth2Error("invalid_token", "Unexpected error occurred while processing OAuth2 authentication", e.getMessage());
                    throw new OAuth2AuthenticationException(oauth2Error, e.getMessage(), e);
                }
            }
        };
    }

    @Bean
    public DefaultAuthorizationCodeTokenResponseClient authorizationCodeTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient tokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
        tokenResponseClient.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter());
        return tokenResponseClient;
    }
}
