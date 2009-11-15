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

import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

/**
 * Lets us change the image and/or background color for the task views
 * @author jantman
 *
 */
public class TaskViewBinder implements SimpleCursorAdapter.ViewBinder {

    // controls debugging-level output
    public static final boolean DEBUG_ON = true;
    
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
    	
    	// set the background color
    	// @TODO - TODO - need to set color of whole line, not just parts
    	/*
        switch (priority) {
        	case 1:  view.setBackgroundColor(COLOR_1); break;
        	case 2:  view.setBackgroundColor(COLOR_2); break;
        	case 3:  view.setBackgroundColor(COLOR_3); break;
        	case 9:  view.setBackgroundColor(COLOR_9); break;
        	default:  view.setBackgroundColor(COLOR_9); break;
        }
        */
    	
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

      return false;
    } 

}
