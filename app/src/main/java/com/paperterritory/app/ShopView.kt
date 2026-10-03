package com.paperterritory.app

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import kotlin.math.*

class ShopView(context:Context,private val economy:Economy,private val onBack:()->Unit):View(context){
 private val p=Paint(1)
 private val s=Paint(1).apply{style=Paint.Style.STROKE;strokeJoin=Paint.Join.ROUND}
 private val t=Paint(1).apply{typeface=Typeface.DEFAULT_BOLD}
 private var anim=0f
 private var opening=false
 private var result:CharacterDef?=null
 private var isNew=false
 private var started=0L
 private var scroll=0f
 private var downY=0f
 private var notice=""

 override fun onDraw(c:Canvas){
  c.drawColor(Color.rgb(20,99,238))
  p.color=Color.argb(24,255,255,255)
  for(i in 0..10){val x=(i*97f)%width;val y=150f+(i*151f)%max(1,height-260);c.drawCircle(x,y,42f,p)}
  header(c)
  if(opening)opening(c) else shop(c)
  anim+=.05f
  postInvalidateDelayed(32)
 }

 private fun header(c:Canvas){
  pill(c,26f,244f,"COINS","%,d".format(economy.coins),Color.rgb(255,214,42))
  pill(c,width-244f,width-26f,"GEMS",economy.gems.toString(),Color.rgb(232,93,255))
  t.color=Color.WHITE;t.textSize=23f;c.drawText("COLLECTION",28f,118f,t)
  t.textSize=12f;t.typeface=Typeface.DEFAULT;c.drawText("${economy.ownedCount()}/${CharacterCatalog.all.size} characters collected",28f,139f,t);t.typeface=Typeface.DEFAULT_BOLD
 }

 private fun pill(c:Canvas,l:Float,r:Float,label:String,value:String,color:Int){
  p.color=Color.argb(215,30,61,174);c.drawRoundRect(l,18f,r,76f,29f,29f,p)
  p.color=color;c.drawCircle(l+31f,47f,22f,p)
  t.color=Color.WHITE;t.textSize=21f;c.drawText(value,l+61f,55f,t)
  t.color=Color.rgb(190,255,70);t.textSize=22f;c.drawText("+",r-23f,55f,t)
  t.color=Color.WHITE;t.textSize=10f;t.typeface=Typeface.DEFAULT;c.drawText(label,l+61f,69f,t);t.typeface=Typeface.DEFAULT_BOLD
 }

 private fun shop(c:Canvas){
  val top=160f-scroll
  p.color=Color.argb(245,240,250,255);c.drawRoundRect(24f,top,width-24f,top+300f,28f,28f,p)
  t.color=Color.rgb(35,45,75);t.textSize=25f;t.textAlign=Paint.Align.CENTER;c.drawText("MYSTERY CRATE",width/2f,top+43f,t)
  t.textSize=13f;t.typeface=Typeface.DEFAULT;c.drawText("One crate • one random character",width/2f,top+66f,t);t.typeface=Typeface.DEFAULT_BOLD
  crate(c,width/2f,top+148f,1f+sin(anim)*.035f)
  t.textSize=17f;c.drawText("OPEN WITH",width/2f,top+205f,t)
  button(c,48f,top+224f,width/2f-8f,top+276f,"1,000 COINS",true)
  button(c,width/2f+8f,top+224f,width-48f,top+276f,"20 GEMS",false)
  val tile=(width-72f)/3f
  CharacterCatalog.all.forEachIndexed{i,ch->{val x=24f+(i%3)*(tile+12f);val y=top+330f+(i/3)*(tile+12f);drawTile(c,x,y,tile,ch)}}
  if(notice.isNotBlank()){p.color=Color.argb(220,20,30,55);c.drawRoundRect(28f,height-92f,width-28f,height-30f,22f,22f,p);t.color=Color.WHITE;t.textSize=14f;t.textAlign=Paint.Align.CENTER;c.drawText(notice,width/2f,height-53f,t);t.textAlign=Paint.Align.LEFT}
  back(c)
 }

