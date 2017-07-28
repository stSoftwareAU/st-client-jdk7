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
 *  HTMLAjaxFieldHelper
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component </i>
 *
 *  @author      Roger Zhao
 *  @since       June 3, 2008, 2:13 PM
 */
public class HTMLAjaxFieldHelper 
{   
    private String domId;
    private String path;
    private String dbClass;
    private String globalKey;
    private String format;
    private boolean bold;
    private boolean includesTags;

    public boolean isIncludesTags() {
        return includesTags;
    }

    public void setIncludesTags(boolean includesTags) {
        this.includesTags = includesTags;
    }
    private int fontSize;
   
   /**
    * Get DbClass
    * @return DbClass
    */
   public String getDbClass()
   {
        return dbClass;
   }

   /**
    * Set DbClass
    * @param dbClass DbClass to be set
    */
   public void setDbClass(String dbClass)
   {
        this.dbClass = dbClass;
   }
   /**
    * Get format
    * @return format
    */
    public String getFormat() 
    {
        return format;
    }
   /**
    * Set format
    * @param format dom id to be set
    */
    public void setFormat(String format) 
    {
        this.format = format;
    }

   /**
    * Get dom id
    * @return dom id
    */
   public String getDomId()
   {
        return domId;
   }

   /**
    * Set dom id
    * @param domId dom id to be set
    */
   public void setDomId(String domId)
   {
        this.domId = domId;
   }

   /**
    * Get global key
    * @return global key
    */
   public String getGlobalKey()
   {
        return globalKey;
   }

   /**
    * Set global key
    * @param globalKey global key to be set
    */
   public void setGlobalKey(String globalKey)
    {
        this.globalKey = globalKey;
    }

   /**
    * Get path
    * @return path
    */
   public String getPath()
    {
        return path;
    }

   /**
    * Set path
    * @param path path to be set
    */
   public void setPath(String path)
    {
        this.path = path;
    }

   /**
     * Is bold
     * @return bold 
     */
    public boolean isBold() 
    {
        return bold;
    }

    /**
     * Get Font Size
     * @return size
     */
    public int getFontSize() 
    {
        return fontSize;
    }

    /**
     * Set Bold
     * @param bold bold
     */
    public void setBold(boolean bold) 
    {
        this.bold = bold;
    }

    /**
     * Font Size
     * @param fontSize fontSize
     */
    public void setFontSize(int fontSize) 
    {
        this.fontSize = fontSize;
    }
   
   
}

