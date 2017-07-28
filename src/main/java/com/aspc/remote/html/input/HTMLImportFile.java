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
package com.aspc.remote.html.input;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLComponent;
import com.aspc.remote.html.HTMLForm;
import com.aspc.remote.html.internal.HTMLFormComponent;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;

/**
 * .html.nput
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       24 May 1998
 */
public class HTMLImportFile extends HTMLFormComponent
{
    /**
     *
     * @param name
     */
    public HTMLImportFile(String name)
    {
        iSetName(name);
        size = 20;
        appendClassName(STYLE_STS_FIELD);
    }

    /**
     *
     * @param name
     * @param value the value
     */
    public HTMLImportFile(String name,String value)
    {
        this( name);
        setValue( value);
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
     *
     * @param call
     */
    @Override
    public void addOnChangeEvent( String call)
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
        iAddEvent( new InternalEvent( "onClick", call), script);
    }

    /**
     *
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param value the value
     */
    @Override
    public final void setValue( String value)
    {
        this.value = value;
    }

    /**
     *
     * @return the value
     */
    @Override
    public String getValue( )
    {
        if( value == null) return "";

        return value;
    }

    /**
     *
     * @param size
     */
    public void setSize( int size)
    {
        this.size = size;
    }

    /**
     *
     * @param browser
     */
    @Override
    protected void compile(ClientBrowser browser)
    {
        /**
         * readonly & disabled seem only to work with IE4 & above
         * I'll add a script for Netscape.
         */

        if( readOnlyFg == true || disabledFg == true || disableManualEntry == true)
        {
            setStyleProperty("background-color",makeColorID(new Color( 225,225,225)));
            setStyleProperty("color", makeColorID(new Color( 125,125,125)));
        }

        if( readOnlyFg == true || disabledFg == true)
        {
            HTMLEvent e = new HTMLEvent( "onFocus", "javascript:this.blur()");
            iAddEvent( e, "");
        }

        if( disableManualEntry == true)
        {
            HTMLEvent e = new HTMLEvent( "onkeydown", "javascript:this.blur()");
            iAddEvent( e, "");
        }


        super.compile( browser);
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( ClientBrowser browser, StringBuilder buffer)
    {
        buffer.append(
            "<INPUT"
        );

        if( StringUtilities.isBlank(value) == false)
        {
            buffer.append( " VALUE=\"");
            buffer.append( StringUtilities.encodeHTML(value));
            buffer.append( "\"");
        }

        buffer.append( " TYPE=FILE");

        /**
         * readonly & disabled seem only to work with IE4 & above
         */
        if( readOnlyFg == true)
        {
            buffer.append( " READONLY");
        }

        if( disabledFg == true)
        {
            buffer.append( " DISABLED");
        }

        buffer.append(" SIZE=").append( size);

        iGenerateAttributes(browser, buffer);

        buffer.append( ">");
    }

    /**
     * Disables this input.
     * @param flag
     */
    public void setDisabled( boolean flag)
    {
        disabledFg = flag;
    }

    /**
     * Sets the field to READONLY
     * @param flag
     */
    public void setReadOnly( boolean flag)
    {
        readOnlyFg = flag;
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
     * Disables tha manual entry
     * @param flag The Boolean vlaue to set the disable flag
     */
    public void setDisableManualEntry( boolean flag)
    {
        disableManualEntry = flag;
    }

    /**
     * Set the parent of this component internally called by
     * addComponent
     * @param orgParent
     */
    @Override
    protected void setParent( HTMLComponent orgParent)
    {
        super.setParent(orgParent);

       HTMLComponent tmpParent = getParent();

        while( tmpParent != null)
        {
            if( tmpParent instanceof HTMLForm)
            {
                HTMLForm form = (HTMLForm)tmpParent;

                form.setHandlesFiles( true);
                break;
            }

            tmpParent = tmpParent.getParent();
        }
    }


    /**
     * the size
     */
    protected int       size;


    /**
     * read only
     */
    protected boolean   readOnlyFg;
    /**
     * disable manual
     */
    protected boolean   disableManualEntry;
    /** disable */
    protected boolean   disabledFg;

    /**
     * the value
     */
    protected String    value;
}
