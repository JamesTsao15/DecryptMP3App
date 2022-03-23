package com.example.decryptmp3app

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_WRITE_PERMISSION=100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestReadPermission()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openKEYFile()
        }
    }
    private fun requestReadPermission(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            val write_permission=android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            val read_permission=android.Manifest.permission.READ_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(this,arrayOf(write_permission,read_permission), REQUEST_WRITE_PERMISSION);
        }
        else {
            openKEYFile();
        }
    }

    private fun openKEYFile() {
        val document=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val key_File= File(document,"EncryptMP3_Key_Text")
        Log.e("JAMES",key_File.path)
        Log.e("JAMES",key_File.isDirectory.toString())
        Log.e("JAMES",key_File.exists().toString())
        Log.e("JAMES", Arrays.toString(key_File.listFiles()))
    }

}