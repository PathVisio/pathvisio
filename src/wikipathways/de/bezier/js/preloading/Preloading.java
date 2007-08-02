/*
 *	this file is part of appletobject.js,
 *	please see:
 *	http://appletobject.org/
 *
 *  ----------------------------------------------------------------------------
 *
 *	implements callbacks for preloading applets via javascript
 *
 *  ----------------------------------------------------------------------------
 *
 *  changed: 2007-05-14 15:48:49 - fjenett
 *
 */

package de.bezier.js.preloading;

// http://www.rgagnon.com/javadetails/java-0240.html

public class Preloading extends java.applet.Applet
{
	public void init () {
		callback( "inited" );
	}

	public void start () {
		callback( "started" );
	}

	public void callback ( String _what )
	{
		// AppletObject id
		//
		String aobj = getParameter("AObject");

		// AppletObjects.started( id )
		//
		String jscmd = "AppletObjects." + _what + "(" +
  						( aobj != null ? aobj : "-1" ) + ")";

  		// first try using JSObject via reflection
  		//
		try
		{
   			Class c = Class.forName("netscape.javascript.JSObject");

   			java.lang.reflect.Method getWin =
   						c.getMethod("getWindow",new Class[]{java.applet.Applet.class});

   			java.lang.reflect.Method eval =
   						c.getMethod("eval",new Class[]{String.class});

   			Object jswin = getWin.invoke(c, new Object[]{this});

  			if ( jswin != null )
	   			eval.invoke(jswin, new Object[]{ jscmd }).toString();

			return;
  		}
  		catch (Exception e) {;}


		// failed? ... then let's try via javascript: - protocol
		//
		try
		{
			java.applet.AppletContext context = getAppletContext();
			if ( context != null ) {
  				context.showDocument(
  									new java.net.URL("javascript:" + jscmd)
  									,"_self"
  									       );
  			}
  		}
  		catch (Exception e) {;}
	}

	// sadly callbacks don't work inside these ...
	//
	public void stop () { }
	public void destroy () { }
}
