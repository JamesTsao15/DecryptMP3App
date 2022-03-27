# DecryptMP3App

利用我上一隻加密程式所輸出之KEY的JPG檔，來作解密，之後交給mediaplayer播放解密後檔案

程式筆記:

1.讀取加密mp3位置:

註:需開寫入讀出權限 

android.Manifest.permission.READ_EXTERNAL_STORAGE

android.Manifest.permission.WRITE_EXTERNAL_STORAGE

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

2.mediaplayer.reset()使用時無效:

解決方案:先release在new一個新的

    mediaPlayer.stop()
    mediaPlayer.release()
    mediaPlayer= MediaPlayer()
    
3.mediaplayer播放歌曲非預期且出現error碼in state 4:

造成原因:startService()重複執行，導致mediaplayer已在背後運行，又重複送入檔案，
而在播放歌曲非預期為activity還未更新檔案service的thread就已經讀到item被點選。

4.在Activity停止Service:

    applicationContext.stopService(intent)
