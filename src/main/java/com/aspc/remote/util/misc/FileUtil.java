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

import com.aspc.remote.util.misc.internal.ImplFileValidationHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.*;
import javax.activation.FileTypeMap;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.*;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  FileUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author Luke
 * @since 17 September 1998
 */
public final class FileUtil
{
    public static final String MIME_APPLICATION_OCTET_STREAM="application/octet-stream";

    /**
     * The document cache directory. If not set then we will use CACHE_DIR.
     */
    public static final String DOC_CACHE="DOC_CACHE";
    
    /*
     * Encrypted file system on Linux have a maximum safe length of 143 characters. 255 for non-encrypted file systems. 
     *
     * https://bugs.launchpad.net/ecryptfs/+bug/344878
     */
    public static final int MAX_FILE_NAME_LENGTH=143;
    
    /**
     * The cache directory. 
     */
    public static final String CACHE_DIR="CACHE_DIR";
    
    private static final ThreadLocal<AtomicBoolean>CALLING_COPY=new ThreadLocal()
    {
        @Override
        protected Object initialValue() {
            return new AtomicBoolean( false);
        }
        
    };

    private FileUtil()
    {
    }

    /**
     *
     * @param file
     * @return the mime type
     */
    //SERVER-START
    @CheckReturnValue @Nonnull
    public static String guessMimeType( final @Nonnull File file)
    {
        String fn = file.getName();

        int pos = fn.lastIndexOf('#');

        if(pos == fn.length() - 1)
        {
            pos = fn.substring(0, pos).lastIndexOf('#');
        }
        String type=null;

        if( pos != -1)
        {
            String tmpType = StringUtilities.decode(fn.substring(pos + 1));
            if( tmpType.matches("[a-zA-Z0-9]+/[a-zA-Z0-9]+"))
            {
                type=tmpType;
            }
        }
        
        if(type ==null)
        {
            FileTypeMap map;

            map = MimetypesFileTypeMap.getDefaultFileTypeMap();
            type = map.getContentType(file);

            type = adjustMimeType(type, file, null);
        }

        return type;
    }
    //SERVER-END

