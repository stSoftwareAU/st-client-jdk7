package com.aspc.remote.util.misc;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  Currently support json path like:
 *  <ul>
 *    <li>Show whole json or json array: $</li>
 *    <li>Show the second element of a json array: $[1]</li>
 *    <li>Show the element with this path: $.a.b[1].c[2].d</li>
 *    <li>Show the element with this path: $[1].a</li>
 *  </ul>
 *  
 *  Currently does <strong>NOT</strong> support json name that contains <span style='color: red;font-weight: bold;'>"</span> or 
 *  <span style='color: red;font-weight: bold;'>.</span> or <span style='color: red;font-weight: bold;'>[</span> or 
 *  <span style='color: red;font-weight: bold;'>]</span>
 * 
 * 
 *  @author      Lei Gao
 *  @since       12 August 2015 
 */
public class JsonPath
{
    /**
     * read the value of the given path
     * @param json json object
     * @param jsonPath json path
     * @return 
     * @throws PathNotFoundException 
     */
    @CheckReturnValue @Nonnull
    public static Object read(final @Nonnull JSONObject json, final @Nonnull String jsonPath) throws PathNotFoundException
    {
        assert json != null : "json can not be null";
        checkPath(jsonPath);
        if(jsonPath.startsWith("$["))
        {
            throw new UnsupportedOperationException("json path for a JSONObject must not starts with $[\\d+]. " + jsonPath);
        }
        
        String[] names = jsonPath.split("\\.");
        Object j = json;
        for(int i = 1;i < names.length;i++)
        {
            String name = names[i];
            int index = -1;
            if(name.matches(".*\\[\\d+\\]"))
            {
                int begin = name.indexOf('[');
                int end = name.indexOf(']');
                index = Integer.parseInt(name.substring(begin + 1, end));
                name = name.substring(0, begin);
            }
            
            Object item;
            if( j instanceof JSONObject)
            {
                JSONObject jsonObject=((JSONObject)j);
                if( jsonObject.has(name))
                {
                    item=jsonObject.get(name);
                }
                else
                {
//                    return new JSONArray();
                    throw new PathNotFoundException(name + " no match"); 
                }
            }
            else if( j instanceof JSONArray)
            {
                String fn=name.replace(" ", "").toLowerCase();
                if( fn.equals("length()"))
                {
                    JSONArray a=(JSONArray)j;
                    return a.length();
                }
                else
                {
                    throw new PathNotFoundException(name + " not a function");
                }
            }
            else
            {
                throw new PathNotFoundException("not a object was: " + j);
            }
            
            try
            {
                if(i == names.length - 1)
                {
                    if(index > -1)
                    {
                        return ((JSONArray)item).get(index);
                    }
                    else
                    {
                        if( item!=null)
                        {
                            return item;
                        }
                        else
                        {
                            return new JSONArray();
                        }
                    }
                }
                
                if(index > -1)
                {
                    JSONArray a=((JSONArray)item);
                    if( a.length()>index)
                    {
                        j = a.getJSONObject(index);
                    }
                    else
                    {
                        return "";
                    }
                }
                else
                {
                    j = item;
                }
            }
            catch(JSONException e)
            {
                throw new PathNotFoundException(e);
            }
        }
        
        //for path $
        return json;
    }
    
    /**
     * read the value of the given path for a JSONArray
     * @param json json array
     * @param jsonPath path
     * @return
     * @throws PathNotFoundException 
     */
    @CheckReturnValue @Nonnull
    public static Object read(final @Nonnull JSONArray json, final @Nonnull String jsonPath) throws PathNotFoundException
    {
        assert json != null : "json can not be null";
        checkPath(jsonPath);
        
        if(jsonPath.startsWith("$."))
        {
            throw new UnsupportedOperationException("json path for a JSONArray must not starts with '$.'. " + jsonPath);
        }
        
        String[] names = jsonPath.split("\\.");
        JSONObject j = new JSONObject();
        for(int i = 0;i < names.length;i++)
        {
            String name = names[i];
            int index = -1;
            if(name.matches(".*\\[\\d+\\]"))
            {
                int begin = name.indexOf('[');
                int end = name.indexOf(']');
                index = Integer.parseInt(name.substring(begin + 1, end));
                name = name.substring(0, begin);
            }
            if(names.length == 1)
            {
                if(index == -1)
                {
                    //for path $
                    return json;
                }
                else
                {
                    //for path $[*]
                    try
                    {
                        return json.get(index);
                    }
                    catch(JSONException e)
                    {
                        throw new PathNotFoundException(e);
                    }
                }
            }
            else
            {
                if(i == 0)
                {
                    j = json.getJSONObject(index);
                    continue;
                }
            }
            try
            {
                if(i == names.length - 1)
                {
                    if(index > -1)
                    {
                        return j.getJSONArray(name).get(index);
                    }
                    else
                    {
                        return j.get(name).toString();
                    }
                }
                if(index > -1)
                {
                    j = j.getJSONArray(name).getJSONObject(index);
                }
                else
                {
                    j = j.getJSONObject(name);
                }
            }
            catch(JSONException e)
            {
                throw new PathNotFoundException(e);
            }
        }
        
        return "";
    }
    
    public static void checkPath(final String jsonPath)
    {
        if(jsonPath == null)
        {
            throw new UnsupportedOperationException("json path can not be null");
        }
        if(jsonPath.startsWith("$") == false)
        {
            throw new UnsupportedOperationException("json path must starts with $. " + jsonPath);
        }
        if(jsonPath.endsWith("."))
        {
            throw new UnsupportedOperationException("json path must not ends with '.'. " + jsonPath);
        }
        if(jsonPath.matches("\\$[^\\[\\.]{1}.*"))
        {
            throw new UnsupportedOperationException("json path $. or $[ " + jsonPath);
        }
        if(jsonPath.contains(".."))
        {
            throw new UnsupportedOperationException("json path currently doesn't support '..' " + jsonPath);
        }
        String[] list = jsonPath.split("\\.");
        for(String s : list)
        {
            if(s.matches(".*\\[\\D*\\]"))
            {
                throw new UnsupportedOperationException("json path must only have numbers inside []. " + jsonPath);
            }
            else if(s.matches(".*\\[\\d+\\].+"))
            {
                throw new UnsupportedOperationException("json path array node must ends with [\\d]. " + jsonPath);
            }
            else if(s.matches("\\[\\d+\\]"))
            {
                throw new UnsupportedOperationException("json path array node must has a name]. " + jsonPath);
            }
        }
    }
}
