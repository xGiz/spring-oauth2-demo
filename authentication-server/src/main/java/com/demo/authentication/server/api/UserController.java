package com.demo.authentication.server.api;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @RequestMapping({ "/user", "/me" })
  public ModelMap user(Principal principal) {
    ModelMap modelMap = new ModelMap();
    modelMap.addAttribute("name", principal.getName());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    modelMap.addAttribute("time", LocalDateTime.now().format(formatter));

    return modelMap;
  }
}
