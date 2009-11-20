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
import com.jasonantman.cycletodo.TaskList.Tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;


/**
 * @author jantman
 * An Action to view a task {@link Intent#ACTION_VIEW}, view and edit a task
 * {@link Intent#ACTION_EDIT}, or create a new task {@link Intent#ACTION_INSERT}.  
 */
public class TaskEditor extends Activity {
    private static final String TAG = "TaskEditor";

    /**
     * Standard projection for the interesting columns of a task.
     */
    private static final String[] PROJECTION = new String[] {
            Tasks._ID, // 0
            Tasks.TITLE, // 1
            Tasks.CATEGORY_ID, // 2
            Tasks.DISPLAY_TS, // 3
            Tasks.PRIORITY, // 4
            Tasks.TIME_MIN, //5
    };
    
    /** The index of the task column */
    private static final int COLUMN_INDEX_TASK = 1;
    private static final int COLUMN_INDEX_CATEGORY = 2;
    private static final int COLUMN_INDEX_DISPLAY_TS = 3;
    private static final int COLUMN_INDEX_PRIORITY = 4;
    private static final int COLUMN_INDEX_TIME_MIN = 5;
    
    // anything past this number is treated as the last priority option
    public static final int MAX_PRIORITY = 3;
    
    // This is our state data that is stored when freezing.
    private static final String ORIGINAL_CONTENT = "origContent";

    // Identifiers for our menu items.
    private static final int REVERT_ID = Menu.FIRST;
    private static final int DISCARD_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private String myOriginalContent;

    // title text area
    private EditText titleEdit;
    // priority spinner
    private Spinner prioritySpinner;
    // category spinner
    private Spinner categorySpinner;
    private ArrayAdapter<CharSequence> categorySpinnerAdapter;
    // time in minutes (timeMins)
    private EditText timeMins;
    // date selector
    private DatePicker datePick;
    
    // move buttons
    private Button button_moveToday;
    private Button button_moveWork;
    
    /**
     * Find the id of a category by name
     * @TODO - TODO - re-code this in a logical way
     * @param name name of the category
     * @return Integer
     */
    
    /*
    private Integer getCategoryIdByName(String name) {
    	for(Integer i = 0; i < TaskList.CATEGORIES.length; i++)
    	{
    		if(name.equals(TaskList.CATEGORIES[i])){ return i;}
    	}
    	return 1; // DEFAULT CASE
    }
    */
    
    //@Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        // Do some setup based on the action being performed.
        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new task into " + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.task_editor);
        
        // text edit view for title
        titleEdit = (EditText) findViewById(R.id.title);

        // spinner for priority
        // @TODO - TODO - for now, this is hard-coded in the XML file.
        prioritySpinner = (Spinner) findViewById(R.id.priority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);
        
        // spinner for category
        // @TODO - TODO - this is just thrown in here for now.
        //   we should really have preferences for this stuff
        //   or even better pull from the database
        categorySpinner = (Spinner)findViewById(R.id.category);
        categorySpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        for(int i = 0; i < TaskList.CATEGORIES.length; i++)
        {
        	categorySpinnerAdapter.add(TaskList.CATEGORIES[i]);
        }
        
        // text edit for time in minutes
        timeMins = (EditText) findViewById(R.id.timeMins);

        // date picker (date)
        datePick = (DatePicker) findViewById(R.id.date);
        Time t = new Time();
        t.setToNow();
        datePick.updateDate(t.year, t.month, t.monthDay);
        
