/**
 * +----------------------------------------------------------------------+
 * | CycleSystem      http://CycleSystem.jasonantman.com          |
 * +----------------------------------------------------------------------+
 * | Copyright (c) 2009 Jason Antman <jason@jasonantman.com>.             |
 * |                                                                      |
 * | This program is free software; you can redistribute it and/or modify |
 * | it under the terms of the GNU General Public License as published by |
 * | the Free Software Foundation; either version 3 of the License, or    |
 * | (at your option) any later version.                                  |
 * |                                                                      |
 * | This program is distributed in the hope that it will be useful,      |
 * | but WITHOUT ANY WARRANTY; without even the implied warranty of       |
 * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        |
 * | GNU General Public License for more details.                         |
 * |                                                                      |
 * | You should have received a copy of the GNU General Public License    |
 * | along with this program; if not, write to:                           |
 * |                                                                      |
 * | Free Software Foundation, Inc.                                       |
 * | 59 Temple Place - Suite 330                                          |
 * | Boston, MA 02111-1307, USA.                                          |
 * +----------------------------------------------------------------------+
 * |Please use the above URL for bug reports and feature/support requests.|
 * +----------------------------------------------------------------------+
 * | Authors: Jason Antman <jason@jasonantman.com>                        |
 * +----------------------------------------------------------------------+
 * | $LastChangedRevision::                                           $ |
 * | $HeadURL::                                                       $ |
 * +----------------------------------------------------------------------+
 * @author Jason Antman <jason@jasonantman.com>
 */
package com.jasonantman.cyclesystem;

import android.text.format.Time;

/**
 * General utility functions for CycleSystem
 * @param ts Long timestamp
 * @return Integer[3] like [year, month, date]
 * @author jantman
 *
 */
public final class Util {

	public static Integer[] tsLongToYMD(Long ts)
	{
		Time t = new Time();
		t.set(ts);
		Integer[] a = new Integer[3];
		a[0] = t.year;
		a[1] = t.month;
		a[2] = t.monthDay;
		return a;
	}

	public static int YMDtoTSint(int year, int month, int date)
	{
		Time t = new Time();
		t.set(date, month, year);
		int foo = (int) (t.toMillis(false) / 1000);
		return foo;
	}
	
}
