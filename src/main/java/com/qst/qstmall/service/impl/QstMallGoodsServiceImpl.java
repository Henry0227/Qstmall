package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallSearchGoodsVO;
import com.qst.qstmall.dao.QstMallGoodsMapper;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.service.QstMallGoodsService;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class QstMallGoodsServiceImpl implements QstMallGoodsService {

    @Autowired
    private QstMallGoodsMapper goodsMapper;
    @Autowired
    QstMallPromotionService qstMallPromotionService;

    @Override
    public PageResult getQstMallGoodsPage(PageQueryUtil pageUtil) {
        List<QstMallGoods> goodsList = goodsMapper.findQstMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalQstMallGoods(pageUtil);
        PageResult pageResult = new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveQstMallGoods(QstMallGoods goods) {
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveQstMallGoods(List<QstMallGoods> qstMallGoodsList) {
        if (!CollectionUtils.isEmpty(qstMallGoodsList)) {
            goodsMapper.batchInsert(qstMallGoodsList);
        }
    }

    @Override
    public String updateQstMallGoods(QstMallGoods goods) {
        QstMallGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public QstMallGoods getQstMallGoodsById(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    /*任务5：搜索商品*/
    @Override
    public PageResult searchQstMallGoods(PageQueryUtil pageUtil) {
        List<QstMallGoods> goodsList = goodsMapper.findQstMallGoodsListBySearch(pageUtil);

        int total = goodsMapper.getTotalQstMallGoodsBySearch(pageUtil);
        List<QstMallSearchGoodsVO> qstMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            qstMallSearchGoodsVOS = BeanUtil.copyList(goodsList, QstMallSearchGoodsVO.class);
            for (QstMallSearchGoodsVO qstMallSearchGoodsVO : qstMallSearchGoodsVOS) {
                Promotion promotion= qstMallPromotionService.getActivatedPromotion(qstMallSearchGoodsVO.getGoodsId());
                if(promotion!=null){
                    qstMallSearchGoodsVO.setSellingPrice(promotion.getPromotionPrice());
                    qstMallSearchGoodsVO.setPromotionStatus(Constants.PROMOTION_STATUS_STARTED);
                }
                String goodsName = qstMallSearchGoodsVO.getGoodsName();
                String goodsIntro = qstMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    qstMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    qstMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(qstMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }
}
