$(function(){
    /* service cont create */
    $('.header_common .header_gnb_sub_2depth').clone().appendTo('.index_service_cont');

    /* header control */
    /*$('.header_common').addClass('header_index');*/
    $(window).on('scroll', function () {
        if( $(this).scrollTop() < 10){
            $('.header_common').addClass('header_index');
        }else{
            $('.header_common').removeClass('header_index');
        }
    });
    $('.header_common .header_gnb').hover(function(){
        $('.header_common').removeClass('header_index');
    }, function(){
        if( $(window).scrollTop() < 10){
            $('.header_common').addClass('header_index');
        }
    });

    /* news hover event */
    $('.index_news_item').hover(function(){
        $('.index_news_cont_bg').css({
            top: $(this).position().top,
            left: $(this).position().left
        });
        $('.index_news_cont_bg').addClass('over');

        if($(this).hasClass('ele_notice')){
            $('.index_news_cont_bg').addClass('ele_notice');
        }else{
            $('.index_news_cont_bg').removeClass('ele_notice');
        }

        if($(this).hasClass('ele_blog')){
            $('.index_news_cont_bg').addClass('ele_blog');
        }else{
            $('.index_news_cont_bg').removeClass('ele_blog');
        }
    }, function(){
        $('.index_news_cont_bg').removeClass('over');
    });
    typing('.slide0 .slide_txt')
});

