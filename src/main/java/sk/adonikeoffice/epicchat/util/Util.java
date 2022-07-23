package sk.adonikeoffice.epicchat.util;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import org.bukkit.entity.Player;
import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.remain.Remain;
import sk.adonikeoffice.epicchat.settings.Settings;
import sk.adonikeoffice.epicchat.task.QuestionTask;

import java.util.ArrayList;
import java.util.List;

public final class Util {

	@Getter
	private static final Util instance = new Util();

	@Getter
	@Setter
	private int lastMessageTime = 0;

	public static void sendType(final Player player, String message, final boolean runnable) {
		message = Variables.replace(message, player);

		if (message.contains("{prefix}"))
			message = message.replace("{prefix}", Settings.PLUGIN_PREFIX);

		final String actionBarType = MessageType.ACTIONBAR.getType();
		final String chatType = MessageType.CHAT.getType();
		final String titleType = MessageType.TITLE.getType();

		if (message.startsWith(actionBarType)) {
			final String replacedActionbarMessage = message.replace(actionBarType, "");

			if (runnable) {
				Common.runTimerAsync(20, () -> {
					if (QuestionTask.questionIsRunning())
						Remain.sendActionBar(player, replacedActionbarMessage);
				});

				QuestionTask.breakCycle = true;
			} else
				Remain.sendActionBar(player, replacedActionbarMessage);
		} else if (message.startsWith(chatType)) {
			if (runnable)
				QuestionTask.breakCycle = true;

			if (message.startsWith("<center>")) {
				message = message.replace("<center>", "");

				message = ChatUtil.center(message);
			}

			Common.tellNoPrefix(player, message.replace(chatType, ""));
		} else if (message.startsWith(titleType)) {
			if (runnable)
				QuestionTask.breakCycle = true;

			final String[] split = message.split("\\|");

			if (split.length == 2)
				Remain.sendTitle(player, split[0].replace(titleType, ""), split[1]);
			else
				Common.log("Invalid title message: " + message + ". Valid: " + titleType + "<title>|<subtitle>");
		} else
			Common.log("Unknown message type. Available: " + actionBarType + ", " + chatType + ", " + titleType);
	}

	@RequiredArgsConstructor
	private enum MessageType {
		ACTIONBAR("{actionbar}"), CHAT("{chat}"), TITLE("{title}");

		@Getter
		private final String type;
	}

	// ================================================================
	// METHODS RELATED TO PLAYERS
	// ================================================================

	public static boolean hasPermission(final Player player, final String permission) {
		if (permission.equals("none"))
			return true;

		return player.hasPermission(permission) || player.isOp();
	}

	public static boolean canChat(final Player player) {
		if (isMuted(player)) {
			Common.tell(player, Settings.Message.MUTED);

			return false;
		}

		if (!HookManager.isLogged(player)) {
			Common.tell(player, Settings.Message.NOT_LOGGED);

			return false;
		}

		if (!Util.hasPermission(player, Settings.Chat.PERMISSION)) {
			Common.tell(player, Settings.Message.NO_PERMISSION.replace("{0}", Settings.Chat.PERMISSION));

			return false;
		}

		return true;
	}

	private static boolean isMuted(final Player player) {
		if (Common.doesPluginExist("AdvancedBan") && PunishmentManager.get().isMuted(UUIDManager.get().getUUID(player.getName())))
			return true;

		return HookManager.isMuted(player);
	}

	// ================================================================
	// /METHODS RELATED TO PLAYERS
	// ================================================================

	public static List<String> replaceVariables(final Player player, final List<String> list) {
		final List<String> replaced = new ArrayList<>();

		for (final String item : list)
			replaced.add(Variables.replace(item, player));

		return replaced;
	}

}