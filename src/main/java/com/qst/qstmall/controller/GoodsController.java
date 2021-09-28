package com.qst.qstmall.controller;

import com.qst.qstmall.common.Constants;
import com.qst.qstmall.controller.vo.QstMallGoodsDetailVO;
import com.qst.qstmall.controller.vo.SearchPageCategoryVO;
import com.qst.qstmall.entity.Promotion;
import com.qst.qstmall.entity.QstMallGoods;
import com.qst.qstmall.service.QstMallCategoryService;
import com.qst.qstmall.service.QstMallGoodsService;
import com.qst.qstmall.service.QstMallPromotionService;
import com.qst.qstmall.utils.BeanUtil;
import com.qst.qstmall.utils.PageQueryUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@Controller
public class GoodsController {
    @Resource
    private QstMallGoodsService qstMallGoodsService;
    @Resource
    private QstMallCategoryService qstMallCategoryService;
    @Resource
    private QstMallPromotionService qstMallPromotionService;

    /**/
    @GetMapping({"/search", "/search.html"})
    public String searchPage(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        if (StringUtils.isEmpty(params.get("page"))) {
            params.put("page", 1);
        }
        //每页十条
        params.put("limit", Constants.GOODS_SEARCH_PAGE_LIMIT);
        //如何類型id不為空，封装分类数据
        if (params.containsKey("goodsCategoryId") && !StringUtils.isEmpty(params.get("goodsCategoryId")+ "")) {
            Long categoryId = Long.valueOf(params.get("goodsCategoryId") + "");
            SearchPageCategoryVO searchPageCategoryVO = qstMallCategoryService.getCategoriesForSearch(categoryId);
            if (searchPageCategoryVO != null) {
                request.setAttribute("goodsCategoryId", categoryId);
                request.setAttribute("searchPageCategoryVO", searchPageCategoryVO);
            }
        }
        //封装参数供前端回显
        if (params.containsKey("orderBy") && !StringUtils.isEmpty(params.get("orderBy") + "")) {
            request.setAttribute("orderBy", params.get("orderBy") + "");
        }
        String keyword = "";
        //对keyword做过滤 去掉空格及其他预定义字符
        if (params.containsKey("keyword") && !StringUtils.isEmpty((params.get("keyword") + "").trim())) {
            keyword = params.get("keyword") + "";
        }
        request.setAttribute("keyword", keyword);
        params.put("keyword", keyword);
        //封装商品数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("pageResult", qstMallGoodsService.searchQstMallGoods(pageUtil));
        return "mall/search";
    }

    /**/
    @GetMapping("/goods/detail/{goodsId}")
    public String detailPage(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        if (goodsId < 1) {
            return "error/error_5xx";
        }
        QstMallGoods goods = qstMallGoodsService.getQstMallGoodsById(goodsId);
        if (goods == null) {
            return "error/error_404";
        }
        QstMallGoodsDetailVO goodsDetailVO = new QstMallGoodsDetailVO();
        BeanUtil.copyProperties(goods, goodsDetailVO);
        goodsDetailVO.setGoodsCarouselList(goods.getGoodsCarousel().split(","));
        goodsDetailVO.setSellingPrice(goods.getOriginalPrice());
        Promotion promotion=qstMallPromotionService.getPromotionsByGoodsId(goodsId);
        if(promotion!=null){
            goodsDetailVO.setStartTime(promotion.getStartTime());
            goodsDetailVO.setEndTime(promotion.getEndTime());
            if(promotion.getStartTime().after(new Date())){
                goodsDetailVO.setPromotionStatus(Constants.PROMOTION_STATUS_NOT_STARTED);//还未开始
            }else if(promotion.getEndTime().before(new Date())){
                    goodsDetailVO.setPromotionStatus(Constants.PROMOTION_STATUS_END);//已经结束
            }else{
                goodsDetailVO.setPromotionStatus(Constants.PROMOTION_STATUS_STARTED);//正在进行
                goodsDetailVO.setSellingPrice(promotion.getPromotionPrice());
            }
        }else{
            goodsDetailVO.setPromotionStatus(Constants.PROMOTION_STATUS_NONE);//无促销
        }
        request.setAttribute("goodsDetail", goodsDetailVO);
        return "mall/detail";
    }
}