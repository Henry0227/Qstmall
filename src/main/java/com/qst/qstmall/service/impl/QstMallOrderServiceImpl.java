package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.PayStatusEnum;
import com.qst.qstmall.common.PayTypeEnum;
import com.qst.qstmall.common.QstMallOrderStatusEnum;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.*;
import com.qst.qstmall.dao.QstMallGoodsMapper;
import com.qst.qstmall.dao.QstMallOrderItemMapper;
import com.qst.qstmall.dao.QstMallOrderMapper;
import com.qst.qstmall.dao.QstMallShoppingCartItemMapper;
import com.qst.qstmall.entity.*;
import com.qst.qstmall.service.QstMallOrderService;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.NumberUtil;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class QstMallOrderServiceImpl implements QstMallOrderService {

    @Autowired
    private QstMallOrderMapper qstMallOrderMapper;
    @Autowired
    private QstMallOrderItemMapper qstMallOrderItemMapper;
    @Autowired
    private QstMallShoppingCartItemMapper qstMallShoppingCartItemMapper;
    @Autowired
    private QstMallGoodsMapper qstMallGoodsMapper;
    @Autowired
    private QstMallPromotionService qstMallPromotionService;

    @Override
    public PageResult getQstMallOrdersPage(PageQueryUtil pageUtil) {
        List<QstMallOrder> qstMallOrders = qstMallOrderMapper.findQstMallOrderList(pageUtil);
        int total = qstMallOrderMapper.getTotalQstMallOrders(pageUtil);
        PageResult pageResult = new PageResult(qstMallOrders, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    @Transactional
    public String updateOrderInfo(QstMallOrder qstMallOrder) {
        QstMallOrder temp = qstMallOrderMapper.selectByPrimaryKey(qstMallOrder.getOrderId());
        //????????????orderStatus>=0????????????????????????????????????????????????
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(qstMallOrder.getTotalPrice());
            temp.setUserAddress(qstMallOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        //????????????????????? ???????????? ???????????????????????????
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (qstMallOrder.getOrderStatus() != 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //?????????????????? ?????????????????????????????? ?????????????????????????????????
                if (qstMallOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //????????????????????????????????????
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "?????????????????????????????????????????????????????????";
                } else {
                    return "????????????????????????????????????????????????????????????????????????????????????";
                }
            }
        }
        //?????????????????? ??????????????????
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        //????????????????????? ???????????? ???????????????????????????
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (qstMallOrder.getOrderStatus() != 1 && qstMallOrder.getOrderStatus() != 2) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //?????????????????? ???????????????????????? ?????????????????????????????????
                if (qstMallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //????????????????????????????????????
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "????????????????????????????????????????????????????????????????????????";
                } else {
                    return "?????????????????????????????????????????????????????????????????????????????????????????????";
                }
            }
        }
        //?????????????????? ??????????????????
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        //????????????????????? ???????????? ???????????????????????????
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                // isDeleted=1 ????????????????????????
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                //??????????????????????????????????????????
                if (qstMallOrder.getOrderStatus() == 4 || qstMallOrder.getOrderStatus() < 0) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //?????????????????? ???????????????????????? ?????????????????????????????????
                if (qstMallOrderMapper.closeOrder(Arrays.asList(ids), QstMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //????????????????????????????????????
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "??????????????????????????????";
                } else {
                    return "??????????????????????????????????????????";
                }
            }
        }
        //?????????????????? ??????????????????
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    /*??????????????????????????????
    1???????????????
    2??????????????????
    3???????????????
    4???????????????
    5??????????????????
    * ?????????user????????????
    * ?????????mySettleShoppingCartItems?????????????????????
    * ??????????????????*/
    @Override
    @Transactional
    public String saveOrder(QstMallUserVO user, List<QstMallShoppingCartItemVO> mySettleShoppingCartItems) {
        /*?????????????????????????????????id???????????????????????????????????????????????????????????????*/
        List<Long> itemIdList = mySettleShoppingCartItems.stream().map(QstMallShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        /*??????????????????id??????*/
        List<Long> goodsIds = mySettleShoppingCartItems.stream().map(QstMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        /*??????????????????????????????????????????????????????????????????*/
        List<QstMallGoods> qstMallGoods = qstMallGoodsMapper.selectByPrimaryKeys(goodsIds);
        /*???list???????????????map???key??????id???value?????????*/
        Map<Long, QstMallGoods> qstMallGoodsMap = qstMallGoods.stream().collect(Collectors.toMap(QstMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //??????????????????
        for (QstMallShoppingCartItemVO shoppingCartItemVO : mySettleShoppingCartItems) {
            //?????????????????????????????????????????????????????????????????????????????????????????????
            if (!qstMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
            }
            //????????????????????????????????????????????????????????????
            if (shoppingCartItemVO.getGoodsCount() > qstMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                return ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult();
            }
        }
        //???????????????
        if (!CollectionUtils.isEmpty(itemIdList) && !CollectionUtils.isEmpty(goodsIds) && !CollectionUtils.isEmpty(qstMallGoods)) {
            if (qstMallShoppingCartItemMapper.deleteBatch(itemIdList) > 0) {  //?????????????????????????????????
                List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(mySettleShoppingCartItems, StockNumDTO.class);

                //????????????
                int updateStockNumResult = qstMallGoodsMapper.updateStockNum(stockNumDTOS);
                if (updateStockNumResult < 1) {
                    return ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult();
                }
                //???????????????
                String orderNo = NumberUtil.genOrderNo();
                int priceTotal = 0;
                //?????????????????????????????????
                QstMallOrder qstMallOrder = new QstMallOrder();

                qstMallOrder.setOrderNo(orderNo);
                qstMallOrder.setUserId(user.getUserId());
                qstMallOrder.setUserAddress(user.getAddress());
                //??????
                for (QstMallShoppingCartItemVO qstMallShoppingCartItemVO : mySettleShoppingCartItems) {
                    Promotion promotion=qstMallPromotionService.getActivatedPromotion(qstMallShoppingCartItemVO.getGoodsId());
                    if(promotion!=null){
                        priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getSellingPrice();
                    }else{
                        priceTotal += qstMallShoppingCartItemVO.getGoodsCount() * qstMallShoppingCartItemVO.getOriginalPrice();
                    }
                }
                if (priceTotal < 1) {
                    return ServiceResultEnum.ORDER_PRICE_ERROR.getResult();
                }
                qstMallOrder.setTotalPrice(priceTotal);
                //todo ??????body??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                String extraInfo = "";
                qstMallOrder.setExtraInfo(extraInfo);

                //???????????????????????????????????????
                if (qstMallOrderMapper.insertSelective(qstMallOrder) > 0) {  //??????????????????????????????
                    //??????????????????????????????????????????????????????
                    List<QstMallOrderItem> qstMallOrderItems = new ArrayList<>();
                    for (QstMallShoppingCartItemVO qstMallShoppingCartItemVO : mySettleShoppingCartItems) {
                        QstMallOrderItem qstMallOrderItem = new QstMallOrderItem();
                        //??????BeanUtil????????????qstMallShoppingCartItemVO?????????????????????qstMallOrderItem?????????
                        BeanUtil.copyProperties(qstMallShoppingCartItemVO, qstMallOrderItem);
                        if(qstMallShoppingCartItemVO.getPromotionStatus()==null){
                            qstMallOrderItem.setSellingPrice(qstMallShoppingCartItemVO.getOriginalPrice());
                        }

                        //QstMallOrderMapper??????insert()??????????????????useGeneratedKeys??????orderId???????????????
                        qstMallOrderItem.setOrderId(qstMallOrder.getOrderId());
                        qstMallOrderItems.add(qstMallOrderItem);
                    }
                    //??????????????????(????????????????????????????????????????????????)
                    if (qstMallOrderItemMapper.insertBatch(qstMallOrderItems) > 0) {
                        //???????????????????????????????????????????????????Controller???????????????????????????
                        return orderNo;
                    }
                    return ServiceResultEnum.ORDER_GENERATE_ERROR.getResult();
                }
                return ServiceResultEnum.DB_ERROR.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
    }

    /*??????7???????????????id?????????id?????????????????????*/
    @Override
    public QstMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //?????????????????????userId???????????????????????????
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return null;
            }
            List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderId(qstMallOrder.getOrderId());
            //?????????????????????
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItems, QstMallOrderItemVO.class);
                QstMallOrderDetailVO qstMallOrderDetailVO = new QstMallOrderDetailVO();
                BeanUtil.copyProperties(qstMallOrder, qstMallOrderDetailVO);
                qstMallOrderDetailVO.setOrderStatusString(QstMallOrderStatusEnum.getQstMallOrderStatusEnumByStatus(qstMallOrderDetailVO.getOrderStatus()).getName());
                qstMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(qstMallOrderDetailVO.getPayType()).getName());
                qstMallOrderDetailVO.setQstMallOrderItemVOS(qstMallOrderItemVOS);
                return qstMallOrderDetailVO;
            }
        }
        return null;
    }

    @Override
    public QstMallOrder getQstMallOrderByOrderNo(String orderNo) {
        return qstMallOrderMapper.selectByOrderNo(orderNo);
    }

    /*??????????????????*/
    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = qstMallOrderMapper.getTotalQstMallOrders(pageUtil);
        List<QstMallOrder> qstMallOrders = qstMallOrderMapper.findQstMallOrderList(pageUtil);
        List<QstMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //???????????? ??????????????????vo
            orderListVOS = BeanUtil.copyList(qstMallOrders, QstMallOrderListVO.class);
            //?????????????????????????????????
            for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                qstMallOrderListVO.setOrderStatusString(QstMallOrderStatusEnum.getQstMallOrderStatusEnumByStatus(qstMallOrderListVO.getOrderStatus()).getName());
                qstMallOrderListVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(qstMallOrderListVO.getPayType()).getName());
            }
            List<Long> orderIds = qstMallOrders.stream().map(QstMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<QstMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(QstMallOrderItem::getOrderId));
                for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                    //????????????????????????????????????????????????
                    if (itemByOrderIdMap.containsKey(qstMallOrderListVO.getOrderId())) {
                        List<QstMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(qstMallOrderListVO.getOrderId());
                        //???QstMallOrderItem?????????????????????QstMallOrderItemVO????????????
                        List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItemListTemp, QstMallOrderItemVO.class);
                        qstMallOrderListVO.setQstMallOrderItemVOS(qstMallOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //?????????????????????userId???????????????????????????
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            //??????????????????
            if (qstMallOrder.getOrderStatus()<(byte) QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                    || qstMallOrder.getOrderStatus()>(byte) QstMallOrderStatusEnum.OREDER_EXPRESS.getOrderStatus()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (qstMallOrderMapper.closeOrder(Collections.singletonList(qstMallOrder.getOrderId()), QstMallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //todo ?????????????????????userId???????????????????????????
            //todo ??????????????????
            qstMallOrder.setOrderStatus((byte) QstMallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            qstMallOrder.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(qstMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //?????????????????? ??????????????????????????????????????????
            if((byte) QstMallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()!= qstMallOrder.getOrderStatus()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            qstMallOrder.setOrderStatus((byte) QstMallOrderStatusEnum.OREDER_PAID.getOrderStatus());
            qstMallOrder.setPayType((byte) payType);
            qstMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            qstMallOrder.setPayTime(new Date());
            qstMallOrder.setUpdateTime(new Date());
            if (qstMallOrderMapper.updateByPrimaryKeySelective(qstMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public List<QstMallOrderItemVO> getOrderItems(Long id) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByPrimaryKey(id);
        if (qstMallOrder != null) {
            List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderId(qstMallOrder.getOrderId());
            //?????????????????????
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItems, QstMallOrderItemVO.class);
                return qstMallOrderItemVOS;
            }
        }
        return null;
    }
}
