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
import java.awt.*;

/**
 *  HTMLMarquee
 *
 * <br>
 * <i>THREAD MODE: MULTI-THREADED</i>
 *  @author      Nigel Leck
 *  @since       June 21, 1999, 3:56 PM
 */
public class HTMLMarquee extends HTMLContainer
{
    /**
     *
     */
    public HTMLMarquee()
    {
       loop ="";
       bgcolor=null;
       direction="";
       behavior="";
       scrollamount="";
       scrolldelay="";
       height="";
       width="";
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
        buffer.append("<MARQUEE");

        iGenerateAttributes(browser, buffer);

        if (bgcolor !=null)
          {
            buffer.append(" BGCOLOR=\"#");
            int c;
            c = bgcolor.getRGB() & 0xffffff;
            String  t;

            t = "000000" + Integer.toHexString(c);

            t = t.substring(t.length() - 6);

            buffer.append( t + "\"");
          }
        if (! loop.equals(""))
          {
            buffer.append(" LOOP=" + this.loop);
          }
        if (! direction.equals(""))
          {
            buffer.append(" DIRECTION=" + this.direction);
          }
        if (! behavior.equals(""))
          {
            buffer.append(" BEHAVIOR=" + this.behavior);
          }
        if (! scrollamount.equals(""))
          {
            buffer.append(" SCROLLAMOUNT=" + this.scrollamount);
          }
        if (! scrolldelay.equals(""))
          {
            buffer.append(" SCROLLDELAY=" + this.scrolldelay);
          }
        if (! height.equals(""))
          {
            buffer.append(" HEIGHT=" + this.height);
          }
        if (! loop.equals(""))
          {
            buffer.append(" WIDTH=" + this.width);
          }
        buffer.append(">\n");

        super.iGenerate(browser, buffer);

        buffer.append("</MARQUEE>\n");
    }

    /**
     * 
     * @param color 
     */
    public void setBackgroundColor(Color color )
    {
        this.bgcolor = color;
    }

    /**
     * 
     * @param loop 
     */
    public void setLoop(int loop )
    {
        this.loop = ""+loop;
    }

    /**
     * 
     * @param direction 
     */
    public void setDirection(String direction )
    {
        this.direction = direction;
    }

    /**
     * 
     * @param behavior 
     */
    public void setBehavior(String behavior )
    {
        this.behavior = behavior;
    }

    /**
     * 
     * @param scrollamount 
     */
    public void setScrollAmount(int scrollamount )
    {
        this.scrollamount = ""+scrollamount;
    }

    /**
     * 
     * @param scrolldelay 
     */
    public void setScrollDelay(int scrolldelay )
    {
        this.scrolldelay = ""+scrolldelay;
    }

    /**
     * 
     * @param height 
     */
    public void setHeightPercent(String height )
    {
        this.height = ""+height+"%";
    }

    /**
     * 
     * @param height 
     */
    public void setHeightPixels(int height)
    {
        this.height = ""+height;
    }

    /**
     * 
     * @param width 
     */
    public void setWidthPercent(String width)
    {
        this.width = ""+width+"%";
    }

    /**
     * 
     * @param width 
     */
    public void setWidthPixels(int width)
    {
        this.width = ""+width;
    }

    String loop,
           direction,
           behavior,
           scrollamount,
           scrolldelay,
           height,
           width;

   Color bgcolor;
}
