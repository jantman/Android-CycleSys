/**
 * +----------------------------------------------------------------------+
 * | CycleSystem      http://CycleSystem.jasonantman.com                  |
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
 * @TODO - TODO - update the license for all of these files. is Apache 2.0 compatible with GPLv3? Can I do this?? 
 */

package com.jasonantman.cyclesystem;

import com.jasonantman.cyclesystem.TaskList.Tasks;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.MotionEvent;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class CycleSystem extends ListActivity implements OnGestureListener {
    /** Called when the activity is first created. */
    
	private static final String TAG = "CycleSystem";
	
	// stuff for gestures
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
	
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Tasks._ID, // 0
            Tasks.TITLE, // 1
            Tasks.PRIORITY, //2
            Tasks.TIME_MIN, // 3
            Tasks.CATEGORY_ID, // 4
            Tasks.IS_FINISHED, // 5
    };
	
    // controls debugging-level output
    public static final boolean DEBUG_ON = true;
    
    // @TODO - TODO - this should be a preference
    private static final CharSequence TITLE_TIME_FORMAT = "E, MMM d yyyy";
    
    /**
     * TODO: no idea why these are here
     */
    // Menu item ids
    public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_GOTO = Menu.FIRST + 1;
    public static final int MENU_ITEM_MANAGE = Menu.FIRST + 2;
    public static final int MENU_ITEM_PURGE = Menu.FIRST + 3;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 4;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 5;
    public static final int CONTEXT_ITEM_FINISH = Menu.FIRST + 6;
    public static final int CONTEXT_ITEM_EDIT = Menu.FIRST + 7;
    
    // currently displayed ts
    private Time t = new Time();
    private long CURRENT_TS;
    private CharSequence timeTitleStr = "";
    
    // for the header views
    private TextView headerDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ts of day to display
        t.setToNow();
        CURRENT_TS = t.toMillis(false);
        
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Tasks.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
        
        // create the header views
        headerDate = new TextView(this);
        headerDate.setMaxLines(1);
        timeTitleStr = DateFormat.format(TITLE_TIME_FORMAT, CURRENT_TS);
        headerDate.setText(timeTitleStr);
        
        // center my_textbox
        
        //RelativeLayout.LayoutParams params_center = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //params_center.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
        // add my_textbox
        //l.addView(my_textbox, params_center); 
        headerDate.setGravity(Gravity.CENTER_HORIZONTAL);
        // headerDate.setLayoutParams();
        // headerDate.getLayout();
        //LayoutParams lp = new LayoutParams(arg0);
        getListView().addHeaderView(headerDate);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, Tasks.DEFAULT_SORT_ORDER);

        // Used to map task entries from the database to views
        TaskCursorAdapter adapter = new TaskCursorAdapter(this, R.layout.main, cursor,
        		new String[] { Tasks.TITLE, Tasks.PRIORITY, Tasks.CATEGORY_ID, Tasks.TIME_MIN }, new int[] { R.id.title, R.id.taskIcon, R.id.category, R.id.timeMins });
        setListAdapter(adapter);
        
        // Gesture detection
        gestureDetector = new GestureDetector(new CycleSysGestureListener());
        gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 if (gestureDetector.onTouchEvent(event)) {
                     return true;
                 }
                 return false;
             }
         };
         
         this.getListView().setOnTouchListener(gestureListener);
         // end Gesture detection
        
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);

        menu.add(0, MENU_ITEM_GOTO, 0, R.string.menu_goto)
        	.setShortcut('4', 'g')
        	.setIcon(android.R.drawable.ic_menu_day);  
        
        menu.add(0, MENU_ITEM_MANAGE, 0, R.string.menu_manage)
        	.setShortcut('6', 'm')
        	.setIcon(android.R.drawable.ic_menu_agenda);  
        
        menu.add(0, MENU_ITEM_PURGE, 0, R.string.menu_purge)
        	.setShortcut('7', 'p')
        	.setIcon(android.R.drawable.ic_menu_delete);  
        
        menu.add(0, MENU_ITEM_HELP, 0, R.string.menu_help)
        	.setShortcut('0', 'h')
        	.setIcon(android.R.drawable.ic_menu_help);

        menu.add(0, MENU_ITEM_SETTINGS, 0, R.string.menu_settings)
        	.setIcon(android.R.drawable.ic_menu_preferences);
        
        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, CycleSystem.class), null, intent, 0, null);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any tasks in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Build menu...  always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                    items);

            // Give a shortcut to the edit action.
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Launch activity to insert a new item
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        case MENU_ITEM_PURGE:
        	// Purge the finished items
            getContentResolver().delete(getIntent().getData(), Tasks.IS_FINISHED + "==1", null);
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //@Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Tasks.TITLE)));

        // Add a menu item to delete the note
        menu.add(0, CONTEXT_ITEM_FINISH, 0, R.string.menu_finish);
        menu.add(0, CONTEXT_ITEM_EDIT, 1, R.string.menu_edit);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case CONTEXT_ITEM_FINISH: {
                // Update the task that the context menu is for
                Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                ContentValues values = new ContentValues();
                values.put(Tasks.IS_FINISHED, 1);
                getContentResolver().update(noteUri, values, null, null);
                return true;
            }
        }
        return false;
    }

    //@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user.  The have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onClick()"); }
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onDown()"); }
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onFling()"); }
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onLongPress()"); }
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onScroll()"); }
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onShowPress()"); }
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		if(CycleSystem.DEBUG_ON) { Log.d(TAG, " onSingleTapUp()"); }
		return false;
	}
}