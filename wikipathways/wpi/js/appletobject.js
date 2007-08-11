/*
 *  AppletObject
 *
 *  Florian Jenett, Stephen Williams, Aaron Steed
 *
 *  http://appletobject.org/
 *
 *  -----------------------------------------------------------
 *
 *	changed: 2007-05-24 07:36:41 - fjenett
 *	version: 0.0.6
 *
 *  -----------------------------------------------------------
 *
 *    
 *
 *  ----------------------------------------------------------- */


/**
        getElement()
        
        return an DOMElement by ID
 */
 
var getElement = function (aID)
{ 
     return ((document.getElementById) ? document.getElementById(aID)
                                      : document.all[aID]);
};


if ( !Array.prototype.push ) {
/**
        Array.push()
        
        win IE 5.01 fix,
        push items onto an array, return new length
 */
Array.prototype.push = function() 
{
    for(var j = 0, n = arguments.length; j < n; ++j) {
        this[this.length] = arguments[j];
    }
    return this.length;
};
};



if ( !Array.prototype.shift ) {
/**
        Array.shift()
        
        win IE 5.01 fix,
        remove and return first item in an array
 */
Array.prototype.shift = function ()
{
    if ( this.length == 0 ) return null;
    var val = this[0];
    var arr = [];
    for ( var i= 0; i < this.length  ; i++) arr[i]  = this[i];
    for ( var i= 0; i < this.length-1; i++) this[i] = arr[i+1];
    this[this.length-1] = null;
    return val;
};
};


if ( !Array.prototype.concat ) {
/**
        Array.concat()
        
        win IE 5.01 fix,
        append an array to another
 */
Array.prototype.concat = function ()
{
    for ( var i=0; i< arguments[0].length; i++ ) this[this.length+i] = arguments[0][i];
};
};


if ( !Number.prototype.toFixed ) {
/**
        Number.toFixed()
        
        win IE 5.01 fix,
        return a fixed length float for a number
 */
Number.prototype.toFixed = function ( fractionDigits )
{
   var fStr = this.toString();
   var len = fStr.lastIndexOf('.') + fractionDigits + 1;
   if ( len == fractionDigits ) return this;
   return (1.0 * fStr.substring(0,len));
};
};

//  Function.prototype.bind , $A
//  taken from:
/*--------------------------------------------------------------------------*/
/*  Prototype JavaScript framework, version 1.5.0_rc2
 *  (c) 2005-2007 Sam Stephenson
 *
 *  Prototype is freely distributable under the terms of an MIT-style license.
 *  For details, see the Prototype web site: http://prototype.conio.net/
 */
/*--------------------------------------------------------------------------*/

/**
        Function.bind()
        
        bind a function to an object
        from: Prototype JavaScript framework, version 1.5.0_rc2
 */

Function.prototype.bind = function()
{  
  var __method = this, args = $A(arguments), object = args.shift();
  return function() {
    return __method.apply(object, args.concat(arguments));
  };
};

/**
        $A
        
        return an array for an iterable
        from: Prototype JavaScript framework, version 1.5.0_rc2
 */
 
var $A = function(iterable) {
  if (!iterable) return [];
  if (iterable.toArray) {
    return iterable.toArray();
  } else {
    var results = [];
    for (var i = 0, length = iterable.length; i < length; i++)
      results.push(iterable[i]);
    return results;
  }
};


/**
        Array.implode()
        
        in love with PHPs implode function,
        opposite to string.split(seperator)
        
        @param string seperator

        example:
          (["1","2","3"]).implode( ", " );
          > "1, 2, 3"
 */
 
Array.prototype.implode = function ( _sep )
{
    var i = 0;    var _str = this[0];
    while ( this[i+1] ) _str = _str + _sep + this[++i];
    return _str;
};



/**
        singleton prototype AppletObjects
        
        a wrapper around all AppletObjects, it does:
        - java detection
        - handles callbacks from the preloader-applet
        - maintains access to all appletobjects
 */
 