 private fun drawTile(c:Canvas,x:Float,y:Float,size:Float,ch:CharacterDef){
  p.color=Color.argb(238,239,250,255);c.drawRoundRect(x,y,x+size,y+size,22f,22f,p)
  s.color=Color.WHITE;s.strokeWidth=3f;c.drawRoundRect(x+1.5f,y+1.5f,x+size-1.5f,y+size-1.5f,21f,21f,s)
  character(c,x+size/2f,y+size*.50f,size*.72f,ch)
  if(economy.owned().contains(ch.id)){p.color=Color.rgb(76,225,61);c.drawCircle(x+size-25f,y+25f,13f,p);t.color=Color.WHITE;t.textSize=16f;t.textAlign=Paint.Align.CENTER;c.drawText("✓",x+size-25f,y+31f,t);t.textAlign=Paint.Align.LEFT}
  t.color=Color.rgb(35,45,75);t.textSize=11f;t.textAlign=Paint.Align.CENTER;c.drawText(ch.name,x+size/2f,y+size-10f,t);t.textAlign=Paint.Align.LEFT
 }

 private fun character(c:Canvas,cx:Float,cy:Float,size:Float,ch:CharacterDef){
  val q=size
  p.color=Color.argb(28,30,40,70);c.drawOval(cx-q*.34f,cy+q*.30f,cx+q*.34f,cy+q*.43f,p)
  when(ch.kind){
   1->{p.color=ch.body;c.drawRoundRect(cx-q*.35f,cy-q*.15f,cx+q*.35f,cy+q*.34f,18f,18f,p);p.color=ch.accent;c.drawRect(cx-q*.08f,cy-q*.38f,cx+q*.08f,cy-q*.12f,p);p.color=ch.body;c.drawCircle(cx,cy-q*.18f,q*.31f,p)}
   2->{p.color=ch.body;c.drawCircle(cx,cy,q*.30f,p);s.color=ch.accent;s.strokeWidth=8f;c.drawLine(cx-q*.17f,cy-q*.30f,cx-q*.32f,cy-q*.48f,s);c.drawLine(cx+q*.17f,cy-q*.30f,cx+q*.32f,cy-q*.48f,s)}
   5->{p.color=ch.body;c.drawRoundRect(cx-q*.30f,cy-q*.05f,cx+q*.30f,cy+q*.35f,15f,15f,p);p.color=ch.accent;val path=Path();path.moveTo(cx-q*.43f,cy-q*.20f);path.lineTo(cx,cy-q*.48f);path.lineTo(cx+q*.43f,cy-q*.20f);path.close();c.drawPath(path,p)}
   9->{p.color=ch.body;c.drawCircle(cx,cy,q*.31f,p);c.drawCircle(cx-q*.22f,cy-q*.27f,q*.13f,p);c.drawCircle(cx+q*.22f,cy-q*.27f,q*.13f,p)}
   12->{p.color=ch.body;c.drawRoundRect(cx-q*.37f,cy-q*.20f,cx+q*.37f,cy+q*.32f,12f,12f,p);p.color=ch.accent;c.drawRect(cx-q*.31f,cy-q*.12f,cx+q*.31f,cy+.02f,p)}
   14->{p.color=ch.body;val path=Path();path.moveTo(cx-q*.32f,cy+q*.30f);path.quadTo(cx-q*.43f,cy-q*.32f,cx,cy-q*.38f);path.quadTo(cx+q*.43f,cy-q*.32f,cx+q*.32f,cy+q*.30f);path.close();c.drawPath(path,p)}
   17->{p.color=ch.accent;c.drawOval(cx-q*.40f,cy-q*.12f,cx+q*.40f,cy+q*.16f,p);p.color=ch.body;c.drawOval(cx-q*.20f,cy-q*.38f,cx+q*.20f,cy,p)}
   else->{p.color=ch.body;c.drawRoundRect(cx-q*.30f,cy-q*.15f,cx+q*.30f,cy+q*.34f,18f,18f,p);p.color=ch.accent;c.drawCircle(cx,cy-q*.22f,q*.27f,p);if(ch.hat!=0){p.color=ch.hat;c.drawRoundRect(cx-q*.32f,cy-q*.43f,cx+q*.32f,cy-q*.27f,8f,8f,p)}}
  }
  p.color=Color.rgb(38,42,55);c.drawOval(cx-q*.12f,cy-q*.20f,cx-q*.03f,cy-q*.09f,p);c.drawOval(cx+q*.03f,cy-q*.20f,cx+q*.12f,cy-q*.09f,p)
  p.color=Color.WHITE;c.drawCircle(cx-q*.08f,cy-q*.17f,q*.025f,p);c.drawCircle(cx+q*.07f,cy-q*.17f,q*.025f,p)
 }

