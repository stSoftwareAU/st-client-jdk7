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
package com.aspc.remote.util.misc;

import com.aspc.remote.database.NotFoundException;

import com.aspc.remote.memory.HashMapFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.commons.logging.Log;


/**
 *  StringUtilities
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED utilities</i>
 *
 *  @author      Nigel Leck
 *  @since       2 July 1997
 */
@Immutable
public final class StringUtilities
{
    /**
     * Valid IP address
     */
    public static final Pattern IP_PATTERN=Pattern.compile("^((::1)|([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])|([0-9a-f]{1,4}:){7}([0-9a-f]){1,4})$");
    
    /**
     * Local IP address
     * 
     * http://en.wikipedia.org/wiki/Private_network
     */
    public static final Pattern LOCAL_IP_PATTERN=Pattern.compile("(0:|(127\\.0|10\\.[0-9]+|172\\.[0-9]+|192\\.168)\\.[0-9]+\\.[0-9]+).*");
    
    public static final Pattern HOST_PATTERN=Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    /**
     * UUENCODED pattern 
     */
    public static final Pattern UUENCODED_PATH_PATTERN=Pattern.compile("([/a-z0-9\\+\\.\\_]|%[0-9a-f]{2})*", Pattern.CASE_INSENSITIVE);
    
    /**
     * URI pattern matching 
     */
    public static final Pattern URI_PATTERN=Pattern.compile("^(([a-z]{3,5}|s3)://([a-z0-9\\+/\\-_\\.]+:[a-z0-9\\+/\\-_\\.:]+@|)|/)([!',a-z0-9\\+\\(\\)/\\-_\\.~\\\\:]|%([0-9a-f][0-9a-e]|[013-9a-f]f))*(|:[0-9]+)/*(|\\?[\\w\\-:;,'./%&=~@\\+\\(\\)\\*]*)(|\\#[\\w\\-:;,'./%&=~@\\+\\(\\)\\*]*)$", Pattern.CASE_INSENSITIVE);

    /**
     * valid phone regular expression
     */
    public static final Pattern PHONE_REGEX=Pattern.compile("^(\\+[0-9]+)*((\\(| |)*[0-9]+(\\)|-| |)*)+((ext|x)+ *[0-9]+)*$");
    
    /**
     * validate mime type
     */
    public static final Pattern FILE_MIME_TYPE_PATTERN = Pattern.compile("^[a-z\\-A-Z]+/[\\.a-z0-9_\\-A-Z\\+]+$");

    /**
     * Encoding GZIP - default
     */
    public static final String COMPRESSION_GZIP = "GZIP";
    /**
     * Encoding NONE
     */
    public static final String COMPRESSION_NONE = "NONE";
    /**
     * Encoding DEFLATE
     */
    public static final String COMPRESSION_DEFLATE = "DEFLATE";
    
    /**
     *
     */
    public static final String ENCODED_URL_QUOTE = "%27";
    /**
     *
     */
    public static final String ENCODED_URL_AND = "%26";
    /**
     *
     */
    public static final String ENCODED_URL_QUESTION = "%3f";
    /**
     *
     */
    public static final String ENCODED_URL_EQUALS = "%3d";
    /**
     *
     */
    public static final String ENCODED_URL_SPACE = "+";
    /**
     *
     */
    public static final int PASSWORD_SEQUENTIAL_LENGTH = 4;
    /**
     *
     */
    public static final String[] HEX_CODES =
    {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07", "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17", "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27", "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37", "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47", "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57", "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67", "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77", "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87", "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97", "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
        "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7", "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
        "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7", "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
        "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7", "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
        "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7", "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
        "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7", "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
        "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7", "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
    };
    /**
     *
     */
    private static final boolean[] ASCII_WHITE_SPACE;

    /**
     *
     * @param c
     * @return
     */
    @CheckReturnValue
    public static boolean isAsciiWhiteSpace( final char c)
    {
        return c < 255 && ASCII_WHITE_SPACE[c];
    }
    
    private static final HashMap<String, Character> HTML_CHARACTER;
    
    private static final NumberFormatException NFE;


    /* The following arrays are used by the toString() function to construct
       the standard Roman numeral representation of the number.  For each i,
       the number numbers[i] is represented by the corresponding string, letters[i].
    */

    private static final int[]    ROMAN_NUMBERS = { 1000,  900,  500,  400,  100,   90,
                                          50,   40,   10,    9,    5,    4,    1 };

    private static final  String[] ROMAN_LETTERS = { "M",  "CM",  "D",  "CD", "C",  "XC",
                                        "L",  "XL",  "X",  "IX", "V",  "IV", "I" };

    @CheckReturnValue @Nonnull
    public static String safeMessage( final @Nullable String message)
    {
        if( isBlank(message)) return "";
        assert message!=null;
        Pattern p=Pattern.compile("password[:=]( *.{1,5}[^,\\}\\)]*)", Pattern.CASE_INSENSITIVE);
        String tmpMessage=message;
        
        Matcher m= p.matcher(tmpMessage);
        if( m.find())
        {
            tmpMessage=tmpMessage.substring(0, m.start(1));
            tmpMessage+="******";
            tmpMessage+=tmpMessage.substring(m.start(1));
        }
        return tmpMessage;
    }
    
    /**
     * An object of type RomanNumeral is an integer between 1 and 3999.  It can
     * be constructed either from an integer or from a string that represents
     * a Roman numeral in this range.  The function toString() will return a
     * standardized Roman numeral representation of the number.  The function
     * toInt() will return the number as a value of type int.
    * @param arabic
     * @return the value
     */
    @CheckReturnValue
    public static String romanNumeral(final int arabic)
    {
       if (arabic < 1)
          throw new NumberFormatException("Value of RomanNumeral must be positive.");
       if (arabic > 3999)
          throw new NumberFormatException("Value of RomanNumeral must be 3999 or less.");
       String roman = "";  // The roman numeral.
       int N = arabic;        // N represents the part of num that still has
                           //   to be converted to Roman numeral representation.
       for (int i = 0; i < ROMAN_NUMBERS.length; i++) {
          while (N >= ROMAN_NUMBERS[i]) {
             roman += ROMAN_LETTERS[i];
             N -= ROMAN_NUMBERS[i];
          }
       }
       return roman;
    }

    /*
     * The Roman number with the given representation.
     * For example, RomanNumeral("xvii") is 17.  If the parameter is not a
     * legal Roman numeral, a NumberFormatException is thrown.  Both upper and
     * lower case letters are allowed.
     */
    @CheckReturnValue
    public static int romanNumeral(final String roman)
    {

       if (roman.length() == 0)
          throw new NumberFormatException("An empty string does not define a Roman numeral.");

       String tmpRoman = roman.toUpperCase();  // Convert to upper case letters.

       int i = 0;       // A position in the string, roman;
       int arabic = 0;  // Arabic numeral equivalent of the part of the string that has
                        //    been converted so far.

       while (i < tmpRoman.length()) {

          char letter = tmpRoman.charAt(i);        // Letter at current position in string.
          int number = letterToNumber(letter);  // Numerical equivalent of letter.

          i++;  // Move on to next position in the string

          if (i == tmpRoman.length()) {
                // There is no letter in the string following the one we have just processed.
                // So just add the number corresponding to the single letter to arabic.
             arabic += number;
          }
          else {
                // Look at the next letter in the string.  If it has a larger Roman numeral
                // equivalent than number, then the two letters are counted together as
                // a Roman numeral with value (nextNumber - number).
             int nextNumber = letterToNumber(tmpRoman.charAt(i));
             if (nextNumber > number) {
                  // Combine the two letters to get one value, and move on to next position in string.
                arabic += (nextNumber - number);
                i++;
             }
             else {
                  // Don't combine the letters.  Just add the value of the one letter onto the number.
                arabic += number;
             }
          }

       }  // end while

       if (arabic > 3999)
          throw new NumberFormatException("Roman numeral must have value 3999 or less.");

       return arabic;

    } // end constructor


    /**
     * Find the integer value of letter considered as a Roman numeral.  Throws
     * NumberFormatException if letter is not a legal Roman numeral.  The letter
     * must be upper case.
     */
    @CheckReturnValue
    private static int letterToNumber(char letter)
    {
       switch (letter) {
          case 'I':  return 1;
          case 'V':  return 5;
          case 'X':  return 10;
          case 'L':  return 50;
          case 'C':  return 100;
          case 'D':  return 500;
          case 'M':  return 1000;
          default:   throw new NumberFormatException(
                       "Illegal character \"" + letter + "\" in Roman numeral");
       }
    }

    /**
     * De-Duplicate words in a string.
     * "abc,XYZ,Abc,hello,world,xyz" -> "abc,XYZ,hello,world"
     * @param words
     * @param delimator
     * @return The unique words list
     */
    @CheckReturnValue @Nonnull
    public static String deduplicate( final @Nonnull String words, final char delimator )
    {
        LinkedHashMap<String, String>list=new LinkedHashMap<>();
        String regex;
        if(
            delimator == '\\' ||
            delimator == '[' ||
            delimator == '?' ||
            delimator == '$' ||
            delimator == '.' ||
            delimator == '(' ||
            delimator == '+'
        )
        {
            regex ="\\" + delimator;
        }
        else
        {
            regex= "" + delimator;
        }
        String[] wordList = words.split(regex);

        for( String w: wordList)
        {
            String key = w.toLowerCase().trim();
            String value = w.trim();
            if( key.length()==0) continue;
            if( list.containsKey(key) == false)
            {
                list.put(key, value);
            }
        }

        StringBuilder sb=new StringBuilder(words.length());

        list.values().stream().forEach((w) -> {
            if( sb.length() != 0)
            {
                sb.append(delimator);
            }
            sb.append( w);
        });

        return sb.toString();
    }

    /**
     * Convert any title to a web safe & human readable path
     * @param title to convert
     * @return the web safe path
     */
    @CheckReturnValue
    public static String webSafePath(final @Nullable String title)
    {
        if( title == null || isBlank(title))
        {
            return "_blank_";
        }
        String path=title.toLowerCase().trim();
        StringBuilder sb=new StringBuilder();
        boolean lastWhitespace=true;
        boolean lastSlash=false;

        String replaceList[][] ={
            {"Œ","CE"}, {"œ","ce"}, {"¥","Y"}, {"Ÿ","Y"}, {"µ","u"}, {"ü","u"},
            {"Š","S"}, {"š","s"}, {"Đ","Dj"}, {"đ","dj"}, {"Ž","Z"}, {"ž","z"},
            {"Č","C"}, {"č","c"}, {"Ć","C"}, {"ć","c"}, {"À","A"}, {"Á","A"}, {"Â","A"},
            {"Ã","A"}, {"Ä","A"}, {"Å","A"}, {"Æ","A"}, {"Ç","C"}, {"È","E"},
            {"É","E"}, {"Ê","E"}, {"Ë","E"}, {"Ì","I"}, {"Í","I"}, {"Î","I"},
            {"Ï","I"}, {"Ñ","N"}, {"Ò","O"}, {"Ó","O"}, {"Ô","O"}, {"Õ","O"},
            {"Ö","O"}, {"Ø","O"}, {"Ù","U"}, {"Ú","U"}, {"Û","U"}, {"Ü","U"},
            {"Ý","Y"}, {"Þ","B"}, {"ß","Ss"}, {"à","a"}, {"á","a"}, {"â","a"},
            {"ã","a"}, {"ä","a"}, {"å","a"}, {"æ","a"}, {"ç","c"}, {"è","e"},
            {"é","e"}, {"ê","e"}, {"ë","e"}, {"ì","i"}, {"í","i"}, {"î","i"},
            {"ï","i"}, {"ð","o"}, {"ñ","n"}, {"ò","o"}, {"ó","o"}, {"ô","o"},
            {"õ","o"}, {"ö","o"}, {"ø","o"}, {"ù","u"}, {"ú","u"}, {"û","u"},
            {"ý","y"}, {"ý","y"}, {"þ","b"}, {"ÿ","y"}, {"Ŕ","R"}, {"ŕ","r"},
            {"¼","1/4"},{"½","1/2"},{"¾","3/4"}, {"¢", "C"}, {"¥","y"},{"§","S"},
            {"ª", "a"},{"®","r"},{"º","o"}
        };

        for( char c: path.toCharArray())
        {
            if( Character.isWhitespace(c) || c == '-' || c == '_')
            {
                if( lastWhitespace == false)
                {
                    if(  c == '-' || c == '_')
                    {
                        sb.append(c);
                    }
                    else
                    {
                        sb.append("-");
                    }
                }
                lastWhitespace=true;
                lastSlash=false;
            }
            else
            {
                if( c == '/')
                {
                    if( lastSlash == false)
                    {
                        sb.append("/");
                    }
                    lastWhitespace=false;
                    lastSlash=true;
                }
                else if (( c >= 'a' && c <= 'z') || ( c>= '0' && c<='9'))
                {
                    lastWhitespace=false;
                    lastSlash=false;
                    sb.append( c);
                }
                else
                {
                    String t = Character.toString(c);

                    for( String pair[] : replaceList)
                    {
                        if( t.equals( pair[0]))
                        {
                            sb.append( pair[1]);
                            lastSlash=false;
                            lastWhitespace=false;
                            break;
                        }
                    }
                }
            }
        }

        String safePath = sb.toString().toLowerCase();

        return safePath;
    }

    /**
     * Invalid character found in the request target. The valid characters are defined in RFC 7230 and RFC 3986
     * @param c the character
     * @return true if valid in URL
     */
    @CheckReturnValue
    public static boolean isCharacterValidURL( final Character c)
    {
        if( c >= 'A' && c <= 'Z') return true;
        if( c >= 'a' && c <= 'z') return true;
        if( c >= '0' && c <= '9') return true;

        return "%-,._~:/?#[]@!$&'()*+,;=".indexOf(c) != -1;
    }

    public static String[] listHashTags( final String text)
    {
        HashMap<String,String>map=HashMapFactory.create();
        ArrayList<String>list=new ArrayList<>();
        int lastPos = 0;
        while( true)
        {
            int pos = text.indexOf('#', lastPos);

            if( pos == -1) break;

            int nextPos = pos+1;
            while( nextPos < text.length())
            {
                char c = text.charAt(nextPos);
                if(
                    c != '_' &&
                    (
                        Character.isWhitespace(c) || Character.isLetterOrDigit(c) == false
                    )
                )
                {
                    break;
                }
                else if( c == '#')
                {
                    pos = nextPos;
                }
                nextPos++;
            }
            if( pos + 1 < nextPos )
            {
                String hash = text.substring(pos + 1, nextPos);
                String key = hash.toUpperCase();
                if( map.containsKey(key)== false)
                {
                    map.put(key, "");
                    list.add(hash);
                }
            }
            lastPos = nextPos;
        }
        String tmp[] = new String[list.size()];
        list.toArray(tmp);
        return tmp;
    }
    
    @CheckReturnValue
    public static String shrinkTo140( final String text, final String preHint[][], final String postHint[][])
    {
        return shrinkTo( text, preHint, postHint, 140, 140);
    }
    
    @CheckReturnValue
    public static int twitterLength( final @Nonnull String text)
    {
        int realLength = text.length();
        int adjustment=0;
        int lastPos = 0;
        while( lastPos<realLength)
        {
            int pos =text.indexOf("http://", lastPos);

            if( pos == -1) break;

            for( int end = pos; end < realLength;end++)
            {
                char c = text.charAt(end);
                if(
                    end + 1 >=realLength ||
                    c == ' ' ||
                    c == '\t' ||
                    c == '\n' ||
                    c == '\r'
                )
                {
                    if( end-pos > 21)
                    {
                        adjustment+= end-pos - 21;
                    }

                    lastPos=end;
                    break;
                }
            }

            if( lastPos <= pos) break;
        }

        return realLength - adjustment;
    }

