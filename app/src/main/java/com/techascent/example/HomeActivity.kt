package com.techascent.example

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.techascent.permissionmanager.PermissionListener
import com.techascent.permissionmanager.PermissionManager

class HomeActivity : AppCompatActivity() {


    private val listOfPermission: Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btn_permission).setOnClickListener {
            //takeMultiplePermission()
        }
        //
    }

    private fun takeMultiplePermission(){
        PermissionManager.with(this, listOfPermission, null, null, object : PermissionListener(){
            override fun onGranted() {
                // Do whatever you want to do
            }

            override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                super.onDenied(context, listOfDeniedPermission)
                // Do whatever you want to do
            }

            override fun onBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onBlocked(context, listOfBlockedPermission)
            }
        })
    }

    private fun takeSinglePermission(){
        PermissionManager.with(this, Manifest.permission.CAMERA, null, object : PermissionListener(){
            override fun onGranted() {
                // Do whatever you want to do
            }

            override fun onDenied(context: Context, listOfDeniedPermission: List<String>) {
                super.onDenied(context, listOfDeniedPermission)
                // Do whatever you want to do
            }

            override fun onBlocked(
                context: Context,
                listOfBlockedPermission: List<String>
            ): Boolean {
                // Do whatever you want to do
                return super.onBlocked(context, listOfBlockedPermission)
            }

        })
    }
}