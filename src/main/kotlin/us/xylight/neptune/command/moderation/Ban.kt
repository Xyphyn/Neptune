package us.xylight.neptune.command.moderation

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import us.xylight.neptune.command.Subcommand
import us.xylight.neptune.config.Config
import us.xylight.neptune.event.Interaction
import us.xylight.neptune.util.EmbedUtil
import java.util.concurrent.TimeUnit
import kotlin.time.Duration


object Ban : Subcommand {
    override val name = "ban"
    override val description = "Bans a user from the guild."
    override val options: List<OptionData> = listOf(
        OptionData(OptionType.USER, "user", "Who should be banned?", true),
        OptionData(OptionType.STRING, "reason", "Why are you banning them?", false)
    )

    override suspend fun execute(interaction: SlashCommandInteractionEvent) {
        val user = interaction.getOption("user")!!
        val reason = interaction.getOption("reason")?.asString ?: "No reason provided."

        if (!interaction.member!!.canInteract(user.asMember!!)) {
            val embed = EmbedUtil.simpleEmbed("Error", "${Config.conf.emoji.uac} You are unable to interact with ${user.asUser.asMention}. Do they have a higher permission than you?")

            interaction.hook.editOriginalEmbeds(embed.build()).queue()

            return
        }

        val embed = Moderation.punishEmbed("Ban", "was banned.", reason, Config.conf.emoji.ban, user.asUser)

        embed.setColor(Config.conf.misc.error)

        val btn = interaction.jda.button(
            ButtonStyle.SECONDARY,
            "Undo",
            Emoji.fromFormatted(Config.conf.emoji.trash),
            false,
            Duration.parse("60s"),
            interaction.user
        ) {
            interaction.hook.retrieveOriginal().queue {
                message ->
                message.editMessageComponents(ActionRow.of(message.buttons[0].asDisabled())).queue()
            }

            interaction.guild?.unban(user.asUser)?.queue()
        }

        embed.setFooter(interaction.guild?.name, interaction.guild?.iconUrl)

        Moderation.notifyUser(user.asUser, embed)

        try {
            user.asMember?.ban(0, TimeUnit.MILLISECONDS)?.queue()

            interaction.replyEmbeds(embed.build()).setActionRow(btn).queue()
        } catch (e: HierarchyException) {
            interaction.replyEmbeds(Embed {
                title = "Error"
                description = "${Config.conf.emoji.uac} Unable to ban that user. Do they have a higher permission than you?"
                color = Config.conf.misc.error
            })
        }
    }

}