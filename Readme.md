# permission-manager
Android runtime permissions were introduced in Android 6.0. Obviously, it's a good thing to have as a user, but this can be a nightmare for the developer if it's not handled properly.
This library reduces the complexity of handling repetitive or multiple permissions at runtime.

## Step 1:
Add the JitPack repository to your build.gradle/settings.gradle file

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

## Step 2:
Add the dependency

```groovy
dependencies {
        implementation 'com.github.alik7-cmd:permission-manager:1.2.8'
}
```

That's it! You are all set to use the library.

## How to use it

Pretty easy! Just add below code if you need multiple permissions to ask at the same time.

``` kotlin
val listOfPermission: Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        
PermissionManager.with(this, listOfPermission, null, null, object : PermissionListener(){
            override fun onGranted() {
                // Do whatever you want to do
            }

            override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                super.onDenied(context, listOfDeniedPermission)
                // Do whatever you want to do
            }

            override fun onBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onBlocked(context, listOfBlockedPermission)
            }
        })
```

And if you want to take a single permission use below code

``` kotlin
PermissionManager.with(this, Manifest.permission.CAMERA, null, object : PermissionListener(){
            override fun onGranted() {
                // Do whatever you want to do
            }

            override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                super.onDenied(context, listOfDeniedPermission)
                // Do whatever you want to do
            }

            override fun onBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onBlocked(context, listOfBlockedPermission)
            }

        })
```

HOLA!! It's too easy to use and saves you to write from a lot of boilerplate codes. So enjoy.  
