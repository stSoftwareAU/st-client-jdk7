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

package com.aspc.remote.database.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.database.CSQL;
import com.aspc.remote.database.DataBase;
import com.aspc.remote.database.DataBaseError;
import com.aspc.remote.database.NextNumber;
import com.aspc.remote.database.NextNumberException;
import com.aspc.remote.database.NoRowsFoundException;
import com.aspc.remote.database.NotFoundException;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.timer.Lap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.sql.Statement;
import javax.annotation.Nonnull;

/**
 *  NextRunner will update the database in another thread so that transactions are not effected.
 *
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED sql</i>
 *
 *  @author      Nigel Leck
 *  @since       January 7, 2002, 12:31 PM
 */
public class NextRunner implements Runnable
{
    /**
     * The property to disable the next number function logic.
     */
    public static final String PROPERTY_DISABLE_NEXT_NUMBER_FUNCTION="DISABLE_NEXT_NUMBER_FUNCTION";

    /**
     * disabled the next number logic function.
     */
    public static final boolean DISABLE_NEXT_NUMBER_FUNCTION;

    /** 
     * Create a next number runner
     * @param dBase the database
     * @param reqSize the number to create
     * @param sv the store
     */
    public NextRunner( final @Nonnull DataBase dBase, int reqSize, final @Nonnull StoredValue sv)
    {
        this.dBase = dBase;
        if( reqSize < 1)
        {
            long ct = System.currentTimeMillis();
            long lt = sv.getLastIncremented();

            /**
             * If the last time you asked for the next number is less than 5 secs ago then get 100 numbers
             */
            if( lt == 0 || lt + 5000 < ct)
            {
                cacheSize = 10;
            }
            else
            {
                cacheSize = 100;
            }
        }
        else
        {
            cacheSize = reqSize;
        }

        this.sv = sv;
    }

    /**
     * Generate more numbers
     * @return the next number.
     * @throws Exception a serious problem
     */
    public synchronized long createMore() throws Exception
    {
        ThreadPool.schedule( this);

        try
        {
            wait( 2 * 60 * 1000 );

            if( completed == false) theError = new Exception( "Next Number timed out");
        }
        catch( InterruptedException e)
        {
            theError = e;
        }

        if( theError != null)
        {
            if( theError instanceof DataBaseError)
            {
                throw new NextNumberException( "error while calculating the next number", theError);
            }
            else if( theError instanceof Exception)
            {
                throw (Exception)theError;
            }
            else
            {
                throw new Exception( theError.toString());
            }
        }

        return nextNr;
    }