    /**
     * Adjust the mime types
     * There seems to be some confusion with blackberry needed a different mime type for word.
     * see <a href="http://filext.com/faq/office_mime_types.php">http://filext.com/faq/office_mime_types.php</a>
     *
     * @param orginalMimeType the original mime type
     * @param file the file
     * @param browser The browser
     * @return the new mime type
     */
    //SERVER-START
    @CheckReturnValue @Nonnull
    public static String adjustMimeType( final @Nullable String orginalMimeType, final @Nonnull File file, final @Nullable com.aspc.remote.html.ClientBrowser browser)
    {
        String type = orginalMimeType;
        if( type == null) type="";
        int pos = type.indexOf(';');
        if( pos != -1)
        {
            type = type.substring(0, pos);
        }
        pos = type.indexOf('/');
        if( pos == -1)
        {
            type = "";
        }
        if(MIME_APPLICATION_OCTET_STREAM.equalsIgnoreCase(type) || StringUtilities.isBlank(type))
        {
            String name = file.getName().toLowerCase();
            if(name.endsWith("#"))
            {
                name = name.substring(0, name.length() - 1);
            }

            if( name.endsWith(".doc") || name.endsWith(".dot"))
            {
                type="application/msword";
            }
            else if( name.endsWith(".docx"))
            {
                type="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
            else if( name.endsWith(".dotx"))
            {
                type="application/vnd.openxmlformats-officedocument.wordprocessingml.template";
            }
            else if( name.endsWith(".docm"))
            {
                type="application/vnd.ms-word.document.macroEnabled.12";
            }
            else if( name.endsWith(".dotm"))
            {
                type="application/vnd.ms-word.template.macroEnabled.12";
            }
            else if( name.endsWith(".xls") || name.endsWith(".xlt") || name.endsWith(".xla"))
            {
                type="application/vnd.ms-excel";
            }
            else if( name.endsWith(".xlsx"))
            {
                type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }
            else if( name.endsWith(".xltx"))
            {
                type="application/vnd.openxmlformats-officedocument.spreadsheetml.template";
            }
            else if( name.endsWith(".xlsm"))
            {
                type="application/vnd.ms-excel.sheet.macroEnabled.12";
            }
            else if( name.endsWith(".xltm"))
            {
                type="application/vnd.ms-excel.template.macroEnabled.12";
            }
            else if( name.endsWith(".xlam"))
            {
                type="application/vnd.ms-excel.addin.macroEnabled.12";
            }
            else if( name.endsWith(".xlsb"))
            {
                type="application/vnd.ms-excel.sheet.binary.macroEnabled.12";
            }
            else if( name.endsWith(".ppt")||name.endsWith(".pot")||name.endsWith(".pps"))
            {
                type="application/vnd.ms-powerpoint";
            }
            else if( name.endsWith(".pptx"))
            {
                type="application/vnd.openxmlformats-officedocument.presentationml.presentation";
            }
            else if( name.endsWith(".potx"))
            {
                type="application/vnd.openxmlformats-officedocument.presentationml.template";
            }
            else if( name.endsWith(".ppsx"))
            {
                type="application/vnd.openxmlformats-officedocument.presentationml.slideshow";
            }
            else if( name.endsWith(".ppam"))
            {
                type="application/vnd.ms-powerpoint.addin.macroEnabled.12";
            }
            else if( name.endsWith(".pptm"))
            {
                type="application/vnd.ms-powerpoint.presentation.macroEnabled.12";
            }
            else if( name.endsWith(".potm"))
            {
                type="application/vnd.ms-powerpoint.template.macroEnabled.12";
            }
            else if( name.endsWith(".ppsm"))
            {
                type="application/vnd.ms-powerpoint.slideshow.macroEnabled.12";
            }
            else if( name.endsWith(".html") || name.endsWith(".htm"))
            {
                type="text/html";
            }
            else if( name.endsWith(".txt") || name.endsWith(".text"))
            {
                type="text/plain";
            }
            else if( name.endsWith(".gif") )
            {
                type="image/gif";
            }
            else if( name.endsWith(".ief") )
            {
                type="image/ief";
            }
            else if( name.endsWith(".jpeg")||name.endsWith(".jpg")||name.endsWith(".jpe") )
            {
                type="image/jpeg";
            }
            else if( name.endsWith(".tiff")||name.endsWith(".tif") )
            {
                type="image/tiff";
            }
            else if( name.endsWith(".png"))
            {
                type="image/png";
            }
            else if( name.endsWith(".css"))
            {
                type="text/css";
            }
            else if( name.endsWith(".js"))
            {
                type="text/javascript";
            }
            else if( name.endsWith(".woff"))
            {
                type="font/woff";
            }           
            else if( name.endsWith(".woff2"))
            {
                type="font/woff2";
            }           
            else if( name.endsWith(".ttf"))
            {
                type="font/truetype";
            }
            else if( name.endsWith(".eot"))
            {
                type = "application/vnd.ms-fontobject";
            }
            else if( name.endsWith(".svg"))
            {
                type = "image/svg+xml";
            }
            
            else if( name.endsWith(".cvs"))
            {
                 // http://stackoverflow.com/questions/398237/how-to-use-the-csv-mime-type
                type="text/csv";
            }
            else if( name.endsWith(".xml"))
            {
                 // http://en.wikipedia.org/wiki/XML
                type="application/xml";
            }
            else if( name.endsWith(".bmp"))
            {
                type="image/bmp";
            }
            else if( name.endsWith(".ico"))
            {
                type="image/vnd.microsoft.icon";
            }            
        }

        if( "application/msword".equalsIgnoreCase(type))
        {
            if( browser != null && browser.isBrowserBlackBerry())
            {
                type="application/vnd.ms-word";
            }
        }

        return type;
    }
    //SERVER-END
    @CheckReturnValue @Nonnull
    public static String requiredExtension( final @Nullable String mimeType, final @Nonnull String defaultExtension)
    {        
        assert defaultExtension!=null;
     
        if( mimeType!=null && StringUtilities.notBlank(mimeType))
        {
            String checks[][]={
                {"application/json", "json"},
                {"text/html", "html"},
                {"application/xml", "xml"},
                {"application/msword", "doc"},
                {"application/msexcel", "xls"},
                {"image/gif", "gif"},
                {"image/jpeg", "jpg"},
                {"image/jpg", "jpg"},
                {"image/png", "png"},
            };
            for( String[] minePair: checks)
            {
                if( mimeType.toLowerCase().contains(minePair[0]))
                {
                    return minePair[1];
                }
            }
        }

        return defaultExtension;
    }

    /**
     * Make quarantine directory. The quarantine directory contains files that have not been verified yet ( virus, cryptolocker etc).
     * 
     * @return the quarantine directory
     * @throws IOException could not create directory.
     */
    @CheckReturnValue @Nonnull
    public static File makeQuarantineDirectory() throws IOException
    {
        File dir = new File( FileUtil.getCachePath() + "quarantine");
        mkdirs(dir);
        
        return dir;
    }
    
    /**
    * Compare the content of two files. Both files must be files (not directories) and exist.
    * 
    * @param first  - first file
    * @param second - second file
    * @return boolean - true if files are binary equal
    * @throws IOException - error in function
    */
    @CheckReturnValue
    public static boolean doesContentMatch(final @Nullable File first, final @Nullable File second) throws IllegalArgumentException, IOException 
    {
        if (first == null) 
        {
            throw new IllegalArgumentException("file1 must NOT be null");
        }
        if (second == null) 
        {
            throw new IllegalArgumentException("file2 must NOT be null");
        }
        
        if (first.exists()== false) 
        {
            LOGGER.info(first + " does NOT exist");
            return false;
        }

        if (second.exists() == false) 
        {
            LOGGER.info(second + " does NOT exist");
            return false;
        }
        
        if (first.isFile() == false) 
        {
            LOGGER.info(first + " is not a file");
            return false;
        }

        if (second.isFile() == false) 
        {
            LOGGER.info(second + " is not a file");
            return false;
        }

        long fileLen1 = first.length();

        if (fileLen1 != second.length()) 
        {
            return false;
        }

        FileInputStream in1 = null;
        FileInputStream in2 = null;

        try 
        {
            in1 = new FileInputStream(first);
            in2 = new FileInputStream(second);

            byte a1[] = new byte[10000];
            byte a2[] = new byte[a1.length];

            long readLen = 0;
            while (true) {
                int len1 = in1.read(a1);
                if (len1 == -1) {
                    break;
                }
                readLen += len1;

                int len2 = in2.read(a2);
                if (len2 != len1) {
                    LOGGER.info("length changed for " + first + " or " + second);
                    return false;
                }

                for (int p = 0; p < len1; p++) {
                    if (a1[p] != a2[p]) {
                        return false;
                    }
                }
            }

            if (fileLen1 != readLen) {
                LOGGER.info("Could not read all of " + first);
                return false;
            }
        } 
        finally 
        {
            try 
            {
                if (in1 != null) 
                {
                    in1.close();
                }
            } finally 
            {
                if (in2 != null) 
                {
                    in2.close();
                }
            }
        }
        return true;
    }
    
    @CheckReturnValue @Nonnull
    public static String readEntry( final @Nonnull ZipFile zipFile, final @Nonnull ZipEntry zipEntry) throws Exception
    {
        try
        (InputStream is = zipFile.getInputStream ( zipEntry)) {

            long size = zipEntry.getSize ();
            if( size == -1)
            {
                throw new Exception( "Could not determine size of entry in file " + zipFile.getName());
            }

            byte b[] = new byte[(int)size];

            int pos = 0;
            while( pos < b.length)
            {
                int read = is.read(b, pos, b.length - pos);
                pos+= read;
            }
            return new String( b, StandardCharsets.UTF_8);
        }
    }

    /**
     * create the directory if NOT present.
     * @param dir the directory to create
     * @throws java.io.IOException if the directory doesn't exist and we can't create it
     */
    public static void mkdirs( final @Nonnull String dir) throws IOException
    {
        if( StringUtilities.isBlank(dir)) throw new IllegalArgumentException("blank directory");
        mkdirs( new File( dir));
    }

    /**
     * create the directory if NOT present.
     * @param dir the directory to create
     * @throws java.io.IOException if the directory doesn't exist and we can't create it
     */
    public static void mkdirs( final @Nonnull File dir) throws IOException
    {
        if( dir.exists())
        {
            if( dir.isDirectory()==false)
            {
                throw new IOException("not a directory: " + dir);
            }
            return; // already exists.
        }

        File absoluteDir = dir.getAbsoluteFile();
        if( absoluteDir.exists())
        {
            LOGGER.warn( "Absolute Directory exists: " + absoluteDir);
            return;
        }
        
        if( absoluteDir.mkdirs())
        {
            return; // we created successfully.
        }

        // we can't create and the directory doesn't exist.
        if( absoluteDir.exists() == false)
        {
            // just wait a bit someone else maybe creating directories.
            LOGGER.info( "pausing to give time for other directories to be created");
            try
            {
                Thread.sleep( (long) (1000 * Math.random()));
            }
            catch( InterruptedException ie)
            {
                Thread.currentThread().interrupt();
                throw new RuntimeException( "interrupted");
            }

            // test if it exits now.
            if( absoluteDir.exists() == false)
            {
                absoluteDir.mkdirs();// try to create again
            }

            // we still can't create
            if( absoluteDir.exists() == false)
            {
                Path path=absoluteDir.toPath();
                if( Files.notExists(path))
                {
                    throw new IOException( "could not create directory " + absoluteDir);
                }
                else
                {
                    LOGGER.warn( "Can not determine if the directory exists or not: " + path);
                }
            }
        }
    }

    /**
     * Delete all from a directory ( including the directory) or simple file. 
     * This method will throw an IOException if the directory/file is not removed for some reason. 
     * 
     * This method will try to delete ( if the file/directory exists) then if that fails rename. 
     * Only if we can not remove/rename will an IOException be thrown.
     *
     * @param fileOrDirectory the file/directory to delete
     * @throws java.io.IOException if an IO exception occurs.
     */
    public static void deleteAll( final @Nullable File fileOrDirectory) throws IOException
    {
        //if doesn't exist then no issue.
        if( fileOrDirectory == null || fileOrDirectory.exists() == false) return;

        if( fileOrDirectory.isDirectory())
        {
            File files[] = fileOrDirectory.listFiles();
            if( files !=null){
                for (File tmpFile : files) 
                {
                    deleteAll(tmpFile);
                }
            }
        }

        if( fileOrDirectory.delete() == false)
        {
            if( fileOrDirectory.exists())
            {
                // just wait a bit someone else maybe creating directories.
                LOGGER.info( "pausing to give time for other files to be created");
                try
                {
                    Thread.sleep( (long) (1000 * Math.random()));
                }
                catch( InterruptedException ie)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException( "interrupted", ie);
                }

                fileOrDirectory.delete();
                if( fileOrDirectory.exists())
                {
                    LOGGER.warn( "could not delete '" + fileOrDirectory + "'... trying to rename");
                    File tmpDelete = new File( fileOrDirectory.getPath() + System.currentTimeMillis() + "_deleted");

                    fileOrDirectory.renameTo(tmpDelete);
                    if( fileOrDirectory.exists())
                    {
                        throw new IOException( "could not delete " + fileOrDirectory);
                    }
                }
            }
        }
    }

