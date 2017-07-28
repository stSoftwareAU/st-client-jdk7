/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
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
import com.aspc.remote.html.internal.HandlesMouseEvents;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.GWTConstants;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.HashMap;

/**
 *  HTMLDiv
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Jason McGrath
 *  @since       14 December 2005
 */
public class HTMLIFrame extends HTMLContainer implements HandlesMouseEvents
{
    /**
     *
     * create a division
     * @param href the URL
     */
    public HTMLIFrame( String href)
    {
        setURL(href);
        borderWidth = 1;
    }

    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     */
    public final void setId( String id)
    {
        iSetId(id);
    }

    /**
     * Set the URL
     * @param href the URL
     */
    public final void setURL( final String href)
    {
        this.href = href;
    }

    /**
     * Add a mouse event to this component.
     *
     * @param me The mouse event
     */
    @Override
    public void addMouseEvent(HTMLMouseEvent me)
    {
        iAddEvent(me, "");
    }

    /** The width of the in line frame in pixels or as a percentage of available space
     *
     * @param width - width of in line frame
     */
    public void setWidth( final String width)
    {
        this.width = width ;
    }

    /**
     * The height of the in-line frame in pixels or as a percentage of available space
     *
     * @param h - height of in-line frame
     */
    public void setHeight( final String h)
    {
        this.height = h;
    }

    /** The width of the left and right margins within the in-line frame.
     *  The value must be greater than one pixel
     *
     * @param width - width of margin
     */
    public void setMarginWidth( final String width)
    {
        marginWidth = width;
    }

    /** The height of the top and bottom margins within the in-line frame.
     *  The value must be greater than one pixel
     *
     * @param height - height of margin
     */
    public void setMarginHeight( final String height)
    {
        marginHeight = height;
    }

    /** Sets the FRAMEBORDER attribute which specifies whether or not a border should be drawn.
     *  The default value of 1 results in a border while a value of 0 suppresses the border
     *
     * @param width - width of border
     */
    public void setBorderWidth( final int width)
    {
        borderWidth = width;
    }

    /**
     *
     * @param seconds
     */
    public void setAutoRefresh( final int seconds)
    {
        this.autoRefreshSeconds = seconds;
    }

    /** Specifies whether adjust the IFrame's size.
     *
     *@param _bAutoFit  -   true :  adjustIframeSize() will be called .
     */
    public void setAutoFit( final boolean _bAutoFit)
    {
        bAutoFit = _bAutoFit;
    }

    /** Specifies whether adjust the IFrame's size.
     *
     *@return boolean  -  true :  adjustIframeSize() will be called .
     */
    public boolean getAutoFit()
    {
        return bAutoFit;
    }

    /**
     * Specifies whether scrollbars are provided for the in-line frame.
     * The default value, auto, generates scrollbars only when necessary.
     * The value yes gives scrollbars at all times, and the value no suppresses
     * scrollbars--even when they are needed to see all the content.
     * The value no should never be used.
     *
     * @param v - auto/yes/no
     */
    public void setScrolling( final String v)
    {
        scrolling = v;
    }

    /**
     *
     */
    public void hideCounter()
    {
        hideCounterFg=true;
    }

