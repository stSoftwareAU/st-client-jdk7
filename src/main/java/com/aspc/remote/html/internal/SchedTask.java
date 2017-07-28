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
import java.util.*;

/**
 *  HTMLSchedTask is a
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Jason McGrath
 *  @since       August 31, 2001, 11:18 AM
 */
public class SchedTask
{
    /**
     * 
     * @param id 
     * @param description 
     * @param resource 
     * @param startDateTime 
     * @param endDateTime 
     * @param url 
     */
    public SchedTask(
        String id,
        String description,
        String resource,
        Date startDateTime,
        Date endDateTime,
       String url)
    {
        this.id = id;
        this.description = description;
        this.resource = resource;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.url = url;
    }

    // Public Methods
    /**
     * 
     * @return the value
     */
    public String getID()
    {
        return id;
    }

    /**
     * 
     * @return the value
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * 
     * @return the value
     */
    public String getURL()
    {
        return url;
    }

    /**
     * 
     * @return the value
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * 
     * @return the value
     */
    public Date getStartDateTime()
    {
        return startDateTime;
    }

    /**
     * 
     * @return the value
     */
    public Date getEndDateTime()
    {
        return endDateTime;
    }

    // Protected Methods

    // Privates
    private String id,//NOPMD
                    description,//NOPMD
                    resource,//NOPMD
                    url;//NOPMD
    private  Date   startDateTime,//NOPMD
                    endDateTime;//NOPMD
}
