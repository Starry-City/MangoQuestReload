package me.Cutiemango.MangoQuest.manager.config;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import me.Cutiemango.MangoQuest.ConfigSettings;
import me.Cutiemango.MangoQuest.DebugHandler;
import me.Cutiemango.MangoQuest.I18n;
import me.Cutiemango.MangoQuest.I18n.SupportedLanguage;
import me.Cutiemango.MangoQuest.Main;
import me.Cutiemango.MangoQuest.QuestIO;
import me.Cutiemango.MangoQuest.QuestStorage;
import me.Cutiemango.MangoQuest.QuestUtil;
import me.Cutiemango.MangoQuest.conversation.FriendConversation;
import me.Cutiemango.MangoQuest.conversation.QuestBaseAction;
import me.Cutiemango.MangoQuest.conversation.QuestBaseAction.EnumAction;
import me.Cutiemango.MangoQuest.conversation.QuestChoice;
import me.Cutiemango.MangoQuest.conversation.QuestChoice.Choice;
import me.Cutiemango.MangoQuest.conversation.QuestConversation;
import me.Cutiemango.MangoQuest.conversation.StartTriggerConversation;
import me.Cutiemango.MangoQuest.manager.CustomObjectManager;
import me.Cutiemango.MangoQuest.manager.QuestChatManager;
import me.Cutiemango.MangoQuest.manager.QuestNPCManager;
import me.Cutiemango.MangoQuest.manager.QuestValidater;
import me.Cutiemango.MangoQuest.model.Quest;
import me.Cutiemango.MangoQuest.model.QuestSetting;
import me.Cutiemango.MangoQuest.objects.GUIOption;
import me.Cutiemango.MangoQuest.objects.QuestNPC;
import me.Cutiemango.MangoQuest.objects.QuestStage;
import me.Cutiemango.MangoQuest.objects.QuestVersion;
import me.Cutiemango.MangoQuest.objects.RequirementType;
import me.Cutiemango.MangoQuest.objects.reward.QuestReward;
import me.Cutiemango.MangoQuest.objects.reward.RewardChoice;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerObject.TriggerObjectType;
import me.Cutiemango.MangoQuest.objects.trigger.TriggerType;
import me.Cutiemango.MangoQuest.questobject.SimpleQuestObject;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreakBlock;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBreedMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectBucketFill;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectConsumeItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectCraftItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectDeliverItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnchantItem;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectEnterCommand;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectFishing;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectKillMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLaunchProjectile;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectLoginServer;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectMoveDistance;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlaceholderAPI;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectPlayerChat;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectReachLocation;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectRegeneration;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectShearSheep;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectSleep;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTalkToNPC;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectTameMob;
import me.Cutiemango.MangoQuest.questobject.objects.QuestObjectUseAnvil;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;

public class QuestConfigLoader {
	public QuestConfigLoader(QuestConfigManager cm) {
		manager = cm;
	}

	private final QuestConfigManager manager;

	public void loadAll() {
		Main.disableScoreboard.clear();
		loadPlayerSettingsData();
		loadTranslation();
		loadChoice();
		loadQuests();
		loadConversation();
		loadGUIOptions();
		loadNPC();

		SimpleQuestObject.initObjectNames(null);
	}
	
