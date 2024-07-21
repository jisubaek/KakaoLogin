package com.example.kakaologin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
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
                    logger.debug("OAuth2 User: {}", oAuth2User.getAttributes());

                    String username = oAuth2User.getAttribute("properties.nickname");
                    String email = oAuth2User.getAttribute("kakao_account.email");
                    String profileImageUrl = oAuth2User.getAttribute("properties.profile_image");

                    logger.debug("Username: {}, Email: {}, Profile Image URL: {}", username, email, profileImageUrl);

                    userService.registerOrUpdateUser(username, email, profileImageUrl);
                    return oAuth2User;
                } catch (OAuth2AuthenticationException e) {
                    logger.error("OAuth2 Authentication Exception", e);
                    throw e;
                } catch (Exception e) {
                    logger.error("Unexpected Exception", e);
                    throw new OAuth2AuthenticationException(e.getMessage(), e);
                }
            }
        };
    }
}
