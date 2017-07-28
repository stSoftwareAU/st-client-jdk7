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

import com.aspc.remote.database.NotFoundException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  HTMLContainer
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       12 February 1998
 */
public class HTMLContainer extends HTMLComponent
{
    /**
     * Adds a component to this container
     * @param component
     */
    public void addComponent( final @Nullable HTMLComponent component)
    {
        iAddComponent( component);
    }

    /**
     * Adds a component to this container at a particular position
     * @param component
     * @param index
     * @throws java.lang.ArrayIndexOutOfBoundsException
     */
    public void addComponent( HTMLComponent component,
                              int index)
                              throws ArrayIndexOutOfBoundsException
    {
        iAddComponent( component, index);
    }

    /**
     *
     * @param at
     */
    public void removeComponent( int at )
    {
        iRemoveComponent( at );
    }
   /**
     * Remove this component ?
     * @param lookFor
     * @return the value
     */
    public boolean removeComponent( final HTMLComponent lookFor)
    {
        int count = getComponentCount();
        int removeCount=0;
        for( int i = 0; i < count; i++)
        {
           HTMLComponent c = getComponent( i);
           
           if( c == lookFor)
           {
               removeComponent(i);
               removeCount++;
               i--;
               count--;
           }
        }

        return removeCount > 0;
    }
    
    /**
     *
     * @param id
     * @throws com.aspc.remote.database.NotFoundException
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public HTMLComponent findId( final @Nonnull String id) throws NotFoundException
    {
        HTMLComponent found;

        String temp = HTMLUtilities.makeValidHTMLId(id);

        found = iFindId( temp );

        if( found == null)
        {
            throw new NotFoundException( "No component found with id '" + id + "'");
        }

        return found;
    }

    /**
     * The count of components contained
     * @return the value
     */
    public int getComponentCount()
    {
        return iGetComponentCount();
    }

    /**
     *
     */
    public void clear()
    {
        iClear();
    }

    /**
     * Have we added this component ?
     * @param lookFor
     * @return the value
     */
    public boolean hasComponent( final HTMLComponent lookFor)
    {
        int count = getComponentCount();

        for( int i = 0; i < count; i++)
        {
           HTMLComponent c = getComponent( i);

           if( c == lookFor)
           {
               return true;
           }
        }

        return false;
    }

    /**
     *
     * @param at
     * @return the value
     */
    @CheckReturnValue @Nullable
    public HTMLComponent getComponent( final @Nonnegative int at)
    {
        return iGetComponent( at);
    }

    /**
     *
     * @param text
     */
    public void addText( String text)
    {
        HTMLText htmlText = new HTMLText( text);

        addComponent( htmlText);
    }
}
