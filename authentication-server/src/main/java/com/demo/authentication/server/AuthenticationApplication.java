package com.demo.authentication.server;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
public class AuthenticationApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthenticationApplication.class)
        .bannerMode(Mode.OFF)
        .web(true)
        .run(args);
  }
}
