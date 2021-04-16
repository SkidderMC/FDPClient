/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
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
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.login.UserUtils.getUUID
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.util.Session
import java.net.Proxy
import java.util.*
import kotlin.math.roundToInt

object LoginUtils : MinecraftInstance() {
    private val nameBase = "Air,Stone,Granite,Diorite,Andesite,GrassBlock,Dirt,CoarseDirt,Podzol,Cobblestone,OakPlanks,BirchPlanks,OakSapling,Bedrock,Sand,RedSand,Gravel,GoldOre,IronOre,CoalOre,OakLog,SpruceLog,BirchLog,JungleLog,AcaciaLog,DarkOakLog,OakLeaves,BirchLeaves,Sponge,WetSponge,Glass,LapisOre,LapisBlock,Dispenser,Sandstone,NoteBlock,PoweredRail,Cobweb,Grass,Fern,DeadBush,Piston,WhiteWool,OrangeWool,MagentaWool,YellowWool,LimeWool,PinkWool,GrayWool,CyanWool,PurpleWool,BlueWool,BrownWool,GreenWool,RedWool,BlackWool,Dandelion,Poppy,BlueOrchid,Allium,AzureBluet,RedTulip,OrangeTulip,WhiteTulip,PinkTulip,OxeyeDaisy,Cornflower,WitherRose,RedMushroom,SugarCane,GoldBlock,IronBlock,OakSlab,SpruceSlab,BirchSlab,JungleSlab,AcaciaSlab,DarkOakSlab,StoneSlab,BrickSlab,QuartzSlab,Bricks,Tnt,Bookshelf,Obsidian,Torch,EndRod,ChorusPlant,PurpurBlock,Spawner,OakStairs,Chest,DiamondOre,Farmland,Furnace,Ladder,Rail,Lever,RedstoneOre,Snow,Ice,SnowBlock,Cactus,Clay,Jukebox,OakFence,SpruceFence,BirchFence,JungleFence,AcaciaFence,Pumpkin,Netherrack,SoulSand,SoulSoil,Glowstone,OakTrapdoor,StoneBricks,IronBars,Chain,GlassPane,Melon,Vine,BrickStairs,Mycelium,LilyPad,EndStone,DragonEgg,EmeraldOre,EnderChest,BirchStairs,Beacon,StoneButton,OakButton,BirchButton,Anvil,Hopper,QuartzBlock,Dropper,Barrier,HayBlock,WhiteCarpet,LimeCarpet,PinkCarpet,GrayCarpet,CyanCarpet,BlueCarpet,BrownCarpet,GreenCarpet,RedCarpet,BlackCarpet,Terracotta,CoalBlock,PackedIce,SlimeBlock,GrassPath,Sunflower,Lilac,RoseBush,Peony,TallGrass,LargeFern,Prismarine,SeaLantern,MagmaBlock,BoneBlock,Observer,ShulkerBox,RedConcrete,BlueIce,IronDoor,OakDoor,SpruceDoor,BirchDoor,JungleDoor,AcaciaDoor,DarkOakDoor,Repeater,Comparator,Apple,Bow,Arrow,Coal,Charcoal,Diamond,IronIngot,GoldIngot,WoodenSword,WoodenAxe,WoodenHoe,StoneSword,StoneShovel,StoneAxe,StoneHoe,GoldenSword,GoldenAxe,GoldenHoe,IronSword,IronShovel,IronPickaxe,IronAxe,IronHoe,DiamondAxe,DiamondHoe,Stick,Bowl,String,Feather,Gunpowder,WheatSeeds,Wheat,Bread,IronHelmet,IronBoots,GoldenBoots,Flint,Porkchop,Painting,GoldenApple,OakSign,SpruceSign,BirchSign,JungleSign,AcaciaSign,DarkOakSign,Bucket,WaterBucket,LavaBucket,Minecart,Saddle,Redstone,Snowball,OakBoat,Leather,MilkBucket,CodBucket,Brick,ClayBall,Paper,Book,SlimeBall,Egg,Compass,FishingRod,Clock,Cod,Salmon,Pufferfish,CookedCod,InkSac,CocoaBeans,LapisLazuli,WhiteDye,OrangeDye,MagentaDye,YellowDye,LimeDye,PinkDye,GrayDye,CyanDye,PurpleDye,BrownDye,GreenDye,RedDye,BoneMeal,Bone,Sugar,Cake,WhiteBed,OrangeBed,MagentaBed,YellowBed,LimeBed,PinkBed,GrayBed,CyanBed,PurpleBed,BlueBed,BrownBed,GreenBed,RedBed,BlackBed,Cookie,FilledMap,Shears,MelonSlice,MelonSeeds,Beef,CookedBeef,Chicken,RottenFlesh,EnderPearl,BlazeRod,GhastTear,GoldNugget,NetherWart,Potion,GlassBottle,SpiderEye,BlazePowder,MagmaCream,Cauldron,EnderEye,BatSpawnEgg,BeeSpawnEgg,CatSpawnEgg,CodSpawnEgg,CowSpawnEgg,FoxSpawnEgg,PigSpawnEgg,VexSpawnEgg,FireCharge,WrittenBook,Emerald,ItemFrame,FlowerPot,Carrot,Potato,BakedPotato,Map,ZombieHead,PlayerHead,CreeperHead,NetherStar,PumpkinPie,NetherBrick,Quartz,TntMinecart,Rabbit,RabbitStew,RabbitFoot,RabbitHide,ArmorStand,Lead,NameTag,Mutton,WhiteBanner,LimeBanner,PinkBanner,GrayBanner,CyanBanner,BlueBanner,BrownBanner,GreenBanner,RedBanner,BlackBanner,EndCrystal,ChorusFruit,Beetroot,TippedArrow,Shield,Elytra,SpruceBoat,BirchBoat,JungleBoat,AcaciaBoat,DarkOakBoat,IronNugget,MusicDisc".split(",").toTypedArray()

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

    @JvmStatic
    fun randomCracked(){
        loginCracked("F"+nameBase[(Math.random() * nameBase.size).roundToInt()]+"_"+RandomUtils.nextInt(0,999))
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