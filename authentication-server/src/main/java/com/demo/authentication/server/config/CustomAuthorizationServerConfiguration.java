package com.demo.authentication.server.config;

import com.demo.authentication.server.dao.UserInfoDao;
import com.demo.authentication.server.service.impl.UserInfoServiceImpl;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.JdbcClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

@Configuration
@EnableConfigurationProperties(AuthorizationServerProperties.class)
public class CustomAuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

  private static final Log logger = LogFactory.getLog(CustomAuthorizationServerConfiguration.class);

  private final BaseClientDetails details;

  private final DataSource dataSource;

  private final AuthenticationManager authenticationManager;

  private final TokenStore tokenStore;

  private final AccessTokenConverter tokenConverter;

  private final AuthorizationServerProperties properties;

  public CustomAuthorizationServerConfiguration(
      BaseClientDetails details,
      ObjectProvider<DataSource> dataSource,
      AuthenticationManager authenticationManager,
      ObjectProvider<TokenStore> tokenStore,
      ObjectProvider<AccessTokenConverter> tokenConverter,
      AuthorizationServerProperties properties) {
    this.details = details;
    this.dataSource = dataSource.getIfAvailable();
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore.getIfAvailable();
    this.tokenConverter = tokenConverter.getIfAvailable();
    this.properties = properties;
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    // Configuring ClientDetailsService to JdbcClientDetailsService
    clients.jdbc(this.dataSource);
  }

  /**
   * Insert one record of client details into database, if no one client details in database.
   *
   * @param builder build a client details service instance
   */
  @SuppressWarnings("unused")
  private void setDefaultClientDetails(JdbcClientDetailsServiceBuilder builder) {
    ClientDetailsServiceBuilder<JdbcClientDetailsServiceBuilder>.ClientBuilder clientBuilder = builder
        .withClient(this.details.getClientId());
    clientBuilder.secret(this.details.getClientSecret())
        .resourceIds(this.details.getResourceIds().toArray(new String[0]))
        .authorizedGrantTypes(
            this.details.getAuthorizedGrantTypes().toArray(new String[0]))
        .authorities(
            AuthorityUtils.authorityListToSet(this.details.getAuthorities())
                .toArray(new String[0]))
        .scopes(this.details.getScope().toArray(new String[0]));

    if (this.details.getAutoApproveScopes() != null) {
      clientBuilder.autoApprove(
          this.details.getAutoApproveScopes().toArray(new String[0]));
    }
    if (this.details.getAccessTokenValiditySeconds() != null) {
      clientBuilder.accessTokenValiditySeconds(
          this.details.getAccessTokenValiditySeconds());
    }
    if (this.details.getRefreshTokenValiditySeconds() != null) {
      clientBuilder.refreshTokenValiditySeconds(
          this.details.getRefreshTokenValiditySeconds());
    }
    if (this.details.getRegisteredRedirectUri() != null) {
      clientBuilder.redirectUris(
          this.details.getRegisteredRedirectUri().toArray(new String[0]));
    }
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints)
      throws Exception {
    if (this.tokenConverter != null) {
      endpoints.accessTokenConverter(this.tokenConverter);
    }
    if (this.tokenStore != null) {
      endpoints.tokenStore(this.tokenStore);
    }
    if (this.details.getAuthorizedGrantTypes().contains("password")) {
      endpoints.authenticationManager(this.authenticationManager);
    }

    // Mapping User Roles to Scopes
    // endpoints.requestFactory(new DefaultOAuth2RequestFactory());
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security)
      throws Exception {
    if (this.properties.getCheckTokenAccess() != null) {
      security.checkTokenAccess(this.properties.getCheckTokenAccess());
    }
    if (this.properties.getTokenKeyAccess() != null) {
      security.tokenKeyAccess(this.properties.getTokenKeyAccess());
    }
    if (this.properties.getRealm() != null) {
      security.realm(this.properties.getRealm());
    }
  }

  @Configuration
  protected static class UserDetailsConfiguration  {

    private final UserInfoDao userInfoDao;

    @Autowired
    public UserDetailsConfiguration(UserInfoDao userInfoDao) {
      this.userInfoDao = userInfoDao;
    }

    @Bean
    public UserDetailsService userDetailsService() {
      System.out.println();
      return new UserInfoServiceImpl(this.userInfoDao);
    }

    @Autowired
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
      builder.userDetailsService(userDetailsService());
    }
  }

  @Configuration
  protected static class TokenStoreConfiguration {

    private final DataSource dataSource;

    public TokenStoreConfiguration(ObjectProvider<DataSource> dataSource) {
      this.dataSource = dataSource.getIfAvailable();
    }

    @Bean
    public TokenStore tokenStore() {
      return new JdbcTokenStore(this.dataSource);
    }
  }

  @Configuration
  protected static class ClientDetailsLogger {

    private final OAuth2ClientProperties credentials;

    protected ClientDetailsLogger(OAuth2ClientProperties credentials) {
      this.credentials = credentials;
    }

    @PostConstruct
    public void init() {
      String prefix = "security.oauth2.client";
      boolean defaultSecret = this.credentials.isDefaultSecret();
      logger.info(String.format(
          "Initialized OAuth2 Client%n%n%s.clientId = %s%n%s.secret = %s%n%n",
          prefix, this.credentials.getClientId(), prefix,
          defaultSecret ? this.credentials.getClientSecret() : "****"));
    }

  }

  @Configuration
  @ConditionalOnMissingBean(BaseClientDetails.class)
  protected static class BaseClientDetailsConfiguration {

    private final OAuth2ClientProperties client;

    protected BaseClientDetailsConfiguration(OAuth2ClientProperties client) {
      this.client = client;
    }

    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.client")
    public BaseClientDetails oauth2ClientDetails() {
      BaseClientDetails details = new BaseClientDetails();
      if (this.client.getClientId() == null) {
        this.client.setClientId(UUID.randomUUID().toString());
      }
      details.setClientId(this.client.getClientId());
      details.setClientSecret(this.client.getClientSecret());
      details.setAuthorizedGrantTypes(Arrays.asList("authorization_code",
          "password", "client_credentials", "implicit", "refresh_token"));
      details.setAuthorities(
          AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
      details.setRegisteredRedirectUri(Collections.emptySet());
      return details;
    }

  }
}
