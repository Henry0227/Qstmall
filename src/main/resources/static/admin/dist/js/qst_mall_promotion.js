$(function () {
    $("#jqGrid").jqGrid({
        url: '/admin/promotion/list',
        datatype: "json",
        colModel: [
            {label: '秒杀名称', name: 'promotionName', index: 'promotionName', width: 60},
            {label: '商品编号', name: 'goodsId', index: 'goodsId', width: 30, key: true},
            {label: '秒杀价格', name: 'promotionPrice', index: 'promotionPrice', width: 30},
            {label: '活动开始时间', name: 'startTime', index: 'startTime', width: 60},
            {label: '活动结束时间', name: 'endTime', index: 'endTime', width: 60},
            {label: '创建时间', name: 'createTime', index: 'createTime', width: 60},
            {label: '操作', name: 'goodsId', index: 'goodsId', width: 60, formatter: operateFormatter}
        ],
        height: 760,
        rowNum: 20,
        rowList: [20, 50, 80],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "data.list",
            page: "data.currPage",
            total: "data.totalPage",
            records: "data.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order",
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    $(window).resize(function () {
        $("#jqGrid").setGridWidth($(".card-body").width());
    });

    function operateFormatter(cellvalue) {
        return "<a href=\'##\' onclick=openGoodsInfo(" + cellvalue+ ")>查看商品详情</a>" ;

    }

});

/**
 * jqGrid重新加载
 */
function reload() {
    initFlatPickr();
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}


/**
 * 查看订单项信息
 * @param orderId
 */
function openGoodsInfo(goodsId) {
    $('.modal-title').html('商品详情');
    $.ajax({
        type: 'GET',//方法类型
        url: '/admin/goods/info/' + goodsId,
        contentType: 'application/json',
        success: function (result) {
            if (result.resultCode == 200) {
                $('#goodsInfoModal').modal('show');
                var goodsInfo =
                    "<div><dl>" +
                    "<dt>商品编号</dt>"+
                        "<dd>"+result.data.goodsId+"</dd>"+
                    "<dt>商品名称</dt>"+
                        "<dd>"+result.data.goodsName+"</dd>"+
                    "<dt>商品简介</dt>"+
                        "<dd>"+result.data.goodsIntro+"</dd>"+
                    "<dt>商品售价</dt>"+
                        "<dd>"+result.data.originalPrice+"</dd>"+
                    "<dt>商品库存</dt>"+
                        "<dd>"+result.data.stockNum+"</dd>"+
                    "</dl></div>";
                $("#goodsInfoString").html(goodsInfo);
            } else {
                swal(result.message, {
                    icon: "error",
                });
            }
            ;
        },
        error: function () {
            swal("操作失败", {
                icon: "error",
            });
        }
    });
}

/**
 * 添加商品
 */
function addPromotion() {
    window.location.href = "/admin/promotion/edit";
}

/**
 * 修改商品
 */
function editGoods() {
    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    window.location.href = "/admin/goods/edit/" + id;
}

/**
 * 上架
 */
function putUpGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行上架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: "/admin/goods/status/0",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.resultCode == 200) {
                            swal("上架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    )
    ;
}

/**
 * 下架
 */
function putDownGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行下架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: "/admin/goods/status/1",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.resultCode == 200) {
                            swal("下架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.message, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    );
}