package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.PromotionItemVO;
import com.qst.qstmall.dao.QstMallGoodsMapper;
import com.qst.qstmall.dao.QstMallPromotionMapper;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.DateUtil;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QstMallPromotionServiceImpl implements QstMallPromotionService {
    @Autowired
    QstMallPromotionMapper qstMallPromotionMapper;
    @Autowired
    private QstMallGoodsMapper qstMallGoodsMapper;

    @Override
    public PageResult getQstMallPromotionPage(PageQueryUtil pageUtil) {
        List<Promotion> promotions = qstMallPromotionMapper.findQstMallPromotionList(pageUtil);
        int total = qstMallPromotionMapper.getTotalQstMallPromotions(pageUtil);
        PageResult pageResult = new PageResult(promotions, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }


    @Override
    public List<PromotionItemVO> getActivatedPromotions(int number) {
        List<PromotionItemVO> promotionItemVOS=new ArrayList<>(number);
        //找到前20条促销活动
        List<Promotion> promotions=qstMallPromotionMapper.findPromotions();
        if(!CollectionUtils.isEmpty(promotions)){
            Date now=new Date();
            for (Promotion promotion:promotions) {
                if (now.after(promotion.getStartTime())&&now.before(promotion.getEndTime())){
                    PromotionItemVO promotionItemVO=new PromotionItemVO();
                    BeanUtil.copyProperties(promotion,promotionItemVO);
                    promotionItemVOS.add(promotionItemVO);
                    if(promotionItemVOS.size()==number){
                        break;
                    }
                }
            }
        }
        if(promotionItemVOS.size()>0){
            //获得所有促销商品ID
            List<Long> goodsIds = promotionItemVOS.stream().map(PromotionItemVO::getGoodsId).collect(Collectors.toList());
            //获得所有促销商品
            List<QstMallGoods> qstMallGoods = qstMallGoodsMapper.selectByPrimaryKeys(goodsIds);
            Map<Long, QstMallGoods> newBeeMallGoodsMap = qstMallGoods.stream().collect(Collectors.toMap(QstMallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            //判断商品库存
            for (PromotionItemVO promotionItemVO : promotionItemVOS) {
                QstMallGoods tempGood= newBeeMallGoodsMap.get(promotionItemVO.getGoodsId());
                promotionItemVO.setGoodsName(tempGood.getGoodsName());
                promotionItemVO.setOriginalPrice(tempGood.getOriginalPrice());
                promotionItemVO.setGoodsCoverImg(tempGood.getGoodsCoverImg());
            }
        }
        return promotionItemVOS;
    }

    @Override
    public Promotion getPromotionsByGoodsId(Long goodsId) {
        Promotion promotion=qstMallPromotionMapper.selectByGoodsId(goodsId);
        if (promotion!=null){
            Date startTime=promotion.getStartTime();
            if(!DateUtil.isBetween24H(startTime)){
                return null;
            }
        }
        return promotion;
    }

    @Override
    public Promotion getActivatedPromotion(Long goodsId) {
        Promotion promotion=this.getPromotionsByGoodsId(goodsId);
        if(promotion!=null){
            Date now=new Date();
            if (now.after(promotion.getStartTime())&&now.before(promotion.getEndTime())){
                return promotion;
            }
        }

        return null;
    }

    @Override
    public String saveQstMallPromotion(Promotion promotion) {
        if (qstMallPromotionMapper.insertSelective(promotion) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }
}
