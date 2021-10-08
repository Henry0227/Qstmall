package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.entity.MallUser;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;

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

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     *
     * @param ids
     * @param lockStatus
     * @return
     */
    Boolean lockUsers(Integer[] ids, int lockStatus);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getQstMallUsersPage(PageQueryUtil pageUtil);
}
