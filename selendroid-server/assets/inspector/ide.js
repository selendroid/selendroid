$(document).ready(function() {

	// offsetX Y set in the main page as global variable.
	var tree = $("#tree").jstree({
		"core" : {
			"animation" : 0,
			"load_open" : true
		},
		"json_data" : {
			"ajax" : {
				"url" : "/inspector/tree"
			}
		},
		"themes" : {
			"theme" : "apple",
			// "dots" : false,
			// "icons" : false
		},
		"plugins" : ["themes", "json_data", "ui"]
	});

	tree.bind("select_node.jstree", function(e, data) {

	});

	tree.bind("hover_node.jstree", function(e, data) {
		if(!lock) {
			setSelected(data);
		}

	});
	var root;
	tree.bind("loaded.jstree", function(event, data) {
		root = tree.jstree('get_json')[0];
		tree.jstree("open_all");
	});
	tree.bind("refresh.jstree", function(event, data) {
		root = tree.jstree('get_json')[0];
		tree.jstree("open_all");
	});
	setSelected = function(node) {
		var rect;
		var type;
		var ref;
		var name;
		var label;
		var value;
		var l10n;

		if(node.metadata) {// from tree parsing, json node
			rect = node.metadata.rect;
			type = node.metadata.type;
			ref = node.metadata.reference;
			name = node.metadata.name;
			label = node.metadata.label;
			value = node.metadata.value;
			l10n = node.metadata.l10n
		} else {// from listener, jstree node
			rect = node.rslt.obj.data("rect");
			type = node.rslt.obj.data('type');
			ref = node.rslt.obj.data('reference');
			name = node.rslt.obj.data('name');
			label = node.rslt.obj.data('label');
			value = node.rslt.obj.data('value');
			l10n = node.rslt.obj.data('l10n');

		}
		tree.jstree('deselect_all');
		tree.jstree('select_node', '#' + ref);
		var translationFound = (l10n.matches != 0);

		highlight(rect.x, rect.y, rect.h, rect.w, translationFound);
		showDetails(type, ref, name, label, value, rect, l10n);
		showActions(type, ref);
	}
	showActions = function(type, ref) {
		// check action per type.
		$('#reference').html("<input type='hidden' name='reference' value='" + ref + "'>");
	}
	highlight = function(x, y, h, w, translationFound) {
		$('#highlight').css('left', x + realOffsetX + 'px');
		$('#highlight').css('top', y + realOffsetY + 'px');
		$('#highlight').css('height', h + 'px');
		$('#highlight').css('width', w + 'px');

		var color;
		if(translationFound) {
			color = "blue";
		} else {
			color = "yellow";
		}
		$('#highlight').css("background-color", color);

	}
	showDetails = function(type, ref, na, label, value, rect, l10n) {
		var prettyL10N = "";

		if(l10n) {
			prettyL10N = "<h3>L10N</h3>";
			var matches = l10n.matches;
			prettyL10N += "<p><b>Matches</b>: " + matches + "</p>";

			if(matches > 0) {
				prettyL10N += "<p><b>Key</b>: " + l10n.key + "</p>";
				var langs = l10n.langs;
				for(var name in langs) {
					var result = langs[name];
					for(var a in result) {
						prettyL10N += "<p><b>" + a + "</b> : " + result[a] + "</p>";
					}

				}
			}

		} else {
			prettyL10N = "no l10n for --" + name + "--";
		}

		$('#details').html("<h3>Details</h3>" + "<p><b>Type</b>: " + type + "</p>" + "<p><b>Reference</b>: " + ref + "</p>" + "<p><b>Name</b>: " + na + "</p>" + "<p><b>Label</b>: " + label + "</p>" + "<p><b>Value</b>: " + value + "</p>" + "<p><b>Rect</b>: x=" + rect.x + ",y=" + rect.y + ",h=" + rect.h + "w=" + rect.w + "</p>" + prettyL10N);

	};
	var root;
	$("#mouseOver").mousemove(function(e) {

		if(!lock) {
			var x = e.pageX / scale - realOffsetX;
			var y = e.pageY / scale - (realOffsetY + 45);
			// x = x / scale;
			// y = y / scale;
			console.log(x + "," + y);
			var finder = new CandidateFinder(x, y, root);
			var node = finder.getNode();
			if(node) {
				setSelected(node);
			} else {
				console.log('couldn t find element at ' + x + ' , ' + y + root);
			}
		}

	});

	$(document).keydown(function(e) {
		if(e.ctrlKey) {
			toggleLock();
		}
	});
	var lock = false;
	toggleLock = function() {
		lock = !lock;
	};
	function CandidateFinder(x, y, rootNode) {
		this.x = x;
		this.y = y;

		this.matchScore = -1;
		this.candidate = null;

		this.rootNode = rootNode;
		// (this.x , this.y) is inside the area covered by
		// this node.
		this._hasCorrectPosition = function(node) {
			var currentX = node.metadata.rect.x;
			var currentY = node.metadata.rect.y;
			var currentH = node.metadata.rect.h;
			var currentW = node.metadata.rect.w;

			if((currentX <= this.x) && (this.x <= (currentX + currentW))) {
				if((currentY <= this.y) && (this.y <= (currentY + currentH))) {
					return true;
				}
			}
			return false;

		};
		this._assignIfBetterCandidate = function(newNode) {
			if(this._hasCorrectPosition(newNode)) {
				var surface = (newNode.metadata.rect.h * newNode.metadata.rect.w);
				if(this.candidate) {
					if(surface < this.matchScore) {
						this.matchScore = surface;
						this.candidate = newNode;
					}
				} else {
					this.matchScore = surface;
					this.candidate = newNode;
				}
			}
		};

		this.getNode = function() {
			this._getCandidate(this.rootNode);
			return this.candidate;
		};

		this._getCandidate = function(from) {
			this._assignIfBetterCandidate(from);
			if(from.children) {
				for(var i = 0; i < from.children.length; i++) {
					var child = from.children[i];
					this._getCandidate(child);
				}
			}
		};
	}

});
var realOffsetX = 0;
var realOffsetY = 0;

