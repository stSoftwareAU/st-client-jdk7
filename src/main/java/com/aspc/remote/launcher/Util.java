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
package com.aspc.remote.launcher;

import com.aspc.remote.tail.TailFile;
import com.aspc.remote.tail.TailQueue;
import com.aspc.remote.util.misc.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.input.InputHandler;

/**
 *  Handle ANT
 *
 * <br>
 * <i>THREAD MODE: SINGLETON MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       24 May 2009
 */
public final class Util
{
    /**
     * Launcher file property
     */
    public static final String PROPERTY_LAUNCHER_FILE="LAUNCHER_FILE";

    private static final String BIN_PATH = "bin/launcher.xml";
    private static final String CONF_PATH = "conf/launcher.xml";

    private Util()
    {
    }

    /**
     * List the targets
     * @return the targets
     * @throws Exception a serious problem.
     */
    public static Hashtable listTargets() throws Exception
    {
        File launcherFile = locateLauncher();

        Project p = new Project();

         /*
         * Setting basedir to the project folder.
         */
        File parentFile = launcherFile.getParentFile();
        LOGGER.info("Launcher Parent file: " + parentFile);
        String baseDir = "";
        if(parentFile != null)
        {
            baseDir = parentFile.getParent();
        }

        if(StringUtilities.isBlank(baseDir) == false)
        {
            LOGGER.info("Project base dir: " + baseDir);
            p.setBasedir(baseDir);// i.e "/home//./stServer/ as a base dir" not "//stserver/bin/"
        }

        p.fireBuildStarted();
        p.init();

        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, launcherFile);
        Hashtable targets = p.getTargets();

        int size = targets.size();
        String names[] = new String[size];
        targets.keySet().toArray(names);

        Hashtable tmp = new Hashtable();
        for( int loop =0; loop < 2; loop++)
        {
            for( String name: names)
            {
                Target t = (Target)targets.get(name);

                int pos = name.indexOf('.');
                if( pos != -1)
                {
                    if( loop == 0) continue;
                    name = name.substring(pos + 1);
                }
                if( tmp.containsKey(name) == false)
                {
                    tmp.put(name, t);
                }
            }
        }

