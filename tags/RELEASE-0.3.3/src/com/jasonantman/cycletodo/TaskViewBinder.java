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

import com.jasonantman.cycletodo.R;

import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.graphics.Paint;

/**
 * Lets us change the image and/or background color for the task views
 * @author jantman
 *
 */
public class TaskViewBinder implements SimpleCursorAdapter.ViewBinder {
   
    public static final String TAG = "TaskViewBinder";
    
    public static final int COLOR_1 = Color.argb(60, 255, 0, 0); 
    public static final int COLOR_2 = Color.argb(60, 216, 216, 86);
    public static final int COLOR_3 = Color.argb(60, 0, 255, 0);
    public static final int COLOR_9 = Color.argb(60, 127, 127, 127);
    public static final int ICON_1 = R.drawable.red;
    public static final int ICON_2 = R.drawable.yellow;
    public static final int ICON_3 = R.drawable.green;
    public static final int ICON_9 = R.drawable.gray;
    
	/* (non-Javadoc)
	 * @see android.widget.SimpleCursorAdapter.ViewBinder#setViewValue(android.view.View, android.database.Cursor, int)
	 */
    //@Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
    	int priority = cursor.getInt(cursor.getColumnIndex(TaskList.Tasks.PRIORITY));
      
    	// set the icon
    	int nImageIndex = cursor.getColumnIndex(TaskList.Tasks.PRIORITY);
    	if(nImageIndex==columnIndex)
    	{
    		ImageView typeControl = (ImageView)view;
            switch (priority) {
        		case 1:  typeControl.setImageResource(ICON_1); break;
        		case 2:  typeControl.setImageResource(ICON_2); break;
        		case 3:  typeControl.setImageResource(ICON_3); break;
        		case 9:  typeControl.setImageResource(ICON_9); break;
        		default:  typeControl.setImageResource(ICON_9); break;
            }
            
    		return true;
    	}

 	
    	// set the time string
    	int nTimeIndex = cursor.getColumnIndex(TaskList.Tasks.TIME_MIN);
    	if(nTimeIndex==columnIndex)
    	{
    		int TimeMins = cursor.getInt(nTimeIndex);
    		TextView timeView = (TextView)view;
    		CharSequence foo = "";
    		if(TimeMins < 60){ foo = Integer.toString(TimeMins); }
    		else{ foo = Integer.toString(TimeMins / 60) +"h " + Integer.toString(TimeMins % 60) + "m";}
    		timeView.setText(foo);
    		return true;
    	}
    	
    	// set category string
    	int nCatIndex = cursor.getColumnIndex(TaskList.Tasks.CATEGORY_ID);
    	if(nCatIndex==columnIndex)
    	{
    		int Cat = cursor.getInt(nCatIndex);
    		TextView catView = (TextView)view;
    		catView.setText("(" + TaskList.CATEGORIES[Cat-1] + ")");
    		return true;
    	}
    	
    	// set the text itself
    	int nTitleIndex = cursor.getColumnIndex(TaskList.Tasks.TITLE);
    	if(nTitleIndex==columnIndex)
    	{
    		int is_finished = cursor.getInt(cursor.getColumnIndex(TaskList.Tasks.IS_FINISHED));
    		String title = cursor.getString(nTitleIndex);
    		TextView titleView = (TextView)view;
    		titleView.setText(title);
    		// got the strikethrough stuff from: http://jsharkey.org/blog/2008/09/15/crossing-things-off-lists-in-android-09-sdk/
    		if(is_finished == 1)
    		{
    			titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    		}
    		return true;
    	}
    	
      return false;
    } 

}
