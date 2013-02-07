Memory Traces
=============

Memory Traces is an interactive documentary showcasing the memories
prominent Italian-Americans in the Boston community.

See the [Memory Traces][] site for more details.

This project was built with [Open Locast][].

Dependencies
------------
*   [MEL ImageCache][]
*   [MelAUtils][] (included)
*   [CWAC Adapter Wrapper][] (included)
*   [android-support-v4][] (included)
*   [Apache HTTP MIME][] (included)

Publishing
----------

To publish this app, the Google maps keys in the various layout files need to
be set. To do this, run:

    ./set_maps_keys.sh KEY

where KEY your Google Maps v1 API key.

License
-------
Memory Traces mobile app  
Copyright 2010-2013 [MIT Mobile Experience Lab][mel]

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

[Memory Traces]: http://locast.mit.edu/memorytraces/
[Open Locast]: http://locast.mit.edu/
[CWAC Adapter Wrapper]: https://github.com/commonsguy/cwac-adapter
[android-support-v4]: http://android-developers.blogspot.com/2011/03/fragments-for-all.html
[android2po]: https://github.com/miracle2k/android2po/
[Pootle]: http://translate.sourceforge.net/wiki/pootle
[MEL ImageCache]: https://github.com/mitmel/Android-Image-Cache
[mel]: http://mobile.mit.edu/
[MelAUtils]: https://github.com/mitmel/MelAUtils
[AppUpdateChecker]: https://github.com/mitmel/AppUpdateChecker
[Apache HTTP MIME]: http://hc.apache.org/httpcomponents-client-ga/httpmime/
