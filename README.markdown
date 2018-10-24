Compass is a sample android application which implements trivial compass functionality.
It was created just as a code snippet reuse in some of my projects.

The main feature of the compass is noise filter (Low-pass filter) which make the arrow
more stable in comparison to other similar applications where arrow always trembles.

You can install it from google play: https://play.google.com/store/apps/details?id=com.sevencrayons.compass

Enhancements are welcome!

### TODO:
* customization of place to point, see [dev branch](https://github.com/iutinvg/compass/tree/dev) for details
* better, correct graphics
* relevant layout for different devices (tablets)
* insert an instructon about how to calibrate device sensors

### How to Build
* launch Android Studio
* select **Open an existing Android Studio project**
* select the project folder
* select menu **Run** -> **Run 'app'**

### Articles to Learn
* http://stackoverflow.com/questions/12800982/accelerometer-with-low-passfilter-in-android
* http://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER
* http://stackoverflow.com/questions/1884699/android-compass-noise-algorithm
* http://en.wikipedia.org/wiki/Low-pass_filter
* Find closest number in array using binary search https://www.geeksforgeeks.org/find-closest-number-array/
