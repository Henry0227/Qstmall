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
        //不为空且orderStatus>=0且状态为出库之前可以修改部分信息
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
        //查询所有的订单 判断状态 修改状态和更新时间
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
                //订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (qstMallOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
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
                //订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (qstMallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<QstMallOrder> orders = qstMallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (QstMallOrder qstMallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (qstMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (qstMallOrder.getOrderStatus() == 4 || qstMallOrder.getOrderStatus() < 0) {
                    errorOrderNos += qstMallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (qstMallOrderMapper.closeOrder(Arrays.asList(ids), QstMallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    /*保存订单并生成订单号
    1、判断库存
    2、删除购物车
    3、修改库存
    4、创建订单
    5、保存订单项
    * 参数：user当前用户
    * 参数：mySettleShoppingCartItems：订单商品集合
    * 返回：订单号*/
    @Override
    @Transactional
    public String saveOrder(QstMallUserVO user, List<QstMallShoppingCartItemVO> mySettleShoppingCartItems) {
        /*所有商品购物车表中商品id集合（为了生成订单后，将购物车表商品删除）*/
        List<Long> itemIdList = mySettleShoppingCartItems.stream().map(QstMallShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        /*订单所有商品id集合*/
        List<Long> goodsIds = mySettleShoppingCartItems.stream().map(QstMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        /*从商品详情表获取所有商品集合（为了读取库存）*/
        List<QstMallGoods> qstMallGoods = qstMallGoodsMapper.selectByPrimaryKeys(goodsIds);
        /*将list集合转换为map，key商品id，value为商品*/
        Map<Long, QstMallGoods> qstMallGoodsMap = qstMallGoods.stream().collect(Collectors.toMap(QstMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //判断商品库存
        for (QstMallShoppingCartItemVO shoppingCartItemVO : mySettleShoppingCartItems) {
            //查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!qstMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
            }
            //存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > qstMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                return ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult();
            }
        }
        //删除购物项
        if (!CollectionUtils.isEmpty(itemIdList) && !CollectionUtils.isEmpty(goodsIds) && !CollectionUtils.isEmpty(qstMallGoods)) {
            if (qstMallShoppingCartItemMapper.deleteBatch(itemIdList) > 0) {  //删除购物车表中商品信息
                List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(mySettleShoppingCartItems, StockNumDTO.class);

                //更新库存
                int updateStockNumResult = qstMallGoodsMapper.updateStockNum(stockNumDTOS);
                if (updateStockNumResult < 1) {
                    return ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult();
                }
                //生成订单号
                String orderNo = NumberUtil.genOrderNo();
                int priceTotal = 0;
                //新建订单对象，保存订单
                QstMallOrder qstMallOrder = new QstMallOrder();

                qstMallOrder.setOrderNo(orderNo);
                qstMallOrder.setUserId(user.getUserId());
                qstMallOrder.setUserAddress(user.getAddress());
                //总价
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
                //todo 订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
                String extraInfo = "";
                qstMallOrder.setExtraInfo(extraInfo);

                //生成订单项并保存订单项纪录
                if (qstMallOrderMapper.insertSelective(qstMallOrder) > 0) {  //将订单插入到订单表中
                    //生成所有的订单项快照，并保存至数据库
                    List<QstMallOrderItem> qstMallOrderItems = new ArrayList<>();
                    for (QstMallShoppingCartItemVO qstMallShoppingCartItemVO : mySettleShoppingCartItems) {
                        QstMallOrderItem qstMallOrderItem = new QstMallOrderItem();
                        //使用BeanUtil工具类将qstMallShoppingCartItemVO中的属性复制到qstMallOrderItem对象中
                        BeanUtil.copyProperties(qstMallShoppingCartItemVO, qstMallOrderItem);
                        if(qstMallShoppingCartItemVO.getPromotionStatus()==null){
                            qstMallOrderItem.setSellingPrice(qstMallShoppingCartItemVO.getOriginalPrice());
                        }

                        //QstMallOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
                        qstMallOrderItem.setOrderId(qstMallOrder.getOrderId());
                        qstMallOrderItems.add(qstMallOrderItem);
                    }
                    //保存至数据库(将订单中所有商品保存到订单商品表)
                    if (qstMallOrderItemMapper.insertBatch(qstMallOrderItems) > 0) {
                        //所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
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

    /*任务7：根据用户id和订单id，返回订单详情*/
    @Override
    public QstMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        QstMallOrder qstMallOrder = qstMallOrderMapper.selectByOrderNo(orderNo);
        if (qstMallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return null;
            }
            List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderId(qstMallOrder.getOrderId());
            //获取订单项数据
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

    /*分页获取订单*/
    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = qstMallOrderMapper.getTotalQstMallOrders(pageUtil);
        List<QstMallOrder> qstMallOrders = qstMallOrderMapper.findQstMallOrderList(pageUtil);
        List<QstMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(qstMallOrders, QstMallOrderListVO.class);
            //设置订单状态中文显示值
            for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                qstMallOrderListVO.setOrderStatusString(QstMallOrderStatusEnum.getQstMallOrderStatusEnumByStatus(qstMallOrderListVO.getOrderStatus()).getName());
                qstMallOrderListVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(qstMallOrderListVO.getPayType()).getName());
            }
            List<Long> orderIds = qstMallOrders.stream().map(QstMallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<QstMallOrderItem> orderItems = qstMallOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<QstMallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(QstMallOrderItem::getOrderId));
                for (QstMallOrderListVO qstMallOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(qstMallOrderListVO.getOrderId())) {
                        List<QstMallOrderItem> orderItemListTemp = itemByOrderIdMap.get(qstMallOrderListVO.getOrderId());
                        //将QstMallOrderItem对象列表转换成QstMallOrderItemVO对象列表
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
            //验证是否是当前userId下的订单，否则报错
            if (userId.longValue()!= qstMallOrder.getUserId().longValue()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            //订单状态判断
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
            //todo 验证是否是当前userId下的订单，否则报错
            //todo 订单状态判断
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
            //订单状态判断 非待支付状态下不进行修改操作
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
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<QstMallOrderItemVO> qstMallOrderItemVOS = BeanUtil.copyList(orderItems, QstMallOrderItemVO.class);
                return qstMallOrderItemVOS;
            }
        }
        return null;
    }
}
