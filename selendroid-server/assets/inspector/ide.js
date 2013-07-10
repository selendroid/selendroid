/*
 * Copyright 2012 ios-driver committers.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the Licence at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

$(document).ready(function() {

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
var treeAndDetailInPercent = 0.5;
findFrameSizeInPixels = function() {
	var width = window.innerWidth;
	var leftForFrame = width * (1 - treeAndDetailInPercent);
	return leftForFrame;

};

var device;
var orientation;
var variation;

configure = function(d, v, o) {
	device = d;
	orientation = o;
	variation = v;
	var SCREEN_TO_TOP_IPHONE = 5;
	var SCREEN_TO_LEFT_IPHONE = 5;
	var rect = document.getElementById('screenshot').getBoundingClientRect();
	console.log(rect.top, rect.right, rect.bottom, rect.left);

	frame_h = document.getElementById('screenshot').height;
	frame_w = document.getElementById('screenshot').width;
	screen_h = document.getElementById('screenshot').height;
	screen_w = document.getElementById('screenshot').width;
	to_top = SCREEN_TO_TOP_IPHONE;
	to_left = SCREEN_TO_LEFT_IPHONE;
};

/**
 * returns the html source of the webview, if any.
 */
getHTMLSource = function() {
	return source;
}
var source = null;
setHTMLSource = function(newSource) {
	source = newSource;
}
resize = function() {
	$('#screenshot').imgscale(); 
	var neededSpace = frame_w;
	if (orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT'
			|| orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT') {
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

	$('#simulator').css('-moz-transform', 'scale(' + scale + ')');
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

	} else if (orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT'
			|| orientation === 'LANDSCAPE') {
		angle = -90;
		$('#rotationCenter').css('left', margin + 'px');
		$('#rotationCenter').css('top', (frame_w + margin) + 'px');
		mouseOver_w = screen_h;
		mouseOver_h = screen_w;
		realOffsetX = margin + to_top;
		realOffsetY = margin + to_left;
		width = frame_h;

	} else if (orientation === 'UIA_DEVICE_ORIENTATION_PORTRAIT'
			|| orientation === 'PORTRAIT') {
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

};

$(window).resize(function() {
	resize();
});