     /**
     * Opens a file in a Linux or Windows environment.
     * @param file the file to open
     * @return true if Ok
     * @throws Exception If something went wrong
     */
    @CheckReturnValue
    public static boolean openFileInDefaultApplication(final @Nonnull File file) throws Exception
    {
        String osName = System.getProperty("os.name").toLowerCase();
        String url = file.getAbsolutePath();
        boolean result  =false;

        if (osName != null && url != null)
        {
            if (osName.contains("windows"))
            {
                // cmd /c start /d "C:\Program Files\Sony Handheld" HOTSYNC.EXE
                StringBuilder urlBuffer = new StringBuilder();
                urlBuffer.append("cmd /c \"");
                urlBuffer.append(url.substring(0, url.indexOf(':')+1));
                urlBuffer.append(" && cd");
                urlBuffer.append(url.substring(url.indexOf(':')+1, url.lastIndexOf('\\')+1));
                urlBuffer.append(" && start /d \"");
                urlBuffer.append(url.substring(0, url.lastIndexOf('\\')+1));
                urlBuffer.append("\" ");

                /*
                 * The following special characters require quotation marks: & < > [ ] { } ^ = ; ! ' + , ` ~ [white space]
                 *
                 * http://www.microsoft.com/resources/documentation/windows/xp/all/proddocs/en-us/cmd.mspx?mfr=true
                 */
                String fileName = file.getName();
                String specialChars[] = {
                    "&",
                    "<",
                    ">",
                    "[",
                    "]",
                    "{",
                    "}",
                    "^",
                    "=",
                    ";",
                    "!",
                    "'",
                    "+",
                    ",",
                    "`",
                    "~",
                    " ",
                    "\n",
                    "\t",
                    "\r"
                };
                for (String specialChar : specialChars) 
                {
                    fileName = fileName.replace( specialChar, "\"" + specialChar + "\"");
                }

                urlBuffer.append( fileName);
                urlBuffer.append("\"");
                String cmd = urlBuffer.toString();

                try
                {

                    Runtime.getRuntime().exec(cmd);
                    result = true;
                }
                catch (java.io.IOException wex)
                {
                    LOGGER.warn(cmd, wex);
                }
            }
            else if (osName.contains("linux"))
            {
                try
                {
                    Runtime.getRuntime().exec("gnome-open " + url);
                    result = true;
                }
                catch (java.io.IOException gex)
                {
                    String cmd = "kfmclient exec " + url;
                    try
                    {
                        Runtime.getRuntime().exec(cmd);
                        result = true;
                    }
                    catch (java.io.IOException kex)
                    {
                        LOGGER.warn(cmd, kex);
                    }
                }
            }
            else if (osName.contains("mac"))
            {
                String cmd = "open " + url;
                try
                {
                    Runtime.getRuntime().exec(cmd);
                    result = true;
                }
                catch (java.io.IOException e)
                {
                    LOGGER.warn(cmd, e);
                }
            }
        }

        return result;
    }

