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
package com.aspc.remote.javaspell;

import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 * @author Tim Stegeman
 */
public class SpellCheckResult {

    private final String word;
    private final int wordIndex;
    private final int startIndex;
    private final List<String> suggestions;

    public SpellCheckResult(final String word, final int wordIndex, final int startIndex, final List<String> suggestions) {
        this.word = word;
        this.wordIndex = wordIndex;
        this.startIndex = startIndex;
        this.suggestions = suggestions;
    }

    public String getWord() {
        return word;
    }

    public int getWordIndex() {
        return wordIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override @CheckReturnValue @Nonnull
    public String toString() {
        return "Word: " + word + ", Index: " + wordIndex + ", Start: " + startIndex + ", Suggestions: " + suggestions.toString();
    }
}