var scale = 1;

var frame_h = 0;
var frame_w = 0;

var screen_h = 0;
var screen_w = 0;

var to_top = 0;
var to_left = 0;

var margin = 25;
var treeAndDetailInPercent = 0.48;
findFrameSizeInPixels = function() {
	var width = window.innerWidth;
	var leftForFrame = width * (1 - treeAndDetailInPercent);
	return leftForFrame;

};
var device;
var orientation;
configure = function(d, o) {
	device = d;
	orientation = o;
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

	if(device === 'ipad') {
		frame_h = FRAME_IPAD_H;
		frame_w = FRAME_IPAD_W;
		screen_h = SCREEN_IPAD_H;
		screen_w = SCREEN_IPAD_W;
		to_top = SCREEN_TO_TOP_IPAD;
		to_left = SCREEN_TO_LEFT_IPAD;
	} else if(device === 'iphone') {
		frame_h = FRAME_IPHONE_H;
		frame_w = FRAME_IPHONE_W;
		screen_h = SCREEN_IPHONE_H;
		screen_w = SCREEN_IPHONE_W;
		to_top = SCREEN_TO_TOP_IPHONE;
		to_left = SCREEN_TO_LEFT_IPHONE;
	} else {
		console.log("error, wrong device :" + device);
	}

};
resize = function() {

	var neededSpace = frame_w;
	if(orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT' || orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT') {
		neededSpace = frame_h;
	}
	var leftInPixel = findFrameSizeInPixels();
	scale = leftInPixel / neededSpace;
	if(scale > 1) {
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

	if(orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT') {
		angle = 90;
		$('#rotationCenter').css('left', (frame_h + margin) + 'px');
		$('#rotationCenter').css('top', margin + 'px');

		// for landscape, w becomes h and h becomes w.
		mouseOver_w = screen_h;
		mouseOver_h = screen_w;
		realOffsetX = margin + to_top;
		realOffsetY = margin + to_left;
		width = frame_h;

	} else if(orientation === 'UIA_DEVICE_ORIENTATION_LANDSCAPELEFT') {
		angle = -90;
		$('#rotationCenter').css('left', margin + 'px');
		$('#rotationCenter').css('top', (frame_w + margin) + 'px');
		mouseOver_w = screen_h;
		mouseOver_h = screen_w;
		realOffsetX = margin + to_top;
		realOffsetY = margin + to_left;
		width = frame_h;

	} else if(orientation === 'UIA_DEVICE_ORIENTATION_PORTRAIT') {
		angle = 0;
		$('#rotationCenter').css('left', margin + 'px');
		$('#rotationCenter').css('top', margin + 'px');

	} else if(orientation === 'UIA_DEVICE_ORIENTATION_PORTRAIT_UPSIDEDOWN') {
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
