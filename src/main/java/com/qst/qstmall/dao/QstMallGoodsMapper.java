package com.qst.qstmall.dao;

import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.entity.StockNumDTO;
import com.qst.qstmall.utils.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QstMallGoodsMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(QstMallGoods record);

    int insertSelective(QstMallGoods record);

    QstMallGoods selectByPrimaryKey(Long goodsId);

    int updateByPrimaryKeySelective(QstMallGoods record);

    int updateByPrimaryKeyWithBLOBs(QstMallGoods record);

    int updateByPrimaryKey(QstMallGoods record);

    List<QstMallGoods> findQstMallGoodsList(PageQueryUtil pageUtil);

    int getTotalQstMallGoods(PageQueryUtil pageUtil);

    List<QstMallGoods> selectByPrimaryKeys(List<Long> goodsIds);

    List<QstMallGoods> findQstMallGoodsListBySearch(PageQueryUtil pageUtil);

    int getTotalQstMallGoodsBySearch(PageQueryUtil pageUtil);

    int batchInsert(@Param("qstMallGoodsList") List<QstMallGoods> qstMallGoodsList);

    int updateStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int batchUpdateSellStatus(@Param("orderIds")Long[] orderIds,@Param("sellStatus") int sellStatus);

}