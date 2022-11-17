package com.techascent.permissionmanager

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class HomeActivity : AppCompatActivity() {


    private val listOfPermission: Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btn_permission).setOnClickListener {
            takeMultiplePermission()
        }
        //
    }


    private fun takeMultiplePermission(){
        PermissionManager.check(this,listOfPermission , null, null, object : PermissionHandler(){
            override fun onPermissionGranted() {
                Toast.makeText(this@HomeActivity, "Granted", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(
                context: Context,
                listOfDeniedPermission: List<String>
            ) {
                super.onPermissionDenied(context, listOfDeniedPermission)
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

    }

    private fun takeSinglePermission(){
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
                super.onPermissionDenied(context, listOfDeniedPermission)
            }

        })
    }
}