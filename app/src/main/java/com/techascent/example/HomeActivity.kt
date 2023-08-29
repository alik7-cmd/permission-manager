package com.techascent.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.techascent.permissionmanager.PermissionListener
import com.techascent.permissionmanager.PermissionManager


class HomeActivity : AppCompatActivity() {


    private val listOfPermission =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btn_permission).setOnClickListener {
            takeMultiplePermission()
        }
    }

    private fun takeMultiplePermission() {
        PermissionManager.Builder()
            .enableLogging(true)
            .onRequestPermission(this,
                listOfPermission,
                null,
                null,
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
                }).enableLogging(true).build()
    }

    private fun takeSinglePermission() {
        PermissionManager.Builder().onRequestPermission(
            this,
            Manifest.permission.CAMERA,
            null,
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
            }).enableLogging(true).build()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivity(intent)
    }
}