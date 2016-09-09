/*
 * This file is part of MMLStripper.
 *
 *  MMLStripper is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  MMLStripper is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MMLStripper.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2016
 */

package mmlstripper;

/**
 * Match a character format over the length of text it applies to
 * @author desmond
 */
public class CharFormat extends MMLObject implements Comparable
{
    String prop;
    String endTag;
    String startTag;
    int matchPos;
    MatchState state;
    CharFormat( String startTag, String endTag, String prop )
    {
        this.endTag = endTag;
        this.startTag = startTag;
        this.prop = prop;
        this.state = MatchState.lookingForStartTag;
    }
    /**
     * Add a token and return the result of the parse
     * @param token the current character token
     * @param ret the resurn value to set
     * @return the character to add or null (ignore) or empty string (add prop)
     */
    MMLRet add( char token, MMLRet ret )
    {
        switch ( state )
        {
            case lookingForStartTag:
                if ( startTag.length() > 0 && token == startTag.charAt(0) )
                {
                     if ( startTag.length()==1 )
                     {
                         state = MatchState.lookingForEndTag;
                         ret.result = "["+prop+"]";   // signal range start
                         ret.active = true;
                     }
                     else if ( startTag.length()>1 )
                     {
                         state = MatchState.matchingStartTag;
                         matchPos = 1;
                         ret.active = true;
                         ret.result = null;
                     }
                }
                else
                {
                    ret.active = false;
                    ret.result = null;
                }
                break;
            case matchingStartTag:
                if ( startTag.length()-matchPos > 0 
                    && token == startTag.charAt(matchPos) )
                {
                    matchPos++;
                    if ( matchPos == startTag.length() )
                    {
                        state = MatchState.lookingForEndTag;
                        matchPos = 0;
                        ret.result = "["+prop+"]";
                        ret.active = true;
                    }
                    else
                    {
                        ret.result = null;
                        ret.active = true;
                    }
                }
                else    // mismatch
                {
                    ret.result = this.startTag.substring(0,matchPos);
                    ret.active = false;
                    reset();
                }
                break;
            case lookingForEndTag:
                if ( endTag.length() > 0 
                    && endTag.charAt(0)==token )
                {
                    if ( endTag.length()==1 )
                    {
                        reset();
                        ret.result = "";
                        ret.active = false;
                    }
                    else
                    {
                        state = MatchState.matchingEndTag;
                        matchPos++;
                    }
                }
                else
                {
                    ret.result = ""+token;
                    ret.active = true;
                }
                break;
            case matchingEndTag:
                if ( endTag.length()-matchPos > 0 
                    && token == endTag.charAt(matchPos) )
                {
                    matchPos++;
                    if ( matchPos == endTag.length() )
                    {
                        reset();
                        ret.result = "";  // signal end-range
                        ret.active = false;
                    }
                    else
                    {
                        ret.result = null;
                        ret.active = true;
                    }
                }
                else
                {
                    ret.result = this.endTag.substring(0,matchPos);
                    ret.active = false;
                    reset();
                }
                break;
        }
        return ret; 
    }
    void reset()
    {
        state = MatchState.lookingForStartTag;
        matchPos = 0;
    }
    String property()
    {
        return prop;
    }
    String key()
    {
        return startTag;
    }
    @Override
    public int compareTo( Object other )
    {
        return this.key().compareTo(((MMLObject)other).key());
    }
}
