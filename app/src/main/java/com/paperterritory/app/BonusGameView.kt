package com.paperterritory.app
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random
class BonusGameView(c:Context,private val economy:Economy,private val onFinished:()->Unit):View(c){
 private data class Prize(var x:Float,var y:Float,val gem:Boolean,val value:Int,var taken:Boolean=false)
 private val bg=Paint(1);private val text=Paint(1).apply{typeface=Typeface.DEFAULT_BOLD};private val prizes=mutableListOf<Prize>()
 private var playerX=.5f;private var playerY=.72f;private var start=System.currentTimeMillis();private var finished=false;private var coins=0;private var gems=0
 init{repeat(16){val gem=it%5==0;prizes+=Prize(.08f+Random.nextFloat()*.84f,.22f+Random.nextFloat()*.62f,gem,if(gem)1 else 50+Random.nextInt(101))}}
 override fun onDraw(c:Canvas){val left=(6000L-(System.currentTimeMillis()-start)).coerceAtLeast(0L);c.drawColor(Color.rgb(19,103,239));text.color=Color.WHITE;text.textAlign=Paint.Align.CENTER;text.textSize=27f;c.drawText("100% BONUS GAME!",width/2f,55f,text);text.textSize=15f;c.drawText("COLLECT AS MUCH AS YOU CAN",width/2f,82f,text);bg.color=Color.argb(65,255,255,255);c.drawRoundRect(24f,98f,width-24f,110f,8f,8f,bg);bg.color=Color.WHITE;c.drawRoundRect(24f,98f,24f+(width-48f)*(left/6000f),110f,8f,8f,bg);text.textAlign=Paint.Align.LEFT;text.textSize=16f;c.drawText("🪙 $coins     💎 $gems",24f,140f,text);for(q in prizes.filter{!it.taken}){if(q.gem){bg.color=Color.rgb(177,95,255);val x=q.x*width;val y=q.y*height;val path=Path().apply{moveTo(x,y-8);lineTo(x+10,y);lineTo(x,y+12);lineTo(x-10,y);close()};c.drawPath(path,bg)}else{bg.color=Color.rgb(255,201,48);c.drawCircle(q.x*width,q.y*height,13f,bg);text.color=Color.rgb(145,91,0);text.textSize=11f;text.textAlign=Paint.Align.CENTER;c.drawText("C",q.x*width,q.y*height+4f,text)}};val px=playerX*width;val py=playerY*height;bg.color=Color.argb(65,255,255,255);c.drawCircle(px,py,25f,bg);bg.color=Color.WHITE;c.drawCircle(px,py,12f,bg);bg.color=Color.rgb(44,84,220);c.drawCircle(px,py,7f,bg);text.color=Color.WHITE;text.textSize=13f;text.textAlign=Paint.Align.CENTER;c.drawText("${ceil(left/1000f).toInt()}s",width/2f,height-28f,text);if(left<=0L&&!finished){finished=true;economy.addCoins(coins);economy.addGems(gems);postDelayed(onFinished,700)}else if(!finished)postInvalidateDelayed(16)}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(finished)return true;when(e.actionMasked){MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE->{playerX=(e.x/width).coerceIn(.03f,.97f);playerY=(e.y/height).coerceIn(.18f,.88f);collect();invalidate();return true}};return true}
 private fun collect(){for(q in prizes){if(q.taken)continue;if(hypot(q.x-playerX,q.y-playerY)<.055f){q.taken=true;if(q.gem)gems+=q.value else coins+=q.value}}}
}