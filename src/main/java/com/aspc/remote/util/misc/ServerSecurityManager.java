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

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.PropertyPermission;
import org.apache.commons.logging.Log;

/**
 *  Don't all Runtime.exec to be call from within the server.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       2 Aug 2007
 */
public final class ServerSecurityManager extends SecurityManager
{
   // private boolean throwExceptionOnRuntimeDotExec;

    private static final int MODE_NONE=0;
    private static final int MODE_SCRIPT=1;
    private static final int MODE_XLST=2;
    
    private static final ThreadLocal<Integer>USER_ACCESS=new ThreadLocal()
    {
        @Override
        protected Object initialValue()
        {
            return 0;
        }
    };

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.ServerSecurityManager");//#LOGGER-NOPMD

    /**
     * throw an exception of Runtime.exec is called otherwise warn
     * @param flag turn on/off
     *
    public void setThrowExceptionOnRuntimeDotExec( final boolean flag)
    {
        throwExceptionOnRuntimeDotExec=flag;
    }
    */

    /**
     * turn on/off user script permission restrictions
     * @param flag turn on/off
     * @return the previous value.
     */
    public static boolean modeUserScriptAccess( boolean flag)
    {
        Integer current = USER_ACCESS.get();
        if( flag)
        {
            USER_ACCESS.set(MODE_SCRIPT);
        }
        else
        {
            USER_ACCESS.set(MODE_NONE);
        }
        
        return current== MODE_SCRIPT;
    }

    /**
     * turn on/off user script permission restrictions
     * @param flag turn on/off
     * @return the previous value.
     */
    public static boolean modeUserXlstAccess( boolean flag)
    {
        Integer current = USER_ACCESS.get();
        if( flag)
        {
            USER_ACCESS.set(MODE_XLST);
        }
        else
        {
            USER_ACCESS.set(MODE_NONE);
        }
        
        return current== MODE_XLST;
    }

    /**
     * We never want to allow the server to call Runtime.exec() as it doubles the
     * memory required by the server and causes massive performance issues.
     *
    @Override
    public void checkExec(String cmd)
    {
        SecurityException mayNeverBeCalledByServer= new SecurityException( "can not call Runtime.exec (" + cmd + ")");
        LOGGER.fatal( "disabled by the ServerSecurityManager", mayNeverBeCalledByServer);

        if( throwExceptionOnRuntimeDotExec)
        {
            throw mayNeverBeCalledByServer;
        }
    }*/

