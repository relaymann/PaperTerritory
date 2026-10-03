package com.paperterritory.app

import android.content.Context
import kotlin.random.Random

data class CharacterDef(val id:String,val name:String,val body:Int,val accent:Int,val hat:Int=0,val kind:Int=0)

object CharacterCatalog {
    val all = listOf(
        CharacterDef("arctic","Arctic",0xffb9c8df.toInt(),0xff66789b.toInt(),0xffffb32c.toInt(),0),
        CharacterDef("pumpkin","Pumpkin",0xffff8a18.toInt(),0xff35a83a.toInt(),0xff35a83a.toInt(),1),
        CharacterDef("antler","Antler",0xffc87932.toInt(),0xff7b3f19.toInt(),0xffa55b24.toInt(),2),
        CharacterDef("santa","Santa",0xfff7d7c7.toInt(),0xffe8343d.toInt(),0xffffffff.toInt(),3),
        CharacterDef("popcorn","Popcorn",0xffffc0a8.toInt(),0xff45c9ff.toInt(),0xfff5f5f5.toInt(),4),
        CharacterDef("witch","Witch",0xffff9acb.toInt(),0xff5b1c91.toInt(),0xfff2b21b.toInt(),5),
        CharacterDef("frost","Frost",0xffd8e7ee.toInt(),0xff5e6a78.toInt(),0xff2f9ce8.toInt(),6),
        CharacterDef("melon","Melon",0xfff3b18b.toInt(),0xffe94f42.toInt(),0xff5bc85b.toInt(),7),
        CharacterDef("angel","Angel",0xffbcecf2.toInt(),0xff63b8ff.toInt(),0xffffdc57.toInt(),8),
        CharacterDef("bear","Bear",0xffa86a43.toInt(),0xff6e3b25.toInt(),0,9),
        CharacterDef("brain","Brain",0xffff9da9.toInt(),0xffe45d75.toInt(),0xffd9dce8.toInt(),10),
        CharacterDef("burger","Burger",0xffffd28c.toInt(),0xff57bd55.toInt(),0xffe9a43a.toInt(),11),
        CharacterDef("truck","Rover",0xffe9574f.toInt(),0xff47aeea.toInt(),0xffffffff.toInt(),12),
        CharacterDef("blue","Blue",0xff66a9ff.toInt(),0xff315de7.toInt(),0xff24306d.toInt(),13),
        CharacterDef("ghost","Ghost",0xffeaf7ff.toInt(),0xff8f77ff.toInt(),0,14),
        CharacterDef("robot","Robot",0xff8795a8.toInt(),0xff3d4c61.toInt(),0xffef5a69.toInt(),15),
        CharacterDef("snow","Snow",0xfff4fbff.toInt(),0xff6d3c8e.toInt(),0xff4e79c9.toInt(),16),
        CharacterDef("ufo","UFO",0xff7be04f.toInt(),0xff8159ee.toInt(),0xffffe16b.toInt(),17)
    )
}

class Economy(private val context:Context) {
    private val prefs=context.getSharedPreferences("territory_economy",Context.MODE_PRIVATE)
    var coins:Int
        get()=prefs.getInt("coins",17660)
        private set(v){prefs.edit().putInt("coins",v.coerceAtLeast(0)).apply()}
    var gems:Int
        get()=prefs.getInt("gems",262)
        private set(v){prefs.edit().putInt("gems",v.coerceAtLeast(0)).apply()}

    fun spendCoins(amount:Int):Boolean{if(coins<amount)return false;coins-=amount;return true}
    fun spendGems(amount:Int):Boolean{if(gems<amount)return false;gems-=amount;return true}
    fun addCoins(amount:Int){coins+=amount}
    fun addGems(amount:Int){gems+=amount}
    fun rewardKill(){addCoins(15)}
    fun owned():MutableSet<String> =(prefs.getString("owned","")?:"").split(",").filter{it.isNotBlank()}.toMutableSet()
    fun own(id:String){val set=owned();if(set.add(id))prefs.edit().putString("owned",set.joinToString(",")).apply()}
    fun ownedCount()=owned().size
    fun selectedCharacter():CharacterDef = CharacterCatalog.all.firstOrNull{it.id in owned()} ?: CharacterCatalog.all.first()
    fun randomCharacter():CharacterDef{val owned=owned();val missing=CharacterCatalog.all.filterNot{owned.contains(it.id)};return(if(missing.isNotEmpty())missing else CharacterCatalog.all).random(Random(System.nanoTime()))}
}
