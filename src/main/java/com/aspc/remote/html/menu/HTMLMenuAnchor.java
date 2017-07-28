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
package com.aspc.remote.html.menu;

import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.html.*;
import com.aspc.remote.html.internal.HTMLAbstractAnchor;
import com.aspc.remote.html.scripts.HTMLEvent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;

/**
 *  <!--#ASPC_HEAD_START-->
 *
 *  A menu item that provides a anchor.
 *                                                                              <BR>
 *                                                                              <BR>
 *  <a href='doc-files/tree_sdbcomponent.html' >data model</a><BR>
 *  <!--#ASPC_HEAD_END--><BR>
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 * @author michael
 * @since 6 January 2006
 */
public class HTMLMenuAnchor extends HTMLMenuItem implements HTMLAbstractAnchor
{

    /**
     * Creates a new instance of HTMLMenu
     *@param href - the menu anchor's href
     */

    public HTMLMenuAnchor(String href)
    {
        setURL(href);
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
     *  get the plain hyper link for this anchor
     * @return the link
     */
    @Override
    public String getURL()
    {
        return href;
    }

   /**
    *This method is to set url
    *@param  href - the href
    */
    public final void setURL( String href)
    {
        this.href = href;
    }

   /**
    *This method is to get href
    *@return String -  the href
    */
    @Override
    public String getHREF( )
    {
        return getHREF( null);
    }

   /**
    *@param browser
    *@return String -  the href
    */
    @Override
    public String getHREF( ClientBrowser browser)
    {
        if( StringUtilities.isBlank( href))
        {
            return null;
        }

        HTMLAnchor anchor = new HTMLAnchor( href);

        if( events != null)
        {
            for (HTMLEvent event: events) {
                anchor.addMouseEvent((HTMLMouseEvent)event);
            }
        }

        String theTarget = target;

        if( theTarget == null)
        {
            theTarget = HTMLAnchor.TARGET_BLANK_WINDOW;
        }

        anchor.setTarget( theTarget);
        anchor.setTargetWindowPlain( targetWindowPlain);
        anchor.setTargetStatusBar( targetStatusBar);

        int tmpWidth=700,
            tmpHeight=480;

        if( targetHeight != 0)
        {
            tmpHeight = targetHeight;
        }
        if( targetWidth != 0)
        {
            tmpWidth = targetWidth;
        }

        anchor.setTargetHeight( tmpHeight);
        anchor.setTargetWidth( tmpWidth);


        return anchor.getHREF();
    }

   /**
    *This method is to set  target
    *@param  target -  target
    */
    @Override
    public void setTarget(String target)
    {
        this.target = target;
    }

   /**
    *This method is to set  target's width
    *@param  pixels - int  target's width
    */
    @Override
    public void setTargetWidth( int pixels)
    {
        targetWidth = pixels;
    }

   /**
    *This method is to set  target's height
    *@param  pixels - int  target's height
    */
    @Override
    public void setTargetHeight( int pixels)
    {
        targetHeight = pixels;
    }

   /**
    *This method is to set  if  target window is plain
    *@param  on - boolean - true: target window is plain  ;  false:  target window is not plain
    */
    @Override
    public void setTargetWindowPlain( boolean on)
    {
        targetWindowPlain = on;
    }

    /**
     *
     * @param toolTip
     */
    @Override
    public void setToolTip( String toolTip)
    {
        this.toolTip = toolTip;
    }
    
   /**
    *This method is to set target's status bar
    *@param  on - boolean - true: need to set status bar  ;  false:  need not to set status bar
    */
    @Override
    public void setTargetStatusBar( boolean on)
    {
        targetStatusBar = on;
    }

    /**
    *This method is to set  menu anchor 's highlight selection
    *@param  on - boolean - true: need hightlight selection;  false: need not highlight selection.
    */
    public void setHighlightSelection( boolean on)
    {
        highlightSelection = on;
    }



   /**
    *This method is to get target
    *@return String - target
    */
    public String getTarget()
    {
        return this.target;
    }

   /**
    *This method is to get target's width
    *@return int - target's width
    */
    public int getTargetWidth()
    {
        return targetWidth;
    }

   /**
    *This method is to get target's height
    *@return int - target's height
    */
    public int getTargetHeight( )
    {
        return targetHeight;
    }

   /**
    *This method is to get if target window is plain
    *@return boolean -  if target window is plain
    */
    public boolean getTargetWindowPlain( )
    {
        return targetWindowPlain;
    }


  /**
    *This method is to get  menu anchor's highlight selection
    *@return - boolean -   menu anchor's highlight selection
    */
    public boolean getHighlightSelection()
    {
        return highlightSelection ;
    }

   /**
    * This method is to add parameter to the href
    * @param token -  parameter's name
    * @param value -  parameter's value
    */
    @Override
    public void addCallParameter(String token, String value)
    {
        if( href.indexOf( '?') == -1)
        {
            href += "?" + token + "=" + value;
        }
        else
        {
            href += "&" + token + "=" + value;
        }
    }


    /**
    *This method is to set menu anchor's image url
    *@param  url - image url
    */
    public void setBaseImageUrl( String url)
    {
        baseImageUrl = url;
    }

    /**
    *This method is to get menu anchor's image url
    *@return - url of image
    */
    public String getBaseImageUrl()
    {
        return baseImageUrl;
    }

    /**
    *This method is to set menu anchor's image url
    *@param  url - image url
    */
    public void setRolloverImageUrl( String url)
    {
        rolloverImageUrl = url;
    }

    /**
    *This method is to get menu anchor's image url
    *@return - url of image
    */
    public String getRolloverImageUrl()
    {
        return rolloverImageUrl;
    }

    private String  target;
    private String  href,
                    baseImageUrl,
                    rolloverImageUrl;
    private boolean targetWindowPlain,
                    targetStatusBar,
                    highlightSelection;
    private int     targetWidth,
                    targetHeight;


}

