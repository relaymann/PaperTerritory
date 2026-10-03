package com.paperterritory.app
import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*
import kotlin.random.Random
class BotGameView(c:Context,private val economy:Economy,private val done:()->Unit):View(c){
 private data class B(var x:Float,var y:Float,var dx:Float,var dy:Float,val color:Int,var alive:Boolean=true)
 private val worldWidth=180f;private val worldHeight=120f;private val cellSize=4f;private val own=mutableSetOf<String>();private val trail=mutableListOf<Pair<Float,Float>>();private val bots=mutableListOf<B>()
 private var x=25f;private var y=60f;private var dx=1f;private var dy=0f;private var alive=true;private var won=false;private var kills=0;private var tick=0
 private val p=Paint(1);private val l=Paint(1).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND};private val t=Paint(1).apply{typeface=Typeface.DEFAULT_BOLD}
 init{home(own,6,15);val cs=intArrayOf(0xffef4d58.toInt(),0xff43c98b.toInt(),0xffa05cff.toInt(),0xffffae32.toInt(),0xff24bfc7.toInt(),0xffff6eb4.toInt(),0xff98ca3b.toInt());for(i in 0..6)bots+=B(35f+(i%4)*35f,20f+(i/4)*60f,Random.nextFloat()*2-1,Random.nextFloat()*2-1,cs[i])}
 private fun k(a:Int,b:Int)=a.toString()+","+b
 private fun home(s:MutableSet<String>,cx:Int,cy:Int){for(j in -6..6)for(i in -6..6){val a=cx+i;val b=cy+j;if(a in 0..44&&b in 0..29)s.add(k(a,b))}}
 private fun inside(px:Float,py:Float,s:Set<String>)=s.contains(k((px/cellSize).toInt().coerceIn(0,44),(py/cellSize).toInt().coerceIn(0,29)))
 private fun fill(){if(trail.size<2){trail.clear();return};val block=own.toMutableSet();for(i in 1 until trail.size){val a=trail[i-1];val b=trail[i];val n=max(1,(hypot((b.first-a.first).toDouble(),(b.second-a.second).toDouble())/1.5).toInt());for(j in 0..n){val u=j.toFloat()/n;block.add(k(((a.first+(b.first-a.first)*u)/cellSize).toInt(),((a.second+(b.second-a.second)*u)/cellSize).toInt()))}};val out=mutableSetOf<String>();val q=java.util.ArrayDeque<Pair<Int,Int>>();for(i in 0..44){q.add(i to 0);q.add(i to 29)};for(j in 0..29){q.add(0 to j);q.add(44 to j)};while(q.isNotEmpty()){val z=q.removeFirst();val kk=k(z.first,z.second);if(z.first !in 0..44||z.second !in 0..29||kk in out||kk in block)continue;out.add(kk);q.add(z.first+1 to z.second);q.add(z.first-1 to z.second);q.add(z.first to z.second+1);q.add(z.first to z.second-1)};for(j in 0..29)for(i in 0..44)if(k(i,j) !in out)own.add(k(i,j));trail.clear()}
 private fun hitTrail(px:Float,py:Float):Boolean{if(trail.size<5)return false;for(i in 0 until trail.size-3)if(hypot((trail[i].first-px).toDouble(),(trail[i].second-py).toDouble())<3)return true;return false}
 private fun step(){val ox=x;val oy=y;x=(x+dx*.95f).coerceIn(1f,worldWidth-1f);y=(y+dy*.95f).coerceIn(1f,worldHeight-1f);val before=inside(ox,oy,own);val after=inside(x,y,own);if(before&&!after)trail.add(x to y)else if(!after&&trail.isNotEmpty())trail.add(x to y)else if(!before&&after)fill();if(hitTrail(x,y)){alive=false;postDelayed(done,800);return};for(b in bots)if(b.alive){if(++tick%35==0){b.dx=Random.nextFloat()*2-1;b.dy=Random.nextFloat()*2-1};val d=hypot(b.dx.toDouble(),b.dy.toDouble()).toFloat().coerceAtLeast(.1f);b.x=(b.x+b.dx/d*.8f).coerceIn(1f,worldWidth-1f);b.y=(b.y+b.dy/d*.8f).coerceIn(1f,worldHeight-1f);if(hypot((b.x-x).toDouble(),(b.y-y).toDouble())<3){b.alive=false;kills++;economy.rewardKill()}}}
  override fun onDraw(c:Canvas){
    c.drawColor(Color.rgb(247,248,250))
    val z=min(width/worldWidth,(height-84f)/worldHeight)*.96f
    val ox=(width-worldWidth*z)/2f
    val oy=72f+(height-72f-worldHeight*z)/2f
    fun screenX(v:Float):Float=ox+v*z
    fun screenY(v:Float):Float=oy+v*z
    p.color=0xff4195ff.toInt()
    for(q in own){
      val a=q.split(',')
      val ix=a[0].toInt()
      val iy=a[1].toInt()
      c.drawRect(screenX(ix*cellSize),screenY(iy*cellSize),screenX((ix+1)*cellSize),screenY((iy+1)*cellSize),p)
    }
    l.color=0xffd0d4da.toInt()
    l.strokeWidth=2f
    c.drawRoundRect(screenX(0f),screenY(0f),screenX(worldWidth),screenY(worldHeight),10f,10f,l)
    if(trail.isNotEmpty()){
      l.color=0xff4195ff.toInt()
      l.strokeWidth=5f*z
      val path=Path()
      trail.forEachIndexed{i,q->if(i==0)path.moveTo(screenX(q.first),screenY(q.second))else path.lineTo(screenX(q.first),screenY(q.second))}
      c.drawPath(path,l)
    }
    p.color=0xff4195ff.toInt()
    c.drawCircle(screenX(x),screenY(y),8f,p)
    for(b in bots)if(b.alive){
      p.color=b.color
      c.drawCircle(screenX(b.x),screenY(b.y),8f,p)
    }
    p.color=Color.WHITE
    c.drawRoundRect(10f,10f,width-10f,60f,18f,18f,p)
    t.color=0xff22252b.toInt()
    t.textSize=17f
    c.drawText("BOT GAME",25f,34f,t)
    t.textSize=12f
    c.drawText("AREA " + own.size + "   KILLS " + kills,25f,50f,t)
    if(alive&&!won){
      step()
      if(own.size>=1350){
        won=true
        postDelayed({(context as? android.app.Activity)?.setContentView(BonusGameView(context,economy){done()})},250L)
      }
    }
    if(alive&&!won)postInvalidateDelayed(33L)
  }
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.actionMasked==MotionEvent.ACTION_DOWN||e.actionMasked==MotionEvent.ACTION_MOVE){val vx=e.x-width/2f;val vy=e.y-height/2f;val d=hypot(vx.toDouble(),vy.toDouble()).toFloat();if(d>6){dx=vx/d;dy=vy/d};return true};return true}
}