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

import com.jasonantman.cyclesystem.TaskList.Tasks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


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
    };
    
    /** The index of the task column */
    private static final int COLUMN_INDEX_TASK = 1;
    
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
    private boolean mTaskOnly = false;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String myOriginalContent;

    /**
     * A custom EditText that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }
        
        //@Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

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
            // in the container.
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
        
        // The text view for our note, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.note);

        // Get the note!
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
            mText.setTextKeepState(taskTitle);
            
            // If we hadn't previously retrieved the original text, do so
            // now.  This allows the user to revert their changes.
            if (myOriginalContent == null) {
                myOriginalContent = taskTitle;
            }

        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
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
            String text = mText.getText().toString();
            int length = text.length();

            // If this activity is finished, and there is no text, then we
            // do something a little special: simply delete the note entry.
            // Note that we do this both for editing and inserting...  it
            // would be reasonable to only do it when inserting.
            if (isFinishing() && (length == 0) && !mTaskOnly) {
                setResult(RESULT_CANCELED);
                deleteTask();

            // Get out updates into the provider.
            } else {
                ContentValues values = new ContentValues();

                // This stuff is only done when working with a full-fledged note.
                if (!mTaskOnly) {
                    // Bump the modification time to now.
                    values.put(Tasks.MODIFIED_TS, System.currentTimeMillis());
                }

                values.put(Tasks.TITLE, text);
            
                // Commit all of our changes to persistent storage. When the update completes
                // the content provider will notify the cursor of the change, which will
                // cause the UI to be updated.
                getContentResolver().update(mUri, values, null, null);
            }
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
            if (!mTaskOnly) {
                menu.add(0, DELETE_ID, 0, R.string.menu_delete)
                        .setShortcut('1', 'd')
                        .setIcon(android.R.drawable.ic_menu_delete);
            }

        // Build the menus that are shown when inserting.
        } else {
            menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
                    .setShortcut('0', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        }

        // If we are working on a full task, then append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        if (!mTaskOnly) {
            Intent intent = new Intent(null, getIntent().getData());
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, TaskEditor.class), null, intent, 0, null);
        }

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
            mText.setText("");
        }
    }
}
