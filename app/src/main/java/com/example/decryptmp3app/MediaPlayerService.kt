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
import java.lang.IllegalStateException
import java.util.*

class MediaPlayerService : Service() {
    private lateinit var mediaPlayer:MediaPlayer
    private lateinit var fis:FileInputStream
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
                var playStart: Boolean =playerState_pref.getBoolean("playStart", false)
                var playStop:Boolean=playerState_pref.getBoolean("playStop",false)
                val isExitApp=playerState_pref.getBoolean("isExitApp",false)

                if(playStart==true) {
                    Log.e("JAMES","inLoop_playStart")
                    if(!mediaPlayer.isPlaying)mediaPlayer.start()
                    playerState_pref.edit().putBoolean("playStart",false).commit()
                    playStart=false
                }
                if(playStop==true){
                    Log.e("JAMES","inLoop_playPause")
                    if(mediaPlayer.isPlaying)mediaPlayer.pause()
                    playerState_pref.edit().putBoolean("playStop",false).commit()
                    playStop=false

                }
                if(isExitApp){
                    Log.e("JAMES","isExitApp")
                    if(mediaPlayer.isPlaying)mediaPlayer.stop()
                    mediaPlayer.release()
                    fis.close()
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
            fis = FileInputStream(tempMp3)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()

        }catch (e: IOException){
            e.printStackTrace()
        }catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        Log.e("JAMES","destroy")
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}