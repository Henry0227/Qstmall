var qswiper = new Swiper('.swiper-container', {
    //设置自动播放
    autoplay: {
        delay: 5000,
        disableOnInteraction: false
    },
    //设置无限循环播放
    loop: false,
    //设置圆点指示器
    pagination: {
        el: '.swiper-pagination',
        dynamicBullets: true,
    },
    //设置上下页按钮
    navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev',
    }
})

/** 数量修改*/
$('#btn-add').on('click',function () {
    if($('#buy-num').val()<200){
        $('#buy-num').prop('value', parseInt($('#buy-num').val())+1);
    }
})
$('#btn-reduce').on('click',function () {
    if($('#buy-num').val()>1){
        $('#buy-num').prop('value', parseInt($('#buy-num').val())-1);
    }
})
$('#buy-num').on('change',function () {
    if($.isNumeric($('#buy-num').val())){
        if($('#buy-num').val()>200){
            $('#buy-num').prop('value', 200);
        }else if($('#buy-num').val()<1){
            $('#buy-num').prop('value', 1);
        }
    }else{
        $('#buy-num').prop('value', 1);
    }
})


$('#detail .tab-main .tab li').on('click',function () {
    $('#detail .tab-main .tab li').removeAttr('class')
    $(this).prop('class','current');
})
