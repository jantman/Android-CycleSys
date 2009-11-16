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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author jantman
 *
 */
public class Help extends Activity {
	private static final String TAG = "Help";
	
	TextView t;
	
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.help);
        
        // text edit view for title
        //titleEdit = (EditText) findViewById(R.id.title);
        t = (TextView) findViewById(R.id.TextView02);
        CharSequence text = "Copyright 2009 Jason Antman. Licensed under GNU GPLv3.\n\n";
        text = text + "Thanks to Tom Limoncelli for writing the book that sparked all of this - Time Management for System Administrators.\n\n";
        text = text + "For help, to report bugs, or to get the source: http://cyclesys.jasonantman.com\n\n";
        text = text + "SVN revision: " + TouchMe.getSvnRev() + "\n";
        text = text + "Version Code: " + getVersionCode() + "\n";
        text = text + "Version Name: " + getVersionName() + "\n";
        t.setText(text);
    }
    
	private String getVersionCode()
	{
        PackageManager pm = getPackageManager();
        try {
            //---get the package info---
            PackageInfo pi =  pm.getPackageInfo("com.jasonantman.cyclesystem", 0);
            //---display the versioncode---        
            return Integer.toString(pi.versionCode);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            return "Unknown (program error)";
        }

	}
	
	private String getVersionName()
	{
        PackageManager pm = getPackageManager();
        try {
            //---get the package info---
            PackageInfo pi =  pm.getPackageInfo("com.jasonantman.cyclesystem", 0);
            //---display the versioncode---        
            return pi.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            return "Unknown (program error)";
        }

	}
	
}
