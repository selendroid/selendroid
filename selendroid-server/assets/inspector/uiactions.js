$(function() {
  $('#capabilities').click(function(){
  	$('#overlay').fadeIn('fast',function(){
      $('#box').animate({'top':'160px'},500);
    });
  });
  
  $('#languages').click(function(){
  	$('#overlayLanguages').fadeIn('fast',function(){
      $('#boxlanguages').animate({'top':'160px'},500);
    });
  });
  
  $('#boxclose').click(function(){
  	$('#box').animate({'top':'-500px'},500,function(){
      $('#overlay').fadeOut('fast');
    });
  });
  
  $('#boxcloselanguages').click(function(){
  	$('#boxlanguages').animate({'top':'-500px'},500,function(){
      $('#overlayLanguages').fadeOut('fast');
    });
  });

});