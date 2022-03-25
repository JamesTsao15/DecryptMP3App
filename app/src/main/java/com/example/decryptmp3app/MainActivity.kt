package com.example.decryptmp3app

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat
import java.io.File
import java.lang.Exception
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val REQUEST_WRITE_AND_READ_PERMISSION=100

    private lateinit var listView_musicList:ListView
    private var MusicArrayList:ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView_musicList=findViewById(R.id.listView_mp3File)
        val readPermisson:String=android.Manifest.permission.READ_EXTERNAL_STORAGE
        val writePermission:String=android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val player_editor=playerState_pref.edit()
        player_editor.putBoolean("isExitApp",false).commit()
        if(ActivityCompat.checkSelfPermission(this,readPermisson)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(readPermisson,writePermission),REQUEST_WRITE_AND_READ_PERMISSION)
        }
        else{
            MusicArrayList=readEncryptMp3List()
            listView_musicList.adapter=MyAdapter(this,MusicArrayList,R.layout.listview_mp3)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences:SharedPreferences=
            getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        listView_musicList.setOnItemClickListener { adapterView, view, i, l ->
            editor.putString("MusicFileName",MusicArrayList[i]).commit()
            val intent=Intent(this,PlayerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun readEncryptMp3List():ArrayList<String>{
        val document_dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val encrypt_mp3_file=File(document_dir,"EncryptMP3")
        val file_Name_arrayList=ArrayList<String>()
        encrypt_mp3_file.walk().forEach {
            if(it.isFile){
                file_Name_arrayList.add(it.name)
            }
        }
        return file_Name_arrayList
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
                MusicArrayList=readEncryptMp3List()
                listView_musicList.adapter=MyAdapter(this,MusicArrayList,R.layout.listview_mp3)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val player_editor=playerState_pref.edit()
        player_editor.putBoolean("isExitApp",true).commit()
    }
}