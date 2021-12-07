/* navigation hover event */
$(function(){
    var headerHeight = $('.header_common').outerHeight();
    $('.header_common .header_gnb_item').hover(function(){
        $('.header_common').css('height', headerHeight + $(this).find('.header_gnb_sub').outerHeight() )
    }, function(){
        $('.header_common').css('height', headerHeight )
    });

});