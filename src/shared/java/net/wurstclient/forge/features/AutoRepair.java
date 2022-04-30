package net.wurstclient.forge.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Feature;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.utils.InventoryUtils;
import net.wurstclient.forge.utils.SkyblockUtils;

public class AutoRepair extends Feature {

	private long timeOfLastRepair;
	
	public AutoRepair() {
		super("AutoRepair", 
			  "Repairs the tool in your hand if\n" + 
			   "it reaches 5% durability.\n\n" +
			   "Requires /repair.",
			  false);
		setCategory(Category.SKYBLOCK);
	}
	
	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		EntityPlayerSP player = event.getPlayer();
		InventoryPlayer inventory =  player.inventory;
		ItemStack currentStack = inventory.getCurrentItem();
		
		String rank = ForgeWurst.getForgeWurst().getFeatureController().getPlayerRank();
		if (!SkyblockUtils.canUseSpecialCommands(rank))
			return;

		if (!InventoryUtils.isTool(currentStack))
			return;
		
		double durability = 1 - currentStack.getItem().getDurabilityForDisplay(currentStack);
		long currentTime = Minecraft.getSystemTime();
		if ((timeOfLastRepair == 0 || currentTime > timeOfLastRepair + 1201000) && durability <= .05) {
			WMinecraft.getPlayer().sendChatMessage("/repair");
			timeOfLastRepair = currentTime;
		}
	}
	
}