package pl.pabilo8.immersiveintelligence.client.gui.ammunition_production;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import pl.pabilo8.immersiveintelligence.ImmersiveIntelligence;
import pl.pabilo8.immersiveintelligence.api.Utils;
import pl.pabilo8.immersiveintelligence.client.gui.elements.GuiLabelNoShadow;
import pl.pabilo8.immersiveintelligence.common.network.IIPacketHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * @author Pabilo8
 * @since 10-07-2019
 */
public abstract class GuiAmmunitionBase<T extends TileEntityMultiblockMetal> extends GuiIEContainerBase
{
	protected static final ResourceLocation TEXTURE = new ResourceLocation(ImmersiveIntelligence.MODID+":textures/gui/ammunition_workshop.png");
	protected static final ResourceLocation TEXTURE_ICONS = new ResourceLocation(ImmersiveIntelligence.MODID+":textures/gui/manual.png");
	T tile;

	public GuiAmmunitionBase(InventoryPlayer inventoryPlayer, T tile, BiFunction<InventoryPlayer, T, ContainerIEBase<T>> container)
	{
		super(container.apply(inventoryPlayer, tile));
		this.xSize = 220;
		this.ySize = 176;
		this.tile = tile;
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Utils.bindTexture(TEXTURE);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		Utils.drawPowerBar(guiLeft+161-4+48, guiTop+19, 7, 47, tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null));
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		this.renderHoveredToolTip(mx, my);

		ArrayList<String> tooltip = new ArrayList<>();
		drawTooltip(mx, my, tooltip);

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	ArrayList<String> drawTooltip(int mx, int my, ArrayList<String> tooltip)
	{
		if(isPointInRegion(161-4+48, 19, 7, 47, mx, my))
			tooltip.add(Utils.getPowerLevelString(tile));
		return tooltip;
	}


	void sendList(String name, String value)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(name, value);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, nbt));
	}

	protected GuiLabelNoShadow addLabel(int x, int y, int textColor, String... text)
	{
		return addLabel(x, y, 0, 0, textColor, text);
	}

	protected GuiLabelNoShadow addLabel(int x, int y, int w, int h, int textColor, String... text)
	{
		GuiLabelNoShadow guiLabel = new GuiLabelNoShadow(this.fontRenderer, labelList.size(), x, y, w, h, textColor);
		Arrays.stream(text).forEachOrdered(guiLabel::addLine);
		labelList.add(guiLabel);
		return guiLabel;
	}
}
