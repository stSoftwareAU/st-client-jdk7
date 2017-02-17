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
import java.awt.*;
import com.aspc.remote.html.scripts.*;
import com.aspc.remote.html.style.*;
/**
 *  HTMLPostItNote
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       28 May 1998
 */
public class HTMLPostItNote extends HTMLComponent
{
    /**
     * 
     * @param id 
     * @param text 
     */
    public HTMLPostItNote(String id, String text)
    {
        this(
            id,
            new HTMLText( "Cover Note"),
            new HTMLText( text)
        );

    }
    /**
     * 
     * @param id 
     * @param text 
     * @param title 
     */
    public HTMLPostItNote( String id, String text, String title)
    {
        this(
            id,
            new HTMLText( title),
            new HTMLText( text)
        );
    }

    /**
     * 
     * @param id 
     * @param content 
     * @param title 
     */
    public HTMLPostItNote(
        String id,
        HTMLComponent content,
        HTMLComponent title
    )
    {
        this.id = id;
        this.content = content;
        this.title = title;
    }

    /**
     * 
     * @param browser 
     */
    @Override
    protected void compile(ClientBrowser browser)
    {

        HTMLTable table = new HTMLTable();

        HTMLDiv coverdiv = new HTMLDiv(id);
        HTMLTable mTable = new HTMLTable();
        mTable.setWidth("100%");
        mTable.setCell(table, 0,0);
        mTable.setCellAlignment("CENTER", 0,0);

        coverdiv.addComponent (mTable);
        iAddComponent(coverdiv);

        table.setTableAlignment ("CENTER");
        table.setWidth("" + width);
        table.setBorder(1);

        if( title instanceof HTMLText)
        {
            HTMLText titleTEXT = (HTMLText) title;

            titleTEXT.setBold (true);
            titleTEXT.setColor(Color.white);
        }

        table.setCell(title, 0, 0);

        table.setCellBackGroundColor(new Color(0,0,154),0,0);
        table.setColumnWidth(0, "" + (width - 20));
        table.setCellAlignment ("LEFT", 0, 0);

        HTMLAnchor anc = new HTMLAnchor("javascript:setStyleVisible('"+this.id+"', false)");
        anc.addMouseEvent (new HTMLMouseEvent("onMouseOver","window.status='Close';return;"));
        anc.addMouseEvent (new HTMLMouseEvent("onMouseOut","window.status='';return;"));
        anc.addMouseEvent (new HTMLMouseEvent("onClick","javascript:setStyleVisible('"+id+"',false)"));
        anc.showUnderline (false);
        anc.addText("=");

        table.setCell(anc, 0, 1);
        table.setCellBackGroundColor(new Color(225,225,225),0,1);
        table.setColumnWidth(1, "20");
        table.setCellAlignment ("CENTER", 0, 1);

        //if( content instanceof HTMLText)
        //{
        //    HTMLText contentTEXT = (HTMLText) content;
        //}
        table.setCell(content, 1, 0);
        table.setCellColSpan (2, 1, 0);
        table.setCellBackGroundColor(new Color(255,255,200),1,0);

        super.compile(browser);
    }

    /**
     * 
     * @param page 
     */
    @Override
    protected void iAddedToPage(HTMLPage page)
    {
        HTMLStyleSheet sytle;

        sytle = new HTMLStyleSheet("#"+id);
        sytle.setZ( 9999);
        sytle.addElement ("top","10");
        sytle.setPosition(HTMLStyleSheet.POSITION_ABSOLUTE);

        page.registerStyleSheet(sytle);
    }

    /**
     * 
     * @return the value
     */
    public String getPostItNoteID()
    {
        return id;
    }

    /**
     * 
     * @param width 
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    int width = 300;
    String id; //WARN: Field HTMLPostItNote.id masks field in superclass com.aspc.remote.html.HTMLComponent
    HTMLComponent   content,
                    title;
}
