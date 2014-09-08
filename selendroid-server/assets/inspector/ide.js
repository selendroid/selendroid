/*
* Copyright 2012-2014 eBay Software Foundation, ios-driver and selendroid committers.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the Licence at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/

$(document).ready(function () {

    var inspector = new Inspector("#tree");

});

var realOffsetX = 0;
var realOffsetY = 0;

var scale = 1;
var scale_highlight = 1;

var frame_h = 0;
var frame_w = 0;

var screen_h = 0;
var screen_w = 0;

var to_top = 0;
var to_left = 0;

var margin = 0;
var treeAndDetailInPercent = 0.48;
findFrameSizeInPixels = function () {
    //var width = window.innerWidth;
    //var leftForFrame = width * (1 - treeAndDetailInPercent);
    //return leftForFrame;
    var w = $("#device").width();
    return w;
};

var device;
var orientation;
var variation;

configure = function (d, v, o) {
    device = d;
    orientation = o;
    variation = v;
    var FRAME_IPAD_H = 1108;
    var FRAME_IPAD_W = 852;
    var SCREEN_IPAD_H = 1024;
    var SCREEN_IPAD_W = 768;
    var SCREEN_TO_TOP_IPAD = 42;
    var SCREEN_TO_LEFT_IPAD = 42;

    var FRAME_IPHONE_H = 716;
    var FRAME_IPHONE_W = 368;
    var SCREEN_IPHONE_H = 480;
    var SCREEN_IPHONE_W = 320;
    var SCREEN_TO_TOP_IPHONE = 118;
    var SCREEN_TO_LEFT_IPHONE = 24;

    if (variation === 'Retina4') {
        SCREEN_IPHONE_H = 568;
        SCREEN_IPHONE_W = 320;
        FRAME_IPHONE_W = 386;
        FRAME_IPHONE_H = 806;
        SCREEN_TO_LEFT_IPHONE = 33;
        SCREEN_TO_TOP_IPHONE = 119;
    }

    if (device === 'ipad') {
        frame_h = FRAME_IPAD_H;
        frame_w = FRAME_IPAD_W;
        screen_h = SCREEN_IPAD_H;
        screen_w = SCREEN_IPAD_W;
        to_top = SCREEN_TO_TOP_IPAD;
        to_left = SCREEN_TO_LEFT_IPAD;
    } else if (device === 'iphone') {
        frame_h = FRAME_IPHONE_H;
        frame_w = FRAME_IPHONE_W;
        screen_h = SCREEN_IPHONE_H;
        screen_w = SCREEN_IPHONE_W;
        to_top = SCREEN_TO_TOP_IPHONE;
        to_left = SCREEN_TO_LEFT_IPHONE;
    } else {
        // console.log("error, wrong device :" + device);
    }

};

/**
* returns the html source of the webview, if any.
*/
getHTMLSource = function () {
    return source;
}
var source = null;
setHTMLSource = function (newSource) {
    source = newSource;
}
resize = function () {

    var neededSpace = frame_w;
    if (orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT' || orientation
        === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT') {
        neededSpace = frame_h;
    }
	var screen_width = document.getElementById('screenshot').height;
	var real_width = document.getElementById('screenshot').naturalHeight;
	scale_highlight = 1 / (real_width / screen_width)
    var leftInPixel = findFrameSizeInPixels();
    scale = leftInPixel / neededSpace;
    if (scale > 1) {
        scale = 1;
    }

    //$('#simulator').css('-moz-transform', 'scale(' + scale + ')');
    $('#screen').css('top', to_top + 'px');
    $('#screen').css('left', to_left + 'px');

    var angle = 0;
    var mouseOver_w = screen_w;
    var mouseOver_h = screen_h;
    var width = frame_w;
    realOffsetX = margin + to_left;
    realOffsetY = margin + to_top;

    if (orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT') {
        angle = 90;
        $('#rotationCenter').css('left', (frame_h + margin) + 'px');
        $('#rotationCenter').css('top', margin + 'px');

        // for landscape, w becomes h and h becomes w.
        mouseOver_w = screen_h;
        mouseOver_h = screen_w;
        realOffsetX = margin + to_top;
        realOffsetY = margin + to_left;
        width = frame_h;

    } else if (orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT' || orientation
        === 'LANDSCAPE') {
        angle = -90;
        $('#rotationCenter').css('left', margin + 'px');
        $('#rotationCenter').css('top', (frame_w + margin) + 'px');
        mouseOver_w = screen_h;
        mouseOver_h = screen_w;
        realOffsetX = margin + to_top;
        realOffsetY = margin + to_left;
        width = frame_h;

    } else if (orientation === 'UIA_DEVICE_ORIENTATION_PORTRAIT' || orientation === 'PORTRAIT') {
        angle = 0;
        $('#rotationCenter').css('left', margin + 'px');
        $('#rotationCenter').css('top', margin + 'px');

    } else if (orientation === 'UIA_DEVICE_ORIENTATION_PORTRAIT_UPSIDEDOWN') {
        angle = 180;
        $('#rotationCenter').css('left', frame_w + margin + 'px');
        $('#rotationCenter').css('top', frame_h + margin + 'px');

    }

    $('#mouseOver').css('top', realOffsetY + 'px');
    $('#mouseOver').css('left', realOffsetX + 'px');
    $('#mouseOver').css('height', mouseOver_h + 'px');
    $('#mouseOver').css('width', mouseOver_w + 'px');

    $('#rotationCenter').css('-moz-transform', 'rotate(' + angle + 'deg)');
    $('#rotationCenter').css('-webkit-transform', 'rotate(' + angle + 'deg)');

    $('#rotationCenter').css('-moz-transform', 'scale(' + scale + ')');
    $('#rotationCenter').css('-webkit-transform', 'scale(' + scale + ')');

};

$(window).resize(function () {
    resize();
});