    /**
     * Creates a file in the user home directory with unique name.
     *
     * @param name The base name
     * @return file to open
     * @throws Exception If something went wrong
     */
    @CheckReturnValue @Nonnull
    public static File createNewFileToOpen(final @Nonnull String name) throws Exception
    {
        File file;

        String path = CProperties.getHomeDir();

        for( int loop =0; true; loop++)
        {
            String ext ="";

            if( loop > 0)
            {
                ext = "[" + loop + "]";
            }

            int pos = name.lastIndexOf( '.');

            String first = name;
            String last = "";

            if( pos != -1)
            {
                first= name.substring(0, pos);
                last = "." + name.substring( pos + 1);
            }

            String temp = first + ext + last;

            file = new File(path + "/" + temp);

            if( file.exists() == false)
            {
                return file;
            }
        }

//        return file;
    }

    /**
     * Rename the temp file to the target file, if the target exists then delete it
     * or move it out of the way ( if it doesn't match the temp file).
     *                                                                                                              <P>
     * Two problems were found :-
     *
     *      1. On the server side we can't copy/decrypt/compress/ftp/sftp to the target file as there
     *         are many race conditions when we have a number of threads/JVMs looking at the same files.
     *      2. Checksum wasn't being performed on existing files only when they were transferred.
     *
     * The combination of these two problems was what we saw. Thread A came along found that a file didn't exist so
     * it started to transfer it from UK, thread B came along a milliseconds later and found that the file did exist
     * ( zero bytes at this stage) and transferred it to NY file store.
     *
     * On the server side we now have a cache of the files that have have a checksum performed. This cache is keyed on
     * the file path and the file modification time & size is checked to make sure it's current.
     *
     * We've changed all the places that we were doing a copy/decrypt/compress/ftp/sftp to the target file and write
     * to a temp file instead, we then attempt to move this temp file to the target file at the end of the process. If
     * this move fails we check if the target file now exists if the target file exists and the checksum/size match then
     * it was just a race condition and we don't bother with the move. If the target file is not the same as the temp
     * file we try to delete the target file, if that fails ( someone reading the target file) then we move the target
     * file out of the way and then rename the temp file.
     *
     * </P>
     *
     * @param tempFile the temp file that was written to.
     * @param targetFile target file is replaced by the temp file.
     * @throws IOException we could not move the temp file to the target file.
     */
    public static void replaceTargetWithTempFile( final @Nonnull File tempFile, final @Nonnull File targetFile) throws IOException
    {
        if( tempFile == null) throw new IllegalArgumentException("tempFile is mandatory");
        if( targetFile == null) throw new IllegalArgumentException("targetFile is mandatory");
        
        if( tempFile.renameTo(targetFile) == false)
        {
            if( tempFile.exists() == false)
            {
                throw new IOException(tempFile + " doesn't exist");
            }
            else if( tempFile.canRead() == false)
            {
                throw new IOException("can not read " + tempFile);
            }
            else if(targetFile.exists())
            {
                // if target file and temp file are the same then don't bother renaming
                if( doesContentMatch(tempFile, targetFile) == false)
                {
                    File oldTempFile = File.createTempFile( "replace_", targetFile.getName(), targetFile.getParentFile());
                    try
                    {
                        /**
                         * move the target out of the way
                         */
                        if(targetFile.renameTo(oldTempFile)==false)
                        {
                            if( targetFile.exists() && targetFile.delete() == false)
                            {
                                throw new IOException( "could not move " + targetFile + " out of the way");
                            }
                        }

                        if( tempFile.renameTo(targetFile) == false)
                        {
                            try{
                                if( CALLING_COPY.get().compareAndSet(false, true))
                                {
                                    copy(tempFile, targetFile);
                                    tempFile.delete();
                                }
                                else{
                                    throw new IOException("couldn't copy to " + targetFile);
                                }
                            }
                            finally
                            {
                                CALLING_COPY.remove();
                            }    
                           // throw new IOException( "could not move " + tempFile + "->" + targetFile);
                        }
                    }
                    finally
                    {
                        oldTempFile.delete();
                    }
                }
                else
                {
                    long lastModified = tempFile.lastModified();

                    /* Ok the target file exists & matches but we can't change it so that we at least see the time change ( caching reasons) */
                    if( targetFile.setLastModified(lastModified))
                    {
                        tempFile.delete();
                    }
                    else
                    {
                        LOGGER.warn("could not set the last modificated time of " + targetFile);
                    }
                }
            }
            else
            {
                File dir = targetFile.getParentFile();
                if( dir.exists() ==false) {
                    dir.mkdirs();
                }
                if( dir.exists()==false)
                {
                    throw new IOException("couldn't rename to " + targetFile + " as can't create parent directory");
                }
                else if( dir.isDirectory()==false)
                {
                    throw new IOException("couldn't rename, target directory " + dir + " is a file");
                }
                else if( dir.canWrite()==false)
                {
                    throw new IOException("couldn't rename, target directroy " + dir + " is not writable");
                }
                
                try{
                    if( CALLING_COPY.get().compareAndSet(false, true))
                    {
                        copy(tempFile, targetFile);
                        tempFile.delete();
                    }
                    else{
                        throw new IOException("couldn't copy to " + targetFile);
                    }
                }
                finally
                {
                    CALLING_COPY.remove();
                }                
            }
        }
    }

