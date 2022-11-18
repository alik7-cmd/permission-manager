# permission-manager
Android runtime permissions were introduced in Android 6.0. Obviously it's a good thing to have as an user, but this can give nightmare to the developer if its not handled properly.
This Library reduces the complexity of handling repetative or multiple permissions in runtime.

## Step 1:
Add the JitPack repository to your build.gradle file

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

## Step 2:
Add the dependency

```
dependencies {
        implementation 'com.github.alik7-cmd:permission-manager:1.1.3'
}
```

That's it! You are all set to use the library.

## How to use it

Pretty easy! Just add below code if you need multiple permissions to ask at the same time.

``` kotlin
private val listOfPermissions: Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

PermissionManager.check(this,listOfPermission , null, null, object : PermissionHandler(){
            override fun onPermissionGranted() {
                // Do whatever you want to do
            }

            override fun onPermissionDenied(
                context: Context,
                listOfDeniedPermission: List<String>
            ) {
                // Do whatever you want to do
            }

            override fun onPermissionBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onPermissionBlocked(context, listOfBlockedPermission)
            }

        })
```

And if you want to take a single permission use below code

``` kotlin
PermissionManager.check(this, Manifest.permission.CAMERA, null ,object : PermissionHandler(){
            override fun onPermissionGranted() {
                // Do whatever you want to do
            }

            override fun onPermissionBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onPermissionBlocked(context, listOfBlockedPermission)
            }

            override fun onPermissionDenied(
                context: Context,
                listOfDeniedPermission: List<String>
            ) {
                // Do whatever you want to do
            }

        })
```

HOLA!! It's too easy to use and saves you to write from a lot of boilerplate codes. So enjoy.  