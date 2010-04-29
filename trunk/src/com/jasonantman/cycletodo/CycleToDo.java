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
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.MotionEvent;
import android.content.SharedPreferences;
import android.content.res.Resources;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class CycleToDo extends ListActivity implements View.OnClickListener
{
    /** Called when the activity is first created. */
    
	private static final String TAG = "CycleToDo";
	
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
    
    // @TODO - TODO - these should be preferences
    private CharSequence TITLE_TIME_FORMAT = "E, MMM d yyyy";
    
    // @TODO - this is probably no longer needed, as we're using settings.
    protected static int firstWorkDay = 1; // 0-6, 0 is Sunday, 1 is Monday, 5 is Friday
    protected static int lastWorkDay = 5; // 0-6, 0 is Sunday, 1 is Monday, 5 is Friday
    
    // workday stuff used to get/set from Preferences
    protected CharSequence[] _WORKDAY_OPTIONS = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    protected static boolean[] workday_values =  new boolean[7];
    // end work day stuff
    
    // Preferences stuff
    public static final String PREFS_NAME = "CycleToDoPrefs";
    public SharedPreferences settings;
    
    // Menu item ids - to make sure every menu item has a constant, unique integer ID
    public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_GOTO = Menu.FIRST + 1;
    public static final int MENU_ITEM_MANAGE = Menu.FIRST + 2;
    public static final int MENU_ITEM_TODAY = Menu.FIRST + 3;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 4;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 5;
    public static final int CONTEXT_ITEM_FINISH = Menu.FIRST + 6;
    public static final int CONTEXT_ITEM_EDIT = Menu.FIRST + 7;
    public static final int CONTEXT_ITEM_MOVE = Menu.FIRST + 8;
    public static final int SETTINGS_MENU_ITEM_BACKUP = Menu.FIRST + 9;
    public static final int SETTINGS_MENU_ITEM_CATEGORY = Menu.FIRST + 10;
    public static final int SETTINGS_MENU_ITEM_WORK_DAYS = Menu.FIRST + 11;
    public static final int CONTEXT_ITEM_DELETE = Menu.FIRST + 12;
    
    // dialog IDs
    static final int DATE_DIALOG_ID = 999;
    static final int MOVE_DATE_DIALOG_ID = 990;
    static final int SETTINGS_WORK_DAYS_DIALOG_ID = 995;
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int month, int date)
                {
                	CURRENT_TS = Util.YMDtoTSint(year, month, date);
                    updateList();
                }
            };
            
    // the callback received when the user moves to a new date
    private Uri FOO_MOVE_URI;
    private ContentResolver FOO_MOVE_RESOLVER;
    private DatePickerDialog.OnDateSetListener mMoveDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

            	public void onDateSet(DatePicker view, int year, int month, int date)
                {
            		int foobar = Util.YMDtoTSint(year, month, date);
            		ContentValues values = new ContentValues();
            		values.put(Tasks.DISPLAY_TS, foobar);
            		FOO_MOVE_RESOLVER.update(FOO_MOVE_URI, values, null, null);
            		updateList();
                }
            };
    
    // currently displayed ts
    private Time t = new Time();
    private int CURRENT_TS;
    private CharSequence timeTitleStr = "";
    
    private int CURRENT_CAT_ID;
    
    // for the header views
    private TextView headerDate;
    private Spinner categorySpinner;
    private ArrayAdapter<CharSequence> categorySpinnerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ts of day to display
        this.t.setToNow();
        this.CURRENT_TS = (int) (this.t.toMillis(false) / 1000);
        
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
        
        timeTitleStr = DateFormat.format(TITLE_TIME_FORMAT, ((long) this.CURRENT_TS * 1000));
        headerDate.setText(timeTitleStr);
        headerDate.setGravity(Gravity.CENTER_HORIZONTAL);
        getListView().addHeaderView(headerDate);
        
        // spinner for category
        // @TODO - TODO - this is just thrown in here for now.
        //   we should really have preferences for this stuff
        //   or even better pull from the database
        categorySpinner = new Spinner(this);
        categorySpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        categorySpinnerAdapter.add("--All--");
        categorySpinner.setOnItemSelectedListener(
                new  AdapterView.OnItemSelectedListener() {           
                	//@Override
                	public void onItemSelected(AdapterView<?> parent, 
                		View view, int position, long id) {
                		//selectItem(position);
                		selectCategory(position);
                	}
                	//@Override
                	public void onNothingSelected(AdapterView<?> parent) {

                	}
                });

        for(int i = 0; i < TaskList.CATEGORIES.length; i++)
        {
        	categorySpinnerAdapter.add(TaskList.CATEGORIES[i]);
        }
        //categorySpinner.
        getListView().addHeaderView(categorySpinner);
        
        updateList();
        
        // Gesture detection
        gestureDetector = new GestureDetector(new CycleSysGestureListener(this));
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
        
         // Restore preferences
         this.settings = getSharedPreferences(PREFS_NAME, 0);
         getPrefsOnStart();
    }

    // this is really just for the prefs.
    @Override
    protected void onStop()
    {
       super.onStop();
    
      // Save user preferences. We need an Editor object to
      // make changes. All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("silentMode", false);

      // Don't forget to commit your edits!!!
      editor.commit();
    }
    
    //@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
        
        menu.add(0, MENU_ITEM_TODAY, 0, R.string.menu_today)
        	.setShortcut('8', 't')
        	.setIcon(android.R.drawable.ic_menu_today);  
        
        menu.add(0, MENU_ITEM_HELP, 0, R.string.menu_help)
        	.setShortcut('0', 'h')
        	.setIcon(android.R.drawable.ic_menu_help);
        
        SubMenu SettingsMenu = menu.addSubMenu(0, MENU_ITEM_SETTINGS, 0, R.string.menu_settings)
        	.setIcon(android.R.drawable.ic_menu_preferences);
        
        SettingsMenu.add(0, SETTINGS_MENU_ITEM_BACKUP, 0, R.string.settings_menu_backup);
        SettingsMenu.add(0, SETTINGS_MENU_ITEM_CATEGORY, 0, R.string.settings_menu_default_category);
        SettingsMenu.add(0, SETTINGS_MENU_ITEM_WORK_DAYS, 0, R.string.settings_menu_work_days);
        
        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, CycleToDo.class), null, intent, 0, null);

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
        case MENU_ITEM_TODAY:
        	// Purge the finished items
        	t.setToNow();
            CURRENT_TS = (int) (this.t.toMillis(false) / 1000);
            updateList();
        	return true;
        case MENU_ITEM_HELP:
        	//startActivity(new Intent(Intent.ACTION_SYSTEM_TUTORIAL)); // total hack
        	Intent i = new Intent(this, Help.class);
        	startActivity(i);
        	return true;
        case MENU_ITEM_GOTO:
        	showDialog(DATE_DIALOG_ID);
        	return true;
        case MENU_ITEM_MANAGE:
        	showNotImplementedDialog("manage");
        	return true;
        case SETTINGS_MENU_ITEM_BACKUP:
        	doBackupToSD();
        	return true;
        case SETTINGS_MENU_ITEM_CATEGORY:
        	showNotImplementedDialog("work days");
        	return true;
        case SETTINGS_MENU_ITEM_WORK_DAYS:
        	showDialog(SETTINGS_WORK_DAYS_DIALOG_ID);
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
        	Integer[] foo = Util.tsLongToYMD(((long) this.CURRENT_TS * 1000));
            return new DatePickerDialog(this, mDateSetListener, foo[0], foo[1], foo[2]);
        case MOVE_DATE_DIALOG_ID:
        	Integer[] bar = Util.tsLongToYMD(((long) this.CURRENT_TS * 1000));
            return new DatePickerDialog(this, mMoveDateSetListener, bar[0], bar[1], bar[2]);
        case SETTINGS_WORK_DAYS_DIALOG_ID:
            return new AlertDialog.Builder( this )
            .setTitle( "Work Days" )
            .setMultiChoiceItems( _WORKDAY_OPTIONS, workday_values, new DialogSelectionClickHandler() )
            .setPositiveButton( "OK", new DialogButtonClickHandler() )
            .create();
        }
        return null;
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

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position-2); // -2 fixes off-by-one error (issue: 
        
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Tasks.TITLE)));

        // Add a menu item to delete the note
        menu.add(0, CONTEXT_ITEM_FINISH, 0, R.string.menu_finish);
        menu.add(0, CONTEXT_ITEM_EDIT, 1, R.string.menu_edit);
        menu.add(0, CONTEXT_ITEM_MOVE, 2, R.string.menu_move);
        menu.add(0, CONTEXT_ITEM_DELETE, 2, R.string.menu_delete);
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

        Uri taskUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
        
        switch (item.getItemId()) {
            case CONTEXT_ITEM_FINISH: {
                // Update the task that the context menu is for
                ContentValues values = new ContentValues();
                values.put(Tasks.IS_FINISHED, 1);
                getContentResolver().update(taskUri, values, null, null);
                return true;
            }
            case CONTEXT_ITEM_EDIT: {
            	startActivity(new Intent(Intent.ACTION_EDIT, taskUri));
            	return true;
            }
            case CONTEXT_ITEM_MOVE: {
            	showMoveItemDialog(taskUri);
            	return true;
            }
            case CONTEXT_ITEM_DELETE: {
            	getContentResolver().delete(taskUri, null, null);
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
    
    /**
     * Update the list to the selected category id.
     * @param id
     */
    protected void selectCategory(int id)
    {
    	this.CURRENT_CAT_ID = id;
    	updateList();
    }
    
    /**
     * Update the list view
     * @param id
     */
    protected void updateList()
    {
    	Cursor cursor;
    	
    	int start_ts = Util.getDayStart(this.CURRENT_TS);
    	int end_ts = start_ts + 86399;
    	
    	String date_where = Tasks.DISPLAY_TS + ">=" + Integer.toString(start_ts) + " AND " + Tasks.DISPLAY_TS + "<=" + Integer.toString(end_ts);
    	
    	if(this.CURRENT_CAT_ID == 0)
    	{
    		cursor = managedQuery(getIntent().getData(), PROJECTION, date_where, null, Tasks.DEFAULT_SORT_ORDER);
    	}
    	else
    	{
    		cursor = managedQuery(getIntent().getData(), PROJECTION, Tasks.CATEGORY_ID + "=" + Integer.toString(this.CURRENT_CAT_ID) + " AND (" + date_where + ")", null, Tasks.DEFAULT_SORT_ORDER);
    	}
    	
        // Used to map task entries from the database to views
        TaskCursorAdapter adapter = new TaskCursorAdapter(this, R.layout.main, cursor,
        		new String[] { Tasks.TITLE, Tasks.PRIORITY, Tasks.CATEGORY_ID, Tasks.TIME_MIN }, new int[] { R.id.title, R.id.taskIcon, R.id.category, R.id.timeMins });
        setListAdapter(adapter);
        // DataSetObserver to trigger updateList() when something is changed (to recalculate time total)
        adapter.registerDataSetObserver(
                new  DataSetObserver() {
                	public void onChanged()
                	{
                		updateList();
                	}
                });
        //adapter.registerDataSetObserver(observer);
        
        timeTitleStr = DateFormat.format(TITLE_TIME_FORMAT, ((long) this.CURRENT_TS * 1000));
        if(! getTimeTotal(cursor).equals("0m"))
        {
        	timeTitleStr = timeTitleStr + "  (" + getTimeTotal(cursor) + ")";
        }
        headerDate.setText(timeTitleStr);
    }
    
    /**
     * Handle a left fling action.
     */
    public void flingLeft()
    {
    	this.CURRENT_TS = this.CURRENT_TS + 86400;
        updateList();
    }
    
    /**
     * Handle a fling right action
     */
    public void flingRight()
    {
    	this.CURRENT_TS = this.CURRENT_TS - 86400;
        updateList();
    }
    
    /**
     * Show dialog to move an item from the context menu.
     * @param uri
     */
    protected void showMoveItemDialog(Uri uri)
    {
    	this.FOO_MOVE_URI = uri;
        this.FOO_MOVE_RESOLVER = getContentResolver();
    	showDialog(MOVE_DATE_DIALOG_ID);
    }
    
    /**
     * Simple method to display an alert that the item isn't implemented.
     * @todo - TODO - remove this in production code
     * got this from http://moazzam-khan.com/blog/?p=134
     */
    protected void showNotImplementedDialog(String s)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("NOTICE - this function (" + s + ") has not been implemented.");
        dlgAlert.setTitle(R.string.alert_notimplemented_title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    /**
     * Backup the SQLite DB to the SD card
     */
    protected boolean doBackupToSD()
    {
    	if(! Util.haveExternStorage())
    	{
    		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
    		dlgAlert.setMessage("ERROR - External storage not available.");
    		dlgAlert.setTitle(R.string.alert_storError_title);
    		dlgAlert.setPositiveButton("OK", null);
    		dlgAlert.setCancelable(true);
    		dlgAlert.create().show();
    		return false;
    	}
    	BackupHandler bh = new BackupHandler(this);
    	bh.execute();
    	return true;
    }
    
    // TODO - Begin dialog stuff
    
    /*
     * DialogSelectionClickHandler just here for the work days dialog, serves no other purpose.
     * Not actually used for anything.
     */
    public class DialogSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener
    {
        public void onClick( DialogInterface dialog, int clicked, boolean selected )
        {
        	
        }
    }

    /*
     * DialogButtonClickHandler just here for the work days dialog, serves no other purpose.
     * Just fires another function when the OK button is clicked on the work days dialog.
     */
    public class DialogButtonClickHandler implements DialogInterface.OnClickListener
    {
            public void onClick( DialogInterface dialog, int clicked ) { switch( clicked ) { case DialogInterface.BUTTON_POSITIVE: updateWorkDays(); break; } }
    }

    /*
     * Test function for dealing with the work days dialog selections.
     * @TODO - TODO - remove this, replace it with something useful.
     */
    protected void printSelectedPlanets(){
            for( int i = 0; i < _WORKDAY_OPTIONS.length; i++ ){
                    Log.i( "ME", _WORKDAY_OPTIONS[ i ] + " selected: " + workday_values[i] );
            }
    }

	/* 
	 * Just here because we have to implement it for the work days dialog stuff - serves no purpose.
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) { }
	
	/**
	 * Writes the updated work days back to preferences.
	 */
	protected void updateWorkDays()
	{
	      SharedPreferences.Editor editor = this.settings.edit();
	      editor.putInt("WORK_DAYS", Util.workDaysToInt(this.workday_values));
	      editor.commit();
	}
	
	/*
	 * Update our settings object on start, also set defaults if not currently set.
	 */
	protected void getPrefsOnStart()
	{	
        // EXAMPLE:
        //boolean silent = settings.getBoolean("silentMode", false);
        //setSilent(silent);
		
		this.workday_values = Util.workDaysFromInt(settings.getInt("WORK_DAYS", 62)); // default of Mon-Fri = 62
	}
	
	/**
	 * Get the total time for all items in the cursor result, return it as a formatted string.
	 * @param Cursor c
	 * @return String - formatted string
	 */
	protected String getTimeTotal(Cursor c)
	{
		int total = 0;
		String foo = "";
	    if (c.moveToFirst())
	    {
	        int timeCol = c.getColumnIndex(Tasks.TIME_MIN); 
	        do
	        {
	            total = total + c.getInt(timeCol);
	        } while (c.moveToNext());
	    }
		if(total < 60){ foo = Integer.toString(total)+"m"; }
		else{ foo = Integer.toString(total / 60) +"h " + Integer.toString(total % 60) + "m";}
		return foo;
	}
    
}