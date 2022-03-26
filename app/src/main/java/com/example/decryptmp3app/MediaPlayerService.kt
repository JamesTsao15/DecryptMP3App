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
    private var isPrepareDone:Boolean=false
    override fun onCreate() {
        Log.e("JAMES","Service_onCreate")
        mediaPlayer= MediaPlayer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val playerState_edtior=playerState_pref.edit()
        initPlayerFile()
        val thread=Thread(Runnable {
            while(true){
                val playStart: Boolean =playerState_pref.getBoolean("playStart", false)
                val playStop:Boolean=playerState_pref.getBoolean("playStop",false)
                val isExitApp:Boolean=playerState_pref.getBoolean("isExitApp",false)
                val isSeeking:Boolean=playerState_pref.getBoolean("isSeeking",false)
                val isItemClick:Boolean=playerState_pref.getBoolean("isItemClick",false)
                val musicVolume:Float=playerState_pref.getFloat("MusicVolume",0F)
                AdjustMusicVolume(musicVolume)
                playerState_edtior.putInt("MusicCurrentTime",mediaPlayer.currentPosition).commit()
                if(isItemClick){
                    Log.e("JAMES","isItemClick")
                    initPlayerFile()
                    playerState_edtior.putBoolean("isItemClick",false).commit()
                }
                if(playStart && isPrepareDone) {
                    if(!mediaPlayer.isPlaying)mediaPlayer.start()
                    playerState_edtior.putBoolean("playStart",false).commit()
                }
                if(playStop && isPrepareDone){
                    if(mediaPlayer.isPlaying)mediaPlayer.pause()
                    playerState_edtior.putBoolean("playStop",false).commit()

                }
                if(isExitApp){
                    Log.e("JAMES","isExitApp")
                    playerState_edtior.putBoolean("isExitApp",false).commit()
                    break
                }
                if(isSeeking && isPrepareDone){
                    val musicSecond=playerState_pref.getInt("SeekBarCurrentPosition",0)
                    mediaPlayer.seekTo(musicSecond)
                    playerState_edtior.putBoolean("isSeeking",false).commit()
                }
                Thread.sleep(1)
            }
        })
        thread.start()
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent): IBinder ?=null
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initPlayerFile(){
        val mp3File_pref=getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val mp3File_Base64String=mp3File_pref.getString("mp3File_byteArray_To_String","")
        val mp3File_byteArray=Base64.getDecoder().decode(mp3File_Base64String)
        Log.e("JAMES",Arrays.toString(mp3File_byteArray))
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.reset()
            isPrepareDone=false
        }
        PlayAudioByByteArray(mp3File_byteArray)
    }
    private fun PlayAudioByByteArray(mp3ByteArray: ByteArray){
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val playerState_edtior=playerState_pref.edit()
        try {
            val tempMp3= File.createTempFile("playing",".mp3")
            tempMp3.deleteOnExit()
            val fos: FileOutputStream = FileOutputStream(tempMp3)
            fos.write(mp3ByteArray)
            fos.close()
            fis = FileInputStream(tempMp3)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                val musicTotalTime=mediaPlayer.duration
                playerState_edtior.putInt("MusicFileTime",musicTotalTime).commit()
                mediaPlayer.start()
                isPrepareDone=true
            }
        }catch (e: IOException){
            e.printStackTrace()
        }catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }
    private fun AdjustMusicVolume(value:Float){
        mediaPlayer.setVolume(value/100f,value/100f)
    }
    override fun onDestroy() {
        Log.e("JAMES","destroy")
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}