AppletObjects =
{
    objects : [], // array containing all appletobjects
    push : function ( _obj )
    {
        return this.objects.push(_obj)-1; // return the ID of the object injected
    },
    
    create : function ( opt )
    {
        return new AppletObject( opt.code, 
                                 opt.archives, 
                                 opt.width, opt.height,
                                 
                                 opt.minimumVersionString,
                                 opt.mayscript,
                                 opt.codebase,
                                 opt.params,
                                 opt.tagType );
    },
    
    // Preloading-applet callbacks
    //
    inited : function ( _id )
    {
        if ( this.objects[_id] ) this.objects[_id].inited = true;
    },
    started : function ( _id )
    {
        if ( this.objects[_id] ) this.objects[_id].started = true;
    },
    
    readRegistry : false,
    useBrutForceDetectionForIE : true,
    IEDetectUnder13 : true, // see not in getJavaVersionWithBrutForce()
    debugLevel : 0,
    
    JAVA_PLUGIN_MISSING : -1,
    JAVA_DISABLED : -2,
    JAVA_PLUGIN_TOO_OLD : -3,
    hasJava : function ()
    {
        // refresh() is not working properly ..
        //if ( navigator.plugins && navigator.plugins.refresh )
        //        navigator.plugins.refresh(true);
        
        var jMimeType = 'application/x-java-applet';
        
        var hasPlugin = false; // true as default for IE
        
        // netscape mac seems to wrongly report javaEnabled() as false.
        // so we take our chances and just assume it's enabled ...
        //
        var isEnabled = (navigator.userAgent.toLowerCase().match("netscape")
                         ? true
                         : navigator.javaEnabled());
        
        // [fjen] i read that actually VBScript is more compatible on IE.
        //          we should think / talk about switching over to that ...
        
        if (    window.ActiveXObject
             && navigator.plugins.length == 0 ) // msIE
        {
            // checks for JavaPlugin versions 1.2.0_00 to 1.6.6_14
            // IE win only
            //
            var vers; var n1 = 12, n2 = 0, n3 = 0;
            for ( n1 = 12; n1 < 17 && !hasPlugin; n1++ )
            {
                for ( n2 = 0; n2 < 7 && !hasPlugin; n2++ )
                {
                    for ( n3 = 0; n3 < 15 && !hasPlugin; n3++ )
                    {
                        vers = n1+(n2+'_'+(n3<10?'0'+n3:n3));
                        try {
                            hasPlugin = new ActiveXObject('JavaPlugin.'+vers);
                            if ( hasPlugin )
                            {
                                var versString = '1.' +
                                                 (n1 - 10) + '.' +
                                                 (n2+'_'+(n3<10?'0'+n3:n3));
                                
                                this.JREVersion =  
                                    new AppletObjects.JavaVersion( versString );
                                
                                if ( this.debugLevel == 0 ) 
                                    this.saveJavaVersionToCookie( javaVersion );
                            }
                        } catch (e) {}
                    }
                }
            }
            if ( !hasPlugin ) return AppletObjects.JAVA_PLUGIN_MISSING;
        }
        
        
        var i, j;
        for (i = 0;    i < navigator.plugins.length 
                    && !hasPlugin; i++)
        {
            if ( navigator.plugins[i].name.toLowerCase().match("java") )
            {
                for (j = 0; j < navigator.plugins[i].length && !hasPlugin; j++)
                {
                    hasPlugin = navigator.plugins[i][j].type.match(jMimeType);
                    //if ( hasPlugin )  alert( navigator.plugins[i][j].type );
                }
            }
        }
        
        // [fjen] this is an opera fix, java will not be in plugins[],
        //          but in mimetypes[]
        //          sidenote: after deinstalling java on winXP, opera 8.51
        //          kept the mimetype around.
        //
        for (i = 0;    i < navigator.mimeTypes.length 
                    && !hasPlugin; i++)
        {
            hasPlugin = navigator.mimeTypes[i].type.toLowerCase().match(jMimeType);
            //if ( hasPlugin )  alert( navigator.mimeTypes[i].type );
        }
        
        
        // [fjen] opera is not registering the java-plugin with 
        //          navigator.plugins, we try to rely on isEnabled
        //          for that ...
        //
        var returnValue = true;
        
        if ( !isEnabled && hasPlugin ) {
        
            returnValue = AppletObjects.JAVA_DISABLED;
            
        } else if ( !hasPlugin ) {
        
            returnValue = AppletObjects.JAVA_PLUGIN_MISSING;
        }

        return returnValue;
    },
    
    /**
     *  This code is based on some code posted to the Java Forum. 
     *  Have another look to find the authors details.
     *
     *    http://forum.java.sun.com/thread.jspa?threadID=168544
     */
    
    JREVersion : null,
    
    getJavaVersion : function ()
    {
        if (this.debugLevel==0)
        {
            var JREVersionFromCookie = this.getCookie("JREVersion");
            if (JREVersionFromCookie)
            {
                //alert("from cookie: "+JREVersionFromCookie);
                this.JREVersion = new AppletObjects.JavaVersion(JREVersionFromCookie);
                return JREVersionFromCookie;
            }
        }
        if ( this.JREVersion ) return this.JREVersion;

        var javaVersion = new AppletObjects.JavaVersion("0.0.0_0");
        var agt=navigator.userAgent.toLowerCase();
        
        this.browser = agt;
        
        var is_major = parseInt(navigator.appVersion);
    
        var is_nav = (      (agt.indexOf('mozilla')!=-1)
                       && (agt.indexOf('spoofer')==-1)
                       && (agt.indexOf('compatible') == -1) 
                       && (agt.indexOf('opera')==-1)
                       && (agt.indexOf('webtv')==-1) 
                       && (agt.indexOf('hotjava')==-1)  );
                       
        var is_nav4up= (is_nav && (is_major >= 4));
        
        var is_ie    = ((agt.indexOf("msie") != -1) && (agt.indexOf("opera") == -1));
        
        var is_ie5   = (is_ie && (is_major == 4) && (agt.indexOf("msie 5.0") !=-1) );
        var is_ie5_5 = (is_ie && (is_major == 4) && (agt.indexOf("msie 5.5") !=-1));
        var is_ie6   = (is_ie && (is_major == 4) && (agt.indexOf("msie 6.0") !=-1));
        var is_ie7   = (is_ie && (is_major == 4) && (agt.indexOf("msie 7.0") !=-1));
        var is_ie5up = (is_ie && (is_major == 4) 
                       && (    (agt.indexOf("msie 5.0")!=-1)
                            || (agt.indexOf("msie 5.5")!=-1)
                            || (agt.indexOf("msie 6.0")!=-1) 
                            || (agt.indexOf("msie 7.0")!=-1) 
                        ) );
    
        var pluginDetected = false;
        var activeXDisabled = false;
        
        // we can check for plugin existence only when browser is 'is_ie5up' or 'is_nav4up'
        if (is_nav4up)
        {
            // Refresh 'navigator.plugins' to get newly installed plugins.
            // Use 'navigator.plugins.refresh(false)' to refresh plugins
            // without refreshing open documents (browser windows)
            //
            // [fjen] this is actually not working in some cases .. opera i think had problems.
            //          have to recheck which browsers ignore it though.
            
            if (navigator.plugins) 
            {
                navigator.plugins.refresh(false);
            }
        
            // check for Java plugin in installed plugins
            if ( navigator.mimeTypes )
            {
                for ( var i=0; i < navigator.mimeTypes.length; i++ )
                {
                    mimeType = navigator.mimeTypes[i].type;
                    
                    // [fjen]
                    // ";jpi-version="
                    // i wonder if all browsers actually report the mimetypes in this format.
                    // - safari mac will not have jpi-version in mimetype
                    
                    if( (mimeType != null)
                        && (mimeType.indexOf( "application/x-java-applet;jpi-version=") != -1) )
                    {
                            var versionIndex = mimeType.indexOf("version=");
                            var tmpJavaVersion = 
                                new AppletObjects.JavaVersion(mimeType.substring(versionIndex+8));
                            if ( tmpJavaVersion.isGreater(javaVersion) )
                            {
                                javaVersion = 
                                new AppletObjects.JavaVersion(mimeType.substring(versionIndex+8));
                            }
                            pluginDetected = true;
                    }
                }
            }
        }
        else if (is_ie5up)     // [fjen] what about IE 5.2 Mac? came installed until osx 10.3
        {
            registryBeenRead = false;
            if ( this.readRegistry )
            {
                /*
                 * Using the shell causes IE to display a warning that the script
                 * may not be safe.
                 *
                 */
                var shell;
                try
                {
                    // Create WSH(WindowsScriptHost) shell, available on Windows only
                    shell = new ActiveXObject("WScript.Shell");
            
                    if (shell != null) 
                    {
                        // Read JRE version from Window Registry
                        try
                        {
                            javaVersion = 
                                new AppletObjects.JavaVersion( shell.regRead(
                                            "HKEY_LOCAL_MACHINE\\Software\\"+
                                            "JavaSoft\\Java Runtime Environment\\CurrentVersion"));
                            registryBeenRead = true;
                            pluginDetected = true;
                        } catch(e) {
                            // handle exceptions raised by 'shell.regRead(...)' here
                            // so that the outer try-catch block would receive only
                            // exceptions raised by 'shell = new ActiveXObject(...)'
                        }
                    } else { 
                        //alert("Couldn t get shell");
                    }
                } catch(e) {
                    // Creating ActiveX controls thru script is disabled
                    // in InternetExplorer security options
                    
                    // To enable it:
                    // a. Go to the 'Tools --> Internet Options' menu
                    // b. Select the 'Security' tab
                    // c. Select zone (Internet/Intranet)
                    // d. Click the 'Custom Level..' button which will display the
                    // 'Security Settings' window.
                    // e. Enable the option 'Initialize and script ActiveX controls
                    // not marked as safe'
                
                    activeXDisabled = true;
                }
            }
            
            // so, this is only IE5+ ?
            
            if ( !registryBeenRead && this.useBrutForceDetectionForIE )
            {
                javaVersion = this.getJavaVersionWithBrutForce( this.minimumVersion ); // where does this arg end up?
                pluginDetected = javaVersion.isGreater(AppletObjects.JavaVersion("0.0.0_0"));
            }
        }
        if(!pluginDetected){
        	// if the plugin can't be detected we probably have a script error
        	// and should just write the applet code to the browser and 
        	// hope that the version is infact new enough
			javaVersion = new AppletObjects.JavaVersion("99.99.99_99");
		}
		
        this.JREVersion = javaVersion;
        if (this.debugLevel==0){
            this.saveJavaVersionToCookie(javaVersion);
        }
        return javaVersion;
    },

    /**
     * This function tries to instantiate JavaPlugin.??? objects.
     * JRE versions 1.1.1_06 through to 1.3.1 installed a JavaSoft.JavaBeansBridge object.
     */
     
    getJavaVersionWithBrutForce: function ()
    {
        var javaVersion = new AppletObjects.JavaVersion("0.0.0_0");
        startOfRegistryClasses = new AppletObjects.JavaVersion("1.3.1_1");
        try
        {
//            if(!this.minimumVersion.isGreater(startOfRegistryClasses)){
            if (this.IEDetectUnder13)
            {
                /* this call caused the java console to load, which takes 1 or two seconds.
                * If you are going to display an applet anyway this is no problem otherwise
                * you might want to avoid using it.
                *
                * [fjen] so, the java-console will open no matter what?
                */
                result = new ActiveXObject("JavaSoft.JavaBeansBridge");
                if (result)
                {
                    javaVersion = new AppletObjects.JavaVersion("1.1.1_06");
                }
            }
        } catch (e) {}
//        }
// if I check every time I write an applet 
// then I can start looking from the supplied min version
//        major = 10+this.minimumVersion.major;

        major = 13;
        for (; major <= 16; major++)            //major  1.3 - 1.6
        {            
            for (minor=0; minor <= 2; minor++)    //minor 0  - 2;  I have also seen Java version 1.1.4 to 1.1.8
            {    
                for (sub=0; sub <= 20; sub++)    //major  0 - 20???
                {
                    subVersion = "";
                    if (sub > 0)
                    {
                        subVersion = "_";
                        if (sub < 10)
                        {
                            subVersion = subVersion + "0" + sub;
                        } 
                        else
                        {
                            subVersion = subVersion + "" + sub;
                        }
                    }
                    regVersion = major+""+minor+subVersion;
                    if (major==15)
                    {
//                        alert(regVersion);
                    }
                    try 
                    {
                        result = new ActiveXObject("JavaPlugin."+regVersion);
                        if (result) 
                        {
                            var version = ""+(major/10) + "." + minor+subVersion;
                            javaVersion = new AppletObjects.JavaVersion(version);
                            
                            if (this.debugLevel==0) 
                            {
                                //alert(regVersion);
                                javaVersion.show();
                            }
                        }
                    } catch(e) {}
//                    if(!this.minimumVersion.isGreater(javaVersion)){
//                        return javaVersion;
//                    }
                }
            }
        }
        return javaVersion;
    },
    
    /**
     *         setCookie(nameOfCookie, value, expireHours);
     *         
     *         used to save something in a cookie on the client machine
     */

    setCookie: function (nameOfCookie, value, expireHours)
    {
        var expireDate = new Date ();
          expireDate.setTime(expireDate.getTime() + (expireHours * 3600 * 1000));
          document.cookie = nameOfCookie + "=" + escape(value) + "; path=/" + ((expireHours == null) ? "" : "; expires=" + expireDate.toGMTString());
    },

    /**
     *         getCookie(nameOfCookie);
     *         
     *         used to get something from a cookie on the client machine
     */

    getCookie: function(nameOfCookie)
    {
        if (document.cookie.length > 0)
        {
            var begin = document.cookie.indexOf(nameOfCookie+"=");
            if (begin != -1)
            {
                begin += nameOfCookie.length+1; 
                var end = document.cookie.indexOf(";", begin);
                if (end == -1)
                    end = document.cookie.length;
            
                return unescape(document.cookie.substring(begin, end));
            }
            return null; 
          }
        return null; 
    },


    /**
     *         saveJavaVersionToCookie();
     *         
     *         used to save the java version so we don't have to retest it 
     *         next time the user wants to load an applet.
     */
    
    saveJavaVersionToCookie : function(JREVersion)
    {
        var now = new Date();

        userid = this.getCookie("AOUSER_ID");
        if (userid == null || (userid==""))
        {
            var randomnumber = Math.floor(Math.random() * 10000);
            userid = "aouser_id" + now.getTime() +"r"+ randomnumber
        };
        //alert(JREVersion);
        this.setCookie("AOUSER_ID", userid, 10000);
        this.setCookie("JREVersion", JREVersion, 10000);
        userid="";
        userid=this.getCookie("AOUSER_ID");
    },
    
    TAG_APPLET : 1,
    TAG_OBJECT : 2,
    TAG_EMBED  : 4,
    
    
    PRELOAD_TIMEDOUT : -13
};


