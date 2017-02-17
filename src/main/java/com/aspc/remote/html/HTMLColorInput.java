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

import com.aspc.remote.html.input.HTMLInput;
import com.aspc.remote.html.internal.HTMLFormComponent;
import com.aspc.remote.html.internal.InternalEvent;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  A HTML component which displays a color and allows the user to change the color value
 *  through a color picker.
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Jason McGrath
 *  @since       December 5, 2000, 6:22 PM
 */
public class HTMLColorInput extends HTMLFormComponent
{
    /**
     * Creates new HTMLColorInput
     * @param name
     */

    public HTMLColorInput(String name)
    {
        iSetName(name);
        showColorPicker = true;
    }

    /**
     *
     * @param name
     * @param value the value
     */
    public HTMLColorInput(String name, String value)
    {
        this( name);
        setValue( value);
        showColorPicker = true;
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
    }

    /**
     * add a mouse event to this component
     *
     * @param me The mouse event
     */
    public void addMouseEvent(HTMLMouseEvent me)
    {
        iAddEvent(me, "");
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
    public final void setValue( final @Nullable String value)
    {
        if(StringUtilities.isBlank(value))
        {
            this.value="";
        }
        else
        {
            assert value!=null;
            try{
                if( value.toUpperCase().startsWith("0X"))
                {
                    this.value = "" +Integer.parseInt(value.substring(2), 16);
                }
                else
                {
                    this.value = "" +Integer.parseInt(value);
                }
            }
            catch( NumberFormatException nfe)
            {
                String tmpValue;
                if( StringUtilities.validCharactersHTML(value))
                {
                    tmpValue="value: " + value;
                }
                else{
                    tmpValue= "encoded value: " + StringUtilities.encode(value);
                }
                throw new IllegalArgumentException("Not a color " + tmpValue,nfe);
            }
        }
    }

    /**
     *
     * @return the value
     */
    @Override @Nonnull
    public String getValue( )
    {
        if( value == null) return "";

        return value;
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
     * set Readonly to this input.
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
     *
     * @param show
     */
    public void setShowColorPicker(boolean show)
    {
        showColorPicker = show;
    }

    @Override
    protected void setParent(final HTMLComponent parent) {
        super.setParent(parent); 
        
        HTMLPage page = getParentPage();
        if( page != null)
        {
            if( page.getFlag( "HTMLColorInput").equals( ""))
            {
                page.addModule("JPICKER");
                page.addJavaScript(
                    "$(document).ready(\n" +
                    "function()\n" +
                    "{\n" +
                    "$('.COLOR_PICKER').jPicker({\nimages:{clientPath: '/ds/jpicker/1.1.6/images/'\n},\nwindow:\n{\n"+
                        "position: { x: '0 px', y: '0 px'}" +
                    "\n}}," +
                    "function(color, context)\n" +
                    "{\n" +
                    "this.onchange();\n" +
                    "}\n" +
                    ");\n" +
                    "});"
                );
                page.putFlag( "HTMLColorInput", "DONE");
            }
        }
    }
    
    /**
     *
     * @param browser
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        HTMLTable mTable = new HTMLTable();
        iAddComponent( mTable);

        String hex = "";

        if( StringUtilities.isBlank(value) == false)
        {
            try
            {
                String temp = Integer.toHexString( Integer.parseInt(value));
                int len = temp.length();
                if( len < 6)
                {
                    hex = "000000".substring( len) + temp;
                }
                else
                {
                    hex = temp;
                }
                value="#" + hex;
            }
            catch( NumberFormatException e)
            {

            }
        }
        HTMLInput in = new HTMLInput(name, value);
        in.setSize(7);
        mTable.setCell( in, 0, 2);
        if( events != null)
        {
            for (HTMLEvent ev : events) {                
                if( ev instanceof HTMLMouseEvent == false)
                {
                    in.iAddEvent( ev, "");
                }
            }
        }

        in.setInvisible(true);
        HTMLInput tmpCP = new HTMLInput("CP_" + name, hex);
        tmpCP.setSize(6);
        tmpCP.appendClassName("COLOR_PICKER");
        mTable.setCell( tmpCP, 0, 0);
        tmpCP.addOnChangeEvent("");

        if( disabledFg)
        {
            tmpCP.setDisabled(true);
        }
        else if(showColorPicker == true)
        {
            tmpCP.addOnChangeEvent("changed" + in.getId() + "()");
            HTMLPage page = getParentPage();
            page.addJavaScript(
                "function changed" + in.getId() + "()\n" +
                "{\n" +
                "  var cpE = findElement( '" + tmpCP.getId() + "');\n" +
                "  var inE = findElement( '" + in.getId() + "');\n" +
                "  var v = cpE.value;\n" +
                "  if( v.trim() !== ''){\n inE.value='#' + v.trim();\n}\n" +
                "  else\n{\n inE.value='';\n}\n" +
                "}"
            );

        }

        super.compile( browser);
    }

    /**
     *
     */
    protected boolean   disabledFg, readOnlyFg;
    private String  value;
    private boolean showColorPicker;
}
