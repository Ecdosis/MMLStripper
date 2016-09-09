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
 * Handle parsing of smart quotes
 * @author desmond
 */
public class SmartQuotes extends MMLObject implements Comparable
{
    int state;
    public SmartQuotes()
    {
        state = 1;
    }
    void reset()
    {
        state = 1;
    }
    MMLRet add( char token, MMLRet ret )
    {
        switch ( state )
        {
            case 0: // looking for leading whitespace
                if ( Character.isWhitespace(token) )
                {
                    state = 1;
                    ret.result = ""+token;
                    ret.active = true;
                }
                else if ( token == 39 ) // single quote
                {
                    ret.result = "’";
                    ret.active = false;
                }
                else if ( token == '"' )
                {
                    ret.result = "”";
                    ret.active = false;
                }
                else
                {
                    ret.result = null;
                    ret.active = false;
                }
                break;
            case 1: // seen whitespace
                if ( token == 39 ) // single quote
                {
                    ret.result = "‘";
                    ret.active = false;
                }
                else if ( token == '"' )
                {
                    ret.result = "“";
                    ret.active = false;
                }
                else if ( !Character.isWhitespace(token) )
                {
                    state = 0;
                    ret.result = null;
                    ret.active = false;
                }
                else
                {
                    ret.result = null;
                    ret.active = false;
                }
                break;
        }
        return ret;
    }
    String property()
    {
        return null;
    }
    String key()
    {
        return "";
    }
    public int compareTo( Object other )
    {
        return this.key().compareTo(((MMLObject)other).key());
    }
}
