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
		
		Integer tempI = new Integer(0);
		Time tempT = new Time();
		int tempDayOfWeek = 0;
			
		if(CycleToDo.DEBUG_ON){ Log.d(TAG, " before loop, ts= " + Integer.toString(ts) + " " + t.format2445()); } // DEBUG - remove me
		
		for(int i = ts + 86400; i <= ts + 604800; i+= 86400)
		{
			tempI = i;
			tempT.set(tempI.longValue() * 1000);
			tempDayOfWeek = tempT.weekDay;
			if(CycleToDo.DEBUG_ON){ Log.d(TAG, " LOOP ts= " + Integer.toString(ts) + " " + t.format2445()); } // DEBUG - remove me
			if(isWorkDay(tempDayOfWeek))
			{
				return i;
			}
		}

		return ts;
	}
	
	/**
	 * True if the weekDay (Time.weekDay, 0-6, 0=Sunday) is a work day, false otherwise
	 * @param weekDay
	 * @return boolean
	 */
	public static boolean isWorkDay(int weekDay)
	{
		if(CycleToDo.workday_values[weekDay] == true){ return true;}
		return false;
	}
   
    /**
     * Generate a filename for the SQLite backup file
     * @return String
     */
    protected static String genBackupFilename()
    {
    	Time t = new Time();
    	t.setToNow();
    	String s = "CycleToDo_" + Integer.toString(t.year) + "-" + Integer.toString(t.month) + "-" + Integer.toString(t.monthDay) + "_";
    	s = s + Integer.toString(t.hour) + "-" + Integer.toString(t.minute) + "-" + Integer.toString(t.second) + ".sqlite"; 
    	return s;
    }
    
    /**
     * True or false whether we have external storage available.
     * @return boolean
     */
    protected static boolean haveExternStorage()
    {
    	      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Convert the boolean array for work days to an int to store in settings.
     * @param foo boolean[]
     * @return int
     */
    protected static int workDaysToInt(boolean[] foo)
    {
    	int bar = 0;
    	if(foo[0] == true){ bar = bar | 1;} 
    	if(foo[1] == true){ bar = bar | 2;}
    	if(foo[2] == true){ bar = bar | 4;}
    	if(foo[3] == true){ bar = bar | 8;}
    	if(foo[4] == true){ bar = bar | 16;}
    	if(foo[5] == true){ bar = bar | 32;}
    	if(foo[6] == true){ bar = bar | 64;}
    	/*
    	 * 00000001 = 1 = Sunday
    	 * 00000010 = 2 = Monday
    	 * 00000100 = 4 = Tuesday
    	 * 00001000 = 8 = Wednesday
    	 * 00010000 = 16 = Thursday
    	 * 00100000 = 32 = Friday
    	 * 01000000 = 64 = Saturday
    	 */
    	
    	// build in some error-handling here
    	if(bar == 0){ bar = 62;} // if someone checked NO work days, default to M-F
    	return bar;
    }
    
    /**
     * Convery the int for work days from settings back into a boolean array.
     * @param foo int
     * @return boolean[7]
     */
    protected static boolean[] workDaysFromInt(int foo)
    {
    	boolean[] bar = new boolean[7];
    	if((foo & 1) == 1){ bar[0] = true;}
    	if((foo & 2) == 2){ bar[1] = true;}
    	if((foo & 4) == 4){ bar[2] = true;}
    	if((foo & 8) == 8){ bar[3] = true;}
    	if((foo & 16) == 16){ bar[4] = true;}
    	if((foo & 32) == 32){ bar[5] = true;}
    	if((foo & 64) == 64){ bar[6] = true;}
    	return bar;
    }
	
}
