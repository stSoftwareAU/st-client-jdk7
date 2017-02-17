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
import com.aspc.remote.html.internal.HandlesMouseEvents;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  HTMLDiv
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       18 February 1998
 */
public class HTMLDiv extends HTMLContainer implements HandlesMouseEvents
{
    /**
     *
     */
    public static final String SCROLLSYNC_METHOD_NONE = "none";
    /**
     *
     */
    public static final String SCROLLSYNC_METHOD_VERTICAL = "vertical";
    /**
     *
     */
    public static final String SCROLLSYNC_METHOD_HORIZONTAL = "horizontal";
    /**
     *
     */
    public static final String SCROLLSYNC_METHOD_BOTH = "both";

    private final boolean allowMixed;

    private String type;
    
    /**
     * create a division
     *
     * @param id The identifier for the division
     * @param allowMixed allow mixed cause IDs
     */
    public HTMLDiv(final String id, final boolean allowMixed)
    {
        this.allowMixed = allowMixed;
        setId( id);
    }

    /**
     * create a division
     *
     * @param id The identifier for the division
     */
    public HTMLDiv(final String id)
    {
  //      if( id.equalsIgnoreCase("DIV_PRINTBTN"))
  //      {
  //          System.out.print("abc");
      //  }
        this.allowMixed = false;
        setId( id);
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
     * set the type
     *
     * @param type the type
     */
    public final void setType( final String type)
    {
        this.type=type;
    }
    
    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     */
    public final void setId( final String id)
    {
        iSetId(id);
    }

    @Override
    protected final void iSetId( final String id)
    {
        assert id != null && (id.length() == 0 || id.matches("^[A-Za-z][A-Za-z0-9_:\\.-]*")) : "invalid div id '" + id + "'";
               
        if( allowMixed)
        {
            this.id=id;
        }
        else
        {
            super.iSetId(id);
        }
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

    /**
     *
     * @param image
     */
    public void setBackgroundImage( String image)
    {
        setStyleProperty( "background-image", "url('" + image + "')");
    }

    /**
     *
     * @param type the type
     * @param color
     */
    public void setColour(
        String type,
        Color color
    )
    {
        if( !HTMLStyleSheet.BACKGROUND_COLOUR.equals(type))
        {
            return;
        }

        int c;
        c = color.getRGB() & 0xffffff;

        String  t;

        t = "000000" + Integer.toHexString(c);

        t = "#" + t.substring(t.length() - 6);

        setStyleProperty( type, t);
    }

    /**
     *
     * @param index
     */
    public void setZ( int index)
    {
        setStyleProperty("z-index", "" + index);
    }

    /**
     *
     * @param type the type
     */
    public void setPosition( String type)
    {
        setStyleProperty( "position", type);
    }

    /**
     *
     * @param visible
     */
    public void setVisible( boolean visible)
    {
        if( visible == true)
        {
            setStyleProperty("visibility", null);
        }
        else
        {
            setStyleProperty( "visibility", "hidden");
        }
    }

    /**
     *
     * @param v the value
     */
    public void setLeft( int v)
    {
        setPosition(HTMLStyleSheet.POSITION_ABSOLUTE);
        setStyleProperty( "left", v + "px");
    }

    /**
     *
     * @param v the value
     */
    public void setTop( int v)
    {
        setPosition(HTMLStyleSheet.POSITION_ABSOLUTE);
        setStyleProperty( "top", v + "px");
    }

    /**
     *
     * @param w
     */
    public void setBorderWidth( int w)
    {
        switch( w)
        {
            case 0:
                setStyleProperty( HTMLStyleSheet.STYLE_BORDER_WIDTH, HTMLStyleSheet.BORDER_WIDTH_NONE);
                break;
            case 1:
                setStyleProperty( HTMLStyleSheet.STYLE_BORDER_WIDTH, HTMLStyleSheet.BORDER_WIDTH_THIN);
                break;
            default:
                setStyleProperty( HTMLStyleSheet.STYLE_BORDER_WIDTH, "" + w);
        }
    }

    /**
     *
     * @param v the value
     */
    public void setRight( int v)
    {
        setStyleProperty( "right", "" + v);
    }

    /**
     * do not wrap this division
     * @param flag true to not wrap.
     */
    public void setNoWrap( final boolean flag)
    {
        if( flag)
        {
            setStyleProperty(HTMLStyleSheet.STYLE_WHITE_SPACE, HTMLStyleSheet.WHITE_SPACE_NOWRAP);
        }
        else
        {
            setStyleProperty(HTMLStyleSheet.STYLE_WHITE_SPACE, "");
        }
    }

    /**
     *
     * @param v the value
     */
    public void setBottom( int v)
    {
        setStyleProperty( "bottom", "" + v);
    }

    /**
     *
     * @param v the value
     */
    public void setWidth( String v)
    {
        setStyleProperty( "width", v);
    }

    /**
     * the max width
     * @param w width
     */
    public void setMaxWidth( String w)
    {
        setStyleProperty( "max-width", w);
    }

    /**
     *
     * @param v the value
     */
    public void setHeight( String v)
    {
        setStyleProperty( "height", v);
    }

    /**
     *
     * @param v the value
     */
    public void setOverflow( String v)
    {
        setStyleProperty( "overflow", v);
    }

    /**
     *
     * @param v the value
     */
    public void setOverflowY( String v)
    {
        setStyleProperty( "overflow-y", v);
    }

    /**
     *
     * @param v the value
     */
    public void setOverflowX( String v)
    {
        setStyleProperty( "overflow-x", v);
    }

    /**
     *
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public void setClip( String top, String bottom, String left, String right)
    {
        setStyleProperty( "clip", "rect(" + top + " " + bottom + " " + left + " " + right + ")");
    }

    /**
     *
     * @param v the value
     */
    public void setCursor( String v)
    {
        setStyleProperty( "cursor", v);
    }

    /**
     *
     * @param displayMode
     */
    public void setDisplayMode(String displayMode)
    {
        setStyleProperty( "display", displayMode);
    }

    /**
     *
     * @param v the value
     */
    public void setTopMargin( String v)
    {
        setStyleProperty( "margin-top", v);
    }

    /**
     *
     * @param v the value
     */
    public void setLeftMargin( String v)
    {
        setStyleProperty( "margin-left", v);
    }

    /**
     *
     * @param v the value
     */
    public void setMarginHeight( String v)
    {
        setStyleProperty( "margin-height", v);
    }

    /**
     *
     * @param v the value
     */
    public void setMarginWidth( String v)
    {
        setStyleProperty( "margin-width", v);
    }

    /**
     *
     * @param borderColor
     */
    public void setBorderColor( String borderColor)
    {
        setStyleProperty( "border-color", borderColor);
    }

    /**
     *
     * @param borderStyle
     */
     public void setBorderStyle( String borderStyle)
    {
        setStyleProperty( "border-style",borderStyle);
    }

    /**
     *
     * @param borderBottomColor
     */
    public void setBorderBottomColor(String borderBottomColor)
    {

        setStyleProperty( "border-bottom-color", borderBottomColor);

    }

    /**
     *
     * @param borderBottomWidth
     */
    public void setBorderBottomWidth(int borderBottomWidth)
    {
        setStyleProperty( "border-bottom-width", "" + borderBottomWidth);
    }

    /**
     *
     * @param borderBottomStyle
     */
    public void setBorderBottomStyle(String borderBottomStyle)
    {
       setStyleProperty( "border-bottom-style", borderBottomStyle);
    }


    /**
     *
     * @param borderLeftColor
     */
    public void setBorderLeftColor(String borderLeftColor)
    {

        setStyleProperty( "border-left-color", borderLeftColor);

    }

    /**
     *
     * @param borderLeftWidth
     */
    public void setBorderLeftWidth(int borderLeftWidth)
    {
        setStyleProperty( "border-left-width", "" + borderLeftWidth);
    }

    /**
     *
     * @param borderLeftStyle
     */
    public void setBorderLeftStyle(String borderLeftStyle)
    {
       setStyleProperty( "border-left-style", borderLeftStyle);
    }

    /**
     *
     * @param borderRightColor
     */
    public void setBorderRightColor(String borderRightColor)
    {

        setStyleProperty( "border-right-color", borderRightColor);

    }

    /**
     *
     * @param borderRightWidth
     */
    public void setBorderRightWidth(int borderRightWidth)
    {
        setStyleProperty( "border-right-width", "" + borderRightWidth);
    }

    /**
     *
     * @param borderRightStyle
     */
    public void setBorderRightStyle(String borderRightStyle)
    {
       setStyleProperty( "border-right-style", borderRightStyle);
    }

    /**
     *
     * @param borderTopColor
     */
    public void setBorderTopColor(String borderTopColor)
    {

        setStyleProperty( "border-top-color", borderTopColor);

    }

    /**
     *
     * @param borderTopWidth
     */
    public void setBorderTopWidth(int borderTopWidth)
    {
        setStyleProperty( "border-top-width", "" + borderTopWidth);
    }

    /**
     *
     * @param borderTopStyle
     */
    public void setBorderTopStyle(String borderTopStyle)
    {
       setStyleProperty( "border-top-style", borderTopStyle);
    }

    /**
     *
     * @param backgroundColor
     */
    public void setBackgroundColor(final String backgroundColor)
    {
       setStyleProperty("background-color", backgroundColor );
    }


    /**
     *
     * @param divId
     * @param method
     */
    public void addSrollingSync( String divId, String method)
    {
        if( scrollingSyncTable == null)
        {
            scrollingSyncTable = new ConcurrentHashMap();
        }
        scrollingSyncTable.put( divId, method);
    }

    /**
     *
     * @param browser
     */
    @Override
    protected void compile( ClientBrowser browser)
    {

//        HTMLPage page = getParentPage();

        if( scrollingSyncTable != null)
        {
            HTMLEvent event;
            Enumeration en = scrollingSyncTable.keys();
            while( en.hasMoreElements())
            {
                String tmpID = (String)en.nextElement();
                String method = (String)scrollingSyncTable.get(tmpID);
                event = new HTMLMouseEvent( "onscroll", "javascript:divScrollSync('" + getId() + "','" + tmpID + "','" + method + "');");
                iAddEvent( event, null);
            }

            String script = "function divScrollSync( baseId, targetId, method)\n" +
                            "{\n" +
                            "  var divBase = findElement( baseId);\n" +
                            "  var divTarget = findElement( targetId);\n" +
                            "  if( divBase != null && divTarget != null)\n" +
                            "  {\n" +
                            "    if( method == 'both' || method == 'horizontal')\n" +
                            "    {\n" +
                            "       if( divTarget.style.overflow == '')\n" +
                            "       { \n" +
                            "           divTarget.style.left = -divBase.scrollLeft + 'px';\n" +
                            "       }\n" +
                            "       else\n" +
                            "       { \n" +
                            "          divTarget.scrollLeft = divBase.scrollLeft;\n" +
                            "       }\n" +
                            "    }\n" +
                            "    if( method == 'both' || method == 'vertical')\n" +
                            "    {\n" +
                            "       if( divTarget.style.overflow == null)\n" +
                            "       { \n" +
                            "           divTarget.style.top = -divBase.scrollTop + 'px';\n" +
                            "       }\n" +
                            "       else\n" +
                            "       { \n" +
                            "          divTarget.scrollTop = divBase.scrollTop;\n" +
                            "       }\n" +
                            "    }\n" +
                            "  }\n" +
                            "}\n";
           addJavaScript("DIVSCROLLSYNC", script);
        }
        super.compile( browser);
    }

    public void addDefaultHTML( final String defaultHTML)
    {
        this.defaultHTML=defaultHTML;
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
        int pos = buffer.length() -1;
        if( pos != -1)
        {
            char c=buffer.charAt(pos);
            if( c != '\n')
            {
                buffer.append("\n");
            }
        }

        String typeName="div";
        if( StringUtilities.notBlank(type))
        {
            typeName=type.toLowerCase();
        }
        buffer.append( "<").append(typeName);
        iGenerateAttributes(browser, buffer);

        StringBuilder tempBuffer=new StringBuilder();
        super.iGenerate(browser, tempBuffer);

        if( tempBuffer.length() == 0)
        {
            buffer.append( ">");
            buffer.append( defaultHTML !=null?defaultHTML:"");
            buffer.append( "</").append(typeName).append(">\n");
        }
        else
        {
            buffer.append( ">\n");
            int pos2 = tempBuffer.length() -1;
            if( pos2 != -1)
            {
                char c=tempBuffer.charAt(pos2);
                if( c != '\n')
                {
                    tempBuffer.append("\n");
                }
            }
            buffer.append( tempBuffer);

            buffer.append( "</").append(typeName).append(">\n");
        }
    }

    private ConcurrentHashMap scrollingSyncTable;
    private String defaultHTML;
}
