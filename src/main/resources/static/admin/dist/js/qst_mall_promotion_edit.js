$('.form_datetime').datetimepicker({
    format:'yyyy-mm-dd hh:ii:00',
    language:'zh-CN',
    weekStart: 1,
    todayBtn: 1,
    autoclose: 1,
    todayHighlight: 1,
    startView: 2,
    forceParse: 0,

});


$('#confirmButton').click(function () {
    var promotionId =$('#promotionId').val();
    var goodsId = $('#goodsId').val();
    var promotionName = $('#promotionName').val();
    var promotionPrice = $('#promotionPrice').val();
    var startTime = $('#startTime').val();
    var endTime = $('#endTime').val();
    if (isNull(goodsId)) {
        swal("请输入商品编号", {
            icon: "error",
        });
        return;
    }
    if (isNull(promotionPrice) || promotionPrice < 1) {
        swal("请输入商品价格", {
            icon: "error",
        });
        return;
    }

    if (!isAfterNow(startTime)) {
        swal("请选择当前时间后的时间", {
            icon: "error",
        });
        return;
    }
    if (endTime-startTime<=0) {
        swal("结束时间应在开始之后", {
            icon: "error",
        });
        return;
    }

    var url = '/admin/promotion/save';
    var swlMessage = '保存成功';
    var data = {
        "goodsId": goodsId,
        "promotionName": promotionName,
        "promotionPrice": promotionPrice,
        "startTime": startTime,
        "endTime": endTime,
    };
    if (promotionId > 0) {
        url = '/admin/promotion/update';
        swlMessage = '修改成功';
        data = {
            "promotionId": promotionId,
            "goodsId": goodsId,
            "promotionName": promotionName,
            "promotionPrice": promotionPrice,
            "startTime": startTime,
            "endTime": endTime,
        };
    }
    console.log(data);
    $.ajax({
        type: 'POST',//方法类型
        url: url,
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (result) {
            if (result.resultCode == 200) {
                swal({
                    title: swlMessage,
                    type: 'success',
                    showCancelButton: false,
                    confirmButtonColor: '#1baeae',
                    confirmButtonText: '返回秒杀列表',
                    confirmButtonClass: 'btn btn-success',
                    buttonsStyling: false
                }).then(function () {
                    window.location.href = "/admin/promotion";
                })
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

});