	public void loadPlayerSettingsData() {
		I18n.getLangMap().clear();
		File file = new File(Main.getInstance().getDataFolder()+"/playerdata.yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		for(String uuid:fc.getKeys(false)) {
			String path = uuid+".";
			String lang = path+"lang";
			I18n.appendLangData(UUID.fromString(uuid), SupportedLanguage.valueOfSave(fc.getString(lang)));
			if(fc.getBoolean(path+"noscoreboard")) {
				Main.disableScoreboard.add(lang);
			}
		}
	}
	public void loadConfig() {
		QuestIO config = manager.getConfig();
		// Load i18n
		boolean useModifiedLanguage = false;

		if (config.getBoolean("useModifiedLanguage"))
			useModifiedLanguage = true;
		
		
		//default language initialization
		if (config.getString("language") != null) {
			String[] lang = config.getString("language").split("_");
			if (lang.length > 1) {
				ConfigSettings.LOCALE_USING = new Locale(lang[0], lang[1]);
				
				I18n.init(ConfigSettings.LOCALE_USING, useModifiedLanguage);
				
				QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.UsingLocale", config.getString("language")));
			}
		} else {
			ConfigSettings.LOCALE_USING = ConfigSettings.DEFAULT_LOCALE;
			I18n.init(ConfigSettings.LOCALE_USING, useModifiedLanguage);
			QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.LocaleNotFound"));
			QuestChatManager.logCmd(Level.INFO,
					I18n.locMsg(null,"Cmdlog.UsingDefaultLocale", ConfigSettings.DEFAULT_LOCALE.toString()));
			config.set("language", ConfigSettings.DEFAULT_LOCALE.toString());
		}
		
		if(config.getConfig().getList("playerLanguage")!=null) {
			for(String lang:((List<String>)config.getConfig().getList("playerLanguage"))) {
				String[] langArgs = lang.split("_");
				ConfigSettings.PLAYER_LOCALE_CHOICES.put(I18n.SupportedLanguage.valueOf(lang),new Locale(langArgs[0],langArgs[1]));
			}
		}else {
			for(String lang:Arrays.asList("zh_TW","zh_CN","en_US")) {
				String[] langArgs = lang.split("_");
				ConfigSettings.PLAYER_LOCALE_CHOICES.put(I18n.SupportedLanguage.valueOf(lang),new Locale(langArgs[0],langArgs[1]));
				
			}
		}
		
		//load I18n lang map
		I18n.initLangMap(useModifiedLanguage);

		//boolean: enable bungeecord command
		if(!config.contains("enablebungeecordsupport")){
			config.set("enablebungeecordsupport",true);
		}
		ConfigSettings.ENABLE_BUNGEECORD_SUPPORT = config.getBoolean("enablebungeecordsupport");
		
		if(!config.contains("bungeecordperm")) {
			config.set("bungeecordperm", "mangoquestreloaded.bungeecord");
		}
		ConfigSettings.BUNGEECORD_PERMISSION = config.getString("bungeecordperm");
		
		// Use weak item check
		ConfigSettings.USE_WEAK_ITEM_CHECK = config.getBoolean("useWeakItemCheck");
		DebugHandler.log(5, "[Config] useWeakItemCheck=" + ConfigSettings.USE_WEAK_ITEM_CHECK);

		// Enable Skip
		ConfigSettings.ENABLE_SKIP = config.getBoolean("enableSkip");
		DebugHandler.log(5, "[Config] enableSkip=" + ConfigSettings.ENABLE_SKIP);

		ConfigSettings.CONVERSATION_ACTION_INTERVAL_IN_TICKS = config.getIntOrDefault("conversationActionInterval", 25);
		DebugHandler.log(5, "[Config] actionInterval=" + ConfigSettings.CONVERSATION_ACTION_INTERVAL_IN_TICKS);

		// Save Interval
		ConfigSettings.PLAYER_DATA_SAVE_INTERVAL = config.getIntOrDefault("saveIntervalInSeconds", 600);
		DebugHandler.log(5, "[Config] saveInterval=" + ConfigSettings.PLAYER_DATA_SAVE_INTERVAL);

		// SQL Clear Interval
		ConfigSettings.SQL_CLEAR_INTERVAL_IN_TICKS = config.getIntOrDefault("databaseClearInterval", 24000);
		DebugHandler.log(5, "[Config] databaseClearInterval=" + ConfigSettings.SQL_CLEAR_INTERVAL_IN_TICKS);

		// Debug mode
		DebugHandler.DEBUG_LEVEL = config.getInt("debugLevel");
		if (config.getInt("debugLevel") > 0)
			QuestChatManager.logCmd(Level.WARNING,
					I18n.locMsg(null,"Cmdlog.DebugMode", Integer.toString(DebugHandler.DEBUG_LEVEL)));

		// Rightclick Settings
		ConfigSettings.USE_RIGHT_CLICK_MENU = config.getBoolean("useRightClickMenu");
		DebugHandler.log(5, "[Config] useRightClickMenu=" + ConfigSettings.USE_RIGHT_CLICK_MENU);

		// Login Message
		ConfigSettings.POP_LOGIN_MESSAGE = config.getBoolean("popLoginMessage");
		DebugHandler.log(5, "[Config] popLoginMessage=" + ConfigSettings.POP_LOGIN_MESSAGE);

		// Plugin Prefix
		QuestStorage.prefix = QuestChatManager
				.translateColor(config.getStringOrDefault("pluginPrefix", "&6MangoQuest>"));

		// Maximum Quests
		ConfigSettings.MAXIMUM_QUEST_AMOUNT = config.getIntOrDefault("maxQuestAmount", 4);

		// Scoreboard settings
		if (!config.contains("enableScoreboard"))
			config.set("enableScoreboard", true);
		ConfigSettings.ENABLE_SCOREBOARD = config.getBoolean("enableScoreboard");
		DebugHandler.log(5, "[Config] enableScoreboard=" + ConfigSettings.ENABLE_SCOREBOARD);

		ConfigSettings.MAXIMUM_DISPLAY_QUEST_AMOUNT = config.getIntOrDefault("scoreboardMaxCanTakeQuestAmount", 3);

		// Particle Settings
		if (!config.contains("useParticleEffect"))
			config.set("useParticleEffect", true);
		ConfigSettings.USE_PARTICLE_EFFECT = config.getBoolean("useParticleEffect");
		DebugHandler.log(5, "[Config] useParticleEffect=" + ConfigSettings.USE_PARTICLE_EFFECT);

		ConfigSettings.SAVE_TYPE = ConfigSettings.SaveType.YML;
		if (config.getString("saveType") != null) {
			try {
				ConfigSettings.SAVE_TYPE = ConfigSettings.SaveType.valueOf(config.getString("saveType").toUpperCase());
			} catch (IllegalArgumentException ex) {
				DebugHandler.log(5, "[Config] User entered an invalid save type. Using default save type...");
			}
		}

		DebugHandler.log(5, "[Config] saveType=" + ConfigSettings.SAVE_TYPE.toString());

		// Database Settings
		if (ConfigSettings.SAVE_TYPE != ConfigSettings.SaveType.YML) {
			ConfigSettings.DATABASE_ADDRESS = config.getString("databaseAddress");
			ConfigSettings.DATABASE_PORT = config.getInt("databasePort");
			ConfigSettings.DATABASE_NAME = config.getString("databaseName");
			ConfigSettings.DATABASE_USER = config.getString("databaseUser");
			ConfigSettings.DATABASE_PASSWORD = config.getString("databasePassword");

			DebugHandler.log(5, "[Config] Database login credentials loaded!");
			DebugHandler.log(5,
					String.format("[Config] address=%s, port=%d, user=%s, pw=%s", ConfigSettings.DATABASE_ADDRESS,
							ConfigSettings.DATABASE_PORT, ConfigSettings.DATABASE_USER,
							ConfigSettings.DATABASE_PASSWORD));
		}
		
		if(Main.getInstance().getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
			if(config.getString("enablediscordsrv")==null) {
				config.set("enablediscordsrv", true);
				config.save();
			}
			ConfigSettings.DISCORDSRV_ENABLED = config.getBoolean("enablediscordsrv");
			if(config.getString("discordsrvchannel")!=null) {
				ConfigSettings.DISCORDSRV_CHANNEL_NAME = config.getString("discordsrvchannel");
				if(!QuestUtil.doesChannelExist(ConfigSettings.DISCORDSRV_CHANNEL_NAME)) {
					ConfigSettings.DISCORDSRV_CHANNEL_NAME = null;
					QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null, "Cmdlog.DiscordSRV404Channel",config.getString("discordsrvchannel")));
				}
				
			}else {
				config.set("discordsrvchannel", "omegalul");
				config.save();
				ConfigSettings.DISCORDSRV_CHANNEL_NAME = null;
			}
		}
		if(Main.getInstance().getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
			if(!config.contains("enablemmnewsupport")) {
				config.set("enablemmnewsupport", false);
				config.save();
			}
			ConfigSettings.MYTHICMOBNEWSUPPORT = config.getBoolean("enablemmnewsupport");
		}
		
		if(!config.contains("fadein")) {
			config.set("fadein", 20);
			config.save();
		}
		if(!config.contains("stay")) {
			config.set("stay", 20);
			config.save();
		}
		if(!config.contains("fadeout")) {
			config.set("fadeout", 20);
			config.save();
		}

		ConfigSettings.titlefadein = config.getInt("fadein");
		ConfigSettings.titlestay = config.getInt("stay");
		ConfigSettings.titlefadeout = config.getInt("fadeout");
		if(!config.contains("translateitementity")) {
			config.set("translateitementity", true);
			config.save();
		}
		ConfigSettings.useTranslation = config.getBoolean("translateitementity");
		
		if(!config.contains("enabledifferentialquestsystem")) {
			config.set("enabledifferentialquestsystem", true);
			config.save();
		}
		ConfigSettings.differentialquestsserver = config.getBoolean("enabledifferentialquestsystem");
	}
	
	public void loadTranslation() {
		QuestIO translation = manager.getTranslation();
		if (translation.isSection("Material")) {
			for (String s : translation.getSection("Material")) {
				Material mat = Material.getMaterial(s);
				if (mat != null)
					QuestStorage.translationMap.put(mat, translation.getString("Material." + s));
				else
					DebugHandler.log(5, "Material " + s + " is null during the compatible search. Skipping...");
			}
		}

		if (translation.isSection("EntityType")) {
			for (String e : translation.getSection("EntityType")) {
				try {
					QuestStorage.entityTypeMap.put(EntityType.valueOf(e),
							translation.getConfig().getString("EntityType." + e));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.TranslationLoaded"));
	}

	private List<File> getAllFiles(String directoryName) {
		File directory = new File(directoryName);
		if (!directory.exists())
			directory.mkdirs();

		File[] fList = directory.listFiles();
		List<File> resultList = new ArrayList<>(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isDirectory())
				resultList.addAll(getAllFiles(file.getAbsolutePath()));
		}
		return resultList;
	}

	public void loadNPC() {
		QuestIO npc = manager.getNPC();
		int count = 0;
		HashMap<Integer, Integer> cloneMap = new HashMap<>();
		if (npc.contains("NPC") && npc.isSection("NPC")) {
			for (Integer id : npc.getIntegerSection("NPC")) {
				DebugHandler.log(5, "[Config] Loading NPC data id=" + id);
				if (!QuestValidater.validateNPC(Integer.toString(id))) {
					QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NPCNotValid", Integer.toString(id)));
					continue;
				}
				NPC npcReal = Main.getHooker().getNPC(id);
				QuestNPC npcdata = QuestNPCManager.hasData(id) ? QuestNPCManager.getNPCData(id) : new QuestNPC(npcReal);
				if (npc.getString("NPC." + id + ".Clone") != null)
					cloneMap.put(id, npc.getInt("NPC." + id + ".Clone"));
				else {
					if (npc.isSection("NPC." + id + ".Messages")) {
						for (String i : npc.getSection("NPC." + id + ".Messages")) {
							List<String> list = npc.getStringList("NPC." + id + ".Messages." + i);
							HashSet<String> set = new HashSet<>(list);
							npcdata.putMessage(Integer.parseInt(i.trim()), set);
						}
					}
					if (npc.getStringList("NPC." + id + ".GUIOptions") != null) {
						HashSet<GUIOption> set = new HashSet<>();
						for (String s : npc.getStringList("NPC." + id + ".GUIOptions")) {
							GUIOption option = QuestNPCManager.getOption(s);
							if (option != null)
								set.add(option);
						}
						npcdata.setOptions(set);
					}
				}
				QuestNPCManager.updateNPC(npcReal, npcdata);
				DebugHandler.log(5, "[Config] Successfully loaded NPC data id=" + id);
				count++;
			}
		}
		for (Integer id : cloneMap.keySet()) {
			QuestNPCManager.updateNPC(Main.getHooker().getNPC(id), QuestNPCManager.getNPCData(cloneMap.get(id)));
		}
		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.NPCLoaded", Integer.toString(count)));
	}

	public void loadConversation() {
		List<File> files = getAllFiles(Main.getInstance().getDataFolder() + File.separator + "conversation");

		int count = 0;
		for (File f : files) {
			QuestIO conv = new QuestIO(f);

			if (conv.isSection("Conversations")) {
				for (String id : conv.getSection("Conversations")) {
					String name = conv.getString("Conversations." + id + ".ConversationName");
					List<String> act = conv.getStringList("Conversations." + id + ".ConversationActions");
					NPC npc = Main.getHooker().getNPC(conv.getInt("Conversations." + id + ".NPC"));
					QuestConversation qc;
					if (conv.getBoolean("Conversations." + id + ".FriendConversation")) {
						qc = new FriendConversation(name, id, npc, loadConvAction(act),
								conv.getInt("Conversations." + id + ".FriendPoint"));
						QuestStorage.friendConversations.add((FriendConversation) qc);
						QuestStorage.localConversations.put(id, qc);
					} else if (conv.getBoolean("Conversations." + id + ".StartTriggerConversation")) {
						Quest q = QuestUtil.getQuest(conv.getString("Conversations." + id + ".StartQuest"));
						if (q != null) {
							StartTriggerConversation sconv = new StartTriggerConversation(name, id, npc,
									loadConvAction(act), q);
							sconv.setAcceptActions(
									loadConvAction(conv.getStringList("Conversations." + id + ".AcceptActions")));
							sconv.setDenyActions(
									loadConvAction(conv.getStringList("Conversations." + id + ".DenyActions")));
							sconv.setAcceptMessage(conv.getString("Conversations." + id + ".AcceptMessage"));
							sconv.setDenyMessage(conv.getString("Conversations." + id + ".DenyMessage"));
							sconv.setQuestFullMessage(conv.getString("Conversations." + id + ".QuestFullMessage"));
							QuestStorage.localConversations.put(id, sconv);
							QuestStorage.startTriggerConversations.put(q, sconv);
						}
					} else {
						qc = new QuestConversation(name, id, npc, loadConvAction(act));
						QuestStorage.localConversations.put(id, qc);
					}
					DebugHandler.log(5, "[Config] Successfully loaded conversation id=" + id);
					count++;
				}
			}
		}

		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.ConversationLoaded", Integer.toString(count)));
	}

	public void loadChoice() {
		List<File> files = getAllFiles(Main.getInstance().getDataFolder() + File.separator + "choice");

		int count = 0;
		for (File f : files) {
			QuestIO choice = new QuestIO(f);
			if (!choice.isSection("Choices"))
				continue;
			for (String id : choice.getSection("Choices")) {
				List<Choice> list = new ArrayList<>();
				TextComponent q = new TextComponent(
						QuestChatManager.translateColor(choice.getString("Choices." + id + ".Question")));
				for (int i : choice.getIntegerSection("Choices." + id + ".Options")) {
					String name = choice.getString("Choices." + id + ".Options." + i + ".OptionName");
					Choice c = new Choice(name,
							loadConvAction(choice.getStringList("Choices." + id + ".Options." + i + ".OptionActions")));
					if (choice.isSection("Choices." + id + ".Options." + i + ".FriendPointReq")) {
						for (int npc : choice
								.getIntegerSection("Choices." + id + ".Options." + i + ".FriendPointReq")) {
							c.setFriendPointReq(npc,
									choice.getInt("Choices." + id + ".Options." + i + ".FriendPointReq." + npc));
						}
					}
					list.add(i - 1, c);
				}
				QuestChoice c = new QuestChoice(q, list);
				QuestStorage.localChoices.put(id, c);
				DebugHandler.log(5, "[Config] Successfully loaded choice id=" + id);
				count++;
			}
		}

		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.ChoiceLoaded", Integer.toString(count)));
	}

	public void loadGUIOptions() {
		QuestIO npc = manager.getNPC();
		if (!npc.isSection("GUIOptions"))
			return;
		int count = 0;
		for (String internal : npc.getSection("GUIOptions")) {
			String path = "GUIOptions." + internal + ".";
			String displayText = npc.getString(path + "DisplayText");
			List<TriggerObject> list = new ArrayList<>();
			for (String s : npc.getStringList(path + "ClickEvent")) {
				String[] split = s.split(" ");
				list.add(new TriggerObject(TriggerObjectType.valueOf(split[0]), QuestUtil.convertArgsString(split, 1),
						-1));
			}

			GUIOption option = new GUIOption(internal, displayText, list);

			if (npc.getString(path + "HoverText") != null)
				option.setHoverText(npc.getString(path + "HoverText"));
			if (npc.isSection(path + "Requirements"))
				option.setRequirementMap(loadRequirements(npc, path));
			QuestNPCManager.registerOption(internal, option);
			DebugHandler.log(5, "[Config] Successfully loaded GUIOption id=" + internal);
			count++;
		}

		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.OptionLoaded", Integer.toString(count)));
	}

	public void loadQuests() {
		List<File> files = getAllFiles(Main.getInstance().getDataFolder() + File.separator + "quest");

		int count = 0;
		for (File f : files) {
			QuestIO quest = new QuestIO(f);
			if (!quest.isSection("Quests"))
				continue;
			for (String internal : quest.getSection("Quests")) {
				String qpath = "Quests." + internal + ".";
				String questname = quest.getString(qpath + "QuestName");
				List<String> questoutline = quest.getStringList(qpath + "QuestOutline");

				// Stages
				List<QuestStage> stages = loadStages(quest, internal);
				QuestReward reward = loadReward(quest, internal);

				if (Main.getHooker().hasCitizensEnabled() && quest.contains(qpath + "QuestNPC")) {
					NPC npc = null;
					if (quest.getInt(qpath + "QuestNPC") != -1
							&& QuestValidater.validateNPC(Integer.toString(quest.getInt(qpath + "QuestNPC"))))
						npc = Main.getHooker().getNPC(quest.getInt(qpath + "QuestNPC"));
					else
						if(!QuestValidater.validateNPC(Integer.toString(quest.getInt(qpath + "QuestNPC"))))
							QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NPCNotValid", Integer.toString(quest.getInt(qpath + "QuestNPC"))));
					registerNPC(npc);

					Quest q = new Quest(internal, questname, questoutline, reward, stages, npc);
					if (quest.getString(qpath + "MessageRequirementNotMeet") != null)
						q.setFailMessage(quest.getString(qpath + "MessageRequirementNotMeet"));

					// Requirements
					q.setRequirements(loadRequirements(quest, qpath));
					q.initRequirements();

					// Triggers
					loadTriggers(quest, q);

					QuestSetting.RedoSetting redo = QuestSetting.RedoSetting.ONCE_ONLY;

					if (quest.getString(qpath + "RedoSetting") != null)
						redo = QuestSetting.RedoSetting.valueOf(quest.getString(qpath + "RedoSetting"));

					q.setRedoSetting(redo);
					q.setRedoDelay(quest.getLong(qpath + "RedoDelayMilliseconds"));
					q.setResetHour(quest.getInt(qpath + "ResetHour"));
					q.setResetDay(quest.getInt(qpath + "ResetDay"));

					if (quest.getLong(qpath + "Version") == 0L) {
						QuestVersion ver = QuestVersion.instantVersion();
						quest.set(qpath + "Version", ver.getTimeStamp());
						q.registerVersion(ver);
					} else {
						QuestVersion qc = new QuestVersion(quest.getLong(qpath + "Version"));
						q.registerVersion(qc);
					}

					q.getSettings().toggle(quest.getBoolean(qpath + "Visibility.onTake"),
							quest.getBoolean(qpath + "Visibility.onProgress"),
							quest.getBoolean(qpath + "Visibility.onFinish"),
							quest.getBoolean(qpath + "Visibility.onInteraction"));
					q.setQuitable(quest.getBoolean(qpath + "QuitSettings.Quitable"));
					if (quest.getString(qpath + "WorldLimit") != null
							&& Bukkit.getWorld(quest.getString(qpath + "WorldLimit")) != null)
						q.setWorldLimit(Bukkit.getWorld(quest.getString(qpath + "WorldLimit")));
					if (quest.getBoolean(qpath + "TimeLimited")) {
						q.setTimeLimited(quest.getBoolean(qpath + "TimeLimited"));
						q.setTimeLimit(quest.getLong(qpath + "TimeLimitMilliseconds"));
					}
					q.setUsePermission(quest.getBoolean(qpath + "UsePermission"));
					q.setQuitAcceptMsg(quest.getString(qpath + "QuitSettings.QuitAcceptMsg"));
					q.setQuitCancelMsg(quest.getString(qpath + "QuitSettings.QuitCancelMsg"));

					QuestStorage.localQuests.put(internal, q);
					
					if (npc != null) {
						QuestNPCManager.getNPCData(npc.getId()).registerQuest(q);
						if (!reward.hasRewardNPC())
							reward.setRewardNPC(npc);
						DebugHandler.log(5, "[Config] Successfully registered Quest of id=" + q.getInternalID()
								+ " into NPC of id=" + npc.getId() + "'s data.");
					}
					if (reward.hasRewardNPC()) {
						registerNPC(reward.getRewardNPC());
						QuestNPCManager.getNPCData(reward.getRewardNPC().getId()).registerReward(q);
					}
					count++;
				} else {
					QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"Cmdlog.NPCError", questname));
				}
			}
		}
		
		QuestChatManager.logCmd(Level.INFO, I18n.locMsg(null,"Cmdlog.QuestLoaded", Integer.toString(count)));
	}

	private List<QuestStage> loadStages(QuestIO quest, String id) {
		String qpath = "Quests." + id + ".";
		List<QuestStage> list = new ArrayList<>();
		if (quest.isSection(qpath + "Stages")) {
			for (String stageCount : quest.getSection(qpath + "Stages")) {
				List<SimpleQuestObject> objs = new ArrayList<>();
				int stage = Integer.parseInt(stageCount.trim());
				for (String objCount : quest.getSection(qpath + "Stages." + stage)) {
					int objIndex = Integer.parseInt(objCount.trim());
					String loadPath = qpath + "Stages." + stage + "." + objIndex + ".";
					String objType = quest.getString(loadPath + "ObjectType");
					SimpleQuestObject obj = null;
					switch (objType) {
					case "LAUNCH_PROJECTILE":
						obj = new QuestObjectLaunchProjectile();
						break;
					case "CRAFT_ITEM":
						obj = new QuestObjectCraftItem();
						break;
					case "USE_ANVIL":
						obj = new QuestObjectUseAnvil();
						break;
					case "BREED_MOB":
						obj = new QuestObjectBreedMob();
						break;
					case "TAME_MOB":
						obj = new QuestObjectTameMob();
						break;
					case "SLEEP":
						obj = new QuestObjectSleep();
						break;
					case "ENCHANT_ITEM":
						obj = new QuestObjectEnchantItem();
						break;
					case "BUCKET_FILL":
						obj = new QuestObjectBucketFill();
						break;
					case "ENTER_COMMAND":
						obj = new QuestObjectEnterCommand();
						break;
					case "PLAYER_CHAT":
						obj = new QuestObjectPlayerChat();
						break;
					case "LOGIN_SERVER":
						obj = new QuestObjectLoginServer();
						break;
					case "MOVE_DISTANCE":
						obj = new QuestObjectMoveDistance();
						break;
					case "SHEAR_SHEEP":
						obj = new QuestObjectShearSheep();
						break;
					case "REGENERATION":
						obj = new QuestObjectRegeneration();
						break;
					case "DELIVER_ITEM":
						obj = new QuestObjectDeliverItem();
						break;
					case "TALK_TO_NPC":
						obj = new QuestObjectTalkToNPC();
						break;
					case "KILL_MOB":
						obj = new QuestObjectKillMob();
						break;
					case "BREAK_BLOCK":
						obj = new QuestObjectBreakBlock();
						break;
					case "CONSUME_ITEM":
						obj = new QuestObjectConsumeItem();
						break;
					case "REACH_LOCATION":
						obj = new QuestObjectReachLocation();
						break;
					case "FISHING":
						obj = new QuestObjectFishing();
						break;
					case "PLACEHOLDER_API":
						obj = new QuestObjectPlaceholderAPI();
						break;
					case "CUSTOM_OBJECT":
						if (CustomObjectManager.hasCustomObject(quest.getString(loadPath + "ObjectClass")))
							obj = CustomObjectManager.getSpecificObject(quest.getString(loadPath + "ObjectClass"));
						else {
							QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"CustomObject.ObjectNotFound",
									quest.getString(loadPath + "ObjectClass")));
							continue;
						}
						break;
					default:
						QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.NoValidObject", id));
						continue;
					}
					if (!obj.load(quest, loadPath)) {
						QuestChatManager.logCmd(Level.SEVERE, I18n.locMsg(null,"Cmdlog.ObjectLoadingError", id,
								Integer.toString(stage), Integer.toString(objIndex)));
						continue;
					}
					if (quest.getString(qpath + "Stages." + stage + "." + objIndex + ".ActivateConversation") != null)
						obj.setConversation(
								quest.getString(qpath + "Stages." + stage + "." + objIndex + ".ActivateConversation"));
					objs.add(obj);
				}
				QuestStage qs = new QuestStage(objs);
				list.add(qs);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private EnumMap<RequirementType, Object> loadRequirements(QuestIO config, String path) {
		EnumMap<RequirementType, Object> map = new EnumMap<>(RequirementType.class);
		if (config.isSection(path + "Requirements")) {
			map.put(RequirementType.LEVEL, config.getInt(path + "Requirements.Level"));
			map.put(RequirementType.MONEY, config.getDouble(path + "Requirements.Money"));

			List<String> list = new ArrayList<>();
			if (config.getStringList(path + "Requirements.Quest") != null)
				list = config.getStringList(path + "Requirements.Quest");
			map.put(RequirementType.QUEST, list);

			// clear won't work because java passes reference
			// list.clear();
			list = new ArrayList<>();
			if (config.getStringList(path + "Requirements.Permission") != null)
				list = config.getStringList(path + "Requirements.Permission");
			map.put(RequirementType.PERMISSION, list);

			List<ItemStack> l = new ArrayList<>();
			if (config.isSection(path + "Requirements.Item")) {
				for (String i : config.getSection(path + "Requirements.Item")) {
					l.add(config.getItemStack(path + "Requirements.Item." + i));
				}
			}
			map.put(RequirementType.ITEM, l);

			HashMap<Integer, Integer> fMap = new HashMap<>();
			if (config.isSection(path + "Requirements.FriendPoint"))
				for (Integer id : config.getIntegerSection(path + "Requirements.FriendPoint"))
					fMap.put(id, config.getInt(path + "Requirements.FriendPoint." + id));

			map.put(RequirementType.FRIEND_POINT, fMap);

			if (Main.getHooker().hasSkillAPIEnabled()) {
				if (config.getString(path + "Requirements.SkillAPIClass") != null)
					map.put(RequirementType.SKILLAPI_CLASS, config.getString(path + "Requirements.SkillAPIClass"));
				if (config.getInt(path + "Requirements.SkillAPILevel") != 0)
					map.put(RequirementType.SKILLAPI_LEVEL, config.getInt(path + "Requirements.SkillAPILevel"));
			}

			if (Main.getHooker().hasQuantumRPGEnabled()) {
				if (config.getString(path + "Requirements.QRPGClass") != null)
					map.put(RequirementType.QRPG_CLASS, config.getString(path + "Requirements.QRPGClass"));
				if (config.getInt(path + "Requirements.QRPGLevel") != 0)
					map.put(RequirementType.QRPG_LEVEL, config.getInt(path + "Requirements.QRPGLevel"));
			}

			if (config.getBoolean(path + "Requirements.AllowDescendant"))
				map.put(RequirementType.ALLOW_DESCENDANT, config.getBoolean(path + "Requirements.AllowDescendant"));

			if (config.contains(path + "Requirements.WorldTime")) {
				List<Long> worldTimeList = new ArrayList<>();
				for (Integer i : config.getConfig().getIntegerList(path + "Requirements.WorldTime")) {
					worldTimeList.add((long)i);
				}
				if (!worldTimeList.isEmpty()) {
					map.put(RequirementType.WORLD_TIME, worldTimeList);
				}
			}
			
			if (config.contains(path + "Requirements.ServerTime")) {
				List<Long> serverTimeList = new ArrayList<>();
				if(config.getConfig().getList(path+"Requirements.ServerTime").get(0) instanceof Date) {
					List<Date> dateList = (List<Date>) config.getConfig().getList(path+"Requirements.ServerTime");
					for(Date i:dateList) {
						if(isValidDate(i)) {
							serverTimeList.add(i.getTime());
						}
					}
				}else if(config.getConfig().getList(path+"Requirements.ServerTime").get(0) instanceof String){
					for (String i :(List<String>) config.getConfig().getStringList(path + "Requirements.ServerTime")) {
							if (i.indexOf('T') != -1) {
								String yearDay = i.substring(0, i.indexOf('T'));
								String Time = i.substring(i.indexOf('T') + 1);
								String[] yearDays = yearDay.split("-");
								boolean error = false;
								if (yearDays[0].length() != 4 || yearDays[1].length() != 2 || yearDays[2].length() != 2) {
									Bukkit.getLogger().severe(
											"[Config]" + path + "Requirements.ServerTime has date format error: " + i);
									error = true;
								}
								String[] Times = Time.split(":");
								if (Times[0].length() != 2 || Times[1].length() != 2 || Times[2].length() != 2) {
									Bukkit.getLogger().severe(
											"[Config]" + path + "Requirements.ServerTime has date format error " + i);
									error = true;
								}
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								sdf.setLenient(false);
								String finalised = i.replaceAll("T", " ");
								long epoch = 0;
								if (!error) {
									try {
										epoch = sdf.parse(finalised).getTime();
										serverTimeList.add(epoch);
									} catch (ParseException e1) {
										Bukkit.getLogger().severe( "[Config] " + path
												+ "Requirements.ServerTime has date error!You made up that date!As follows:");
										e1.printStackTrace();
									}
								}else {
									Bukkit.getLogger().severe( "[Config]2 " + path
											+ "Requirements.ServerTime has date error!You made up that date!As follows:");
								}
							} else {
								DebugHandler.log(1,
										"[Config]" + path + "Requirements.ServerTime has date format error: " + i);
							}
						

					}
				}else {
					for(Long i:config.getConfig().getLongList(path+"Requirements.ServerTime")) {
						serverTimeList.add((long)i);
						DebugHandler.log(1, "[config] ServerTimeList "+i);
					}
				}
				if (!serverTimeList.isEmpty()) {
					map.put(RequirementType.SERVER_TIME, serverTimeList);
				}else {
					Bukkit.getLogger().info(
							"[Config] Quest" + path + "Requirements.ServerTime has defined section but cannot read date for unknown reasons. " );
				}
			}
			if (config.isSection(path + "Requirements.PlaceholderAPI")) {
				if (Main.getHooker().hasPlaceholderAPIEnabled()) {
					Map<String, String> placeholdersMap = new HashMap<>();
					for (String placeholder : config.getConfig()
							.getConfigurationSection(path + "Requirements.PlaceholderAPI").getKeys(false)) {
						String placeholderPath = path + "Requirements.PlaceholderAPI." + placeholder;
						String retrievedValue = null;
						if ((retrievedValue = config.getString(placeholderPath)) != null) {
							placeholdersMap.put(placeholder, retrievedValue);
						}
					}
					if (!placeholdersMap.isEmpty()) {
						map.put(RequirementType.PLACEHOLDER_API, placeholdersMap);
					}
				} else {
					DebugHandler.log(1, "Quest " + path
							+ " contains placeholders requirements but papi plugin is not enabled or installed.");
				}
			}
			if(config.isSection(path+"Requirements.mcMMO")) {
				if(Main.getHooker().hasmcMMOEnabled()) {
					Map<PrimarySkillType,Integer> skillMap = new HashMap<>();
					String mmoSec = path+"Requirements.mcMMO.";
					
					for(String skillType:config.getConfig().getConfigurationSection(path+"Requirements.mcMMO").getKeys(false)) {
						String levelS = mmoSec+skillType+".level";
						if(ExperienceAPI.isValidSkillType(skillType)) {
							PrimarySkillType skill = mcMMO.p.getSkillTools().matchSkill(skillType);
							int level = config.getConfig().getInt(levelS);
							skillMap.put(skill, level);
						}else {
							DebugHandler.log(1, "[Config] Invalid Sklll Type %s in quest %s", skillType,path);
						}
					}
					if(!skillMap.isEmpty()) {
						map.put(RequirementType.MCMMO_LEVEL, skillMap);
					}
						
				}
			}

		}

		return map;
	}

	private void loadTriggers(QuestIO quest, Quest q) {
		String triggerPath = "Quests." + q.getInternalID() + ".TriggerEvents";
		EnumMap<TriggerType, List<TriggerObject>> map = new EnumMap<>(TriggerType.class);
		if (quest.isSection(triggerPath)) {
			for (String type : quest.getSection(triggerPath)) {
				TriggerType t = TriggerType.valueOf(type);
				List<TriggerObject> list = new ArrayList<>();
				switch (t) {
				case TRIGGER_ON_FINISH:
				case TRIGGER_ON_QUIT:
				case TRIGGER_ON_TAKE:
					for (String obj : quest.getStringList(triggerPath + "." + type)) {
						String[] split = obj.split(" ");
						String object = QuestUtil.convertArgsString(split, 1);
						list.add(new TriggerObject(TriggerObjectType.valueOf(split[0]), object, -1));
					}
					break;
				case TRIGGER_STAGE_FINISH:
				case TRIGGER_STAGE_START:
					for (String obj : quest.getStringList(triggerPath + "." + type)) {
						String[] split = obj.split(" ");
						String object = QuestUtil.convertArgsString(split, 2);
						list.add(new TriggerObject(TriggerObjectType.valueOf(split[1]), object,
								Integer.parseInt(split[0].trim())));
					}
					break;
				}
				map.put(t, list);
			}
		}
		q.setTriggers(map);
	}

	private QuestReward loadReward(QuestIO quest, String id) {
		String qpath = "Quests." + id + ".";
		QuestReward reward = new QuestReward();
		reward.setRewardAmount(quest.getInt(qpath + "Rewards.RewardAmount"));
		reward.setInstantGiveReward(quest.getBoolean(qpath + "Rewards.InstantGiveReward"));

		if (quest.isSection(qpath + "Rewards.Choice")) {
			List<RewardChoice> list = new ArrayList<>();
			for (int index : quest.getIntegerSection(qpath + "Rewards.Choice")) {
				if (index > 9)
					continue;
				RewardChoice choice = new RewardChoice(new ArrayList<>());
				for (int itemIndex : quest.getIntegerSection(qpath + "Rewards.Choice." + index)) {
					choice.addItem(quest.getItemStack(qpath + "Rewards.Choice." + index + "." + itemIndex));
				}
				list.add(choice);
			}
			reward.setChoice(list);
		}

		if (quest.getString(qpath + "Rewards.RewardNPC") != null) {
			if (QuestValidater.validateNPC(quest.getString(qpath + "Rewards.RewardNPC")))
				reward.setRewardNPC(Main.getHooker().getNPC(quest.getString(qpath + "Rewards.RewardNPC")));
		}

		if (quest.getDouble(qpath + "Rewards.Money") != 0)
			reward.setMoney(quest.getDouble(qpath + "Rewards.Money"));
		if (quest.getInt(qpath + "Rewards.Experience") != 0)
			reward.setExp(quest.getInt(qpath + "Rewards.Experience"));
		if (quest.isSection(qpath + "Rewards.FriendlyPoint")) {
			for (String s : quest.getSection(qpath + "Rewards.FriendlyPoint")) {
				reward.addFriendPoint(Integer.parseInt(s.trim()), quest.getInt(qpath + "Rewards.FriendlyPoint." + s));
			}
		}

		if (quest.getStringList(qpath + "Rewards.Commands") != null) {
			List<String> l = quest.getStringList(qpath + "Rewards.Commands");
			for (String s : l) {
				reward.addCommand(s);
			}
		}
		if (Main.getHooker().hasSkillAPIEnabled()) {
			if (quest.getInt(qpath + "Rewards.SkillAPIExp") != 0)
				reward.setSkillAPIExp(quest.getInt(qpath + "Rewards.SkillAPIExp"));
		}

		if (Main.getHooker().hasQuantumRPGEnabled()) {
			if (quest.getInt(qpath + "Rewards.QRPGExp") != 0)
				reward.setQRPGExp(quest.getInt(qpath + "Rewards.QRPGExp"));
		}
		return reward;
	}

	private List<QuestBaseAction> loadConvAction(List<String> fromlist) {
		List<QuestBaseAction> list = new ArrayList<>();
		EnumAction e;
		for (String s : fromlist) {
			if (s.contains("#")) {
				try {
					e = EnumAction.valueOf(s.split("#")[0]);
				} catch (Exception ex) {
					QuestChatManager.logCmd(Level.WARNING, I18n.locMsg(null,"Cmdlog.EnumActionError", s.split("#")[0]));
					continue;
				}
				QuestBaseAction action;
				switch (e) {
				case CHOICE:
				case NPC_TALK:
				case WAIT:
				case SENTENCE:
				case FINISH:
				case COMMAND:
				case COMMAND_PLAYER:
				case COMMAND_PLAYER_OP:
					action = new QuestBaseAction(null,e, s.split("#")[1]);
					break;
				case BUTTON:
				case CHANGE_LINE:
				case CHANGE_PAGE:
				case TAKE_QUEST:
				case EXIT:
				default:
					action = new QuestBaseAction(null,e, null);
					break;
				}
				list.add(action);
			}
		}
		return list;
	}

	private void registerNPC(NPC npc) {
		if (npc != null && !QuestNPCManager.hasData(npc.getId())) {
			DebugHandler.log(5, "[Config] NPC of id=" + npc.getId() + " registered.");
			QuestNPCManager.registerNPC(npc);
		}
	}
	private static boolean isValidDate(Date date) {
		
        try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.setLenient(false);
            format.format(date);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
