/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.html;
import java.util.Enumeration;
import java.util.Properties;

/**
 *  HTMLApplet
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 5:29 PM
 */
public class HTMLApplet extends HTMLComponent
{
    /**
     *
     * @param code
     */
    public HTMLApplet(String code)
    {
        this.code = code;
        params = new Properties();
    }

    /**
     *
     * @param code
     * @param codeBase
     */
    public HTMLApplet(String code, String codeBase)
    {
        this(code);

        this.codeBase = codeBase;
    }

    /**
     *
     * @param code
     * @param codeBase
     * @param archive
     */
    public HTMLApplet(String code, String codeBase, String archive)
    {
        this(code, codeBase);
        this.archive = archive;

    }

    /**
     *
     * @param archive
     */
    public void setArchive( String archive)
    {
        this.archive = archive;
    }

    /**
     *
     * @param ID
     */
    public void setID( String ID)
    {
        this.id = ID;
    }

    /**
     *
     * @param name
     */
    public void setName( String name)
    {
        this.name = name;
    }

    /**
     *
     * @param onError
     */
    public void setOnError( String onError)
    {
        this.onError = onError;
    }

    /**
     * Should we allow scripting in this applet
     * @param isAllowed true if allowed.
     */
    public void setAllowScripting( boolean isAllowed)
    {
        allowScripting=isAllowed;
    }

    /**
     * Should we use the standard APPLET tag ?
     * @param isStandard true if we should use the APPLET tag.
     */
    public void setStandard( boolean isStandard)
    {
        this.isStandard = isStandard;
    }

    /**
     *
     * @param removeAlt
     */
    public void setRemoveAlt( boolean removeAlt)
    {
        this.removeAlt = removeAlt;
    }

    /**
     *
     * @param alt
     */
    public void setAlternative( HTMLComponent alt)
    {
        this.alt = alt;
    }

    /**
     *
     * @param width
     */
    public void setWidth( String width)
    {
        this.width = width;
    }

    /**
     *
     * @param height
     */
    public void setHeight( String height)
    {
        this.height = height;
    }

    /**
     *
     * @param name
     * @param value the value
     */
    public void addParameter( String name, String value)
    {
        params.put(name, value);
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        if( browser.knownNotToSupportJavaApplets()) return;

        if( browser.isBrowserIE() && !isStandard)
        {
            generateIE( buffer);
        }
        else
        {
            generateStandard( browser, buffer);
        }
    }