    /**
     *
     * What letters can be compressed? Here's the list: cc, ms, ns, ps, in, ls, fi, fl, ffl, ffi, iv, ix, vi, oy, ii, xi, nj, ". " (period space), and ", " (comma space).
     *
     * @param text
     * @param preHint
     * @param postHint
     * @param requestedMaxLength the max length
     * @param requestedTargetLength the length the user waits the tweet
     * @return the value
     */
    @CheckReturnValue
    public static String shrinkTo(
        final String text,
        final String preHint[][],
        final String postHint[][],
        final int requestedTargetLength,
        final int requestedMaxLength
    )
    {
        if( text == null) throw new IllegalArgumentException( "The text must not be null");
        if( requestedTargetLength <= 0) throw new IllegalArgumentException( "target length must be greater than zero was " + requestedTargetLength);
        if( requestedMaxLength <= 0) throw new IllegalArgumentException( "maximum length must be greater than zero was " + requestedMaxLength);

        int targetLength=requestedTargetLength;
        int maxLength=requestedMaxLength;

        if( targetLength > maxLength && maxLength > 0) targetLength=maxLength;
        if( maxLength <= targetLength) maxLength=targetLength;

        String editedText = text.trim();

        String stdWords[][]={
            {"and", "&"},
            {"~=", "" + (char)0x2248},
            {"...", "" + (char)0x2026, "TRAILING"},
            {"..", "" + (char)0x20E8, "TRAILING"},
            {"therefore", "" + (char)0x2234},
            {"Therefore", "" + (char)0x2234},
            {"repeat", "" + (char)0x27f2},
            {"happy", "" + (char)0x263a},
            {"write", "" + (char)0x270d},
            {"Write", "" + (char)0x270d},
            {"writing", "" + (char)0x270d},
            {"Writing", "" + (char)0x270d},
            {"circle", "" + (char)0x25ef},
            {"Circle", "" + (char)0x25ef},

            {"square", "" + (char)0x2b1c},
            {"Square", "" + (char)0x2b1c},

            {"postal", "" + (char)0x2b1c},
            {"spot", "" + (char)0x2981},
            {"triangle", "" + (char)0x25b3, "MIXED"},

            {"positive", "+ve"},
            {"Positive", "+ve"},
            {"negative", "-ve"},
            {"Negative", "-ve"},
            {"number", "nmbr"},

            {"here is", "here's"},
            {"there is", "there's"},
            {"what is", "what's"},
            {"that is", "that's"},

            {"it is", "it's"},
            {"is not", "isn't"},
            {"can not", "can't"},
            {"was not", "wasn't"},

            {"As far as I know", "AFAIK"},
            {"as far as I know", "AFAIK"},

            {"Bye for now", "BFN"},
            {"bye for now", "BFN"},

            {"background", "BGD"},
            {"Background", "BGD"},

            {"Blockhead", "BH"},
            {"blockhead", "BH"},

            {"Best regards", "BR"},
            {"best regards", "BR"},

            {"By the way", "BTW"},
            {"by the way", "BTW"},

            {"check", "CHK"},
            {"Check", "CHK"},

            {"See you later", "CUL8R"},
            {"see you later", "CUL8R"},

            {"See you later", "CUL8R"},
            {"see you later", "CUL8R"},

            {"Laugh out loud", "LOL"},
            {"laugh out loud", "LOL"},

            {"Thank you", "TY"},
            {"Thank you", "TY"},

            {"not safe for work", "NSFW", "MIXED"},
//            {"Not safe for work", "NSFW"},

            {"Safe for work", "SFW", "MIXED"},
            //{"safe for work", "SFW"},


            {"Joint venture", "JV"},
            {"joint venture", "JV"},

            {"Are you fucking kidding me with this shit", "AYFKMWTS", "MIXED"},
            //{"are you fucking kidding me with this shit", "AYFKMWTS"},

            {"do not", "don't"},

            {"will not", "won't"},
            {"has not", "hasn't"},
            {"have not", "haven't"},
            {"does not", "doesn't"},
            {"are not", "aren't"},
            {"should not", "shouldn't"},
            {"should have", "should've"},
            {"could have", "could've"},
            {"could not", "couldn't"},
            {"would not", "wouldn't"},
            {"did not", "didn't"},
            {"government", "govn't"},

            {"we are", "we're"},
            {"we will", "we'll"},
            {"we have", "we've"},

            {"I am", "I'm"},
            {"I will", "I'll"},
            {"I have", "I've"},

            {"they are", "they're"},
            {"they will", "they'll"},
            {"they have", "they've"},

            {"you are", "you're"},
            {"you will", "you'll"},
            {"you have", "you've"},

            {"Queensland", "QLD"},
            {"first", "1st"},
            {"second", "2nd"},
            {"third", "3rd"},
            {"forth", "4th"},
            {"fifth", "5th"},
            {"sixth", "6th"},
            {"seventh", "7th"},
            {"eighth", "8th"},
            {"ninth", "9th"},
            {"tenth", "10th"},
            {"great", "gr8"},
            {"straight","str8"},

            {"okay", "OK"},
            {"average","AVG"},
            {"year", "yr"},
            {"years", "yrs"},
            {"plus", "+"},
            {"minus", "-"},
            {"because", "b/c"},
            {"Before", "B4"},
            {"before", "b4"},
            {"Thanks", "Thx"},
            {"thanks", "thx"},
            {"today", "2day"},
            {"Why are you", "YRU"},
            {"why are you", "yru"},
            {"are you", "ru"},
            {"you to", "u2"},
            {"half time", "\u00bd\u231a", "MIXED"}, // 1/2 watch
            {"halftime", "\u00bd\u231a", "MIXED"}, // 1/2 watch
            {"should", "shld"},
            {"Are you", "RU"},
            {"You are", "UR"},
            {"forward", "fwd"},
            {"about", "abt"},
            {"I see", "IC"},
            {"foresee", "4C"},
            {"to see", "2C"},
            {"see", "C"},
            {"it is", "itz"},
            {"later", "l8er"},
            {"Later", "L8er"},
            {"late", "l8"},
            {"Late", "L8"},
            {"please", "plz"},
            {"honey pot", "\u1f36f", "MIXED"},

            {"Saturday", "Sat"},
            {"Sunday", "Sun"},
            {"Monday", "Mon"},
            {"Tuesday", "Tues"},
            {"Wednesday", "Wed"},
            {"Thursday", "Thu"},
            {"Friday", "Fri"},
            {"Please", "Plz"},
            {"people", "ppl"},
            {"People", "PPL"},
            {"to be", "2B"},
            {"be", "b"},
            {"to", "2", "NOT_NEAR_NUMBER"},
            {"too", "2", "NOT_NEAR_NUMBER"},
            {"For", "4", "NOT_NEAR_NUMBER"},
            {"for", "4", "NOT_NEAR_NUMBER"},
            {"at", "@"},
            {"are", "r"},
            {"you", "u"},
            {"You", "U"},
            {"why", "y"},
            {"Why", "Y"},
            {"with", "w/"},
            {"without", "w/o"},
            {"quarter","\u00bc", "MIXED"},
            {"1/4","" + (char)0x00BC},
            {"1/2","" + (char)0x00BD},
            {"3/4","" + (char)0x00BE},
            {"1/3","" + (char)0x2153},
            {"2/3","" + (char)0x2154},
            {"1/5","" + (char)0x2155},
            {"2/5","" + (char)0x2156},
            {"3/5","" + (char)0x2157},
            {"4/5","" + (char)0x2158},
            {"1/6","" + (char)0x2159},
            {"5/6","" + (char)0x215A},
            {"1/8","" + (char)0x215B},
            {"3/8","" + (char)0x215C},
            {"5/8","" + (char)0x215D},
            {"7/8","" + (char)0x215E},
            {"II","" + (char)0x2161},
            {"III","" + (char)0x2162},
            {"IV","" + (char)0x2163},
            {"VI","" + (char)0x2165},
            {"VII","" + (char)0x2166},
            {"VIII","" + (char)0x2167},
            {"IX","" + (char)0x2168},
            {"XI","" + (char)0x216A},
            {"XII","" + (char)0x216B},

            {"!=","" + (char)0x2260},

            {"(1)","" + (char)0x2474},
            {"(2)","" + (char)0x2475},
            {"(3)","" + (char)0x2476},
            {"(4)","" + (char)0x2477},
            {"(5)","" + (char)0x2478},
            {"(6)","" + (char)0x2479},
            {"(7)","" + (char)0x247A},
            {"(8)","" + (char)0x247B},
            {"(9)","" + (char)0x247C},
            {"(10)","" + (char)0x247D},
            {"(11)","" + (char)0x247E},
            {"(12)","" + (char)0x247F},
            {"(13)","" + (char)0x2480},
            {"(14)","" + (char)0x2481},
            {"(15)","" + (char)0x2482},
            {"(16)","" + (char)0x2483},
            {"(17)","" + (char)0x2484},
            {"(18)","" + (char)0x2485},
            {"(19)","" + (char)0x2486},
            {"(20)","" + (char)0x2487},

            {"celsius","" + (char)0x2103},
            {"trade mark","" + (char)0x2122},
            {"tel","" + (char)0x2121},
         //   {"in","" + (char)0x33cc},
            {"sunny", (char)0x2600 +"y"},
            {"sun", "" + (char)0x2600},
            {"up", "" + (char)0x2191},
            {"down", "" + (char)0x2193},
            {"One", "1"},
            {"one", "1"},
            {"won", "1"},
            {"Two", "2", "MIXED"},
            {"two", "2", "MIXED"},
            {"Three", "3", "MIXED"},
            {"three", "3", "MIXED"},
            {"four", "4", "MIXED"},
            {"for", "4", "MIXED"},
            {"five", "5", "MIXED"},
            {"Five", "5", "MIXED"},
            {"six", "6", "MIXED"},
            {"seven", "7", "MIXED"},
            {"eight", "8", "MIXED"},
            {"nine", "9", "MIXED"},
            {"ten", "10", "MIXED"},
            {"eleven", "11", "MIXED"},
            {"twelve", "12", "MIXED"},
            {"sixteenth", "16th"},

            //{"http://", "www.", "LEADING"},
            {"the", "", "FIRST"},
            {"The", "", "FIRST"},

            {"Academy", "A㏅my"},

            {"Professor", "Prof."},
            {"Christopher","Chris"},
            {"/via", "via"},
            {"months","mths"},
            {"supposed","supsd"},
            {"ticket", "tckt"},
            {"Global Warming", "GW"},
            {"global warming", "GW"},
            {"would", "wld"},
            {"anyone", "any1"},
            {"minutes", "mins"},            
            {"Genesis", "Gen"},
            {"degree celsius", "" + (char)0x2103},
            {"recycling", "" + (char)0x267B},
            {"plane", "" + (char)0x2708},
            {"cloud", ""+ (char)0x2601},
            {"clouds", ""+ (char)0x2601+ (char)0x2601},
            {"umbrella", "" + (char)0x2602},
            {"snowman", ""+ (char)0x2603},
            {"comet", ""+ (char)0x2604},
            {"black star", ""+ (char)0x2605},
            {"white star", ""+ (char)0x2606},
            {"thunderstorm", ""+ (char)0x2608},
            {"thunderstorm", ""+ (char)0x2608},
            {"telephone", ""+ (char)0x260f},
            {"raining", "" + (char)0x2614},
            {"rain", "" + (char)0x2614},
            {"shamrock", "" + (char)0x2618},
            {"skull & crossbones", "" + (char)0x2620},
            {"poison", "" + (char)0x2620},
            {"radioactive", "" + (char)0x2622},
            {"star and crescent", "" + (char)0x262a},
            {"Soviet Union", "" + (char)0x262d},
            {"soviet", "" + (char)0x262d},
            {"peace", "" + (char)0x262e},
            {"sun", "" + (char)0x263c},
            {"first quarter moon", "" + (char)0x263d},
            {"last quarter moon", "" + (char)0x263e},
            {"anchor", "" + (char)0x2693},
            {"white flag", "" + (char)0x2690},
            {"black flag", "" + (char)0x2690},
            {"crossed swords", "" + (char)0x2694},
            {"medical", "" + (char)0x2695},
            {"legal", "" + (char)0x2696},
            {"lighting", ""+ (char)0x26a1},
            {"scissors", "" + (char)0x2702},
            {"jews", "" + (char)0x2721},
            {"Jews", "" + (char)0x2721},
            {"star of david", "" + (char)0x2721},
            {"flower", "" + (char)0x2740},
            {"flowers", "" + (char)0x2740+ (char)0x2741},
            {"arrow", "" + (char)0x27b3},


            {"natural", "" + (char)0x266e},
            {"keyboard", "" + (char)0x2328},
            {"wheelchair", "" + (char)0x26a2},
            {"lesbianism", "" + (char)0x267f},
            {"atom", "" + (char)0x269b},
            {"homosexuality", "" + (char)0x26a3},
            {"homosexual", "" + (char)0x26a3},


            //OK but not great
            {"house", "" + (char)0x2302},
            {"Love", "" + (char)0x2661},
            {"love", "" + (char)0x2661},
           // {"right", "" + (char)0x261e},
            {"hate", "h8"},
            {"your", "yr"},
            {"into", "in2"},
            {"tweets","twts"},
            {"tweet","twt"},
            
            //http://www.w3schools.com/charsets/ref_utf_symbols.asp
            {"BLACK SUN WITH RAYS","\u2600","MIXED"},
            {"CLOUD","\u2601","MIXED"},
            {"UMBRELLA","\u2602","MIXED"},
            {"SNOWMAN","\u2603","MIXED"},
            {"COMET","\u2604","MIXED"},
            {"BLACK STAR","\u2605","MIXED"},
            {"WHITE STAR","\u2606","MIXED"},
            {"LIGHTNING","\u2607","MIXED"},
            {"THUNDERSTORM","\u2608","MIXED"},
            {"SUN","\u2609","MIXED"},
            {"ASCENDING NODE","\u260A","MIXED"},
            {"DESCENDING NODE","\u260B","MIXED"},
            {"CONJUNCTION","\u260C","MIXED"},
            {"OPPOSITION","\u260D","MIXED"},
            {"BLACK TELEPHONE","\u260E","MIXED"},
            {"WHITE TELEPHONE","\u260F","MIXED"},
            {"BALLOT BOX","\u2610","MIXED"},
            {"BALLOT BOX WITH CHECK","\u2611","MIXED"},
            {"BALLOT BOX WITH X","\u2612","MIXED"},
            {"SALTIRE","\u2613","MIXED"},
            {"UMBRELLA WITH RAIN DROPS","\u2614","MIXED"},
            {"HOT BEVERAGE","\u2615","MIXED"},
            {"WHITE SHOGI PIECE","\u2616","MIXED"},
            {"BLACK SHOGI PIECE","\u2617","MIXED"},
            {"SHAMROCK","\u2618","MIXED"},
            {"REVERSED ROTATED FLORAL HEART BULLET","\u2619","MIXED"},
            {"BLACK LEFT POINTING INDEX","\u261A","MIXED"},
            {"BLACK RIGHT POINTING INDEX","\u261B","MIXED"},
            {"WHITE LEFT POINTING INDEX","\u261C","MIXED"},
            {"WHITE UP POINTING INDEX","\u261D","MIXED"},
            {"WHITE RIGHT POINTING INDEX","\u261E","MIXED"},
            {"WHITE DOWN POINTING INDEX","\u261F","MIXED"},
            {"SKULL AND CROSSBONES","\u2620","MIXED"},
            {"CAUTION SIGN","\u2621","MIXED"},
            {"RADIOACTIVE SIGN","\u2622","MIXED"},
            {"BIOHAZARD SIGN","\u2623","MIXED"},
            {"CADUCEUS","\u2624","MIXED"},
            {"ANKH","\u2625","MIXED"},
            {"ORTHODOX CROSS","\u2626","MIXED"},
            {"CHI RHO","\u2627","MIXED"},
            {"CROSS OF LORRAINE","\u2628","MIXED"},
            {"CROSS OF JERUSALEM","\u2629","MIXED"},
            {"STAR AND CRESCENT","\u262A","MIXED"},
            {"FARSI SYMBOL","\u262B","MIXED"},
            {"ADI SHAKTI","\u262C","MIXED"},
            {"HAMMER AND SICKLE","\u262D","MIXED"},
            {"PEACE SYMBOL","\u262E","MIXED"},
            {"YIN YANG","\u262F","MIXED"},
            {"TRIGRAM FOR HEAVEN","\u2630","MIXED"},
            {"TRIGRAM FOR LAKE","\u2631","MIXED"},
            {"TRIGRAM FOR FIRE","\u2632","MIXED"},
            {"TRIGRAM FOR THUNDER","\u2633","MIXED"},
            {"TRIGRAM FOR WIND","\u2634","MIXED"},
            {"TRIGRAM FOR WATER","\u2635","MIXED"},
            {"TRIGRAM FOR MOUNTAIN","\u2636","MIXED"},
            {"TRIGRAM FOR EARTH","\u2637","MIXED"},
            {"WHEEL OF DHARMA","\u2638","MIXED"},
            {"WHITE FROWNING FACE","\u2639","MIXED"},
            {"WHITE SMILING FACE","\u263A","MIXED"},
            {"BLACK SMILING FACE","\u263B","MIXED"},
            {"WHITE SUN WITH RAYS","\u263C","MIXED"},
            {"FIRST QUARTER MOON","\u263D","MIXED"},
            {"LAST QUARTER MOON","\u263E","MIXED"},
            {"MERCURY","\u263F","MIXED"},
            {"FEMALE SIGN","\u2640","MIXED"},
            {"EARTH","\u2641","MIXED"},
            {"MALE SIGN","\u2642","MIXED"},
            {"JUPITER","\u2643","MIXED"},
            {"SATURN","\u2644","MIXED"},
            {"URANUS","\u2645","MIXED"},
            {"NEPTUNE","\u2646","MIXED"},
            {"PLUTO","\u2647","MIXED"},
            {"ARIES","\u2648","MIXED"},
            {"TAURUS","\u2649","MIXED"},
            {"GEMINI","\u264A","MIXED"},
            {"CANCER","\u264B","MIXED"},
            {"LEO","\u264C","MIXED"},
            {"VIRGO","\u264D","MIXED"},
            {"LIBRA","\u264E","MIXED"},
            {"SCORPIUS","\u264F","MIXED"},
            {"SAGITTARIUS","\u2650","MIXED"},
            {"CAPRICORN","\u2651","MIXED"},
            {"AQUARIUS","\u2652","MIXED"},
            {"PISCES","\u2653","MIXED"},
            {"WHITE CHESS KING","\u2654","MIXED"},
            {"WHITE CHESS QUEEN","\u2655","MIXED"},
            {"WHITE CHESS ROOK","\u2656","MIXED"},
            {"WHITE CHESS BISHOP","\u2657","MIXED"},
            {"WHITE CHESS KNIGHT","\u2658","MIXED"},
            {"WHITE CHESS PAWN","\u2659","MIXED"},
            {"BLACK CHESS KING","\u265A","MIXED"},
            {"BLACK CHESS QUEEN","\u265B","MIXED"},
            {"BLACK CHESS ROOK","\u265C","MIXED"},
            {"BLACK CHESS BISHOP","\u265D","MIXED"},
            {"BLACK CHESS KNIGHT","\u265E","MIXED"},
            {"BLACK CHESS PAWN","\u265F","MIXED"},
            {"BLACK SPADE SUIT","\u2660","MIXED"},
            {"WHITE HEART SUIT","\u2661","MIXED"},
            {"WHITE DIAMOND SUIT","\u2662","MIXED"},
            {"BLACK CLUB SUIT","\u2663","MIXED"},
            {"WHITE SPADE SUIT","\u2664","MIXED"},
            {"BLACK HEART SUIT","\u2665","MIXED"},
            {"BLACK DIAMOND SUIT","\u2666","MIXED"},
            {"WHITE CLUB SUIT","\u2667","MIXED"},
            {"HOT SPRINGS","\u2668","MIXED"},
            {"QUARTER NOTE","\u2669","MIXED"},
            {"EIGHTH NOTE","\u266A","MIXED"},
            {"BEAMED EIGHTH NOTES","\u266B","MIXED"},
            {"BEAMED SIXTEENTH NOTES","\u266C","MIXED"},
            {"MUSIC FLAT SIGN","\u266D","MIXED"},
            {"MUSIC NATURAL SIGN","\u266E","MIXED"},
            {"MUSIC SHARP SIGN","\u266F","MIXED"},
            {"WEST SYRIAC CROSS","\u2670","MIXED"},
            {"EAST SYRIAC CROSS","\u2671","MIXED"},
            {"UNIVERSAL RECYCLING SYMBOL","\u2672","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-1 PLASTICS","\u2673","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-2 PLASTICS","\u2674","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-3 PLASTICS","\u2675","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-4 PLASTICS","\u2676","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-5 PLASTICS","\u2677","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-6 PLASTICS","\u2678","MIXED"},
            {"RECYCLING SYMBOL FOR TYPE-7 PLASTICS","\u2679","MIXED"},
            {"RECYCLING SYMBOL FOR GENERIC MATERIALS","\u267A","MIXED"},
            {"BLACK UNIVERSAL RECYCLING SYMBOL","\u267B","MIXED"},
            {"RECYCLED PAPER SYMBOL","\u267C","MIXED"},
            {"PARTIALLY-RECYCLED PAPER SYMBOL","\u267D","MIXED"},
            {"PERMANENT PAPER SIGN","\u267E","MIXED"},
            {"WHEELCHAIR SYMBOL","\u267F","MIXED"},
            {"DIE FACE-1","\u2680","MIXED"},
            {"DIE FACE-2","\u2681","MIXED"},
            {"DIE FACE-3","\u2682","MIXED"},
            {"DIE FACE-4","\u2683","MIXED"},
            {"DIE FACE-5","\u2684","MIXED"},
            {"DIE FACE-6","\u2685","MIXED"},
            {"WHITE CIRCLE WITH DOT RIGHT","\u2686","MIXED"},
            {"WHITE CIRCLE WITH TWO DOTS","\u2687","MIXED"},
            {"BLACK CIRCLE WITH WHITE DOT RIGHT","\u2688","MIXED"},
            {"BLACK CIRCLE WITH TWO WHITE DOTS","\u2689","MIXED"},
            {"MONOGRAM FOR YANG","\u268A","MIXED"},
            {"MONOGRAM FOR YIN","\u268B","MIXED"},
            {"DIGRAM FOR GREATER YANG","\u268C","MIXED"},
            {"DIGRAM FOR LESSER YIN","\u268D","MIXED"},
            {"DIGRAM FOR LESSER YANG","\u268E","MIXED"},
            {"DIGRAM FOR GREATER YIN","\u268F","MIXED"},
            {"WHITE FLAG","\u2690","MIXED"},
            {"BLACK FLAG","\u2691","MIXED"},
            {"HAMMER AND PICK","\u2692","MIXED"},
            {"ANCHOR","\u2693","MIXED"},
            {"CROSSED SWORDS","\u2694","MIXED"},
            {"STAFF OF AESCULAPIUS","\u2695","MIXED"},
            {"SCALES","\u2696","MIXED"},
            {"ALEMBIC","\u2697","MIXED"},
            {"FLOWER","\u2698","MIXED"},
            {"GEAR","\u2699","MIXED"},
            {"STAFF OF HERMES","\u269A","MIXED"},
            {"ATOM SYMBOL","\u269B","MIXED"},
            {"FLEUR-DE-LIS","\u269C","MIXED"},
            {"OUTLINED WHITE STAR","\u269D","MIXED"},
            {"THREE LINES CONVERGING RIGHT","\u269E","MIXED"},
            {"THREE LINES CONVERGING LEFT","\u269F","MIXED"},
            {"WARNING SIGN","\u26A0","MIXED"},
            {"HIGH VOLTAGE SIGN","\u26A1","MIXED"},
            {"DOUBLED FEMALE SIGN","\u26A2","MIXED"},
            {"DOUBLED MALE SIGN","\u26A3","MIXED"},
            {"INTERLOCKED FEMALE AND MALE SIGN","\u26A4","MIXED"},
            {"MALE AND FEMALE SIGN","\u26A5","MIXED"},
            {"MALE WITH STROKE SIGN","\u26A6","MIXED"},
            {"MALE WITH STROKE AND MALE AND FEMALE SIGN","\u26A7","MIXED"},
            {"VERTICAL MALE WITH STROKE SIGN","\u26A8","MIXED"},
            {"HORIZONTAL MALE WITH STROKE SIGN","\u26A9","MIXED"},
            {"MEDIUM WHITE CIRCLE","\u26AA","MIXED"},
            {"MEDIUM BLACK CIRCLE","\u26AB","MIXED"},
            {"MEDIUM SMALL WHITE CIRCLE","\u26AC","MIXED"},
            {"MARRIAGE SYMBOL","\u26AD","MIXED"},
            {"DIVORCE SYMBOL","\u26AE","MIXED"},
            {"UNMARRIED PARTNERSHIP SYMBOL","\u26AF","MIXED"},
            {"COFFIN","\u26B0","MIXED"},
            {"FUNERAL URN","\u26B1","MIXED"},
            {"NEUTER","\u26B2","MIXED"},
            {"CERES","\u26B3","MIXED"},
            {"PALLAS","\u26B4","MIXED"},
            {"JUNO","\u26B5","MIXED"},
            {"VESTA","\u26B6","MIXED"},
            {"CHIRON","\u26B7","MIXED"},
            {"BLACK MOON LILITH","\u26B8","MIXED"},
            {"SEXTILE","\u26B9","MIXED"},
            {"SEMISEXTILE","\u26BA","MIXED"},
            {"QUINCUNX","\u26BB","MIXED"},
            {"SESQUIQUADRATE","\u26BC","MIXED"},
            {"WHITE DRAUGHTS MAN","\u26C0","MIXED"},
            {"WHITE DRAUGHTS KING","\u26C1","MIXED"},
            {"BLACK DRAUGHTS MAN","\u26C2","MIXED"},
            {"BLACK DRAUGHTS KING","\u26C3","MIXED"},
            {"ASTRONOMICAL SYMBOL FOR URANUS","\u2600","MIXED"},
            {"Live long and prosper","\u1f596", "MIXED"},
            {"DOWN","\u261f", "MIXED"}, //white down pointing
            {"HALF", "\u00bd", "MIXED"}, // half 1/2
            {"smiling", "\u1f601", "MIXED"}, // Grinning face
            {"baby", "\u1f476", "MIXED"}, // Baby
        };

        int length=stdWords.length;
        if( preHint != null ) length += preHint.length;
        if( postHint != null ) length += postHint.length;

        String words[][];
        words = new String[length][];

        int pos = 0;
        if( preHint != null )
        {
            System.arraycopy(preHint, 0, words, pos, preHint.length);
            pos = preHint.length;
        }
        System.arraycopy(stdWords, 0, words, pos, stdWords.length);
        pos += stdWords.length;
        if( postHint != null )
        {
            System.arraycopy(postHint, 0, words, pos, postHint.length);
        }

        boolean changed=false;
        if( twitterLength(editedText) > targetLength)
        {
            changed=true;
            editedText = " " + editedText + " ";
        }

        String terminators[]={
            "\n",
            " ",
            ",",
            ".",
            "\"",
            "'",
            ";",
            "(",
            ")",
            "!",
            "?",
            "" + (char)0x2026,
            "" + (char)0x20E8,
        };
        for (String[] word : words)
        {
            if( changed)
            {
                String edits[][]={
                    {"\t",      " "},
                    {"\r\n",    "\n"},
                    {"\n\r",    "\n"},
                    {"\r",      "\n"},
                    {"  ",      " "},
                    {" \n",     "\n"},
                    {" \n",     "\n"},
                };
                for (String[] edit : edits)
                {
                    if( twitterLength(editedText)<= targetLength + 2) break;
                    String name = edit[0];
                    String value = edit[1];
                    while( editedText.contains(name))
                    {
                        editedText = editedText.replace( name, value);
                    }
                }
            }
            if( twitterLength(editedText)<= targetLength + 2) break;

            for (String terminator : terminators)
            {
                if( twitterLength(editedText)<= targetLength+ 2) break;
                for (int k = 0; k < 3; k++) {
                    if( twitterLength(editedText)<= targetLength+ 2) break;
                    String prefix=" ";
                    if (k == 0 || k == 1) {
                        prefix = terminator;
                    }
                    String posfix=" ";
                    if (k == 1 || k == 2) {
                        posfix = terminator;
                    }
                    String mode ="WHOLE";
                    if (word.length == 3)
                    {
                        mode = word[2];
                    }
                    String name;
                    String value;
                    switch (mode)
                    {
                        case "LEADING":
                            name = prefix + word[0];
                            value = prefix + word[1];
                            break;
                        case "TRAILING":
                            name = word[0] + posfix;
                            value = word[1] + posfix;
                            break;
                        default:
                            name = prefix + word[0] + posfix;
                            value = prefix + word[1] + posfix;
                            break;
                    }
                    while( editedText.contains(name))
                    {
                        if( twitterLength(editedText)<= targetLength+ 2) break;
                        if( mode.equals( "FIRST"))
                        {
                            int start=0;
                            if( editedText.startsWith( " @") || editedText.startsWith(" http") || editedText.startsWith(" www."))
                            {
                                start = editedText.indexOf(prefix, 2);
                            }

                            if( start != -1)
                            {
                                if( editedText.startsWith(name, start))
                                {
                                    editedText = editedText.substring(0, start) + value + editedText.substring( start + name.length());
                                    changed=true;
                                }
                            }

                            break;
                        }
                        else
                        {
                            editedText = editedText.replace(name, value);
                            changed=true;
                        }
                    }
                }
            }
        }

        editedText = editedText.trim();
//cc, ms, ns, ps, in, ls, fi, fl, ffl, ffi, iv, ix, vi, oy, ii, xi, nj, \". \" (period space), and \", \" (comma space)
        String compressCharacters[][]={
            {"cc", "" + (char)0x33c4},
            {"ms", "" + (char)0x33b3},
            {"ns", "" + (char)0x33b1},
            {"ps", "" + (char)0x33b0},
           // {"in", "" + (char)0x33cc},
            {"ls", "" + (char)0x02aa},
            {"ffl", "" + (char)0xfb04},
            {"ffi", "" + (char)0xfb03},
            {"fi", "" + (char)0xfb01},
            {"fl", "" + (char)0xfb02},
            {"iv", "" + (char)0x2173},
            {"ix", "" + (char)0x2178},
            {"vi", "" + (char)0x2175},
            {"oy", "" + (char)0x0479},
            {"ii", "" + (char)0x2171},

            {"xi", "" + (char)0x217a},

            {"nj", "" + (char)0x01cc},
            {". ", "" + (char)0xff0e},
            {", ", "" + (char)0xff0c},
        };

        boolean started=false;
        //SERVER-START
        for( int cp=0; cp < editedText.length();cp++)
        {
            if( twitterLength(editedText) <= targetLength) break;

            char c = editedText.charAt(cp);
            if(started)
            {
                if(
                    c == ' ' ||
                    c == '\t' ||
                    c == '\n' ||
                    c == '\r'
                )
                {
                    started=false;
                }
            }

            if(
                editedText.startsWith("http", cp) ||
                editedText.startsWith("www.", cp) ||
                editedText.startsWith("@", cp) ||
                editedText.startsWith("#", cp)
            )
            {
                started=true;
            }

            if( started == false)
            {
                for (String[] replaceChars : compressCharacters)
                {
                    if( twitterLength(editedText) <= targetLength) break;

                    if( editedText.startsWith(replaceChars[0], cp))
                    {
                        String tmp = editedText.substring(0, cp) + replaceChars[1] + editedText.substring(cp + replaceChars[0].length());

                        editedText=tmp;
                        break;
                    }
                }
            }
        }
        //SERVER-END

        String compressSeperators[]={"\n",".","~",",", "?", "&", "!", ")"};
        for( int loop=0;loop<2;loop++)
        {
            if( twitterLength(editedText) <= targetLength) break;
            for (String sep : compressSeperators)
            {
                if( twitterLength(editedText) <= targetLength) break;

                while( true)
                {
                    if( twitterLength(editedText) <= targetLength) break;

                    String tmp;
                    if( loop == 0)
                    {
                        tmp=sep + " ";
                    }
                    else
                    {
                        tmp=" " + sep;
                    }

                    if( editedText.contains(tmp) == false) break;
                    editedText=editedText.replace( tmp, sep);
                }
            }
        }

      //  if( editedText.length() > maxLength)
       // {
       //     editedText=replace(editedText, "http://www.", "www.");
      //      editedText=replace(editedText, "http://", "www.");
      //  }

        if( twitterLength(editedText) > maxLength)
        {
            int cutby=twitterLength(editedText)-maxLength;
            int httpPos=editedText.indexOf(" http");
            if( httpPos != -1 )
            {
                int spacePos = editedText.indexOf(' ', httpPos + 1);
                if( spacePos != -1)
                {
                    if( editedText.length() - spacePos < cutby )
                    {
                        editedText=editedText.substring(0, spacePos);
                    }
                    else
                    {
                        editedText=editedText.substring(0, editedText.length() - cutby);
                    }
                }
                else if( httpPos > cutby)
                {
                    String first=editedText.substring(0, httpPos - cutby);
                    String last=editedText.substring(httpPos);

                    editedText=first + last;
                }
            }
            else
            {
                editedText=editedText.substring(0, editedText.length() - cutby);
            }


        }
        return editedText;
    }
    