        return tmp;
    }

    /**
     * process the program
     * @param target the target
     * @param consoleLogger the logger
     * @param tailQueue
     * @param inputHandler
     * @throws Exception a serious problem
     */
    public static void runTarget(
        final String target,
        final DefaultLogger consoleLogger,
        final TailQueue tailQueue,
        final InputHandler inputHandler
    ) throws Exception
    {
        runTarget(target, consoleLogger, tailQueue, inputHandler, null);
    }
    
    /**
     * 
     * @param target
     * @param consoleLogger
     * @param tailQueue
     * @param inputHandler
     * @param bl
     * @throws Exception 
     */
    public static void runTarget(
        final String target,
        final DefaultLogger consoleLogger,
        final TailQueue tailQueue,
        final InputHandler inputHandler,
        final BuildListener bl
    ) throws Exception
    {
        LOGGER.info( "runTarget " + target);

        /**
         * Search for launcher.xml file in bin folder (i.e, //../stServer/bin/launcher.xml).
         */
        File launcherFile = locateLauncher();
        System.getProperties().remove("LOG_PROPERTIES");
        Project p = new Project();
        if(bl != null)
        {
            p.addBuildListener(bl);
        }

        p.addBuildListener(consoleLogger);//to print the build result.

        try
        {
             /*
             * Setting basedir to the project folder.
             */
            File parentFile = launcherFile.getParentFile();
            LOGGER.info("Launcher Parent file: " + parentFile);
            String baseDir = "";
            if(parentFile != null)
            {
                baseDir = parentFile.getParent();
            }

            if(StringUtilities.isBlank(baseDir) == false)
            {
                LOGGER.info("Project base dir: " + baseDir);
                p.setBasedir(baseDir);// i.e "/home//./stServer/ as a base dir" not "//stserver/bin/"
            }

            p.fireBuildStarted();
            p.init();

            ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, launcherFile);

            // Check that the target exists
            if (!p.getTargets().containsKey(target))
            {
                throw new IllegalArgumentException(target + " invalid target");
            }

            LOGGER.info("Starting the Target: "+ target );

            String tmpTarget=target;
            if( StringUtilities.isBlank(tmpTarget))
            {
                tmpTarget = p.getDefaultTarget();
            }
            if( tailQueue != null)
            {
                String logDir=(String)p.getProperties().get( "log.dir");
                if( StringUtilities.isBlank(logDir)==false)
                {
                    String tmpName=tmpTarget.trim();
                    String tmp=(String)p.getProperties().get( "LOG_PROPERTIES");
                    if( StringUtilities.isBlank(tmp)==false)
                    {
                        int startPos = tmp.lastIndexOf("/");
                        if( startPos> 0)
                        {
                            int endPos = tmp.indexOf("_", startPos);
                            if( endPos >0)
                            {
                                tmpName=tmp.substring(startPos +1, endPos);
                            }
                        }
                    }
                    File logFile = new File( logDir +"/" + tmpName + ".log");

                    new TailFile( logFile, tailQueue).schedule();
                }
            }

            if( inputHandler != null)
            {
                p.setInputHandler(inputHandler);
            }
            p.executeTarget(tmpTarget);

            p.fireBuildFinished(null);
        }
        catch(Exception e)
        {
            p.fireBuildFinished(e);
            //throw e;
        }
    }

    /**
     * Get launcher mode
     *
     * @return true if launcher is launcher_remote
     * @throws Exception a serious problem
     */
    public static boolean isRemoteMode() throws Exception
    {
        File launcherFile = locateLauncher();
        String fileName = launcherFile.getName();
        if (fileName.contains("launcher_remote"))
        {
            return true;
        }
        return false;
    }

    /**
     * Get base dir
     *
     * @return base dir
     * @throws Exception a serious problem
     */
    public static String getBaseDir() throws Exception
    {
        File launcherFile = locateLauncher();
        File parentFile = launcherFile.getParentFile();
        String baseDir = "";
        if (parentFile != null)
        {
            baseDir = parentFile.getParent();
        }
        return baseDir;
    }

    /**
     * locate the file
     * @return the file.
     * @throws Exception a serious problem.
     */
    public static File locateLauncher()throws Exception
    {
        try
        {
            String launcherFile = System.getProperty(PROPERTY_LAUNCHER_FILE);
            if( StringUtilities.isBlank(launcherFile) == false)
            {
                File file = new File( launcherFile);

                if( file.exists())
                {
                    return file;
                }
            }

            LOGGER.info("Locating Launcher file in the path of : "+ BIN_PATH);

            String classResourceName = "/" + App.class.getName().replace('.', '/') + ".class";
            URL resource = App.class.getResource(classResourceName);
            if (resource == null)
            {
                throw new IOException("File not found" + ": " + App.class.getName());
            }

            // always look for /bin/launcher.xml. If find this pattern then use it.

            String resourcePath;
            String protocol = resource.getProtocol();
            boolean isJar = false;

            if(  protocol != null &&
                    protocol.contains("jar"))
            {
                isJar = true;
            }

            if(isJar)
            {
                resourcePath = URLDecoder.decode(resource.getFile(), "utf-8");
            }
            else
            {
                resourcePath = URLDecoder.decode(resource.toExternalForm(), "utf-8");
            }


            if (resourcePath.indexOf("file:") == 0)
                resourcePath = resourcePath.substring(5);

            File file = null;
            while(resourcePath.lastIndexOf("/") != -1)
            {
                resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf("/"));
                file = new File( resourcePath +"/"+ BIN_PATH);
                if (file.exists())
                {
                    File tmp = new File( resourcePath +"/"+ CONF_PATH);

                    if( tmp.exists() )
                    {
                        file = tmp;
                    }
                    break;
                }
            }

            LOGGER.info("Locating Launcher file in the path of : "+ "/com/aspc/Install/Common/support/"+ BIN_PATH);
            String srcDir = System.getProperty("SRC_DIR");
            if( file.exists() == false)
            {

                if( StringUtilities.isBlank(srcDir) == false)
                {
                     file = new File( srcDir +"/com/aspc/Install/Common/support/"+ BIN_PATH);
                }
            }

            LOGGER.info("Locating Launcher file in the path of : "+ "stRemote.jar");
            if (!file.exists() || !file.canRead())
            {
                file = new File(System.getProperty("user.dir") + "/bin", "launcher_remote.xml");
                file.delete();
                file.getParentFile().delete();
                if (!file.exists())
                {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                InputStream in = Util.class.getResourceAsStream("/com/aspc/remote/launcher_remote.xml");
                if (in.available() > 0)
                {
                    try (OutputStream out = new FileOutputStream(file)) {
                        int read;
                        byte[] bytes = new byte[1024];
                        
                        while ((read = in.read(bytes)) != -1)
                        {
                            out.write(bytes, 0, read);
                        }
                        in.close();
                        out.flush();
                    }
                    return file;
                } else
                {
                    throw new IOException("launcher file not found");
                }
            }

            return file.getCanonicalFile();
        }
        catch (Exception e)
        {
            throw new IOException("Unable to locate launcher.xml file: ", e);
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.launcher.Util");//#LOGGER-NOPMD
}
