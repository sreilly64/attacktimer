package com.attacktimer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import rr.raids.tombsofamascutstats.TombsOfAmascutStatsPlugin;

public class AttackTimerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AttackTimerMetronomePlugin.class, TombsOfAmascutStatsPlugin.class);
		RuneLite.main(args);
	}
}