    /**
     * compile the IFRAME
     * @param browser The browser to compile for
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        if( bAutoFit)
        {
            String   strScript = "function adjustIframeSize(theId) {\n"
                    + "  adjIframeSize(theId);\n"
                    + "}\n"
                    + "function adjIframeSize(theId) {\n" +
                "  try\n" +
                "  {\n" +
                "    var ifrm = findElement( theId);\n" +
                "    if( ifrm === null || ifrm === undefined) return;\n" +
                "    \n" +
                "    var cw = ifrm.contentWindow;\n" +
                "    \n" +
                "    if( cw === null || cw === undefined) return;\n" +
                "    \n" +
                "    var doc = cw.document;\n" +
                "    \n" +
                "    if( doc === null || doc === undefined) return;\n" +
                "    \n" +
                "    var b = doc.body;\n" +
                "    \n" +
                "    if( b === null || b === undefined) return;\n" +
                "    if( navigator.userAgent !== null)\n" +
                "    {\n" +
                "      if( navigator.userAgent.indexOf( 'Firefox' ) !== -1 || navigator.userAgent.indexOf( 'MSIE' ) !== -1)\n" +
                "      {\n" +
                "         ifrm.height = b.scrollHeight + 40;\n" +
                "      }\n" +
                "      else \n" +
                "      {\n" +
                "         ifrm.height = b.scrollHeight;\n" +
                "      }\n" +
                "    } else \n" +
                "    {\n" +
                "      ifrm.height = b.scrollHeight; \n" +
                "    }\n" +
                //"    ifrm.style.width = '100%'; \n" +
                //"    if( navigator.vendor != null)\n" +
                //"    {\n" +
                //"      if( navigator.vendor.indexOf( 'Apple' ) != -1 )\n" +
                //"      {\n" +
                //"         ifrm.width = '100%';\n" +
                //"      }\n" +
                //"    }\n" +
                "   }\n" +
                "   catch( e)\n" +
                "   {\n" +
                "   ;//\n" +
                "   }\n" +
                "}\n";

            addJavaScript("ADJUSTIFRAMESIZE", strScript);


        }

        String closeScript = "function closeIFrameCurtain(theId)\n" +
            "{\n" +
            " var curtain = findElement( theId);\n" +
            " if( curtain !== null)\n" +
            " {\n" +
            "    curtain.style.display = 'none';" +
            " }\n" +
            "}\n";

        addJavaScript("CLOSE_IFRM_CURTAIN", closeScript);

        if( autoRefreshSeconds > 0)
        {
            HTMLPage page = getParentPage();

            page.registerPostCompileCallBack( this);

            page.addGWT( "com.aspc.gwt.doublebuffer.Reload");

            HashMap map = (HashMap)page.getWorkingStorage( GWTConstants.DICTIONARY_RELOAD);

            if( map == null)
            {
                map = HashMapFactory.create();
                page.setWorkingStorage( GWTConstants.DICTIONARY_RELOAD, map);
            }

            //hide/show counter
            String temp = (String)map.get( GWTConstants.ELEMENT_COUNTER);
            if( temp == null)
            {
                temp = "";
            }
            else
            {
                temp += ",";
            }

            temp += hideCounterFg ? "HIDE":"SHOW";

            map.put( GWTConstants.ELEMENT_COUNTER, temp);

            // the reload target
            temp = (String)map.get( GWTConstants.ELEMENT_TARGET);
            if( temp == null)
            {
                temp = "";
            }
            else
            {
                temp += ",";
            }

            temp += getId();

            map.put( GWTConstants.ELEMENT_TARGET, temp);

            // the reload seconds
            temp = (String)map.get( GWTConstants.ELEMENT_SECONDS);
            if( temp == null)
            {
                temp = "";
            }
            else
            {
                temp += ",";
            }

            temp += autoRefreshSeconds;

            map.put( GWTConstants.ELEMENT_SECONDS, temp);
        }

        if ( StringUtilities.isBlank(height) == false)
        {
            setStyleProperty("height", height);
        }
        super.compile( browser);
    }

    private String makeEncodedURL()
    {
        if (StringUtilities.isBlank(href) == false)
        {
            //StringUtilities su = new StringUtilities();
            StringBuilder tempBuffer = new StringBuilder();

            int pos = href.indexOf( "?");
            if( pos != -1)
            {
                String url=href.substring(0, pos);
                tempBuffer.append( url ).append( "?" );
                if (href.contains("LAYERID="))
                {
                    int layer;
                    String layerSub=href.substring( href.indexOf( "LAYERID=")+8);
                    String layerId="";
                    int i=0;
                    while (i<layerSub.length() && Character.isDigit( layerSub.charAt( i ) ))
                    {
                        layerId += layerSub.charAt( i );
                        i++;
                    }
                    if (!StringUtilities.isBlank( layerId ))
                    {
                        layer=Integer.parseInt( layerId );
                        if (layer>0)
                        {
                            tempBuffer.append( "LAYERID=" ).append( layer ).append( "&amp;" );
                        }
                    }
                }

                tempBuffer.append( "ECMD=");
                tempBuffer.append(
                    StringUtilities.encodeUTF8base64(
                        href.substring( pos + 1)
                    )
                );
            }
            else
            {
                tempBuffer.append( href);
            }
            String tempURL = tempBuffer.toString();

            return tempURL;
        }

        return "";
    }

    /**
     * This is the post compile if needed
     *
     * @param browser
     */
    @Override
    protected void postCompile( final ClientBrowser browser)
    {
        super.postCompile( browser);

        HTMLPage page = getParentPage();
        HashMap map = (HashMap)page.getWorkingStorage( GWTConstants.DICTIONARY_RELOAD);

        if( map != null)
        {
            StringBuilder script = new StringBuilder();

            script.append("var ");
            script.append( GWTConstants.DICTIONARY_RELOAD);
            script.append( " = {\n");

            String counterList= (String)map.get( GWTConstants.ELEMENT_COUNTER);

            script.append( GWTConstants.ELEMENT_COUNTER);
            script.append( ": \"");
            script.append( counterList);
            script.append( "\",\n");

            String targetList= (String)map.get( GWTConstants.ELEMENT_TARGET);
            script.append( GWTConstants.ELEMENT_TARGET);
            script.append( ": \"");
            script.append( targetList);
            script.append( "\",\n");

            String secondsList= (String)map.get( GWTConstants.ELEMENT_SECONDS);

            script.append( GWTConstants.ELEMENT_SECONDS);
            script.append( ": \"");
            script.append( secondsList);
            script.append( "\"\n};");

            page.addJavaScript( script.toString());

            page.setWorkingStorage( GWTConstants.DICTIONARY_RELOAD, null);
        }
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
        String strId = this.getId();

        if(StringUtilities.isBlank(strId))
        {
            strId="BLANK";
        }
        String strDivId = "IFRMCURTAIN_" + strId.toUpperCase();

        if( browser.canHandleDHTML())
        {
            final String curtain = "\n<div id=\"" + strDivId + "\" style='top: 0px;width: 100%;height: 100%;left: 0px;position: relative;z-index: 100'>" +
                "\n<table BGCOLOR=\"#fafafa\" style=\"height:100%; border:0; width: 100%; \" >" +
                "<TR><TD ALIGN=center><SPAN STYLE=\"font-size: 14px;font-family: Arial;font-weight:bold\">Please&nbsp;wait&nbsp;loading...</SPAN>" +
                "</TD></TR></table></div>\n";
            buffer.append(curtain);
        }

        buffer.append( "<iframe");

        iGenerateAttributes(browser, buffer);

        String tempURL = makeEncodedURL();

        if (StringUtilities.isBlank(href) == false)
        {
            buffer.append( " src=\"");

            buffer.append( tempURL);

            buffer.append( "\"");
        }

        if (StringUtilities.isBlank(width) == false)
        {
            buffer.append( " width=\"");
            buffer.append( width);
            buffer.append( "\"");
        }

        if ( StringUtilities.isBlank(marginWidth) == false)
        {
            buffer.append( " MARGINWIDTH=\"");
            buffer.append( marginWidth);
            buffer.append( "\"");
        }

        String temp = marginHeight;
        if (StringUtilities.isBlank(temp ) && bAutoFit == true)
        {
            // Autofit stays at container height if no height specified
            // so set minimum so that it can expand
            temp = "20";
        }

        if ( StringUtilities.isBlank(temp) == false)
        {
            buffer.append( " MARGINHEIGHT=\"");
            buffer.append( temp);
            buffer.append( "\"");
        }

        if (borderWidth != 1)
        {
            buffer.append( " FRAMEBORDER=\"");
            buffer.append( borderWidth);
            buffer.append( "\"");
        }

        if (StringUtilities.isBlank(scrolling) == false )
        {
            buffer.append( " SCROLLING=\"");
            buffer.append( scrolling);
            buffer.append( "\"");
        }

        if( StringUtilities.isBlank(strId))
        {
            strId = getParentPage().doGenerateId();
        }

        if( browser.canHandleDHTML())
        {
            buffer.append("  onload=\"closeIFrameCurtain('").append(strDivId).append( "');");

            if (bAutoFit == true)
            {
                buffer.append("adjustIframeSize('").append(strId).append("');");
            }
            buffer.append("\"");
        }

        buffer.append( ">\n");

        super.iGenerate(browser, buffer);

        buffer.append( "\n</iframe>");
    }

    private String      href,
        width,
        marginWidth,
        marginHeight,
        height,
        scrolling;

    private int         borderWidth;

    private int         autoRefreshSeconds;

    private boolean     bAutoFit ;
    private boolean hideCounterFg;
}
