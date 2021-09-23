package com.qst.qstmall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController {

    @RequestMapping("/test")
    public ModelAndView test(){
        ModelAndView view = new ModelAndView();
        view.setViewName("/a");
        view.addObject("msg","zhang");

        return view;
    }

}