    /**
     * All other permissions are allowed.
     */
    @Override
    public void checkPermission(final Permission perm)
    {
        Integer current = USER_ACCESS.get();
        if( current == MODE_SCRIPT)
        {
            boolean lowSecurity=false;

            if( perm instanceof FilePermission)
            {
                FilePermission fp=(FilePermission)perm;

                if( perm.getActions().equals("read"))
                {
                    String name = fp.getName();

                    if( name.endsWith("/classes")|| name.endsWith(".class")|| name.endsWith(".jar")||name.contains("org/mozilla/")||name.contains("org.apache.xml.serializer."))
                    {
                        lowSecurity=true;
                    }
                }
            }
            else if( perm instanceof PropertyPermission)
            {
                if( perm.getActions().equals("read"))
                {
                   // String name = perm.getName();

                 //   if( name.endsWith(".class"))
                 //   {
                        lowSecurity=true;
                 //   }
                }
            }
            else if( perm instanceof ReflectPermission)
            {
                lowSecurity=true;
            }
            else if( perm instanceof RuntimePermission)
            {
                String name=perm.getName();
                if(
                    name.equalsIgnoreCase("nashorn.createGlobal") ||
                    name.equals("createClassLoader")||
                    name.startsWith("accessClassInPackage.sun.reflect") ||
                    name.startsWith("accessClassInPackage.jdk.nashorn.") ||
                    name.startsWith("accessClassInPackage.sun.text.")||
                    name.startsWith("accessClassInPackage.sun.util.resources")||
                    name.startsWith("accessClassInPackage.jdk.internal.org.objectweb.asm")||
                    name.equals("accessDeclaredMembers")
                )
                {
                    lowSecurity=true;
                }
            }
            else
            {
                String name =perm.getName();

                if(
                    name.startsWith("accessClassInPackage.sun.org.mozilla.javascript.")||
                    name.equals("specifyStreamHandler")
                )
                {
                    lowSecurity=true;
                }

            }

            if( lowSecurity == false)
            {
                String msg="may not call from user script (perm: " + perm + " name: " + perm.getName() + " action: " + perm.getActions() + ")";
                LOGGER.warn( "SECURITY: " + msg);
                SecurityException mayNotCallFromUserScript= new SecurityException( msg);
                throw mayNotCallFromUserScript;
            }
        }
        else if( current == MODE_XLST)
        {
            boolean lowSecurity=false;

            if( perm instanceof FilePermission)
            {
                FilePermission fp=(FilePermission)perm;

                if( perm.getActions().equals("read"))
                {
                    String name = fp.getName();

                    if( 
                        name.endsWith("javax.xml.transform.TransformerFactory") ||
                        (
                            (
                                name.contains("xerces")
                            ) && name.endsWith(".jar")
                        ) ||
                        name.endsWith("xalan.properties")||
                        name.contains("org/apache/xml/serializer")||
                        name.endsWith("xerces.properties")||
                        name.endsWith("currency.data")||
                        name.endsWith("org.apache.xml.dtm.DTMManager")||                            
                        name.endsWith("currency.properties")||
                        name.endsWith(".class") ||
                        name.endsWith("classes") ||
                        name.endsWith("ext") ||
                        name.endsWith(".jar") ||
                        name.endsWith("org.apache.xerces.xni.parser.XMLParserConfiguration")
                    )
                    {
                        lowSecurity=true;
                    }                   
                }
            }
            else if( perm instanceof PropertyPermission)
            {
                if( perm.getActions().equals("read"))
                {
                    String name = perm.getName();

                    if( 
                        name.equals("line.separator")||
                        name.equals("media-type")||
                        name.equals("version")||
                        name.equals("org.apache.xml.dtm.DTMManager")||
                        name.equals("user.dir")||
                        name.equals("standalone")||
                        name.equals("encoding")||
                        name.equals("omit-xml-declaration")||
                        name.equals("method")||
                        name.equals("indent")||
                        name.startsWith("javax.xml.") || 
                        name.startsWith("java.home") || 
                        name.equals("file.separator") || 
                        name.equals("path.separator") || 
                        name.contains(".class.path") || 
                        name.contains("org.apache.xalan")||
                        name.contains("org/apache/xml") ||
                        name.contains("xml.apache.org")||
                        name.contains("java.ext.dirs")||
                        name.contains("JavaClass.debug")||
                        name.contains("org.apache.xerces")
                    )
                    {
                        lowSecurity=true;
                    }
                }
            }            
            else if( perm instanceof RuntimePermission)
            {
                String name=perm.getName();
                if(
                    name.equals("readFileDescriptor")||
                    name.equals("writeFileDescriptor")||
                    name.equals("accessClassInPackage.sun.text.resources")||
                    name.equals("getClassLoader")||
                    name.equals("createClassLoader")
                )
                {
                    lowSecurity=true;
                }
            }
            else if( perm instanceof ReflectPermission)
            {
                lowSecurity=true;
            }
            
            if( lowSecurity == false)
            {
                String msg="may not call from user script (perm: " + perm + " name: " + perm.getName() + " action: " + perm.getActions() + ")";
                LOGGER.error( "SECURITY: " + msg);
                SecurityException mayNotCallFromUserScript= new SecurityException( msg);
                throw mayNotCallFromUserScript;
            }
        }
    }

    /**
     * All other permissions are allowed.
     */
    @Override
    public void checkPermission(Permission perm, Object context)
    {
        checkPermission(perm);
    }

    static
    {
        LOGGER.info( "Server Security Manager $Revision: 1.22 $ ");//#NOSYNC
    }
}
