package com.example.decryptmp3app

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MediaPlayerService : Service() {
    private lateinit var mediaPlayer:MediaPlayer
    override fun onCreate() {
        Log.e("JAMES","Service_onCreate")
        mediaPlayer= MediaPlayer()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mp3File_pref=getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val mp3File_Base64String=mp3File_pref.getString("mp3File_byteArray_To_String","")
        val mp3File_byteArray=Base64.getDecoder().decode(mp3File_Base64String)
        Log.e("JAMES",Arrays.toString(mp3File_byteArray))
        PlayAudioByByteArray(mp3File_byteArray)
        val thread=Thread(Runnable {
            while(true){
                val isPlayingOrNot=playerState_pref.getBoolean("isPlayingOrNot",true)
                val isExitApp=playerState_pref.getBoolean("isExitApp",false)
                Log.e("JAMES",isPlayingOrNot.toString())
                if(isPlayingOrNot)mediaPlayer.start()
                else mediaPlayer.pause()
                if(isExitApp){
                    Log.e("JAMES","isExitApp")
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    break
                }
                Thread.sleep(1)
            }
        })
        thread.start()
        return START_STICKY
    }
    override fun onBind(intent: Intent): IBinder ?=null
    private fun PlayAudioByByteArray(mp3ByteArray: ByteArray){
        try {
            val tempMp3= File.createTempFile("playing",".mp3")
            tempMp3.deleteOnExit()
            val fos: FileOutputStream = FileOutputStream(tempMp3)
            fos.write(mp3ByteArray)
            fos.close()
            val fis: FileInputStream = FileInputStream(tempMp3)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()

        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        Log.e("JAMES","destroy")
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}