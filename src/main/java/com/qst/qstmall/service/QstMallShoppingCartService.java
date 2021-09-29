package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.QstMallShoppingCartItemVO;
import com.qst.qstmall.entity.QstMallShoppingCartItem;

import java.util.List;

public interface QstMallShoppingCartService {

    /**
     * 保存商品至购物车中
     *
     * @param qstMallShoppingCartItem
     * @return
     */
    String saveQstMallCartItem(QstMallShoppingCartItem qstMallShoppingCartItem);

    /**
     * 修改购物车中的属性
     *
     * @param qstMallShoppingCartItem
     * @return
     */
    String updateQstMallCartItem(QstMallShoppingCartItem qstMallShoppingCartItem);

    /**
     * 获取购物项详情
     *
     * @param qstMallShoppingCartItemId
     * @return
     */
    QstMallShoppingCartItem getQstMallCartItemById(Long qstMallShoppingCartItemId);

    /**
     * 删除购物车中的商品
     *
     * @param qstMallShoppingCartItemId
     * @return
     */
    Boolean deleteById(Long qstMallShoppingCartItemId);

    /**
     * 清空购物车中的商品
     *
     * @param userId
     * @return
     */
    Boolean deleteByUserId(Long userId);

    /**
     * 获取我的购物车中的列表数据
     *
     * @param qstMallUserId
     * @return
     */
    List<QstMallShoppingCartItemVO> getMyShoppingCartItems(Long qstMallUserId);

    List<QstMallShoppingCartItemVO> getMySettleShoppingCartItems(List<Long> cartItemIds);
}
