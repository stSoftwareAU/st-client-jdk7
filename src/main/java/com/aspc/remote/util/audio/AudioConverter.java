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
package com.aspc.remote.util.audio;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CUtilities;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;

/**
 * Convert an audio file from one format to another. 
 * 
 * <a href="http://www.w3schools.com/html/html5_audio.asp">W3 Schools HTML5 Audio</a>
 * 
 * @author Nigel Leck
 */
public class AudioConverter {
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.audio.AudioConverter");//#LOGGER-NOPMD

    public static enum Format{
        MP3("audio/mpeg"),
        OGG("audio/ogg"),
        WAV("audio/wav");
        
        public final String mime;

        private Format( final String mime)
        {
            this.mime=mime;
        }
    }
    private final File srcFile;
    private Format format=Format.MP3;

    private AudioConverter(final File srcFile) throws IOException {
        this.srcFile=srcFile;
        if( srcFile.exists()==false) throw new IOException("does not exist " + srcFile);
    }

    /**
     * Build a converter.
     * @param srcFile the source file.
     * @return The converter
     * @throws IOException the file doesn't exist.
     */
    public static AudioConverter build(final File srcFile) throws IOException
    {
        return new AudioConverter(srcFile);
    }

    /**
     * Set's the format
     * @param format the format to target.
     * @return this.
     */
    public AudioConverter setFormat( Format format)
    {
        this.format=format;
        return this;
    }

    public File process() throws AudioConversionException
    {
        if (CUtilities.isWindose())
        {
            throw new AudioConversionException( "Only supported on Linux");
        }
        File tmpFile=null;
        try
        {
            String sha1=new String( StringUtilities.encodeBase64( FileUtil.generateCheckSum(srcFile)), "ascii").replace("=", "").replace("/", "_");
            String prefix=srcFile.getName();
            int pos = prefix.lastIndexOf(".");
            if( pos > 3)
            {
                prefix=prefix.substring(0, pos);
            }
            String cachePath=FileUtil.getCachePath() + "/audio/";
            File parentFile=srcFile.getParentFile();
            String subFolder="/";
            for( int loop=0; parentFile != null && loop< 3; loop++)
            {
                subFolder = "/" + parentFile.getName() + subFolder;
                parentFile=parentFile.getParentFile();
            }
            cachePath+= subFolder;
            File targetFile=new File(cachePath, sha1 +"." + format.name().toLowerCase());

            if( targetFile.exists()) return targetFile;
            
            tmpFile=File.createTempFile( prefix, "." + format.name().toLowerCase(), FileUtil.makeQuarantineDirectory());

            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i",
                srcFile.toString(),
                "-y",
                "-f",
                format.name().toLowerCase(),
                tmpFile.toString()
            );
            Process process = processBuilder.start();
//            LOGGER.info( processBuilder.command());
            try
            {

                try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream())))
                {
                    process.waitFor();
                    String line;
                    while ((line = input.readLine()) != null) {
                        LOGGER.info(line);
                    }
                }
                int status = process.exitValue();
                if( status != 0)
                {
                    throw new AudioConversionException("ffmpeg exit status " + status);
                }

            }
            catch( InterruptedException ie)
            {
                throw new AudioConversionException( "interrupted converting " + srcFile, ie);
            }
            finally
            {
                process.destroy();
            }

            if( tmpFile.length()<1)
            {
                throw new AudioConversionException( "zero length target");
            }
            
//            FileUtil.mkdirs(targetFile.getParentFile());
            FileUtil.replaceTargetWithTempFile(tmpFile, targetFile);

            return targetFile;
        }
        catch( IOException io)
        {
            throw new AudioConversionException("could not convert " + srcFile + "->" + format, io);
        }
        finally
        {
            if( tmpFile != null) tmpFile.delete();
        }
    }
}
