package com.paperterritory.app

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*\nimport android.graphics.drawable.GradientDrawable

class MainActivity:Activity(){
 private val n=NetworkClient()
 private lateinit var economy:Economy

 private fun base()=LinearLayout(this).apply{
  orientation=LinearLayout.VERTICAL
  gravity=Gravity.CENTER
  setPadding(42,42,42,42)
  setBackgroundColor(Color.rgb(22,103,240))
 }
 private fun b(t:String,a:()->Unit)=Button(this).apply{
  text=t;setOnClickListener{a()};textSize=16f;isAllCaps=false
  setTextColor(Color.WHITE);background=GradientDrawable().apply{setColor(Color.rgb(39,75,188));cornerRadius=28f;setStroke(2,Color.argb(70,255,255,255))}
  layoutParams=LinearLayout.LayoutParams(-1,64).apply{setMargins(0,9,0,9)}
 }
 override fun onCreate(x:Bundle?){super.onCreate(x);economy=Economy(this);menu()}
 private fun menu(){
  val v=base()
  v.addView(TextView(this).apply{
   text="TERRITORY";textSize=38f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;setShadowLayer(4f,0f,3f,0x66000000)
  },LinearLayout.LayoutParams(-1,70))
  v.addView(TextView(this).apply{
   text="Claim ground. Cut trails. Survive.";setTextColor(Color.rgb(205,220,255));gravity=Gravity.CENTER
  },LinearLayout.LayoutParams(-1,45))
  val wallet=TextView(this).apply{
   text="🪙 ${economy.coins}     💎 ${economy.gems}"
   textSize=18f;setTextColor(Color.WHITE);gravity=Gravity.CENTER
  }
  v.addView(wallet,LinearLayout.LayoutParams(-1,54))\n  v.addView(TextView(this).apply{text="★  HEROES   •   MYSTERY CRATE";textSize=14f;setTextColor(Color.rgb(255,235,80));gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(-1,38))
  v.addView(b("PLAY ONLINE"){play(false,"")})
  v.addView(b("CREATE PRIVATE ROOM"){create()})
  v.addView(b("JOIN PRIVATE ROOM"){join()})
  v.addView(b("CHARACTERS & MYSTERY CRATE"){shop()})
  v.addView(b("HOW TO PLAY"){help()})
  setContentView(v)
 }
 private fun shop(){setContentView(ShopView(this,economy){menu()})}
 private fun play(p:Boolean,r:String){
  val holder=arrayOfNulls<GameView>(1)
  n.connect("Player"+(100..999).random(),r,p,{state->runOnUiThread{
   if(holder[0]==null){holder[0]=GameView(this,n,economy,state);setContentView(holder[0])}
   else holder[0]!!.updateState(state)
  }},{m->runOnUiThread{Toast.makeText(this,m,Toast.LENGTH_LONG).show();menu()}})
 }
 private fun create(){
  val c="ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toList().shuffled().take(5).joinToString("")
  AlertDialog.Builder(this).setTitle("Private room").setMessage("Room code: $c\nShare this code with friends.")
   .setPositiveButton("ENTER"){_,_->play(true,c)}.setNegativeButton("CANCEL",null).show()
 }
 private fun join(){
  val i=EditText(this).apply{hint="5-character room code";textSize=20f;gravity=Gravity.CENTER}
  AlertDialog.Builder(this).setTitle("Join private room").setView(i)
   .setPositiveButton("JOIN"){_,_->val c=i.text.toString().trim().uppercase();if(c.length!=5)Toast.makeText(this,"Room codes are 5 characters.",Toast.LENGTH_SHORT).show()else play(true,c)}
   .setNegativeButton("CANCEL",null).show()
 }
 private fun help(){
  AlertDialog.Builder(this).setTitle("How to play")
   .setMessage("Drag anywhere to steer. Leave your territory to draw a trail, then return to your territory to fill the enclosed area. Crossing an exposed enemy trail eliminates them.\n\nCharacters are cosmetic collection items. Mystery Crates are the only crate type: spend coins or gems to receive one random character. Duplicate characters convert into 250 coins. There are no character upgrades.")
   .setPositiveButton("OK",null).show()
 }
}
