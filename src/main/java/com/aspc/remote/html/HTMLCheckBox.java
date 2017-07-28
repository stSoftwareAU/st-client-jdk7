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

import com.aspc.remote.html.internal.*;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.StringUtilities;

/**
 *  HTMLCheckBox
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       19 June 1998
 */
public class HTMLCheckBox extends HTMLFormComponent implements HandlesMouseEvents,HTMLReadOnlyToggle, HandlesSingleClick
{
    /**
     *
     * @param name
     */
    public HTMLCheckBox(final String name)
    {
        iSetName( name);
        appendClassName(STYLE_STS_FIELD);
    }

    /**
     *
     * @param name
     * @param checked
     */
    public HTMLCheckBox(String name, boolean checked)
    {
        this( name);
        this.checked = checked;
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
     * @param toolTip
     */
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param checked
     */
    public void setChecked( boolean checked)
    {
        this.checked = checked;
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
        iAddEvent( new InternalEvent( "onClick", call), script);
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
            "<INPUT TYPE=CHECKBOX"
        );

        if( checked == true)
        {
            buffer.append(
                " CHECKED"
            );
        }

        if( StringUtilities.isBlank( value) == false)
        {
            buffer.append( " VALUE='");
            buffer.append( value);
            buffer.append("'");
        }

        if( disabledFg )
        {
            setTabIndex( -1);
        }

        if( (readOnlyFg || disabledFg) && !forceWritable)
        {
           // buffer.append( " READONLY");
            buffer.append( " DISABLED");
        }

        iGenerateAttributes(browser, buffer);

        buffer.append( ">");

        if( readOnlyFg == false && disabledFg == false)
        {
            buffer.append("<INPUT TYPE=HIDDEN NAME=");
            buffer.append( name);
            buffer.append( "_CHECKED VALUE='");

            if( checked)
            {
                buffer.append( "Y");
            }
            else
            {
                buffer.append( "N");
            }

            buffer.append("'>");
        }
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
     * @return the value
     */
    @Override
    public String getValue()
    {
        if( value == null)
        {
            return "" + checked;
        }
        else
        {
            return value;
        }

    }

    /**
     *
     * @return the value
     */
    public boolean isChecked()
    {
        return checked;
    }

    /**
     *
     * @param value the value
     */
    @Override
    public void setValue( final String value)
    {
        String newVal = value;
        if(
            value.equalsIgnoreCase("true")  ||
            value.equalsIgnoreCase("Y")     ||
            value.equalsIgnoreCase("on")
        )
        {
            checked = true;
            newVal = null;
        }
        else if(
            StringUtilities.isBlank(value) ||
            value.equalsIgnoreCase("false")  ||
            value.equalsIgnoreCase("N")     ||
            value.equalsIgnoreCase("off")
        )
        {
            checked = false;
            newVal = null;
        }
        else
        {
            throw new IllegalArgumentException( getName() + " not a boolean value: " + StringUtilities.encode(value));
        }
        
        this.value = newVal;
    }

    /**
     * add a mouse event to this component
     *
     * @param me The mouse event
     */
    @Override
    public void addMouseEvent(HTMLMouseEvent me)
    {
        iAddEvent(me, "");
    }

    /**
     * Disables this input.
     * @param flag
     */
    @Override
    public void setDisabled(boolean flag)
    {
        disabledFg = flag;
    }

    /**
     * Sets the field to READONLY
     * @param flag
     */
    @Override
    public void setReadOnly(boolean flag)
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
     * Sets the field to writable state.
     * @param forceWritable
     */
    public void setWritable(boolean forceWritable)
    {
        this.forceWritable = forceWritable;
    }

    private boolean checked;
    private boolean disabledFg;
    private boolean readOnlyFg;
    private boolean forceWritable;
    private String  value;
}
