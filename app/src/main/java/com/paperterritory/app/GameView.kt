package com.paperterritory.app

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

class GameView(context:Context,private val network:NetworkClient,private val economy:Economy,private var state:GameState):View(context){
 private val fill=Paint(1);private val line=Paint(1).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND}
 private val text=Paint(1).apply{typeface=Typeface.create("sans",Typeface.BOLD)}
 private var lastKills=-1;private var toastUntil=0L;private var toastText="";private var touchX=0f;private var touchY=0f;private var lastSend=0L;private var pulse=0f;private var steering=false;private var lastArea=-1;private var captureUntil=0L
 fun isSelf(id:String)=state.selfId==id
 fun bonusAllowed()=state.mode=="standard"&&!state.privateRoom
 fun updateState(v:GameState){
  val me=v.players.firstOrNull{it.id==v.selfId}
  if(me!=null){
   if(lastKills<0)lastKills=me.kills
   if(me.kills>lastKills){val count=me.kills-lastKills;repeat(count){economy.rewardKill()};toastText="+"+(count*15)+" COINS";toastUntil=System.currentTimeMillis()+1200L}
   if(lastArea>=0&&me.area>lastArea+10)captureUntil=System.currentTimeMillis()+500L
   lastKills=me.kills;lastArea=me.area
  };state=v;invalidate()
 }
 override fun onDraw(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();c.drawColor(Color.rgb(242,244,248))
  val hud=72f;val scale=min((w-24f)/state.width,(h-hud-20f)/state.height)*.96f;val aw=state.width*scale;val ah=state.height*scale;val left=(w-aw)/2f;val top=hud+(h-hud-ah)/2f
  fun sx(v:Float)=left+v*scale;fun sy(v:Float)=top+v*scale
  fill.color=Color.argb(28,23,34,55);c.drawRoundRect(left+3,top+5,left+aw+3,top+ah+5,18f,18f,fill);fill.color=Color.WHITE;c.drawRoundRect(left,top,left+aw,top+ah,18f,18f,fill)
  val players=state.players.associateBy{it.id}
  for((key,owner)in state.cells){val q=key.split(',');val gx=q.getOrNull(0)?.toIntOrNull()?:continue;val gy=q.getOrNull(1)?.toIntOrNull()?:continue;val a=players[owner]?:continue;val col=a.color;fill.color=Color.argb(218,Color.red(col),Color.green(col),Color.blue(col));c.drawRect(sx(gx*4f),sy(gy*4f),sx((gx+1)*4f),sy((gy+1)*4f),fill)}
  line.color=Color.argb(20,80,90,110);line.strokeWidth=1f;for(x in 0..state.width step 20)c.drawLine(sx(x.toFloat()),top,sx(x.toFloat()),top+ah,line);for(y in 0..state.height step 20)c.drawLine(left,sy(y.toFloat()),left+aw,sy(y.toFloat()),line)
  line.color=Color.rgb(194,199,208);line.strokeWidth=2.5f;c.drawRoundRect(left,top,left+aw,top+ah,18f,18f,line)
  for(a in state.players.filter{it.alive&&it.trail.size>1}){
   val path=Path();path.moveTo(sx(a.trail[0].first),sy(a.trail[0].second))
   for(i in 1 until a.trail.size){val u=a.trail[i-1];val v=a.trail[i];path.lineTo(sx((u.first+v.first)/2f),sy((u.second+v.second)/2f))}
   val col=a.color;line.color=Color.argb(55,Color.red(col),Color.green(col),Color.blue(col));line.strokeWidth=15f;c.drawPath(path,line);line.color=col;line.strokeWidth=7f;c.drawPath(path,line);line.color=Color.argb(170,255,255,255);line.strokeWidth=1.4f;c.drawPath(path,line)
  }
  pulse+=.08f
  for(a in state.players.filter{it.alive}){
   val x=sx(a.x);val y=sy(a.y);val col=a.color;fill.color=Color.argb(38,Color.red(col),Color.green(col),Color.blue(col));c.drawCircle(x,y,19f,fill);fill.color=Color.argb(70,30,38,55);c.drawOval(x-10,y+7,x+10,y+14,fill);drawCharacter(c,x,y,13f,a.character,col)
   if(a.id==state.selfId){line.color=Color.WHITE;line.strokeWidth=3f;c.drawCircle(x,y,18f+sin(pulse)*2f,line)}
  }
  fill.color=Color.WHITE;c.drawRoundRect(10f,10f,w-10f,62f,19f,19f,fill);text.textAlign=Paint.Align.LEFT;text.color=Color.rgb(29,35,46);text.textSize=17f;c.drawText(if(state.mode=="duel")"1V1 DUEL" else "TERRITORY",26f,33f,text)
  val me=state.players.firstOrNull{it.id==state.selfId};val total=(state.width/4f)*(state.height/4f);val pct=if(total>0)((me?.area?:0)/total*100f).roundToInt()else 0
  text.textSize=10f;text.typeface=Typeface.DEFAULT;text.color=Color.rgb(105,113,128);c.drawText("AREA $pct%   •   KILLS "+(me?.kills?:0),26f,51f,text);text.textAlign=Paint.Align.RIGHT;c.drawText(if(state.room.isBlank())"PUBLIC" else "PRIVATE  "+state.room,w-26f,32f,text);c.drawText(state.players.count{it.alive}.toString()+" ALIVE",w-26f,50f,text);text.typeface=Typeface.create("sans",Typeface.BOLD)
  val rows=min(5,state.players.size);fill.color=Color.argb(242,255,255,255);c.drawRoundRect(14f,82f,214f,92f+rows*24f,16f,16f,fill)
  state.players.sortedByDescending{it.area}.take(5).forEachIndexed{i,a->{text.textAlign=Paint.Align.LEFT;text.textSize=11f;text.color=if(a.id==state.selfId)a.color else Color.rgb(56,62,72);c.drawText((i+1).toString()+". "+a.name.take(12),26f,108f+i*24f,text);text.textAlign=Paint.Align.RIGHT;text.color=Color.rgb(85,92,104);c.drawText(a.area.toString(),200f,108f+i*24f,text)}
  if(steering){line.color=Color.argb(115,255,255,255);line.strokeWidth=2f;c.drawCircle(touchX,touchY,25f,line)}
  if(System.currentTimeMillis()<toastUntil){fill.color=Color.rgb(39,73,184);c.drawRoundRect(w/2f-78f,82f,w/2f+78f,116f,17f,17f,fill);text.textAlign=Paint.Align.CENTER;text.color=Color.rgb(255,226,69);text.textSize=14f;c.drawText(toastText,w/2f,104f,text)}
  if(System.currentTimeMillis()<captureUntil){fill.color=Color.argb(50,75,170,255);c.drawRoundRect(left,top,left+aw,top+ah,18f,18f,fill)}
  if(me?.alive==false){fill.color=Color.argb(205,18,22,30);c.drawRoundRect(w/2f-130,h/2f-54,w/2f+130,h/2f+54,22f,22f,fill);text.textAlign=Paint.Align.CENTER;text.color=Color.WHITE;text.textSize=22f;c.drawText("ELIMINATED",w/2f,h/2f-8f,text);text.textSize=11f;text.typeface=Typeface.DEFAULT;c.drawText("Respawning…",w/2f,h/2f+17f,text);text.typeface=Typeface.create("sans",Typeface.BOLD)}
  postInvalidateDelayed(33L)
 }
 private fun drawCharacter(c:Canvas,x:Float,y:Float,r:Float,id:String,fallback:Int){
  val d=CharacterCatalog.all.firstOrNull{it.id==id};fill.color=d?.body?:fallback;c.drawCircle(x,y,r,fill);fill.color=Color.argb(80,0,0,0);c.drawCircle(x,y+r*.13f,r*.88f,fill);fill.color=d?.body?:fallback;c.drawCircle(x,y-r*.08f,r*.9f,fill);fill.color=d?.accent?:Color.WHITE;c.drawCircle(x,y-r*.36f,r*.33f,fill);fill.color=Color.rgb(38,42,52);c.drawCircle(x-r*.27f,y-r*.08f,r*.10f,fill);c.drawCircle(x+r*.27f,y-r*.08f,r*.10f,fill);fill.color=Color.WHITE;c.drawCircle(x-r*.24f,y-r*.11f,r*.035f,fill);c.drawCircle(x+r*.30f,y-r*.11f,r*.035f,fill)
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{touchX=e.x;touchY=e.y;steering=true;invalidate();return true};MotionEvent.ACTION_MOVE->{touchX=e.x;touchY=e.y;send(e.x-width/2f,e.y-height/2f);invalidate();return true};MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{steering=false;invalidate();return true}};return true}
 private fun send(x:Float,y:Float){val now=System.currentTimeMillis();if(now-lastSend<35L)return;val d=hypot(x.toDouble(),y.toDouble()).toFloat();if(d<4f)return;lastSend=now;network.direction(x/d,y/d)}
}
