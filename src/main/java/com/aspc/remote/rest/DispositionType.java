package com.aspc.remote.rest;

import com.aspc.remote.util.misc.StringUtilities;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 *  @author      Nigel Leck
 *  @since       13 August 2017
 */
public class DispositionType
{
    public final static DispositionType ATTACHMENT=new DispositionType( "attachment");
    public final static DispositionType FORM_DATA=new DispositionType( "form-data");

    public final String type;

    private DispositionType(final @Nonnull String type)
    {
        if( type == null) throw new IllegalArgumentException( "type is mandatory");
        this.type=type;        
    }

    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return "Content-Disposition: " + type;
    }
    
}