/**
        prototype JavaVersion()
        
        represents a java version
 */

AppletObjects.JavaVersion = function (version)
{
    this.minor = 0;
    this.rev = 0;
    arrVersion = version.split(".");
    
    if (arrVersion[2] != null) 
    {
        arrMinorAndRev = arrVersion[2].split("_");
        this.minor = arrMinorAndRev[0] != null ? parseInt(arrMinorAndRev[0]) : 0;
        
        if (arrMinorAndRev[1] != null)
        {
            if (arrMinorAndRev[1].substring(0,1)=="0")
            {
                this.rev = parseInt(arrMinorAndRev[1].substring(1,2));
            }
            else
            {
                this.rev = parseInt(arrMinorAndRev[1]);
            }
        }
    }
    
    this.superMajor = arrVersion[0] != null ? parseInt(arrVersion[0]) : 0;
    this.major = arrVersion[1] != null ? parseInt(arrVersion[1]) : 0;
};


/**
        AppletObjects.JavaVersion.isGreater()
        
        compare two JavaVersion objects
 */

AppletObjects.JavaVersion.prototype.isGreater = function (fv)
{
    if(this.major < fv.major) return false;
    if(this.major > fv.major) return true;
    if(this.minor < fv.minor) return false;
    if(this.minor > fv.minor) return true;
    if(this.rev < fv.rev) return false;
    return true;
};


