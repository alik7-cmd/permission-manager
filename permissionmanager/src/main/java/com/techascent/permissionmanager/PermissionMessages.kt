package com.techascent.permissionmanager

import java.io.Serializable


data class PermissionMessages(

    var settingsText: String = "Settings",
    var rationaleDialogTitle: String = "Permissions Required",
    var settingsDialogTitle: String = "Permissions Required",
    var settingsDialogMessage: String = "Required permission(s) have been set"
        .plus(" not to ask again! Please provide them from settings."),
    var sendBlockedToSettings: Boolean = true,
    var createNewTask: Boolean = false
) : Serializable
