package com.qst.qstmall.common;

/**
 * @apiNote 常量配置
 */
public class Constants {
    public final static String FILE_UPLOAD_DIC = "D:\\WXD\\upload\\";//上传文件的默认url前缀，根据部署设置自行修改
    public final static int INDEX_CAROUSEL_NUMBER = 5;//首页轮播图数量(可根据自身需求修改)
    public final static int INDEX_CATEGORY_NUMBER = 10;//首页一级分类的最大数量
    public final static int SEARCH_CATEGORY_NUMBER = 8;//搜索页一级分类的最大数量
    public final static int INDEX_GOODS_HOT_NUMBER = 4;//首页热卖商品数量
    public final static int INDEX_GOODS_NEW_NUMBER = 5;//首页新品数量
    public final static int INDEX_GOODS_RECOMMOND_NUMBER = 10;//首页推荐商品数量
    public final static int PROMOTION_GOODS_NUMBER = 20;//首页推荐商品数量
    public final static int SHOPPING_CART_ITEM_TOTAL_NUMBER = 20;//购物车中商品的最大数量(可根据自身需求修改)
    public final static int SHOPPING_CART_ITEM_LIMIT_NUMBER = 200;//购物车中单个商品的最大购买数量(可根据自身需求修改)
    public final static String MALL_VERIFY_CODE_KEY = "mallVerifyCode";//验证码key
    public final static String MALL_USER_SESSION_KEY = "qstMallUser";//session中user的key
    public final static int GOODS_SEARCH_PAGE_LIMIT = 10;//搜索分页的默认条数(每页10条)
    public final static int ORDER_SEARCH_PAGE_LIMIT = 5;//我的订单列表分页的默认条数(每页5条)
    public final static int SELL_STATUS_UP = 0;//商品上架状态
    public final static int SELL_STATUS_DOWN = 1;//商品下架状态
    public final static int PROMOTION_STATUS_NONE = 0;//无促销状态
    public final static int PROMOTION_STATUS_NOT_STARTED = 1;//促销还未开始状态
    public final static int PROMOTION_STATUS_STARTED = 2;//促销开始状态
    public final static int PROMOTION_STATUS_END = 3;//促销结束状态
    public final static int ADMIN_USER_LOCKED = 1;//管理员账号锁定
}
