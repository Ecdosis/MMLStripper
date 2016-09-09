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
 * Manage a global exchange as we parse the text of a line
 * @author desmond
 */
public class GlobalExchange extends MMLObject implements Comparable
{
    GENode root;
    GENode current;
    GlobalExchange( String seq, String rep )
    {
        current = root = new GENode(null,seq,rep);
    }
    void append( String seq, String rep )
    {
        root.build(null,seq,rep);
    }    
    MMLRet add( char token, MMLRet ret )
    {
        GENode res = current.match(token);
        if ( res == null )
        {
            if ( current != null )
            {
                if ( current.rep != null )
                {
                    ret.result = current.rep;
                    ret.pushed = token;
                }
                else 
                {
                    String bt = current.backtrack("");
                    if ( bt == null )
                        ret.result = null;
                    else
                    {
                        ret.result = bt;
                        ret.pushed = token;
                    }
                }
            }
            else
                ret.result = null;
            ret.active = false;
            current = root;
        }
        else if ( res.size() == 0 )
        {
            ret.result = res.rep;
            ret.active = false;
            current = root;
        }
        else
        {
            ret.active = true;
            ret.result = null;
            current = res;
        }
        return ret;
    }
    void reset()
    {
        current = root;
        if ( root.containsKey(' ') )
            current = root.get(' ');
    }
    /**
     * Flush half-finished globals to output
     * @return a string or null
     */
    String flush()
    {
        if ( current == root || current == null )
            return null;
        else if ( current.rep != null )
            return current.rep;
        else
            return current.backtrack("");
    }
    String property()
    {
        return null;
    }
    String key()
    {
        return "";
    }
    @Override
    public int compareTo( Object other )
    {
        return this.key().compareTo(((MMLObject)other).key());
    }
}
