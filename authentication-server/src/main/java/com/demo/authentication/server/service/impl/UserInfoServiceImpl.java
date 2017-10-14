package com.demo.authentication.server.service.impl;

import com.demo.authentication.server.dao.UserInfoDao;
import com.demo.authentication.server.service.UserInfoService;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {

  private static final Logger logger = Logger.getLogger(UserInfoServiceImpl.class);

  private final UserInfoDao userInfoDao;

  @Autowired
  public UserInfoServiceImpl(UserInfoDao userInfoDao) {
    this.userInfoDao = userInfoDao;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    List<UserDetails> users = userInfoDao.findByUsername(username);

    if (users.size() == 0) {
      logger.debug("Query returned no results for user '" + username + "'");

      throw new UsernameNotFoundException("Don't find Username {" + username + "}");
    }

    UserDetails user = users.get(0); // contains no GrantedAuthority[]

    return new User(user.getUsername(), user.getPassword(), user.isEnabled(),
        true, true, true, Collections.emptyList());
  }
}
