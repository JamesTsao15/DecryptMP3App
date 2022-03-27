package com.example.decryptmp3app

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PlayerActivity : AppCompatActivity() {
    private lateinit var tv_MusicFileName:TextView
    private lateinit var btn_PlayOrPause:ImageButton
    private lateinit var tv_TotalTime:TextView
    private lateinit var tv_playingTime:TextView
    private lateinit var seekBar_CurrentProgress:SeekBar
    private lateinit var seekBar_VoiceControl: SeekBar
    private var IVAES:ByteArray= ByteArray(0)
    private var AESKey:ByteArray= ByteArray(0)
    private var isPlayingOrNot:Boolean=false
    private var isDecryptDone:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        tv_TotalTime=findViewById(R.id.textView_TotalTime)
        tv_playingTime=findViewById(R.id.textView_playing_time)
        tv_MusicFileName=findViewById(R.id.textView_MusicPlayerFileName)
        btn_PlayOrPause=findViewById(R.id.imageButton_PlayOrPause)
        seekBar_CurrentProgress=findViewById(R.id.seekBar_currentProgress)
        seekBar_VoiceControl=findViewById(R.id.seekBar_VoiceControl)
        isDecryptDone=false
        Log.e("JAMES","onCreate_playerActivity")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.e("JAMES","onRessume_playerActivity")
        val mp3FileInformation_pref: SharedPreferences =
            getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val player_editor=playerState_pref.edit()
        val musicName: String? =mp3FileInformation_pref.getString("MusicFileName","")
        val isStartTheService:Boolean=playerState_pref.getBoolean("isStartTheService",false)
        player_editor.putBoolean("isDecryptDone",isDecryptDone).commit()
        tv_MusicFileName.text=musicName
        val document_dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val encrypt_mp3_file=File(document_dir,"EncryptMP3")
        val music_path=encrypt_mp3_file.path+"/$musicName"
        val music_byteArray=File(music_path).readBytes()
        var isSeeking:Boolean=true
        openKEYFile()
        val decrypt_musicFile_ByteArray:ByteArray=DecryptByteArrayToMp3(IVAES,AESKey,music_byteArray)
        Log.e("JAMES","Decrypt:"+Arrays.toString(decrypt_musicFile_ByteArray))
        val intent= Intent(this,MediaPlayerService::class.java)
        val mp3File_byteArray_To_String=Base64.getEncoder().encodeToString(decrypt_musicFile_ByteArray)
        mp3FileInformation_pref.edit().putString("mp3File_byteArray_To_String",mp3File_byteArray_To_String).commit()
        isDecryptDone=true
        player_editor.putBoolean("isDecryptDone",isDecryptDone).commit()
        if(!isStartTheService)startService(intent)
        isPlayingOrNot=true
        player_editor.putBoolean("playStart",true).commit()

        btn_PlayOrPause.setOnClickListener {
            if(isPlayingOrNot==true){
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            else{
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_pause_24)
            }

            isPlayingOrNot= !isPlayingOrNot
            //Log.e("JAMES","isPlaying:"+isPlayingOrNot.toString())
            if(isPlayingOrNot==true)player_editor.putBoolean("playStart",true).commit()
            else{
                player_editor.putBoolean("playStop",true).commit()
            }
        }
        seekBar_VoiceControl.progress=60
        seekBar_VoiceControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var isSeekingVolume:Boolean=false
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isSeekingVolume=true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player_editor.putFloat("MusicVolume",seekBar!!.progress.toFloat())
                             .putBoolean("isSeekingVolume",true).commit()
                isSeekingVolume=false
            }

        })
        seekBar_CurrentProgress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player_editor.putInt("SeekBarCurrentPosition", seekBar!!.progress)
                    .putBoolean("isSeeking",isSeeking).commit()
                isSeeking = false
            }
        })
        val thread=Thread(
            Runnable {
                while(true){
                    runOnUiThread {
                        initTotaltime(playerState_pref.getInt("MusicFileTime",0))
                        playingTime(playerState_pref.getInt("MusicCurrentTime",0))
                    }
                    val isExitApp=playerState_pref.getBoolean("isExitApp",false)
                    seekBar_CurrentProgress.progress=playerState_pref.getInt("MusicCurrentTime",0)

                    if(isExitApp){
                        Log.e("JAMES","isExitApp")
                        player_editor.putBoolean("isExitApp",false).commit()
                        break
                    }
                    Thread.sleep(500)
                }
            }
        )
        thread.start()
    }
    private fun DecryptByteArrayToMp3(ivAes: ByteArray,Aeskey: ByteArray,Mp3ByteArray_Encrypt: ByteArray):ByteArray{
        try {
            val myAlgorithmParameterSpec: AlgorithmParameterSpec = IvParameterSpec(ivAes)
            val mySecretKeySpec= SecretKeySpec(Aeskey,"AES")
            val myCipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            myCipher.init(Cipher.DECRYPT_MODE,mySecretKeySpec,myAlgorithmParameterSpec)
            return myCipher.doFinal(Mp3ByteArray_Encrypt)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        return ByteArray(0)
    }
    private fun openKEYFile() {
        val document= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val key_File= File(document,"EncryptMP3_Key_Text")
        key_File.walk().forEach {
            if(it.isFile){
                if(it.name=="IVAES.jpg"){
                    IVAES= File(it.absolutePath).readBytes()
                }
                else if(it.name=="KEY.jpg") {
                    AESKey= File(it.absolutePath).readBytes()
                }
            }
        }
    }
    private fun initTotaltime(time:Int){
        var second:Int=time/1000
        var minute:Int=0
        seekBar_CurrentProgress.max=time
        if(second>60){
            minute=second/60
        }
        if(minute>=10){
            second=second-minute*60
            if(second>=10)tv_TotalTime.text="$minute:$second"
            else tv_TotalTime.text="$minute:0$second"
        }
        else{
            second=second-minute*60
            if(second>=10)tv_TotalTime.text="0$minute:$second"
            else tv_TotalTime.text="0$minute:0$second"
        }
    }
    private fun playingTime(time:Int){
        var totalsecond:Int=time/1000
        var second:Int=0
        var minute:Int=0
        var show:String=""
        minute=totalsecond/60
        second=totalsecond-minute*60
        //Log.e("JAMES","$minute:$second")
        if(minute>=10){
            if(second>=10)show="$minute:$second"
            else show="$minute:0$second"
        }
        else{
            if(second>=10)show="0$minute:$second"
            else show="0$minute:0$second"
        }
        runOnUiThread {
            tv_playingTime.text=show
        }
    }


}