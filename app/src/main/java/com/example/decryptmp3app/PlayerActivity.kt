package com.example.decryptmp3app

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
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
    private lateinit var seekBar_CurrentProgress:SeekBar
    private lateinit var seekBar_VoiceControl: SeekBar
    private var IVAES:ByteArray= ByteArray(0)
    private var AESKey:ByteArray= ByteArray(0)
    private var isPlayingOrNot:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        tv_MusicFileName=findViewById(R.id.textView_MusicPlayerFileName)
        btn_PlayOrPause=findViewById(R.id.imageButton_PlayOrPause)
        seekBar_CurrentProgress=findViewById(R.id.seekBar_currentProgress)
        seekBar_VoiceControl=findViewById(R.id.seekBar_VoiceControl)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val mp3FileInformation_pref: SharedPreferences =
            getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val playerState_pref=getSharedPreferences("PlayerState", MODE_PRIVATE)
        val player_editor=playerState_pref.edit()
        val musicName: String? =mp3FileInformation_pref.getString("MusicFileName","")
        tv_MusicFileName.text=musicName
        val document_dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val encrypt_mp3_file=File(document_dir,"EncryptMP3")
        val music_path=encrypt_mp3_file.path+"/$musicName"
        val music_byteArray=File(music_path).readBytes()
        openKEYFile()
        val decrypt_musicFile_ByteArray:ByteArray=DecryptByteArrayToMp3(IVAES,AESKey,music_byteArray)
        val intent= Intent(this,MediaPlayerService::class.java)
        val mp3File_byteArray_To_String=Base64.getEncoder().encodeToString(decrypt_musicFile_ByteArray)
        mp3FileInformation_pref.edit().putString("mp3File_byteArray_To_String",mp3File_byteArray_To_String).commit()
        startService(intent)
        isPlayingOrNot=true
        player_editor.putBoolean("isPlayingOrNot",isPlayingOrNot).commit()
        btn_PlayOrPause.setOnClickListener {
            if(isPlayingOrNot==true){
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            else{
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_pause_24)
            }

            isPlayingOrNot= !isPlayingOrNot
            //Log.e("JAMES","isPlaying:"+isPlayingOrNot.toString())
            player_editor.putBoolean("isPlayingOrNot",isPlayingOrNot).commit()
        }
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

}