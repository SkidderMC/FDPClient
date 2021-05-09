package net.ccbluex.liquidbounce.backend

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.utils.misc.HttpUtils

object NeteaseAPI {
    private val parser=JsonParser()

    /***
     * @param keyword 搜索关键词
     * @param type 1: 单曲, 10: 专辑, 100: 歌手, 1000: 歌单, 1002: 用户, 1004: MV, 1006: 歌词, 1009: 电台, 1014: 视频
     * @param limit 单页限制
     * @param offset 偏移数量 (页数 -1)*30
     *
     * @return 搜索结果json
     */
    fun search(keyword: String,type: Int=1,limit: Int=30,offset: Int=0): JsonObject{
        val result=HttpUtils.post("https://music.163.com/api/cloudsearch/pc","s=$keyword&type=$type&limit=$limit&offset=$offset")
        return parser.parse(result).asJsonObject
    }

    /***
     * @param id 音乐id
     *
     * @return mp3直链
     */
//    fun getUrl(id: Int): String{
//        val conn=HttpUtils.make("https://music.163.com/song/media/outer/url?id=$id.mp3","GET","")
//
//        conn.connect()
//        conn.inputStream
//        println(conn.url.toString())
//
//        return ""
//    }
}