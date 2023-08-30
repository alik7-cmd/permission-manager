package com.techascent.permissionmanager

import androidx.lifecycle.ViewModel
import java.util.ArrayList

class PermissionViewModel : ViewModel() {

    var allPermissions: ArrayList<String>? = null
    var deniedPermissions: ArrayList<String>? = null
    var noRationaleList: ArrayList<String>? = null

    var options: Options? = null
}