package com.paperterritory.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.widget.EditText
import android.widget.Toast

class MainActivity : Activity() {
    private val network = NetworkClient()
    private lateinit var economy: Economy

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        economy = Economy(this)
        showMenu()
    }

    private fun showMenu() { setContentView(HomeView()) }

    private fun play(privateRoom: Boolean, room: String, mode: String) {
        val holder = arrayOfNulls<GameView>(1)
        network.connect("Player" + (100..999).random(), room, privateRoom, mode, economy.selectedCharacter().id,
            { state -> runOnUiThread {
                if (holder[0] == null) { holder[0] = GameView(this, network, economy, state); setContentView(holder[0]) }
                else holder[0]!!.updateState(state)
            }},
            { message -> runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); showMenu() }},
            { winner -> runOnUiThread {
                if (holder[0]?.isSelf(winner) == true && holder[0]?.bonusAllowed() == true)
                    setContentView(BonusGameView(this, economy) { showMenu() })
                else showMenu()
            }})
    }

    private fun createRoom() {
        val code = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toList().shuffled().take(5).joinToString("")
        AlertDialog.Builder(this).setTitle("Private room")
            .setMessage("Your room code\n\n" + code + "\n\nShare this code with friends. Up to 8 players.")
            .setPositiveButton("ENTER") { _, _ -> play(true, code, "standard") }
            .setNegativeButton("CANCEL", null).show()
    }

    private fun joinRoom() {
        val input = EditText(this).apply { hint = "ABCDE"; textSize = 22f; gravity = Gravity.CENTER; setSingleLine() }
        AlertDialog.Builder(this).setTitle("Join private room")
            .setMessage("Enter the 5-character room code.").setView(input)
            .setPositiveButton("JOIN") { _, _ ->
                val code = input.text.toString().trim().uppercase()
                if (code.length != 5) Toast.makeText(this, "Room codes are 5 characters.", Toast.LENGTH_SHORT).show()
                else play(true, code, "standard")
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun help() {
        AlertDialog.Builder(this).setTitle("How to play")
            .setMessage("MOVE\nDrag anywhere to steer your character. Leave your territory, draw a trail, then return home to capture the enclosed area.\n\n" +
                "CUT\nTouch an opponent's exposed trail to eliminate them. Protect your own trail.\n\n" +
                "MODES\nOnline is public matchmaking. 1V1 Duel uses a larger arena and first to 35%. Bot Game is offline. Private rooms use 5-character codes.\n\n" +
                "REWARDS\nPlayer eliminations award 15 coins. A qualifying public/Bot full-map win opens a 6-second bonus game with coins, gems and green +2 second pickups.")
            .setPositiveButton("GOT IT", null).show()
    }

    private inner class HomeView : View(this) {
        private val card = Paint(1)
        private val text = Paint(1).apply { typeface = Typeface.create("sans", Typeface.BOLD) }
        private var downX = 0f
        private var downY = 0f

        override fun onDraw(c: Canvas) {
            val w = width.toFloat(); val h = height.toFloat()
            c.drawColor(Color.rgb(247,248,251))
            val header = LinearGradient(0f,0f,0f,min(h,470f),Color.rgb(47,104,240),Color.rgb(28,72,181),Shader.TileMode.CLAMP)
            card.shader = header; c.drawRect(0f,0f,w,min(h,470f),card); card.shader = null

            text.textAlign=Paint.Align.CENTER; text.color=Color.WHITE; text.textSize=38f
            c.drawText("TERRITORY",w/2f,92f,text)
            text.textSize=12f; text.letterSpacing=.18f; c.drawText("CLAIM  •  CUT  •  SURVIVE",w/2f,116f,text); text.letterSpacing=0f

            round(c,28f,142f,w-28f,202f,20f,Color.argb(45,255,255,255))
            text.textAlign=Paint.Align.LEFT; text.textSize=11f; text.color=Color.rgb(220,231,255); c.drawText("YOUR WALLET",48f,166f,text)
            text.textSize=17f; text.color=Color.WHITE
            c.drawText("🪙 "+economy.coins,48f,188f,text); c.drawText("💎 "+economy.gems,w/2f+4f,188f,text)

            action(c,28f,232f,w-28f,294f,"PLAY ONLINE","PUBLIC MATCH",true)
            action(c,28f,306f,w-28f,368f,"1V1 DUEL","FIRST TO 35%",false)
            action(c,28f,380f,w-28f,442f,"BOT GAME","OFFLINE PRACTICE",false)

            round(c,28f,466f,w-28f,540f,22f,Color.WHITE)
            text.textAlign=Paint.Align.LEFT; text.color=Color.rgb(31,38,52); text.textSize=15f; c.drawText("PRIVATE MATCH",48f,496f,text)
            text.textSize=11f; text.color=Color.rgb(116,124,139); c.drawText("Play with friends using a 5-character room code",48f,518f,text)
            round(c,w-126f,483f,w-48f,524f,15f,Color.rgb(43,91,220))
            text.textAlign=Paint.Align.CENTER; text.color=Color.WHITE; text.textSize=11f; c.drawText("CREATE",w-87f,509f,text)

            round(c,28f,552f,w-28f,624f,22f,Color.WHITE)
            text.textAlign=Paint.Align.LEFT; text.color=Color.rgb(31,38,52); text.textSize=14f; c.drawText("CHARACTERS & CRATE",48f,580f,text)
            text.textSize=11f; text.color=Color.rgb(116,124,139)
            c.drawText(economy.ownedCount()+"/"+CharacterCatalog.all.size+" collected  •  One mystery crate",48f,601f,text)
            text.textAlign=Paint.Align.CENTER; text.color=Color.rgb(43,91,220); text.textSize=11f; c.drawText("OPEN",w-72f,590f,text)

            text.textAlign=Paint.Align.CENTER; text.color=Color.rgb(102,110,124); text.textSize=12f
            c.drawText("JOIN PRIVATE ROOM",w*.28f,min(h-36f,668f),text); c.drawText("HOW TO PLAY",w*.72f,min(h-36f,668f),text)
        }

        private fun action(c:Canvas,l:Float,top:Float,r:Float,bottom:Float,title:String,sub:String,primary:Boolean) {
            round(c,l,top,r,bottom,19f,if(primary)Color.rgb(43,91,220) else Color.WHITE)
            text.textAlign=Paint.Align.LEFT; text.color=if(primary)Color.WHITE else Color.rgb(31,38,52); text.textSize=16f
            c.drawText(title,l+20f,top+27f,text); text.textSize=10f; text.color=if(primary)Color.rgb(207,222,255) else Color.rgb(121,129,143)
            c.drawText(sub,l+20f,top+47f,text); text.textAlign=Paint.Align.RIGHT; text.textSize=23f; text.color=if(primary)Color.WHITE else Color.rgb(48,94,207)
            c.drawText("›",r-22f,top+38f,text)
        }

        private fun round(c:Canvas,l:Float,t:Float,r:Float,b:Float,radius:Float,color:Int) {
            card.color=Color.argb(16,0,0,0); c.drawRoundRect(l+1f,t+3f,r+1f,b+3f,radius,radius,card); card.color=color; c.drawRoundRect(l,t,r,b,radius,radius,card)
        }

        override fun onTouchEvent(e:MotionEvent):Boolean {
            when(e.actionMasked) {
                MotionEvent.ACTION_DOWN->{downX=e.x;downY=e.y;return true}
                MotionEvent.ACTION_UP->{
                    if(abs(e.x-downX)>18f||abs(e.y-downY)>18f)return true
                    val y=e.y;val w=width.toFloat()
                    when {
                        y in 232f..294f->play(false,"","standard")
                        y in 306f..368f->play(false,"","duel")
                        y in 380f..442f->setContentView(BotGameView(this@MainActivity,economy){showMenu()})
                        y in 466f..540f&&e.x>w-145f->createRoom()
                        y in 552f..624f->setContentView(ShopView(this@MainActivity,economy){showMenu()})
                        y>630f&&e.x<w/2f->joinRoom()
                        y>630f->help()
                    };return true
                }
            };return true
        }
    }
}
