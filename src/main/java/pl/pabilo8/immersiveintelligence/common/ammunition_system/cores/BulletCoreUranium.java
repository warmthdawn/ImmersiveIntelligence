package pl.pabilo8.immersiveintelligence.common.ammunition_system.cores;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import pl.pabilo8.immersiveintelligence.api.bullets.BulletRegistry.EnumComponentRole;
import pl.pabilo8.immersiveintelligence.api.bullets.IBulletCore;

/**
 * @author Pabilo8
 * @since 30-08-2019
 */
public class BulletCoreUranium implements IBulletCore
{
	@Override
	public String getName()
	{
		return "core_uranium";
	}

	@Override
	public IngredientStack getMaterial()
	{
		return new IngredientStack("nuggetUranium");
	}

	@Override
	public float getDensity()
	{
		return 0.45f;
	}

	@Override
	public float getDamageModifier()
	{
		return 1.35f;
	}

	@Override
	public float getExplosionModifier()
	{
		return 0.75f;
	}

	@Override
	public float getPenetrationHardness()
	{
		return 36;
	}

	@Override
	public int getColour()
	{
		return 0x659269;
	}
}
