package com.qst.qstmall.dao;

import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.utils.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QstMallPromotionMapper {

    List<Promotion> findQstMallPromotionList(PageQueryUtil pageUtil);

    int getTotalQstMallPromotions(PageQueryUtil pageUtil);

    Promotion selectByPrimaryKey(Long promotionId);

    Promotion selectByGoodsId(Long goodsId);

    int deleteByPrimaryKey(Long promotionId);

    int insertSelective (Promotion promotion);

    int updateByPrimaryKeySelective(Promotion promotion);

    List<Promotion> findPromotions();
}
