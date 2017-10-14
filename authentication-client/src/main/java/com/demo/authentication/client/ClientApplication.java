package com.demo.authentication.client;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableOAuth2Sso
public class ClientApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ClientApplication.class)
        .bannerMode(Mode.OFF)
        .web(true)
        .run(args);
  }
}
