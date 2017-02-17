package com.aspc.remote.util.misc;

/**
 *
 *  @author      Lei Gao
 *  @since       12 August 2015 
 */
public class PathNotFoundException extends Exception
{
    public PathNotFoundException()
    {
    }

    public PathNotFoundException(String message)
    {
        super(message);
    }

    public PathNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PathNotFoundException(Throwable cause)
    {
        super(cause);
    }

}
