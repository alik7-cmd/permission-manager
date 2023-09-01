package com.techascent.permissionmanager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

class PermissionManager internal constructor() {

    companion object{
        var isLoggingEnabled = true

        @JvmStatic
        fun log(message: String) {
            if (isLoggingEnabled)
                Log.d("", message)
        }
    }

    fun shouldEnableLogging(isEnable : Boolean){
        isLoggingEnabled = isEnable
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context     the android context.
     * @param permission  list of requested permissions.
     * @param rationale   an explanation message to shown the user explaining why this permission is necessary.
     *                    if s/he denied the permission earlier.
     * @param handler     a handler object of type [PermissionListener] to handle different user action like permissions grant,
     *                    permissions denied and permissions blocked.
     */
    fun with(
        context: Context,
        permission: String,
        rationale: String?,
        handler: PermissionListener
    ) {
        val permissions = arrayOf(permission)
        with(context, permissions, rationale, null, handler)
    }

    /**
     * Check/Request Permission and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permission   requested permissions.
     * @param rationaleId  a string id of an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param handler      a handler object of type [PermissionListener] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    fun with(
        context: Context,
        permission: String,
        rationaleId: Int,
        handler: PermissionListener
    ) {
        val rationale: String? = try {
            context.getString(rationaleId)
        } catch (e: Exception) {
            null
        }
        val permissions = arrayOf(permission)
        with(context, permissions, rationale, null, handler)
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permissions  list of requested permissions.
     * @param rationaleId  a string id of an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param option       message option for handling permissions.
     * @param handler      a handler object of type [PermissionListener] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    fun with(
        context: Context,
        permissions: Array<String>,
        rationaleId: Int?,
        option: Options?,
        handler: PermissionListener?
    ) {
        val rationale: String? = try {
            context.getString(rationaleId!!)
        } catch (e: Exception) {
            null
        }
        with(context, permissions, rationale, option, handler)
    }

    /**
     * Check/Request Permissions and triggers the handler method properly.
     *
     * @param context      the android context.
     * @param permissions  array of requested permission/permissions.
     * @param rationale    an explanation message to shown the user explaining why this permission is necessary.
     *                     if s/he denied the permission earlier.
     * @param option       message option for handling permissions.
     * @param handler      a handler object of type [PermissionListener] to handle different user action like permissions grant,
     *                     permissions denied and permissions blocked.
     */
    private fun with(
        context: Context,
        permissions: Array<String>,
        rationale: String?,
        option: Options?,
        handler: PermissionListener?
    ) {
        if (Build.VERSION.SDK_INT < 23) {
            handler?.onGranted()
            log("SDK version is less than 23")
        } else {
            val permissionSet = permissions.toSet()
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
                handler?.onGranted()
                PermissionActivity.permissionListener = null
            } else {
                PermissionActivity.permissionListener = handler
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

    class Builder private constructor(){
        private lateinit var context: Context
        private lateinit var listOfPermissions: Array<String>
        private var rationaleId : Int? = null
        private var rationale: String? = null
        private var option: Options? = null
        private lateinit var handler: PermissionListener

        private var manager : PermissionManager? = null

        private var flag = -1
        private var isLoggingEnabled = true

        constructor(withContext: Context) : this(){
            this.context = withContext
        }

        fun onRequestPermission(
            permissions: Array<String>,
            rationaleId: Int?,
            option: Options?,
            handler: PermissionListener
        ): Builder {
            this.listOfPermissions= permissions
            this.rationaleId = rationaleId
            this.option= option
            this.handler= handler
            flag = 1

            return this
        }

        fun onRequestPermission(
            permission: String,
            rationaleId: Int,
            handler: PermissionListener
        ): Builder {
            this.listOfPermissions= arrayOf(permission)
            this.rationaleId = rationaleId
            this.handler= handler
            flag = 2

            return this
        }

        fun onRequestPermission(
            permission: String,
            rationale: String?,
            handler: PermissionListener
        ): Builder {
            this.listOfPermissions= arrayOf(permission)
            this.rationale = rationale
            this.handler= handler
            flag = 3

            return this
        }

        fun enableLogging(isEnable: Boolean) : Builder{
            isLoggingEnabled = isEnable
            return this
        }

        fun build(){
            if(manager == null){
                manager = PermissionManager()
            }
            manager?.shouldEnableLogging(isLoggingEnabled)
            when(flag){
                1 -> manager?.with(context, listOfPermissions, rationaleId, option, handler)
                2 -> manager?.with(context, listOfPermissions, rationale, null, handler)
                3 -> manager?.with(context, listOfPermissions, rationale, null, handler)
            }

        }
    }


}
