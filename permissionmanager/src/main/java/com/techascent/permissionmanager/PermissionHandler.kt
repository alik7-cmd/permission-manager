package com.techascent.permissionmanager

import android.content.Context
import java.lang.StringBuilder

abstract class PermissionHandler {

    var isLoggingEnabled = true

    abstract fun onPermissionGranted()

    open fun onPermissionDenied(context: Context, listOfDeniedPermission: List<String>) {
        if (isLoggingEnabled) {

            val builder = StringBuilder()
            builder.append("Denied permissions are :")
            listOfDeniedPermission.forEach { permission ->
                builder.append(" ")
                builder.append(permission)
            }
            AppPermission.log(builder.toString())
        }
    }

    open fun onPermissionBlocked(context: Context, listOfBlockedPermission: List<String>): Boolean {
        if (isLoggingEnabled) {
            val builder = StringBuilder()
            builder.append("Blocked permissions are :")
            listOfBlockedPermission.forEach { permission ->
                builder.append(" ")
                builder.append(permission)
            }
            AppPermission.log(builder.toString())
        }
        return false
    }

    fun onJustBlocked(
        context: Context,
        listOfJustBlockedPermission: List<String>,
        listOfDeniedPermission: List<String>
    ) {
        if (isLoggingEnabled) {
            val builder = StringBuilder()
            builder.append("Just blocked permissions are :")
            listOfJustBlockedPermission.forEach { permission ->
                builder.append(" ")
                builder.append(permission)
            }
            AppPermission.log(builder.toString())
        }
        onPermissionDenied(context, listOfDeniedPermission)
    }
}