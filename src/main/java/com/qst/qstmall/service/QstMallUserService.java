package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.entity.MallUser;

import javax.servlet.http.HttpSession;

public interface QstMallUserService {

    /**
     * 用户注册
     */
    String register(String loginName, String password);

    String login(String loginName, String passwordMD5, HttpSession httpSession);
    /**
     * 用户信息修改并返回最新的用户信息
     *
     * @param mallUser
     * @return
     */
    QstMallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession);


}
