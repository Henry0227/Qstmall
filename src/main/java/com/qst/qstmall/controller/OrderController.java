package com.qst.qstmall.controller;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.QstMallOrderStatusEnum;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallOrderDetailVO;
import com.qst.qstmall.controller.vo.QstMallShoppingCartItemVO;
import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.entity.QstMallOrder;
import com.qst.qstmall.service.QstMallOrderService;
import com.qst.qstmall.service.QstMallShoppingCartService;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.Result;
import com.qst.qstmall.utils.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Resource
    private QstMallShoppingCartService qstMallShoppingCartService;
    @Resource
    private QstMallOrderService qstMallOrderService;

    /*任务7：进入到订单详情页面*/
    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        /*根据订单id，获取订单所有信息，返回前端页面*/
        QstMallOrderDetailVO orderDetailVO = qstMallOrderService.getOrderDetailByOrderNo(orderNo, user.getUserId());
        if (orderDetailVO == null) {
            return "error/error_5xx";
        }
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    /*处理我的订单请求*/
    @GetMapping("/orders")
    public String orderListPage(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());
        if (StringUtils.isEmpty(params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", Constants.ORDER_SEARCH_PAGE_LIMIT);
        //封装我的订单数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("orderPageResult", qstMallOrderService.getMyOrders(pageUtil));
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }

    /*任务7：处理订单确定页面（order-settle.html）提交订单请求
    * 提交订单后生成订单号，和订单商品分布保存到订单表和订单商品表*/
    @GetMapping("/saveOrder")
    public String saveOrder(@RequestParam("cartItemIds") String itemIds, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        /*将改订单商品id转为list集合*/
        List<Long> cartItemIds= Arrays.asList(itemIds.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        /*根据商品id集合获取商品集合*/
        List<QstMallShoppingCartItemVO> mySettleShoppingCartItems = qstMallShoppingCartService.getMySettleShoppingCartItems(cartItemIds);
        if (StringUtils.isEmpty(user.getAddress().trim())) {
            //无收货地址
            return "error/error_5xx";
        }
        if (CollectionUtils.isEmpty(mySettleShoppingCartItems)) {
            //购物车中无数据则跳转至错误页
            return "error/error_5xx";
        } else {
            //保存订单并返回订单号
            String saveOrderResult = qstMallOrderService.saveOrder(user, mySettleShoppingCartItems);
            if (ServiceResultEnum.ORDER_PRICE_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.DB_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult().equals(saveOrderResult)
                    || ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult().equals(saveOrderResult)) {
                //订单生成失败
                return "error/error_5xx";
            }
            //跳转到订单详情页，请求到本类中orderDetailPage方法
            return "redirect:/orders/" + saveOrderResult;
        }
    }

    /*任务7：处理订单详情页order-detail.html页面中取消订单请求*/
    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        /*取消订单（就是设置订单状态为取消）*/
        String cancelOrderResult = qstMallOrderService.cancelOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }


    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public Result finishOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String finishOrderResult = qstMallOrderService.finishOrder(orderNo, user.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    /*任务7：处理订单详情order-detail页面支付请求*/
    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        QstMallOrder qstMallOrder = qstMallOrderService.getQstMallOrderByOrderNo(orderNo);
        //判断订单userId
        if (qstMallOrder.getUserId().longValue()!=user.getUserId().longValue()){
            return "error/error_5xx";
        }
        //判断订单状态
        if (QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
            return "error/error_5xx";
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", qstMallOrder.getTotalPrice());
        return "mall/pay-select";  //转发到支付方式选择
    }

    /*任务7：支付选择页面（pay-select.html）支付请求*/
    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession, @RequestParam("payType") int payType) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        QstMallOrder qstMallOrder = qstMallOrderService.getQstMallOrderByOrderNo(orderNo);
        //判断订单userId
        if (qstMallOrder.getUserId().longValue()!=user.getUserId().longValue()){
            return "error/error_5xx";
        }
        //判断订单状态
        if (QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
            return "error/error_5xx";
        }
        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", qstMallOrder.getTotalPrice());
        if (payType == 1) {  //打开支付宝收款页面
            return "mall/alipay";
        } else {
            return "mall/wxpay";  //打开微信收款页面
        }
    }

    /*支付成功请求*/
    @GetMapping("/paySuccess")
    @ResponseBody
    public Result paySuccess(@RequestParam("orderNo") String orderNo, @RequestParam("payType") int payType) {
        /*修改订单状态*/
        String payResult = qstMallOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }
}