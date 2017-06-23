Compass is a sample android application which implements trivial compass functionality.
It was created just as a code snippet reuse in some of my projects.

The main feature of the compass is noise filter (Low-pass filter) which make the arrow
more stable in comparison to other similar applications where arrow always trembles.

You can install it from google play: https://play.google.com/store/apps/details?id=com.sevencrayons.compass

Enhancements are welcome!

Nearest changes:

* better, correct graphics
* relevant layout for different devices (tablets)
* insert an instructon about how to calibrate device sensors

### Important Notes
Current WIP:
* `Compass` class refactoring (detach working with views) [DONE]
* customization of place to point
See [dev branch](https://github.com/iutinvg/compass/tree/dev) for details.

So for now, if you use `Compass` class in your project please consider the
[Compass](https://github.com/iutinvg/compass/blob/dev/app/src/main/java/com/sevencrayons/compass/Compass.java)
from `dev` branch: it's shorter and easier to integrate.

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
