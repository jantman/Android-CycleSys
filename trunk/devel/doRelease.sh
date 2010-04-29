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

echo "Remember to export the *signed* APK through Eclipse as /home/jantman/android-dev/cyclesystem/APKs/svn/REVISION-NUMBER/CycleToDo-unaligned.apk"

read -p "SVN Rev Numver: " REVNUM
read -p "Release Number (x.x.x): " RELNUM

echo "renaming package..."
mv "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM/CycleSystem-unaligned.apk" "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM/CycleToDo-unaligned.apk"

echo "zipaligning package..."
zipalign -v 4 "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM/CycleToDo-unaligned.apk" "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM/CycleToDo.apk"

echo "linking release directory..."
ln -s "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM" "/home/jantman/android-dev/cyclesystem/APKs/releases/$RELNUM"

echo "copying file to web1..."
scp "/home/jantman/android-dev/cyclesystem/APKs/svn/$REVNUM/CycleToDo.apk" "jantman@web1:/srv/www/vhosts/cycletodo.jasonantman.com/APKs/CycleToDo-$RELNUM.apk"

echo "tagging in SVN..."
svn copy http://svn.jasonantman.com/Android-CycleSys/trunk/ "http://svn.jasonantman.com/Android-CycleSys/tags/RELEASE-$RELNUM/" -m "$RELNUM release - SVN rev $REVNUM"

echo "Done."
