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
 * Format that applies to an entire paragraph
 * @author desmond
 */
public class ParaFormat implements Comparable
{
    String endTag;
    String startTag;
    String prop;
    ParaFormat( String startTag, String endTag, String prop )
    {
        this.endTag = endTag;
        this.startTag = startTag;
        this.prop = prop;
    }
    @Override
    public int compareTo( Object other )
    {
        return this.startTag.compareTo(((ParaFormat)other).startTag);
    }
}
