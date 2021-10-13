package com.qst.qstmall.controller;

import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.Result;
import com.qst.qstmall.utils.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/admin")
public class QstMallPromotionController {


    @Autowired
    QstMallPromotionService qstMallPromotionService;


    @GetMapping("/promotion")
    public String ordersPage(HttpServletRequest request) {
        request.setAttribute("path", "promotion");
        return "admin/qst_mall_promotion";
    }


    /**
     * 列表
     */
    @GetMapping("/promotion/edit")
    public String edit(HttpServletRequest request) {
        request.setAttribute("path", "promotion-edit");
        return "admin/qst_mall_promotion_edit";
    }

    @RequestMapping(value = "/promotion/list", method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(qstMallPromotionService.getQstMallPromotionPage(pageUtil));
    }

    @ResponseBody
    @PostMapping("/promotion/save")
    public Result save(@RequestBody Promotion promotion, HttpSession session) {
        if (Objects.isNull(promotion.getGoodsId())
                || StringUtils.isEmpty(promotion.getPromotionName())
                || Objects.isNull(promotion.getPromotionPrice())
                || Objects.isNull(promotion.getStartTime())
                || Objects.isNull(promotion.getEndTime())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        Integer loginUserId=(Integer) session.getAttribute("loginUserId");
        promotion.setCreateUser(loginUserId);
        String result = qstMallPromotionService.saveQstMallPromotion(promotion);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }
}