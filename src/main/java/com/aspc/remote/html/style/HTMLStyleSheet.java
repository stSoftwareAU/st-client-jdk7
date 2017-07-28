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
package com.aspc.remote.html.style;

import com.aspc.remote.html.ClientBrowser;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  HTMLStyleSheet
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       20 August 1998
 */
public class HTMLStyleSheet extends InternalStyleSheet implements StyleSheetInterface
{
    /**
     *
     * @param target
     */
    public HTMLStyleSheet(final String target)
    {
        isAutoClass = false;
        sortedKeys = new ArrayList();
        this.target = target;
    }

    /**
     *
     * @param isAutoClass if this style sheet is an automatically generated one
     */
    public HTMLStyleSheet(final boolean isAutoClass)
    {
        this.isAutoClass = isAutoClass;
        sortedKeys = new ArrayList();
    }

    public void lock()
    {
        lockFg=true;
    }

    /**
     *
     * @return target
     */
    public String getTarget()
    {
        return makeTarget();
    }

    /**
     *
     * @param type the type
     * @param value the value
     */
    @Override
    public void addElement( final String type, final String value)
    {
        assert type != null && type.trim().length() > 0: "type is blank";
        assert type.matches("([.#]?[a-zA-Z_\\-0-9]+\\ *)*"): "invalid type " + type;
        assert value != null && value.trim().length() > 0: "value is blank";
        assert value.matches("(&amp;|[ a-zA-Z_\\-0-9\\(\\)':@!~`#$%^&\\*\\+=\\{\\[\\}\\]\\|\\\\\\?/\\.,<>])+( !important)?$"): "invalid value " + type + "='" + value +"'";
        
        if(!items.containsKey(type))
        {
            if( lockFg)
            {
                throw new RuntimeException( "style sheet locked");
            }
            sortedKeys.add(type);
        }

        if( lockFg)
        {
            String temp = items.get( type);

            if( temp.equals(value) == false)
            {
                throw new RuntimeException( "style sheet locked");
            }
        }

        items.put(type, value);
    }

    /**
     * remove the given type
     * @param type type
     */
    public void removeType(final @Nonnull String type)
    {
        if( lockFg) throw new RuntimeException( "style sheet locked");
        if(items == null)
        {
            return;
        }
        items.remove(type);
        sortedKeys.remove(type);
    }

    /**
     * get the style value of this type
     * @param type type
     * @return value, null for not found
     */
    public @Nullable String getValue(final @Nonnull String type)
    {
        if(items == null)
        {
            return null;
        }
        return items.get(type);
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    @Override
    public void iGenerate( final ClientBrowser browser, final @Nonnull StringBuilder buffer)
    {
        if( items == null) return;

        buffer.append(toString());
    }

    @CheckReturnValue @Nonnull
    public String makeTarget()
    {
        if( StringUtilities.isBlank(target) == false) return target;

        boolean created=false;

        if( pageUniqueCounter == 0)
        {
            created =true;
            pageUniqueCounter = UNIQUE_COUNTER.incrementAndGet();
        }

        String temp = "s_" + Long.toHexString(pageUniqueCounter);

        if( created)
        {
            target=temp;
        }

        return temp;
    }

    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        if( items == null) return "";

        StringBuilder buffer = new StringBuilder();

        if(isAutoClass)
        {
            buffer.append(".");
        }
        buffer.append(makeTarget());
        if(sortedKeys.size() > 0)
        {
            buffer.append(" {");
            buffer.append(printStyles());
            buffer.append("}");
        }
        return buffer.toString();
    }

    @CheckReturnValue @Nullable
    public String toInlineStyleSheet()
    {
        if( items == null) return null;

        String s = printStyles();
        return "style=\"" + s + "\"";
    }

    @CheckReturnValue @Nullable
    private String printStyles()
    {
        StringBuilder buffer = new StringBuilder();
        for (String key : sortedKeys) 
        {
            if( StringUtilities.isBlank(key)) continue;
            buffer.append(key).append(": ");
            buffer.append(items.get(key));

            buffer.append(";");
        }
        
        String style= buffer.toString();
        assert style.replace("&amp;", "&").matches("(((-?[_a-zA-Z]+[_a-zA-Z0-9\\-]* *:)[^;]*;)| )*"): "invalid style " + style;
        return style;
    }

    /**
     * check if the object equals this
     * @param obj
     * @return true of false
     */
    @Override @CheckReturnValue
    public boolean equals(final Object obj)
    {
        if(obj == null)
        {
            return false;
        }
        if(getClass() != obj.getClass())
        {
            return false;
        }
        final HTMLStyleSheet other = (HTMLStyleSheet)obj;
        if((this.target == null) ? (other.target != null) : !this.target.equals(other.target))
        {
            return false;
        }
        if(this.isAutoClass != other.isAutoClass)
        {
            return false;
        }
        if((this.items == null) ? (other.items != null) : !this.items.equals(other.items))
        {
            return false;
        }
        return true;
    }

    /**
     *
     * @return hash code of this object
     */
    @Override @CheckReturnValue
    public int hashCode()
    {
        int hash = 7;
        hash = 23 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 23 * hash + (this.isAutoClass ? 1 : 0);
        hash = 23 * hash + (this.items != null ? this.items.hashCode() : 0);
        return hash;
    }

    protected void setSortedKeys(final ArrayList sortedKeys)
    {
        this.sortedKeys = new ArrayList( sortedKeys);
    }

    public long getPageUniqueCount()
    {
        return pageUniqueCounter;
    }

    public void setPageUniqueCount( final @Nonnegative long counter)
    {
        assert counter>0: "should be a positive number";
        assert counter<START_NUMBER: "should be less than " + START_NUMBER;
        
        if( pageUniqueCounter != 0 && pageUniqueCounter != counter)
        {
            throw new RuntimeException( "page unique counter already set ( was " + pageUniqueCounter + " now " + counter + ")");
        }
        pageUniqueCounter=counter;
    }

    /**
     * the target
     */
    protected String target;
    private boolean lockFg;
    private long pageUniqueCounter;
    private static final long START_NUMBER=1024;
    /** start at a number above what is expected in a single page */
    private static final AtomicLong UNIQUE_COUNTER=new AtomicLong(START_NUMBER); 
    private ArrayList<String> sortedKeys;
    /** if this style sheet is a auto generated class style sheet */
    @SuppressWarnings("ProtectedField")
    protected boolean isAutoClass = false;
}
