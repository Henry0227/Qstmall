package com.qst.qstmall.controller;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.service.QstMallUserService;
import com.qst.qstmall.utils.Result;
import com.qst.qstmall.utils.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
public class PersonalController {

    @Resource
    private QstMallUserService qstMallUserService;

    @GetMapping({"/register","/register.html"})
    public String register(){
        return "mall/register";
    }

    @PostMapping("/register")
    @ResponseBody
    public Result register(String loginName, String verifyCode, String password, HttpSession httpSession) {
        String kaptchaCode = httpSession.getAttribute(Constants.MALL_VERIFY_CODE_KEY) + "";
        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.equals(kaptchaCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        //todo 清verifyCode
        String registerResult = qstMallUserService.register(loginName, password);
        //注册成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(registerResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //注册失败
        return ResultGenerator.genFailResult(registerResult);
    }
}