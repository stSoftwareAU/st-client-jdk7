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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.*;
import org.apache.commons.logging.Log;

/**
 *  Version ( ENV)
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       2 July 2010
 */
@SuppressWarnings("AssertWithSideEffects")
public class VersionENV
{
    public final int defaultVersion;
    public final int minVersion;
    public final int maxVersion;
    protected final AtomicInteger databaseVersion=new AtomicInteger();
    protected final AtomicInteger environmentVersion=new AtomicInteger();
    protected final String environmentName;
    protected final ConcurrentHashMap<Integer, String>deprecatedVersionMap=new ConcurrentHashMap();
    protected static final boolean ALLOW_DEPRECATED_VERSION;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.VersionENV");//#LOGGER-NOPMD

    /**
     * create a new version
     *
     * @param environmentName the environment variable name.
     * @param defaultVersion the default version to use
     * @param minVersion minimum version
     * @param maxVersion maximum version
     */    
    public VersionENV( final @Nonnull String environmentName, final @Nonnegative int defaultVersion, final @Nonnegative int minVersion, final @Nonnegative int maxVersion)
    {
        this.environmentName=environmentName;
        if( minVersion < 1)
        {
            LOGGER.warn( environmentName + " minimum version must be greater than zero was " + minVersion);
            this.minVersion=1;
        }
        else
        {
            this.minVersion=minVersion;
        }

        if( maxVersion < 1 || maxVersion < minVersion)
        {
            LOGGER.warn( environmentName + " maximum version must be greater than zero and not less than the minimum version was " + maxVersion );
            this.maxVersion=minVersion;
        }
        else
        {
            this.maxVersion=maxVersion;
        }

        if( defaultVersion>maxVersion)
        {
            LOGGER.warn( environmentName + " default version " + defaultVersion + " more than MAX " + maxVersion );
            this.defaultVersion=maxVersion;
        }
        else if( defaultVersion< minVersion)
        {
            LOGGER.warn( environmentName + " default version " + defaultVersion + " less than MIN " + minVersion );
            this.defaultVersion=minVersion;
        }
        else
        {
            this.defaultVersion=defaultVersion;
        }

        String temp;

        temp = System.getProperty( environmentName, "0");

        int tmpVersion=Integer.parseInt(temp);
        if( tmpVersion != 0 && tmpVersion < minVersion)
        {
            LOGGER.warn( environmentName + " ENVIROMENT version must not be less than " + minVersion + " was " + tmpVersion );
            tmpVersion=minVersion;
        }
        else if( tmpVersion > maxVersion)
        {
            if( tmpVersion != Integer.MAX_VALUE)
            {
                LOGGER.warn( environmentName + " ENVIROMENT version must not be greater than " + maxVersion + " was " + tmpVersion );
            }
            tmpVersion=maxVersion;
        }

        environmentVersion.set(tmpVersion);
    }

    @Override @CheckReturnValue @Nonnull
    public String toString() 
    {
        int dbVersion=databaseVersion.get();
        int envVersion=environmentVersion.get();
        String values=" " + defaultVersion;
        if( dbVersion > 0 || envVersion>0)
        {
            values=" default:" + defaultVersion +","; 
            
            if( dbVersion>0)
            {
                values+="db:" + dbVersion;
                if( envVersion>0) values+=",";
            }
            if( envVersion>0)
            {
                values+="env:" + envVersion;
            }
        }
        return environmentName + values + " (" + minVersion +"..." + maxVersion +") =" + calculateVersion();
    }

    /**
     * Do not allow this version to be used in production
     * @param version The version number
     * @param message The reason message.
     */
    public void deprecateVersion( final @Nonnegative int version, final @Nonnull String message)
    {
        if( version != 0)
        {
            deprecatedVersionMap.put(version, message);

            if( version == environmentVersion.get())
            {
                if( ALLOW_DEPRECATED_VERSION)
                {
                    LOGGER.warn( environmentName + " ENVIROMENT version " + version + " is DEPRECATED: " + message );
                }
                else
                {
                    LOGGER.warn( environmentName + " ENVIROMENT version " + version + " is marked as deprecated and IGNORED: " + message );

                    environmentVersion.set(0);
                }
            }
        }
    }
    
    /**
     * set the version
     * @param version the version, ZERO means default
     * @return the previous version
     */
    public int setDataBaseVersion( final @Nonnegative int version)
    {
        int tmpVersion=version;
        if( tmpVersion != 0)
        {
            if( tmpVersion < minVersion)
            {
                LOGGER.warn( environmentName + " DATABASE version must not be less than " + minVersion + " was " + tmpVersion );
                tmpVersion=minVersion;
            }
            else if( tmpVersion > maxVersion)
            {
                if( tmpVersion != Integer.MAX_VALUE)
                {
                    LOGGER.warn( environmentName + " DATABASE version must not be greater than " + maxVersion + " was " + tmpVersion );
                }
                tmpVersion=maxVersion;
            }

            String deprecateMessage=deprecatedVersionMap.get(tmpVersion);
            if( deprecateMessage != null)
            {
                if( ALLOW_DEPRECATED_VERSION)
                {
                    LOGGER.warn( environmentName + " DATABASE version " + tmpVersion + " is DEPRECATED: " + deprecateMessage );
                }
                else
                {
                    LOGGER.warn( environmentName + " DATABASE version " + tmpVersion + " is marked as deprecated and IGNORED: " + deprecateMessage);

                    return databaseVersion.get();
                }
            }
        }
        
        int previousVersion = databaseVersion.getAndSet(tmpVersion);

        return previousVersion;
    }

    /**
     * Calculate the current version number to use.
     *
     * Thread or default version CAPPED by the Program->Environment->Database version number.
     *
     * @return the value
     */
    @CheckReturnValue
    public int calculateVersion()
    {
        int tmpEnv=environmentVersion.get();
        if( tmpEnv>0) return tmpEnv;

        int tmpDB = databaseVersion.get();

        if( tmpDB>0) return tmpDB;

        return defaultVersion;
    }
    
    static
    {
        boolean flag=false;
        assert flag=true;
        
        ALLOW_DEPRECATED_VERSION=flag;
    }
}
