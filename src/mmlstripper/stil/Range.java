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
import org.json.simple.JSONObject;
/**
 * Simple STIL range representation
 * @author desmond
 */
public class Range extends JSONObject
{
    public Range( String name )
    {
        this.put("name", name);
    }
    public void setOffset( int offset )
    {
        this.put("offset", offset);
    }
    public void setLen( int len )
    {
        this.put("len",len);
    }
    public int getLen()
    {
        if ( this.containsKey("len") )
            return ((Number)this.get("len")).intValue();
        else
            return 0;
    }
    public int getOffset()
    {
        if ( this.containsKey("offset") )
            return ((Number)this.get("offset")).intValue();
        else
            return 0;
    }
    /**
     * One-off replacement of offset for relative offset
     * @param reloff the reloff value
     */
    public void setRelOffset( int reloff )
    {
        remove( "offset");
        put("reloff",reloff);
    }
    public String getName()
    {
        return (String)this.get("name");
    }
    public boolean isPage()
    {
        String name = (String)get("name");
        return name.equals("page");
    }
}
