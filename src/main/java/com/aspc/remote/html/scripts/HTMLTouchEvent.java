package com.aspc.remote.html.scripts;

/**
 *
 *  @author      Lei Gao
 *  @since       08 April 2014 
 */
public class HTMLTouchEvent extends HTMLEvent
{
    public static final String onTouchStart  = "onTouchStart";
    public static final String onTouchEnd  = "onTouchEnd";
    public static final String onTouchMove  = "onTouchMove";
    public static final String onTouchCancel  = "onTouchCancel";

    public HTMLTouchEvent(String name, String call)
    {
        super(name, call);
    }

}