    @CheckReturnValue
    public static double parseDouble( final String text)
    {
        int len = text.length();

        boolean found = false;
        for( int i = 0; i < len && found == false; i++)
        {
            switch( text.charAt(i))
            {
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    found = true;
                    break;
                case ' ':
                    continue;
                case 'N':
                case 'I':
                    if(
                        "NaN".equalsIgnoreCase(text)||
                        "Infinity".equalsIgnoreCase(text)
                    )
                    {
                        found = true;
                    }
                    break; // could be Nan or Infinity
                default:
                    throw NFE;
            }
        }

        if( found == false)
        {
            throw NFE;
        }
        return Double.parseDouble(text);
    }

    /**
     *
     */
    private StringUtilities()
    {
    }
    
    /**
     * Compress the text to a gzip byte array
     *
     * @param text the text to compress
     * @throws Exception a serious problem
     * @return the gzip bytes
     */
    @CheckReturnValue
    public static byte[] compressToBytes(final @Nonnull String text) throws Exception
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        (GZIPOutputStream gzip = new GZIPOutputStream(out))
        {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8))) {
                writer.append(text);
            }
        }

        byte cdata[] = out.toByteArray();

        return cdata;
    }

    /**
     *
     * @param text
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static String compress(final String text) throws Exception
    {
        byte cdata[] = compressToBytes(text);

        String str = encode(cdata, 0, cdata.length, false);

        return str;
    }

    /**
     * decompress a string to a normal native java string
     * @param compressedData
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static String decompress(final String compressedData, final String type) throws Exception
    {
        if (isBlank(type))
        {
            return compressedData;
        }

        if (type.equalsIgnoreCase(COMPRESSION_GZIP))
        {
            byte array[] = decodeToBytes(compressedData);

            ByteArrayInputStream in = new ByteArrayInputStream(array);

            byte temp[] = new byte[2048];
            StringBuilder buffer = new StringBuilder();

            try
            (GZIPInputStream stream = new GZIPInputStream(in)
            )
            {
                while (true)
                {
                    int len = stream.read(temp);

                    if (len <= 0)
                    {
                        break;
                    }
                    String str = new String(temp, 0, len, "ISO-8859-1");
                    buffer.append(str);
                }
            }

            String utf8 = buffer.toString();

            String data = decodeUTF8(utf8);

            return data;
        }
        else if (type.equalsIgnoreCase(COMPRESSION_DEFLATE))
        {
            byte array[] = decodeToBytes(compressedData);

            ByteArrayInputStream in = new ByteArrayInputStream(array);


            byte temp[] = new byte[2048];
            StringBuilder buffer = new StringBuilder();
            try
            (InflaterInputStream stream = new InflaterInputStream(in)) {

                while (true)
                {
                    int len = stream.read(temp);

                    if (len <= 0)
                    {
                        break;
                    }
                    String str = new String(temp, 0, len, "ISO-8859-1");
                    buffer.append(str);
                }
            }

            String utf8 = buffer.toString();

            String data = decodeUTF8(utf8);

            return data;
        }
        else if (type.equalsIgnoreCase(COMPRESSION_NONE))
        {
            return compressedData;
        }
        else
        {
            throw new RuntimeException("unknown compress type: " + type);
        }
    }

    /**
     * decompress a string to a normal native java string
     * @param array
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static String decompress(final byte[] array) throws Exception
    {
        return decompress(array, COMPRESSION_GZIP);
    }

    /**
     * decompress a string to a normal native java string
     * @param array
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static String decompress(final byte[] array, final String type) throws Exception
    {
        if (type.equalsIgnoreCase(COMPRESSION_GZIP) || isBlank(type))
        {
            ByteArrayInputStream in = new ByteArrayInputStream(array);

            byte temp[] = new byte[2048];
            StringBuilder buffer = new StringBuilder();

            try
            (GZIPInputStream stream = new GZIPInputStream(in)
            ) {

                while (true)
                {
                    int len = stream.read(temp);

                    if (len <= 0)
                    {
                        break;
                    }
                    String str = new String(temp, 0, len, "ISO-8859-1");
                    buffer.append(str);
                }
            }

            String utf8 = buffer.toString();

            String data = decodeUTF8(utf8);

            return data;
        }
        else if (type.equalsIgnoreCase(COMPRESSION_DEFLATE))
        {
            ByteArrayInputStream in = new ByteArrayInputStream(array);

            byte temp[] = new byte[2048];
            StringBuilder buffer = new StringBuilder();

            try
            (InflaterInputStream stream = new InflaterInputStream(in)
            ) {

                while (true)
                {
                    int len = stream.read(temp);

                    if (len <= 0)
                    {
                        break;
                    }
                    String str = new String(temp, 0, len, "ISO-8859-1");
                    buffer.append(str);
                }
            }

            String utf8 = buffer.toString();

            String data = decodeUTF8(utf8);

            return data;
        }
        else
        {
            throw new RuntimeException("unknown compress type: " + type);
        }
    }

    /**
     * split attributes.
     *
     *   UPDATE ABC SET name='abc', email='nigel@abc.com.au'
     *
     * @param attributes
     * @return list of attributes.
     */
    @CheckReturnValue
    public static String[][] splitAttributes(final String attributes)
    {
        if (StringUtilities.isBlank(attributes))
        {
            return new String[0][];
        }

        int currentPos = 0;
        ArrayList list = new ArrayList();

        int len = attributes.length();

        while (true)
        {
            if (currentPos >= len)
            {
                break;
            }
            boolean singleQuote = false;
            int bracketCount = 0;
            boolean doubleQuote = false;
            boolean lastEscape = false;

            int startPos = currentPos;

            boolean found = false;
            for (; currentPos < len; currentPos++)
            {
                char c = attributes.charAt(currentPos);

                if (lastEscape)
                {
                    lastEscape = false;
                }
                else if (c == '\'')
                {
                    if (singleQuote)
                    {
                        singleQuote = false;
                    }
                    else if (doubleQuote == false && bracketCount == 0)
                    {
                        singleQuote = true;
                    }
                }
                else if (c == '"')
                {
                    if (doubleQuote)
                    {
                        doubleQuote = false;
                    }
                    else if (singleQuote == false && bracketCount == 0)
                    {
                        doubleQuote = true;
                    }
                }
                else if (c == '{')
                {
                    if (singleQuote == false && doubleQuote == false)
                    {
                        bracketCount++;
                    }
                }
                else if (c == '}')
                {
                    if (singleQuote == false && doubleQuote == false && bracketCount > 0)
                    {
                        bracketCount--;
                    }
                }
                else if (c == '\\')
                {
                    lastEscape = true;
                }
                else if (singleQuote == false && doubleQuote == false && bracketCount == 0 && c == ',')
                {
                    found = true;
                    currentPos++;
                    break;
                }
            }

            String temp = attributes.substring(startPos, currentPos - (found ? 1 : 0)).trim();

            if (StringUtilities.isBlank(temp))
            {
                continue;
            }
            list.add(temp);
        }

        String array[][] = new String[list.size()][];

        for (int i = 0; i < array.length; i++)
        {
            String temp = (String) list.get(i);

            int tempLen = temp.length();
            int tempPos = 0;
            boolean singleQuote = false;
            int bracketCount = 0;
            boolean doubleQuote = false;
            boolean lastEscape = false;

            boolean found = false;
            for (; tempPos < tempLen; tempPos++)
            {
                char c = temp.charAt(tempPos);

                if (lastEscape)
                {
                    lastEscape = false;
                }
                else if (c == '\'')
                {
                    if (singleQuote)
                    {
                        singleQuote = false;
                    }
                    else if (doubleQuote == false && bracketCount == 0)
                    {
                        singleQuote = true;
                    }
                }
                else if (c == '"')
                {
                    if (doubleQuote)
                    {
                        doubleQuote = false;
                    }
                    else if (singleQuote == false && bracketCount == 0)
                    {
                        doubleQuote = true;
                    }
                }
                else if (c == '{')
                {
                    if (singleQuote == false && doubleQuote == false)
                    {
                        bracketCount++;
                    }
                }
                else if (c == '}')
                {
                    if (singleQuote == false && doubleQuote == false && bracketCount > 0)
                    {
                        bracketCount--;
                    }
                }
                else if (c == '\\')
                {
                    lastEscape = true;
                }
                else if (singleQuote == false && doubleQuote == false && bracketCount == 0 && c == '=')
                {
                    found = true;
                    break;
                }
            }

            String value[] = new String[2];
            if (found)
            {
                value[0] = temp.substring(0, tempPos);
                String tempValue = temp.substring(tempPos + 1);
                String trimValue = tempValue.trim();

                int trimSize = trimValue.length();
                if (trimSize > 1)
                {
                    char startQuote = trimValue.charAt(0);
                    char lastQuote = trimValue.charAt(trimSize - 1);

                    if (startQuote == lastQuote && (startQuote == '\'' ||
                            startQuote == '"'))
                    {
                        StringBuilder buffer = new StringBuilder();

                        lastEscape = false;
                        for (int j = 1; j < trimSize - 1; j++)
                        {
                            char c = trimValue.charAt(j);

                            if (lastEscape)
                            {
                                buffer.append(c);
                                lastEscape = false;
                            }
                            else if (c == '\\')
                            {
                                lastEscape = true;
                            }
                            else
                            {
                                buffer.append(c);
                            }
                        }
                        value[1] = buffer.toString();
                    }
                    else
                    {
                        value[1] = trimValue;
                    }
                }
                else
                {
                    value[1] = trimValue;
                }
            }
            else
            {
                value[0] = temp;
                value[1] = "";
            }

            array[i] = value;
        }

        return array;
    }

    /**
     *
     * @param currentSql
     * @return the value
     */
    @CheckReturnValue
    public static String[] splitCommands(final String currentSql)
    {
        int currentPos = 0;
        ArrayList list = new ArrayList();

        int len = currentSql.length();

        while (true)
        {
            if (currentPos >= len)
            {
                break;
            }
            boolean singleQuote = false;
            int bracketCount = 0;
            boolean doubleQuote = false;
            boolean lastEscape = false;
            boolean aComment = false;
            boolean commandStarted=false;
            int startPos = currentPos;

            boolean found = false;
            for (; currentPos < len; currentPos++)
            {
                char c = currentSql.charAt(currentPos);

                if( aComment )
                {
                    if( c == '/' && currentPos > 0 && currentSql.charAt(currentPos-1) == '*')
                    {
                        aComment=false;
                        if( commandStarted == false)
                        {
                            startPos=currentPos+1;
                        }
                    }
                }
                else if (lastEscape)
                {
                    lastEscape = false;
                }
                else if (c == '\'')
                {
                    if (singleQuote)
                    {
                        singleQuote = false;
                    }
                    else if (doubleQuote == false && bracketCount == 0)
                    {
                        singleQuote = true;
                    }
                }
                else if (c == '"')
                {
                    if (doubleQuote)
                    {
                        doubleQuote = false;
                    }
                    else if (singleQuote == false && bracketCount == 0)
                    {
                        doubleQuote = true;
                    }
                }
                else if (c == '{')
                {
                    if (singleQuote == false && doubleQuote == false)
                    {
                        bracketCount++;
                    }
                }
                else if (c == '}')
                {
                    if (singleQuote == false && doubleQuote == false && bracketCount > 0)
                    {
                        bracketCount--;
                    }
                }
                else if (c == '\\')
                {
                    lastEscape = true;
                }
                else if( singleQuote == false && doubleQuote == false && c == '/' && currentPos< len -2 && currentSql.charAt(currentPos+1) == '*')
                {
                    aComment=true;
                }
                else if (singleQuote == false && doubleQuote == false && bracketCount == 0 && c == ';')
                {
                    found = true;
                    currentPos++;
                    break;
                }
                else if( Character.isLetter(c))
                {
                    commandStarted=true;
                }
            }

            String temp = currentSql.substring(startPos, currentPos - (found ? 1 : 0)).trim();
//            commandStarted=false;

            if (StringUtilities.isBlank(temp))
            {
                continue;
            }
            list.add(temp);
        }

        String array[] = new String[list.size()];

        list.toArray(array);

        return array;
    }

    /**
     *
     * @param text
     * @return the value
     */
    @CheckReturnValue @Nonnull 
    public static String rightTrim(final @Nonnull String text)
    {
        int last = text.length();

        while (last > 0 && Character.isWhitespace(text.charAt(last - 1)))
        {
            last--;
        }

        return text.substring(0, last);
    }

    /**
     * Is a value LIKE and a comma separated list of patterns with excludes
     *
     * @return true if match
     * @param path the "file" path to check
     * @param pattern The pattern to match to.
     */
    @CheckReturnValue
    public static boolean isPatternMatch(final @Nonnull String pattern, final @Nonnull String path)
    {
        StringTokenizer st = new StringTokenizer(pattern, ",");

        boolean found = false;
        while (st.hasMoreTokens())
        {
            String temp = st.nextToken().trim();

            if (temp.startsWith("[") && temp.endsWith("]"))
            {
                String cut = temp.substring(1, temp.length() - 1);

                if (StringUtilities.isLike(cut, path))
                {
                    return false;
                }
            }
            else if (found == false)
            {
                found = StringUtilities.isLike(temp, path);
            }
        }

        return found;
    }

    /**
     * Is a value Contain any of pattern
     *
     * @param pattern The pattern to match to.
     * @param value The value to match
     * @return true if match
     */
    @CheckReturnValue
    public static boolean isContainAnyOf(final String pattern, final String value)
    {
        if(pattern == null && value == null)
        {
            return true;
        }
        else if(pattern == null || value == null)
        {
            return false;
        }
        String uPattern = pattern.toUpperCase().trim();
        String uValue = value.toUpperCase().trim();
        if(uPattern.isEmpty() && uValue.isEmpty())
        {
            return true;
        }
        else if(uPattern.isEmpty() || uValue.isEmpty())
        {
            return false;
        }
        StringTokenizer st = new StringTokenizer(uPattern, " ");
        while(st.hasMoreTokens())
        {
            String token = st.nextToken();
            if(uValue.contains(token))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a value matches a pattern. The pattern is whitespace separated with
     * support for wild cards and negation.
     *
     * @param pattern the pattern to check
     * @param value the value
     * @return matches.
     */
    @CheckReturnValue
    public static boolean matches(final @Nonnull String pattern, final @Nonnull String value)
    {
        if( StringUtilities.isBlank(pattern)) return false;

        String uValue = " " + value.replaceAll( "[^\\w]", " ").toUpperCase().trim() + " ";

        while( uValue.contains("  "))
        {
            uValue = uValue.replace("  ", " ");
        }

        String tmpPattern=pattern.toUpperCase();

        while( tmpPattern.contains("  "))
        {
            tmpPattern = tmpPattern.replace("  ", " ");
        }

        while( true)
        {
            int startPos=tmpPattern.indexOf('"');
            if( startPos == -1) break;

            int endPos=tmpPattern.indexOf('"', startPos +1);
            if( endPos == -1) break;

            String tmpStart=tmpPattern.substring(0, startPos);
            String tmpMiddle=tmpPattern.substring(startPos + 1, endPos);

            if( isLike("* " + tmpMiddle +" *", uValue)==false)
            {
                return false;
            }
            String tmpEnd=tmpPattern.substring(endPos + 1);

            tmpPattern=tmpStart+tmpEnd;
        }

        String uPattern = tmpPattern.replaceAll( "[^\\w\\*%\\-]", " ").trim();

        StringTokenizer st = new StringTokenizer(uPattern, " *%");

        while (st.hasMoreElements())
        {
            String term = st.nextToken();

            boolean negTerm=false;
            if( term.startsWith("-"))
            {
                negTerm=true;
                term=term.substring(1);
            }

            term = term.replace("-", " ");
            if( uValue.contains( " " + term ) == negTerm)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Is a value LIKE and a pattern
     *
     * @return true if match
     * @param patterns The patterns to match to.
     * @param value The value to match
     */
    @CheckReturnValue
    public static boolean isLike(final String[] patterns, final String value)
    {
        for( String pattern: patterns)
        {
            if( isLike( pattern, value))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Is a value LIKE and a pattern
     *
     * @return true if match
     * @param pattern The pattern to match to.
     * @param value The value to match
     */
    @CheckReturnValue
    public static boolean isLike(final @Nonnull String pattern, final @Nonnull String value)
    {
        String uPattern = pattern.toUpperCase();//.trim();

        String uValue = value.toUpperCase();//.trim();

        if (uPattern.isEmpty() && uValue.isEmpty() == false)
        {
            return false;
        }

        StringTokenizer st = new StringTokenizer(uPattern, "*%", true);

        int pos = 0;

        while (st.hasMoreElements())
        {
            String name = st.nextToken();

            if (name.equals("*") || name.equals("%"))
            {
                if (st.hasMoreTokens() == false)
                {
                    return true;
                }

                name = st.nextToken();
                int i;
                i = uValue.substring(pos).indexOf(name);

                if (i == -1)
                {
                    return false;
                }

                pos += i;
            }

            if (st.hasMoreTokens())
            {
                if (uValue.substring(pos).startsWith(name) == false)
                {
                    return false;
                }
                pos+=name.length();
            }
            else
            {
                if (uValue.substring(pos).equals(name) == false)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * The function replace() replaces every occurrence of the second parameter "name" with the third parameter
     * "value" in the first parameter text.
     * If no changes were made, the first parameter is returned without changes (no new string object is generated.)
     *
     * @param text The string that will be iterated through to replace parts
     * @param name The string that will be replaced
     * @param value The string that will replace the original text.
     * @return the value
     * 
     * @deprecated  As of JDK 1.5, replaced by {@link String#replace(String, String)}
     */
    @CheckReturnValue @Nullable @Deprecated
    public static String replace(
        @Nullable String text,
        @Nonnull String name,
        @Nonnull String value
    )
    {
        assert name!=null;
        assert value!=null;
        if (text == null)
        {
            return null;
        }

        // look for occurances of the string that needs to be replaced
        if (!text.contains(name))
        {
            /**
             * nothing was found, return the original string without creating a new instance
             */
            return text;
        }

        int nLen = name.length();
        int tLen = text.length();

        StringBuilder buffer;

        /*
         * If we are going to make the resulting string bigger then
         * allow a bigger buffer.
         */
        if (nLen < value.length())
        {
            int sLen = (int) (tLen * 1.5);
            buffer = new StringBuilder(sLen);
        }
        else
        {
            buffer = new StringBuilder(tLen);
        }

        char array[] = new char[tLen];

        text.getChars(0, tLen, array, 0);

        int pos = 0;

        while (pos < tLen)
        {
            int loc = text.indexOf(name, pos);

            if (loc == -1)
            {
                buffer.append(array, pos, tLen - pos);
                break;
            }

            int skip = loc - pos;

            if (skip > 0)
            {
                buffer.append(array, pos, skip);
            }

            buffer.append(value);

            pos = loc + nLen;
        }

        return buffer.toString();
    }

    /**
     * Is the string blank same as trim().equals( "") just faster.
     * @param obj
     * @return the value
     */
    @CheckReturnValue
    public static boolean notBlank(final @Nullable Object obj)
    {
        return isBlank( obj)==false;
    }

    /**
     * Is the string blank same as trim().equals( "") just faster.
     * @param obj
     * @return the value
     */
    @CheckReturnValue
    public static boolean isBlank(final @Nullable Object obj)
    {
        if( obj instanceof CharSequence)
        {
            CharSequence cs=(CharSequence)obj;
            int len=cs.length();
            for (int i = 0; i < len; i++)
            {
                char c = cs.charAt(i);
                if (c > 255 || ASCII_WHITE_SPACE[c] == false)
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return obj == null;
        }
    }

    /**
     *
     * @param sqlString
     * @return the value
     */
    @CheckReturnValue
    public static String decodeSQL(final @Nullable String sqlString)
    {
        if (sqlString == null)
        {
            return null;
        }

        int pos = sqlString.indexOf('#');

        if (pos == -1)
        {
            return sqlString;
        }

        StringBuilder buffer = new StringBuilder(sqlString.length());

        int lastPos = 0;

        while (true)
        {
            pos = sqlString.indexOf('#', lastPos);

            if (pos == -1)
            {
                buffer.append(sqlString.substring(lastPos));

                return buffer.toString();
            }

            if (pos != lastPos)
            {
                buffer.append(sqlString.substring(lastPos, pos));
            }

            pos++;

            int pos2 = sqlString.indexOf(';', pos);

            if (pos2 == -1)
            {
                throw new RuntimeException("missing ';' in '" + sqlString + "'");
            }

            String temp = sqlString.substring(pos, pos2);

            char c = (char) Integer.parseInt(temp, 16);

            buffer.append(c);

            lastPos = pos2 + 1;
        }
    }

    /**
     * http://en.wikipedia.org/wiki/Character_encodings_in_HTML
     * 
     * @param text
     * @return true 
     * @throws AssertionError 
     */
    public static boolean assertIllegalCharactersHTML( final @Nonnull String text)
    {
        try{
            checkIllegalCharactersHTML(text);
        }
        catch( IllegalArgumentException iae)
        {
            throw new AssertionError(iae.getMessage());
        }
        return true;
    }
    
    /**
     * http://en.wikipedia.org/wiki/Character_encodings_in_HTML
     * 
     * @param text
     * @throws IllegalArgumentException 
     */
    public static void checkIllegalCharactersHTML( final @Nonnull String text) throws IllegalArgumentException
    {
        assert text!=null: "text is NULL";
        
        for( char c: text.toCharArray())
        {
            if( c < 32)
            {
                if( c==0)
                {
                    throw new IllegalArgumentException( "Character reference expands to zero.");
                }
                else if( c != 9 && c != 10 && c != 13)
                {
                    throw new IllegalArgumentException( "May not contain illegal control character: 0x" + Integer.toHexString(c));
                }
            }
            else if( c == 127)
            {
                throw new IllegalArgumentException( "illegal DEL character");
            }
            else if( c > 127 && c < 160)
            {
                throw new IllegalArgumentException( "illegal C1 control character: 0x" + Integer.toHexString(c));
            }
            else if( c >= 0xd800 && c <= 0xdfff)
            {
                throw new IllegalArgumentException( "Character reference expands to a surrogate: 0x" + Integer.toHexString(c));
            }
//            else if( c >= 55296 && c <= 57343)
//            {
//                LOGGER.warn( "Uses the Unicode Private Use Area(s), which should not be used in publicly exchanged documents. " + (int)c);
////                throw new IllegalArgumentException( "illegal UTF-16 surrogate halves " + (int)c);
//            }
            else if( c >= 0xFFFE && c <= 0xFFFF)
            {
                throw new IllegalArgumentException( "non-characters, related to xFEFF, the byte order mark. " + (int)c);
            }
            else if( c > 0x10FFFF)
            {
                throw new IllegalArgumentException( "Illegal XML character: 0x" + Integer.toHexString(c));
            }
        }
    }
    
    @CheckReturnValue
    public static boolean validCharactersHTML( final @Nonnull String text)
    {
        try
        {
            checkIllegalCharactersHTML( text);
            return true;
        }
        catch( IllegalArgumentException iae)
        {
            return false;
        }
    }
    
    public static String makeSafeHTML( final @Nonnull String text, final char replacementChar)
    {
        if( validCharactersHTML( text)) return text;
        
        StringBuilder sb=new StringBuilder( text.length());
        for( char c: text.toCharArray())
        {
            if( StringUtilities.validCharactersHTML("" + c)==false)
            {
                sb.append(replacementChar);
            }
            else 
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }
    /**
     * http://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references
     * @param encodedHTML
     * @return the value
     */
    @CheckReturnValue
    public static String decodeHTML( final @Nonnull String encodedHTML)
    {
        assert validCharactersHTML( encodedHTML): encodedHTML;
        
        int len = encodedHTML.length();

        StringBuilder buffer = new StringBuilder(len);
        for (int i = 0; i < len; i++)
        {
            char c = encodedHTML.charAt(i);

            if (c == '&' )
            {
                int pos = encodedHTML.indexOf(';', i);
                
                if( pos == -1 || pos > i + 7)
                {
                    LOGGER.warn( "invalid HTML at " + i + "\n" + encodedHTML);
                    buffer.append(c);
                }
                else
                {
                    String symbol=encodedHTML.substring(i+1, pos);
                    i=pos;
                    
                    if( symbol.startsWith("#"))
                    {
                        int cValue = Integer.parseInt(symbol.substring(1));
                        buffer.append( (char)cValue);
                    }
                    else
                    {

                        switch( symbol)
                        {
                            case "lt": 
                                buffer.append( "<");
                                break;
                            case "gt": 
                                buffer.append( ">");
                                break;
                            default: 
                                Character c2=HTML_CHARACTER.get( symbol);
                                
                                if( c2==null)
                                {
                                    LOGGER.warn( "invalid HTML at " + i + " " + symbol);
                                }
                                else
                                {
                                   buffer.append( c2); 
                                }
                        }
                    }                   
                }
            }
            else 
            {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
    
    /**
     * http://en.wikipedia.org/wiki/Character_encodings_in_HTML
     * http://www.w3.org/TR/html401/types.html#type-name
     * @param value the value
     * @return the value
     */
    @CheckReturnValue
    public static String encodeHTML(final String value)
    {
        if (value == null)
        {
            return null;
        }
        assert validCharactersHTML( value): "invalid HTML '" + encode( value) +"'";
        int len = value.length();

        StringBuilder buffer = new StringBuilder(len);

        for (int i = 0; i < len; i++)
        {
            char c = value.charAt(i);

            if (
                c >= 'A' && c <= 'Z' ||
                c >= 'a' && c <= 'z' ||
                c >= '0' && c <= '9' ||
                c == '-' ||
                c == ',' ||
                c == '=' ||
                c == ' ' ||
                c == ':' ||
                c == '_' ||
                c == '.' ||
                c == '*'
            )
            {
                buffer.append(c);
            }
            else if( c== '&')
            {
                buffer.append("&amp;");
            }
            else if( c== '"')
            {
                buffer.append("&quot;");
            }
            else if( c== '<')
            {
                buffer.append("&lt;");
            }
            else if( c== '>')
            {
                buffer.append("&gt;");
            }
            else
            {
                buffer.append("&#");
                buffer.append((int) c);
                buffer.append(";");                
            }
        }

        return buffer.toString();
    }

    /**
     * get the Adler32 checksum value of this string
     * @param str String
     * @return checksum value as hex ( 8 characters).
     */
    @CheckReturnValue
    public static String checkSumAdler32(final String str)
    {
        String temp = str;
        if(temp == null)
        {
            temp = "";
        }
        return StringUtilities.checkSumAdler32(temp.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * calculates the Adler32 checksum value of this byte array 
     * @param data byte array
     * @return checksum 32-bit integer in hex
     */
    @CheckReturnValue
    public static String checkSumAdler32(final byte[] data)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
        
        final byte readBuffer[] = new byte[data.length < 2048? data.length: 2048];
        try
        {
            while (cis.read(readBuffer) >= 0)
            {
            }
        }
        catch( IOException io)
        {
            throw new RuntimeException( "could not do a checkSum", io);
        }
        finally
        {
            try
            {
                cis.close();
            }
            catch (IOException ex)
            {
            }

            try
            {
                bais.close();
            }
            catch (IOException ex)
            {
            }
        }
        long value = cis.getChecksum().getValue();
        String hex = Long.toHexString(value).toUpperCase();
        return hex;
    }

    /**
     * Some characters are valid in Java Docs comments that aren't in HTML.
     * @param doubleByteStr
     * @return the value
     */
    @CheckReturnValue
    public static String encodeJavaDoc(final String doubleByteStr)
    {
        if (doubleByteStr == null)
        {
            return null;
        }
        int len = doubleByteStr.length();

        StringBuilder buffer = new StringBuilder(len);

        for (int i = 0; i < len; i++)
        {
            char c = doubleByteStr.charAt(i);

            if (
                c >= 'A' && c <= 'Z' ||
                c >= 'a' && c <= 'z' ||
                c >= '0' && c <= '9' ||
                c == '@' ||
                c == '"' ||
                c == '?' ||
                c == ':' ||
                c == ';' ||
                c == '=' ||
                c == '+' ||
                c == '!' ||
                c == '$' ||
                c == '\'' ||
                (c == '/' && (i==0|| doubleByteStr.charAt(i-1) != '*')) ||
                (c == '&' && i+ 1< len && doubleByteStr.charAt(i+1) == ' ') ||
                c == '(' ||
                c == ')' ||
                c == '[' ||
                c == ']' ||
                c == '{' ||
                c == '}' ||
                c == ',' ||
                c == '-' ||
                c == ' ' ||
                c == '_' ||
                c == '.' ||
                c == '*'
            )
            {
                buffer.append(c);
            }
            else if( c == '<')
            {
                buffer.append("&lt;");
            }
            else if( c == '>')
            {
                buffer.append("&gt;");
            }
            else
            {
                buffer.append("&#");
                buffer.append((int) c);
                buffer.append(";");
            }
        }

        return buffer.toString();
    }

    /**
     * Check that this is a value SQL value which includes WHERE clauses and columns
     * @param value the value
     * @return the value
     * @throws IllegalArgumentException
     */
    @CheckReturnValue
    public static String sqlValueCheck(final @Nullable String value) throws IllegalArgumentException
    {
        if(value == null)
        {
            return value;
        }

        if( value.equals("\\"))
        {
            throw new IllegalArgumentException( "Value ends in escape: " + value);
        }
        char quote='\0';
        boolean lastEscape=false;

        for( char c : value.toCharArray())
        {
            if( c == '\\')
            {
                if( lastEscape)
                {
                    lastEscape=false;
                }
                else if( quote == '\0')
                {
                    throw new IllegalArgumentException( "unquoted escape: " + value);
                }
                else
                {
                    lastEscape=true;
                }
            }
            else if( lastEscape )
            {
                lastEscape=false;
            }
            else if( c== '\'' || c == '"')
            {
                if( quote == c)
                {
                    quote='\0';
                }
                else if( quote == '\0')
                {
                    quote=c;
                }
            }
            else if( quote =='\0')
            {
                if( c == ';')
                {
                    throw new IllegalArgumentException( "unquoted semicolon: " + value);
                }
            }
        }

        if( quote != '\0')
        {
            throw new IllegalArgumentException( "unclosed quote: " + value);
        }
        return value;
    }
    /**
     *
     * @param doubleByteStr
     * @return the value
     */
    @CheckReturnValue
    public static String encodeSQL(final @Nullable String doubleByteStr)
    {
        if (doubleByteStr == null)
        {
            return null;
        }
        int len = doubleByteStr.length();

        StringBuilder buffer = null;

        for (int i = 0; i < len; i++)
        {
            char c = doubleByteStr.charAt(i);

            if (
                c == '#' ||
                c > 0x7e ||
                (
                    c < 0x20 && // Less than a space
                    c != 0x09 && // Tab
                    c != 0x0a && // Line Feed
                    c != 0x0d // Carage return
                )
            )
            {
                if (buffer == null)
                {
                    if (i > 0)
                    {
                        buffer = new StringBuilder(doubleByteStr.substring(0, i));
                    }
                    else
                    {
                        buffer = new StringBuilder();
                    }
                }

                buffer.append("#");
                buffer.append(Integer.toHexString(c));
                buffer.append(";");
            }
            else
            {
                if (buffer != null)
                {
                    buffer.append(c);
                }
            }
        }

        if (buffer == null)
        {
            return doubleByteStr;
        }

        return buffer.toString();
    }

    /**
     *
     * @param utf8
     * @return the value
     */
    @CheckReturnValue
    public static String decodeUTF8(final @Nullable String utf8)
    {
        if (utf8 == null || isBlank(utf8))
        {
            return utf8;
        }

        boolean check = false;

        int len = utf8.length();

        for (int i = 0; check == false && i < len; i++)
        {
            char c = utf8.charAt(i);

            if (c >= 127)
            {
                check = true;
            }
        }

        if (check == false)
        {
            return utf8;
        }
        byte b[] = new byte[utf8.length()];

        for (int i = 0; i < b.length; i++)
        {
            char c = utf8.charAt(i);

            if (((int) c & 0x8000) > 0)
            {
                b[i] = (byte) (-1 * (c & 0x00ff));
            }
            else
            {
                b[i] = (byte) (c & 0x00ff);
            }
        }

        String doubleByteStr;

        doubleByteStr = new String(b, StandardCharsets.UTF_8);

        return doubleByteStr;
    }

    /**
     * Strip the tags from the HTML
     *
     * @param HTML the HTML to strip the tags from.
     * @param tags the tags to strip
     * @return the clean HTML.
     */
    @CheckReturnValue
    public static String stripTagsFromHTML( final @Nonnull String HTML, final @Nonnull String tags)
    {
        String temp = HTML.toUpperCase();
        StringBuilder builder=new StringBuilder();
        StringTokenizer st = new StringTokenizer(tags, ",");
        ArrayList list=new ArrayList();
        HashMap map = HashMapFactory.create();
        while( st.hasMoreTokens())
        {
            String rawTag = st.nextToken().trim().toUpperCase();
            String tag = "<" + rawTag;

            int pos = -1;
            while( true)
            {
                pos = temp.indexOf(tag, pos);

                if( pos == -1) break;

                Integer key = pos;
                if( map.containsKey(key) == false)
                {
                    map.put(key, "");
                    list.add(key);
                }
                pos++;
            }

            pos = -1;
            String tag2 = "</" + rawTag;
            while( true)
            {
                pos = temp.indexOf(tag2, pos);

                if( pos == -1) break;

                Integer key = pos;
                if( map.containsKey(key) == false)
                {
                    map.put(key, "");
                    list.add(key);
                }
                pos++;
            }
        }

        Collections.sort(list);

        int lastPos = 0;

        for (Object list1 : list)
        {
            int pos = ((Integer) list1);
            builder.append(HTML.substring(lastPos, pos));
            int nextPos = HTML.indexOf('>', pos);
            if( nextPos == -1)
            {
                lastPos=HTML.length();
            }
            else
            {
                lastPos=nextPos+1;
            }
        }

        builder.append(HTML.substring(lastPos));
        return builder.toString();
    }

    /**
     * Stript out passwords from urls in the form http://user:password@hostname
     * @param url
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String stripPasswordFromURL(final @Nullable String url)
    {
        if (url == null || isBlank(url))
        {
            return "";
        }
        else if( url.contains(":")==false)
        {
            return url;
        }
        //"sftp://docmgr:docmgr1@devserver/docs/2/2/6/6140/"

        String cleanURL=url;
        char splits[]={',','|'};
        
        for( char splitBy: splits)
        {
            String list[] =cleanURL.split("\\" + splitBy);
            int start=-1;
            StringBuilder sb= new StringBuilder();

            int length = list.length;
            for (int i = 0; i < length; i++)
            {
                String tmpURL = list[i];
                if( sb.length() > 0) sb.append( splitBy);
                int posProtocol = tmpURL.indexOf("://", start);

                if( posProtocol < 0) break;

                int posColon = tmpURL.indexOf(':', posProtocol + 1);
                int posAt = tmpURL.indexOf('@');
                String resUrl;
                if (posColon > 0 && posAt > 0 && posAt > posColon)
                {
                    int len = posAt - posColon;
                    if (len > 10)
                    {
                        len = 10;
                    }
                    String blank = "***********";
                    resUrl = tmpURL.substring(0, posColon + 1) + blank.substring(0, len) + tmpURL.substring(posAt);
                }
                else
                {
                    resUrl = tmpURL;
                }
                sb.append( resUrl);
            }
            cleanURL=sb.toString();
        }
        return cleanURL;
    }

    /**
     * Stript out passwords from URLs in the form http://user:password@hostname
     * @param urls the URLs
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String stripPasswordFromURLs(final @Nullable String urls)
    {
        if (urls == null || StringUtilities.isBlank(urls))
        {
            return "";
        }

        StringBuilder result = new StringBuilder("");
        String[] arrayStr = urls.split(",");
        for (int i = 0; i <arrayStr.length; i++)
        {
            if (i != 0)
            {
                result.append(",");
            }
            result.append(stripPasswordFromURL(arrayStr[i]));
        }
        return result.toString();
    }

    /**
     * encode a value used for isql to prevent sql injection
     * @param value the value
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String encodeISqlValue(final @Nonnull String value)
    {
        String temp = value.replace( "\\", "\\\\");
        temp = temp.replace( "'", "\\'");
        return "'" + temp + "'";
    }
    
    /**
     * count word count in a piece of html
     * @param html html code
     * @return word count
     */
    @CheckReturnValue
    public static int htmlWordCount(final @Nullable String html)
    {
        if(html == null)
        {
            return 0;
        }
        String strVal = html.toLowerCase();
        strVal = strVal.replaceAll("<.[^<>]*?>", " ").replaceAll("&nbsp;|&#160;|&#09;|&#10;|&#13;", " ").replaceAll("&ndash;|&mdash;|–|—|\u2013|\u2014", " ");
        strVal = strVal.replaceAll("&amp;", "a").replaceAll("(\\w+)(&.+?;)+(\\w+)", "$1$3").replaceAll("&.+?;", " ");
        String[] wordArray = strVal.split("\\s+");
        int count = 0;
        for(String str : wordArray)
        {
            if(StringUtilities.notBlank(str) && StringUtilities.notBlank(str.trim()))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Convert HTML to Text
     *
     * @param htmlText The HTML to convert
     * @throws Exception a serious problem
     * @return the plain text
     */
    @CheckReturnValue @Nonnull
    public static String convertHtmlToText(final @Nonnull String htmlText) throws Exception
    {
        // if blank then just return without starting a thread.
        if( isBlank( htmlText)) return htmlText;
        // if doesn't contain any HTML tags then just return.
        if(
            htmlText.contains("<") == false &&
            htmlText.contains("&") == false
        )
        {
            return htmlText;
        }

        try
        {
            HTMLEditorKit editor = new HTMLEditorKit();

            HTMLDocument doc = new HTMLDocument();

            String temp;

            temp = htmlText;
            for (String[] simplifyHTML1 : SIMPLIFY_HTML)
            {
                String from = simplifyHTML1[0];
                String to = simplifyHTML1[1];
                temp = temp.replace( from, to);
            }

            StringBuilder sb = new StringBuilder( temp.length());
            boolean startBracket = false;
            for( int i = 0; i < temp.length();i++)
            {
                char c = temp.charAt(i);
                if( c == '<')
                {
                    startBracket = true;
                }
                else if( c == '>')
                {
                    startBracket = false;
                }

                if( c == '/' && startBracket==false)
                {
                    sb.append("&#47;");
                }
                else
                {
                    sb.append( c);
                }
            }

            StringReader r = new StringReader(sb.toString());

            doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
            editor.read(r, doc, 0);

            StringWriter w = new StringWriter();
            TextWriter tw = new TextWriter(w, doc);

            tw.write();

            String text = w.toString();

            return text;
        }
        catch( IOException | BadLocationException | EmptyStackException t)
        {
            Exception e = new Exception( "convertHtmlToText( " + htmlText + ")", t); // wrap to control stack overflow

            throw e;
        }
    }

    /**
     *
     * @param doubleByteStr
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String encodeUTF8(String doubleByteStr)
    {
        if (isBlank(doubleByteStr))
        {
            return doubleByteStr;
        }
        assert checkUnicode( doubleByteStr): "contains invalid " + doubleByteStr;

        byte array[];

        array = doubleByteStr.getBytes( StandardCharsets.UTF_8);

        StringBuilder buffer = new StringBuilder(array.length);

        for (int i = 0; i < array.length; i++)
        {
            int b;

            b = array[i];
            if (b < 0)
            {
                b += 256;
            }

            char c = (char) b;

            buffer.append(c);
        }

        return buffer.toString();
    }

    /**
     *  Performs simple word wrapping.
     *
     * @param pWidth
     * @param pRow
     * @param text text to be wrap
     * @throws com.aspc.remote.database.NotFoundException
     * @return the value
     */
    @CheckReturnValue
    public static String wrap(
            String text,
            int pWidth,
            int pRow) throws NotFoundException
    {
        return wrap(text, pWidth, pRow, false);
    }

    /**
     *  Performs simple word wrapping.
     *  TODO handle case where next char after max columns is a new line
     *
     * @param pWidth
     * @param pRow
     * @param text text to be wrap
     * @param addMoreSymbol adds "..." to value if last line is truncated
     * @throws com.aspc.remote.database.NotFoundException
     * @return the value
     */
    @CheckReturnValue
    public static String wrap(
            String text,
            int pWidth,
            int pRow,
            boolean addMoreSymbol) throws NotFoundException
    {
        String result;

        boolean found = false;

        int i,
                tab_i,
                l,
                offset_i,
                row_i;

        result = "";

        /*
         ** Start at the begining of the string and loop wraping the text
         ** until we get to the row required.
         */
        row_i = 0;
        offset_i = 0;
        for (; row_i < pRow + 1; row_i++)
        {
            result = "";
            i = 0;
            tab_i = 0;
            OUTER:
            for (found = false; i + tab_i < pWidth && offset_i + i < text.length(); i++) {
                found = true;
                char charAt;
                charAt = text.charAt(offset_i + i);
                switch (charAt) {
                    case '\n':
                    case '\r':
                        /* terminate string and move pass the return */
                        i++;
                        if (charAt == '\n' &&
                                offset_i + i < text.length() &&
                                text.charAt(offset_i + i) == '\r')
                        {
                            i++;
                        }
                        else if (charAt == '\r' &&
                                offset_i + i < text.length() &&
                                text.charAt(offset_i + i) == '\n')
                        {
                            i++;
                        }   break OUTER;
                    case '\t':
                        for (l = 0; (l == 0 || (i + l) % 4 != 0) && i + tab_i < pWidth; l++)
                        {
                            result += ' ';
                        }   if (l > 0)
                        {
                            tab_i += l - 1;
                        }   break;
                    default:
                        result += charAt;
                        break;
                }
            }

            i += tab_i;

            boolean truncated = false;

            if (i >= pWidth &&
                    i == result.length() &&
                    offset_i + i < text.length())
            {
                truncated = true;
                if (addMoreSymbol && row_i == pRow)
                {
                    i -= 3;
                    result = result.substring(0, i);
                }
            }

            // don't roll back if we are at the end of a word
            if (truncated &&
                    Character.isWhitespace(text.charAt(offset_i + i)) == false)
            {
                /*
                 ** Come back until we find the beginning of the word.
                 ** Include '(', ')', '<' & '>' as part of a word
                 */
                char c = ' ';
                for (i--; i >= 0; i--)
                {
                    c = result.charAt(i);

                    if (Character.isLetterOrDigit(c) == false &&
                            c != '<' &&
                            c != '_' &&
                            c != '>' &&
                            c != '(' &&
                            c != ')')
                    {
                        break;
                    }
                }

                if (i > 0)
                {
                    if (Character.isWhitespace(c) == true)
                    {
                        result = result.substring(0, i);
                        i++;
                    }
                    else
                    {
                        result = result.substring(0, i);
                    }
                }
                else
                {
                    i = pWidth;
                }
            }

            if (truncated && addMoreSymbol && row_i == pRow)
            {
                result = result.concat("...");
            }

            i -= tab_i;
            if (i < 1)
            {
                i = 1;
            }

            // Role forward to first character on new line
            for (; offset_i + i < text.length(); i++)
            {
                char c = text.charAt(offset_i + i);

                if (Character.isWhitespace(c) == false)
                {
                    break;
                }
            }

            offset_i += i;
        }

        if (found == false)
        {
            throw new NotFoundException("Line not found");
        }

        return result;
    }

    /**
     * Encodes a <code>String</code> into a MIME format called
     * "<code>x-www-form-urlencoded</code>" format.
     *
     * for performance reasons check that we need to encode before creating new objects.
     *
     * @See StringUtilities.decode
     * @param str
     * @return the value
     */
    @CheckReturnValue @SuppressWarnings("null")
    public static @Nonnull String encode(final @Nonnull String str)
    {
        assert str!=null;
        if(str == null)
        {
            return null;
        }
        int len = str.length();
        boolean required = false;

        for (int i = 0; i < len && required == false; i++)
        {
            char c;

            c = str.charAt(i);

            if (
                (
                    c >= 'A' && c <= 'Z' ||
                    c >= 'a' && c <= 'z' ||
                    c >= '0' && c <= '9'
                ) == false
            )
            {
                required = true;
            }
        }

        if (required == false)
        {
            return str;
        }
        byte array[];
        array = str.getBytes(StandardCharsets.UTF_8);

        return encode(array, 0, array.length, false);
    }

    /**
     *
     * @param str
     * @return the value
     */
    @CheckReturnValue
    public static @Nonnull String encodeStrict(final @Nonnull String str)
    {
        byte array[];
        array = str.getBytes(StandardCharsets.UTF_8);

        return encode(array, 0, array.length, true);
    }

    /**
     *
     * @param array
     * @return the value
     */
    @CheckReturnValue
    public static @Nonnull String encode(final @Nonnull byte array[])
    {
        return encode(array, 0, array.length, false);
    }

    /**
     *
     * @param array
     * @param off
     * @param len
     * @param strict
     * @return the value
     */
    @CheckReturnValue
    public static String encode(final byte array[], final int off, final int len, final boolean strict)
    {
        StringBuilder sb = new StringBuilder(len * 3);

        for (int i = 0; i < len; i++)
        {
            int b;

            b = array[off + i];
            
            if (b < 0)
            {
                b += 256;
            }

            if (
                b >= 'A' && b <= 'Z' ||
                b >= 'a' && b <= 'z' ||
                b >= '0' && b <= '9' ||
                strict == false && ( // if not strict encode that the following characters are added as are
                    b == '-' ||
                    b == '_' ||
                    b == '.' ||
                    b == '*' ||
                    b == '(' ||
                    b == ')'
                )
            )
            {
                sb.append((char) b);
            }
            else if (b == ' ')
            {
                sb.append('+');
            }
            else
            {
                String t;
                t = HEX_CODES[b];
                sb.append(t);
            }
        }

        return sb.toString();
    }
    
    /**
     * check if the str is NOT strictly encoded into
     * "<code>x-www-form-urlencoded</code>" format.
     * @param str string to test
     * @return true if the str is NOT strictly encoded, false when the str is null or it could be encoded, eg: "%20" could be or could be NOT encoded
     */
    public static @CheckReturnValue boolean isNotEncoded(final @Nullable String str)
    {
        if(str == null)
        {
            return true;
        }
        
        if(
            str.matches(".*%[^0-9a-fA-F][^0-9a-fA-F].*") ||
            str.matches(".*%[0-9a-fA-F][^0-9a-fA-F].*") ||
            str.matches(".*%[^0-9a-fA-F].*") ||
            str.matches(".*%.?")
        )
        {
            return true;
        }
        byte[] array = str.getBytes(StandardCharsets.UTF_8);
        for (byte b: array)
        {
//            int b = array[i];
            
//            if (b < 0)
//            {
//                b += 256;
//            }

            if (
                (b < 'A' || b > 'Z') &&
                (b < 'a' || b > 'z') &&
                (b < '0' || b > '9') &&
                b != '-' &&
                b != '_' &&
                b != '.' &&
                b != '*' &&
                b != '(' &&
                b != ')' &&
                b != '%' &&
                b != '+'
            )
            {
                return true;
            }

        }
        return false;
    }

    /**
     * Decodes a <code>String</code> from a MIME format called
     * "<code>x-www-form-urlencoded</code>" format.
     * <p>
     * <ul>
     * <li>The ASCII characters '<code>a</code>' through '<code>z</code>',
     *     '<code>A</code>' through '<code>Z</code>', and '<code>0</code>'
     *     through '<code>9</code>' remain the same.
     * <li>The space character '<code>&nbsp;</code>' is converted into a
     *     plus sign '+'.
     * <li>Special characters '-' '_' '.' '*' remain unchanged
     * <li>All other characters are converted into the 3-character string
     *     "<code>%<i>xy</i></code>", where <i>xy</i> is the two-digit
     *     hexadecimal representation of the lower 8-bits of the character.
     * </ul>
     * @param str
     * @throws java.lang.RuntimeException
     * @return the value
     */
    @CheckReturnValue
    public static String decode(final String str)
    {
        /*
         * The need for speed.
         */
        if (
            isBlank(str) ||
            (
                str.indexOf('+') == -1 &&
                str.indexOf('%') == -1)
            )
        {
            return str;
        }

        byte output[];

        output = decodeToBytes(str);
        String s = new String(output, StandardCharsets.UTF_8);

        return s;
    }

    /**
     * Decode into bytes
     * @param str
     * @throws java.lang.RuntimeException
     * @return the value
     */
    @SuppressWarnings({"ValueOfIncrementOrDecrementUsed", "AssignmentToForLoopParameter"})
    @CheckReturnValue
    public static byte[] decodeToBytes(final String str)
    {
        byte array[],
                output[];

        array = str.getBytes(StandardCharsets.UTF_8);
        final int inLen=array.length;

        output = new byte[inLen];

        int len = 0;
        for (int i = 0; i < inLen; i++)
        {
            int b;

            b = array[i];

            if (b < 0)
            {
                b += 256;
            }
            switch (b) {
                case '+':
                case ' ':
                    output[len++] = ' ';
                    break;
                case '%':
                    if( i + 2>=inLen)
                    {
                        throw new IllegalArgumentException("not an encoded value: " + str);
                    }
                    char c1 = (char) array[i + 1];
                    char c2 = (char) array[i + 2];
                    b = Character.digit(c1, 16);
                    b *= 16;
                    b += Character.digit(c2, 16);
                    output[len++] = (byte) b;
                    i += 2;
                    break;
                default:
                    output[len++] = (byte) b;
                    break;
            }
        }

        // Need for speed.
        if (len == output.length)
        {
            return output;
        }

        byte result[] = new byte[len];

        System.arraycopy(output, 0, result, 0, len);

        return result;
    }

    /**
     *
     * @param strToPad
     * @param len
     * @param padStr
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static String leftPad(final @Nonnull String strToPad, final @Nonnegative int len, final @Nonnull String padStr)
    {
        if( strToPad == null) throw new IllegalArgumentException( "strToPad is mandatory");
        if( len <0) throw new IllegalArgumentException( "len must be nonnegative: " + len);
        if( padStr == null) throw new IllegalArgumentException( "padStr is mandatory");
        if( padStr.length()==0) throw new IllegalArgumentException( "padStr is zero length");
        
        StringBuilder buffer = new StringBuilder(strToPad);

        while (buffer.length() < len)
        {
            buffer.insert(0, padStr);
        }

        return buffer.substring(0, len);
    }

    /**
     *
     * @param obj
     * @return the value
     */
    @CheckReturnValue
    public static String obscure(Object obj)
    {
        if (isBlank(obj))
        {
            return "";
        }
        StringBuilder buffer = new StringBuilder();

        String temp = obj.toString();

        for (int i = 0; i < temp.length(); i++)
        {
            buffer.append("*");
        }

        return buffer.toString();
    }

    /**
     * Pad the string with the padding string and then truncate to the expected size.
     * 
     * @param strToPad The string that will be appended to.
     * @param len The final length of the string.
     * @param padStr the string to pad with. 
     * @return the value the resulting value which will always be expected length long. 
     */
    @CheckReturnValue
    public static String rightPad(final @Nonnull String strToPad, final @Nonnegative int len, final @Nonnull String padStr)
    {
        if( strToPad == null) throw new IllegalArgumentException( "strToPad is mandatory");
        if( len <0) throw new IllegalArgumentException( "len must be nonnegative: " + len);
        if( padStr == null) throw new IllegalArgumentException( "padStr is mandatory");
        if( padStr.length()==0) throw new IllegalArgumentException( "padStr is zero length");
        StringBuilder buffer = new StringBuilder(strToPad);

        while (buffer.length() < len)
        {
            buffer.append(padStr);
        }

        return buffer.substring(0, len);
    }

    /**
     * compress a java string with gzip and encode to ASCII with basE91
     * @param text the string to be compressed
     * @return ASCII string
     * @throws Exception a serious exception
     */
    @CheckReturnValue
    public static String compressB91(final String text) throws Exception
    {
        byte[] data = compressToBytes(text);
        return encodeBase91(data);
    }

    /**
     * decompress a string with gzip to a normal native java string
     * @param compressedData ASCII string to be decompressed
     * @return original string
     * @throws Exception a serious exception
     */
    @CheckReturnValue
    public static String decompressB91(final String compressedData) throws Exception
    {
        byte data[] = decodeBase91(compressedData);
        return decompress(data);
    }

    /**
     * encode a String to basE91 ASCII, basE91 is similar to BASE64 but more
     * efficient in both encoding speed and string size
     * @param data the data
     * @return ASCII string
     */
    @CheckReturnValue
    public static String encodeBase91(final byte[] data )
    {
        StringBuilder builder = new StringBuilder((int)(data.length * 1.3));

        int ebq = 0, en = 0;
        int size = data.length;
        for(int i=0; i<size; i++)
        {
            byte b = data[i];
            ebq |= ((b & 0xFF) & 255) << en;
            en += 8;
            if(en > 13)
            {
                int ev = ebq & 8191;

                if(ev > 88)
                {
                    ebq >>= 13;
                    en -= 13;
                }
                else
                {
                    ev = ebq & 16383;
                    ebq >>= 14;
                    en -= 14;
                }
                builder.append((char)(BASE91BYTES[ev % 91] & 0xFF));
                builder.append((char)(BASE91BYTES[ev / 91] & 0xFF));
            }
        }

        if(en > 0)
        {
            builder.append((char)(BASE91BYTES[ebq % 91] & 0xFF));
            if(en > 7 || ebq > 90)
            {
                builder.append((char)(BASE91BYTES[ebq / 91] & 0xFF));
            }
        }

        return builder.toString();
    }

    /**
     * decode a basE91 to original string
     * @param src string to be decoded
     * @return the original string
     * @throws Exception a serious exception
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    @CheckReturnValue
    public static byte[] decodeBase91(final String src) throws Exception
    {
        byte data[] = new byte[src.length()];
        int len = 0;
        int dv = -1, dbq = 0, dn = 0;

        for(int i = 0; i < src.length(); i++)
        {
            byte c = (byte)(src.charAt(i) & 0xFF);
            if( c< 0)
            {
                throw new IllegalArgumentException("not base91 encoded: " + src);
            }
            if (BASE91DECODER[c] == -1)
            {
                continue;
            }
            if(dv == -1)
            {
                dv = BASE91DECODER[c];
            }
            else
            {
                dv += BASE91DECODER[c] * 91;
                dbq |= dv << dn;
                dn += (dv & 8191) > 88 ? 13 : 14;
                do
                {
                    data[ len++] = (byte)((byte)dbq & 0xFF);
                    dbq >>= 8;
                    dn -= 8;
                }
                while(dn > 7);
                dv = -1;
            }
        }

        if (dv != -1)
        {
            data[ len++] = (byte)((byte)(dbq | dv << dn) & 0xFF);
        }

        byte targetArray[];
        if( len != data.length)
        {
            targetArray = new byte[len];
            System.arraycopy(data, 0, targetArray, 0, len);
        }
        else
        {
            targetArray=data;
        }

        return targetArray;
    }

    /**
     * Encode a normal string UTF8 and then base 64
     * @param normalString normal Java String
     * @return utf8 and base64 encoded string.
     */
    @CheckReturnValue
    public static String encodeUTF8base64( final String normalString)
    {
        String utf8=encodeUTF8(normalString);
        String encoded64=encodeBase64(utf8);

        return encoded64;
    }

    /**
     * decode a normal string from UTF8 & base 64 String.
     * @param base64utf8 a String that is first UTF8 and then base64
     * @return utf8 and base64 encoded string.
     */
    @CheckReturnValue
    public static String decodeUTF8base64( final String base64utf8)
    {
        String utf8=decodeBase64(base64utf8);
        String normal=decodeUTF8(utf8);

        return normal;
    }

    /**
     * check all characters are Unicode.
     * @param text the text to check
     * @return true if all valid
     */
    @CheckReturnValue
    public static boolean checkUnicode( final String text)
    {
        for( char c : text.toCharArray())
        {
            if( c > 0xD7FF && c < 0xE000)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * check all characters are valid XML characters.
     * @param text the text to check
     * @return true if all valid or text is NULL.
     */    
    @CheckReturnValue
    public static boolean checkXML( final @Nullable String text)
    {
        if( text == null) return false;
        
        for( char c : text.toCharArray())
        {
            if( checkXML(c)==false)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * http://en.wikipedia.org/wiki/Valid_characters_in_XML
     * @param c the character to check
     * @return true if valid
     */
    @CheckReturnValue
    public static boolean checkXML( final int c)
    {
        return (c == 0x9) ||
                (c == 0xA) ||
                (c == 0xD) ||
                ((c >= 0x20) && (c <= 0xD7FF)) ||
                ((c >= 0xE000) && (c <= 0xFFFD)) ||
                ((c >= 0x10000) && (c <= 0x10FFFF));
    }
    
    @CheckReturnValue
    public static boolean check8Bit( final String text)
    {
        for( char c : text.toCharArray())
        {
            if( c > 0xff)
            {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @param szSrc
     * @return the value
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    @CheckReturnValue
    public static String encodeBase64(final String szSrc)
    {
        assert check8Bit( szSrc): "not 8 but chars " + szSrc;
        byte[] bytes = szSrc.getBytes(StandardCharsets.UTF_8);
        if( bytes.length==0) return "====";
        byte[] out =encodeBase64(bytes);

        return new String( out, StandardCharsets.UTF_8);
    }

    /**
     * The Web Servers strip off the trailing ='s so just append them if
     * needed
     * @param encodedStr encoded string
     * @return the decoded string
     */
    @CheckReturnValue @Nonnull
    public static String decodeBase64(final @Nonnull String encodedStr)
    {
        assert encodedStr!=null:"encoded string must be non null";
        if( encodedStr==null) throw new IllegalArgumentException( "encoded string must be non null");
//        if (encodedStr == null)
//        {
//            return null;
//        }

        assert check8Bit( encodedStr): "not all 8 bit chars " + encodedStr;
        byte[] bytes = encodedStr.replace(" ", "+").getBytes(StandardCharsets.UTF_8);
        if( bytes.length == 0) return "";
        byte[] decoded = decodeBase64(bytes);

        return new String( decoded, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a byte array to base64
     *
     * @return byte[] the encoded bytes
     * @param input the byte array to encode
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    @CheckReturnValue
    public static byte[] encodeBase64(final byte[] input)
    {
        isEmpty(input);

        int full24 = (input.length / 3);
        int partial = (input.length % 3);
        int len = full24 * 4;
        if (partial != 0)
        {
            len += 4; // final plus padding
        }
        byte[] result = new byte[len];
        int pos = 0; // current position in result

        if (full24 > 0) // input is three bytes or greater
        {
            for (int i = 0; i < (full24 * 3);)
            {
                /* left shift 24 gets rid of sign bits when java casts byte to int */
                int bits = ((input[i++] << 24) >>> 8) |
                        ((input[i++] << 24) >>> 16) |
                        ((input[i++] << 24) >>> 24);

                result[pos++] = BASE64BYTES[(bits >>> 18)];
                result[pos++] = BASE64BYTES[(bits >>> 12) & 0x3f];
                result[pos++] = BASE64BYTES[(bits >>> 6) & 0x3f];
                result[pos++] = BASE64BYTES[(bits) & 0x3f];
            }
        }

        if (partial > 0)
        {
            int offset = (full24 * 3);
            int bits = ((input[offset] << 24) >>> 8);
            if (partial == 2)
            {
                bits |= ((input[offset + 1] << 24) >>> 16);
            }

            result[pos++] = BASE64BYTES[(bits >>> 18)];
            result[pos++] = BASE64BYTES[(bits >>> 12) & 0x3f];
            if (partial == 2)
            {
                result[pos++] = BASE64BYTES[(bits >>> 6) & 0x3f];
            }

            /* padding */
            while (pos < result.length)
            {
                result[pos++] = BASE64BYTES[ 64];
            }
        }

        return result;
    }

    /**
     * Decodes a byte array that is base64 encoded. The decoder will only handle
     * base64 characters and the padding byte '=', if your input contains other characters,
     * such as newlines used by certain protocol max line lengths, an exception will be thrown.
     * You can split/remove the illegal characters before calling this method. It is expecting
     * well formed input, i.e. input should be 4 byte multiples with padding as appropriate. It
     * will however *sometimes* handle input where the padding has been stripped ( by a web server for example )
     * @param input the base64 byte array to decode
     * @return byte[] the decoded byte array
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    @CheckReturnValue
    public static byte[] decodeBase64(final byte[] input)
    {
        isEmpty(input);

        int pad = 0;
        for (int i = (input.length - 1); i >= 0 && input[i--] == BASE64BYTES[ 64];)
        {
            pad++;
        }

        int octets = (input.length / 4);
        int partial = (input.length % 4); /* web server might strip '=' */
        int len = octets * 3;


        if (partial == 2)
        {
            len += 1;
        }
        if (partial == 3)
        {
            len += 2;
        }

        if (partial > 0 && pad > 0)
        {
            /* if the input is not a multiple of four and we have padding there is
            something wrong with the input */
            throw new IllegalArgumentException("invalid input byte array length " + input.length);
        }

        if (partial == 1)
        {
            throw new IllegalArgumentException("illegal input length " + input.length);
        }

        if (pad == input.length)
        {
            return new byte[0];
        }

        len -= pad;

        if (pad > 0 && octets > 0)
        {
            octets -= 1;
        }

        byte[] result = new byte[len];
        int pos = 0; // current result position

        /* scan for illegal input */
        for (int i = 0; i < input.length; i++)
        {
            byte b = input[i];
            if (BASE64DECODER[b] == -1)
            {
                IllegalArgumentException illegalArgumentException = new IllegalArgumentException(
                    "Parameter not base64 encoded, illegal byte '" + encode( new byte[]{b}) +"' at offset " + i
                );
//                LOGGER.warn( encode(input), illegalArgumentException);
                
                throw illegalArgumentException;
            }
        }

        if (octets > 0) // input is four bytes or greater
        {
            for (int i = 0; i < (octets * 4);)
            {
                int bits = ((BASE64DECODER[input[i++]] << 24) >>> 6) |
                           ((BASE64DECODER[input[i++]] << 24) >>> 12) |
                           ((BASE64DECODER[input[i++]] << 24) >>> 18) |
                           ((BASE64DECODER[input[i++]] << 24) >>> 24);

                result[pos++] = (byte) (bits >> 16);
                result[pos++] = (byte) (bits >> 8);
                result[pos++] = (byte) (bits);
            }
        }

        if (partial > 0 || pad > 0)
        {
            int offset = (octets * 4);
            int bits = ((BASE64DECODER[input[offset]] << 24) >>> 6) |
                       ((BASE64DECODER[input[offset + 1]] << 24) >>> 12);
            if (partial == 3 || pad == 1)
            {
                bits |= ((BASE64DECODER[input[offset + 2]] << 24) >>> 18);
            }

            result[pos++] = (byte) (bits >>> 16);
            if (partial == 3 || pad == 1)
            {
                result[pos++] = (byte) (bits >>> 8);
            }
        }

        return result;
    }

    private static void isEmpty(final byte[] input) throws IllegalArgumentException
    {
        if (input == null)
        {
            throw new IllegalArgumentException("input byte[] is null");
        }

        if (input.length == 0)
        {
            throw new IllegalArgumentException("zero length input");
        }
    }

    /**
     * byte array to hex string
     * @param b byte array
     * @return hex string
     */
    @CheckReturnValue
    public static String byteArrayToHexString(byte[] b)
    {
        String result = "";
        for(int i = 0; i < b.length; i++)
        {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     *
     * @param szSrc
     * @return the value
     */
    @CheckReturnValue
    public static String encodeHex(String szSrc)
    {
        StringBuilder sResult = new StringBuilder(szSrc.length() * 2);

        int len = szSrc.length();
        int src;

        for (src = 0; src < len; src++)
        {
            char c;
            c = szSrc.charAt(src);

            sResult.append(Integer.toHexString(c).toUpperCase());
        }

        return sResult.toString();
    }

    /**
     * The Web Servers strip off the trailing ='s so just append them if
     * needed
     * @param szSrc
     * @return the value
     */
    @CheckReturnValue
    public static String decodeHex(String szSrc)
    {
        if (szSrc == null)
        {
            return null;
        }
        StringBuilder sResult = new StringBuilder(szSrc.length() / 2);

        int len = szSrc.length();
        int src;

        if (len % 2 > 0)
        {
            return "";
        }

        for (src = 0; src < len; src += 2)
        {
            int res;
            char c;

            res = Integer.parseInt(szSrc.substring(src, src + 2), 16);
            c = (char) res;
            sResult.append(c);
        }

        return sResult.toString();
    }

    /**
     * Checks if the query can be found in the source string.
     * @param src
     * @param qry
     * @return true if the query string was found, false otherwise
     */
    @CheckReturnValue
    public static boolean contains(final @Nullable String src, final @Nullable String qry)
    {
        boolean found = false;

        if (src != null && qry != null)
        {
            int pos = src.indexOf(qry);
            if (pos != -1)
            {
                found = true;
            }
        }

        return found;
    }

    /**
     * Checks if the query string is in the given list
     * @param lst the list to search
     * @param qry the query string
     * @return boolean true if the query string is in the list of strings, false otherwise
     */
    @CheckReturnValue
    public static boolean inList(final String[] lst, final String qry)
    {
        boolean found = false;

        if (lst != null && qry != null)
        {
            for (String lst1 : lst)
            {
                if (qry.equals(lst1))
                {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    /**
     * Determines whether 1.4. version is enabled.
     * @param version
     * @return true if enabled false otherwise
     */
    @CheckReturnValue
    public static boolean is14Enabled(String version)
    {
        boolean isEnabled = false;

        if (version != null &&
                version.length() > 2 &&
                version.equalsIgnoreCase("null") == false)
        {
            int upper = Integer.parseInt(Character.toString(version.charAt(0)));
            int lower = Integer.parseInt(Character.toString(version.charAt(2)));

            if (upper >= 1 && lower >= 4)
            {
                isEnabled = true;
            }
        }
        return isEnabled;
    }

    /**
     * Converts and array of bytes to a hexadecimal string, two characters per byte
     *
     * @return String the hexadecimal representation
     * @param bytes the bytes to convert
     */
    @CheckReturnValue
    public static String toHexString(final byte[] bytes)
    {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++)
        {
            hexString.append(HEX_CHAR[(bytes[i] & 0xf0) >>> 4]);
            hexString.append(HEX_CHAR[(bytes[i] & 0x0f)]);
        }

        return hexString.toString();
    }

    /*//LEGACY_START
    public static String[] oldSplit(final String fieldDelim, String values)
    {
        return values.split(fieldDelim.replace("\\", "\\\\").replace(".", "\\."));
    }

    public static StringBuffer replaceAllBufferChar( StringBuffer buffy, char c, String value)
    {
        String v = buffy.toString();
        return new StringBuffer( v.replace("" +c, value));
    }
    *///LEGACY_END

    @CheckReturnValue
    public static String join(final String fieldDelim, List list)
    {
        StringBuilder sb=new StringBuilder();
        list.stream().forEach((v) -> {
            if( sb.length() != 0) sb.append( fieldDelim);

            sb.append(v);
        });
        return sb.toString();
    }


    /**
     * Performs a split on a string that contains multiple values separated by a field delimiter
     * and with strings grouped by a string delimiter
     * @param values - value to be parsed
     * @param fieldDelim the field delimiter 
     * @param quote the quote character
     * @return String[] - list of String values
     */
    @SuppressWarnings("null")
    @CheckReturnValue
    public static String[] split(final String values, final char fieldDelim, final char quote)
    {
        String temp = values.trim();
        ArrayList fieldArray = new ArrayList();

        while (StringUtilities.isBlank(temp) == false)
        {
            if (temp.charAt(0) == quote)
            {
                int last = -1;
                StringBuilder buffer = new StringBuilder();
                boolean lastEscape = false;

                for (int i = 1; i < temp.length(); i++)
                {
                    char c = temp.charAt(i);

                    if (lastEscape == true)
                    {
                        lastEscape = false;
                        buffer.append(c);
                        continue;
                    }

                    if (c == '\\')
                    {
                        lastEscape = true;
                    }
                    else if (c == quote)
                    {
                        last = i;
                        break;
                    }
                    else
                    {
                        buffer.append(c);
                    }
                }

                if (last > 0)
                {
                    fieldArray.add(buffer.toString());

                    temp = temp.substring(last + 1);
                }
                else// if( buffer.length() != 0)
                {                    
                    throw new IllegalArgumentException("unclosed quote string: " + values);
                }

                int pos = temp.indexOf(fieldDelim);

                if (pos == -1)
                {
                    break;
                }
                temp = temp.substring(pos + 1).trim();
            }
            else
            {
                int pos = temp.indexOf(fieldDelim);

                String value;

                if (pos == -1)
                {
                    value = temp;

                    temp = null;
                }
                else
                {
                    value = temp.substring(0, pos);

                    if (temp.length() > pos + 1)
                    {
                        temp = temp.substring(pos + 1).trim();
                    }
                    else
                    {
                        temp = null;
                    }
                }

                fieldArray.add(value);
            }
        }
        
        String[] valueList = new String[fieldArray.size()];
        fieldArray.toArray(valueList);
        return valueList;
    }

    /**
     * Capitalizes the first character IN EACH WORD of the specified string.
     *
     * @param string the name to capitalize
     * @return The specified string with the first letter capitalized.
     */
    @CheckReturnValue
    public static String capitalizeWordsInString(String string)
    {
          char[] chars = string.toLowerCase().toCharArray();
          boolean found = false;
          for (int i = 0; i < chars.length; i++)
          {
              if (!found && Character.isLetter(chars[i]))
              {
                  chars[i] = Character.toUpperCase(chars[i]);
                  found = true;
              }
              else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'' || chars[i]=='-')  // You can add other chars here
              {
                 found = false;
              }
      }
      return String.valueOf(chars);
    }

    /**
     * Capitalizes the first letter of the specified string.
     *
     * @param str the name to capitalize
     * @return The specified string with the first letter capitalized.
     */
    @CheckReturnValue
    public static String capitalize(final String str)
    {
        if (isBlank(str))
        {
            return str;
        }

        String firstLetter = str.substring(0, 1);
        String remainder = (str.length() > 1) ? str.substring(1) : "";

        return firstLetter.toUpperCase() + remainder;
    }

    /**
     * Returns the index within the first string of the first occurrence of the
     * second string, ignoring case considerations.
     * The integer returned is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * string1.toUpperCase().startsWith(string2.toUpperCase(), <i>k</i>)
     * </pre></blockquote>
     * is <code>true</code>.
     *
     * @param string1 the string for which to be searched.
     * @param string2 the string for which to search.
     * @return if the second string occurs as a substring within the first string,
     *         then the index of the first character of the first such substring is returned;
     *         if it does not occur as a substring, -1 is returned.
     */
    @CheckReturnValue
    public static int indexOfIgnoreCase(String string1, String string2)
    {
        return string1.toUpperCase().indexOf(string2.toUpperCase());
    }

    /**
     * check if password have sequential characters.
     *
     * @param password the password to check
     * @return boolean value
     */
    @CheckReturnValue
    public static boolean passwordHaveSequentialCharacters(final String password)
    {
        return passwordHaveSequentialCharacters(password, true, true, PASSWORD_SEQUENTIAL_LENGTH);
    }

    /**
     * check if password have sequential characters.
     *
     * @param password the password to check
     * @param repeat
     * @param descend
     * @param limit
     * @return boolean value
     */
    @CheckReturnValue
    public static boolean passwordHaveSequentialCharacters(
        final String password,
        boolean repeat,
        boolean descend,
        int limit
    )
    {
        if (isBlank( password ) == false)
        {
            char passwordArray[] = password.trim().toCharArray();
            int old = passwordArray[0];
            int countUp = 1;
            int countDown = 1;
            int countEqual = 1;
            boolean firstLoop = true;

            for (char ch : passwordArray)
            {
                int ascii = ch;
                if (firstLoop)
                {
                    firstLoop = false;
                }
                else
                {
                    if (ascii == old + 1)
                    {
                        countUp++;
                        if (countUp > limit)
                        {
                            return true;
                        }
                    } else if (ascii == old && repeat)
                    {
                        countEqual++;
                        if (countEqual > limit)
                        {
                            return true;
                        }
                    } else if (ascii == old - 1 && descend)
                    {
                        countDown++;
                        if (countDown > limit)
                        {
                            return true;
                        }
                    } else
                    {
                        countUp = 1;
                        countDown = 1;
                        countEqual = 1;
                    }
                }
                old = ascii;
            }
        }
        return false;
    }

    /**
     * Does this word contain a rude word.
     *
     * When generating a password just skip over rude words.
     * @param word the word to check
     * @return true if there is a possible rude word.
     */
    @CheckReturnValue
    public static boolean containsRudeWord( final String word)
    {
        String tmpWord=word.toLowerCase();
        String list[]={
            "fuck",
            "suck",
            "dick",
            "crap",
            "sex",
            "cunt",
            "shit",
            "nigger"
        };
        for( String w: list)
        {
            if( tmpWord.contains(w))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * This method cleans chars that cause parsing problems when we want to parse String to Number (Integer,Double,Float,Long....)
     * @param valueToClean - the String that we want to clean
     * @return new clean value
     */
    @CheckReturnValue
    public static String cleanNumberString(final String valueToClean)
    {
        int len = valueToClean.length();
        boolean needed = false;
        for( int i = 0; i < len;i++)
        {
            char c = valueToClean.charAt(i);

            if( c != '.' && (c >= '0' && c <= '9') == false )
            {
                needed=true;
                break;
            }
        }

        if( needed == false) return valueToClean;

        String temp = valueToClean;
        temp = temp.replace("$", "");
        temp = temp.replaceAll(",", "");
        temp = temp.replaceAll("%", "");

        return temp.trim();
    }

     /**
     * Determines whether the value contains only numbers,
      * Please do not use this method for checking numeric value,
      * because it not allows dots,minus,hex...
     * @param value value
     * @return true if the value contains only numbers
     */
    @CheckReturnValue
    public static boolean isNumber(String value)
    {
        if (value.length() == 0)
        {
            return false;
        }
        char[] chars = value.toCharArray();

        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] < '0' || chars[i] > '9')
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Is valid phone number
     * @param phone phone
     * @return the value
     */
    @CheckReturnValue
    public static boolean isValidPhoneNumber(final String phone)
    {
        boolean valid = PHONE_REGEX.matcher(phone).matches();

        if (valid)
        {
            String tmpPhone = phone.replaceAll("[^0-9]", "");

            if (tmpPhone.length() > 15 && tmpPhone.length() > 2)
            {
                valid = false;
            }
        }
        return valid;
    }

     /**
     * Escapes special characters using a backslash
     * @param value the string
     * @return escaped string
     */
    @CheckReturnValue
    public static String escape(final String value)
    {
        if (value == null || value.length() == 0)
        {
            return value;
        }
 
        String temp=value;

        temp = temp.replace( "\\", "\\\\");
        temp = temp.replace( ",", "\\,");
        temp = temp.replace( "=", "\\=");
        temp = temp.replace( "+", "\\+");
        temp = temp.replace( "<", "\\<");
        temp = temp.replace( ">", "\\>");
        temp = temp.replace( "#", "\\#");
        temp = temp.replace( ";", "\\;");

        temp = temp.replace( "\"", "\\\"");
        temp = temp.replace( "'", "\\'");

        return temp;
    }

     /**
     * Appends param and value to the given url.
     *
     * @param uri
     * @param param
     * @param value the value
     * @return the value
     */
    @CheckReturnValue
    public static String appendURLParam(final String uri, final String param, final String value)
    {
        StringBuilder urlString = new StringBuilder(uri);
        if (!uri.contains("?"))
        {
            urlString.append("?");
        }
        else if(uri.endsWith("&") == false)
        {
            urlString.append("&");
        }
        urlString.append(encode(param));
        urlString.append("=");
        urlString.append(encode(value));
        String url2=urlString.toString();
     //   if( HTMLPage.URI_PATTERN.matcher( url2).find() ==false)
      //  {
       //     LOGGER.info(url2);
        //}
        assert URI_PATTERN.matcher( url2).find(): "invalid " + url2;
        return url2;
    }
    
    /**
     * Source <a href="http://www.w3.org/TR/html4/sgml/entities.html">reference/<a>
     *
     * The symbols ARE case sensitive see Alpha and alpha.
     */
    private static final String SIMPLIFY_HTML[][] =
    {
        {
            "<br />", "<BR>"
        },
        {
            "<br/>", "<BR>"
        },
        {
            "<hr />", ""// horizontal row -- just remove
        },
        {
            "<hr/>", ""// horizontal row -- just remove
        },
        {
            "&iexcl;", "&#161;"
        }, // inverted exclamation mark, U+00A1 ISOnum

        {
            "&cent;", "&#162;"
        }, // cent sign, U+00A2 ISOnum

        {
            "&pound;", "&#163;"
        }, // pound sign, U+00A3 ISOnum

        {
            "&curren;", "&#164;"
        }, // currency sign, U+00A4 ISOnum

        {
            "&yen;", "&#165;"
        }, // yen sign = yuan sign, U+00A5 ISOnum

        {
            "&brvbar;", "&#166;"
        }, // broken bar = broken vertical bar,U+00A6 ISOnum

        {
            "&sect;", "&#167;"
        }, // section sign, U+00A7 ISOnum

        {
            "&uml;", "&#168;"
        }, // diaeresis = spacing diaeresis, U+00A8 ISOdia

        {
            "&copy;", "&#169;"
        }, // copyright sign, U+00A9 ISOnum

        {
            "&ordf;", "&#170;"
        }, // feminine ordinal indicator, U+00AA ISOnum

        {
            "&laquo;", "&#171;"
        }, // left-pointing double angle quotation mark = left pointing guillemet, U+00AB ISOnum

        {
            "&not;", "&#172;"
        }, // not sign, U+00AC ISOnum

        {
            "&shy;", "&#173;"
        }, // soft hyphen = discretionary hyphen, U+00AD ISOnum

        {
            "&reg;", "&#174;"
        }, // registered sign = registered trade mark sign, U+00AE ISOnum

        {
            "&macr;", "&#175;"
        }, // macron = spacing macron = overline = APL overbar, U+00AF ISOdia

        {
            "&deg;", "&#176;"
        }, // degree sign, U+00B0 ISOnum

        {
            "&plusmn;", "&#177;"
        }, // plus-minus sign = plus-or-minus sign, U+00B1 ISOnum

        {
            "&sup2;", "&#178;"
        }, // superscript two = superscript digit two = squared, U+00B2 ISOnum

        {
            "&sup3;", "&#179;"
        }, // superscript three = superscript digit three = cubed, U+00B3 ISOnum

        {
            "&acute;", "&#180;"
        }, // acute accent = spacing acute, U+00B4 ISOdia

        {
            "&micro;", "&#181;"
        }, // micro sign, U+00B5 ISOnum

        {
            "&para;", "&#182;"
        }, // pilcrow sign = paragraph sign, U+00B6 ISOnum

        {
            "&middot;", "&#183;"
        }, // middle dot = Georgian comma = Greek middle dot, U+00B7 ISOnum

        {
            "&cedil;", "&#184;"
        }, // cedilla = spacing cedilla, U+00B8 ISOdia

        {
            "&sup1;", "&#185;"
        }, // superscript one = superscript digit one, U+00B9 ISOnum

        {
            "&ordm;", "&#186;"
        }, // masculine ordinal indicator, U+00BA ISOnum

        {
            "&raquo;", "&#187;"
        }, // right-pointing double angle quotation mark = right pointing guillemet, U+00BB ISOnum

        {
            "&frac14;", "&#188;"
        }, // vulgar fraction one quarter = fraction one quarter, U+00BC ISOnum

        {
            "&frac12;", "&#189;"
        }, // vulgar fraction one half = fraction one half, U+00BD ISOnum

        {
            "&frac34;", "&#190;"
        }, // vulgar fraction three quarters = fraction three quarters, U+00BE ISOnum

        {
            "&iquest;", "&#191;"
        }, // inverted question mark = turned question mark, U+00BF ISOnum

        {
            "&Agrave;", "&#192;"
        }, // latin capital letter A with grave = latin capital letter A grave, U+00C0 ISOlat1

        {
            "&Aacute;", "&#193;"
        }, // latin capital letter A with acute, U+00C1 ISOlat1

        {
            "&Acirc;", "&#194;"
        }, // latin capital letter A with circumflex, U+00C2 ISOlat1

        {
            "&Atilde;", "&#195;"
        }, // latin capital letter A with tilde, U+00C3 ISOlat1

        {
            "&Auml;", "&#196;"
        }, // latin capital letter A with diaeresis, U+00C4 ISOlat1

        {
            "&Aring;", "&#197;"
        }, // latin capital letter A with ring above = latin capital letter A ring, U+00C5 ISOlat1

        {
            "&AElig;", "&#198;"
        }, // latin capital letter AE = latin capital ligature AE, U+00C6 ISOlat1

        {
            "&Ccedil;", "&#199;"
        }, // latin capital letter C with cedilla, U+00C7 ISOlat1

        {
            "&Egrave;", "&#200;"
        }, // latin capital letter E with grave, U+00C8 ISOlat1

        {
            "&Eacute;", "&#201;"
        }, // latin capital letter E with acute, U+00C9 ISOlat1

        {
            "&Ecirc;", "&#202;"
        }, // latin capital letter E with circumflex, U+00CA ISOlat1

        {
            "&Euml;", "&#203;"
        }, // latin capital letter E with diaeresis, U+00CB ISOlat1

        {
            "&Igrave;", "&#204;"
        }, // latin capital letter I with grave, U+00CC ISOlat1

        {
            "&Iacute;", "&#205;"
        }, // latin capital letter I with acute, U+00CD ISOlat1

        {
            "&Icirc;", "&#206;"
        }, // latin capital letter I with circumflex, U+00CE ISOlat1

        {
            "&Iuml;", "&#207;"
        }, // latin capital letter I with diaeresis, U+00CF ISOlat1

        {
            "&ETH;", "&#208;"
        }, // latin capital letter ETH, U+00D0 ISOlat1

        {
            "&Ntilde;", "&#209;"
        }, // latin capital letter N with tilde, U+00D1 ISOlat1

        {
            "&Ograve;", "&#210;"
        }, // latin capital letter O with grave, U+00D2 ISOlat1

        {
            "&Oacute;", "&#211;"
        }, // latin capital letter O with acute, U+00D3 ISOlat1

        {
            "&Ocirc;", "&#212;"
        }, // latin capital letter O with circumflex, U+00D4 ISOlat1

        {
            "&Otilde;", "&#213;"
        }, // latin capital letter O with tilde, U+00D5 ISOlat1

        {
            "&Ouml;", "&#214;"
        }, // latin capital letter O with diaeresis, U+00D6 ISOlat1

        {
            "&times;", "&#215;"
        }, // multiplication sign, U+00D7 ISOnum

        {
            "&Oslash;", "&#216;"
        }, // latin capital letter O with stroke = latin capital letter O slash, U+00D8 ISOlat1

        {
            "&Ugrave;", "&#217;"
        }, // latin capital letter U with grave, U+00D9 ISOlat1

        {
            "&Uacute;", "&#218;"
        }, // latin capital letter U with acute, U+00DA ISOlat1

        {
            "&Ucirc;", "&#219;"
        }, // latin capital letter U with circumflex, U+00DB ISOlat1

        {
            "&Uuml;", "&#220;"
        }, // latin capital letter U with diaeresis, U+00DC ISOlat1

        {
            "&Yacute;", "&#221;"
        }, // latin capital letter Y with acute, U+00DD ISOlat1

        {
            "&THORN;", "&#222;"
        }, // latin capital letter THORN, U+00DE ISOlat1

        {
            "&szlig;", "&#223;"
        }, // latin small letter sharp s = ess-zed, U+00DF ISOlat1

        {
            "&agrave;", "&#224;"
        }, // latin small letter a with grave = latin small letter a grave, U+00E0 ISOlat1

        {
            "&aacute;", "&#225;"
        }, // latin small letter a with acute, U+00E1 ISOlat1

        {
            "&acirc;", "&#226;"
        }, // latin small letter a with circumflex, U+00E2 ISOlat1

        {
            "&atilde;", "&#227;"
        }, // latin small letter a with tilde, U+00E3 ISOlat1

        {
            "&auml;", "&#228;"
        }, // latin small letter a with diaeresis, U+00E4 ISOlat1

        {
            "&aring;", "&#229;"
        }, // latin small letter a with ring above = latin small letter a ring, U+00E5 ISOlat1

        {
            "&aelig;", "&#230;"
        }, // latin small letter ae = latin small ligature ae, U+00E6 ISOlat1

        {
            "&ccedil;", "&#231;"
        }, // latin small letter c with cedilla, U+00E7 ISOlat1

        {
            "&egrave;", "&#232;"
        }, // latin small letter e with grave, U+00E8 ISOlat1

        {
            "&eacute;", "&#233;"
        }, // latin small letter e with acute, U+00E9 ISOlat1

        {
            "&ecirc;", "&#234;"
        }, // latin small letter e with circumflex, U+00EA ISOlat1

        {
            "&euml;", "&#235;"
        }, // latin small letter e with diaeresis, U+00EB ISOlat1

        {
            "&igrave;", "&#236;"
        }, // latin small letter i with grave, U+00EC ISOlat1

        {
            "&iacute;", "&#237;"
        }, // latin small letter i with acute, U+00ED ISOlat1

        {
            "&icirc;", "&#238;"
        }, // latin small letter i with circumflex, U+00EE ISOlat1

        {
            "&iuml;", "&#239;"
        }, // latin small letter i with diaeresis, U+00EF ISOlat1

        {
            "&eth;", "&#240;"
        }, // latin small letter eth, U+00F0 ISOlat1

        {
            "&ntilde;", "&#241;"
        }, // latin small letter n with tilde, U+00F1 ISOlat1

        {
            "&ograve;", "&#242;"
        }, // latin small letter o with grave, U+00F2 ISOlat1

        {
            "&oacute;", "&#243;"
        }, // latin small letter o with acute, U+00F3 ISOlat1

        {
            "&ocirc;", "&#244;"
        }, // latin small letter o with circumflex, U+00F4 ISOlat1

        {
            "&otilde;", "&#245;"
        }, // latin small letter o with tilde, U+00F5 ISOlat1

        {
            "&ouml;", "&#246;"
        }, // latin small letter o with diaeresis, U+00F6 ISOlat1

        {
            "&divide;", "&#247;"
        }, // division sign, U+00F7 ISOnum

        {
            "&oslash;", "&#248;"
        }, // latin small letter o with stroke, = latin small letter o slash, U+00F8 ISOlat1

        {
            "&ugrave;", "&#249;"
        }, // latin small letter u with grave,U+00F9 ISOlat1

        {
            "&uacute;", "&#250;"
        }, // latin small letter u with acute, U+00FA ISOlat1

        {
            "&ucirc;", "&#251;"
        }, // latin small letter u with circumflex, U+00FB ISOlat1

        {
            "&uuml;", "&#252;"
        }, // latin small letter u with diaeresis, U+00FC ISOlat1

        {
            "&yacute;", "&#253;"
        }, // latin small letter y with acute, U+00FD ISOlat1

        {
            "&thorn;", "&#254;"
        }, // latin small letter thorn, U+00FE ISOlat1

        {
            "&yuml;", "&#255;"
        }, // latin small letter y with diaeresis, U+00FF ISOlat1

        {
            "&fnof;", "&#402;"
        }, // latin small f with hook = function = florin, U+0192 ISOtech

        /** Greek*/
        {
            "&Alpha;", "&#913;"
        }, // greek capital letter alpha, U+0391

        {
            "&Beta;", "&#914;"
        }, // greek capital letter beta, U+0392

        {
            "&Gamma;", "&#915;"
        }, // greek capital letter gamma, U+0393 ISOgrk3

        {
            "&Delta;", "&#916;"
        }, // greek capital letter delta, U+0394 ISOgrk3

        {
            "&Epsilon;", "&#917;"
        }, // greek capital letter epsilon, U+0395

        {
            "&Zeta;", "&#918;"
        }, // greek capital letter zeta, U+0396

        {
            "&Eta;", "&#919;"
        }, // greek capital letter eta, U+0397

        {
            "&Theta;", "&#920;"
        }, // greek capital letter theta, U+0398 ISOgrk3

        {
            "&Iota;", "&#921;"
        }, // greek capital letter iota, U+0399

        {
            "&Kappa;", "&#922;"
        }, // greek capital letter kappa, U+039A

        {
            "&Lambda;", "&#923;"
        }, // greek capital letter lambda, U+039B ISOgrk3

        {
            "&Mu;", "&#924;"
        }, // greek capital letter mu, U+039C

        {
            "&Nu;", "&#925;"
        }, // greek capital letter nu, U+039D

        {
            "&Xi;", "&#926;"
        }, // greek capital letter xi, U+039E ISOgrk3

        {
            "&Omicron;", "&#927;"
        }, // greek capital letter omicron, U+039F

        {
            "&Pi;", "&#928;"
        }, // greek capital letter pi, U+03A0 ISOgrk3

        {
            "&Rho;", "&#929;"
        }, // greek capital letter rho, U+03A1
        /** there is no Sigmaf, and no U+03A2 character either */
        {
            "&Sigma;", "&#931;"
        }, // greek capital letter sigma, U+03A3 ISOgrk3

        {
            "&Tau;", "&#932;"
        }, // greek capital letter tau, U+03A4

        {
            "&Upsilon;", "&#933;"
        }, // greek capital letter upsilon, U+03A5 ISOgrk3

        {
            "&Phi;", "&#934;"
        }, // greek capital letter phi, U+03A6 ISOgrk3

        {
            "&Chi;", "&#935;"
        }, // greek capital letter chi, U+03A7

        {
            "&Psi;", "&#936;"
        }, // greek capital letter psi, U+03A8 ISOgrk3

        {
            "&Omega;", "&#937;"
        }, // greek capital letter omega, U+03A9 ISOgrk3

        {
            "&alpha;", "&#945;"
        }, // greek small letter alpha, U+03B1 ISOgrk3

        {
            "&beta;", "&#946;"
        }, // greek small letter beta, U+03B2 ISOgrk3

        {
            "&gamma;", "&#947;"
        }, // greek small letter gamma, U+03B3 ISOgrk3

        {
            "&delta;", "&#948;"
        }, // greek small letter delta, U+03B4 ISOgrk3

        {
            "&epsilon;", "&#949;"
        }, // greek small letter epsilon, U+03B5 ISOgrk3

        {
            "&zeta;", "&#950;"
        }, // greek small letter zeta, U+03B6 ISOgrk3

        {
            "&eta;", "&#951;"
        }, // greek small letter eta, U+03B7 ISOgrk3

        {
            "&theta;", "&#952;"
        }, // greek small letter theta, U+03B8 ISOgrk3

        {
            "&iota;", "&#953;"
        }, // greek small letter iota, U+03B9 ISOgrk3

        {
            "&kappa;", "&#954;"
        }, // greek small letter kappa, U+03BA ISOgrk3

        {
            "&lambda;", "&#955;"
        }, // greek small letter lambda, U+03BB ISOgrk3

        {
            "&mu;", "&#956;"
        }, // greek small letter mu, U+03BC ISOgrk3

        {
            "&nu;", "&#957;"
        }, // greek small letter nu, U+03BD ISOgrk3

        {
            "&xi;", "&#958;"
        }, // greek small letter xi, U+03BE ISOgrk3

        {
            "&omicron;", "&#959;"
        }, // greek small letter omicron, U+03BF NEW

        {
            "&pi;", "&#960;"
        }, // greek small letter pi, U+03C0 ISOgrk3

        {
            "&rho;", "&#961;"
        }, // greek small letter rho, U+03C1 ISOgrk3

        {
            "&sigmaf;", "&#962;"
        }, // greek small letter final sigma, U+03C2 ISOgrk3

        {
            "&sigma;", "&#963;"
        }, // greek small letter sigma, U+03C3 ISOgrk3

        {
            "&tau;", "&#964;"
        }, // greek small letter tau, U+03C4 ISOgrk3

        {
            "&upsilon;", "&#965;"
        }, // greek small letter upsilon, U+03C5 ISOgrk3

        {
            "&phi;", "&#966;"
        }, // greek small letter phi, U+03C6 ISOgrk3

        {
            "&chi;", "&#967;"
        }, // greek small letter chi, U+03C7 ISOgrk3

        {
            "&psi;", "&#968;"
        }, // greek small letter psi, U+03C8 ISOgrk3

        {
            "&omega;", "&#969;"
        }, // greek small letter omega, U+03C9 ISOgrk3

        {
            "&thetasym;", "&#977;"
        }, // greek small letter theta symbol, U+03D1 NEW

        {
            "&upsih;", "&#978;"
        }, // greek upsilon with hook symbol, U+03D2 NEW

        {
            "&piv;", "&#982;"
        }, // greek pi symbol, U+03D6 ISOgrk3

        /** General Punctuation */
        {
            "&bull;", "&#8226;"
        }, // bullet = black small circle, U+2022 ISOpub
        /** bullet is NOT the same as bullet operator, U+2219 */
        {
            "&hellip;", "&#8230;"
        }, // horizontal ellipsis = three dot leader, U+2026 ISOpub

        {
            "&prime;", "&#8242;"
        }, // prime = minutes = feet, U+2032 ISOtech

        {
            "&Prime;", "&#8243;"
        }, // double prime = seconds = inches, U+2033 ISOtech

        {
            "&oline;", "&#8254;"
        }, // overline = spacing overscore, U+203E NEW

        {
            "&frasl;", "&#8260;"
        }, // fraction slash, U+2044 NEW

        /** Letterlike Symbols */
        {
            "&weierp;", "&#8472;"
        }, // script capital P = power set = Weierstrass p, U+2118 ISOamso

        {
            "&image;", "&#8465;"
        }, // blackletter capital I = imaginary part, U+2111 ISOamso

        {
            "&real;", "&#8476;"
        }, // blackletter capital R = real part symbol, U+211C ISOamso

        {
            "&trade;", "&#8482;"
        }, // trade mark sign, U+2122 ISOnum

        {
            "&alefsym;", "&#8501;"
        }, // alef symbol = first transfinite cardinal, U+2135 NEW
        /**
         * alef symbol is NOT the same as hebrew letter alef,
         * U+05D0 although the same glyph could be used to depict both characters
         */
        /** Arrows */
        {
            "&larr;", "&#8592;"
        }, // leftwards arrow, U+2190 ISOnum

        {
            "&uarr;", "&#8593;"
        }, // upwards arrow, U+2191 ISOnum-->

        {
            "&rarr;", "&#8594;"
        }, // rightwards arrow, U+2192 ISOnum

        {
            "&darr;", "&#8595;"
        }, // downwards arrow, U+2193 ISOnum

        {
            "&harr;", "&#8596;"
        }, // left right arrow, U+2194 ISOamsa

        {
            "&crarr;", "&#8629;"
        }, // downwards arrow with corner leftwards = carriage return, U+21B5 NEW

        {
            "&lArr;", "&#8656;"
        }, // leftwards double arrow, U+21D0 ISOtech
        /**
         * ISO 10646 does not say that lArr is the same as the 'is implied by' arrow
         * but also does not have any other character for that function. So ? lArr can
         * be used for 'is implied by' as ISOtech suggests
         */
        {
            "&uArr;", "&#8657;"
        }, // upwards double arrow, U+21D1 ISOamsa

        {
            "&rArr;", "&#8658;"
        }, // rightwards double arrow,  U+21D2 ISOtech
        /**
         * ISO 10646 does not say this is the 'implies' character but does not have
         * another character with this function so ?
         * rArr can be used for 'implies' as ISOtech suggests
         */
        {
            "&dArr;", "&#8659;"
        }, // downwards double arrow, U+21D3 ISOamsa

        {
            "&hArr;", "&#8660;"
        }, // left right double arrow, U+21D4 ISOamsa

        /** Mathematical Operators */
        {
            "&forall;", "&#8704;"
        }, // for all, U+2200 ISOtech

        {
            "&part;", "&#8706;"
        }, // partial differential, U+2202 ISOtech

        {
            "&exist;", "&#8707;"
        }, // there exists, U+2203 ISOtech

        {
            "&empty;", "&#8709;"
        }, // empty set = null set = diameter, U+2205 ISOamso

        {
            "&nabla;", "&#8711;"
        }, // nabla = backward difference, U+2207 ISOtech

        {
            "&isin;", "&#8712;"
        }, // element of, U+2208 ISOtech

        {
            "&notin;", "&#8713;"
        }, // not an element of, U+2209 ISOtech

        {
            "&ni;", "&#8715;"
        }, // contains as member, U+220B ISOtech
        /** should there be a more memorable name than 'ni'? */
        {
            "&prod;", "&#8719;"
        }, // n-ary product = product sign, U+220F ISOamsb
        /**
         * prod is NOT the same character as U+03A0 'greek capital letter pi' though
         * the same glyph might be used for both
         */
        {
            "&sum;", "&#8721;"
        }, // n-ary sumation, U+2211 ISOamsb
        /**
         * sum is NOT the same character as U+03A3 'greek capital letter sigma'
         * though the same glyph might be used for both
         */
        {
            "&minus;", "&#8722;"
        }, // minus sign, U+2212 ISOtech

        {
            "&lowast;", "&#8727;"
        }, // asterisk operator, U+2217 ISOtech

        {
            "&radic;", "&#8730;"
        }, // square root = radical sign, U+221A ISOtech

        {
            "&prop;", "&#8733;"
        }, // proportional to, U+221D ISOtech

        {
            "&infin;", "&#8734;"
        }, // infinity, U+221E ISOtech

        {
            "&ang;", "&#8736;"
        }, // angle, U+2220 ISOamso

        {
            "&and;", "&#8743;"
        }, // logical and = wedge, U+2227 ISOtech

        {
            "&or;", "&#8744;"
        }, // logical or = vee, U+2228 ISOtech

        {
            "&cap;", "&#8745;"
        }, // intersection = cap, U+2229 ISOtech

        {
            "&cup;", "&#8746;"
        }, // union = cup, U+222A ISOtech

        {
            "&int;", "&#8747;"
        }, // integral, U+222B ISOtech

        {
            "&there4;", "&#8756;"
        }, // therefore, U+2234 ISOtech

        {
            "&sim;", "&#8764;"
        }, // tilde operator = varies with = similar to, U+223C ISOtech
        /**
         *  tilde operator is NOT the same character as the tilde, U+007E,
         *  although the same glyph might be used to represent both
         */
        {
            "&cong;", "&#8773;"
        }, // approximately equal to, U+2245 ISOtech

        {
            "&asymp;", "&#8776;"
        }, // almost equal to = asymptotic to, U+2248 ISOamsr

        {
            "&ne;", "&#8800;"
        }, // not equal to, U+2260 ISOtech

        {
            "&equiv;", "&#8801;"
        }, // identical to, U+2261 ISOtech

        {
            "&le;", "&#8804;"
        }, // less-than or equal to, U+2264 ISOtech

        {
            "&ge;", "&#8805;"
        }, // greater-than or equal to, U+2265 ISOtech

        {
            "&sub;", "&#8834;"
        }, // subset of, U+2282 ISOtech

        {
            "&sup;", "&#8835;"
        }, // superset of, U+2283 ISOtech
        /**
         * note that nsup, 'not a superset of, U+2283' is not covered by the Symbol
         * font encoding and is not included. Should it be, for symmetry?
         * It is in ISOamsn
         */
        {
            "&nsub;", "&#8836;"
        }, // not a subset of, U+2284 ISOamsn

        {
            "&sube;", "&#8838;"
        }, // subset of or equal to, U+2286 ISOtech

        {
            "&supe;", "&#8839;"
        }, // superset of or equal to, U+2287 ISOtech

        {
            "&oplus;", "&#8853;"
        }, // circled plus = direct sum, U+2295 ISOamsb

        {
            "&otimes;", "&#8855;"
        }, // circled times = vector product, U+2297 ISOamsb

        {
            "&perp;", "&#8869;"
        }, // up tack = orthogonal to = perpendicular, U+22A5 ISOtech

        {
            "&sdot;", "&#8901;"
        }, // dot operator, U+22C5 ISOamsb
        /** dot operator is NOT the same character as U+00B7 middle dot */
        /** Miscellaneous Technical */
        {
            "&lceil;", "&#8968;"
        }, // left ceiling = apl upstile, U+2308 ISOamsc

        {
            "&rceil;", "&#8969;"
        }, // right ceiling, U+2309 ISOamsc

        {
            "&lfloor;", "&#8970;"
        }, // left floor = apl downstile, U+230A ISOamsc

        {
            "&rfloor;", "&#8971;"
        }, // right floor, U+230B ISOamsc

        {
            "&lang;", "&#9001;"
        }, // left-pointing angle bracket = bra, U+2329 ISOtech
        /** lang is NOT the same character as U+003C 'less than'  or U+2039 'single left-pointing angle quotation mark' */
        {
            "&rang;", "&#9002;"
        }, // right-pointing angle bracket = ket, U+232A ISOtech
        /** rang is NOT the same character as U+003E 'greater than' or U+203A 'single right-pointing angle quotation mark' */
        /** Geometric Shapes */
        {
            "&loz;", "&#9674;"
        }, // lozenge, U+25CA ISOpub

        /** Miscellaneous Symbols */
        {
            "&spades;", "&#9824;"
        }, // black spade suit, U+2660 ISOpub
        /** black here seems to mean filled as opposed to hollow */
        {
            "&clubs;", "&#9827;"
        }, // black club suit = shamrock, U+2663 ISOpub

        {
            "&hearts;", "&#9829;"
        }, // black heart suit = valentine, U+2665 ISOpub

        {
            "&diams;", "&#9830;"
        }, // black diamond suit, U+2666 ISOpub

        /**
         * Relevant ISO entity set is given unless names are newly introduced.
         * New names (i.e., not in ISO 8879 list) do not clash with any
         * existing ISO 8879 entity names. ISO 10646 character numbers
         * are given for each character, in hex.;", values are decimal
         * conversions of the ISO 10646 values and refer to the document
         * character set. Names are ISO 10646 names.
         */
        /** C0 Controls and Basic Latin */
        {
            "&quot;", "&#34;"
        }, /* quotation mark = APL quote, U+0022 ISOnum */
        {
            "&amp;", "&#38;"
        }, /* ampersand, U+0026 ISOnum */
        {
            "&lt;", "&#60;"
        }, /* less-than sign, U+003C ISOnum */
        {
            "&gt;", "&#62;"
        }, /* greater-than sign, U+003E ISOnum */
        /** Latin Extended-A */
        {
            "&OElig;", "&#338;"
        }, /* latin capital ligature OE, U+0152 ISOlat2 */
        {
            "&oelig;", "&#339;"
        }, /* latin small ligature oe, U+0153 ISOlat2 */
        /** ligature is a misnomer, this is a separate character in some languages */
        {
            "&Scaron;", "&#352;"
        }, /* latin capital letter S with caron, U+0160 ISOlat2 */
        {
            "&scaron;", "&#353;"
        }, /* latin small letter s with caron, U+0161 ISOlat2 */
        {
            "&Yuml;", "&#376;"
        }, /* latin capital letter Y with diaeresis, U+0178 ISOlat2 */
        /** Spacing Modifier Letters */
        {
            "&circ;", "&#710;"
        }, /* modifier letter circumflex accent, U+02C6 ISOpub */
        {
            "&tilde;", "&#732;"
        }, /* small tilde, U+02DC ISOdia */
        /** General Punctuation */
        {
            "&ensp;", "&#8194;"
        }, /* en space, U+2002 ISOpub */
        {
            "&emsp;", "&#8195;"
        }, /* em space, U+2003 ISOpub */
        {
            "&thinsp;", "&#8201;"
        }, /* thin space, U+2009 ISOpub */
        {
            "&zwnj;", "&#8204;"
        }, /* zero width non-joiner, U+200C NEW RFC 2070 */
        {
            "&zwj;", "&#8205;"
        }, /* zero width joiner, U+200D NEW RFC 2070 */
        {
            "&lrm;", "&#8206;"
        }, /* left-to-right mark, U+200E NEW RFC 2070 */
        {
            "&rlm;", "&#8207;"
        }, /* right-to-left mark, U+200F NEW RFC 2070 */
        {
            "&ndash;", "&#8211;"
        }, /* en dash, U+2013 ISOpub */
        {
            "&mdash;", "&#8212;"
        }, /* em dash, U+2014 ISOpub */
        {
            "&lsquo;", "&#8216;"
        }, /* left single quotation mark, U+2018 ISOnum */
        {
            "&rsquo;", "&#8217;"
        }, /* right single quotation mark, U+2019 ISOnum */
        {
            "&sbquo;", "&#8218;"
        }, /* single low-9 quotation mark, U+201A NEW */
        {
            "&ldquo;", "&#8220;"
        }, /* left double quotation mark, U+201C ISOnum */
        {
            "&rdquo;", "&#8221;"
        }, /* right double quotation mark, U+201D ISOnum */
        {
            "&bdquo;", "&#8222;"
        }, /* double low-9 quotation mark, U+201E NEW */
        {
            "&dagger;", "&#8224;"
        }, /* dagger, U+2020 ISOpub */
        {
            "&Dagger;", "&#8225;"
        }, /* double dagger, U+2021 ISOpub */
        {
            "&permil;", "&#8240;"
        }, /* per mille sign, U+2030 ISOtech */
        {
            "&lsaquo;", "&#8249;"
        }, /* single left-pointing angle quotation mark, U+2039 ISO proposed */
        /** lsaquo is proposed but not yet ISO standardized */
        {
            "&rsaquo;", "&#8250;"
        }, /* single right-pointing angle quotation mark, U+203A ISO proposed */
        /** rsaquo is proposed but not yet ISO standardized */
        {
            "&euro;", "&#8364;"
        }, /* euro sign, U+20AC NEW */

    };
    private static final char[] HEX_CHAR =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    static
    {
        boolean array[] = new boolean[256];

        for (int i = 0; i < array.length; i++)
        {
            array[i] = Character.isWhitespace((char) i);
        }
        ASCII_WHITE_SPACE = array;
    }
    /** lookup table used by Base64ify and deBase64ify */
    private static final byte[] BASE64BYTES;
    private static final byte[] BASE64DECODER = new byte[256];

    public static final String BASE91CIPHER = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"";
    private static final byte[] BASE91BYTES;
    private static final byte[] BASE91DECODER = new byte[256];

    static
    {
        /* prepare the encoding table */
        BASE64BYTES = new byte[]
        {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '='
        };

        /* prepare the decoding table */
        Arrays.fill(BASE64DECODER, (byte) -1);
        for (int i = 0; i < BASE64BYTES.length; i++)
        {
            BASE64DECODER[BASE64BYTES[i]] = (byte) i;
        }

        /* prepare the basE91 encoding table */
        BASE91BYTES = BASE91CIPHER.getBytes(StandardCharsets.UTF_8);

        /* prepare the basE91 decoding table */
        Arrays.fill(BASE91DECODER, (byte) -1);
        for (int i = 0; i < 91; ++i)
        {
            BASE91DECODER[BASE91BYTES[i]] = (byte) i;
        }
        
        NFE=new NumberFormatException( "could not parse");
        NFE.setStackTrace(new StackTraceElement[0]);
        
        HashMap<String, Character> tmpHtmlToCharacter=new HashMap<>();
        
        for( String pair[]: SIMPLIFY_HTML)
        {
            String hValue=pair[1];
            if( hValue.startsWith( "&#"))
            {
                Character c = (char)Integer.parseInt(hValue.substring(2, hValue.length() -1));
                String hName=pair[0];
                
                tmpHtmlToCharacter.put(hName.substring( 1, hName.length() -1), c);              
            }
        }
                     
        HTML_CHARACTER=tmpHtmlToCharacter;
    
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.StringUtilities");//#LOGGER-NOPMD
}
