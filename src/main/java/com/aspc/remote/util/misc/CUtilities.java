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
package com.aspc.remote.util.misc;

import com.aspc.remote.database.InvalidDataException;
//SERVER-START
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.internal.ReSTUtil;
//SERVER-END
import org.apache.commons.logging.Log;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;

/**
 *  CUtilities a Collection of bits and pieces.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       5 December 1996
 */
public final class CUtilities
{
    private CUtilities()
    {
        
    }

    // Defult params
    /**
     *
     */
    public static final String DEFAULT_HOSTNAME = "localhost";
    /**
     *
     */
    public static final String DEFAULT_PORT = "80";
    /**
     *
     */
    public static final String DEFAULT_HTTPSPORT = "443";
    
    /**
     * Application short name
     */
    public static final String PROPERTY_APP_SHORTNAME="APP_SHORTNAME";

    /**
     * Application name
     */
    public static final String PROPERTY_APP_NAME="APP_NAME";
    
    /**
     * Area
     */
    public static final String PROPERTY_AREA_NAME="AREA";
    
    /**
     * HTTP Port
     */
    public static final String PROPERTY_PORT="HTTP_PORT";
    
    /**
     * HTTPS Port
     */
    public static final String PROPERTY_SSLPORT="HTTPS_PORT";
    
    /**
     * Host name
     */
    public static final String PROPERTY_HOSTNAME="HOSTNAME";
    
    public static final String WEB_PROTOCOL_PREFIX = "web://";
    
    public static final String HTTPS_PROTOCOL_PREFIX = "https://";
    
    public static final String HTTP_PROTOCOL_PREFIX = "http://";
    
    
    /**
     * The application short name.
     *
     * @return the name.
     */
    public static String makeAppShortName()
    {
        String applicationName = CProperties.getProperty( PROPERTY_APP_SHORTNAME);
        
        if( StringUtilities.isBlank(applicationName))
        {
            applicationName = CProperties.getProperty( PROPERTY_APP_NAME);
            if( StringUtilities.isBlank(applicationName))
            {
                applicationName = CProperties.getProperty(
                    "application.name",
                    "java app"
                );
                applicationName = StringUtilities.replace(
                    applicationName,
                    "com.aspc.",
                    ""
                );

                applicationName = StringUtilities.replace(
                    applicationName,
                    ".StartUp",
                    ""
                );
            }
        }
        
        
        return applicationName;
    }
        
    /**
     * Return the application name
     *
     * @return the name.
     */
    public static String getAppName()
    {
        String applicationName = CProperties.getProperty( PROPERTY_APP_SHORTNAME, "");
        
        return applicationName;
    }
    
    /**
     * Return the application name
     *
     * @return the name.
     */
    public static String getAreaName()
    {
        String area = CProperties.getProperty( PROPERTY_AREA_NAME, "");
                
        return area;
    }
    
    /**
     * HTTP port of the server 
     * @return port as String
     */
    public static String getHTTPPort()
    {
        String port = System.getProperty(PROPERTY_PORT);
        return port;
    }
    
    /**
     * HTTPS port of the server if SSL is enabled 
     * @return port as String
     */
    public static String getHTTPSPort()
    {
        String port = System.getProperty(PROPERTY_SSLPORT);
        return port;
    }
    
    /**
     * Host name on which server is running.
     * @return Host name as String
     */
    public static String getHostName()
    {
        String host = System.getProperty(PROPERTY_HOSTNAME);
        return host;
    }
    
    /**
     * 
     * @param value the value
     * @return the value
     */
    public static boolean isZero( double value)
    {
        return value > -0.00000001 && value < 0.00000001;
    }
    
    /**
     * Has this module been enabled.
     * @param module The module to check
     * @return true if enabled
     */
    public static boolean isEnabled( String module)
    {
        String list;
        
        list = CProperties.getProperty(CProperties.PROPERTY_ENABLE,"");
        assert list!=null;
        if( list.contains(","))
        {
            StringTokenizer st = new StringTokenizer( list, ",");
            
            while( st.hasMoreTokens())
            {
                String item = st.nextToken();
                
                if( item.equalsIgnoreCase( module))
                {
                    return true;
                }
            }
        }
        else
        {
            if( list.equalsIgnoreCase( module))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @return the value
     */
    public static boolean isWindose( )
    {
        return !CProperties.isUnix();
    }
   
    /**
     * 
     * @param baseComponent base component
     * @param componentToCenter the component to center
     */
    public static void center(final Component baseComponent, final Component componentToCenter)
    {
        Dimension size = null;
        Point where = null;
        
        if( baseComponent != null )
        {
            size    = baseComponent.getSize();
            where   = baseComponent.getLocation();
            
            // The component must not be visable.
            if( size.width == 0) 
            {
                size = null;
            }
        }
        
        if( size == null)
        {
            size = Toolkit.getDefaultToolkit().getScreenSize();
            where = new Point( 0,0);
        }
        assert size!=null;
        assert where!=null;
        where.x += size.width / 2;
        where.y += size.height / 2;
        where.x -= componentToCenter.getSize().width / 2;
        where.y -= componentToCenter.getSize().height / 2;
        
        componentToCenter.setLocation( where.x, where.y);
    }
    
    /**
     * Get a named icon from the current jar file.
     * 
     * @return The ImageIcon object containing the icon data
     * @param base the base object
     * @param path The path to the icon.
     * @param description The icon's tooltip
     */
    public static ImageIcon makeImageIcon(Object base, String path, String description)
    {
        try
        {
            InputStream is = base.getClass().getResourceAsStream(path);
            
            if( is == null)
            {
                LOGGER.warn("Could not find icon:" + path );
                return null;
            }
            
            return new ImageIcon( toByteArray(is), description );
        }
        catch( Exception e )
        {
            LOGGER.error("Icon load failure: " + path, e );
        }
        
        return null;
    }
    
    /**
     * Convert a Java InputStream to a byte array.
     * @param is InputStream to convert to a byte array
     * @return byte[]   The data from the InputStream as byte array
     * @throws IOException A serious problem
     */
    public static byte[] toByteArray(InputStream is)
    throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] chunk = new byte[10000];
        while (true)
        {
            int bytesRead = is.read(chunk, 0, chunk.length);
            if (bytesRead <= 0)
            {
                break;
            }
            output.write(chunk, 0, bytesRead);
        }
        return output.toByteArray();
    }
    
