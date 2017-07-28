/**
 * STS Remote library
 *
 * Copyright (C) 2006 stSoftware Pty Ltd
 *
 * stSoftware.com.au
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Bug fixes, suggestions and comments should be sent to:
 *
 * info AT stsoftware.com.au
 *
 * or by snail mail to:
 *
 * stSoftware building C, level 1, 14 Rodborough Rd Frenchs Forest 2086
 * Australia.
 */
package com.aspc.remote.launcher;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

/**
 * Launch the application
 *
 * <br> <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 * @author Jason McGath
 * @since 24 May 2009
 */
public class ExtractApp
{
    private static boolean autoMode;
    /**
     * process the program
     */
    @SuppressWarnings({"SleepWhileInLoop", "UseOfSystemOutOrSystemErr"})
    public void process()
    {
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        PrintStream out = System.out;
        
        String user = System.getProperty("user.name");
        if (user!=null && user.equalsIgnoreCase("root"))
        {
            out.println("Can't run as root");
            System.exit(1);
        }

        byte array[] = new byte[100 * 1024];

        try
        {
            String base=null;
            String excludePattern="";

            String writablePattern="";
            String executablePattern="";
            Enumeration<URL> systemResources = ClassLoader.getSystemResources("META-INF/MANIFEST.MF");
            while( systemResources.hasMoreElements())
            {
                URL manifestURL = systemResources.nextElement();

                try
                (InputStream in = manifestURL.openStream()) {
                    Manifest manifest = new Manifest(in);

                    Attributes mainAttributes = manifest.getMainAttributes();
                    String value = mainAttributes.getValue("base-dir");
                    if (value != null)
                    {
                        base=value;
                    }
                    value = mainAttributes.getValue("exclude-pattern");
                    if (value != null)
                    {
                        excludePattern=value;
                    }

                    value = mainAttributes.getValue("writable-pattern");
                    if (value != null)
                    {
                        writablePattern=value;
                    }
                    value = mainAttributes.getValue("executable-pattern");
                    if (value != null)
                    {
                        executablePattern=value;
                    }
                }
                if( base != null) break;
            }
            
            File baseDir = new File(base);
            if (baseDir.exists() == false)
            {
                baseDir.mkdirs();

                String extractorClassName = getClass().getName().replace(".", "/") + ".class";

                URL resource = ClassLoader.getSystemResource(extractorClassName);

                String urlStr = resource.toString();
                int from = "jar:file:".length();
                int to = urlStr.indexOf("!/");
                File jarFile = new File( URLDecoder.decode(urlStr.substring(from, to), "utf-8"));//.replace(" ", "\\ "));
                out.println("Jar file: " + jarFile);

                java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);

                Enumeration<JarEntry> e = jar.entries();

                while (e.hasMoreElements())
                {
                    JarEntry entry = e.nextElement();
                    String name = entry.getName();
                    File target = new File(baseDir, name);
                    // if the entry is not directory and matches relative file then extract it
                    if (name.matches(excludePattern))
                    {
                        continue;
                    }

                    if (name.endsWith("/"))
                    {
                        target.mkdirs();
                    }
                    else
                    {
                        target.getParentFile().mkdirs();
                        try (
                            java.io.InputStream is = jar.getInputStream(entry); 
                            java.io.FileOutputStream fos = new java.io.FileOutputStream(target)) {
                            while (is.available() > 0)
                            {  // write contents of 'is' to 'fos'
                                int len = is.read(array);
                                fos.write(array, 0, len);
                            }
                        }
                    }

                    target.setLastModified(entry.getTime());
                    if( target.isFile())
                    {
                        target.setReadable(false, false);
                        target.setWritable(false, false);
                        target.setExecutable(false, false);

                        target.setReadable(true);
                        if( name.matches(writablePattern))
                        {
                            target.setWritable(true);
                        }
                        if( name.matches(executablePattern))
                        {
                            target.setExecutable(true);
                        }
                    }
                }
            
            }
            else if (autoMode == false)
            {
                throw new Exception("target " + baseDir + " exists!");
            }
            
            if( autoMode)
            {
                String cmd = System.getProperty("java.home") + "/bin/java -jar " + baseDir +"/launcher.jar install";
                
                final Process process = Runtime.getRuntime().exec(cmd);
                InputStream in = process.getInputStream();
                InputStream err = process.getErrorStream();
                                
                while( true)
                {
                    Thread.sleep(100);
                    if( err.available()>0)
                    {
                        byte[] a=new byte[2048];
                        int len = err.read(a);

                        if( len > 0)
                        {
                            String s = new String( a);
                            System.err.print(s);
                        }
                    }
                    else if( in.available()>0)
                    {
                        byte[] a=new byte[2048];
                        int len = in.read(a);

                        if( len > 0)
                        {
                            String s = new String( a);
                            out.print(s);
                        }
                    }
                    else
                    {
                        try
                        {
                            process.exitValue();
                            if( in.available()==0)
                            {
                                break;
                            }
                        }
                        catch( IllegalThreadStateException itse)
                        {
                            
                        }
                    }
                }
                
                int status = process.exitValue();
                if( status != 0)
                {
                    out.println(  "FAILED: " + cmd);
                    System.exit(status);
                }
            }
        } 
        catch (Exception e)
        {
            out.println(e.toString());
            e.printStackTrace(out);
            System.exit(1);
        } finally
        {
            out.flush();
        }
    }

    /**
     * The main for the program
     *
     * @param argv The command line arguments
     */
    public static void main(String argv[])
    {
        if( argv != null)
        {
            for( String arg: argv)
            {
                if(arg.equalsIgnoreCase("-auto"))
                {
                    autoMode = true;
                }
            }
        }
        new ExtractApp().process();
    }
}
