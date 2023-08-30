# Permission Manager

Android runtime permissions were introduced in Android 6.0. Obviously, it's a good thing to have as
a user, but this can be a nightmare for the developer if it's not handled properly.
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
    implementation 'com.github.alik7-cmd:permission-manager:1.3.0'
}
```

That's it! You are all set to use the library.

## How to use it

Pretty easy! Just add below code if you need multiple permissions to ask at the same time.

``` kotlin
private val listOfPermission = 
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        
        PermissionManager.Builder(this)
            .enableLogging(true)
            .onRequestPermission(listOfPermission, null, null,
                object : PermissionListener() {
                    override fun onGranted() {
                        openCamera()
                    }

                    override fun onBlocked(
                        context: Context,
                        listOfBlockedPermission: List<String>
                    ): Boolean {
                        return super.onBlocked(context, listOfBlockedPermission)
                    }

                    override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                        super.onDenied(context, listOfDeniedPermission)
                    }
                }).enableLogging(true)
            .build()
        
```

And if you want to take a single permission use below code

``` kotlin
PermissionManager.Builder(this).onRequestPermission(Manifest.permission.CAMERA, null,
            object : PermissionListener() {
                override fun onGranted() {
                    openCamera()
                }

                override fun onBlocked(
                    context: Context,
                    listOfBlockedPermission: List<String>
                ): Boolean {
                    return super.onBlocked(context, listOfBlockedPermission)
                }

                override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                    super.onDenied(context, listOfDeniedPermission)
                }
            }).enableLogging(true)
            .build()
```

You can also customize permission request if you want using below code

```kotlin

private val listOfPermission =
    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

val option = Options(
    "Goto Settings",
    "Permissions Needed",
    "Permissions Needed",
    "Please provide requested permission(s) from settings.",
    true,
    false)

PermissionManager.Builder(this)
    .enableLogging(true)
    .onRequestPermission(listOfPermission, null, option,
        object : PermissionListener() {
            override fun onGranted() {
                openCamera()
            }

            override fun onBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                return super.onBlocked(context, listOfBlockedPermission)
            }

            override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                super.onDenied(context, listOfDeniedPermission)
            }
        }).enableLogging(true)
    .build()


```

## What this library offers :

- Very simple to use, and implementing it just requires a small   amount of code.
- Can request single or multiple permissions at the same time.
- Compact (10-15KB).
- Can handle the permission blocked case.
- Highly customizable.
- Dark Mode supported.

## License

```
Copyright (C), Permission Manager Library

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```