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

/**
 *  HTMLFrameSet
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       14 May 1998
 */
public class HTMLFrameSet extends HTMLContainer
{
    /**
     *
     * @param rows
     * @param spacing
     */
    public HTMLFrameSet(boolean rows, String spacing)
    {
        this.rows = rows;
        this.spacing = spacing;
        this.border = true;
    }

    /**
     *
     * @param on
     */
    public void setBorder( boolean on)
    {
        border = on;
    }

    /**
     *
     * @return the value
     */
    public boolean isByRows()
    {
        return rows;
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
        String t;

        if( rows == true)
        {
            t = "rows=\"" + spacing + "\"";
        }
        else
        {
            t = "cols=\"" + spacing + "\" rows=\"*\"";
        }

        // If the frameset only has 1 frame
        // netscape 4.0 will not show it. So I'll make a dummy one.
        if( iGetComponentCount() == 1)
        {
            spacing = "100%,*";
        }

        String bordr = "";
        if( border == false)
        {
            // We need BORDER=0 for netscape.
            bordr =  " FRAMEBORDER=NO border=0 FRAMESPACING=0";
        }

        //HTMLTheme theme = getTheme();

        buffer.append("<frameset ").append(t).append(bordr).append(">\n");

        super.iGenerate(browser, buffer);

        if( iGetComponentCount() == 1)
        {
            buffer.append( "<frame>\n");
        }

        /*buffer.append( "<NOFRAMES>\n");

        // Put these raw links so that search engine crawlers can scan our site.
        ArrayList v = items;

        for( int i = 0; v != null && i < v.size(); i++)
        {
            HTMLComponent c = (HTMLComponent)v.get( i);

            if( c instanceof HTMLFrame)
            {
                HTMLFrame f = (HTMLFrame)c;

                buffer.append("<A href=\"").append(f.getSrc()).append("\" target=\"").append(f.getName()).append("\">").append(f.getName()).append( "</A>\n");

            }
        }
        buffer.append( "</NOFRAMES>\n");*/

        buffer.append(
            "</frameset>\n"
        );
    }

    /**
     *  If a FrameSet is added to a page then the
     * page can't have a 'BODY' statement
     * @param page
     */
    @Override
    protected void iAddedToPage( HTMLPage page)
    {
        page.setHasFrameSet( true);
    }


    private boolean rows,//NOPMD
                    border;

    private String spacing;
}