        // move to next day button
        button_moveToday = (Button) findViewById(R.id.moveOne);
        button_moveToday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	moveToday();
            }
        });

        // move to next work day button
        button_moveWork = (Button) findViewById(R.id.moveNextWork);
        button_moveWork.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	moveNextWorkDay();
            }
        });
        
        // Get the task!
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);

        // If an instance of this activity had previously stopped, we can
        // get the original text it started with.
        if (savedInstanceState != null) {
            myOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null) {
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();

            // Modify our overall title depending on the mode we are running in.
            if (mState == STATE_EDIT) {
                setTitle(getText(R.string.title_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_add));
            }

            // This is a little tricky: we may be resumed after previously being
            // paused/stopped.  We want to put the new text in the text view,
            // but leave the user where they were (retain the cursor position
            // etc).  This version of setText does that for us.
            String taskTitle = mCursor.getString(COLUMN_INDEX_TASK);
            titleEdit.setTextKeepState(taskTitle);
            
            Integer priority = mCursor.getInt(COLUMN_INDEX_PRIORITY);
            if(priority > MAX_PRIORITY){ prioritySpinner.setSelection(prioritySpinner.getCount()-1);}
            else { prioritySpinner.setSelection(priority-1);}
            
            Integer category = mCursor.getInt(COLUMN_INDEX_CATEGORY);
            categorySpinner.setSelection(category - 1);
            
            Integer estTime = mCursor.getInt(COLUMN_INDEX_TIME_MIN);
            timeMins.setTextKeepState(estTime.toString());
            
            Long myDateTS = mCursor.getLong(COLUMN_INDEX_DISPLAY_TS) * 1000; // need to cast to millis
            Integer[] myDate = Util.tsLongToYMD(myDateTS);
            datePick.updateDate(myDate[0], myDate[1], myDate[2]);
            
            // If we hadn't previously retrieved the original text, do so
            // now.  This allows the user to revert their changes.
            if (myOriginalContent == null) {
                myOriginalContent = taskTitle;
            }

        } else {
        	if(CycleSystem.DEBUG_ON) { Log.d(TAG, "onResume, mCursor IS NULL"); }
            setTitle(getText(R.string.error_title));
            titleEdit.setText(getText(R.string.error_message));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, myOriginalContent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider.  We don't need
        // to do this if only editing.
        if (mCursor != null) {
            String title = titleEdit.getText().toString();
            
            int priority = prioritySpinner.getSelectedItemPosition();
            if(priority == (prioritySpinner.getCount()-1)){ priority = MAX_PRIORITY+1;}
            else { priority = priority + 1;}

            int category = categorySpinner.getSelectedItemPosition() + 1;
            
            String estTime = timeMins.getText().toString();
            
            int display_ts = Util.YMDtoTSint(datePick.getYear(), datePick.getMonth(), datePick.getDayOfMonth());


            // If this activity is finished, and there is no text, then we
            // do something a little special: simply delete the task entry.
            // Note that we do this both for editing and inserting...  it
            // would be reasonable to only do it when inserting.
            if (isFinishing() && title.trim().equalsIgnoreCase("")) {
            	if(CycleSystem.DEBUG_ON) { Log.d(TAG, "onPause - isFinishing()"); }
                setResult(RESULT_CANCELED);
                deleteTask();

            // Get out updates into the provider.
            } else {
            	if(CycleSystem.DEBUG_ON) { Log.d(TAG, "onPause - NOT isFinishing()"); }
                ContentValues values = new ContentValues();
                    
                // Bump the modification time to now.
                values.put(Tasks.MODIFIED_TS, ((int) System.currentTimeMillis() / 1000));
                values.put(Tasks.TITLE, title);
                values.put(Tasks.PRIORITY, priority);
                values.put(Tasks.CATEGORY_ID, category);
                values.put(Tasks.TIME_MIN, estTime);
                values.put(Tasks.DISPLAY_TS, display_ts);
                
            
                // Commit all of our changes to persistent storage. When the update completes
                // the content provider will notify the cursor of the change, which will
                // cause the UI to be updated.
                getContentResolver().update(mUri, values, null, null);
            }
        }
        else {
        	if(CycleSystem.DEBUG_ON) { Log.d(TAG, "onPause - mCursor IS NULL"); }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Build the menus that are shown when editing.
        if (mState == STATE_EDIT) {
            menu.add(0, REVERT_ID, 0, R.string.menu_revert)
                    .setShortcut('0', 'r')
                    .setIcon(android.R.drawable.ic_menu_revert);
                menu.add(0, DELETE_ID, 0, R.string.menu_delete)
                        .setShortcut('1', 'd')
                        .setIcon(android.R.drawable.ic_menu_delete);

        // Build the menus that are shown when inserting.
        } else {
            menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
                    .setShortcut('0', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        }


            Intent intent = new Intent(null, getIntent().getData());
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, TaskEditor.class), null, intent, 0, null);

        return true;
    }

    //@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case DELETE_ID:
            deleteTask();
            finish();
            break;
        case DISCARD_ID:
            cancelTask();
            break;
        case REVERT_ID:
            cancelTask();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of canceling work on a task.  Deletes the task if we
     * had created it, otherwise reverts to the original text.
     */
    private final void cancelTask() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(Tasks.TITLE, myOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteTask();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a task.  Simply deletes the entry.
     */
    private final void deleteTask() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            titleEdit.setText("");
        }
    }
    
    /**
     * Simple method to display an alert that the item isn't implemented.
     * @todo - TODO - remove this in production code
     * got this from http://moazzam-khan.com/blog/?p=134
     */
    protected void showNotImplementedDialog()
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(R.string.alert_notimplemented);
        dlgAlert.setTitle(R.string.alert_notimplemented_title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
    
    /**
     * Move this task to today.
     */
    protected void moveToday()
    {
    	Time t = new Time();
    	t.setToNow();
        int foo = (int) (t.toMillis(false) / 1000);
        foo = Util.getDayStart(foo);
        updateDatePicker(foo);
    }
    
    /**
     * Move task to next work day.
     */
    protected void moveNextWorkDay()
    {
    	Time t = new Time();
    	Integer display_ts = Util.YMDtoTSint(datePick.getYear(), datePick.getMonth(), datePick.getDayOfMonth());
    	t.set(display_ts.longValue() * 1000);
        int foo = (int) (t.toMillis(false) / 1000);
    	int bar = Util.findNextWorkDay(foo);
    	updateDatePicker(bar);
    }
    
    /**
     * Update the date picker to reflect a TS
     * @param ts
     */
    private void updateDatePicker(int ts)
    {
    	Integer foo = ts;
    	Long myDateTS = foo.longValue() * 1000; // need to cast to millis
        Integer[] myDate = Util.tsLongToYMD(myDateTS);
        datePick.updateDate(myDate[0], myDate[1], myDate[2]);
    }
    
}
