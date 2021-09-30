package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.QstMallOrderDetailVO;
import com.qst.qstmall.controller.vo.QstMallOrderItemVO;
import com.qst.qstmall.controller.vo.QstMallShoppingCartItemVO;
import com.qst.qstmall.controller.vo.QstMallUserVO;
import com.qst.qstmall.entity.QstMallOrder;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;

import java.util.List;

public interface QstMallOrderService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getQstMallOrdersPage(PageQueryUtil pageUtil);

    /**
     * 订单信息修改
     *
     * @param qstMallOrder
     * @return
     */
    String updateOrderInfo(QstMallOrder qstMallOrder);

    /**
     * 配货
     *
     * @param ids
     * @return
     */
    String checkDone(Long[] ids);

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    String closeOrder(Long[] ids);

    /**
     * 保存订单
     *
     * @param user
     * @param mySettleShoppingCartItems
     * @return
     */
    String saveOrder(QstMallUserVO user, List<QstMallShoppingCartItemVO> mySettleShoppingCartItems);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    QstMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @return
     */
    QstMallOrder getQstMallOrderByOrderNo(String orderNo);

    /**
     * 我的订单列表
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * 手动取消订单
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    List<QstMallOrderItemVO> getOrderItems(Long id);
}
