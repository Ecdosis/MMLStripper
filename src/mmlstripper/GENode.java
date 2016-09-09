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
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * A (root) node in a global exchange FSM
 * @author desmond
 */
public class GENode extends HashMap<Character,GENode> 
{
    GENode parent;
    String rep;
    GENode( GENode parent, String seq, String rep )
    {
        this.parent = parent;
        build( parent, seq, rep );
    }
    void build( GENode parent, String seq, String rep )
    {
        if ( seq.length()==0 )
        {
            this.rep = rep;
        }
        else if ( this.containsKey(seq.charAt(0)) )
        {
            GENode gen = get(seq.charAt(0));
            gen.build( this, seq.substring(1),rep );
        }
        else
        {
            this.put( seq.charAt(0), new GENode(this,seq.substring(1),rep) );
        }
    }
    GENode match( char token )
    {
        if ( containsKey(token) )
            return get(token);
        else
            return null;
    }
    String backtrack( String path )
    {
        if ( this.parent == null )
            return (path.length()==0)?null:path;
        else
        {
            Set<Character> keys = parent.keySet();
            Iterator<Character> iter = keys.iterator();
            while ( iter.hasNext() )
            {
                Character c = iter.next();
                GENode value = parent.get(c);
                if ( value == this )
                {
                    return parent.backtrack(c+path);
                }
            }
            return "!"+path;
        }
    }
    public static void main( String[] args )
    {
        String[] tests = new String[4];
        tests[0] = "Dr. Wooley's successor -- Dr. Badham.";
        tests[1] = "---or what else he likes.";
        tests[2] = "of Chatterton\"---remembering your brave life. [Keats and Chatterton were";
        tests[3] = "fragment of yours -- the \"Vision";
        GENode gen = new GENode(null,"---","—");
        gen.build(null,"--", "–");
        for ( int i=0;i<tests.length;i++ )
        {
            String line = tests[i];
            GENode current = gen;
            for ( int j=0;j<line.length();j++ )
            {
                char token = line.charAt(j);
                GENode res = current.match(token);
                if ( res == null )
                {
                    if ( current != null )
                    {
                        if ( current.rep != null )
                        {
                            System.out.print(current.rep+token);
                            current = gen;
                        }
                        else 
                        {
                            String bt = current.backtrack("");
                            if ( bt.length()==0 )
                                System.out.print(token);
                            else
                                System.out.print(bt+token);
                        }
                    }
                    else
                        System.out.print(""+token);
                }
                else if ( res.size() == 0 )
                {
                    System.out.print(res.rep);
                    current = gen;
                }
                else
                    current = res;
            }
            System.out.println("");
        }
    }
}
