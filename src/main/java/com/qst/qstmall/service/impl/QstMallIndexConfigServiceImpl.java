package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallIndexConfigGoodsVO;
import com.qst.qstmall.dao.IndexConfigMapper;
import com.qst.qstmall.dao.QstMallGoodsMapper;
import com.qst.qstmall.entity.IndexConfig;
import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.service.QstMallIndexConfigService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QstMallIndexConfigServiceImpl implements QstMallIndexConfigService {

    @Autowired
    private IndexConfigMapper indexConfigMapper;

    @Autowired
    private QstMallGoodsMapper goodsMapper;

    @Override
    public PageResult getConfigsPage(PageQueryUtil pageUtil) {
        List<IndexConfig> indexConfigs = indexConfigMapper.findIndexConfigList(pageUtil);
        int total = indexConfigMapper.getTotalIndexConfigs(pageUtil);
        PageResult pageResult = new PageResult(indexConfigs, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveIndexConfig(IndexConfig indexConfig) {
        //todo 判断是否存在该商品
        if (indexConfigMapper.insertSelective(indexConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateIndexConfig(IndexConfig indexConfig) {
        //todo 判断是否存在该商品
        IndexConfig temp = indexConfigMapper.selectByPrimaryKey(indexConfig.getConfigId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        if (indexConfigMapper.updateByPrimaryKeySelective(indexConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public IndexConfig getIndexConfigById(Long id) {
        return null;
    }

    @Override
    public List<QstMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number) {
        List<QstMallIndexConfigGoodsVO> qstMallIndexConfigGoodsVOS = new ArrayList<>(number);
        List<IndexConfig> indexConfigs = indexConfigMapper.findIndexConfigsByTypeAndNum(configType, number);
        if (!CollectionUtils.isEmpty(indexConfigs)) {
            //取出所有的goodsId
            List<Long> goodsIds = indexConfigs.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
            List<QstMallGoods> qstMallGoods = goodsMapper.selectByPrimaryKeys(goodsIds);
            qstMallIndexConfigGoodsVOS = BeanUtil.copyList(qstMallGoods, QstMallIndexConfigGoodsVO.class);
            // 字符串过长导致文字超出的问题
            for (QstMallIndexConfigGoodsVO qstMallIndexConfigGoodsVO : qstMallIndexConfigGoodsVOS) {
                String goodsName = qstMallIndexConfigGoodsVO.getGoodsName();
                String goodsIntro = qstMallIndexConfigGoodsVO.getGoodsIntro();
                if (goodsName.length() > 30) {
                    goodsName = goodsName.substring(0, 30) + "...";
                    qstMallIndexConfigGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 22) {
                    goodsIntro = goodsIntro.substring(0, 22) + "...";
                    qstMallIndexConfigGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return qstMallIndexConfigGoodsVOS;
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除数据
        return indexConfigMapper.deleteBatch(ids) > 0;
    }
}
