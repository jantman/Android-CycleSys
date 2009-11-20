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

import java.util.HashMap;

import com.jasonantman.cycletodo.TaskList.Tasks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.content.UriMatcher;


/**
 * @author jantman
 *
 */
public class CycleSystemTaskContentProvider extends ContentProvider {

	private static final String TAG = "CycleSystemTaskContentProvider"; // for debugging/Log
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.jasonantman.cycletodo.cyclesystemtaskcontentprovider");
	public static final String AUTHORITY = "com.jasonantman.cycletodo.cyclesystemtaskcontentprovider";
	
    private static final String DATABASE_NAME = "cycletodo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TASKS_TABLE_NAME = "tasks";
	
    private static final int TASKS = 1;
    private static final int TASK_ID = 2;
    
    private static HashMap<String, String> sTasksProjectionMap;
    
    private static final UriMatcher sUriMatcher;

    // TODO - these should be settings
	private static final int DEFAULT_PRIORITY = 1;
	private static final int DEFAULT_CATEGORY = 1;
	private static final int DEFAULT_TIME_MIN = 5;
	private static final String DEFAULT_SORT_ORDER = Tasks.DEFAULT_SORT_ORDER;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TASKS_TABLE_NAME + " ("
                    + Tasks._ID + " INTEGER PRIMARY KEY,"
                    + Tasks.CREATED_TS + " INTEGER,"
                    + Tasks.MODIFIED_TS + " INTEGER,"
                    + Tasks.DISPLAY_TS + " INTEGER,"
                    + Tasks.CATEGORY_ID + " INTEGER,"
                    + Tasks.PRIORITY + " INTEGER,"
                    + Tasks.TIME_MIN + " INTEGER,"
                    + Tasks.TITLE + " TEXT,"
                    + Tasks.IS_FINISHED + " INTEGER DEFAULT 0"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
            onCreate(db);
        }
    }
    
    private DatabaseHelper myOpenHelper;
    
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
	    myOpenHelper = new DatabaseHelper(getContext());
	    return true;
	}
    
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case TASKS:
            count = db.delete(TASKS_TABLE_NAME, where, whereArgs);
            break;

        case TASK_ID:
            String taskId = uri.getPathSegments().get(1);
            count = db.delete(TASKS_TABLE_NAME, Tasks._ID + "=" + taskId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	/* (non-Javadoc)
	 * Mark a task as finished
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	//@Override
	public int markFinished(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(Tasks.IS_FINISHED, 1);
        
        int count;
        switch (sUriMatcher.match(uri)) {
        case TASKS:
            count = db.update(TASKS_TABLE_NAME, values, where, whereArgs);
            break;

        case TASK_ID:
            String taskId = uri.getPathSegments().get(1);
            count = db.update(TASKS_TABLE_NAME, values, Tasks._ID + "=" + taskId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case TASKS:
            return Tasks.CONTENT_TYPE;

        case TASK_ID:
            return Tasks.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != TASKS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());
        now = now / 1000;

        // Make sure that the fields are all set
        if (values.containsKey(Tasks.CREATED_TS) == false) {
            values.put(Tasks.CREATED_TS, now);
        }

        if (values.containsKey(Tasks.MODIFIED_TS) == false) {
            values.put(Tasks.MODIFIED_TS, now);
        }

        if (values.containsKey(Tasks.DISPLAY_TS) == false) {
            values.put(Tasks.DISPLAY_TS, now);
        }
        
        if (values.containsKey(Tasks.CATEGORY_ID) == false) {
            values.put(Tasks.CATEGORY_ID, DEFAULT_CATEGORY);
        }
        
        if (values.containsKey(Tasks.PRIORITY) == false) {
            values.put(Tasks.PRIORITY, DEFAULT_PRIORITY);
        }
        
        if (values.containsKey(Tasks.TIME_MIN) == false) {
            values.put(Tasks.TIME_MIN, DEFAULT_TIME_MIN);
        }
        
        if (values.containsKey(Tasks.TITLE) == false) {
            values.put(Tasks.TITLE, "");
        }

        if (values.containsKey(Tasks.IS_FINISHED) == false) {
            values.put(Tasks.IS_FINISHED, 0);
        }

        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        long rowId = db.insert(TASKS_TABLE_NAME, Tasks.TITLE, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }



	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs, String sortOrder) {
		
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case TASKS:
            qb.setTables(TASKS_TABLE_NAME);
            qb.setProjectionMap(sTasksProjectionMap);
            break;

        case TASK_ID:
            qb.setTables(TASKS_TABLE_NAME);
            qb.setProjectionMap(sTasksProjectionMap);
            qb.appendWhere(Tasks._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = myOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, where, whereArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case TASKS:
            count = db.update(TASKS_TABLE_NAME, values, where, whereArgs);
            break;

        case TASK_ID:
            String taskId = uri.getPathSegments().get(1);
            count = db.update(TASKS_TABLE_NAME, values, Tasks._ID + "=" + taskId + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TaskList.AUTHORITY, "tasks", TASKS);
        sUriMatcher.addURI(TaskList.AUTHORITY, "tasks/#", TASK_ID);

        sTasksProjectionMap = new HashMap<String, String>();
        sTasksProjectionMap.put(Tasks._ID, Tasks._ID);
        sTasksProjectionMap.put(Tasks.CREATED_TS, Tasks.CREATED_TS);
        sTasksProjectionMap.put(Tasks.MODIFIED_TS, Tasks.MODIFIED_TS);
        sTasksProjectionMap.put(Tasks.DISPLAY_TS, Tasks.DISPLAY_TS);
        sTasksProjectionMap.put(Tasks.CATEGORY_ID, Tasks.CATEGORY_ID);
        sTasksProjectionMap.put(Tasks.PRIORITY, Tasks.PRIORITY);
        sTasksProjectionMap.put(Tasks.TIME_MIN, Tasks.TIME_MIN);
        sTasksProjectionMap.put(Tasks.TITLE, Tasks.TITLE);
        sTasksProjectionMap.put(Tasks.IS_FINISHED, Tasks.IS_FINISHED);
    }
	
}