    /**
     * 
     * @param image 
     * @return the value
     */
    public static BufferedImage makeBufferedImage( Image image)
    {
        if( waitForImage( image) == false) 
        {
            return null;
        }
        
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth( null),
                image.getHeight( null),
                BufferedImage.TYPE_INT_RGB
                );
        
        Graphics2D g2 = bufferedImage.createGraphics();
        //g2.setBackground( new Color(invisibleColor));
        g2.drawImage( image, null, null);
        return bufferedImage;
    }
    
    private static synchronized int createImageID( )
    {
        if( TRACKER == null)//NOPMD
        {
            TRACKER = new MediaTracker( new Component()
            {});
        }
        
        int id;
        
        id = ++sID;
        if( sID == Integer.MAX_VALUE - 1) 
        {
            sID = 1;
        }

        return id;
    }
    
    /**
     * 
     * @param image 
     * @return the value
     */
    public static boolean waitForImage( final Image image)
    {
        if( image == null) 
        {
            return false;
        }

        int id = createImageID();
        
        TRACKER.addImage( image, id);
        
        try
        { 
            TRACKER.waitForID( id); 
            if( TRACKER.isErrorID( id))
            {
                StringBuilder buffer = new StringBuilder();

                Object list[] = TRACKER.getErrorsID(id);

                for( int i = 0; i < list.length; i++)
                {
                    if( i != 0) 
                    {
                        buffer.append( "\n");
                    }

                    buffer.append( list[i]);
                }

                LOGGER.error(
                    "waitForImage - image had error (id:" + id + ") " + buffer
                );
                return false;
            }
        }
        catch( InterruptedException ie)
        {
            LOGGER.error( "waitForImage - thread interupted", ie);
            return false;
        }
        finally
        {
            TRACKER.removeImage( image);
        }
        
        return true;
    }
    
    
   /**
     * 
     * @return the value
     */
    public static boolean isSystemOnline()
    {
        if( isOnline) return true;
        
        String testURL = "https://www.google.com";

        long now=System.currentTimeMillis();
        if (now < lastOnlineChecked + 30 * 60 * 1000)
        {
             return false;
        }
        else
        {
            lastOnlineChecked = now;
            isOnline = checkWebsiteURL(testURL);
        }
        
        return isOnline;
    }
    
    public static boolean checkWebsiteURL(final String websiteUrl)
    {
//SERVER-START        
        try 
        {
            if (StringUtilities.notBlank(websiteUrl))
            {                
                if( websiteUrl.toLowerCase().matches("http(s|)://.*\\..*"))
                {
                    if( ReSTUtil.validateURL(websiteUrl))
                    {
                        Response rr=ReST
                            .builder(websiteUrl)
                            .setErrorCachePeriod("1 hour")
                            .setMinCachePeriod("3 month")
                            .setMaxBlockPeriod("15 SECS")
                            .getResponse();

                        String status =rr.checkStatus();
                        LOGGER.info( status + " status for website: " + websiteUrl );
                        return true;
                    }
                    else{
                        LOGGER.info( "Invalid website: " + websiteUrl );
                        return false;
                    }
                }
                else
                {
                    LOGGER.warn( "not a valid website: " + websiteUrl);
                }
            }
        }
        catch (FileNotFoundException | MalformedURLException | InvalidDataException | ReSTException e)
        {
            LOGGER.warn("Check URL " + websiteUrl, e);
        }
//SERVER-END        
        return false; 
    }

    private static MediaTracker TRACKER;//NOPMD
    
    private static int sID;//MT CHECKED
    private static boolean isOnline;
    private static long lastOnlineChecked;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.CUtilities");//#LOGGER-NOPMD
}
