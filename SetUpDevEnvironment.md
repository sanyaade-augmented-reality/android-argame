# Setup Android development environment #

  * [Install the Android SDK](http://developer.android.com/sdk/installing.html)
  * [Hello World Tutorial](http://developer.android.com/guide/tutorials/hello-world.html) (To verify your environment)

# Understanding Android #
  * [Android Fundamentals](http://developer.android.com/guide/tutorials/hello-world.html) (Very good article, should be read first.)


# Getting ARgame to run in emulator #
**Prerequisite**
  * Make sure you have Android SDK, Eclipse(with Android plugin) installed.

**Create a Android Virtual Device (If you already create a 1.6 AVD (android-4), skip this step.)**
  * In Eclipse: Windows -> Android AVD Manager
  * Name = Whatever you like
  * Target = Android 1.6 (android-4)
  * Skin = Default
  * Click on "Create AVD"

# Checking out the Code #
**Manually checkout source codes (If you would like to use subclipse, skip to next step.)**
  * Make sure you have SVN command tools installed.
  * Go to the project site and checkout the source code.
  * In Eclipse: File -> New -> Other -> Android Project
  * Select "Create project from existing code", with the local svn folder you just checked out.
  * Project name = Whatever you like
  * Build Target = "Android 1.6 (android-4)"
  * Application name = Whatever you like
  * Min SDK Version = 4

**checkout source code with subclipse (This is not necessary if you use svn command manually)**
  * Install subclipse
  * In Eclipse: Window -> Show View -> other-> SVN Repositories
  * Click on the "+" sign
  * url: http://android-argame.googlecode.com/svn -> Finish
  * Right click url -> Checkout.... Use the defaults and click Finish.

# Run the application #
**In Eclipse**
  * Project -> Build All
  * Run -> Android Application

**In Emulator: Launch ARgame**
  * Start using the app!