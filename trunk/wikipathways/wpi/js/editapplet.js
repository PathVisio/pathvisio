var label_maximize = '<img src="/skins/common/images/magnify-clip.png" id="maximize"/>';
var label_minimize = '<img src="/skins/common/images/magnify-clip.png" id="minimize"/>';
var initial_applet_width = '500px';

//Uses appletobject.js
function doApplet(idImg, idApplet, keys, values) {
	var image = document.getElementById(idImg);
	
	var w = getParentWidth(image);

	image.style.width = w + 'px';
	image.style.height = initial_applet_width;
	//Clear all existing content
	image.innerHTML = '';
	image.setAttribute('class', 'thumbinner');

	//First create a new div for the applet and add it to the idImg
	appletDiv = document.createElement('div');
	appletDiv.id = idApplet;
	appletDiv.setAttribute('class', 'internal');
	appletDiv.style.width = '100%';
	appletDiv.style.height = '95%';
	appletDiv.style.clear = 'both';
	image.appendChild(appletDiv);
		
	//Create maximize div
	maximize = document.createElement('div');
	maximize.innerHTML = createMaximizeButton(idImg);
	maximize.style.cssFloat = 'center';
	maximize.style.marginBottom = '10px';

	//Create resize hint
	resize = document.createElement('div');
	resize.innerHTML = '<img src="/skins/common/images/resize.png';
	resize.style.position = 'absolute';
	resize.style.bottom = '0';
	resize.style.right = '0';
	image.appendChild(resize);
	image.appendChild(maximize);

	var ao = new AppletObject(	'org.pathvisio.gui.wikipathways.AppletMain',
				['/wpi/applet/wikipathways.jar'],
				'100%', '100%', '1.5.0', 'false',
				'/wpi/applet',
				[],
				AppletObjects.TAG_OBJECT );
	if(keys != null && values != null) {
		for(i=0; i < keys.length; i++) {
					ao.addParam(keys[i], values[i]);
		}
	}
	
	new Resizeable(idImg, {bottom: 10, right: 10, left: 0, top: 0});

	ao.load( idApplet );
}

//Manually (doesn't work well, applet is started twice on firefox
function replaceWithApplet(idImg, idApplet, keys, values) {
	var image = document.getElementById(idImg);
	var applet = createObjectElement(idApplet, keys, values);

	image.setAttribute('class', 'thumbinner');

	var w = getParentWidth(image);

	image.style.width = w + 'px';
	image.style.height = '500px';

	new Resizeable(idImg, {bottom: 10, right: 10, left: 0, top: 0});
	image.innerHTML = applet;
}

function getParentWidth(elm) {
	var p = findEnclosingTable(elm);
	return p.offsetWidth;	
}

function findEnclosingTable(elm) {
	//find getWidth of enclosing element / table
	var parent = elm.parentNode;
	var nn = parent.nodeName.toLowerCase();
	if(nn == 'td' || nn == 'tr' || nn == 'tbody') {
		while(true) {
			if(parent.parentNode == null || parent.nodeName.toLowerCase() == 'table') {
				break;
			} else {
				parent = parent.parentNode;
			}
		}
	}
	if(parent.nodeName.toLowerCase() == 'table') return parent;
	else return elm.parentNode; //Not in a table, just return the parent
}

function replaceElement(elmOld, elmNew) {
	var p = elmOld.parentNode;
	p.insertBefore(elmNew, elmOld);
	p.removeChild(elmOld);
}

var maxImg = '/skins/common/images/magnify-clip.png';

/* Maximize functions
 * TODO: create maximizable class with prototype
 */
function createMaximizeButton(id) {
	return('<img href="javascript:;" src="' + maxImg + "\" onclick=\"toggleMaximize(this, '" + id + "', 'maximize', '', '');\" />");
}

function toggleMaximize(button, id, action, oldWidth, oldHeight) {
	var element = document.getElementById(id);
	if(action == 'maximize') {
		var oldWidth = element.offsetWidth;
		var oldHeight = element.offsetHeight;
		var parent = element.parentNode;
		var left = parent.offsetLeft;
		var top = parent.offsetTop;
		while((parent = parent.parentNode) != document) { left += parent.offsetLeft; top += parent.offsetTop; }
		var newWidth = getViewportWidth() - left;
		var newHeight = getViewportHeight() - top;
		element.style.width = newWidth + 'px';
		element.style.height = newHeight + 'px';
		button.setAttribute('onclick', 'toggleMaximize(this, "' + id + '", "restore", "' + oldWidth + 'px", "' + oldHeight + 'px");');
	} else {
		element.style.width = oldWidth;
		element.style.height = oldHeight;
		button.setAttribute('onclick', 'toggleMaximize(this, "' + id + '", "maximize", "", "");');
	}
}

/** COPIED FROM Util v1.06:
 * Copyright (c)2005-2007 Matt Kruse (javascripttoolbox.com)
 * 
 * Dual licensed under the MIT and GPL licenses. 
 * This basically means you can use this code however you want for
 * free, but don't claim to have written it yourself!
 * Donations always accepted: http://www.JavascriptToolbox.com/donate/
 * 
 * Please do not link to the .js files on javascripttoolbox.com from
 * your site. Copy the files locally to your server instead.
 * 
 */
function getViewportWidth() {
		if (document.documentElement && (!document.compatMode || document.compatMode=="CSS1Compat")) {
			return document.documentElement.clientWidth;
		}
		else if (document.compatMode && document.body) {
			return document.body.clientWidth;
		}
		return self.innerWidth;
};
function getViewportHeight() {
		if (!window.opera && document.documentElement && (!document.compatMode || document.compatMode=="CSS1Compat")) {
			return document.documentElement.clientHeight;
		}
		else if (document.compatMode && !window.opera && document.body) {
			return document.body.clientHeight;
		}
		return self.innerHeight;
};
/** END COPIED **/


function createObjectElement(id, keys, values) {
	var tag = '<object classid="java:org.pathvisio.gui.wikipathways.AppletMain.class"'
		+ ' TYPE="application/x-java-applet"'
		+ ' ARCHIVE="wikipathways.jar"'
		+ ' CODEBASE="/wpi/applet"'
		+ ' WIDTH="100%"'
		+ ' HEIGHT="100%"'
		+ ' STANDBY="Loading applet..."'
		+ ' >';
	if(keys != null && values != null) {
		for(var i = 0; i < keys.length; i++) {
			tag += '<param  name="' + keys[i]  + '" ' + 
				'value="' + values[i] + '" />';
		}
	}
	tag += '</object>';
		return tag;
}

function createAppletElement(id, keys, values) {
	var tag = '<applet code="org.pathvisio.gui.wikipathways.AppletMain"'
		+ 'ARCHIVE="wikipathways.jar"'
		+ 'CODEBASE="/wpi/applet"'
		+ 'WIDTH="100%"'
		+ '"HEIGHT="100%"'
		+ '>';
	if(keys != null && values != null) {
		for(var i = 0; i < keys.length; i++) {
			tag += '<param  name="' + keys[i]  + '" ' + 
				'value="' + values[i] + '" />';
		}
	}
	tag += '</applet>';
		return tag;
}