    /**
     * Here is how you would rewrite your HTML for Internet Explorer:                                               <BR/>
     *                                                                                                              <BR/>
     *  &lt;object                                                                                                  <BR/>
     *         classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"                                                 <BR/>
     *         codebase="http://java.sun.com/products/plugin/1.1/jinstall-11-win32.cab#Version=1,3,0,0"             <BR/>
     *         width=50 height=50&gt;                                                                               <BR/>
     *      &lt;param name="code" value="AppletThatRequiresPlugin.class"&gt;                                        <BR/>
     *      &lt;param name="codebase" value="./classFiles/"&gt;                                                     <BR/>
     *      &lt;param name="animal" value="fish"&gt;                                                                <BR/>
     *  &lt;/object&gt;                                                                                             <BR/>
     *                                                                                                              <BR/>
     *  The classid attribute looks slightly alarming, but Microsoft isn't trying to drive you insane
     *  (this time). This long string (8AD9C840-044E-11D1-B3E9-00805F499D93) is the unique identification
     *  for the ActiveX control more commonly known as the Java Plug-in. The good news is that you just
     *  need to cut and paste it into your HTML file.                                                               <BR/>
     *                                                                                                              <BR/>
     *  Next we have the codebase attribute, which is different from the codebase attribute in your
     *  original HTML with the <applet> tag. This codebase attribute specifies the location of the Java
     *  Plug-in JRE (Java Runtime Environment). If the JRE isn't already on the user's computer, it will
     *  be downloaded. Like the classid attribute, you can cut and paste this value into your HTML file.            <BR/>
     *                                                                                                              <BR/>
     *  Notice that code and codebase (the original codebase) are now parameters, instead of attributes
     *  of the <object> tag.
     *
     *  <H3>Rewrite of the OBJECT tag by an external script to get around the patch KB912945</H3>
     */
    private void generateIE( final StringBuilder buffer)
    {
        buffer.append( "\n<script type=\"text/javascript\" src=\"/scripts/inactivex.js\"></script>\n" );
        buffer.append( "\n<script type=\"text/javascript\">\n" );
        buffer.append( "doApplet( '<OBJECT ");
        buffer.append( "   classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" ");
        //buffer.append( "   codebase=\"https://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=1,4,2,mn\" ");

        if( id != null)
        {
            buffer.append("   id=\"").append(id).append( "\"");
        }

        if( name != null)
        {
            buffer.append("   name=\"").append(name).append( "\"");
        }

        if( height != null)
        {
            buffer.append("   height=\"").append(height).append( "\"");
        }

        if( width != null)
        {
            buffer.append("   width=\"").append(width).append( "\"");
        }
        buffer.append( " > ");

        if( allowScripting)
        {
            buffer.append( "    <PARAM NAME=\"MAYSCRIPT\" VALUE=\"true\"> ");
        }

        buffer.append( "    <PARAM NAME=\"code\" ");
        buffer.append("VALUE=\"").append(code).append( "\"> ");


        if( codeBase != null)
        {
            buffer.append("    <PARAM NAME=\"codebase\" VALUE=\"").append(codeBase).append( "\"> ");
        }

        if( archive != null)
        {
            buffer.append("    <PARAM NAME=\"archive\" VALUE=\"").append(archive).append( "\"> ");
        }
        buffer.append( "    <PARAM NAME=\"type\" VALUE=\"application/x-java-applet;jpi-version=1.4.2\"> ");
        Enumeration keys;

        keys = params.keys();

        while( keys.hasMoreElements())
        {
            String key;

            key = (String)keys.nextElement();

            buffer.append("    <PARAM NAME=\"").append(key).append("\" VALUE=\"").append(params.getProperty(key,"")).append(
                "\"> ");
        }

        /*if( alt == null)
        {
            makeDefaultAlt();
        }

        alt.setParent(parent);
        alt.iGenerate( browser, buffer);
         */
        buffer.append( "</OBJECT> ' );");
        buffer.append( "\n</script>\n" );
    }

    private void generateStandard(final ClientBrowser browser, final StringBuilder buffer)
    {
        // To check wheather java enabled or not on machine as well as on browser
        buffer.append( "<SCRIPT type=\"text/javascript\"> try { java.lang.String.valueOf( true ); " );
        buffer.append( " document.write(' " );
        buffer.append("<APPLET code=\"").append(code).append( "\"");

        if( height != null)
        {
            buffer.append(" height=\"").append(height).append( "\"");
        }

        if( width != null)
        {
            buffer.append(" width=\"").append(width).append( "\"");
        }

        if( codeBase != null)
        {
            buffer.append(" codebase=\"").append(codeBase).append( "\"");
        }

        if( archive != null)
        {
            buffer.append(" archive=\"").append(archive).append( "\"");
        }

        if( id != null)
        {
            buffer.append(" id=\"").append(id).append( "\"");
        }

        if( name != null)
        {
            buffer.append(" name=\"").append(name).append( "\"");
        }

        if( onError != null)
        {
            buffer.append(" onError=\"").append(onError).append( "\"");
        }

        if( allowScripting)
        {
            buffer.append( " MAYSCRIPT");
        }

        buffer.append( ">");

        Enumeration keys;

        keys = params.keys();

        while( keys.hasMoreElements())
        {
            String key;

            key = (String)keys.nextElement();

            buffer.append("    <param name=\"").append(key).append("\" value=\"").append(params.getProperty(key,"")).append(
                "\">");
        }

        if(!removeAlt)
        {
            if( alt == null)
            {
                makeDefaultAlt();
            }

            alt.setParent(parent);
            alt.iGenerate( browser, buffer);
        }
        buffer.append( "<\\/APPLET>");
        buffer.append("' );} catch( e ) { } </SCRIPT>\n");
    }

    private void makeDefaultAlt()
    {
        HTMLContainer c;

        c = new HTMLContainer();

        HTMLText text = new HTMLText( "Java is not available\n");
        text.setFontSize(10);
        text.setBold(true);
        c.addComponent(text);

        text = new HTMLText( "Either your browser doesn't support Java or the feature has been turned off.\n");
        text.setFontSize(10);
        c.addComponent(text);

        text = new HTMLText( "If you are using a browser that is not capable of running Java, you can download a FREE Java enabled browser from :-\n");
        text.setFontSize(10);
        c.addComponent(text);
        HTMLAnchor a1 = new HTMLAnchor("http://www.mozilla.org/");
        text = new HTMLText("http://www.mozilla.org/");
        text.setFontSize(10);
        a1.addComponent(text);
        c.addComponent(a1);

        text = new HTMLText( "\n you are running a Java enabled browser but have disabled to java virtual machine you can turn it back on by :-\n");
        text.setFontSize(10);
        c.addComponent(text);

        text = new HTMLText( "For Netscape\n1) Choose \"Preferences\" from the \"Edit\" menu\n2) Click on \"Advanced\"\n3) Select  \"Enable Java\"\n");
        text.setFontSize(10);
        c.addComponent(text);
        text = new HTMLText( "For Internet Explorer\n1) Choose \"Internet Options\" from the \"View\" menu\n2) Click on \"Security\"\n3) Select \"Medium\"\n");
        text.setFontSize(10);
        c.addComponent(text);

        alt = c;
    }

    private String  height,
                    width;

    private HTMLComponent alt;
    private Properties params;
    private String  code,
                    codeBase,
                    archive,
                    id,
                    name,
                    onError;
    private boolean isStandard,
                    allowScripting;
    private boolean removeAlt;

}
