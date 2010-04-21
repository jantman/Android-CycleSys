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

/**
 * Just exists to get the SVN rev numbers into the program itself.
 * Should be "touched" before each SVN commit.
 * @param ts Long timestamp
 * @return Integer[3] like [year, month, date]
 * @author jantman   
 *   
 */   
public final class TouchMe {

	public static final String SVNrev = "$LastChangedRevision$";
	
	// touched
	public static String getSvnRev()
	{
		String foo = SVNrev;
		foo = foo.replace('$', ' ');
		foo = foo.substring(foo.indexOf(":")+1);
		foo = foo.trim();
		return foo;
	}

}
