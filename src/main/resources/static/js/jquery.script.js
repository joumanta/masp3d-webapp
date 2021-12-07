$(function(){
    /* footer sitemap create */
    $('.header_common .header_gnb').clone().appendTo('.footer_nav');


    /* side menu btn event */
    $('.side_menu_btn').on('click', function(){
        $('.side_util').toggleClass('on');
        return false
    });

    $(document).on('click', function(e){
        /* side menu event */
        if(e.target.className == 'side_util'){
            return false
        } else {
            $('.side_util').removeClass('on')
        }
    });

    /* header x-scroll event */
    $(window).on('scroll', function(){
        $('.header_common').css('left', -($(this).scrollLeft()) );
        $('.calculator_wrap').css('left', -($(this).scrollLeft()) );
    });

    /* btn_toggle event */
    $('.btn_toggle').on('click', function(){
        $(this).toggleClass('on');
    });

    /* tab event */
    $('.cont_tab_btn').on('click', function(){
        $(this).addClass('on');
        $(this).siblings('.cont_tab_btn').removeClass('on');

        $( '#'+$(this).attr('data-target') ).show();
        $( '#'+$(this).attr('data-target') ).siblings().hide();
    });



    $('.select_wrap select').selectric();
    customCheck();
    customRadio();
    $('.js_dropdown').dropDownBox();
});

/* input custom */
function customCheck() {
    var check = $('.label_check input[type=checkbox]');
    check.each(function(){
        if ($(this).is(":checked")) {
            $(this).parent().addClass('selected');
        }
    });
    check.on('change', function(){
        $(this).parent().toggleClass('selected');
    });
    check.on('focus', function(){
        $(this).parent().addClass('focus');
    }).on('blur', function(){
        $(this).parent().removeClass('focus');
    });
}
function customRadio() {
    var radio = $('.label_radio input[type=radio]');
    if(!radio.length){return;}

    radio.each(function(){
        this.flag = $(this).prop('checked') ? true : false;
        if(this.flag){$(this).parent().addClass('selected');}
    });

    radio.on('click keypress', function(){
        var name = $(this).attr('name');
        var $parent = $(this).parent();
        var $siblings = $('input[name='+name+']').parent();

        this.flag = $(this).prop('checked') ? true : false;
        $siblings.removeClass('selected');
        $parent.addClass('selected');
    });

    radio.on('focus', function(){
        $(this).parent().addClass('focus');
    }).on('blur', function(){
        $(this).parent().removeClass('focus');
    })
}

/* popup event */
function popupOpen(pop){
    $(pop).fadeIn(100);
    $('body').addClass('scrolling');
    $(pop).find('.pop_btn_close, .btn_btm.event_close').on('click', function () {
        $(pop).fadeOut(100);
        $('body').removeClass('scrolling');
    })
}

/* mobile check */
// function headerCSSFormatter(){
//     if (isMobile()) {
//         $('<link href="/lib/css/mobile.css" rel="stylesheet">').appendTo("head");
//         $('<script src="/lib/js/jquery.script.mobile.js">').appendTo("body");
//     }else{
//         $('<link href="/lib/css/pc.css" rel="stylesheet">').appendTo("head");
//         $('<script src="/lib/js/jquery.script.pc.js">').appendTo("body");
//     }
// }
// headerCSSFormatter();
//
// function isMobile(){
//     var UserAgent = navigator.userAgent;
//     return UserAgent.match(/iPhone|iPod|iPad|Android|Windows CE|BlackBerry|Symbian|Windows Phone|webOS|Opera Mini|Opera Mobi|POLARIS|IEMobile|lgtelecom|nokia|SonyEricsson/i) != null || UserAgent.match(/LG|SAMSUNG|Samsung/) != null;
// }

/* event popup setting */
var popEvent = function(popup,date){
    var layer = $('#'+popup);
    var btnClose = layer.find( $('.pop_btn_today') ) ;
    var popClose = layer.find( $('.pop_event_close') );
    var edate= new Date(date);

    layer.show();

    //닫기버튼 누를시 하루동안 열지않기
    cookiedata = document.cookie;
    if ( Date.now() <= edate && cookiedata.indexOf(popup+"=done") < 0 ){
        layer.show();
    }
    else {
        layer.hide();
    }

    function setCookie( name, value, expiredays ) {
        var todayDate = new Date();
        todayDate.setDate( todayDate.getDate() + expiredays );
        document.cookie = name + "=" + escape( value ) + "; path=/; expires=" + todayDate.toGMTString() + ";"
    }

    btnClose.on("click", function(){
        //expiredays의 1은 하루를 의미한다, 일주일은 7, 1년은 365로 입력
        setCookie( popup, "done" , 1);
        layer.fadeOut('fast');
    });

    popClose.on("click", function(){
        layer.fadeOut('fast');
    });
};

/* onepage menu */
function scrollMenu(name) {
    var current = $('[data-name='+ name +']');
    var target = current.parent().find('.header_gnb_scroll');
    current.addClass('on');
    target.find('li:first a').addClass('on');

    var c1 = $("#cont02").offset().top-100;
    var c2 = $("#cont03").offset().top-100;

    $('.header_gnb_scroll a').on('click', function(){
        var _top = $('#cont0'+ ($(this).parent().index()+1)).offset().top;
        $('html, body').stop().animate({scrollTop: _top },500);
        return false;
    });

    $(window).on('scroll', function(){
        var scrollTop = $(window).scrollTop();
        target.find('a').removeClass('on');
        if (scrollTop < c1) {
            target.find('li:eq(0) a').addClass("on")
        } else {
            if (scrollTop >= c1 && scrollTop < c2) {
                target.find('li:eq(1) a').addClass("on")
            } else {
                if( $("#cont04").length ){
                    var c3 = $("#cont04").offset().top-100;
                    if (scrollTop >= c2 && scrollTop < c3) {
                        target.find('li:eq(2) a').addClass("on")
                    } else {
                        if (scrollTop >= c3 ) {
                            target.find('li:eq(3) a').addClass("on")
                        }
                    }
                } else {
                    if (scrollTop >= c2 ) {
                        target.find('li:eq(2) a').addClass("on")
                    }
                }
            }
        }
    });
}

/* dropdown */
$.fn.dropDownBox = function(options) {
    return this.each(function() {
        var $obj = $(this),
            $btn = $obj.find(".js_dropdown_btn"),
            $list = $obj.find(".js_dropdown_list");
        // 상태초기화
        function reset() {
            $(".js_dropdown").each(function () {
                $(this).removeClass('active');
                $(this).find('.js_dropdown_list').slideUp(50);
            });
        }

        $btn.on("click", function(e) {
            if (!$obj.hasClass("active")) {
                reset();
                $obj.addClass("active");
                $list.slideDown(50);
                $('.header_common').addClass('over');
            } else { reset(); }

            $(document).on("click", function () {
                reset();
                $(document).unbind("click");
                setTimeout(function() {
                    $('.header_common').removeClass('over');
                }, 100);
            });

            // 이벤트 방지
            e.stopPropagation();
        });

    });
};