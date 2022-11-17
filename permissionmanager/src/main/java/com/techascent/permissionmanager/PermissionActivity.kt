/*
package com.techascent.permissionmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionActivity : AppCompatActivity() {

    private var options: com.techascent.permissionmanager.Options? = null
    private var allPermissionList: List<String>? = null
    private var deniedPermissionsList = mutableListOf<String>()
    private var noRationaleList =  mutableListOf<String>()

    private var rationale: String? = null

    companion object {
        var permissionHandler: com.techascent.permissionmanager.PermissionHandler? = null

        const val PERMISSION_LIST = "PERMISSION_LIST"
        const val MESSAGE = "MESSAGE"
        const val OPTIONS = "OPTIONS"

        const val SETTINGS_CODE = 6756
        const val PERMISSION_CODE = 6794


        fun onNewIntent(
            context: Context?,
            permissionsList: ArrayList<String>,
            option: com.techascent.permissionmanager.Options?,
            rationale: String
        ): Intent {
            val bundle = Bundle()
            bundle.putStringArrayList(PERMISSION_LIST, permissionsList)
            bundle.putParcelable(OPTIONS, option)
            bundle.putString(MESSAGE, rationale)
            return Intent(context, PermissionActivity::class.java).apply {
                putExtras(bundle)
                if (option != null && option.createNewTask) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent == null || !intent.extras!!.containsKey(PERMISSION_LIST)){
            finish()
            return
        }
        setFinishOnTouchOutside(false)
        window.statusBarColor = 0
        intent.extras.apply {
            rationale = this?.getString(MESSAGE)
            options = this?.getParcelable(OPTIONS)
            allPermissionList = this?.getStringArrayList(PERMISSION_LIST)
        }

        if(options == null){
            options = com.techascent.permissionmanager.Options()
        }

        var noRationale = true
        allPermissionList?.forEach {
            if(checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED){
                deniedPermissionsList.add(it)
                if (shouldShowRequestPermissionRationale(it)) {
                    noRationale = false
                } else {
                    noRationaleList.add(it)
                }
            }
        }
        if(deniedPermissionsList.isEmpty()){
            grant()
            return
        }

        if(noRationale || TextUtils.isEmpty(rationale)){
            com.techascent.permissionmanager.AppPermission.log("No rationale.")
            requestPermissions(
                deniedPermissionsList.toTypedArray(),
                PERMISSION_CODE
            )
        }else{
            com.techascent.permissionmanager.AppPermission.log("Show rationale.")
            showRationaleDialog(rationale!!)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showRationaleDialog(message: String) {
        val listener =
            DialogInterface.OnClickListener { dialog, which ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    requestPermissions(
                        deniedPermissionsList.toTypedArray(),
                        PERMISSION_CODE
                    )
                } else {
                    deny()
                }
            }
        AlertDialog.Builder(this).setTitle(options!!.rationaleDialogTitle)
            .setMessage(message)
            .setPositiveButton(R.string.permission_manager_text_ok, listener)
            .setNegativeButton(R.string.permission_manager_text_cancel, listener)
            .setOnCancelListener { deny() }.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showMaterialRationaleDialog(message: String){
        MaterialAlertDialogBuilder(this)
            .setTitle(options?.rationaleDialogTitle)
            .setMessage(message).setPositiveButton(R.string.permission_manager_text_ok) { _, _ ->
                run {
                    requestPermissions()
                }
            }.setNegativeButton(R.string.permission_manager_text_cancel) { _, _ ->
                run {
                    deny()
                }
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        requestPermissions(allPermissionList?.toTypedArray() ?: emptyArray<String>(), PERMISSION_CODE)
    }

    private fun deny() {
        val handler = permissionHandler
        finish()
        if (handler != null) {
            handler.onPermissionDenied(this, deniedPermissionsList)
        }
    }

    private fun grant() {
        val handler = permissionHandler
        finish()
        if (handler != null) {
            handler.onPermissionGranted()
        }
    }

    @SuppressLint("MissingSuperCall")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isEmpty()){
            deny()
        }else{
            deniedPermissionsList.clear()
            grantResults.forEach {
                if(grantResults[it] != PackageManager.PERMISSION_GRANTED){
                    deniedPermissionsList.add(permissions[it])
                }
            }

            if(deniedPermissionsList.size == 0){
                com.techascent.permissionmanager.AppPermission.log("Just denied")
                grant()
            }else{
                val blockedList = mutableListOf<String>()
                val justBlockedList = mutableListOf<String>()
                val justDeniedList = mutableListOf<String>()

                deniedPermissionsList.forEach {
                    if(shouldShowRequestPermissionRationale(it)){
                        justDeniedList.add(it)
                    }else{
                        blockedList.add(it)
                        if (!noRationaleList.contains(it)) {
                            justBlockedList.add(it)
                        }
                    }
                }

                if(justBlockedList.size > 0){
                    val handler = permissionHandler
                    finish()
                    if(handler != null){
                        handler.onJustBlocked(this, justBlockedList, deniedPermissionsList)
                    }
                }else if(justDeniedList.size > 0){
                    deny()
                }else{
                    //unavailable permissions were already set not to ask again.
                    if (permissionHandler != null &&
                        permissionHandler!!.onPermissionBlocked(
                            applicationContext, blockedList
                        )
                    ) {
                        sendToSettings()
                    } else{
                        finish()
                    }
                }

            }

        }
    }

    private fun sendToSettings() {
        if (!options!!.sendBlockedToSettings) {
            deny()
            return
        }
        AppPermission.log("Ask to go to settings.")
        AlertDialog.Builder(this).setTitle(options!!.settingsDialogTitle)
            .setMessage(options!!.settingsDialogMessage)
            .setPositiveButton(
                options!!.settingsText
            ) { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivityForResult(
                    intent,
                    SETTINGS_CODE
                )
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { dialog, which -> deny() }
            .setOnCancelListener { deny() }.create().show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SETTINGS_CODE && permissionHandler != null){
            com.techascent.permissionmanager.AppPermission.check(
                this, allPermissionList!!.toTypedArray(), null, options,
                permissionHandler
            )
        }
        super.finish()
    }

    override fun finish() {
        permissionHandler = null
        super.finish()
    }
}*/
