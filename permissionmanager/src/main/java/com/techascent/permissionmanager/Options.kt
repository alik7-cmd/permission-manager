package com.techascent.permissionmanager

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Options(

    var settingsText: String? = "Settings",
    var rationaleDialogTitle: String? = "Permissions Required",
    var settingsDialogTitle: String? = "Permissions Required",
    var settingsDialogMessage: String? = "Requested permission(s) have been set"
        .plus(" not to ask again. Please provide them from settings."),
    var sendBlockedToSettings: Boolean = true,
    var createNewTask: Boolean = false
) : Parcelable