 private fun crate(c:Canvas,cx:Float,cy:Float,scale:Float){
  c.save();c.scale(scale,scale,cx,cy);p.color=Color.argb(40,20,40,90);c.drawRoundRect(cx-76f,cy-50f,cx+76f,cy+68f,20f,20f,p);p.color=Color.rgb(238,118,255);c.drawRoundRect(cx-68f,cy-60f,cx+68f,cy+60f,18f,18f,p);p.color=Color.rgb(112,72,232);c.drawRoundRect(cx-54f,cy-46f,cx+54f,cy+46f,13f,13f,p);p.color=Color.rgb(255,218,74);c.drawRect(cx-7f,cy-46f,cx+7f,cy+46f,p);p.color=Color.WHITE;c.drawCircle(cx-24f,cy-17f,7f,p);c.drawCircle(cx+22f,cy+13f,5f,p);c.restore()
 }

 private fun opening(c:Canvas){
  val progress=((System.currentTimeMillis()-started)/1350f).coerceIn(0f,1f)
  p.color=Color.argb(215,7,27,92);c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p)
  if(progress<1f){crate(c,width/2f,height/2f-30f,1f+sin(progress*PI.toFloat())*.28f);t.color=Color.WHITE;t.textSize=26f;t.textAlign=Paint.Align.CENTER;c.drawText(if(progress<.45f)"OPENING…" else "WHAT DID YOU GET?",width/2f,height/2f+120f,t);t.textAlign=Paint.Align.LEFT}
  else{val ch=result?:CharacterCatalog.all.first();p.color=Color.argb(245,241,249,255);c.drawRoundRect(34f,170f,width-34f,height-180f,30f,30f,p);t.color=Color.rgb(39,47,79);t.textSize=20f;t.textAlign=Paint.Align.CENTER;c.drawText(if(isNew)"NEW CHARACTER!" else "DUPLICATE CHARACTER",width/2f,215f,t);character(c,width/2f,430f,260f,ch);t.textSize=28f;c.drawText(ch.name,width/2f,610f,t);t.textSize=14f;t.typeface=Typeface.DEFAULT;c.drawText(if(isNew)"Added to your collection" else "Duplicate converted into 250 coins",width/2f,638f,t);t.typeface=Typeface.DEFAULT_BOLD;button(c,58f,height-125f,width-58f,height-65f,"CONTINUE",true);t.textAlign=Paint.Align.LEFT}
 }

 private fun button(c:Canvas,l:Float,top:Float,r:Float,bottom:Float,label:String,primary:Boolean){
  p.color=if(primary)Color.rgb(65,205,72) else Color.rgb(72,112,224);c.drawRoundRect(l,top,r,bottom,18f,18f,p);s.color=Color.argb(130,255,255,255);s.strokeWidth=2f;c.drawRoundRect(l+1,top+1,r-1,bottom-1,17f,17f,s);t.color=Color.WHITE;t.textSize=14f;t.textAlign=Paint.Align.CENTER;c.drawText(label,(l+r)/2f,(top+bottom)/2f+5f,t);t.textAlign=Paint.Align.LEFT
 }

 private fun back(c:Canvas){p.color=Color.argb(220,25,52,135);c.drawRoundRect(20f,height-76f,116f,height-24f,20f,20f,p);t.color=Color.WHITE;t.textSize=14f;t.textAlign=Paint.Align.CENTER;c.drawText("BACK",68f,height-42f,t);t.textAlign=Paint.Align.LEFT}

 override fun onTouchEvent(e:MotionEvent):Boolean{
  when(e.actionMasked){
   MotionEvent.ACTION_DOWN->{downY=e.y;return true}
   MotionEvent.ACTION_MOVE->{if(!opening){val d=downY-e.y;if(abs(d)>4f){scroll=(scroll+d*.65f).coerceIn(0f,900f);downY=e.y;invalidate()}};return true}
   MotionEvent.ACTION_UP->{
    if(opening){if(result!=null&&System.currentTimeMillis()-started>=1350&&e.y>height-150f){opening=false;result=null;invalidate()};return true}
    val top=160f-scroll
    if(e.y>=top+224f&&e.y<=top+276f){if(e.x<width/2f)buy(false) else buy(true);return true}
    if(e.y>height-100f&&e.x<140f){onBack();return true}
    return true
   }
  }
  return true
 }

 private fun buy(gems:Boolean){
  if(opening)return
  val paid=if(gems)economy.spendGems(20) else economy.spendCoins(1000)
  if(!paid){notice=if(gems)"Not enough gems." else "Not enough coins.";return}
  val ch=economy.randomCharacter()
  isNew=!economy.owned().contains(ch.id)
  if(isNew)economy.own(ch.id) else economy.addCoins(250)
  result=ch;started=System.currentTimeMillis();opening=true;notice=""
 }
}
