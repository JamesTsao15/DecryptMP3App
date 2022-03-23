package com.example.decryptmp3app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_WRITE_AND_READ_PERMISSION=100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val readPermisson:String=android.Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission:String=android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        if(ActivityCompat.checkSelfPermission(this,readPermisson)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(readPermisson,writePermission),REQUEST_WRITE_AND_READ_PERMISSION)
        }
        else{
            openKEYFile()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && requestCode==REQUEST_WRITE_AND_READ_PERMISSION){
            val result=grantResults[0]
            if(result==PackageManager.PERMISSION_DENIED)
                finish()
            else{
                openKEYFile()
            }
        }
    }
}