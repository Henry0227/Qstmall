package com.qst.qstmall.service.impl;

import com.qst.qstmall.common.ServiceResultEnum;
import com.qst.qstmall.controller.vo.QstMallIndexCarouselVO;
import com.qst.qstmall.dao.CarouselMapper;
import com.qst.qstmall.entity.Carousel;
import com.qst.qstmall.service.QstMallCarouselService;
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
public class QstMallCarouselServiceImpl implements QstMallCarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    @Override
    public PageResult getCarouselPage(PageQueryUtil pageUtil) {
        List<Carousel> carousels = carouselMapper.findCarouselList(pageUtil);
        int total = carouselMapper.getTotalCarousels(pageUtil);
        PageResult pageResult = new PageResult(carousels, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveCarousel(Carousel carousel,Integer user) {
        carousel.setCreateUser(user);
        carousel.setUpdateUser(user);
        if (carouselMapper.insertSelective(carousel) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateCarousel(Carousel carousel,Integer user) {
        Carousel temp = carouselMapper.selectByPrimaryKey(carousel.getCarouselId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        temp.setCarouselRank(carousel.getCarouselRank());
        temp.setRedirectUrl(carousel.getRedirectUrl());
        temp.setCarouselUrl(carousel.getCarouselUrl());
        temp.setUpdateTime(new Date());
        temp.setUpdateUser(user);
        if (carouselMapper.updateByPrimaryKeySelective(temp) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public Carousel getCarouselById(Integer id) {
        return carouselMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Integer[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //????????????
        return carouselMapper.deleteBatch(ids) > 0;
    }

    /*??????4?????????????????????*/
    @Override
    public List<QstMallIndexCarouselVO> getCarouselsForIndex(int number) {
        /*????????????????????????VO???????????????*/
        List<QstMallIndexCarouselVO> qstMallIndexCarouselVOS = new ArrayList<>(number);

        /*??????dao???????????????????????????PO??????????????????*/
        List<Carousel> carousels = carouselMapper.findCarouselsByNum(number);

        /*???PO????????????????????????VO???????????????*/
        if (!CollectionUtils.isEmpty(carousels)) {
            qstMallIndexCarouselVOS = BeanUtil.copyList(carousels, QstMallIndexCarouselVO.class);
        }
        return qstMallIndexCarouselVOS;
    }
}
