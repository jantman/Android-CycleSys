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
package com.jasonantman.cycletodo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * @author jantman
 * this is from http://www.screaming-penguin.com/node/7749
 */
class BackupHandler extends AsyncTask<Void, Void, Boolean>
{
    private CycleToDo parent;
    private ProgressDialog dialog;
    private static final String TAG = "BackupHandler"; // for debugging
    private String filename;
	
    public BackupHandler(CycleToDo parent)
    {
    	super();
    	this.parent = parent;
    	this.dialog = new ProgressDialog(this.parent);
    	this.filename = Util.genBackupFilename();
    }

    // can use UI thread here
    protected void onPreExecute()
    {
    	this.dialog.setMessage("Exporting database...");
    	this.dialog.show();
    }

    // automatically done on worker thread (separate from UI thread)
    protected Boolean doInBackground(Void... params)
    {
    	if(CycleToDo.DEBUG_ON) { Log.d(TAG, " start doInBackground()"); } // DEBUG - remove me
    	File dbFile = new File(Environment.getDataDirectory() + "/data/com.jasonantman.cycletodo/databases/cycletodo.db");

    	File exportFile = new File(Environment.getExternalStorageDirectory(), this.filename);
    	if(CycleToDo.DEBUG_ON) { Log.d(TAG, " setting export file: " + this.filename); } // DEBUG - remove me

    	try
    	{
    		exportFile.createNewFile();
    		this.copyFile(dbFile, exportFile);
    		if(CycleToDo.DEBUG_ON) { Log.d(TAG, " finished copying file."); } // DEBUG - remove me
    		return true;
    	}
    	catch (IOException e)
    	{
    		if(CycleToDo.DEBUG_ON) { Log.d(TAG, " IOException while copying file: "+e.getMessage()); }
    		return false;
    	}
    }

    // can use UI thread here
    protected void onPostExecute(final Boolean success)
    {
       if (this.dialog.isShowing())
       {
          this.dialog.dismiss();
       }
       
       if (success)
       {
   			AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.parent);
   			dlgAlert.setMessage("Backup Successful. SQLite database backed up to: " + this.filename);
   			dlgAlert.setTitle(R.string.backup_ok_title);
   			dlgAlert.setPositiveButton("OK", null);
   			dlgAlert.setCancelable(true);
   			dlgAlert.create().show();
       }
       else
       {
    	   	AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.parent);
  			dlgAlert.setMessage("Backup Failure. I'm sorry, but there was an error writing the backup file.");
  			dlgAlert.setTitle(R.string.backup_error_title);
  			dlgAlert.setPositiveButton("OK", null);
  			dlgAlert.setCancelable(true);
  			dlgAlert.create().show();
       }
    }

    void copyFile(File src, File dst) throws IOException
    {
       FileChannel inChannel = new FileInputStream(src).getChannel();
       FileChannel outChannel = new FileOutputStream(dst).getChannel();
       try
       {
          inChannel.transferTo(0, inChannel.size(), outChannel);
       }
       finally
       {
          if (inChannel != null)
          {
             inChannel.close();
          }
          if (outChannel != null)
          {
             outChannel.close();
          }
       }
    }

}
