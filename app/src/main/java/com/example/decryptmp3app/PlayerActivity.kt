package com.example.decryptmp3app

import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
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
    private var isPlayingOrNot:Boolean=false
    private var IVAES:ByteArray= ByteArray(0)
    private var AESKey:ByteArray= ByteArray(0)
    private lateinit var  mediaPlayer:MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        tv_MusicFileName=findViewById(R.id.textView_MusicPlayerFileName)
        btn_PlayOrPause=findViewById(R.id.imageButton_PlayOrPause)
        seekBar_CurrentProgress=findViewById(R.id.seekBar_currentProgress)
        seekBar_VoiceControl=findViewById(R.id.seekBar_VoiceControl)

    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("Mp3FileInformation", MODE_PRIVATE)
        val musicName: String? =sharedPreferences.getString("MusicFileName","")

        tv_MusicFileName.text=musicName
        val document_dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val encrypt_mp3_file=File(document_dir,"EncryptMP3")
        val music_path=encrypt_mp3_file.path+"/$musicName"
        val music_byteArray=File(music_path).readBytes()
        openKEYFile()
        val decrypt_musicFile_ByteArray:ByteArray=DecryptByteArrayToMp3(IVAES,AESKey,music_byteArray)
        PlayAudioByByteArray(decrypt_musicFile_ByteArray)
        btn_PlayOrPause.setOnClickListener {
            if(isPlayingOrNot==true){
                mediaPlayer.pause()
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                isPlayingOrNot=false
            }
            else{
                mediaPlayer.start()
                btn_PlayOrPause.setImageResource(R.drawable.ic_baseline_pause_24)
                isPlayingOrNot=true
            }
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
    private fun PlayAudioByByteArray(mp3ByteArray: ByteArray){
        try {
            val tempMp3=File.createTempFile("playing",".mp3")
            tempMp3.deleteOnExit()
            val fos:FileOutputStream= FileOutputStream(tempMp3)
            fos.write(mp3ByteArray)
            fos.close()
            val fis:FileInputStream= FileInputStream(tempMp3)
            mediaPlayer= MediaPlayer()
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlayingOrNot=true
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
}