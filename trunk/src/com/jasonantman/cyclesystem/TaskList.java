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

/**
 * Convenience definitions for CycleSystemTaskContentProvider
 * @author jantman
 *
 */

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * NotePadProvider
 */
public final class TaskList {
    public static final String AUTHORITY = "com.jasonantman.cyclesystem.cyclesystemtaskcontentprovider";

    public static final String[] CATEGORIES = new String[] {
        "Work", // 0
        "Personal" // 1
    };
    
    // This class cannot be instantiated
    private TaskList() {}
    
    /**
     * Tasks table
     */
    public static final class Tasks implements BaseColumns {
        // This class cannot be instantiated
        private Tasks() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tasks");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of tasks.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jasonantman.task";

        /**
         * The MIME type of a {@link #CONTENT_URI} single task.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jasonantman.task";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "priority ASC,created_ts ASC";

        
        /**
         * The task id
         * <P>Type: INTEGER</P>
         */
        public static final String _ID = "_id";
        
        /**
         * The timestamp for when the task was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_TS = "created_ts";

        /**
         * The timestamp for when the task was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_TS = "modified_ts";
        
        /**
         * The timestamp for when the task should be displayed
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String DISPLAY_TS = "display_ts";
        
        /**
         * The integer ID for the category that holds the task
         * <P>Type: INTEGER</P>
         */
        public static final String CATEGORY_ID = "category_id";
        
        /**
         * The priority of the task
         * <P>Type: INTEGER (1-3 incl, 9)</P>
         */
        public static final String PRIORITY = "priority";
        
        /**
         * The estimated time the task will take
         * <P>Type: INTEGER (1-3 incl, 9)</P>
         */
        public static final String TIME_MIN = "time_min";
        
        /**
         * The title of the task
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";
        
        /**
         * Whether or not the task is finished
         * <P>Type: BOOLEAN</P>
         */
        public static final String IS_FINISHED = "is_finished";
        
    }
}

