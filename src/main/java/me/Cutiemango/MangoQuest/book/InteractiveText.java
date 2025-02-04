package me.Cutiemango.MangoQuest.book;

import me.Cutiemango.MangoQuest.data.QuestPlayerData;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.model.Quest;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InteractiveText
{	
	public InteractiveText(Player p,TextComponent t) {
		text = t;
		this.p = p;
	}

	public InteractiveText(Player p,String s) {
		this(p,new TextComponent(TextComponent.fromLegacyText(QuestChatManager.translateColor(s))));
	}

	// similar to showItem
	public InteractiveText(Player p,@NotNull ItemStack item) {
		text = TextComponentFactory.convertItemHoverEvent(item, false);
		this.p = p;
	}

	private TextComponent text;
	private Player p;

	// "/" needed.
	public InteractiveText clickCommand(String cmd) {
		if (!cmd.startsWith("/"))
			cmd = "/" + cmd;
		text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
		return this;
	}

	public InteractiveText showItem(@NotNull ItemStack item) {
		text.addExtra(TextComponentFactory.convertItemHoverEvent(item, false));
		return this;
	}

	public InteractiveText showText(String s) {
		text.setHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { new TextComponent(QuestChatManager.translateColor(s)) }));
		return this;
	}

	public InteractiveText showNPCInfo(@NotNull NPC npc) {
		text.addExtra(TextComponentFactory.convertLocHoverEvent(this.p,me.Cutiemango.MangoQuest.compatutils.Minecraft.MinecraftCompatability.getName(npc), npc.getStoredLocation(), false));
		return this;
	}

	// display: quest's displayName
	// hover: "click to view"
	public InteractiveText showQuest(Quest q) {
		text = TextComponentFactory.convertViewQuest(p,q);
		return this;
	}

	// display: quest's displayName
	// hover: requirement message
	public InteractiveText showRequirement(QuestPlayerData qd, Quest q) {
		text = TextComponentFactory.convertRequirement(qd, q);
		return this;
	}

	public TextComponent get() {
		return text;
	}
}
