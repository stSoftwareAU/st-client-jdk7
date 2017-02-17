/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.formula;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.ServerSecurityManager;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.*;
import org.apache.commons.logging.Log;

/**
 *  Formula
 *
 *  A formula is made up of operators, functions, numerical values and user defined data items.
 *
 *  Operators are ()-+/*,"'
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED formula is shared</i>
 *
 *  @author         Nigel Leck
 *  @since          10 July 2012
 *  
 */
public class Formula
{
    /**
     * number of times to call before we JIT the formula
     */
    public static final int JIT_CALL_LIMIT=10;

    private final String definition;
    private CompiledScript script;
    private final AtomicLong CALL_COUNT=new AtomicLong();
    private final AtomicBoolean JIT=new AtomicBoolean();
    private static final ScriptEngine ENGINE;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.formula.Formula");//#LOGGER-NOPMD


    /**
     * Create a new formula
     * @param definition
     */
    public Formula(final @Nonnull String definition) 
    {
        if( definition == null )
        {
            throw new IllegalArgumentException( "Formula must not be null");
        }
        this.definition = definition.trim();

    }

    /**
     * Has this formula been compiled.
     * @return true if compiled.
     */
    @CheckReturnValue
    public boolean isCompiled()
    {
        return script != null;
    }

    /**
     *
     * @param binding The binding
     * @return the value
     * @throws Exception a serious problem
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    @CheckReturnValue @Nullable
    public Object compute( final Bindings binding) throws Exception
    {
        SimpleScriptContext ctxt = new SimpleScriptContext( );
        ctxt.setBindings(binding, SimpleScriptContext.ENGINE_SCOPE);
        ctxt.setBindings(new SimpleBindings(), SimpleScriptContext.GLOBAL_SCOPE);

        long count = CALL_COUNT.addAndGet(1);
        if( script == null && count > JIT_CALL_LIMIT)
        {
            if( ENGINE instanceof Compilable && JIT.getAndSet(true) == false)
            {
                script = ((Compilable)ENGINE).compile(definition );
            }
        }

        try
        {
            ServerSecurityManager.modeUserScriptAccess(true);

            Object eval;
            if( script != null)
            {
                eval= script.eval( ctxt);
            }
            else
            {
                eval =ENGINE.eval( definition, ctxt);
            }

            return eval;
        }
        catch( Throwable se)
        {
            Throwable t = se.getCause();
            if( t instanceof Exception )
            {
                throw (Exception)t;
            }

            if( se instanceof Exception )
            {
                throw (Exception)se;
            }

            throw new Exception( definition, se);
        }
        finally
        {
            ServerSecurityManager.modeUserScriptAccess(false);
        }
    }

    @CheckReturnValue @Nonnull
    public String getDefinition()
    {
        return definition;
    }

    static
    {
        ScriptEngineManager em = new ScriptEngineManager();

        ENGINE= em.getEngineByName("JavaScript");
        
        assert ENGINE!=null: "no engine found";
    }
}
