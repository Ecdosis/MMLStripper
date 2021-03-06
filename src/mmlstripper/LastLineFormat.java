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

import mmlstripper.stil.Range;

/**
 * Wrapper to remember the last property applied to the line
 * @author desmond
 */
public class LastLineFormat {
    Range range;
    LastLineFormat( Range r )
    {
        this.range = r;
    }
    public void addLen( int len )
    {
        int oldLen = range.getLen();
        this.range.put("len",oldLen+len);
    }
}