/**
        AppletObjects.JavaVersion.show()
        
        used for debugging
 */

AppletObjects.JavaVersion.prototype.show = function ()
{
    //alert(this.toString());
};


/**
        AppletObjects.JavaVersion.toString()
        
        return a string representation of this JavaVersion object
 */

AppletObjects.JavaVersion.prototype.toString = function ()
{
    var versionString = ""+this.superMajor+
            "."+this.major+
            "."+this.minor;

    if ( this.rev )
    {
        if( this.rev >= 10 )
        {
            versionString += "_"+this.rev;
        }
        else
        {
            versionString += "_0"+this.rev;
        }
    }
    return versionString;
};


var AppletObject = 
/**
        prototype AppletObject()
        
        represents an applet object
 */
function AppletObject ( ) 
{
    this.code         = arguments[0];
    
    this.archives = new Array();
    if ( arguments[1] )
    {
        this.archives     = (arguments[1][0].length > 1)
                          ? arguments[1]
                          : arguments[1].split(',');
    }
    
/*ORIGINAL
    this.width      = arguments[2] > 0 ? arguments[2] : 100; // [fjen] alert? read from element?
    this.height     = arguments[3] > 0 ? arguments[3] : 100;
*/
//MODIFIED
    this.width      = arguments[2] > 0 ? arguments[2] : '100%'; // [fjen] alert? read from element?
    this.height     = arguments[3] > 0 ? arguments[3] : '100%';

    var minimumVersionString = arguments[4] ? arguments[4] : 0;
                      
    this.mayscript  = arguments[5] ? arguments[5] : 'true';
    
    this.codebase   = arguments[6] != '' ? arguments[6] : null;
    
    this.params = new Array();
    if (arguments[7]) {
        this.addParams(arguments[7]);
    }
    
    this.tagType    = arguments[8] && arguments[8] > 0 && arguments[8] < 5
                      ? arguments[8]
                      : AppletObjects.TAG_OBJECT; // [fjen] changed that to object as default
    
    this.fallback    = 'To view this content, you need to install '+
                       'Java from <A HREF="http://java.com">java.com</A>';
                       
    this.java_disabled_message = '<p><strong>'+
                                'Java is disabled in your browsers preferences.<br />'+
                                'You need to activate it to view this applet.'+
                                '<'+'/strong><'+'/p>'+
                                'Reload this page once you enabled Java.';
                                
    this.java_plugin_message = '<p><strong>'+
                                'This browser does not have a Java Plug-in.'+
                                '<'+'/strong><'+'/p>'+
                                '<a href="http://java.sun.com/products/plugin/downloads/index.html">'+
                                'Get the latest Java Plug-in here.'+
                                '<'+'/a>';

    this.java_version_message = '<p><strong>'+
                                'This browser does not have a recent enough Java Plug-in.'+
                                '<'+'/strong><'+'/p>'+
                                '<a href="http://java.sun.com/products/plugin/downloads/index.html">'+
                                'Get the latest Java Plug-in here.'+
                                '<'+'/a>';

    this.loading_message = '<b>Loading applet ...<'+'/'+'b>';
    
    this.preload_timedout_message = '<b>Preloading timed out!<'+'/'+'b>';
    
    this.currentJar = 0;
    
    this.loaded     = false;
    this.inited     = false;
    this.started     = false;
    
    this.container     = null;
    this.preloadContainer = null;
    this.preLoadClass = "de.bezier.js.preloading.Preloading.class";
    this.preLoadJar   = "wikipathways.jar";
    this.timeoutFunctionID     = null;
    this.timeLastPreload = 0;
    
    // [fjenett 20070219] 1 minute, timeout if preloader fails (no callbacks)
    this.preloadTimeout = 1000*60*1;
    
    // [fjen] should fork, but 0 would strangle konqeror 3.x
    this.wait         = 500;
    this.loadChecks = 0;
    
    this.javaCheck = AppletObjects.hasJava();
    
    if ( minimumVersionString.length > 0 )
    {        
        AppletObjects.getJavaVersion();
        this.minimumVersion = new AppletObjects.JavaVersion( minimumVersionString );
        
        if ( !AppletObjects.JREVersion.isGreater( this.minimumVersion ) )
        {
            this.javaCheck = AppletObjects.JAVA_PLUGIN_TOO_OLD;
        }
    }
    
    this.id = AppletObjects.push(this);
};


