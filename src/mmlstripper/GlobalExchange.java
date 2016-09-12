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
    /** special 'char' for line-start won't be echoed back */
    public static char LINESTART=0;
    GlobalExchange( String seq, String rep )
    {
        current = root = new GENode(null,seq,rep);
    }
    boolean startsWith( char c )
    {
        return root.containsKey(c);
    }
    void append( String seq, String rep )
    {
        if ( Character.isWhitespace(seq.charAt(0)) 
            && Character.isWhitespace(rep.charAt(0)) )
            root.build(null, LINESTART+seq.substring(1), 
                rep.substring(1));
        // *also* build it with leading space
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
    public static void main(String[] args )
    {
        String[] tests = new String[4];
        tests[0] = "Dr. Wooley's successor -- Dr. Badham.";
        tests[1] = "---or what else he likes.";
        tests[2] = "of Chatterton\"---remembering your brave life. [Keats and Chatterton were";
        tests[3] = "fragment of yours -- the \"Vision";
        //{"seq":"---","rep":"—"},{"seq":"--","rep":"–"},{"seq":" ---","rep":" —"},{"seq":" --","rep":" –"}]}
        GlobalExchange ge = new GlobalExchange("---","—");
        MMLRet ret = new MMLRet(false,null);
        StringBuilder sb = new StringBuilder();
        ge.append("--", "—");
        ge.append(" ---"," —");
        ge.append(" --"," –");
        for ( int i=0;i<4;i++ )
        {
            String line = tests[i];
            if ( ge.startsWith('\000') )
                line = '\000'+line;
            int j=0;    // allow for tokens to be pushed back on input
            while ( j < line.length() || ret.pushed != 0 )
            {
                char token;
                if ( ret.pushed != 0 )
                {
                    token = ret.pushed;
                    ret.pushed = 0;
                }
                else 
                    token = line.charAt(j++);
                ret = ge.add(token,ret);
                if ( ret.result != null && ret.result.length()>0 )
                    sb.append(ret.result);
                else if ( !ret.active && ret.result == null )
                    sb.append( token );
            }
            // clean up at line-end
            String res = ge.flush();
            if ( res != null )
                sb.append( res );
            line = sb.toString();
            sb.setLength(0);
            System.out.println(line);
        }
    }
}
