/*
* Copyright 2012-2014 eBay Software Foundation, ios-driver and selendroid committers.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/
$(function() {
  $('#capabilities').click(function(){
   $('#overlay').fadeIn('fast',function(){
      $('#box').animate({'left':'0px'},500);
    });
  });
    
  $('#boxclose').click(function(){
   $('#box').animate({'left':'-2000px'},500,function(){
      $('#overlay').fadeOut('fast');
    });
  });
  $('#htmlshow').click(function(){
   $('#overlayhtml').fadeIn('fast',function(){
      $('#boxhtml').animate({'right':'0px'},500);
    });
  });
    
  $('#boxclosehtml').click(function(){
   $('#boxhtml').animate({'right':'-2000px'},500,function(){
      $('#overlayhtml').fadeOut('fast');
    });
  });
  
});