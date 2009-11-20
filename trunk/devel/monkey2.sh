#!/bin/bash

# +----------------------------------------------------------------------+
# | CycleToDo                    http://CycleToDo.jasonantman.com        |
# +----------------------------------------------------------------------+
# | Copyright (c) 2009 Jason Antman <jason@jasonantman.com>.             |
# |                                                                      |
# | This program is free software; you can redistribute it and/or modify |
# | it under the terms of the GNU General Public License as published by |
# | the Free Software Foundation; either version 3 of the License, or    |
# | (at your option) any later version.                                  |
# |                                                                      |
# | This program is distributed in the hope that it will be useful,      |
# | but WITHOUT ANY WARRANTY; without even the implied warranty of       |
# | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        |
# | GNU General Public License for more details.                         |
# |                                                                      |
# | You should have received a copy of the GNU General Public License    |
# | along with this program; if not, write to:                           |
# |                                                                      |
# | Free Software Foundation, Inc.                                       |
# | 59 Temple Place - Suite 330                                          |
# | Boston, MA 02111-1307, USA.                                          |
# +----------------------------------------------------------------------+
# |Please use the above URL for bug reports and feature/support requests.|
# +----------------------------------------------------------------------+
# | Authors: Jason Antman <jason@jasonantman.com>                        |
# +----------------------------------------------------------------------+
# | $LastChangedRevision::                                             $ |
# | $HeadURL::                                                         $ |
# +----------------------------------------------------------------------+

adb shell monkey -p com.jasonantman.cyclesystem -v -s 65654657 --throttle 500 --pct-motion 0 --pct-trackball 0 --kill-process-after-error 500 > monkey2.log
