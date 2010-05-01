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
class RestoreHandler extends AsyncTask<String, Void, Boolean>
{
    private CycleToDo parent;
    private ProgressDialog dialog;
    private static final String TAG = "RestoreHandler"; // for debugging
    private String filename;
	
    public RestoreHandler(CycleToDo parent)
    {
    	super();
    	this.parent = parent;
    	this.dialog = new ProgressDialog(this.parent);
    }

    // can use UI thread here
    protected void onPreExecute()
    {
    	this.dialog.setMessage("Restoring database...");
    	this.dialog.show();
    }

    // automatically done on worker thread (separate from UI thread)
    protected Boolean doInBackground(String... params)
    {
    	int count = params.length;
    	// we shouldnt really need a loop here should we?
    	for (int i = 0; i < count; i++) 
    	{
    		File dbFile = new File(Environment.getDataDirectory() + "/data/com.jasonantman.cycletodo/databases/cycletodo.db");
    		File restoreFile = new File(Environment.getExternalStorageDirectory(), params[i]);
        
    		try
    		{
    			this.copyFile(restoreFile, dbFile);
    			if(CycleToDo.DEBUG_ON) { Log.d(TAG, " finished copying file."); } // DEBUG - remove me
    			return true;
    		}
    		catch (IOException e)
    		{
    			if(CycleToDo.DEBUG_ON) { Log.d(TAG, " IOException while copying file: "+e.getMessage()); }
    			return false;
    		}
    	}
    	return false;
    }

    /*
     * TODO - @TODO - 2010-04-30 test died here
E/CycleToDo(  510): RestoreFileButtonHandler OK, selection=3
D/AndroidRuntime(  510): Shutting down VM
W/dalvikvm(  510): threadid=3: thread exiting with uncaught exception (group=0x4001b188)
E/AndroidRuntime(  510): Uncaught handler: thread main exiting due to uncaught exception
E/AndroidRuntime(  510): java.lang.ArrayIndexOutOfBoundsException
E/AndroidRuntime(  510):        at com.jasonantman.cycletodo.CycleToDo$RestoreFileButtonHandler.onClick(CycleToDo.java:818)
E/AndroidRuntime(  510):        at com.android.internal.app.AlertController$ButtonHandler.handleMessage(AlertController.java:158)
E/AndroidRuntime(  510):        at android.os.Handler.dispatchMessage(Handler.java:99)
E/AndroidRuntime(  510):        at android.os.Looper.loop(Looper.java:123)
E/AndroidRuntime(  510):        at android.app.ActivityThread.main(ActivityThread.java:4310)
E/AndroidRuntime(  510):        at java.lang.reflect.Method.invokeNative(Native Method)
E/AndroidRuntime(  510):        at java.lang.reflect.Method.invoke(Method.java:521)
E/AndroidRuntime(  510):        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:860)
E/AndroidRuntime(  510):        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:618)
E/AndroidRuntime(  510):        at dalvik.system.NativeStart.main(Native Method)
I/Process (   57): Sending signal. PID: 510 SIG: 3
I/dalvikvm(  510): threadid=7: reacting to signal 3
I/dalvikvm(  510): Wrote stack trace to '/data/anr/traces.txt'
I/Process (  510): Sending signal. PID: 510 SIG: 9
I/ActivityManager(   57): Process com.jasonantman.cycletodo (pid 510) has died.
I/WindowManager(   57): WIN DEATH: Window{44caa798 com.jasonantman.cycletodo/com.jasonantman.cycletodo.CycleToDo paused=false}
I/WindowManager(   57): WIN DEATH: Window{44c7a410 com.jasonantman.cycletodo/com.jasonantman.cycletodo.CycleToDo paused=false}

     * 
     */
    
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
   			dlgAlert.setMessage("Restore Successful.");
   			dlgAlert.setTitle(R.string.backup_ok_title);
   			dlgAlert.setPositiveButton("OK", null);
   			dlgAlert.setCancelable(true);
   			dlgAlert.create().show();
       }
       else
       {
    	   	AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.parent);
  			dlgAlert.setMessage("Restore Failure. I'm sorry, but there was an error restoring the backup file.");
  			dlgAlert.setTitle(R.string.backup_error_title);
  			dlgAlert.setPositiveButton("OK", null);
  			dlgAlert.setCancelable(true);
  			dlgAlert.create().show();
       }
    }

    private void copyFile(File src, File dst) throws IOException
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
