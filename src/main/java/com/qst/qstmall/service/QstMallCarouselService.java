package com.qst.qstmall.service;

import com.qst.qstmall.controller.vo.QstMallIndexCarouselVO;
import com.qst.qstmall.entity.Carousel;
import com.qst.qstmall.utils.PageQueryUtil;
import com.qst.qstmall.utils.PageResult;

import java.util.List;

public interface QstMallCarouselService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getCarouselPage(PageQueryUtil pageUtil);

    String saveCarousel(Carousel carousel, Integer user);

    String updateCarousel(Carousel carousel,Integer user);

    Carousel getCarouselById(Integer id);

    Boolean deleteBatch(Integer[] ids);

    /**
     * 返回固定数量的轮播图对象(首页调用)
     *
     * @param number
     * @return
     */
    List<QstMallIndexCarouselVO> getCarouselsForIndex(int number);
}
