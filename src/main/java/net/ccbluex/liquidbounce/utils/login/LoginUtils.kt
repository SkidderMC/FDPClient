/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.gson.JsonParser
import com.mojang.authlib.Agent
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.exceptions.AuthenticationUnavailableException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.SessionEvent
import net.ccbluex.liquidbounce.features.module.modules.misc.KillInsults
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.login.UserUtils.getUUID
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.util.Session
import org.apache.commons.io.IOUtils
import java.net.Proxy
import java.util.*
import kotlin.math.roundToInt

object LoginUtils : MinecraftInstance() {
    @JvmStatic
    fun login(username: String?, password: String?): LoginResult {
        val userAuthentication = YggdrasilAuthenticationService(Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

        userAuthentication.setUsername(username)
        userAuthentication.setPassword(password)

        return try {
            userAuthentication.logIn()
            mc.session = Session(userAuthentication.selectedProfile.name,
                    userAuthentication.selectedProfile.id.toString(), userAuthentication.authenticatedToken, "mojang")
            LiquidBounce.eventManager.callEvent(SessionEvent())
            LoginResult.LOGGED
        } catch (exception: AuthenticationUnavailableException) {
            LoginResult.NO_CONTACT
        } catch (exception: AuthenticationException) {
            val message = exception.message!!
            when {
                message.contains("invalid username or password.", ignoreCase = true) -> LoginResult.INVALID_ACCOUNT_DATA
                message.contains("account migrated", ignoreCase = true) -> LoginResult.MIGRATED
                else -> LoginResult.NO_CONTACT
            }
        } catch (exception: NullPointerException) {
            LoginResult.WRONG_PASSWORD
        }
    }

    @JvmStatic
    fun loginCracked(username: String?) {
        mc.session = Session(username, getUUID(username!!), "-", "legacy")
        LiquidBounce.eventManager.callEvent(SessionEvent())
    }

    private val iChar=arrayOf("1","i","I","l")
    private val oChar=arrayOf("o","O","0")
    private val countries=arrayOf("JP","CN","BR","ES","US","UK","TW","HK","KR","FR")
    private val names=JsonParser().parse(IOUtils.toString(KillInsults::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/misc/names.json"),Charsets.UTF_8)).asJsonArray
        .map { it.asString }

    @JvmStatic
    fun randomCracked(){
        fun getRandomInt() = RandomUtils.nextInt(0,100)
        fun getRandomNameWord() = names[RandomUtils.nextInt(0, names.size-1)]
        fun getRandomCountry() = countries[RandomUtils.nextInt(0, countries.size-1)]
        
        val random1=getRandomInt()
        // generate name base
        var name=if(random1<20){
            "Xx${getRandomNameWord().toUpperCase()}xX"
        }else if(random1<40){
            "X${getRandomNameWord().toLowerCase()}X"
        }else if(random1<60){
            "${getRandomNameWord()}Zin"
        }else if(random1<80){
            "${getRandomNameWord()}GOD"
        }else if(random1<95){
            getRandomNameWord()
        }else{ // OHHH THIS IS A EGG
            val sb=StringBuilder()
            if(Math.random()>0.5){
                for(i in 0..RandomUtils.nextInt(4,12)){
                    sb.append(iChar[(Math.random() * iChar.size).roundToInt()])
                }
            }else{
                for(i in 0..RandomUtils.nextInt(4,13)){
                    sb.append(oChar[(Math.random() * oChar.size).roundToInt()])
                }
            }
            sb.toString()
        }

        // change chars to pvp player liked
        val random2=getRandomInt()
        val sb=StringBuilder()
        for(char in name.toCharArray()){
            if(random2<30){
                if(Math.random()>0.5){
                    sb.append(char.toLowerCase())
                }else{
                    sb.append(char.toUpperCase())
                }
            }else if(random2<60&&Math.random()>0.3){
                sb.append(when(char.toLowerCase()){
                    'i' -> '1'
                    'l' -> '1'
                    'e' -> '3'
                    'a' -> '4'
                    's' -> '5'
                    'o' -> '0'
                    else -> char
                })
            }else{
                sb.append(char)
            }
        }
        name=sb.toString()

        // add additions
        val random3=getRandomInt()
        name+=if(random3<15){
            "_"
        }else if(random3<30){
            "__"
        }else if(random3<45){
            "_YT"
        }else if(random3<55){
            "_PVP"
        }else if(random3<65){
            getRandomCountry()
        }else if(random3<75){
            "_${getRandomCountry()}"
        }else{
            ""
        }

        loginCracked(name)
    }

    @JvmStatic
    fun loginSessionId(sessionId: String): LoginResult {
        val decodedSessionData = try {
            String(Base64.getDecoder().decode(sessionId.split(".")[1]), Charsets.UTF_8)
        } catch (e: Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val sessionObject = try {
            JsonParser().parse(decodedSessionData).asJsonObject
        } catch (e: java.lang.Exception){
            return LoginResult.FAILED_PARSE_TOKEN
        }
        val uuid = sessionObject.get("spr").asString
        val accessToken = sessionObject.get("yggt").asString

        if (!UserUtils.isValidToken(accessToken)) {
            return LoginResult.INVALID_ACCOUNT_DATA
        }

        val username = UserUtils.getUsername(uuid) ?: return LoginResult.INVALID_ACCOUNT_DATA

        mc.session = Session(username, uuid, accessToken, "mojang")
        LiquidBounce.eventManager.callEvent(SessionEvent())

        return LoginResult.LOGGED
    }

    enum class LoginResult {
        WRONG_PASSWORD, NO_CONTACT, INVALID_ACCOUNT_DATA, MIGRATED, LOGGED, FAILED_PARSE_TOKEN
    }
}