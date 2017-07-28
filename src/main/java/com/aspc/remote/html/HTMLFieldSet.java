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
import com.aspc.remote.util.misc.StringUtilities;

/**
 *  &lt;fieldset&gt;
 *      &lt;legend&gt;Personalia:&lt;/legend&gt;
 *      Name: &lt;input type="text" size="30" /&gt;&lt;br /&gt;
 *      Email: &lt;input type="text" size="30" /&gt;&lt;br /&gt;
 *  &lt;/fieldset&gt;
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       28 June 2011
 */
public class HTMLFieldSet extends HTMLContainer
{
    private String legend;
    private HTMLComponent legendComponent;

    /**
     * create a division
     *
     * @param legend the legend
     */
    public HTMLFieldSet(final String legend)
    {
        this.legend=legend;
    }

    /**
     * create a division
     *
     * @param legend the legend
     */
    public HTMLFieldSet(final HTMLComponent legend)
    {
        this.legendComponent=legend;
    }


    /**
     * This is the spot to put all your generation
     * of the HTML components. Do not put it in iGenerate()
     * @param browser
     */
    @Override
    protected void compile( final ClientBrowser browser)
    {
        if( isCompiled() == true)
        {
            return;
        }

        if( legendComponent != null)
        {
            legendComponent.compile(browser);
        }
        appendClassName("sts-fieldset");
        super.compile(browser);
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
        buffer.append( "<fieldset");
        iGenerateAttributes(browser, buffer);
        buffer.append( ">\n");
        if( legendComponent != null || StringUtilities.isBlank(legend) == false)
        {
            buffer.append( "<legend class=\"sts-legend\">");
            if( legendComponent != null)
            {
                legendComponent.iGenerate(browser, buffer);
            }
            else
            {
                buffer.append( StringUtilities.encodeHTML(legend));
            }
            buffer.append( "</legend>\n");
        }
        super.iGenerate(browser, buffer);
        buffer.append( "</fieldset>\n");
    }
}