/**
        AppletObject.getContainer()
        
        set and return the DOMElement into which the applet will be created
 */

AppletObject.prototype.getContainer = function (elmID)
{
    if (!elmID && this.container) return this.container;
    if (this.container == null ) this.container = getElement(elmID);
    return this.container;
};



/**
        AppletObject.alterElement()
        
        2007-02-10 17:38:49 - fjenett
        this is a fix for camino 1.0.3 mac,
        which will flicker when using Element.innerHTML = ...
        
        todo: check element is !null and DOM
 */

AppletObject.prototype.alterElement = function ( element, html_snip )
{
    setTimeout( function(){ element.innerHTML=html_snip; }, 10 );
};


/**
        default callback from Preloading applet
        
        onfail() will be called once if loading fails.
 */

AppletObject.prototype.onfail = function( err, element_id )
{
    switch ( err )
    {
        case AppletObjects.PRELOAD_TIMEDOUT:
            this.alterElement(getElement( this.element_id ), this.preload_timedout_message);
            break;
            
        case AppletObjects.JAVA_DISABLED:
            this.alterElement(getElement( this.element_id ), this.java_disabled_message);
            break;
            
        case AppletObjects.JAVA_PLUGIN_TOO_OLD:
            this.alterElement(getElement( this.element_id ), this.java_version_message);
            break;
            
        case AppletObjects.JAVA_PLUGIN_MISSING:
        default:
            this.alterElement(getElement( this.element_id ), this.java_plugin_message);
    }
};


