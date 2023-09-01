package com.techascent.permissionmanager

import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.techascent.permissionmanager.PermissionManager.Companion.log
import java.util.ArrayList

class PermissionActivity : AppCompatActivity() {

    private lateinit var mViewModel: PermissionViewModel

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        supportActionBar?.hide()
        supportActionBar?.title = ""
        setFinishOnTouchOutside(false)
        val intent = intent
        val bundle = intent.extras
        if (intent == null || !bundle?.containsKey(BUNDLE_PERMISSIONS)!!) {
            finish()
            return
        }

        mViewModel.allPermissions = bundle.getStringArrayList(BUNDLE_PERMISSIONS)
        mViewModel.options = bundle.getParcelable(BUNDLE_MESSAGES)
        if (mViewModel.options == null) {
            mViewModel.options = Options()
        }
        mViewModel.deniedPermissions = ArrayList()
        mViewModel.noRationaleList = ArrayList()
        var noRationale = true
        mViewModel.allPermissions?.forEach { permission ->
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                mViewModel.deniedPermissions!!.add(permission)
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false
                } else {
                    mViewModel.noRationaleList!!.add(permission)
                }
            }
        }
        if (mViewModel.deniedPermissions!!.isEmpty()) {
            grant()
            return
        }
        val rationale = bundle.getString(BUNDLE_RATIONALE)
        if (noRationale || TextUtils.isEmpty(rationale)) {
            log("No rationale.")
            requestPermissions(mViewModel.deniedPermissions?.toTypedArray() ?: emptyArray(), RC_PERMISSION)
        } else {
            log("Show rationale.")
            showRationale(rationale)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showRationale(rationale: String?) {
        val listener = DialogInterface.OnClickListener { dialog, which ->
            if (which == DialogInterface.BUTTON_POSITIVE) {
                requestPermissions(mViewModel.deniedPermissions?.toTypedArray() ?: emptyArray(), RC_PERMISSION)
            } else {
                deny()
            }
        }
        AlertDialog.Builder(this).setTitle(mViewModel.options!!.rationaleDialogTitle)
            .setMessage(rationale)
            .setPositiveButton(R.string.permission_manager_text_ok, listener)
            .setNegativeButton(R.string.permission_manager_text_cancel, listener)
            .setOnCancelListener { deny() }.create().show()
    }

    @SuppressLint("MissingSuperCall")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()) {
            deny()
        } else {
            mViewModel.deniedPermissions!!.clear()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    mViewModel.deniedPermissions!!.add(permissions[i])
                }
            }
            if (mViewModel.deniedPermissions!!.size == 0) {
                log("Just allowed.")
                grant()
            } else {
                val blockedList = ArrayList<String>() //set not to ask again.
                val justBlockedList = ArrayList<String>() //just set not to ask again.
                val justDeniedList = ArrayList<String>()
                mViewModel.deniedPermissions?.forEach {permission ->
                    if (shouldShowRequestPermissionRationale(permission)) {
                        justDeniedList.add(permission)
                    } else {
                        blockedList.add(permission)
                        if (!mViewModel.noRationaleList!!.contains(permission)) {
                            justBlockedList.add(permission)
                        }
                    }
                }
                if (justBlockedList.size > 0) { //checked don't ask again for at least one.
                    val pelicanPermissionHandler = permissionListener
                    finish()
                    pelicanPermissionHandler?.onJustBlocked(
                        applicationContext, justBlockedList,
                        mViewModel.deniedPermissions!!
                    )
                } else if (justDeniedList.size > 0) { //clicked deny for at least one.
                    deny()
                } else { //unavailable permissions were already set not to ask again.
                    if (permissionListener != null &&
                        !permissionListener!!.onBlocked(
                            applicationContext, blockedList
                        )
                    ) {
                        sendToSettings()
                    } else finish()
                }
            }
        }
    }

    private fun sendToSettings() {
        if (!mViewModel.options!!.sendBlockedToSettings) {
            deny()
            return
        }
        log("Ask to go to settings.")
        AlertDialog.Builder(this).setTitle(mViewModel.options!!.settingsDialogTitle)
            .setMessage(mViewModel.options!!.settingsDialogMessage)
            .setPositiveButton(mViewModel.options!!.settingsText) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )

                startActivityForResult(intent, RC_SETTINGS)
            }
            .setNegativeButton(R.string.permission_manager_text_cancel) { _, _ -> deny() }
            .setOnCancelListener { deny() }.create().show()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SETTINGS && permissionListener != null) {
            PermissionManager().with(
                this, mViewModel.allPermissions!!.toTypedArray(), null, mViewModel.options,
                permissionListener
            )
        }
        super.finish()
    }

    override fun finish() {
        permissionListener = null
        super.finish()
    }

    private fun deny() {
        val pelicanPermissionHandler = permissionListener
        finish()
        pelicanPermissionHandler?.onDenied(applicationContext, mViewModel.deniedPermissions!!)
    }

    private fun grant() {
        val pelicanPermissionHandler = permissionListener
        finish()
        pelicanPermissionHandler?.onGranted()
    }

    companion object {
        private const val RC_SETTINGS = 5599
        private const val RC_PERMISSION = 5717
        const val BUNDLE_PERMISSIONS = "BUNDLE_PERMISSIONS"
        const val BUNDLE_RATIONALE = "BUNDLE_RATIONALE"
        const val BUNDLE_MESSAGES = "BUNDLE_MESSAGES"
        var permissionListener: PermissionListener? = null
        fun onNewIntent(
            context: Context?,
            permissionList: ArrayList<String>?,
            options: Options?,
            rationale: String?
        ): Intent {
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_MESSAGES, options)
            bundle.putStringArrayList(BUNDLE_PERMISSIONS, permissionList)
            bundle.putString(BUNDLE_RATIONALE, rationale)
            val intent = Intent(context, PermissionActivity::class.java).apply {
                putExtras(bundle)
            }
            if (options != null && options.createNewTask) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }
}