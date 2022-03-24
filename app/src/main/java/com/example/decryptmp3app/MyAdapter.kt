package com.example.decryptmp3app

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyAdapter(context: Context,data:ArrayList<String>,private val layout:Int):
ArrayAdapter<String>(context,layout,data){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view=View.inflate(parent.context,layout,null)
        val item=getItem(position)?: return view
        val tv_musicName:TextView=view.findViewById(R.id.textView_musicName)
        tv_musicName.text=item
        return view
    }

}