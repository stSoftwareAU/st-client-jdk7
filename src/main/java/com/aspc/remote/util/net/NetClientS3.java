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
package com.aspc.remote.util.net;

import com.aspc.remote.rest.ContentType;
import com.aspc.remote.rest.DispositionType;
import com.aspc.remote.rest.Method;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.internal.AWSReSTAuthorization;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 * Implements the ftp protocol for NetClient
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      luke
 * @since       February 21, 2006
 */
public class NetClientS3 implements NetClient
{
    private String accessKeyID;
    private String secretAccessKey;
    private String server;
    private String baseDir;
    private String path;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetClientS3");//#LOGGER-NOPMD
    @Override
    public boolean exists(String path) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String makeURL( String relPath)
    {
        String tmpBaseDir=baseDir;
        if( StringUtilities.notBlank(path))
        {
            tmpBaseDir=path;
        }
        String tmpPath=server + "/" + tmpBaseDir + "/" + relPath;
        while( tmpPath.contains("//"))
        {
            tmpPath=tmpPath.replace("//", "/");
        }

        return "https://"+tmpPath;
    }
    @Override
    public void fetch(String fetchPath, File target) throws Exception {

        String url=makeURL( fetchPath);

        String cachePeriod="";
        if( NetUtil.REPAIR_MODE.get()==false)
        {
            cachePeriod =NetUtil.CACHE_PERIOD.get();
        }
        File tmpFile = ReST
            .builder(url)
            .setMethod(Method.GET)
            .setMinCachePeriod(cachePeriod)
//            .setBody(new File("/home/nigel/Pictures/smiling-star.png"),new ContentType("image/png"), DispositionType.ATTACHMENT)
            .setAuthorization(new AWSReSTAuthorization(accessKeyID,secretAccessKey))
            .getResponseAndCheck()
            .getContentAsFile();

        FileUtil.copy(tmpFile, target);
    }

    @Override
    public void remove(String path) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rename(String from, String to) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override

    public void send(File rawFile, String sendPath) throws Exception {

        String url=makeURL( sendPath);

        Response r = ReST
                .builder(url)
                .setMethod(Method.PUT)
                .setBody(rawFile,ContentType.APPLICATION_OCTET_STREAM, DispositionType.ATTACHMENT)
                .setAuthorization(new AWSReSTAuthorization(accessKeyID,secretAccessKey))
                .getResponseAndCheck();

        String data=r.getContentAsString();
        if( StringUtilities.notBlank(data))
        {
            LOGGER.info( data);
        }
    }

    @Override
    public boolean changeDirectory(final @Nonnull String path, boolean create) {
        if( path.contains("..")) throw new IllegalArgumentException("Invalid path: " + path);
        this.path=path;
        return true;
    }

    @Override
    public String[] retrieveFileList() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDirectory() throws Exception {
        return baseDir;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canConnect(String url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void activate() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void make(String url) throws Exception {

        URLParser parser = new URLParser(url);

        accessKeyID = parser.getUserName();
        secretAccessKey = parser.getPassword();
        server = parser.getHostName();

        baseDir = parser.getURI();
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public ProgressListener setProgressListener(ProgressListener monitor) {
        return null;
    }

    @Override
    public Object getUnderlyingConnectionObject() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
