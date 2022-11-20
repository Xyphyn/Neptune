package us.xylight.surveyer.event

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import us.xylight.surveyer.command.Translate
import us.xylight.surveyer.handler.CommandHandler

class Reaction(jda: JDA, commandHandler: CommandHandler) {
    /*


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
     */

    private val flags: Map<String, String> = mutableMapOf(
        "\uD83C\uDDEC\uD83C\uDDE7" to "en",
        "\uD83C\uDDEA\uD83C\uDDF8" to "es",
        "\uD83C\uDDEE\uD83C\uDDF1" to "he",
        "\uD83C\uDDEF\uD83C\uDDF5" to "ja",
        "\uD83C\uDDE8\uD83C\uDDF3" to "zh",
        "\uD83C\uDDEB\uD83C\uDDF7" to "fr",
        "\uD83C\uDDE9\uD83C\uDDEA" to "de",
        "\uD83C\uDDEE\uD83C\uDDF9" to "it",
        "\uD83C\uDDF7\uD83C\uDDFA" to "ru"
    )

    init {
        jda.listener<MessageReactionAddEvent> {
            val emoji = flags[it.reaction.emoji.asReactionCode] ?: return@listener
            val reactMessage = it.retrieveMessage().complete()
            val reactMessageText = reactMessage.contentRaw
            if (reactMessage == null) return@listener

            (commandHandler.commandFromName("translate") as Translate).execute(
                reactMessage,
                reactMessageText,
                flags[it.reaction.emoji.asReactionCode]!!,
                it.user!!
            )
        }
    }
}