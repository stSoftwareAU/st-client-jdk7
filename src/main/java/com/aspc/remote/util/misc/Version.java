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

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.*;
import org.apache.commons.logging.Log;

/**
 *  Version handler
 *
 *  <br>
 *  <i>THREAD MODE: Mutable</i>
 *
 *  @author      Nigel Leck
 *  @since       2 July 1997
 */
public class Version extends VersionENV
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.Version");//#LOGGER-NOPMD
    private final AtomicInteger programVersion=new AtomicInteger();
    private static final ThreadLocal<HashSet<Version>> CHANGED_VERSIONS=new ThreadLocal();
    private final ThreadLocal<AtomicInteger> threadVersion=new ThreadLocal();
    
    /**
     * create a new version
     *
     * @param environmentName the environment variable name.
     * @param defaultVersion the default version to use
     * @param minVersion minimum version
     * @param maxVersion maximum version
     */
    public Version( final @Nonnull String environmentName, final @Nonnegative int defaultVersion, final @Nonnegative int minVersion, final @Nonnegative int maxVersion)
    {
        super( environmentName, defaultVersion, minVersion, maxVersion);
    }

    /**
     * Purify the thread versions
     */
    public static void purifyThreadVersion()
    {
        HashSet<Version> set = CHANGED_VERSIONS.get();
        if( set != null)
        {
            HashSet<Version>set2=(HashSet<Version>) set.clone();
            for( Version v: set2)
            {
                v.setThreadVersion(0);
            }
        }
    }

    /**
     * set the version
     * @param version the version, ZERO means default
     * @return the previous version
     */
    public int setThreadVersion( final @Nonnegative int version)
    {
        int tmpVersion=version;
        if( tmpVersion != 0)
        {
            if( tmpVersion < minVersion)
            {
                String msg=environmentName + " THREAD version must not be less than " + minVersion + " was " + tmpVersion;
                assert false: msg;
                
                LOGGER.warn( msg );
                tmpVersion=minVersion;
            }
            else if( tmpVersion > maxVersion)
            {
                if( tmpVersion != Integer.MAX_VALUE)
                {
                    String msg=environmentName + " THREAD version must not be greater than " + maxVersion + " was " + tmpVersion;
                    assert false: msg;
                    LOGGER.warn( msg );
                }
                tmpVersion=maxVersion;
            }

            String deprecateMessage=deprecatedVersionMap.get(tmpVersion);
            if( deprecateMessage != null)
            {
                if( ALLOW_DEPRECATED_VERSION)
                {
                    LOGGER.warn( environmentName + " THREAD version " + tmpVersion + " is DEPRECATED: " + deprecateMessage );
                }
                else
                {
                    LOGGER.warn( environmentName + " THREAD version " + tmpVersion + " is marked as deprecated and IGNORED: " + deprecateMessage );

                    return 0;
                }
            }
        }
        int previousVersion=0;

        if( tmpVersion == 0)
        {
            AtomicInteger ai = threadVersion.get();
            if( ai != null)
            {
                previousVersion=ai.get();
            }

            threadVersion.remove();

            HashSet<Version> set = CHANGED_VERSIONS.get();
            if( set != null)
            {
                set.remove(this);
            }
        }
        else
        {
            AtomicInteger ai = threadVersion.get();
            if( ai == null)
            {
                ai=new AtomicInteger(tmpVersion);
                threadVersion.set(ai);
                HashSet<Version> set = CHANGED_VERSIONS.get();
                if( set == null)
                {
                    set = new HashSet();
                    CHANGED_VERSIONS.set( set);
                }

                set.add(this);
            }
            else
            {
                previousVersion=ai.getAndSet(tmpVersion);
            }
        }

        return previousVersion;
    }

    /**
     * set the version
     * @param version the version, ZERO means default
     * @return the previous version
     */
    public int setProgramVersion( final @Nonnegative int version)
    {
        int tmpVersion=version;
        if( tmpVersion != 0)
        {
            if( tmpVersion < minVersion)
            {
                LOGGER.warn( environmentName + " PROGRAM version must not be less than " + minVersion + " was " + tmpVersion );
                tmpVersion=minVersion;
            }
            else if( tmpVersion > maxVersion)
            {
                if( tmpVersion != Integer.MAX_VALUE)
                {
                    LOGGER.warn( environmentName + " PROGRAM version must not be greater than " + maxVersion + " was " + tmpVersion );
                }
                tmpVersion=maxVersion;
            }

            String deprecateMessage=deprecatedVersionMap.get(tmpVersion);
            if( deprecateMessage != null)
            {
                if( ALLOW_DEPRECATED_VERSION)
                {
                    LOGGER.warn( environmentName + " PROGRAM version " + tmpVersion + " is DEPRECATED: " + deprecateMessage );
                }
                else
                {
                    LOGGER.warn( environmentName + " PROGRAM version " + tmpVersion + " is marked as deprecated and IGNORED: " + deprecateMessage );

                    return programVersion.get();
                }
            }
        }
        int previousVersion = programVersion.getAndSet(tmpVersion);

        return previousVersion;
    }

    /**
     * Calculate the current version number to use.
     *
     * Thread or default version CAPPED by the Program->Environment->Database version number.
     *
     * @return the value
     */
    @Override @CheckReturnValue
    public int calculateVersion()
    {
        int tmpMax = programVersion.get();

        if( tmpMax < 1)
        {
            tmpMax = environmentVersion.get();

            if( tmpMax < 1)
            {
                tmpMax = databaseVersion.get();
            }
        }

        int tv=0;
        AtomicInteger ai = threadVersion.get();
        if( ai != null)
        {
            tv=ai.get();
        }

        if( tmpMax > 0)
        {
            if( tv > 0)
            {
                if( tv < tmpMax)
                {
                    return tv;
                }
            }

            return tmpMax;
        }

        if( tv > 0)
        {
            return tv;
        }

        return defaultVersion;
    }
}
