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
package com.aspc.remote.task.extdbsync;

import com.aspc.remote.database.DataBase;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


/**
 *  Updates an external database with changes made to stSoftware
 *  data in real time
 *
 *  <br>
 *  <i>THREAD MODE: NON-MUTABLE</i>
 *
 *
 *  @author      Jason McGrath
 *  @since       23 September 2005
 */
public class ExtDBSyncTableDef
{
    /**
     * constructor
     *
     * @param filter filter name
     * @param srcName source name
     * @param destName dest name
     * @param mode the transaction mode
     * @param database the destination database object
     * @param updateHandlerName class name of class to perform updates
     */
    public ExtDBSyncTableDef(
        String srcName,
        String destName,
        DataBase database,
        final String mode,
        String updateHandlerName,
        String filter
    )
    {
        this.srcName = srcName;
        this.destName = destName;
        this.database = database;
        this.mode = mode;
        assert mode != null: "mode must not be null";
        this.updateHandlerName = updateHandlerName;
        this.filter = filter;
    }

    /**
     * The field def
     * @return the def
     */
    public ExtDBSyncFieldDef getKeyFieldDef()
    {
        return keyFieldDef;
    }

    /**
     * get the field def
     * @param srcFieldName the source field
     * @return the def
     */
    public ExtDBSyncFieldDef getFieldDef( String srcFieldName)
    {
        return (ExtDBSyncFieldDef)fieldMap.get( srcFieldName);
    }

    /**
     * the source name
     * @return the name
     */
    public String getSrcName()
    {
        return srcName;
    }

    /**
     * the dest name
     * @return the name
     */
    public String getDestName()
    {
        return destName;
    }

    /**
     * the destination database
     * @return the name
     */
    public DataBase getDataBase()
    {
        return database;
    }

    /**
     * the transaction mode
     * @return the name
     */
    public String getMode()
    {
        return mode;
    }

    /**
     * the filter
     * @return the filter
     */
    public String getFilter()
    {
        return filter;
    }

    /**
     * add a field
     * @param srcPath the src path
     * @param destName the dest name
     * @param isKey a key
     * @param isString a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     */
    public void addField( String srcPath,
                          String destName,
                          boolean isKey,
                          boolean isString,
                          String dependentClasses)
    {
        addField( "",
                  srcPath,
                  destName,
                  isKey,
                  isString,
                  dependentClasses,
                  null);

    }

    /**
     * add a field
     * @param srcValue the fixed src value
     * @param srcPath the src path
     * @param destName the dest name
     * @param isKey a key
     * @param isString a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     */
    public void addField( String srcValue,
                          String srcPath,
                          String destName,
                          boolean isKey,
                          boolean isString,
                          String dependentClasses)
    {
        addField( srcValue,
                  srcPath,
                  destName,
                  isKey,
                  isString,
                  dependentClasses,
                  null);
    }


    /**
     * add a field
     * @param srcPath the src path
     * @param destName the dest name
     * @param isKey a key
     * @param isString a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     * @param dateFormat date format
     */
    public void addField( String srcPath,
                          String destName,
                          boolean isKey,
                          boolean isString,
                          String dependentClasses,
                          String dateFormat)
    {
        addField( "",
                  srcPath,
                  destName,
                  isKey,
                  isString,
                  dependentClasses,
                  dateFormat);
    }

    /**
     * add a field
     * @param srcValue the src value
     * @param srcPath the src path
     * @param destName the dest name
     * @param isKey a key
     * @param isString a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     * @param dateFormat date format
     * @param validations the list of validations
     */
    public void addField(
        String srcValue,
        String srcPath,
        String destName,
        boolean isKey,
        boolean isString,
        String dependentClasses,
        String dateFormat,
        final ExtDBSyncFieldValidation... validations
    )
    {
        ExtDBSyncFieldDef fieldDef = new ExtDBSyncFieldDef( srcValue, srcPath, destName, isKey, isString, dependentClasses, dateFormat, validations);
        if( isKey)
        {
            keyFieldDef  = fieldDef;
        }

        fieldMap.put( srcPath, fieldDef);
    }


    /**
     * list the fields
     * @return a list of fields
     */
    public String[] getFieldNames()
    {
        ArrayList list = new ArrayList();
        Iterator en = fieldMap.keySet().iterator();
        while( en.hasNext())
        {
            list.add( (String)en.next());
        }
        String[] res = new String[ list.size()];
        list.toArray(res);
        return res;
    }

    /**
     * Retrieves the name of the class that will be used to perform the updates
     * @return the name of the class
     */
    public String getUpdateHandlerName()
    {
        return updateHandlerName;
    }

    private ConcurrentHashMap<String, ExtDBSyncFieldDef> fieldMap= new ConcurrentHashMap();//MT fix: use concurrent hash map not HashMap

    /**
     * the key field
     */
    private ExtDBSyncFieldDef keyFieldDef;
    private final String srcName,
                         destName,
                         mode,
                         updateHandlerName,
                         filter;

    private final DataBase database;
}


