package com.qst.qstmall.controller;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallShoppingCartItemVO;
import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.entity.QstMallShoppingCartItem;
import com.qst.qstmall.service.QstMallShoppingCartService;
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
import java.util.stream.Collectors;

/*任务6：处理购物车相关请求*/
@Controller
public class ShoppingCartController {

    @Resource
    private QstMallShoppingCartService qstMallShoppingCartService;

    /*任务6：进入购物车页面get请求*/
    @GetMapping("/shop-cart")
    public String cartListPage(HttpServletRequest request,HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<QstMallShoppingCartItemVO> myShoppingCartItems = qstMallShoppingCartService.getMyShoppingCartItems(user.getUserId());
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/cart";
    }

    /*任务6：添加购物车post请求*/
    @PostMapping("/shop-cart")
    @ResponseBody
    public Result saveQstMallShoppingCartItem(@RequestBody QstMallShoppingCartItem qstMallShoppingCartItem,
                                              HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        qstMallShoppingCartItem.setUserId(user.getUserId());
        //todo 判断数量
        String saveResult = qstMallShoppingCartService.saveQstMallCartItem(qstMallShoppingCartItem);
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    /*任务6：修改购物车put请求*/
    @PutMapping("/shop-cart")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(@RequestBody QstMallShoppingCartItem qstMallShoppingCartItem,
                                                   HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        qstMallShoppingCartItem.setUserId(user.getUserId());
        //todo 判断数量
        String saveResult = qstMallShoppingCartService.updateQstMallCartItem(qstMallShoppingCartItem);
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(saveResult);
    }

    /*任务6：删除购物车商品*/
    @DeleteMapping("/shop-cart/{qstMallShoppingCartItemId}")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(@PathVariable("qstMallShoppingCartItemId") Long qstMallShoppingCartItemId) {
        Boolean deleteResult = qstMallShoppingCartService.deleteById(qstMallShoppingCartItemId);
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    /*删除购物车所有商品（清空购物车）*/
    @DeleteMapping("/shop-cart")
    @ResponseBody
    public Result updateQstMallShoppingCartItem(HttpSession httpSession) {
        QstMallUserVO user = (QstMallUserVO) httpSession.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Boolean deleteResult = qstMallShoppingCartService.deleteByUserId(user.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    /*任务7：处理购物车页面（cart.html）结算请求
    * itemIds:页面传递的用户要结算的所有商品id拼接成的字符串*/
    @GetMapping("/shop-cart/settle")
    public String settlePage(@RequestParam("cartItemIds")String itemIds,  HttpServletRequest request) {
        int itemsTotal = 0;   //商品总个数
        int priceTotal = 0;   //商品总价格
        if (StringUtils.isEmpty(itemIds)) {
            //无数据则不跳转至结算页
            return "redirect:/shop-cart";
        }
        /*将包含所有商品id的字符串转换为list集合*/
        List<Long> cartItemIds= Arrays.asList(itemIds.split(",")).stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
        //根据商品id集合返回商品集合
        List<QstMallShoppingCartItemVO> mySettleShoppingCartItems = qstMallShoppingCartService.getMySettleShoppingCartItems(cartItemIds);
        if (!CollectionUtils.isEmpty(mySettleShoppingCartItems)) {
            //订单项总数
            itemsTotal = mySettleShoppingCartItems.stream().mapToInt(QstMallShoppingCartItemVO::getGoodsCount).sum();
            if (itemsTotal < 1) {
                return "error/error_5xx";
            }
            //总价
            for (QstMallShoppingCartItemVO qstMallShoppingCartItemVO : mySettleShoppingCartItems) {
                if(qstMallShoppingCartItemVO.getPromotionStatus()==null){  //如果没有活动按照原价计算总价
                    priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getOriginalPrice();
                }else if(qstMallShoppingCartItemVO.getPromotionStatus()==Constants.PROMOTION_STATUS_STARTED){
                    /*如果有活动，按照销售价计算总价*/
                    priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getSellingPrice();
                }
            }
            if (priceTotal < 1) {
                return "error/error_5xx";
            }
        }else{
            return "redirect:/shop-cart";
        }
        request.setAttribute("itemsTotal",itemsTotal);  //商品总数放到请求域返回前端页面
        request.setAttribute("priceTotal",priceTotal);  //商品总价格放到请求域返回前端页面
        //要结算的所有商品放到请求域返回前端页面
        request.setAttribute("mySettleShoppingCartItems", mySettleShoppingCartItems);
        return "mall/order-settle";   //转发到订单确认页
    }
}