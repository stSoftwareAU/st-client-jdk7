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

import com.aspc.remote.database.InvalidDataException;
//SERVER-START
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import com.aspc.remote.rest.internal.ReSTUtil;
//SERVER-END
import org.apache.commons.logging.Log;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.StringTokenizer;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @CheckReturnValue @Nonnull
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
                assert applicationName!=null;
                applicationName = applicationName.replace("com.aspc.", "");

                applicationName = applicationName.replace( ".StartUp", "");
            }
        }
        assert applicationName!=null;
        
        return applicationName;
    }
        
    /**
     * Return the application name
     *
     * @return the name.
     */
    @CheckReturnValue @Nonnull
    public static String getAppName()
    {
        String applicationName = CProperties.getProperty( PROPERTY_APP_SHORTNAME);
        if(applicationName==null|| StringUtilities.isBlank(applicationName))
        {
            return "";
        }
        return applicationName;
    }
    
    /**
     * Return the application name
     *
     * @return the name.
     */
    @CheckReturnValue @Nonnull
    public static String getAreaName()
    {
        String area = CProperties.getProperty( PROPERTY_AREA_NAME);
        if(area==null|| StringUtilities.isBlank(area))
        {
            return "";
        }
        return area;
    }
    
    /**
     * HTTP port of the server 
     * @return port as String
     *
    @CheckReturnValue @Nonnull
    public static String getHTTPPort()
    {
        String port = System.getProperty(PROPERTY_PORT);
        return port;
    }
    */
    /**
     * HTTPS port of the server if SSL is enabled 
     * @return port as String
     */
    @CheckReturnValue @Nullable
    public static String getHTTPSPort()
    {
        String port = System.getProperty(PROPERTY_SSLPORT);
        return port;
    }
    
    /**
     * Host name on which server is running.
     * @return Host name as String
     */
    @CheckReturnValue @Nullable
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
    @CheckReturnValue
    public static boolean isZero( double value)
    {
        return value > -0.00000001 && value < 0.00000001;
    }
    
    /**
     * Has this module been enabled.
     * @param module The module to check
     * @return true if enabled
     */    
    @CheckReturnValue
    public static boolean isEnabled( final @Nullable String module)
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
    @CheckReturnValue
    public static boolean isWindose( )
    {
        return !CProperties.isUnix();
    }
   
    /**
     * 
     * @param baseComponent base component
     * @param componentToCenter the component to center
     */
    public static void center(final @Nullable Component baseComponent, final @Nonnull Component componentToCenter)
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
    @CheckReturnValue @Nullable
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
        catch( IOException e )
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
    @CheckReturnValue @Nonnull
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
    @CheckReturnValue @Nullable
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
    @CheckReturnValue
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
    @CheckReturnValue
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
    @CheckReturnValue
    public static boolean checkWebsiteURL(final String websiteUrl)
    {
        return checkWebsiteURL(websiteUrl, "15 Secs", "1 hour");
    }
        //SERVER-START        

    @CheckReturnValue @Nonnull
    public static Status checkURL(final String websiteUrl, final String maxBlockPeriod, final String errorCachePeriod)
    {

        if (StringUtilities.notBlank(websiteUrl))
        {                
            if( websiteUrl.toLowerCase().matches("http(s|)://.*\\..*"))
            {
                String tmpURL=websiteUrl;
                int anchorPos = tmpURL.indexOf("#");
                if( anchorPos!=-1)
                {
                    tmpURL=tmpURL.substring(0, anchorPos);
                }
                if( ReSTUtil.validateURL(tmpURL))
                {
                    Response rr;
                
                    try{
                        rr=ReST
                            .builder(tmpURL)
                            .setErrorCachePeriod(errorCachePeriod)
                            .setMinCachePeriod("3 month")
                            .setAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                            .setMaxBlockPeriod(maxBlockPeriod)
                            .getResponse();
                    }
                    catch( InvalidDataException | MalformedURLException e)
                    {
                        LOGGER.warn("invalid URL: " + websiteUrl,e);
                        
                        return Status.C400_ERROR_BAD_REQUEST;
                    }

                    Status status = rr.status;

                    if( status.isError())
                    {
                        LOGGER.info( status + ") " + websiteUrl );
                    }

                    return status;
                }
                else{
                    LOGGER.info( "Invalid URL: " + websiteUrl );
                    return Status.C400_ERROR_BAD_REQUEST;
                }
            }
            else
            {
                LOGGER.warn( "Invalid URL: " + websiteUrl);
            }
        }

        return Status.C400_ERROR_BAD_REQUEST;
    }
//SERVER-END        

    
    @CheckReturnValue
    public static boolean checkWebsiteURL(final String websiteUrl, final String maxBlockPeriod, final String errorCachePeriod)
    {
        //SERVER-START        
        Status status=checkURL(websiteUrl, maxBlockPeriod, errorCachePeriod);
        
        if( status.isError())
        {
            return false;
        }
//SERVER-END        
        return true;
    }

    private static MediaTracker TRACKER;//NOPMD
    
    private static int sID;//MT CHECKED
    private static boolean isOnline;
    private static long lastOnlineChecked;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.CUtilities");//#LOGGER-NOPMD
}
