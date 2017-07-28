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
package com.aspc.remote.html.internal;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.html.HTMLComponent;
import com.aspc.remote.html.scripts.HTMLMouseEvent;

/**
 *  HTMLAbstractAnchor.java
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED </i>
 *
 *  @author      Nigel Leck
 *  @since       December 7, 2000, 1:04 PM
 */
public interface HTMLAbstractAnchor extends HandlesMouseEvents, HandlesSingleClick
{
    /**
     * Marks the start of a URL that we will check for.
     */
    static final String URL_CHECK="UC";
    /**
     * Marks the end of the URL. If we see the URL_CHECK but not the URL_END then we have an error.
     */
    static final String URL_END="UE";

    /**
     * The maximum length of a URL that we can safely have without the proxies or the browser
     * truncating it. According to an article by Microsoft the maximum length should be 2086
     * http://support.microsoft.com/kb/208427 but during testing we have found the limit to be
     * closer to 1500.
     */
    static final int URL_MAX_LENGTH=1500;

    /**
     * add a component to this anchor.
     *
     * @param component The component to add.
     */
    void addComponent( HTMLComponent component);

    /**
     * add a mouse event to this anchor
     *
     * @param me The mouse event
     */
    @Override
    void addMouseEvent(HTMLMouseEvent me);

    /**
     * get the hyper link for this anchor
     * @return the link
     */
    String getHREF();

    /**
     * get the hyper link for this anchor
     * @return the link
     * @param browser the browser
     */
    String getHREF( ClientBrowser browser);

    /**
     * The window target
     * @param target The target window.
     */
    void setTarget( String target);

    /**
     * The target window width
     * @param pixels the width in pixels
     */
    void setTargetWidth( int pixels);

    /**
     * The target window height
     * @param pixels the height in pixels
     */
    void setTargetHeight( int pixels);

    /**
     * make the target window plain ie. no menus etc
     * @param on this is a plan window
     */
    void setTargetWindowPlain( boolean on);

    /**
     * The tab index
     * @param index the tab index.
     */
    void setTabIndex( int index);

    /**
     *
     * add a call parameter
     * @param token the name
     * @param value the value
     */
    void addCallParameter( String token, String value);
    
    /**
     * set tooltip
     * @param toolTip the tooltip
     */
    void setToolTip( String toolTip);

     /**
     *
     * Should the status bar be shown in the target window
     * @param on show the status bar
     */
    void setTargetStatusBar( boolean on);

    /**
     *  get the plain hyper link for this anchor
     * @return the link
     */
    String getURL();
}
