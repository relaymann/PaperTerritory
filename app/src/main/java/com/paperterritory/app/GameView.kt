package com.paperterritory.app
import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

class GameView(c:Context,private val n:NetworkClient,private val economy:Economy,private var s:GameState):View(c){
 private var lastKills=-1
 fun isSelf(id:String)=s.selfId==id
 private var rewardToastUntil=0L
 private var rewardText=""
 private val bg=Paint().apply{color=Color.rgb(247,248,250)}
 private val p=Paint(1);private val l=Paint(1).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND}
 private val t=Paint(1);private var sx=0f;private var sy=0f;private var last=0L;private var pulse=0f;private var down=false
 fun updateState(v:GameState){val me=v.players.firstOrNull{it.id==v.selfId};if(me!=null){if(lastKills<0)lastKills=me.kills else if(me.kills>lastKills){val d=me.kills-lastKills;repeat(d){economy.rewardKill()};rewardText="+"+(15*d)+" COINS";rewardToastUntil=System.currentTimeMillis()+1200};lastKills=me.kills};s=v;invalidate()}
 override fun onDraw(c:Canvas){
  c.drawColor(bg.color);val z=min(width.toFloat()/s.width,(height-88f)/s.height)*.96f;val ox=(width-s.width*z)/2f;val oy=76f+(height-76f-s.height*z)/2f
  fun X(v:Float)=ox+v*z
  fun Y(v:Float)=oy+v*z
  p.color=Color.argb(22,60,70,80);c.drawRoundRect(X(0f)+3,Y(0f)+4,X(s.width.toFloat())+3,Y(s.height.toFloat())+4,10f,10f,p)
  val by=s.players.associateBy{it.id}
  for((k,id)in s.cells){val q=k.split(',');val x=q.getOrNull(0)?.toIntOrNull();val y=q.getOrNull(1)?.toIntOrNull();val a=by[id];if(x!=null&&y!=null&&a!=null){p.color=Color.argb(232,Color.red(a.color),Color.green(a.color),Color.blue(a.color));c.drawRect(X(x*4f),Y(y*4f),X((x+1)*4f),Y((y+1)*4f),p)}}
  l.color=Color.rgb(205,210,216);l.strokeWidth=2.5f;c.drawRoundRect(X(0f),Y(0f),X(s.width.toFloat()),Y(s.height.toFloat()),10f,10f,l)
  for(a in s.players.filter{it.alive&&it.trail.size>1}){val path=Path();path.moveTo(X(a.trail[0].first),Y(a.trail[0].second));a.trail.drop(1).forEach{q->path.lineTo(X(q.first),Y(q.second))};l.color=Color.argb(75,Color.red(a.color),Color.green(a.color),Color.blue(a.color));l.strokeWidth=11f;c.drawPath(path,l);l.color=a.color;l.strokeWidth=6f;c.drawPath(path,l);l.color=Color.argb(125,255,255,255);l.strokeWidth=1.5f;c.drawPath(path,l)}
  pulse+=.11f
  for(a in s.players.filter{it.alive}){p.color=Color.argb(45,Color.red(a.color),Color.green(a.color),Color.blue(a.color));c.drawCircle(X(a.x),Y(a.y),15f,p);p.color=a.color;c.drawCircle(X(a.x),Y(a.y),8.5f,p);if(a.id==s.selfId){l.color=Color.WHITE;l.strokeWidth=3f;c.drawCircle(X(a.x),Y(a.y),14f+sin(pulse)*2f,l);if(down){l.color=Color.argb(150,255,255,255);l.strokeWidth=2f;c.drawCircle(X(a.x),Y(a.y),19f+sin(pulse)*3f,l)}}}
  p.color=Color.argb(242,255,255,255);c.drawRoundRect(10f,10f,width-10f,66f,18f,18f,p);t.color=Color.rgb(30,33,38);t.textSize=18f;t.typeface=Typeface.DEFAULT_BOLD;c.drawText(if(s.mode=="duel")"1V1 DUEL" else "TERRITORY",25f,35f,t)
  val me=s.players.firstOrNull{it.id==s.selfId};if(lastKills==0&&me!=null)lastKills=me.kills;t.textSize=12f;t.typeface=Typeface.DEFAULT;val total=(s.width/4f)*(s.height/4f);val pct=if(total>0)((me?.area?:0)/total*100f).roundToInt()else 0;c.drawText("AREA "+pct+"%",25f,54f,t);t.textAlign=Paint.Align.RIGHT;c.drawText(if(s.mode=="duel")"FIRST TO 35%" else if(s.room.isBlank())"PUBLIC MATCH" else "PRIVATE • "+s.room,width-25f,35f,t);c.drawText(s.players.count{it.alive}.toString()+" PLAYERS",width-25f,54f,t);t.textAlign=Paint.Align.LEFT
  val rows=min(5,s.players.size);p.color=Color.argb(228,255,255,255);c.drawRoundRect(12f,84f,205f,94f+rows*25f,15f,15f,p);s.players.sortedByDescending{it.area}.take(5).forEachIndexed{i,a->{t.color=if(a.id==s.selfId)a.color else Color.rgb(55,58,65);t.textSize=12f;t.typeface=if(a.id==s.selfId)Typeface.DEFAULT_BOLD else Typeface.DEFAULT;c.drawText((i+1).toString()+". "+a.name.take(12),25f,112f+i*25f,t);t.textAlign=Paint.Align.RIGHT;c.drawText(a.area.toString(),191f,112f+i*25f,t);t.textAlign=Paint.Align.LEFT}}
  if(System.currentTimeMillis()<rewardToastUntil){p.color=Color.argb(225,35,72,190);c.drawRoundRect(width/2f-82f,76f,width/2f+82f,112f,18f,18f,p);t.color=Color.rgb(255,235,67);t.textSize=16f;t.textAlign=Paint.Align.CENTER;t.typeface=Typeface.DEFAULT_BOLD;c.drawText(rewardText,width/2f,100f,t);t.textAlign=Paint.Align.LEFT}
  if(System.currentTimeMillis()<rewardToastUntil){p.color=Color.argb(225,35,72,190);c.drawRoundRect(width/2f-82f,76f,width/2f+82f,112f,18f,18f,p);t.color=Color.rgb(255,235,67);t.textSize=16f;t.textAlign=Paint.Align.CENTER;t.typeface=Typeface.DEFAULT_BOLD;c.drawText(rewardText,width/2f,100f,t);t.textAlign=Paint.Align.LEFT}
  if(me?.alive==false){p.color=Color.argb(220,20,22,27);c.drawRoundRect(width/2f-135,height/2f-55,width/2f+135,height/2f+55,20f,20f,p);t.color=Color.WHITE;t.textAlign=Paint.Align.CENTER;t.textSize=23f;t.typeface=Typeface.DEFAULT_BOLD;c.drawText("ELIMINATED",width/2f,height/2f-8,t);t.textSize=13f;t.typeface=Typeface.DEFAULT;c.drawText("Respawning…",width/2f,height/2f+20,t);t.textAlign=Paint.Align.LEFT}
  postInvalidateDelayed(33)
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{sx=e.x;sy=e.y;down=true;invalidate();return true};MotionEvent.ACTION_MOVE->{send(e.x-sx,e.y-sy);return true};MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{down=false;invalidate();return true}};return true}
 private fun send(x:Float,y:Float){val now=System.currentTimeMillis();if(now-last<35)return;val d=hypot(x.toDouble(),y.toDouble()).toFloat();if(d<=3)return;last=now;n.direction(x/d,y/d)}
}