package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.dao.MallUserMapper;
import com.qst.qstmall.entity.MallUser;
import com.qst.qstmall.service.QstMallUserService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class QstMallUserServiceImpl implements QstMallUserService {

    @Autowired
    private MallUserMapper mallUserMapper;

    @Override
    public String register(String loginName, String password) {

        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        String passwordMD5 = MD5Util.MD5Encode(password);
        registerUser.setPasswordMd5(passwordMD5);

        if (mallUserMapper.insertSelective(registerUser) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String login(String loginName, String passwordMD5, HttpSession httpSession) {
        MallUser user = mallUserMapper.selectByLoginNameAndPasswd(loginName, passwordMD5);
        if (user != null && httpSession != null) {
            if (user.getLockedFlag() == 1) {
                return ServiceResultEnum.LOGIN_USER_LOCKED.getResult();
            }
            QstMallUserVO qstMallUserVO = new QstMallUserVO();
            BeanUtil.copyProperties(user, qstMallUserVO);
            //设置购物车中的数量
            httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, qstMallUserVO);
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.LOGIN_ERROR.getResult();
    }
}