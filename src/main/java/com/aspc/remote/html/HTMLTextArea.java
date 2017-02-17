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

import com.aspc.remote.html.internal.HTMLFormComponent;
import com.aspc.remote.html.internal.HTMLReadOnlyToggle;
import com.aspc.remote.html.internal.HandlesSingleClick;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.util.misc.StringUtilities;

/**
 *  HTMLTextArea
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 * @since 29 September 2006
 *  @since
 */
public class HTMLTextArea extends HTMLFormComponent implements HTMLReadOnlyToggle, HandlesSingleClick
{
    private String placeHolder;
    
    /**
     *
     * @param name
     * @param rows
     * @param cols
     */
    public HTMLTextArea(
        String name,
        int rows,
        int cols
    )
    {
        iSetName(name);
        this.rows = rows;
        this.cols = cols;
        appendClassName(STYLE_STS_FIELD);
    }

    /**
     * set the placeholder string
     * @param plSring the place holder string
     */
    public void setPlaceHolder( final String plSring)
    {
        placeHolder=plSring;
    }

    /**
     * get the placeholder string
     * @return the place holder string
     */
    public String getPlaceHolder()
    {
        return placeHolder;
    }
    
    /**
     *
     * @return the value
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Prevent the click of the mouse from propagation up the dom and kicking off other things.
     */
    @Override
    public void cancelClickBubble()
    {
        cancelBubble = true;
        touch();
    }

    /**
     *
     * @param call
     */
    @Override
    public void addOnChangeEvent( final String call)
    {
        addOnChangeEvent( call, "");
    }

    /**
     *
     * @param call
     * @param script
     */
    @Override
    public void addOnChangeEvent( String call, String script)
    {
        iAddEvent( new InternalEvent( "onChange", call), script);
    }

    /**
     * Sets the field to READONLY
     * @param flag
     */
    @Override
    public void setReadOnly( boolean flag)
    {
        readOnlyFg = flag;
    }

    /**
     * Disables this input.
     * @param flag
     */
    @Override
    public void setDisabled(boolean flag)
    {
        setReadOnly( flag);
        disabledFg=flag;
    }

   /**
     * Is this is Disabled
     * @return TRUE if disabled
     */
    @Override
    public boolean isDisabled()
    {
        return disabledFg;
    }

    /**
     * Is this is ReadOnly
     * @return TRUE if readonly
     */
    @Override
    public boolean isReadOnly()
    {
        return readOnlyFg;
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
        buffer.append("\n<textarea rows=")
            .append(rows)
            .append(" cols=")
            .append(cols);

        if( disabledFg)
        {
            buffer.append( " disabled");
        }

        if( readOnlyFg == true)
        {
            buffer.append( " readonly");
        }
        
        if( StringUtilities.notBlank(placeHolder ))
        {
            buffer.append(" placeholder=\"").append(StringUtilities.encodeHTML(placeHolder.replace("\r", "").replace("\n", " \n"))).append("\"");
        }
        
        iGenerateAttributes(browser, buffer);

        buffer.append( ">");

        if( text != null)
        {
            String temp;
            temp = text.replace( "&", "&amp;");
            temp = temp.replace( "<", "&lt;");
            temp = temp.replace( ">", "&gt;");
            
            if(temp.length()>1)
            {
                char c = temp.charAt(0);
                if( Character.isWhitespace(c))
                {
                    temp = "&#" +(int)c + ";" + temp.substring(1);
                }
            }

            buffer.append(temp);
        }

        buffer.append("</textarea>\n");
    }

    /**
     *
     * @return the value
     */
    @Override
    public String getValue()
    {
        return text;
    }

    /**
     *
     * @return the value
     */
    public int getRows()
    {
        return rows;
    }

    /**
     *
     * @param value the value
     */
    @Override
    public void setValue(final String value)
    {
        assert StringUtilities.checkXML(value): "not valid XML value (encoded): " + StringUtilities.encode(value);
        
        text = value;
    }

    private final int   cols,//NOPMD
                        rows;//NOPMD

    private String      text;

    /**
     *
     */
    protected boolean     readOnlyFg,
                          disabledFg;
}
