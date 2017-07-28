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
package com.aspc.remote.task.extdbsync;

import com.aspc.remote.util.misc.StringUtilities;
import java.util.ArrayList;

/**
 *  Updates an external database with changes made to stSoftware
 *  data in real time
 *
 *
 *  <br>
 *  <i>THREAD MODE: NON-MUTABLE</i>
 *
 *  @author      Jason McGrath
 *  @since       7 October 2005
 */
public class ExtDBSyncFieldDef
{
    private final ExtDBSyncFieldValidation validationArray[];

    /**
     * constructor
     *
     * @param srcPath source path
     * @param destName dest
     * @param isKey is a key
     * @param isString is a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     * @param dateFormat date format
     */
    public ExtDBSyncFieldDef(
        final String srcPath,
        final String destName,
        final boolean isKey,
        final boolean isString,
        final String dependentClasses,
        final String  dateFormat
    )
    {
        this(null, srcPath, destName, isKey, isString, dependentClasses, null);
    }

    /**
     * constructor
     *
     * @param srcValue fixed source value
     * @param srcPath source path
     * @param destName dest
     * @param isKey is a key
     * @param isString is a string
     * @param dependentClasses classes that should be processed that contain data that this field value may refer to
     * @param dateFormat date format
     * @param validations the validation list
     */
    public ExtDBSyncFieldDef(
        final String srcValue,
        final String srcPath,
        final String destName,
        final boolean isKey,
        final boolean isString,
        final String dependentClasses,
        final String dateFormat,
        final ExtDBSyncFieldValidation... validations
    )
    {
        this.srcPath = srcPath;
        int i = srcPath.indexOf(".");
        if( i > 0)
        {
            srcBaseField = srcPath.substring( 0,i);
        }
        else
        {
            srcBaseField = srcPath;
        }
        this.destName = destName;
        this.isKey = isKey;
        this.isString = isString;
        if( StringUtilities.isBlank(dependentClasses) == false)
        {
            this.dependentList = dependentClasses.split(",");
        }
        else
        {
            dependentList = new String[0];
        }
        this.dateFormat = dateFormat;

        this.srcValue = srcValue;

        if( validations != null)
        {
            validationArray=validations;
        }
        else
        {
            validationArray = new ExtDBSyncFieldValidation[0];
        }
    }

/**
     * constructor
     * @param dependentClasses List of dependent classes that this field refers to
     * @param srcPath source path
     * @param destName Destination field name
     * @param isKey is a key
     * @param isString is a string
     */
    public ExtDBSyncFieldDef( final String srcPath,
                              final String destName,
                              final boolean isKey,
                              final boolean isString,
                              final String dependentClasses)
    {
        this(srcPath, destName, isKey, isString, dependentClasses, null);
    }

    /**
     * constructor
     * @param dependentClasses List of dependent classes that this field refers to
     * @param srcValue fixed source value
     * @param srcPath source path
     * @param destName Destination field name
     * @param isKey is a key
     * @param isString is a string
     */
    public ExtDBSyncFieldDef(final String srcValue,
            final String srcPath,
            final String destName,
            final boolean isKey,
            final boolean isString,
            final String dependentClasses)
    {
        this(srcPath, destName, isKey, isString, dependentClasses);
        this.srcValue = srcValue;
    }

    /**
     * the src field
     * @return src field
     */
    public String getSrcBaseField()
    {
        return srcBaseField;
    }

    /**
     * the class that src field is linked to
     * @return linked class
     */
    public String[] getSrcDependentClasses()
    {
        return dependentList.clone();
    }

    /**
     * The list of validations.
     *
     * @return list the validations
     */
    public ExtDBSyncFieldValidation[] listValidations()
    {
        ArrayList<ExtDBSyncFieldValidation> l=new ArrayList<>();
        for( ExtDBSyncFieldValidation v: validationArray)
        {
            if( v!=null)
            {
                l.add(v);
            }
        }
        
        ExtDBSyncFieldValidation[] a=new ExtDBSyncFieldValidation[l.size()];
        l.toArray(a);
        return a;
    }

    /**
     * is simple
     * @return true if simple
     */
    public boolean isSimpleField()
    {
        return srcBaseField.equals( srcPath);
    }

    /**
     * the source path
     * @return the path
     */
    public String getSrcPath()
    {
        return srcPath;
    }

    /**
     * the source value
     * @return the value
     */
    public String getSrcValue()
    {
        return srcValue;
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
     * is a key
     * @return true if a key
     */
    public boolean isKey()
    {
        return this.isKey;
    }

    /**
     * is a string
     * @return true if a string
     */
    public boolean isString()
    {
        return this.isString;
    }

    /**
     * get the format
     * @return the format
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    private String srcValue = "";
    private final String srcPath;
    private final String srcBaseField;
    private final String destName;
    private final String[] dependentList;
    private final String dateFormat;

    private final boolean isKey,//NOPMD
                          isString;
}
