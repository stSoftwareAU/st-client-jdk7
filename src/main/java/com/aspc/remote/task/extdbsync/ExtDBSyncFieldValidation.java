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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
public class ExtDBSyncFieldValidation
{
    @SuppressWarnings("PublicInnerClass")
    public static enum ACTION
    {
        DEBUG("debug"),
        INFO("info"),
        WARNING("warn"),
        ERROR("error");

        /**
         * the name
         */
        public final String name;

        /**
         * the construction
         * @param name the name
         */
        private ACTION( final String name)
        {
            this.name=name;
        }

        /**
         * find
         * @param name the name
         * @return the matching action.
         */
        public static ACTION find( final String name)
        {
            for( ACTION a : values())
            {
                if( name.toLowerCase().startsWith(a.name.toLowerCase()))
                {
                    return a;
                }
            }

            throw new IllegalArgumentException( "unknown action '" + name + "'");
        }
    }

    public final String code;
    public final String msg;
    public final String filterField;
    private final ExtDBSyncFieldValidation[] conditions;
    public final Pattern pattern;
    public final Pattern filterPattern;

    public final ACTION action;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.extdbsync.ExtDBSyncFieldValidation");//#LOGGER-NOPMD

    /**
     * constructor
     *
     * @param code
     * @param msg the message
     * @param pattern the pattern
     * @param action the action
     * @param filterField the field to filter on
     * @param filterPattern  the field pattern to filter on
     * @param conditions
     */
    public ExtDBSyncFieldValidation(
        final String code,
        final String msg,
        final String pattern,
        final ACTION action,
        final String filterField,
        final String filterPattern,
        final ExtDBSyncFieldValidation... conditions
    )
    {
        this.code=code;
        this.msg=msg;
        this.pattern=Pattern.compile( pattern);
        this.action=action;
        this.filterField=filterField;
        if( conditions == null)
        {
            this.conditions=new ExtDBSyncFieldValidation[0];
        }
        else
        {
            this.conditions=conditions.clone();
        }
        if( StringUtilities.notBlank(filterPattern))
        {
            if(StringUtilities.isBlank(filterField))
            {
                throw new IllegalArgumentException( "can not set the filter pattern without the filter field");
            }
            this.filterPattern=Pattern.compile( filterPattern);
        }
        else
        {
            if(StringUtilities.notBlank(filterField))
            {
                throw new IllegalArgumentException( "can not set the filter field without the filter pattern");
            }
            this.filterPattern=null;
        }
    }

    /**
     * create the validation list.
     *
     * @param fieldsElement the field element
     * @return the list
     * @throws ExtDBSyncValidationException
     */
    public static ExtDBSyncFieldValidation[] createValidationList(final Element fieldsElement) throws ExtDBSyncValidationException
    {
        NodeList validationList = fieldsElement.getElementsByTagName( ExtDBSyncHandler.TAG_VALIDATE);

        ArrayList<ExtDBSyncFieldValidation>vList=new ArrayList();

        HashMap<String, ExtDBSyncFieldValidation> conditions=new HashMap();
        for( int loop=0; loop<2;loop++)
        {
            for( int v = 0; v < validationList.getLength(); v++)
            {
                Element e = (Element)validationList.item( v);
                String conditionsList="";

                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_CONDITIONS))
                {
                    conditionsList=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_CONDITIONS);
                }

                ExtDBSyncFieldValidation cl[]=null;
                if( loop == 0)
                {
                    if( StringUtilities.notBlank(conditionsList)) continue;
                }
                else
                {
                    if( StringUtilities.isBlank(conditionsList)) continue;
                    String[] l = conditionsList.split(",");

                    cl=new ExtDBSyncFieldValidation[l.length];
                    for( int i=0;i< l.length;i++)
                    {
                        ExtDBSyncFieldValidation cv = conditions.get( l[i]);
                        if( cv == null)
                        {
                            throw new ExtDBSyncValidationException( "no condition " + l[i]);
                        }
                        cl[i]=cv;
                    }
                }

                ACTION action = ACTION.DEBUG;
                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_ACTION))
                {
                    action=ExtDBSyncFieldValidation.ACTION.find( e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_ACTION));
                }

                String code="";
                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_CODE))
                {
                    code=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_CODE);
                }

                String message="";
                if(e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_MESSAGE))
                {
                    message=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_MESSAGE);
                }

                String p=".*";
                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_PATTERN))
                {
                    p=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_PATTERN);
                }

                String filterField=null;
                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_FILTER_FIELD))
                {
                    filterField=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_FILTER_FIELD);
                }

                String filterPattern=null;
                if( e.hasAttribute(ExtDBSyncHandler.ATT_VALIDATE_FILTER_PATTERN))
                {
                    filterPattern=e.getAttribute(ExtDBSyncHandler.ATT_VALIDATE_FILTER_PATTERN);
                }

                ExtDBSyncFieldValidation fv=new ExtDBSyncFieldValidation(code, message, p, action, filterField, filterPattern, cl);

                if( StringUtilities.notBlank(code))
                {
                    conditions.put(code, fv);
                }
                vList.add(fv);
            }
        }

        ExtDBSyncFieldValidation l[]=new ExtDBSyncFieldValidation[vList.size()];

        vList.toArray(l);

        return l;
    }

    public void validate(
        final String value,
        final HashMap<String, String> valueMap
    ) throws ExtDBSyncValidationException
    {
        for( ExtDBSyncFieldValidation c: conditions)
        {
            if( c.match(value, valueMap) == false) return;
        }

        if( StringUtilities.notBlank(filterField))
        {
            String filterValue = valueMap.get( filterField.toLowerCase());

            if( filterValue == null)
            {
                LOGGER.info("validation fields:-\n" + valueMap);
                throw new ExtDBSyncValidationException( "No filter field '" + filterField + "' found");
            }

            if( filterPattern.matcher(filterValue).matches()==false)
            {
                return;// no need to validate if the filter doesn't match.
            }
        }

        if( match(value, valueMap) == false)
        {
            String temp= String.format(msg, value, pattern.pattern());

            if( action == ACTION.ERROR)
            {
                throw new ExtDBSyncValidationException( temp);
            }
            else if( action == ACTION.WARNING)
            {
                LOGGER.warn( temp);
            }
            else if( action == ACTION.INFO)
            {
                LOGGER.info( temp);
            }
            else if( action == ACTION.DEBUG)
            {
                LOGGER.debug( temp);
            }
            else
            {
                LOGGER.info("validation fields:-\n" + valueMap);
                throw new ExtDBSyncValidationException( "unknown action " + action);
            }
        }
    }

    public boolean match(
        final String value,
        final HashMap<String, String> valueMap
    )
    {
        if( StringUtilities.notBlank(filterField))
        {
            String filterValue = valueMap.get( filterField.toLowerCase());

            if( filterValue == null)
            {
                return false;
            }

            if( filterPattern.matcher(filterValue).matches()==false)
            {
                return false;// no need to validate if the filter doesn't match.
            }
        }

        if( pattern.matcher(value).matches()==false)
        {
            return false;
        }

        return true;
    }
}
