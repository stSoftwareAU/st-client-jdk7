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
package com.aspc.remote.javaspell;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;

/**
 *
 * @author Tim Stegeman
 */
public class ASpellWrapper {
    private static final Pattern LINE_PAT = Pattern.compile("& (.+) [0-9]+ ([0-9]+): (.+)");
    private final String language;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.javaspell.ASpellWrapper");//#LOGGER-NOPMD
    private static Locale SUPPORTED_LOCALE[];

    private final HashSet<String> ignoreSet=new HashSet();
    
    public ASpellWrapper(final String language) {
        this.language = language;
    }

    /**
     * The list of languages we can check.
     * 
     * @return the list.
     */
    public static Locale[] listLocales()
    {
        Locale list[]=SUPPORTED_LOCALE;
        if(list == null)
        {
            try
            {
                ArrayList<Locale>tmp=new ArrayList<>();
                for( Locale l: Locale.getAvailableLocales())
                {
                    String lang=l.toLanguageTag();
                    if( lang.contains("-"))continue;

                    ASpellWrapper aSpell=new ASpellWrapper(lang);

                    try
                    {
                        aSpell.checkString("hello world");
                        tmp.add(l);
                    }
                    catch( ASpellException ase)
                    {
                    }
                }
                list=new Locale[tmp.size()];

                tmp.toArray(list);
                SUPPORTED_LOCALE=list;
            }
            catch( IOException ioe)
            {
                LOGGER.warn( "could not load", ioe);
                list=new Locale[]{};
                SUPPORTED_LOCALE=list;
            }
        }
        return list.clone();
    }

    /**
     * Is ASpell supported ?
     * 
     * @return true if supported.
     */
    public static boolean isSupported()
    {
        if (CUtilities.isWindose())
        {
            return false;
        }
        else
        {
            Locale langs[];
            langs = listLocales();
            if( langs.length > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * The list of words to ignore. 
     * @param wordList A comma separated list of words. 
     * @return this
     */
    public ASpellWrapper setPersonalWordList( final String wordList)
    {
        ignoreSet.clear();
        for( String word: wordList.split(","))
        {
            ignoreSet.add(word.trim());
        }
        return this;
    }
    
    /** 
     * Check the spelling of text. 
     * 
     * aspell -a --lang=%s --encoding=utf-8 -H --rem-sgml-check=alt
     * 
     * @param text the text to check.
     * @return the list of misspelt words.
     * @throws IOException a problem occurred. 
     * @throws ASpellException could not check the spelling.
     */
    public List<SpellCheckResult> checkString(final String text) throws IOException, ASpellException {
       
        String line;

        ProcessBuilder processBuilder = new ProcessBuilder(
            "aspell", 
            "-a", 
            "--lang=" + language, 
            "--encoding=utf-8",
            "-H",
            "--rem-sgml-check=alt"    
        );
        List<SpellCheckResult> results= new ArrayList<>();
        Process process = processBuilder.start();
        try
        {
            try (PrintStream ps = new PrintStream(process.getOutputStream(), true)) {
                ps.print(text);
            }

            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) 
            {
                BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorInput.readLine()) != null) {
                    throw new ASpellException(line.trim());
                }   
                
                int index = 0;

                while ((line = input.readLine()) != null) {
                    if (line.startsWith("*")) {
                        index++;
                    } else if (line.startsWith("&")) {

                        SpellCheckResult spr=parseLine(index++, line);
                        if( spr == null)
                        {
                            throw new ASpellException( "could not parse " + line);
                        }
                            
                        if( ignoreSet.contains(spr.getWord())==false)
                        {
                            results.add(spr);
                        }
                    }
                }
            }
        }
        finally
        {
            process.destroy();
        }

        return results;
    }

    private SpellCheckResult parseLine(final int index, final String line) {
        Matcher matcher = LINE_PAT.matcher(line);
        if (matcher.find()) {
            String word = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            String suggestionsStr = matcher.group(3);
            String[] suggestionsArr = suggestionsStr.split(", ");
            List<String> suggestions = new ArrayList<>();
            for (String suggestion : suggestionsArr) {
                suggestions.add(suggestion.trim());
            }
            return new SpellCheckResult(word, index, start, suggestions);
        }
        return null;
    }
}
