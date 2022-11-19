package com.techascent.permissionmanager

import android.content.Context
import java.lang.StringBuilder

abstract class PermissionListener {

    var isLoggingEnabled = true

    abstract fun onGranted()

    open fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
        if (isLoggingEnabled) {

            val builder = StringBuilder()
            builder.append("Denied permissions are :")
            listOfDeniedPermission.forEach { permission ->
                builder.append(" ")
                builder.append(permission)
            }
            PermissionManager.log(builder.toString())
        }
    }

    open fun onBlocked(context: Context, listOfBlockedPermission: List<String>): Boolean {
        if (isLoggingEnabled) {
            val builder = StringBuilder()
            builder.append("Blocked permissions are :")
            listOfBlockedPermission.forEach { permission ->
                builder.append(" ")
                builder.append(permission)
            }
            PermissionManager.log(builder.toString())
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
            PermissionManager.log(builder.toString())
        }
        onDenied(context, listOfDeniedPermission)
    }
}