    /**
     *
     * If targetLocation does not exist, it will be created.
     *
     * @param sourceLocation the source directory
     * @param targetLocation the target directory
     * @throws Exception a serious problem
     */
    public static void copyDirectory(final @Nonnull File sourceLocation , final @Nonnull File targetLocation) throws Exception
    {
        if( sourceLocation==null) throw new IllegalArgumentException( "sourceLocation is mandatory");
        if( targetLocation==null) throw new IllegalArgumentException( "targetLocation is mandatory");
        if (sourceLocation.isDirectory()==false) throw new IllegalArgumentException( sourceLocation + ": must be a directory");
        if (targetLocation.isFile()==true) throw new IllegalArgumentException( targetLocation + ": must be a directory( not a file )");
        
        for (File file: sourceLocation.listFiles()) 
        {
            if( file.isDirectory())
            {
                File tmpDir=new File(targetLocation, file.getName());
                mkdirs(tmpDir);
                copyDirectory(file, tmpDir);
            }
            else
            {
                copy( file, new File(targetLocation, file.getName()));
            }
        }
    }

    /**
     * Copies file from one file to another
     *
     * @param srcFile source file
     * @param targetFile target file
     * @throws IOException a serious problem
     */
    public static void copy( final @Nonnull File srcFile, final @Nonnull File targetFile) throws IOException
    {
        if( srcFile ==null) throw new IllegalArgumentException( "srcFile is mandatory");
        if( targetFile ==null) throw new IllegalArgumentException( "targetFile is mandatory");
        
        if( srcFile.exists()==false) throw new IOException( "Does NOT exist: " + srcFile);
        if( srcFile.isFile()==false) throw new IOException( "Not a file (exists but not a file): " + srcFile);
        if( srcFile.canRead()==false) throw new IOException( "Can not read: " + srcFile);
        
        mkdirs(targetFile.getParentFile());
        File tempFile = File.createTempFile( targetFile.getName(), "copy", targetFile.getParentFile());

        BufferedInputStream r = null;
        BufferedOutputStream w = null;

        try
        {
            r = new BufferedInputStream(
                new FileInputStream( srcFile )
            );

            w = new BufferedOutputStream(
                new FileOutputStream( tempFile )
            );

            byte array[] = new byte[2048];

            while( true)
            {
                int len;

                len = r.read( array);

                if( len == -1) break;
                w.write(array, 0, len);
            }
        }
        catch( IOException e)
        {
            LOGGER.error(
                "File.copy( '" + srcFile + "','" + targetFile + "') failed",
                e
            );

            throw e;
        }
        finally
        {
            try
            {
                if( r != null) r.close();
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + r , e);
            }

            try
            {
                if( w != null) w.close();
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing output stream " + w , e);
            }
        }