/**
        default callback from Preloading applet
        
        oninit() will be called once just before loading starts.
 */
 
AppletObject.prototype.oninit = function()
{
    this.alterElement( getElement( this.element_id ), this.loading_message);
};



/**
        default callback from Preloading applet
        
        onstep() will be called twice per jar.
 */
 
AppletObject.prototype.onstep = function(perc)
{
    this.alterElement( getElement( this.element_id ),
        '<b>Loading applet:<'+'/b><br /><br />' +
        '<div style="width:100px"><p style="color:#ffffff;' + 
                    'background-color:#AAAA99;' + 
                    'width:' + Math.floor(perc)+'%;' + 
                    'overflow:hidden;' + 
                    '">' +
            perc + "%" +
        '<'+'/p>'+'<'+'/div>' );
};



/**
        default callback from Preloading applet
        
        onload() will be called once when preloading is finished.
 */
 
AppletObject.prototype.onload = function()
{
    //this.debug( this.element_id );
    this.writeToElement( this.element_id );
};


/**
        AppletObject._checkNext()
        
        check preloading status in preloading loop
 */

AppletObject.prototype._checkNext = function ()
{
    window.clearTimeout(this.timeoutFunctionID);
    
    if ( !this.preloadContainer )
    {
        this.preloadContainer = document.createElement("div");
        // opera 9 mac won't load hidden applets, so let it
        // be very tiny and sit in the upper left corner

        this.preloadContainer.style.position = "absolute";
        this.preloadContainer.style.top  = '0px';
        this.preloadContainer.style.left = '0px';
        this.preloadContainer.style.width  = '1px';
        this.preloadContainer.style.height = '1px';
        this.preloadContainer.style.borderWidth = '0px';
        this.preloadContainer.style.zIndex = 1000;
        document.body.insertBefore( this.preloadContainer, document.body.lastChild.nextSibling ); // insertAfter
    }
    
    var loadlet = this.preloadContainer.firstChild;
    
    var isActive = ( loadlet && document.applets.length > 0
                       && loadlet == document.applets[document.applets.length-1]
                       ? true : false );
    
    if ( !this.loaded && isActive )
    {
        if ( this.currentJar >= this.archives.length )
        {
            this.loaded = true;
            if ( Function.prototype.apply )
                this.timeoutFunctionID = window.setTimeout(
                            this._loadCleanup.bind(this), 5000 );
                            // 5sec, let FF 1.5 have some time to breathe
            else
                this._setTimeout( '_loadCleanup()', 1000);
                
            return this.onload();
        }
                
        if ( this.inited )
        {
            this.inited  = false;
            this.perc++;
            this.onstep( (this.perc * this.stepPerc).toFixed(2) );
        }
    
        if ( this.started )
        {
            this.started = false;
            this.perc++;
            this.onstep( (this.perc * this.stepPerc).toFixed(2) );
            this._loadNext();
        }
        
        if ( ((new Date()).getTime() - this.timeLastPreload) > this.preloadTimeout )
        {
            
            if ( Function.prototype.apply )
                this.timeoutFunctionID = window.setTimeout(
                            this._loadCleanup.bind(this), 5000 );
            else
                this._setTimeout( '_loadCleanup()', 1000);
                
            this.onfail( AppletObjects.PRELOAD_TIMEDOUT );
            
            return;
        }
    }
    else if ( !loadlet )
    {
        this._loadNext();
    }
    else return alert( 'Error: preloading class is missing.' );
    
    if ( Function.prototype.apply ) 
        this.timeoutFunctionID = window.setTimeout( this._checkNext.bind(this), this.wait);
    else
        this._setTimeout( '_checkNext()', 1000);
};


/**
        AppletObject._setTimeout()
        
        internal function to do a bound setTimeout on this AppletObject.
        this is a fix for win IE 5.01
 */

AppletObject.prototype._setTimeout = function ( _fnc, delay )
{
    // fix win 5.01
    var __code = 'AppletObjects.objects['+this.id+'].'+_fnc;
    this.timeoutFunctionID = window.setTimeout( function(){eval(__code);}, delay);
};


/**
        AppletObject._loadCleanup()
        
        internally called to do some cleanup after preloading
 */

AppletObject.prototype._loadCleanup = function ()
{
    window.clearTimeout(this.timeoutFunctionID);
    this.preloadContainer.style.top  = '-10px';
    this.preloadContainer.style.left = '-10px';
    this.preloadContainer.style.display = 'none';
    this.preloadContainer.parentNode.removeChild(this.preloadContainer.parentNode.lastChild);
};


