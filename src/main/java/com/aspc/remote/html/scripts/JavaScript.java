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
package com.aspc.remote.html.scripts;
import com.aspc.remote.html.ClientBrowser;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  JavaScript
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       27 August 1998
 */
public class JavaScript
{
    /**
     *
     */
    public JavaScript()
    {
        this( "" );
    }

    /**
     * 
     * @param listing 
     */
    public JavaScript(String listing)
    {
        setProgramList(listing);//NOPMD
    }

    /**
     * 
     * @return the value
     */
    public final String getProgramList()
    {
        return programList;
    }

    /**
     * 
     * @param listing 
     */
    public final void setProgramList(String listing)
    {
        assert listing != null: "the script should not be null";
        programList = listing;
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */    
    public void iGenerate( final ClientBrowser browser, final StringBuilder buffer)
    {
        assert buffer !=null: "the buffer must not be null";
        buffer.append( getProgramList().trim());
    }

    /**
     * 
     * @return the script
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        iGenerate( null, sb);
        
        return sb.toString();
    }
    
    /**
     * the program listing.
     */
    private String programList;
}
