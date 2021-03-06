package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.data.manager.RaidBossManager.StatusEnum;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class ForestOfTheDead extends L2AttackableAIScript
{
	public static final SpawnLocation HELLMANN_DAY_LOC = new SpawnLocation(-104100, -252700, -15542, 0);
	public static final SpawnLocation HELLMANN_NIGHT_LOC = new SpawnLocation(59050, -42200, -3003, 100);
	
	private static final String qn = "Q024_InhabitantsOfTheForestOfTheDead";
	
	private static final int HELLMANN = 25328;
	private static final int NIGHT_DORIAN = 25332;
	private static final int LIDIA_MAID = 31532;
	
	private static final int DAY_VIOLET = 31386;
	private static final int DAY_KURSTIN = 31387;
	private static final int DAY_MINA = 31388;
	private static final int DAY_DORIAN = 31389;
	
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;
	
	private final List<Npc> _cursedVillageNpcs = new ArrayList<>();
	
	private final Npc _lidiaMaid;
	
	public ForestOfTheDead()
	{
		super("ai/group");
		
		// Initial List feed. NPCs are then unspawned if night is occuring.
		_cursedVillageNpcs.add(addSpawn(DAY_VIOLET, 59618, -42774, -3000, 5636, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_KURSTIN, 58790, -42646, -3000, 240, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_MINA, 59626, -41684, -3000, 48457, false, 0, false));
		_cursedVillageNpcs.add(addSpawn(DAY_DORIAN, 60161, -42086, -3000, 30212, false, 0, false));
		
		_lidiaMaid = addSpawn(LIDIA_MAID, 47108, -36189, -1624, -22192, false, 0, false);
		
		if (!GameTimeTaskManager.getInstance().isNight())
		{
			final RaidBoss boss = RaidBossManager.getInstance().getBosses().get(HELLMANN);
			if (boss != null)
			{
				boss.getSpawn().setLoc(ForestOfTheDead.HELLMANN_DAY_LOC);
				boss.teleportTo(ForestOfTheDead.HELLMANN_DAY_LOC, 0);
			}
			
			_lidiaMaid.getSpawn().setRespawnState(false);
			_lidiaMaid.deleteMe();
		}
		else
		{
			despawnCursedVillageNpcs();
			
			_lidiaMaid.getSpawn().setRespawnState(true);
			_lidiaMaid.getSpawn().doSpawn(false);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addCreatureSeeId(NIGHT_DORIAN);
		
		addGameTimeNotify();
	}
	
	@Override
	public String onCreatureSee(Npc npc, Creature creature)
	{
		if (creature instanceof Player)
		{
			final Player player = creature.getActingPlayer();
			
			final QuestState st = player.getQuestState(qn);
			if (st == null)
				return super.onCreatureSee(npc, creature);
			
			if (st.getInt("cond") == 3)
			{
				st.set("cond", "4");
				st.takeItems(SILVER_CROSS, -1);
				st.giveItems(BROKEN_SILVER_CROSS, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				npc.broadcastNpcSay("That sign!");
			}
		}
		return super.onCreatureSee(npc, creature);
	}
	
	@Override
	public void onGameTime()
	{
		// Hellmann despawns at day.
		if (GameTimeTaskManager.getInstance().getGameTime() == 360)
		{
			final RaidBoss boss = RaidBossManager.getInstance().getBosses().get(HELLMANN);
			if (boss != null)
			{
				boss.getSpawn().setLoc(HELLMANN_DAY_LOC);
				
				if (boss.getRaidStatus() == StatusEnum.ALIVE)
					boss.teleportTo(HELLMANN_DAY_LOC, 0);
			}
			spawnCursedVillageNpcs();
		}
		// And spawns at night.
		else if (GameTimeTaskManager.getInstance().getGameTime() == 0)
		{
			final RaidBoss boss = RaidBossManager.getInstance().getBosses().get(HELLMANN);
			if (boss != null)
			{
				boss.getSpawn().setLoc(HELLMANN_NIGHT_LOC);
				boss.teleportTo(HELLMANN_NIGHT_LOC, 0);
			}
			despawnCursedVillageNpcs();
		}
	}
	
	private void spawnCursedVillageNpcs()
	{
		for (Npc npc : _cursedVillageNpcs)
		{
			npc.getSpawn().setRespawnState(true);
			npc.getSpawn().doSpawn(false);
		}
	}
	
	private void despawnCursedVillageNpcs()
	{
		for (Npc npc : _cursedVillageNpcs)
		{
			npc.getSpawn().setRespawnState(false);
			npc.deleteMe();
		}
	}
}