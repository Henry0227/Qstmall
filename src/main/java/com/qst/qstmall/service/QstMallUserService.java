package com.qst.qstmall.service;

import javax.servlet.http.HttpSession;

public interface QstMallUserService {

    /**
     * 用户注册
     */
    String register(String loginName, String password);

    String login(String loginName, String passwordMD5, HttpSession httpSession);
}
