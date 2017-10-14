package com.demo.authentication.server.dao;

import com.demo.authentication.server.entity.UserInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoDao extends JpaRepository<UserInfo, Long> {

  List<UserDetails> findByUsername(String username);
}
