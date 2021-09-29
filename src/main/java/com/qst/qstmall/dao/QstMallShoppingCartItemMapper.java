package com.qst.qstmall.dao;

import com.qst.qstmall.entity.QstMallShoppingCartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QstMallShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(QstMallShoppingCartItem record);

    int insertSelective(QstMallShoppingCartItem record);

    QstMallShoppingCartItem selectByPrimaryKey(Long cartItemId);

    QstMallShoppingCartItem selectByUserIdAndGoodsId(@Param("qstMallUserId") Long qstMallUserId, @Param("goodsId") Long goodsId);

    List<QstMallShoppingCartItem> selectByUserId(@Param("qstMallUserId") Long qstMallUserId, @Param("number") int number);

    int selectCountByUserId(Long qstMallUserId);

    int updateByPrimaryKeySelective(QstMallShoppingCartItem record);

    int updateByPrimaryKey(QstMallShoppingCartItem record);

    int deleteBatch(List<Long> ids);

    int deleteByUserId(@Param("userId")Long userId);

    List<QstMallShoppingCartItem> selectByPrimaryKeys(@Param("cartItemIds") List<Long> cartItemIds, @Param("number") int number);
}