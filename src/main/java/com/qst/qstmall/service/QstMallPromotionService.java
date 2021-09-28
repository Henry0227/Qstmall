package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.PromotionItemVO;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;

import java.util.List;

public interface QstMallPromotionService {

    PageResult getQstMallPromotionPage(PageQueryUtil pageUtil);

    List<PromotionItemVO> getActivatedPromotions(int number);

    Promotion getPromotionsByGoodsId(Long goodsId);

    Promotion getActivatedPromotion(Long goodsId);

    String saveQstMallPromotion(Promotion promotion);
}
