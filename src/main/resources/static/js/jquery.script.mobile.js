/* navigation control */
$('.header_common .header_gnb_item').each(function(){
    if( $(this).find('.header_gnb_sub_menu').length > 0 || $(this).find('.header_gnb_sub_2depth').length > 0 ){
        $(this).find('.header_gnb_link').attr('href','#');

        $(this).find('.header_gnb_link').on('click', function(){
            $(this).parent().parent().find('.header_gnb_link').removeClass('open');
            $(this).addClass('open');
            $(this).parent().parent().find('.header_gnb_sub').hide();
            $(this).next('.header_gnb_sub').show();
        });
    }
    if( $(this).find('.header_gnb_sub_2depth').length > 0 ){
        $(this).find('.header_gnb_sub_2depth > li > a').attr('href','#');

        $(this).find('.header_gnb_sub_2depth > li > a').on('click', function(){
            $(this).parent().parent().find('a').removeClass('open');
            $(this).addClass('open');
            $(this).parent().parent().find('.header_gnb_sub_3depth').hide();
            $(this).next('.header_gnb_sub_3depth').show();
        });
    }
});

/* navigation btn event */
$('.header_gnb_btn').on('click', function(){
    $('.header_common').toggleClass('header_open');
    $('body').toggleClass('scrolling');
});
$(document).on('click', function(e){
    if(e.target.className == 'header_bg'){
        $('.header_common').removeClass('header_open');
        $('body').removeClass('scrolling');
    }
});
$('.header_common nav').on('scroll', function () {
    if( $(this).scrollTop() > 10){
        $('.header_common.header_open .header_gnb_btn').addClass('outer');
    }else{
        $('.header_common.header_open .header_gnb_btn').removeClass('outer');
    }
});