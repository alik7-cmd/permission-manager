package com.techascent.permissionmanager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.util.*

object PermissionManager {

    var isLoggingEnabled = true

    @JvmStatic
    fun log(message: String) {
        if (isLoggingEnabled)
            Log.d("", message)
    }

    @JvmStatic
    fun disableLogging() {
        isLoggingEnabled = false
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context     the android context.
     * @param permission  list of requested permissions.
     * @param rationale   an explanation message to shown the user explaining why this permission is necessary.
     *                    if s/he denied the permission earlier.
     * @param handler     a handler object of type [PermissionHandler] to handle different user action like permissions grant,
     *                    permissions denied and permissions blocked.
     */
    @JvmStatic
    fun check(
        context: Context,
        permission: String,
        rationale: String?,
        handler: PermissionHandler
    ) {
        val permissions = arrayOf(permission)
        check(context, permissions, rationale, null, handler)
    }

    /**
     * Check/Request Permission and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permission   requested permissions.
     * @param rationaleId  a string id of an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param handler      a handler object of type [PermissionHandler] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    @JvmStatic
    fun check(
        context: Context,
        permission: String,
        rationaleId: Int,
        handler: PermissionHandler
    ) {
        val rationale: String? = try {
            context.getString(rationaleId)
        } catch (e: Exception) {
            null
        }
        val permissions = arrayOf(permission)
        check(context, permissions, rationale, null, handler)
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permissions  list of requested permissions.
     * @param rationaleId  a string id of an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param option       message option for handling permissions.
     * @param handler      a handler object of type [PermissionHandler] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    @JvmStatic
    fun check(
        context: Context,
        permissions: Array<String>,
        rationaleId: Int?,
        option: Options?,
        handler: PermissionHandler?
    ) {
        val rationale: String? = try {
            context.getString(rationaleId!!)
        } catch (e: Exception) {
            null
        }
        check(context, permissions, rationale, option, handler)
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permissions  array of requested permission/permissions.
     * @param rationale    an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param option       message option for handling permissions.
     * @param handler      a handler object of type [PermissionHandler] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    @JvmStatic
    private fun check(
        context: Context,
        permissions: Array<String>,
        rationale: String?,
        option: Options?,
        handler: PermissionHandler?
    ) {
        if (Build.VERSION.SDK_INT < 23) {
            handler?.onPermissionGranted()
            log("SDK version is less than 23")
        } else {
            val permissionSet = mutableSetOf<String>()
            Collections.addAll(permissionSet, *permissions)
            var allPermissionGranted = true
            run breaking@{
                permissionSet.forEach { permission ->
                    if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        allPermissionGranted = false
                        return@breaking
                    }
                }
            }

            if (allPermissionGranted) {
                handler?.onPermissionGranted()
                PermissionActivity.permissionHandler = null
            } else {
                PermissionActivity.permissionHandler = handler
                val allPermissionList = arrayListOf<String>()
                allPermissionList.addAll(permissionSet)
                val intent = PermissionActivity.onNewIntent(
                    context,
                    allPermissionList,
                    option,
                    rationale ?: ""
                )
                if (option != null || option?.createNewTask ?: false) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }

        }
    }


}