        replaceTargetWithTempFile( tempFile, targetFile);
    }

    /**
     * Read a file into a String
     * @param fileName The file to read.
     * @return The data from the file as a string.
     * @throws Exception a serious problem
     */
    @CheckReturnValue @Nonnull
    public static String readFile( final @Nonnull String fileName ) throws Exception
    {
        if( StringUtilities.isBlank(fileName) == false )
        {
            return readFile( new File( fileName));
        }

        return "";
    }

    /**
     * Read the file content as a String. 
     * @param file the file to read.
     * @return the value
     * @throws IOException Exception A serious problem
     */
    @CheckReturnValue @Nonnull
    public static String readFile( final @Nonnull File file ) throws IOException
    {
        StringBuilder buffer = new StringBuilder();

        try
        (FileReader fr = new FileReader( file )) {
            
            char array[] = new char[10240];
            while ( true )
            {
                int len = fr.read( array );

                if ( len <= 0 )
                {
                    break;
                }

                buffer.append( array, 0, len );
            }
        }

        return buffer.toString();
    }
    
    /**
     * Read the file content as a String. 
     * @param fn the file to read.
     * @return the value
     * @throws IOException Exception A serious problem
     */
    @CheckReturnValue @Nonnull
    public static byte[] readFileAsBytes( final @Nonnull String fn ) throws IOException
    {       
        return readFileAsBytes(new File(fn));
    }
    
    /**
     * Read the file content as a String. 
     * @param file the file to read.
     * @return the value
     * @throws IOException Exception A serious problem
     */
    @CheckReturnValue @Nonnull
    public static byte[] readFileAsBytes( final @Nonnull File file ) throws IOException
    {        
        try
        (
            FileInputStream in = new FileInputStream( file );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            byte array[] = new byte[10240];
            
            while ( true )
            {
                int len = in.read( array );

                if ( len <= 0 )
                {
                    break;
                }

                out.write( array, 0, len );
            }
            return out.toByteArray();
        }
    }
    
    /**
     * Generates SHA512.
     *
     * @param file The input file
     * @return digest
     * @throws IOException If something went wrong
     */    
    public static byte[] generateSHA512( final @Nonnull File file) throws IOException
    {
        return generateDigest(file, "SHA-512");
    }
    
    /**
     * Generates MD5.
     *
     * @param file The input file
     * @return digest
     * @throws IOException If something went wrong
     */
    @CheckReturnValue @Nonnull
    public static byte[] generateMD5( final @Nonnull File file) throws IOException
    {
        return generateDigest(file, "MD5");
    }
    
    /**
     * Generates SHA1.
     *
     * @param file The input file
     * @return digest
     * @throws IOException If something went wrong
     */
    @CheckReturnValue @Nonnull
    public static byte[] generateSHA1( final @Nonnull File file) throws IOException
    {
        return generateDigest(file, "SHA1");
    }
    /**
     * Generates Digest.
     *
     * @param file The input file
     * @param algorithum SHA1, SHA512
     * @return checksum
     * @throws IOException If something went wrong
     */
    @CheckReturnValue @Nonnull
    public static byte[] generateDigest( final @Nonnull File file, final String algorithum) throws IOException
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance(algorithum);
        } 
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException( "could not get message digest: " + algorithum, ex);
        }

        int buffsize = Math.max((int)Math.min(file.length(), 16 * 1024 * 1024), 1024);
        
        byte[] dataBytes = new byte[buffsize];

        try
        (InputStream fis = Files.newInputStream(file.toPath(),  StandardOpenOption.READ)) {

            while( true )
            {
                int len;
                len = fis.read(dataBytes);

                if( len == -1 )
                {
                    break;
                }
                md.update(dataBytes, 0, len);
            }
        }

        return md.digest();
    }

    /**
     *  Create a simple zip file
     *
     * @param files the files
     * @param zipfile the target
     */
    public static void doZip(final @Nullable ArrayList<File> files , final @Nonnull File zipfile)
    {
        if(files == null || files.isEmpty())
        {
            return;
        }

//        ZipOutputStream out = null;
        

        try( ZipOutputStream out=new ZipOutputStream(new FileOutputStream(zipfile)))
        {
//            out = new ZipOutputStream((OutputStream) new FileOutputStream(zipfile));
            out.setLevel(6);

            int num = 0;
            for (File file: files)
            {
                if(file.isDirectory())
                {
                   continue;
                }
                num++;


                byte[] buf = new byte[10 * 1024];

                LOGGER.debug("Adding file:" + file.getPath() +", into zipfile");

                try(FileInputStream in =  new FileInputStream(file))
                {
                    ZipEntry entry = new ZipEntry(file.getName());

                    out.putNextEntry(entry);

                    while (true)
                    {
                        int len;
                        len = in.read(buf);

                        if( len < 1) break;

                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
//                    in.close();
                }
            }
//            out.close();

            LOGGER.debug("Total " + num +" files zipped into zipfile:" + zipfile.getPath());

        } 
        catch (IOException e)
        {
            LOGGER.error("Exception in zipping files:" + files +", into zipfile:" + zipfile.getPath(), e);

        } 
//        finally
//        {
//            try
//            {
//                if (out != null)
//                {
//                    out.close();
//                }
//            } 
//            catch (IOException e)
//            {
//                LOGGER.warn("Exception causing when closing ZipOutputStream " + out, e);
//            }

//            try
//            {
//                if (in != null)
//                {
//                    in.close();
//                }
//            } 
//            catch (IOException e)
//            {
//                LOGGER.warn("Exception causing when closing FileInputStream " + in, e);
//            }
//        }
    }
    
    /**
     * add a file or directory to the zip file, if the zip file exists, it will be overridden
     * if a file matches both incPattern and excPattern, it will not be added to the zip
     * @param fileOrDirToAdd full name of file or dir
     * @param zipFileName zip file name
     * @param incPattern file path patterns that should be included, comma separated
     * @param excPattern file path patterns that should be excluded, comma separated
     * @throws IOException IO exception
     */
    public static void doZip(final @Nonnull String fileOrDirToAdd, final @Nonnull String zipFileName, final @Nullable String incPattern, final @Nullable String excPattern) throws IOException
    {
        File zipFile = new File( zipFileName);
        FileUtil.mkdirs( zipFile.getParentFile());
        FileOutputStream os = new FileOutputStream( zipFile);
        
        try(ZipOutputStream jo = new ZipOutputStream(os))
        {
            File file = new File(fileOrDirToAdd);
            addFileToZip(jo, file, "", incPattern, excPattern);
        }
        finally
        {
            os.close();
        }
    }

    private static void addFileToZip(final ZipOutputStream jo, final File file, final String parentDirName, final String incPattern, final String excPattern) throws IOException
    {
        if(file == null || file.exists() == false)
        {
            return;
        }
        String zipEntryName = file.getName();
        if(StringUtilities.notBlank(parentDirName))
        {
            zipEntryName = parentDirName + "/" + zipEntryName;
        }

        if( StringUtilities.isBlank( excPattern) == false)
        {
            if( StringUtilities.isPatternMatch( excPattern, zipEntryName))
            {
                return;
            }
        }
        
        if( StringUtilities.isBlank( incPattern) == false)
        {
            if( StringUtilities.isPatternMatch( incPattern, zipEntryName) == false)
            {
                return;
            }
        }

        if(file.isDirectory())
        {
            for(File f : file.listFiles())
            {
                addFileToZip(jo, f, zipEntryName, incPattern, excPattern);
            }
        }
        else
        {
            jo.putNextEntry( new ZipEntry( zipEntryName));
            try(FileInputStream fis = new FileInputStream(file))
            {
                int    bRead;
                byte[] buf = new byte[2048];
                while (true)
                {
                    bRead = fis.read(buf);
                    if( bRead <= 0) break;
                    jo.write(buf, 0, bRead);
                }
            }
            finally
            {
                jo.closeEntry();
            }
        }
    }
    
    /**
     *  unzip a zip file
     * @param zipfile
     */
    public static void unZip(final @Nonnull File zipfile)
    {
        if(!zipfile.exists())
        {
            LOGGER.warn("unzip failed , zipfile:" + zipfile.getPath() + "doesn't exist");
            return;
        }

        Enumeration entries;
        ZipFile zipFile;

        InputStream in = null;
        OutputStream out = null;

        try
        {
            zipFile = new ZipFile(zipfile);
            entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File targetFile = new File(zipfile.getParentFile(),entry.getName());
                if (entry.isDirectory())
                {
                    targetFile.mkdirs();
                    
                    continue;
                }
                targetFile.getParentFile().mkdirs();
                LOGGER.debug("Extracting file: " + entry.getName());
                in = zipFile.getInputStream(entry);
                out = new BufferedOutputStream(new FileOutputStream(targetFile));

                byte[] buffer = new byte[10240];

                while (true)
                {
                    int len;
                    len = in.read(buffer);

                    if( len < 1) break;

                    out.write(buffer, 0, len);
                }

                in.close();
                out.close();
            }

            zipFile.close();
        }
        catch (IOException e)
        {
            String msg="Error, when unzip file:" + zipfile.getPath();
            LOGGER.error(msg, e);
            throw new RuntimeException( msg,e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                LOGGER.warn("Exception causing when closing ZipOutputStream " + out, e);
            }

            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                LOGGER.warn("Exception causing when closing FileInputStream " + in, e);
            }
        }
    }

    /**
     * Add all child directories and files matched one pattern Under a directory into a collection
     *
     * @param file
     * @param all
     * @param pattern  file pattern, not for directory
     */
    public static void addFilesRecursively(final @Nonnull File file,final @Nonnull  Collection all, final @Nonnull String pattern)
    {
        final File[] children = file.listFiles();
        if (children != null)
        {
            for (File child : children) 
            {
                if(child.isFile() == true && child.getName().toLowerCase().endsWith(pattern.toLowerCase()) == false )
                {
                    continue;
                }
                all.add(child);
                addFilesRecursively(child, all, pattern);
            }
        }
    }

    /**
     * Compresses a file
     * @param targetFile the compressed file
     * @param inFile the file to compress
     * @throws Exception failure to compress the file
     */
    public static void compressFile( final @Nonnull File inFile, final @Nonnull File targetFile) throws Exception
    {
        File tempFile = File.createTempFile( targetFile.getName(), "compress", targetFile.getParentFile());

        FileOutputStream out = null;
        BufferedOutputStream bo = null;
        GZIPOutputStream gos = null;

        FileInputStream in = null;

        try
        {
            out = new FileOutputStream(tempFile);
            bo = new BufferedOutputStream( out);
            gos = new GZIPOutputStream(bo);

            in = new FileInputStream(inFile);

            byte array[] = new byte[5 * 4096];
            while( true)
            {
                int len = in.read(array);

                if( len == -1) break;

                gos.write( array, 0, len);
            }
        }
        finally
        {
            try
            {
                if( in != null )
                {
                    in.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing input stream " + in , e );
            }

            try
            {
                if( gos != null )
                {
                    gos.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing output stream " + gos , e );
            }

            try
            {
                if( bo != null )
                {
                    bo.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing output stream " + bo , e );
            }

            try
            {
                if( out!= null )
                {
                    out.close();
                }
            }
            catch(IOException e)
            {
                LOGGER.warn("Exception causing when closing ouput stream " + out , e );
            }
        }
        replaceTargetWithTempFile( tempFile, targetFile);
    }

    /**
     * Decompress a file using the gzip format.
     * @param targetFile Output file
     * @param inFile Input file
     * @throws Exception If something went wrong
     */
    public static void decompressFile( final @Nonnull File inFile, final @Nonnull File targetFile) throws Exception
    {
        File tempFile = File.createTempFile( targetFile.getName(), "decompress", targetFile.getParentFile());
        try{
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            InputStream gis = null;
            BufferedOutputStream out = null;
            FileInputStream in = null;

            try
            {
                fis = new FileInputStream( inFile );
                bis = new BufferedInputStream( fis );

                in = new FileInputStream( inFile );

                int gzipmgk = in.read()|(in.read() << 8); // two byte magic number
                int zipmgk = gzipmgk | ((in.read()|(in.read()<<8))<<16); // four byte magic number

                if( gzipmgk == GZIP_MAGIC )
                {
                    gis = new GZIPInputStream(bis);
                }
                else if( zipmgk == ZIP_MAGIC )
                {
                    ZipFile zipper = new ZipFile( inFile );

                    Enumeration en = zipper.entries();
                    ZipEntry ze = (ZipEntry)en.nextElement();
                    gis = zipper.getInputStream( ze );
                }
                else
                {
                    throw new Exception( "Unsupported compression type" );
                }

                out = new BufferedOutputStream( new FileOutputStream(tempFile));

                byte array[] = new byte[5 * 4096];
                while( true)
                {
                    int len = gis.read(array);

                    if( len == -1)
                    {
                        break;
                    }

                    out.write( array, 0, len);
                }
            }
            finally
            {

                try
                {
                    if( in != null )
                    {
                        in.close();
                    }
                }
                catch(IOException e)
                {
                    LOGGER.warn("Exception causing when closing input stream " + in, e);
                }

                try
                {
                    if( gis != null )
                    {
                        gis.close();
                    }
                }
                catch(IOException e)
                {
                    LOGGER.warn("Exception causing when closing input stream " + gis, e );
                }

                try
                {
                    if( bis != null )
                    {
                        bis.close();
                    }
                }
                catch(IOException e)
                {
                    LOGGER.warn("Exception causing when closing input stream " + bis, e );
                }

                try
                {
                    if( fis != null )
                    {
                        fis.close();
                    }
                }
                catch(IOException e)
                {
                    LOGGER.warn("Exception causing when closing input stream " + fis, e );
                }

                try
                {
                    if( out!= null )
                    {
                        out.close();
                    }
                }
                catch(IOException e)
                {
                    LOGGER.warn("Exception causing when closing ouput stream " + out , e );
                }
            }
            replaceTargetWithTempFile( tempFile, targetFile);
        }
        finally
        {
            tempFile.delete();
        }
    }
    
    /**
     * Verifies a file against a checksum.
     * @return boolean true if a checksum generated on the file == expected checksum
     * @param size the file size to check
     * @param file the file to verify
     * @param checkSum the expected checksum for the file
     */
    @CheckReturnValue
    public static boolean isValid( final @Nonnull File file, final @Nullable String checkSum, final long size)
    {
        if( file.exists() == false)
        {
            return false;
        }

        try
        {
            validate( file, checkSum, size);

            return true;
        }
        catch( FileValidationException e)
        {
            LOGGER.warn( file.toString(), e);
            return false;
        }
    }

    /**
     * Verifies a file against a checksum.
     * @param size the file size to check
     * @param file the file to verify
     * @param chk the expected checksum for the file
     * @throws com.aspc.remote.util.misc.FileValidationException validation exception
     */
    public static void validate( final File file, final String chk, final long size) throws FileValidationException
    {
        validationHandler.validate( file, chk, size);
    }

    /**
     * Gets the directory portion of a file path, if there is one.
     *
     * @param path the file path
     * @return String the directory portion
     */
    @CheckReturnValue @Nonnull
    public static String getBaseFromPath( final @Nonnull String path )
    {
        String base = "";

        int position = path.lastIndexOf( '/' );
        if( position != -1 )
        {
            base = path.substring( 0, position );
        }

        return base;
    }

    /**
     * Gets the name portion of a file path
     *
     * @param path the file path
     * @return String the name portion
     */
    @CheckReturnValue @Nonnull
    public static String getName( final @Nonnull String path )
    {
        String name = path;

        int position = path.lastIndexOf( '/' );
        if( position != -1 )
        {
            ++position;
            name = path.substring( position );
        }

        return name;
    }

    /**
     * Updates a name that is numbered with the convention name[n] where n represents
     * a number. examples:
     * "name" becomes "name[1]"
     * "name[1]" becomes "name[2]"
     * @param n the name to update
     * @return String the updated name
     */
    @CheckReturnValue @Nonnull
    public static String updateNumberedName( final @Nullable String n )
    {
        String name = (n==null)?"":n;

        int count = 0;

        if( name.endsWith( "]"))
        {
            int start = name.lastIndexOf( '[');

            if( start != -1)
            {
                String temp = name.substring( start + 1, name.length() - 1);

                try
                {
                    count = Integer.parseInt( temp);

                    name = name.substring( 0, start);
                }
                catch( NumberFormatException e)
                {
                    LOGGER.debug( "unable to get number from name: "+name, e );
                }
            }
        }

        name += "[" + ( count + 1) + "]";//NOPMD

        return name;
    }

    /**
     * get the doc cache path
     *
     * @return the path
     */
    @CheckReturnValue @Nonnull
    public static String getCachePath()
    {
        String temp = CProperties.getProperty(CACHE_DIR, null);
        if( temp == null)
        {
            String docPath=getDocCachePath().trim();
            while( docPath.endsWith("/"))
            {
                docPath=docPath.substring(0, docPath.length()-1);
            }
            int pos=docPath.lastIndexOf('/');
            if( pos!=-1)
            {
                temp=docPath.substring(0, pos);
            }
        }
        assert temp!=null;
        if( temp.endsWith("/") == false) temp += "/";

        while( temp.contains("\\"))
        {
            temp = temp.replace("\\", "/");
        }

        while( temp.contains("//"))
        {
            temp = temp.replace( "//", "/");
        }

        if( temp.endsWith("/") == false) temp+="/";

        return temp;
    }
    
    /**
     * get the doc cache path
     *
     * @return the path
     */
    @CheckReturnValue @Nonnull
    public static String getDocCachePath()
    {
        String temp = CProperties.getProperty(DOC_CACHE, null);
        if( temp == null)
        {
            String cacheDir=CProperties.getProperty(CACHE_DIR, null);
            if( cacheDir==null)
            {
                String tmpDir=System.getProperty("java.io.tmpdir");
                temp = tmpDir + "/cache/docs/";
            }
            else
            {
                temp=cacheDir +"/docs/";
            }
        }

        if( temp.endsWith("/") == false) temp += "/";

        while( temp.contains("\\"))
        {
            temp = temp.replace("\\", "/");
        }

        while( temp.contains("//"))
        {
            temp = temp.replace( "//", "/");
        }
        
        if( temp.endsWith("/") == false) temp+="/";
        return temp;
    }

    /**
     * Make a cache file and create the sub directories.
     * @return The cache file
     * @param URLs the urls
     * @param path The path for the file.
     * @throws Exception A serious problem
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed") 
    @CheckReturnValue @Nonnull
    public static File makeCacheFile( final @Nullable String URLs, final @Nonnull String path) throws Exception
    {
        String temp = getDocCachePath();

        String volume = "";

        if( StringUtilities.isBlank( URLs) == false)
        {
            StringTokenizer st=new StringTokenizer( URLs, ",|");
            volume = st.nextToken();
            volume +="/";

            int i = volume.indexOf( '@' );
            if( i != -1 )
            {
                volume = volume.substring( ++i );
            }

            volume = volume.replaceAll( "[\\\\.:]", "/" );

            while( volume.contains("//"))
            {
                volume = volume.replace( "//", "/" );
            }
        }
        File cacheFile = new File( temp + volume + path);

        if( cacheFile.exists() == false)
        {

            File dir = cacheFile.getParentFile();
            mkdirs( dir);
        }

        return cacheFile;
    }

    /**
     * a common function to add tmp folder to a url
     * @return the src url path with tmp folder
     * @param srcUrl String the src url path
     *
     */
    @CheckReturnValue @Nonnull
    public static String addTmpFolder(final @Nonnull String srcUrl)
    {
        String[] list = srcUrl.split(",");
        StringBuilder sb=new StringBuilder();
        for( String url: list)
        {
            String resultStr = url;
//            if (resultStr == null)
//            {
//                return resultStr;
//            }
            if( resultStr.endsWith("/") == true ) // adds 'tmp' folder for the sftp login
            {
                resultStr += "tmp";//NOPMD
            }
            else
            {
                resultStr += "/tmp";//NOPMD
            }
            if( sb.length() != 0)
            {
                sb.append(",");
            }
            
            sb.append(resultStr);
        }
        return sb.toString();
    }

    /**
     * set the validation handler.
     * @param handler the handler to use
     */
    public static void iSetFileValidationHandler( final @Nonnull FileValidationHandler handler)
    {
        validationHandler=handler;
    }
    
    /**
     * Readable format in KB/MB/GB
     * @param size
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String readableFileSize(long size) 
    {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    private static final int GZIP_MAGIC = GZIPInputStream.GZIP_MAGIC;
    private static final int ZIP_MAGIC = 0x04034b50; /* 'PK\003\004' */

    /**
     * The validation handler ( to be overridden by DBFile validation handler)
     */
    private static FileValidationHandler validationHandler;//MT CHECKED
    static
    {
        validationHandler=new ImplFileValidationHandler();

//SERVER-START
        String mimeType = "image/png    png PNG";
        javax.activation.MimetypesFileTypeMap map = new javax.activation.MimetypesFileTypeMap();
        map.addMimeTypes(mimeType);
        javax.activation.FileTypeMap.setDefaultFileTypeMap(map);
//SERVER-END
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.FileUtil");//#LOGGER-NOPMD
}
