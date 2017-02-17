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

/**
 *  HTMLText
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       23 April 1998
 */
public final class HTMLLabel extends HTMLComponent
{
    private final HTMLFormComponent input;

    //private HTMLText text;
    private HTMLComponent component;

    /**
     *
     * @param text
     * @param input the input
     */
    public HTMLLabel(final String text, final HTMLFormComponent input)
    {
        super( );
        appendClassName("sts-label");
        this.component=new HTMLText( text);
        this.input=input;
        iSetId(input.getId() + "_LABEL");
    }


    public HTMLComponent getComponent()
    {
        return component;
    }

    public void setComponent(final HTMLText component)
    {
        this.component=component;
    }

    public void setComponent(final HTMLAnchor component)
    {
        this.component=component;
    }

    /**
     * @param browser to compile for
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        if( component != null) component.compile(browser);
        if( input != null) ((HTMLComponent)input).compile(browser);

        super.compile(browser);
    }


    /**
     * generated
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    protected void iGenerate( ClientBrowser browser, StringBuilder buffer)
    {
        buffer.append( "<label for=\"");
        buffer.append( input.getId());
        buffer.append( "\"");
        iGenerateAttributes(browser, buffer);
        buffer.append( ">");

        if( component != null) component.iGenerate(browser, buffer);

        buffer.append( "</label>");
    }

    /*@Override*/
    /** {@inheritDoc} *
    protected boolean needTag()
    {
        return true;
    }

    /** {@inheritDoc} *
    @Override
    protected void startTag(final StringBuilder buffer)
    {
        buffer.append( "<label for=\"");
        buffer.append( input.getId());
        buffer.append( "\"");
    }

    /** {@inheritDoc} *
    @Override
    protected void endTag(final StringBuilder buffer)
    {
        buffer.append( "</label>");
    }*/
}
