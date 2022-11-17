package com.techascent.permissionmanager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PermissionActivity extends AppCompatActivity {

    private static final int RC_SETTINGS = 6739;
    private static final int RC_PERMISSION = 6937;

    static final String EXTRA_PERMISSIONS = "permissions";
    static final String EXTRA_RATIONALE = "rationale";
    static final String EXTRA_OPTIONS = "options";

    static PermissionHandler permissionHandler;

    private ArrayList<String> allPermissions, deniedPermissions, noRationaleList;
    private PermissionMessages permissionMessages;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_PERMISSIONS)) {
            finish();
            return;
        }

        getWindow().setStatusBarColor(0);
        allPermissions = (ArrayList<String>) intent.getSerializableExtra(EXTRA_PERMISSIONS);
        permissionMessages = (PermissionMessages) intent.getSerializableExtra(EXTRA_OPTIONS);
        if (permissionMessages == null) {
            permissionMessages = new PermissionMessages();
        }
        deniedPermissions = new ArrayList<>();
        noRationaleList = new ArrayList<>();

        boolean noRationale = true;
        for (String permission : allPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
                if (shouldShowRequestPermissionRationale(permission)) {
                    noRationale = false;
                } else {
                    noRationaleList.add(permission);
                }
            }
        }

        if (deniedPermissions.isEmpty()) {
            grant();
            return;
        }

        String rationale = intent.getStringExtra(EXTRA_RATIONALE);
        if (noRationale || TextUtils.isEmpty(rationale)) {
            AppPermission.log("No rationale.");
            requestPermissions(toArray(deniedPermissions), RC_PERMISSION);
        } else {
            AppPermission.log("Show rationale.");
            showRationale(rationale);
        }
    }

    private void showRationale(String rationale) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    requestPermissions(toArray(deniedPermissions), RC_PERMISSION);
                } else {
                    deny();
                }
            }
        };
        new AlertDialog.Builder(this).setTitle(permissionMessages.getRationaleDialogTitle())
                .setMessage(rationale)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        deny();
                    }
                }).create().show();
    }

    @SuppressLint("MissingSuperCall")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length == 0) {
            deny();
        } else {
            deniedPermissions.clear();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.size() == 0) {
                AppPermission.log("Just allowed.");
                grant();
            } else {
                ArrayList<String> blockedList = new ArrayList<>(); //set not to ask again.
                ArrayList<String> justBlockedList = new ArrayList<>(); //just set not to ask again.
                ArrayList<String> justDeniedList = new ArrayList<>();
                for (String permission : deniedPermissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        justDeniedList.add(permission);
                    } else {
                        blockedList.add(permission);
                        if (!noRationaleList.contains(permission)) {
                            justBlockedList.add(permission);
                        }
                    }
                }

                if (justBlockedList.size() > 0) { //checked don't ask again for at least one.
                    PermissionHandler pelicanPermissionHandler = PermissionActivity.permissionHandler;
                    finish();
                    if (pelicanPermissionHandler != null) {
                        pelicanPermissionHandler.onJustBlocked(getApplicationContext(), justBlockedList,
                                deniedPermissions);
                    }

                } else if (justDeniedList.size() > 0) { //clicked deny for at least one.
                    deny();

                } else { //unavailable permissions were already set not to ask again.
                    if (permissionHandler != null &&
                            !permissionHandler.onPermissionBlocked(getApplicationContext(), blockedList)) {
                        sendToSettings();

                    } else finish();
                }
            }
        }
    }

    private void sendToSettings() {
        if (!permissionMessages.getSendBlockedToSettings()) {
            deny();
            return;
        }
        AppPermission.log("Ask to go to settings.");
        new AlertDialog.Builder(this).setTitle(permissionMessages.getSettingsDialogTitle())
                .setMessage(permissionMessages.getSettingsDialogMessage())
                .setPositiveButton(permissionMessages.getSettingsText(), new DialogInterface.OnClickListener() {
                    @Override
                    @SuppressWarnings("InlinedAPI")
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        startActivityForResult(intent, RC_SETTINGS);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deny();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        deny();
                    }
                }).create().show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SETTINGS && permissionHandler != null) {
            AppPermission.check(this, toArray(allPermissions), null, permissionMessages,
                    permissionHandler);
        }
        // super, because overridden method will make the handler null, and we don't want that.
        super.finish();
    }

    private String[] toArray(ArrayList<String> arrayList) {
        int size = arrayList.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = arrayList.get(i);
        }
        return array;
    }

    @Override
    public void finish() {
        permissionHandler = null;
        super.finish();
    }

    private void deny() {
        PermissionHandler pelicanPermissionHandler = PermissionActivity.permissionHandler;
        finish();
        if (pelicanPermissionHandler != null) {
            pelicanPermissionHandler.onPermissionDenied(getApplicationContext(), deniedPermissions);
        }
    }

    private void grant() {
        PermissionHandler pelicanPermissionHandler = PermissionActivity.permissionHandler;
        finish();
        if (pelicanPermissionHandler != null) {
            pelicanPermissionHandler.onPermissionGranted();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        allPermissions = null;
        deniedPermissions = null;
        noRationaleList = null;
    }

    public static Intent onNewIntent(Context context, ArrayList<String> permissionList, PermissionMessages permissionMessages, String rationale){
        Intent intent = new Intent(context, PermissionActivity.class)
                .putExtra(EXTRA_PERMISSIONS, permissionList)
                .putExtra(EXTRA_RATIONALE, rationale)
                .putExtra(EXTRA_OPTIONS, permissionMessages);
        if (permissionMessages != null && permissionMessages.getCreateNewTask()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }
}