/**
        AppletObject._loadNext()
        
        internally called to preload one jar
        
        todo: maybe change to <object> ?
 */

AppletObject.prototype._loadNext = function ()
{
    this.preloadContainer.archives = this.archives[this.currentJar]+','+this.preLoadJar;
    this.currentJar++;
    this.alterElement( this.preloadContainer, 
					   '<applet '+  'code="'+this.preLoadClass+'" '+
									 'archive="'+this.preloadContainer.archives+'" '+
									   ( this.codebase ? 
									   'codebase="' + this.codebase + '" ' : '' ) +
									 'width="1"'+
									 'height="1"'+
									 'mayscript="true">'+
							'<param name="AObject" value="'+this.id+'" />'+
							'<param name="boxbgcolor" value="'+this.getParam('boxbgcolor')+'" />'+
						'</applet>' 
						);
    
    this.timeLastPreload = (new Date()).getTime();
    
    //alert( this.preloadContainer.innerHTML );
};


/**
        AppletObject.preload()
        
        start preloading loop
 */

AppletObject.prototype.preload = function ( emlID )
{
    this.element_id = emlID;

    if (this.javaCheck!=true) { return this.onfail(this.javaCheck); }
    
    // no archives, no preload!
    if ( this.archives.length <= 0 )
    {
        this.oninit();
        this.onload();
        return;
    }
    
    this.loaded  = false;
    this.started = false;
    this.inited  = false;
    
    this.oninit();
    
    this.currentJar = 0;
    this.preloadContainer = null;
    this.stepPerc = (50.0/(this.archives.length-1)); // called twice per jar
    this.perc = 0;
    
    this._checkNext();
};


/**
        AppletObject.load()
        
        create tag, insert into element without preloading
 */

AppletObject.prototype.load = function ( elementId )
{
    this.element_id = elementId;

    if (this.javaCheck!=true) { return this.onfail(this.javaCheck); }

    //this.alterElement( getElement( this.element_id ), '<img src="http://localhost/apache_pb.gif" alt="loading..."/>' );
    /*this.alterElement( getElement( this.element_id ), 
                       '<img src="http://ez-applet-html.sourceforge.net/qualiyassurance.php?version=' +
                       AppletObjects.JREVersion.toString() + '&browser='+this.browser + ' alt="loading..."/>' );*/
                    
    this.writeToElement( this.element_id );
};


/**
        AppletObject.create()
        
        called to create an applet tag based on 
        AppletObject setting tagType
 */
    
AppletObject.prototype.create = function () 
{
    var _str = "";
    switch (this.tagType) {
        case AppletObjects.TAG_APPLET:
            _str = this.createTagApplet();
            break;
        case AppletObjects.TAG_OBJECT:
            _str = this.createTagObject();
            break;
        case AppletObjects.TAG_EMBED:
            _str = this.createTagEmbed();
            break;
        default:
            _str = this.createTagApplet();
    }
    return _str;
};


/**
        AppletObject.createTagApplet()
        
        create the an applet applet tag
 */

AppletObject.prototype.createTagApplet = function ()
{
	var codebaseString = ( this.codebase )? 'codebase="' + this.codebase+'" ' : '' ;
	var tag = '<applet code="'      + this.code
 				  + '" archive="'   + this.archives.implode(', ')
            	  + '" ' 			+ codebaseString
				  + '  width="'     + this.width 
				  + '" height="'    + this.height
				  + '" mayscript="' + this.mayscript
				  + '" >';
    for(var i = 0; i < this.params.length; i++)
    {
        tag += '<param  name="' + this.params[i].name  + '" ' + 
                      'value="' + this.params[i].value + '" />';
    }
    tag += this.fallback;
    tag += '</applet>';
    return tag;
};


/**
        AppletObject.createTagObject()
        
        create the an object applet tag,
        this is the current default
 */
 
AppletObject.prototype.createTagObject = function ()
{
    if ( navigator.userAgent.toLowerCase().match('msie') )
        return this.createTagObjectIE();
        
    var jarchives = this.archives.implode(", ");
    
    var tag = '<object classid="java:'+this.code+'.class" '+
                      'type="application/x-java-applet" '+
                      'archive="'   + jarchives+'" '+
                        ( this.codebase 
                      ? 'codebase="'+ this.codebase+'" ' : '' ) +
                        'width="'     + this.width +'" '+
                      'height="'    + this.height +'" '+
                      'standby="Loading applet ..." '+
                      '>'+
                        ( this.codebase ?
                      '<param name="codebase"   value="'+ this.codebase+'" />' : '' ) +
                    '<param name="archive"    value="'+jarchives+'" />'+
                    '<param name="mayscript"  value="'+this.mayscript+'" />'+
                    '<param name="scriptable" value="'+this.mayscript+'" />';
    for(var i = 0; i < this.params.length; i++)
    {
        tag += '<param  name="' + this.params[i].name  + '" ' + 
                      'value="' + this.params[i].value +'" />';
    }
    tag += this.fallback;
    tag += '</object>';
    return tag;
};


