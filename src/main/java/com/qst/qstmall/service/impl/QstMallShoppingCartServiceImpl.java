package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallShoppingCartItemVO;
import com.qst.qstmall.dao.QstMallGoodsMapper;
import com.qst.qstmall.dao.QstMallShoppingCartItemMapper;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.entity.QstMallShoppingCartItem;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.service.QstMallShoppingCartService;
import com.qst.qstmall.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QstMallShoppingCartServiceImpl implements QstMallShoppingCartService {

    @Autowired
    private QstMallShoppingCartItemMapper qstMallShoppingCartItemMapper;

    @Autowired
    private QstMallGoodsMapper qstMallGoodsMapper;
    @Autowired
    private QstMallPromotionService qstMallPromotionService;

    //todo 修改session中购物项数量

    /*任务6：添加一个商品到购物车*/
    @Override
    public String saveQstMallCartItem(QstMallShoppingCartItem qstMallShoppingCartItem) {

        QstMallShoppingCartItem temp = qstMallShoppingCartItemMapper.selectByUserIdAndGoodsId(qstMallShoppingCartItem.getUserId(), qstMallShoppingCartItem.getGoodsId());
        if (temp != null) {//该商品如果已存在购物车则修改该记录
            //如果数量大于200，返回失败信息
            if(temp.getGoodsCount()+ qstMallShoppingCartItem.getGoodsCount()>Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER){
                return ServiceResultEnum.SHOPPING_CART_ITEM_EXIST_AND_LIMIT_NUMBER_ERROR.getResult();
            }
            temp.setGoodsCount(qstMallShoppingCartItem.getGoodsCount()+temp.getGoodsCount());
            return updateQstMallCartItem(temp);
        }
        /*根据要添加商品id查询商品详情*/
        QstMallGoods qstMallGoods = qstMallGoodsMapper.selectByPrimaryKey(qstMallShoppingCartItem.getGoodsId());
        //如果商品不存在返回失败结果
        if (qstMallGoods == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        /*根据用户id获取该用户购物车最多能几个商品*/
        int totalItem = qstMallShoppingCartItemMapper.selectCountByUserId(qstMallShoppingCartItem.getUserId());
        //超出购物车数量上限
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        //商品已下架
        if(Constants.SELL_STATUS_DOWN== qstMallGoods.getGoodsSellStatus()){
            return ServiceResultEnum.GOODS_DOWN.getResult();
        }
        //保存记录（将商品添加到购物车表）
        if (qstMallShoppingCartItemMapper.insertSelective(qstMallShoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    /*任务6：修改购物车商品数量*/
    @Override
    public String updateQstMallCartItem(QstMallShoppingCartItem qstMallShoppingCartItem) {
        QstMallShoppingCartItem qstMallShoppingCartItemUpdate = qstMallShoppingCartItemMapper.selectByPrimaryKey(qstMallShoppingCartItem.getCartItemId());
        /*所要修改的商品在购物车中不存在*/
        if (qstMallShoppingCartItemUpdate == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        //超出最大数量
        if (qstMallShoppingCartItem.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //todo 数量相同不会进行修改
        qstMallShoppingCartItemUpdate.setGoodsCount(qstMallShoppingCartItem.getGoodsCount());
        qstMallShoppingCartItemUpdate.setUpdateTime(new Date());
        //保存记录
        if (qstMallShoppingCartItemMapper.updateByPrimaryKeySelective(qstMallShoppingCartItemUpdate) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public QstMallShoppingCartItem getQstMallCartItemById(Long qstMallShoppingCartItemId) {
        return qstMallShoppingCartItemMapper.selectByPrimaryKey(qstMallShoppingCartItemId);
    }
    /*任务6：根据id删除购物车商品*/
    @Override
    public Boolean deleteById(Long qstMallShoppingCartItemId) {
        return qstMallShoppingCartItemMapper.deleteByPrimaryKey(qstMallShoppingCartItemId) > 0;
    }

    /*任务6：根据用户id，清空购物车*/
    @Override
    public Boolean deleteByUserId(Long userId) {
        return qstMallShoppingCartItemMapper.deleteByUserId(userId) > 0;
    }

    /*任务6：根据用户id返回该用户购物车所有商品集合*/
    @Override
    public List<QstMallShoppingCartItemVO> getMyShoppingCartItems(Long qstMallUserId) {
        List<QstMallShoppingCartItem> qstMallShoppingCartItems = qstMallShoppingCartItemMapper.selectByUserId(qstMallUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        return conventFromModel(qstMallShoppingCartItems);
    }

    /*任务7：根据商品id集合返回商品集合*/
    @Override
    public List<QstMallShoppingCartItemVO> getMySettleShoppingCartItems(List<Long> cartItemIds) {
        List<QstMallShoppingCartItem> qstMallShoppingCartItems = qstMallShoppingCartItemMapper.selectByPrimaryKeys(cartItemIds, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        return conventFromModel(qstMallShoppingCartItems);
    }

    public List<QstMallShoppingCartItemVO> conventFromModel(List<QstMallShoppingCartItem> qstMallShoppingCartItems){
        List<QstMallShoppingCartItemVO> qstMallShoppingCartItemVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(qstMallShoppingCartItems)) {
            //查询商品信息并做数据转换
            List<Long> qstMallGoodsIds = qstMallShoppingCartItems.stream().map(QstMallShoppingCartItem::getGoodsId).collect(Collectors.toList());
            List<QstMallGoods> qstMallGoods = qstMallGoodsMapper.selectByPrimaryKeys(qstMallGoodsIds);
            Map<Long, QstMallGoods> qstMallGoodsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(qstMallGoods)) {
                qstMallGoodsMap = qstMallGoods.stream().collect(Collectors.toMap(QstMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            for (QstMallShoppingCartItem qstMallShoppingCartItem : qstMallShoppingCartItems) {
                QstMallShoppingCartItemVO qstMallShoppingCartItemVO = new QstMallShoppingCartItemVO();
                BeanUtil.copyProperties(qstMallShoppingCartItem, qstMallShoppingCartItemVO);
                if (qstMallGoodsMap.containsKey(qstMallShoppingCartItem.getGoodsId())) {
                    QstMallGoods qstMallGoodsTemp = qstMallGoodsMap.get(qstMallShoppingCartItem.getGoodsId());
                    qstMallShoppingCartItemVO.setGoodsCoverImg(qstMallGoodsTemp.getGoodsCoverImg());
                    String goodsName = qstMallGoodsTemp.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    //查询该商品促销状态确定价格
                    Promotion promotion=qstMallPromotionService.getActivatedPromotion(qstMallGoodsTemp.getGoodsId());
                    if(promotion!=null){
                        qstMallShoppingCartItemVO.setPromotionStatus(Constants.PROMOTION_STATUS_STARTED);
                        qstMallShoppingCartItemVO.setSellingPrice(promotion.getPromotionPrice());
                    }else{
                        qstMallShoppingCartItemVO.setOriginalPrice(qstMallGoodsTemp.getOriginalPrice());
                    }
                    qstMallShoppingCartItemVO.setGoodsName(goodsName);
                    qstMallShoppingCartItemVOS.add(qstMallShoppingCartItemVO);
                }
            }
        }
        return qstMallShoppingCartItemVOS;
    }
}