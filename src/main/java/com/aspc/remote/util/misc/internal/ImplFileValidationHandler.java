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
package com.aspc.remote.util.misc.internal;

import com.aspc.remote.util.misc.*;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import javax.annotation.Nonnegative;
import javax.annotation.*;
import javax.annotation.Nullable;

/**
 *  File validation
 *
 * <B>THREAD MODE: MULTI-THREADED </B>
 * 
 * @author Luke
 * @since 17 September 1998
 */
public class ImplFileValidationHandler implements FileValidationHandler
{
    /**
     * Verifies a file against a checksum.
     * @param size the file size to validate
     * @param file the file to verify
     * @param chk the expected checksum for the file
     * @throws com.aspc.remote.util.misc.FileValidationException 
     */
    @Override
    public void validate( final @Nonnull File file, final @Nullable String chk, final @Nonnegative long size) throws FileValidationException
    {   
        if( file.exists() == false)
        {
            throw new FileValidationException( "file doesn't exist", null);
        }

        if( file.isDirectory())
        {
            throw new FileValidationException( "not a file but a directory", null);
        }
        
        // if the check sum passes then don't bother with the length
        if(chk!=null&& StringUtilities.isBlank( chk) == false)
        {            
            boolean matched = false;
            try
            {
                byte checkSum[] = StringUtilities.decodeBase64( chk.getBytes("ascii") );
                byte[] mresult = FileUtil.generateCheckSum( file);

                matched = MessageDigest.isEqual(checkSum, mresult);
            }
            catch( IOException e)
            {
                throw new FileValidationException( "could not perform a checksum", e);
            } 
            if( matched == false)
            {
                throw new FileValidationException( "failed checksum " + chk + " " + file, null);
            }
        }
        
        long len = file.length();

        if( size != -1)
        {
            if( size != len)
            {
               throw new FileValidationException( "file length is " + len + " not " + size, null);
            }
        }
    }
}
