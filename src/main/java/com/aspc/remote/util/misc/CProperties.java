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
package com.aspc.remote.util.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  Allows a properties file before the properties are required
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author Nigel Leck
 *  @since       19 March2004
 */
public final class CProperties
{
    /**
     * Disable the feature
     */
    public static final String PROPERTY_DISABLE="DISABLE";

    /**
     * enable the feature
     */
    public static final String PROPERTY_ENABLE="ENABLE";

    /**
     * location of properties file
     */
    public static final String PROPERTY_PROP_FILE="ST_PROPERTIES";
    
    /**
     * cservlet request timeout
     */
    public static final String PROPERTY_REQUEST_TIMEOUT="REQUEST_TIMEOUT";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.CProperties");//#LOGGER-NOPMD
    
    private CProperties()
    {

    }
 
    /**
     * Has this module been disabled.
     * @param module The module to check
     * @return true if disabled.
     */
    @CheckReturnValue
    public static boolean isDisabled( final @Nonnull String module)
    {
        String list;

        list = findProperty(CProperties.PROPERTY_DISABLE);

        if( list.indexOf( ',') != -1)
        {
            StringTokenizer st = new StringTokenizer( list, ",");

            while( st.hasMoreTokens())
            {
                String item = st.nextToken();

                if( item.equalsIgnoreCase( "ALL"))
                {
                    return true;
                }
                else if( item.equalsIgnoreCase( module))
                {
                    return true;
                }
            }
        }
        else
        {
            if( list.equalsIgnoreCase( "ALL"))
            {
                return true;
            }
            else if( list.equalsIgnoreCase( module))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * is UNIX ?
     *
     * @return true if Unix
     */
    @CheckReturnValue
    public static boolean isUnix( )
    {
        boolean result = false;
        String osName = findProperty("os.name");

        if(
            osName.equalsIgnoreCase( "SunOS") ||
            osName.equalsIgnoreCase( "Linux") ||
            osName.equalsIgnoreCase( "Mac OS X") ||
            osName.equalsIgnoreCase( "Solaris")
        )
        {
            result = true;
        }

        return result;
    }
    
    
     /**
     * is Mac?
     *
     * @return true if Unix
     */
    @CheckReturnValue
    public static boolean isMac()
    {
        boolean result = false;
        String osName = findProperty("os.name");

        if(osName.equalsIgnoreCase( "Mac OS X"))
        {
            result = true;
        }

        return result;
    }
    

    /**
     * Home directory path property name
     */
     public static final String ST_HOME = "ST_HOME";

     /**
      * Root document directory path property name
     */
     public static final String DEFAULT_DOC_ROOT_PATH = "/webapps/st";

     static
     {
        try {
            iLoadProperties();
        } catch (IOException ex) {
            throw CLogger.rethrowRuntimeExcepton(ex);
        }
     }

     /**
     * Return specified property value.
     * @param name name of property
     * @return the property value
     */
     @CheckReturnValue @Nullable
     public static String getProperty(final @Nonnull String name)
     {
         return System.getProperty(name);
     }
     
     /**
     * Return specified property value.
     * @param name name of property
     * @return the property value
     */
     @CheckReturnValue @Nonnull
     public static String findProperty(final @Nonnull String name)
     {
         String value=System.getProperty(name);
         if( StringUtilities.isBlank(value))
         {
             return "";
         }
         
         return value;
     }
      
     /**
     * Return specified property value.
     * @param name name of property
     * @param defaultValue the default value
     * @return the property value
     */
     @CheckReturnValue @Nonnull
     public static String findProperty(final @Nonnull String name, final @Nonnull String defaultValue)
     {
         if( name==null) throw new IllegalArgumentException("name parameter is mandatory");
         if( defaultValue==null) throw new IllegalArgumentException("defaultValue parameter is mandatory");
         
         String value=System.getProperty(name);
         if( StringUtilities.isBlank(value))
         {
             return defaultValue;
         }
         
         return value;
     }
     
     /**
     * Return specified property value.
     * @param name name of property
     * @param defaultValue value to be returned if not value associated with supplied property
     * @return the property value of default value if none found
     */
     @CheckReturnValue @Nullable
     public static String getProperty(final @Nonnull String name, final @Nullable String defaultValue)
     {
         return System.getProperty(name, defaultValue);
     }

    /**
     * Determines if the property list contains a setting for a property
     * @param name name of property to check
     * @return true if the property list contains a setting for the supplied property name
     */
     @CheckReturnValue
     public static boolean hasProperty(final @Nonnull String name)
     {
         return System.getProperties().containsKey(name);
     }

    /**
     * Forces reload of properties
     * @throws java.io.IOException
     */
     public static void reloadProperties() throws IOException
     {
         iLoadProperties();
     }

     /**
      * Load properties.
      */
    @SuppressWarnings({ "BroadCatchBlock", "TooBroadCatch"})
     private static void iLoadProperties() throws IOException
     {
        String propFile =System.getProperty(PROPERTY_PROP_FILE, "");
        if( propFile.startsWith("file://")) propFile=propFile.substring(7);
        if (StringUtilities.notBlank(propFile))
        {
            File pFile = new File(propFile);
            if( pFile.exists())
            {
                try(FileInputStream in=new FileInputStream(pFile))
                {
                    System.getProperties().load(in);
                }
            }
            else
            {
                throw new IOException( "does not exist: " + propFile);
            }
        }
        
        File home = new File(getHomeDir());
        if( home.isDirectory())
        {
            File confDIR = new File(home, "conf");
            if( confDIR.isDirectory())
            {
                File properties = new File(confDIR, "common.properties");
                if( properties.exists())
                {
                    try(FileInputStream in=new FileInputStream(properties))
                    {
                        System.getProperties().load(in);
                    }
                }
            }
//            else 
//            {
//                LOGGER.info( "conf directory doesn't exist: " + confDIR);
//            }
        }
        else
        {
            throw new IOException( "home directory doesn't exist: " + home);
        }
     }

     /**
     * Get the current home setting
     * @return the path of the home directory
     */
     @Nonnull @CheckReturnValue
     public static String getHomeDir()
     {
         String home = System.getProperty(ST_HOME);
         if( home == null)
         {
             // handle backward compatability
             home = System.getProperty("STSERVER_HOME");
         }
         if( home == null)
         {
             // handle backward compatability
             home = System.getProperty("STLITE.HOME");
         }
         if( home == null)
         {
             home = System.getProperty("user.home");
         }
         assert home !=null;
         return home;
     }


     /**
      * Get value for DOC_ROOT.
      * @return the path of the document root directory
      */
     @CheckReturnValue @Nonnull
     public static String getDocRoot()
     {
         String docDir = CProperties.getProperty("DOC_ROOT");
         if( StringUtilities.isBlank(docDir))
         {
            docDir = CProperties.getHomeDir() + DEFAULT_DOC_ROOT_PATH;
         }
         assert docDir!=null;
         return docDir;
    }
}
