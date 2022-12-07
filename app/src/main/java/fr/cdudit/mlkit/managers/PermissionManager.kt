package fr.cdudit.mlkit.managers

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionManager {
    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}