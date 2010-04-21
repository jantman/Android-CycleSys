/**
 * +----------------------------------------------------------------------+
 * | CycleToDo        http://CycleToDo.jasonantman.com                    |
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
 * | $LastChangedRevision::                                             $ |
 * | $HeadURL::                                                         $ |
 * +----------------------------------------------------------------------+
 * @author Jason Antman <jason@jasonantman.com>
 */
package com.jasonantman.cycletodo;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

/**
 * General utility functions for CycleToDo
 * @param ts Long timestamp
 * @return Integer[3] like [year, month, date]
 * @author jantman
 *
 */
public final class Util {
	
	public static final String TAG = "Util";
	
	/**
	 * turn a timestamp (millis) into an Integer[] array like (year, month, date)
	 * @param ts
	 * @return array
	 */
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

	/**
	 * turn a set of Y M D ints into a timestamp (non-millis)
	 * @param year
	 * @param month
	 * @param date
	 * @return
	 */
	public static int YMDtoTSint(int year, int month, int date)
	{
		Time t = new Time();
		t.set(date, month, year);
		int foo = (int) (t.toMillis(false) / 1000);
		return foo;
	}
	
	/**
	 * get the start timestamp of a day (non-millis) from a timestamp (non-millis)
	 * @param ts
	 * @return int timestamp
	 */
	public static int getDayStart(int ts)
	{
		Integer bar = ts;
		
		Integer[] foo = tsLongToYMD(bar.longValue() * 1000);
		
		Time baz = new Time();
		baz.set(foo[2], foo[1], foo[0]);
		int quux = (int) (baz.toMillis(false) / 1000);
		
		return quux;
	}
	
	/**
	 * Return the timestamp (int) for the start of the next work day
	 * @param ts
	 * @return
	 */
	public static int findNextWorkDay(int ts)
	{
		Integer bar = ts;
		Time t = new Time();
		t.set(bar.longValue() * 1000);
		
		int dayOfWeek = t.weekDay; // 0-6, 0 is sunday
		
		// we have a ts and the day of the week. find the next work day.
		if(dayOfWeek < CycleToDo.firstWorkDay)
		{
			// before the start of the work week. move to next firstWorkDay
			return (ts + ( (CycleToDo.firstWorkDay - dayOfWeek) * 86400));
		}
		else if(dayOfWeek >= CycleToDo.lastWorkDay)
		{
			// after the end of the work week. move to next firstWorkDay
			int foo = 6 - dayOfWeek;
			foo = foo + CycleToDo.firstWorkDay + 1;
			return (ts + ( foo * 86400) );
		}
		else if(dayOfWeek >= CycleToDo.firstWorkDay && dayOfWeek < CycleToDo.lastWorkDay)
		{
			// next day is a work day, just move to next day
			return (ts + 86400);
		}
		else
		{
			if(CycleToDo.DEBUG_ON) { Log.d(TAG, "findNextWorkDay() UNHANDLED CASE ts=" + Integer.toString(ts) + " dayOfWeek=" + Integer.toString(dayOfWeek)); }
		}
		
		return ts;
	}
	
   
    /**
     * Generate a filename for the SQLite backup file
     */
    protected static String genBackupFilename()
    {
    	Time t = new Time();
    	t.setToNow();
    	String s = "CycleToDo_" + Integer.toString(t.year) + "-" + Integer.toString(t.month) + "-" + Integer.toString(t.monthDay) + "_";
    	s = s + Integer.toString(t.hour) + "-" + Integer.toString(t.minute) + "-" + Integer.toString(t.second) + ".sqlite"; 
    	return s;
    }
    
    protected static boolean haveExternStorage()
    {
    	      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

	
}
