package us.xylight.surveyer.command

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import us.xylight.surveyer.handler.CommandHandler
import us.xylight.surveyer.util.EmbedUtil

class Translate : Command {
    override val name = "translate"
    override val description = "Translates any text to any language!"
    override val options: List<OptionData> = listOf(
        OptionData(OptionType.STRING, "text", "The text to translate.", true),
        OptionData(OptionType.STRING, "language", "The language to translate to.", true).addChoices(
            Choice("Spanish", "es"),
            Choice("Hebrew", "he"),
            Choice("Japanese", "ja"),
            Choice("Chinese", "zh"),
            Choice("French", "fr"),
            Choice("German", "de"),
            Choice("Italian", "it"),
            Choice("Russian", "ru")
        )
    )
    override val subcommands: List<Subcommand> = listOf()
    override val permission = null

    @Serializable
    data class TranslationRequest(
        @SerialName("q") val text: String,
        val source: String,
        val target: String,
        val format: String = "text",
        @SerialName("api_key") val apiKey: String
    )

    @Serializable
    data class TranslationResponse(
        val detectedLanguage: DetectedLanguage? = null,
        val translatedText: String
    )

    @Serializable
    data class DetectedLanguage(val confidence: Float, val language: String)

    private val client = CommandHandler.httpClient

    /*

            Choice("Spanish", "es"),
            Choice("Hebrew", "he"),
            Choice("Japanese", "ja"),
            Choice("Chinese", "zh"),
            Choice("French", "fr"),
            Choice("German", "de"),
            Choice("Italian", "it"),
            Choice("Russian", "ru")
     */

    private val langNames: Map<String, String> = mapOf(
        "en" to "\uD83C\uDDEC\uD83C\uDDE7 English",
        "es" to "\uD83C\uDDEA\uD83C\uDDF8 Spanish",
        "he" to "\uD83C\uDDEE\uD83C\uDDF1 Hebrew",
        "ja" to "\uD83C\uDDEF\uD83C\uDDF5 Japanese",
        "zh" to "\uD83C\uDDE8\uD83C\uDDF3 Chinese",
        "fr" to "\uD83C\uDDEB\uD83C\uDDF7 French",
        "de" to "\uD83C\uDDE9\uD83C\uDDEA German",
        "it" to "\uD83C\uDDEE\uD83C\uDDF9 Italian",
        "ru" to "\uD83C\uDDF7\uD83C\uDDFA Russian"
    )

    override suspend fun execute(interaction: SlashCommandInteractionEvent) {
        interaction.deferReply().queue()

        val text = interaction.getOption("text")!!
        val lang = interaction.getOption("language")!!

        val jsonPayload = Json.encodeToJsonElement(TranslationRequest(text.asString, "auto", lang.asString, "text", ""))

        val request = Request.Builder()
            .method("POST", jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .url(CommandHandler.translateServer)
            .build()

        client.newCall(request).execute().use { res ->
            val resText = res.body?.string()!!
            val translation = Json.decodeFromString<TranslationResponse>(resText)

            val confidence = translation.detectedLanguage?.confidence
            val stringConfidence = if (confidence == null) "" else "${langNames[translation.detectedLanguage.language]} [${confidence}%] "

            interaction.hook.sendMessage("")
                .setEmbeds(
                    EmbedUtil.simpleEmbed("Translation", "")
                        .addField("Input", text.asString, false)
                        .addField("Translated", translation.translatedText, false)
                        .setFooter("${stringConfidence}to ${langNames[lang.asString]}")
                        .build()
                )
                .queue()

            res.body?.close()
        }
    }

}