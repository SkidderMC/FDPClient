/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import com.google.gson.annotations.SerializedName

/**
 * Data classes for the API
 */
data class Build(
    @SerializedName("build_id")
    val buildId: Int,
    @SerializedName("commit_id")
    val commitId: String,
    val branch: String,
    @SerializedName("lb_version")
    val lbVersion: String,
    @SerializedName("mc_version")
    val mcVersion: String,
    val release: Boolean,
    val date: String,
    val message: String,
    val url: String
)

/**
 * Message of the day
 *
 * Contains only a message
 */
data class MessageOfTheDay(val message: String)

/**
 * Settings
 *
 * Settings only stores the setting ID, name, type, description, date, contributors and status
 * The setting id will later be used to actually request the setting and load it
 */
data class AutoSettings(
    @SerializedName("setting_id")
    val settingId: String,
    val name: String,
    @SerializedName("setting_type")
    val type: AutoSettingsType,
    val description: String,
    var date: String,
    val contributors: String,
    @SerializedName("status_type")
    val statusType: AutoSettingsStatusType,
    @SerializedName("status_date")
    var statusDate: String
)

/**
 * Settings type
 *
 * Some might prefer RAGE to LEGIT and vice versa
 * Might add more in the future
 */
enum class AutoSettingsType(val displayName: String) {
    @SerializedName("Rage")
    RAGE("Rage"),

    @SerializedName("Legit")
    LEGIT("Legit")
}

/**
 * Status of the settings will allow you to know whether it is bypassing or not
 */
enum class AutoSettingsStatusType(val displayName: String) {
    @SerializedName("NotBypassing")
    NOT_BYPASSING("Not Bypassing"),

    @SerializedName("Bypassing")
    BYPASSING("Bypassing"),

    @SerializedName("Undetectable")
    UNDETECTABLE("Undetectable"),

    @SerializedName("Unknown")
    UNKNOWN("Unknown")
}

/**
 * Upload response
 */
data class UploadResponse(val status: Status, val message: String, val token: String)

/**
 * Report response
 */
data class ReportResponse(val status: Status, val message: String)

enum class Status {
    @SerializedName("success")
    SUCCESS,

    @SerializedName("error")
    ERROR
}