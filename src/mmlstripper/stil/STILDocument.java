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
package mmlstripper.stil;
import org.json.simple.*;
/**
 * A simple stil document representation
 * @author desmond
 */
public class STILDocument extends JSONObject
{
    String style;
    JSONArray ranges;
    public STILDocument( String style )
    {
        this.put("style", style);
        this.ranges = new JSONArray();
        put("ranges",ranges);
    }
    public void add( Range r )
    {
        this.ranges.add( r );
    }
    public String toJSON()
    {
        // relative offset
        int current = 0;
        for ( int i=0;i<ranges.size();i++ )
        {
            Range r = (Range)ranges.get(i);
            int oldOffset = r.getOffset();
            r.setRelOffset(oldOffset-current);
            current = oldOffset;            
        }
        return this.toJSONString();
    }
    public void remove( Range r )
    {
        this.ranges.remove(r);
    }
    public boolean isEmpty()
    {
        return this.ranges.size()==0;
    }
}