    /**
     * run
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    @Override
    public synchronized void run()
    {
        String type = dBase.getType();

        if(
            DISABLE_NEXT_NUMBER_FUNCTION == false && (
                type.equals( DataBase.TYPE_ORACLE) ||
                type.equals( DataBase.TYPE_POSTGRESQL)
            )
        )
        {
            CSQL sql;

            sql = new CSQL(dBase).setReadOnly(false);
            sql.setQueryTimeOutSeconds(120);
            String eID=dBase.encodeString(sv.getID());
            StringBuilder sb=new StringBuilder( 200);
            if( DataBase.TYPE_ORACLE.equals(type))
            {
                sb.append("SELECT ");
                sb.append( NextNumber.NEXT_NUMBER_FUNCTION );
                sb.append( "(" );
                sb.append( eID);
                sb.append( "," );
                sb.append( cacheSize );
                sb.append( ") FROM dual" );
            }
            else
            {
                sb.append("SELECT * FROM ");
                sb.append( NextNumber.NEXT_NUMBER_FUNCTION );
                sb.append( "(" );
                sb.append( eID);
                sb.append( "," );
                sb.append( cacheSize );
                sb.append( ")" );
            }

            try
            {
                sql.findOne( sb.toString());

                long startNr;
                startNr = sql.getLong( 1);
                sv.setNumber( startNr, cacheSize);

                nextNr = sv.getNextNumber();
            }
            catch( Throwable e)
            {
                LOGGER.error( "could not call " + NextNumber.NEXT_NUMBER_FUNCTION , e);
                theError = e;
            }             
        }
        else if(
            type.equals( DataBase.TYPE_SYBASE) ||
            type.equals( DataBase.TYPE_POSTGRESQL)
        )
        {
            try
            {
                if( type.equals( DataBase.TYPE_SYBASE))
                {
                    updateSybase( true);
                }
                else
                {
                    updatePostgres( true);                
                }
            }
            catch( Throwable e)
            {
                LOGGER.error( "NextNumber.get( " + sv.getID() + "," + cacheSize + ") for " + dBase.getTypeKey(), e);
                theError = e;
            }
        }
        else
        {
            CSQL sql;

            sql = new CSQL(dBase);
            sql.setQueryTimeOutSeconds(120);
            try
            {
                sql.beginTransaction();

                doUpdate( sql);
                sql.commit();
            }
            catch( Throwable e)
            {
                LOGGER.error( "NextNumber.get( " + sv.getID() + "," + cacheSize + ") for " + dBase.getTypeKey(), e);
                theError = e;
                try
                {
                    sql.rollback();
                }
                catch( Exception e2)
                {
                    LOGGER.error( "couldn't commit", e2);
                }
            }
        }
        
        completed=true;
        notifyAll();
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    private void updateSybase( final boolean firstTime) throws Exception
    {        
        String eID=dBase.encodeString(sv.getID());
        StringBuilder statementBuffer = new StringBuilder(50);

        statementBuffer.append("BEGIN TRAN");

        statementBuffer.append( "\nUPDATE next_number SET next_nr=next_nr+");
        statementBuffer.append( cacheSize);
        statementBuffer.append( " WHERE name=");
        statementBuffer.append( eID);

        statementBuffer.append( "\nSELECT next_nr");

        statementBuffer.append( "-");
        statementBuffer.append( cacheSize);
        statementBuffer.append( " FROM next_number WHERE name=");
        statementBuffer.append( eID);
        statementBuffer.append( "\n");
        statementBuffer.append( "COMMIT TRAN");
        
        String statementSQL = statementBuffer.toString();
        
        Lap start=new Lap();
        Connection conn         = null;//NOPMD
        Statement stmt = null;
        long startNr = -1;
        try
        {
            conn = dBase.checkOutConnection();

            stmt = conn.createStatement();
           
            stmt.execute( statementSQL);
            // no result set for UPDATE statement
            stmt.getMoreResults();
            // result set for SELECT statement
            try (ResultSet rs = stmt.getResultSet()) {
                if( rs.next())
                {
                    startNr = rs.getLong(1);
                }
            }
            
            // just clear out any remaining
            stmt.getMoreResults();            
        }
        catch( Exception t)
        {
            if( conn != null)
            {
                conn.rollback();
            }
            String msg = "update failed for NextNumber.get( " + sv.getID() + "," + cacheSize + ") on " + dBase.getTypeKey();
            LOGGER.warn(msg, t);
            throw new DataBaseError( msg, t);
        }
        finally
        {
            if( stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "Could not close statement", e);
                }
            }
            
            dBase.checkInConnection( conn);
        }
        
        CSQL.recordTime( statementSQL, start, conn, null, 0,dBase);
        
        // The key must not exist yet
        if( startNr == -1)
        {
            if( firstTime)
            {
                CSQL sql = new CSQL( dBase);
                
                try
                {
                    sql.perform(
                        "INSERT INTO next_number(name,next_nr)VALUES(" + dBase.encodeString(sv.getID()) + ",1)"
                    );
                }
                catch( SQLException e)
                {
                    // we can continue as two servers maybe creating the record at one time.
                    LOGGER.warn( "possible race condition NextNumber.get( " + sv.getID() + "," + cacheSize + ") for " + dBase.getTypeKey(), e);
                }
                // try again we should have created the entry now.
                updateSybase( false);
                return;
            }
            else
            {
                throw new Exception( "update failed for NextNumber.get( " + sv.getID() + "," + cacheSize + ") on " + dBase.getTypeKey());
            }
        }
        
        sv.setNumber( startNr, cacheSize);

        nextNr = sv.getNextNumber();
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    private void updatePostgres( final boolean firstTime) throws Exception
    {        
        String eID=dBase.encodeString(sv.getID());

        StringBuilder statementBuffer = new StringBuilder(50);

        statementBuffer.append("BEGIN");
        statementBuffer.append(dBase.getBatchSeperator());

        statementBuffer.append( "\nUPDATE next_number SET next_nr=next_nr+");
        statementBuffer.append( cacheSize);
        statementBuffer.append( " WHERE name=");
        statementBuffer.append( eID);
        statementBuffer.append(dBase.getBatchSeperator());
        
        statementBuffer.append( "\nSELECT next_nr");

        statementBuffer.append( "-");
        statementBuffer.append( cacheSize);
        statementBuffer.append( " FROM next_number WHERE name=");
        statementBuffer.append( eID);
        statementBuffer.append(dBase.getBatchSeperator());

        statementBuffer.append( "\nCOMMIT");
        
        String statementSQL = statementBuffer.toString();
        
        Lap start=new Lap();
        Connection conn         = null;//NOPMD
        Statement stmt = null;
        long startNr = -1;
        try
        {
            conn = dBase.checkOutConnection();

            stmt = conn.createStatement();
           
            stmt.execute( statementSQL);
            // no result set for BEGIN statement
            stmt.getMoreResults();
            // no result set for UPDATE statement
            stmt.getMoreResults();
            try (ResultSet rs = stmt.getResultSet()) {
                if( rs.next())
                {
                    startNr = rs.getLong(1);
                }
            }
            // just clear out any remaining
            stmt.getMoreResults();            
        }
        catch( Exception t)
        {
            if( conn != null)
            {
                conn.rollback();
            }
            String msg = "update failed for NextNumber.get( " + sv.getID() + "," + cacheSize + ") on " + dBase.getTypeKey();
            LOGGER.warn(msg, t);
            throw new DataBaseError( msg, t);
        }
        finally
        {
            if( stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "Could not close statement", e);
                }
            }
            
            dBase.checkInConnection( conn);
        }
        
        CSQL.recordTime( statementSQL, start, conn, null, 0,dBase);
        
        // The key must not exist yet
        if( startNr == -1)
        {
            if( firstTime)
            {
                CSQL sql = new CSQL( dBase);
                sql.setQueryTimeOutSeconds(120);
                try
                {
                    sql.perform(
                        "INSERT INTO next_number(name,next_nr)VALUES(" + dBase.encodeString(sv.getID()) + ",1)"
                    );
                }
                catch( SQLException e)
                {
                    // we can continue as two servers maybe creating the record at one time.
                    LOGGER.warn( "possible race condition NextNumber.get( " + sv.getID() + "," + cacheSize + ") for " + dBase.getTypeKey(), e);
                }
                // try again we should have created the entry now.
                updatePostgres( false);
                return;
            }
            else
            {
                throw new Exception( "update failed for NextNumber.get( " + sv.getID() + "," + cacheSize + ") on " + dBase.getTypeKey());
            }
        }
        
        sv.setNumber( startNr, cacheSize);

        nextNr = sv.getNextNumber();
    }

    private void doUpdate( final CSQL sql) throws Exception
    {
        String eID=dBase.encodeString(sv.getID());
        try
        {
            StringBuilder sb = new StringBuilder( 100);

            sb.append("UPDATE ");
            sb.append("next_number SET next_nr=next_nr+");
            sb.append(cacheSize);
            sb.append(" WHERE name=");
            sb.append(eID);

            for( int attempt=0;true;attempt++){
                try{
                    sql.findOne(
                        sb.toString()
                    );
                    break;
                }
                catch( SQLTransactionRollbackException r)
                {                
                    if(attempt>3)
                    {
                        throw r;
                    }
                    LOGGER.warn( sb.toString(), r);
                }
            }
        }
        catch( NotFoundException nf)
        {
            StringBuilder sb = new StringBuilder( 100);

            sb.append("INSERT ");

            sb.append("INTO next_number(name,next_nr)VALUES(");
            sb.append(eID);
            sb.append(",");
            sb.append((cacheSize + 1));
            sb.append( ")");

            sql.findOne(
                sb.toString()
            );
        }

        StringBuilder buffer = new StringBuilder(50);

        buffer.append( "SELECT ");
        buffer.append( "next_nr");

        buffer.append( "-");
        buffer.append( cacheSize);
        buffer.append( " FROM next_number WHERE name=");
        buffer.append( eID);
        
        sql.perform(
            buffer.toString()
        );

        long startNr=-1;
        
        while( sql.next())
        {
            long tmpStartNr;
            tmpStartNr = sql.getLong( 1);
            
            if( startNr == -1)
            {
                startNr=tmpStartNr;
            }
            else 
            {
                LOGGER.warn( "multiple rows returned for " + buffer);
                if( tmpStartNr > startNr)
                {
                    startNr=tmpStartNr;
                }
            }
        }
        
        if( startNr < 0)
        {
            throw new NoRowsFoundException( "No Rows Found for :-\n" + buffer);
        }
        
        sv.setNumber( startNr, cacheSize);

        nextNr = sv.getNextNumber();
            
    }

    static
    {
        String temp = System.getProperty( PROPERTY_DISABLE_NEXT_NUMBER_FUNCTION, "NO").toLowerCase();
        
        if( temp.startsWith("y") || temp.startsWith("t") )
        {
            DISABLE_NEXT_NUMBER_FUNCTION=true;
        }
        else
        {
            DISABLE_NEXT_NUMBER_FUNCTION=false;
        }
    }

    private final StoredValue sv;//NOPMD
    private boolean completed;

    /**
     * The actual database
     */
    private final DataBase    dBase;//NOPMD

    private long        nextNr;
    private final int         cacheSize;//NOPMD

    /** an error occurred during load */
    private Throwable   theError;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.NextRunner");//#LOGGER-NOPMD
}
