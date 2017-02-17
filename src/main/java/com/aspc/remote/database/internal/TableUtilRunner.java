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
package com.aspc.remote.database.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.database.*;
import com.aspc.remote.util.misc.*;
import java.sql.SQLException;

/**
 *  NextRunner will update the database in another thread so that transactions are not effected.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       January 7, 2002, 12:31 PM
 */
public final class TableUtilRunner implements Runnable
{
    /**
     *
     * @param dBase the database
     * @param statement the statement
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    public static void perform( final DataBase dBase, final String statement) throws SQLException
    {
        TableUtilRunner r = new TableUtilRunner( dBase, statement);

        r.execute();
    }

    /**
     * schedule into the background.
     * @param dBase the database.
     * @param statement the statement
     */
    public static void schedule( final DataBase dBase, final String statement)
    {
        TableUtilRunner r = new TableUtilRunner( dBase, statement);

        ThreadPool.schedule( r);
    }

    private TableUtilRunner(DataBase dBase, String statement)
    {
        this.dBase = dBase;
        this.statement = statement;
    }

    private synchronized void execute() throws SQLException
    {
        if( dBase.getType().equals(DataBase.TYPE_HSQLDB))
        {
            run();
        }
        else
        {
            ThreadPool.schedule( this);

            try
            {
                wait( 120000);
            }
            catch( Exception e)
            {
                theError = e;
            }
        }

        if( complete == false)
        {
            throw new SQLException( "Statement did not complete");
        }

        if( theError != null)
        {
            if( theError instanceof SQLException)
            {
                throw (SQLException)theError;
            }
            else
            {
                throw new SQLException( theError.getMessage(), theError);
            }
        }
    }

    /**
     * run
     */
    @SuppressWarnings({"empty-statement", "SleepWhileHoldingLock", "SleepWhileInLoop"})
    @Override
    public synchronized void run()
    {
        CSQL sql;

        sql = new CSQL(dBase);

        for( int loop =0; true; loop++)
        {
            try
            {
                sql.execute( statement);
            }
            catch( DeadLockException dl)
            {
                LOGGER.error( "Deadlock", dl);

                /* Try five times to create the tables if deadlocks occur */
                if( loop < 5)
                {
                    try
                    {
                        Thread.sleep( 1000);
                    }
                    catch( Exception e)
                    {
                        ;
                    }
                    continue;
                }

                theError = dl;
            }
            catch( Throwable e)
            {
                LOGGER.error( statement, e);
                theError = e;
            }

            break;
        }

        complete = true;
        notifyAll();
    }

    private final String statement;

    private final DataBase    dBase;

    private Throwable   theError;
    private boolean     complete;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.TableUtilRunner");//#LOGGER-NOPMD
}