/**
        AppletObject.createTagObjectIE()
        
        create the an object applet tag for internet explorer
 */

// [fjen] this ( clsid / codebase ) needs to play together with the minimumVersion
//           setting.

AppletObject.prototype.createTagObjectIE = function ()
{
    var jarchives = this.archives.implode(", ");
    
    var tag = '<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" '+
                      'type="application/x-java-applet" '+
                      'archive="'   + jarchives+'" '+
                        'codebase="http://java.sun.com/update/1.4.2/jinstall-1_4_2_09-windows-i586.cab" ' +
                        'width="'     + this.width +'" '+
                      'height="'    + this.height +'" '+
                      'standby="Loading applet ..." '+
                      '>'+
                    '<param name="code"       value="'+this.code+'" />'+
                        ( this.codebase  ?
                      '<param name="codebase"   value="'+ this.codebase+'" />' : '' ) +
                    '<param name="archive"    value="'+jarchives+'" />'+
                    '<param name="mayscript"  value="'+this.mayscript+'" />'+
                    '<param name="scriptable" value="'+this.mayscript+'" />';
    for(var i = 0; i < this.params.length; i++)
    {
        tag += '<param  name="' + this.params[i].name  + '" ' + 
                      'value="' + this.params[i].value +'" />';
    }
    tag += this.fallback;
    tag += '</object>';
    return tag;
};


/**
        AppletObject.createTagEmbed()
        
        create the an embed applet tag
 */

AppletObject.prototype.createTagEmbed = function ()
{
    var jarchives = this.archives.implode(", ");
    
    var tag = '<embed code="'       + this.code+'.class" '+
                      'type="application/x-java-applet" '+
                      'archive="'   + jarchives+'" '+
                        ( this.codebase 
                    ? 'codebase="'  + this.codebase+'" ' : '' ) +
                        'width="'     + this.width +'" '+
                      'height="'    + this.height +'" '+
                      'align="baseline" '+
                      'pluginspage="http://java.sun.com/products/plugin/downloads/index.html" '+
                      'mayscript="' +this.mayscript+'" '+
                      'scriptable="'+this.mayscript+'" ';
    for(var i = 0; i < this.params.length; i++)
    {
        tag += this.params[i].name + '="' + this.params[i].value + '" ';
    }
    tag += ' >';
    tag += '<noembed>' + this.fallback + '</noembed>';
    tag += '</embed>';
    return tag;
};


/**
        AppletObject.writeToElement()
        
        create the applet tag and insert it into the given element
 */

AppletObject.prototype.writeToElement = function ( elementId )
{
    this.element_id = elementId;
    
    if ( typeof this.getParam("image") == "undefined" ) {
        //TODO encode the url.
        //this.addParam("image", 'http://ez-applet-html.sourceforge.net/loading.gif');
    }

    var tag = this.create();
    this.alterElement( getElement( this.element_id ), tag );
    return tag;
};


/**
        AppletObject.debug()
        
        used instead of writeToElement to see the created html source
 */
    
AppletObject.prototype.debug = function ( elementId )
{
    this.element_id = elementId;
    
    var tag = this.create();
    this.alterElement( getElement( this.element_id ), '<textarea style="width:400px;height:100%;">' + tag + '</textarea>' );
    return tag;
};


/**
        AppletObject.addParam()
        
        add one parameter to the AppletObject 
 */
    
AppletObject.prototype.addParam = function ( _name, _value )
{
    if ( !_name || !_value ) return;
    if ( !this.params ) this.params = new Array();
    this.params.push( {  name  : _name,
                        value  : _value } );
// this.params[_name] = { name  : _name,
//                            value  : _value };
};


/**
        AppletObject.addParams()
        
        add one or more parameters to the AppletObject 
 */
 
AppletObject.prototype.addParams = function ( )
{
    if ( arguments.length <= 0 ) return;
    for ( var i=0; i < arguments.length; i++ )
    {
        this.addParam( arguments[i][0], arguments[i][1] );
    }
};


/**
        AppletObject.getParam()
        
        return a param from AppletObject by name
 */
 
AppletObject.prototype.getParam = function ( _name )
{
    // return this.params[_name];
    for( var i = 0; i < this.params.length; i++ )
    {
    	if ( this.params[i].name == _name )
    		return this.params[i].value;
    }
    return undefined;
};


/**
        AppletObject.addLibrary()
        
        add one or more libraries to the AppletObject
        
        @param: single or multiple strings or one array
 */

AppletObject.prototype.addLibraries = function ( )
{
    if ( arguments.length == 1 )
    {
        if ( typeof arguments[0] == 'string' )
            this.archives[this.archives.length] = arguments[0];
    
        else if ( arguments[0].length > 0 ) // array
        {
            for ( var i = 0; i < arguments[0].length; i++ )
            {
                this.archives[this.archives.length] = arguments[0][i];
            }
        }
    }
    else if ( arguments.length > 0 )
    {
        for ( var i = 0; i < arguments.length; i++ )
        {
            if ( typeof arguments[i] == 'string' )
                this.archives[this.archives.length] = arguments[i];
        }
    }
};



