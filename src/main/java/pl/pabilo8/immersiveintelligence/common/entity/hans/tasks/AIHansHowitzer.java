package pl.pabilo8.immersiveintelligence.common.entity.hans.tasks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pl.pabilo8.immersiveintelligence.api.Utils;
import pl.pabilo8.immersiveintelligence.common.IIContent;
import pl.pabilo8.immersiveintelligence.common.entity.EntityFieldHowitzer;
import pl.pabilo8.immersiveintelligence.common.entity.EntityHans;
import pl.pabilo8.immersiveintelligence.common.entity.EntityVehicleSeat;
import pl.pabilo8.immersiveintelligence.common.entity.bullets.EntityBullet;

import java.util.List;
import java.util.Optional;

/**
 * @author Pabilo8
 * @since 05.04.2021
 */
public class AIHansHowitzer extends EntityAIBase
{
	private final EntityLiving hans;
	private EntityFieldHowitzer howitzer=null;
	private int seat=0;

	private Optional<Entity> target = Optional.empty();

	public AIHansHowitzer(EntityLiving hans)
	{
		this.hans=hans;
		this.setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute()
	{
		if(hans.getRidingEntity() instanceof EntityVehicleSeat && hans.getRidingEntity().getRidingEntity() instanceof EntityFieldHowitzer)
		{
			seat= ((EntityVehicleSeat)hans.getRidingEntity()).seatID;
			howitzer= ((EntityFieldHowitzer)hans.getRidingEntity().getRidingEntity());
		}
		else
		{
			hans.tasks.removeTask(this);
			return false;
		}

		if(howitzer==null||howitzer.isDead)
		{
			hans.tasks.removeTask(this);
			return false;
		}

		if(seat==0)
			target= Optional.ofNullable(hans.getAttackTarget());
		else
		{
			List<Entity> passengers = EntityVehicleSeat.getOrCreateSeat(howitzer, 0).getPassengers();
			if(passengers.size()>0&&passengers.get(0) instanceof EntityLiving)
			{
				EntityLiving entityLiving = (EntityLiving)passengers.get(0);
				target=Optional.ofNullable(entityLiving.getAttackTarget());
			}
		}
		return true;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void updateTask()
	{
			if(seat==0)
			{
				if(!(howitzer.turnLeft||howitzer.turnRight||howitzer.forward||howitzer.backward)&&target.isPresent())
				{
					Entity t = target.get();
					this.hans.faceEntity(t,10,10);

					float pp = getAnglePrediction(howitzer.getPositionVector().addVector(0, 1, 0), t.getPositionVector(), new Vec3d(t.motionX, t.motionY, t.motionZ))[1];

					howitzer.gunPitchUp = howitzer.gunPitch-pp < 0;
					howitzer.gunPitchDown = howitzer.gunPitch-pp > 0;
					if(Math.abs(howitzer.gunPitch-pp) < 5)
					{
						if(howitzer.shell.isEmpty())
						{
							Entity entity = EntityVehicleSeat.getOrCreateSeat(howitzer, 0).getPassengers().get(0);

							//it should be
							if(entity instanceof EntityHans&&((EntityHans)entity).getHeldItemMainhand().isEmpty())
							{
								ItemStack shell = IIContent.itemAmmoLightArtillery.getBulletWithParams("core_brass", "canister", "hmx", "tracer_powder");
								NBTTagCompound tag = new NBTTagCompound();
								tag.setInteger("colour", 0xff0000);
								//NBTTagCompound tag2 = new NBTTagCompound();
								//tag2.setString("text", "Das ist die Propaganda des Kriegsministeriums!\nGegen die Macht der Ingenieure hast du keine Chance!\nGib sofort auf, bis du kannst!");
								//IIContent.itemAmmoMachinegun.setComponentNBT(shell, tag2, tag);
								entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,shell);
							}
							else if(howitzer.reloadProgress==0&&!howitzer.reloadKeyPress)
								howitzer.reloadKeyPress=true;
						}
						else
						{
							if(howitzer.shootingProgress==0&&!howitzer.fireKeyPress)
								howitzer.fireKeyPress=true;
							else
								howitzer.fireKeyPress=false;
						}
						if(howitzer.fireKeyPress)
							howitzer.getWorld().getEntitiesWithinAABB(EntityItem.class,howitzer.getEntityBoundingBox()).forEach(Entity::setDead);
					}
				}
			}
			else
			{

				if(target.isPresent())
				{
					Entity entity = target.get();
					this.hans.faceEntity(entity,10,10);

					float[] yp = getAnglePrediction(howitzer.getPositionVector().addVector(0, 1, 0),entity.getPositionVector(),new Vec3d(entity.motionX,entity.motionY,entity.motionZ));
					if(!isAimedAt(yp[0],yp[1]))
					{
						float y =  MathHelper.wrapDegrees(360+yp[0]-this.howitzer.rotationYaw);
						howitzer.turnRight=Math.signum(y)>0;
						howitzer.turnLeft=Math.signum(y)<0;

						if(Math.abs(y) < 5)
						{
							howitzer.turnRight=false;
							howitzer.turnLeft=false;
							howitzer.rotationYaw=MathHelper.wrapDegrees(yp[0]);
							howitzer.prevRotationYaw=MathHelper.wrapDegrees(yp[0]);
						}
					}
					else
					{
						howitzer.turnRight=false;
						howitzer.turnLeft=false;
					}
				}
				else
				{
					howitzer.turnRight=false;
					howitzer.turnLeft=false;
				}
			}
	}

	public boolean isAimedAt(float yaw, float pitch)
	{
		return pitch==howitzer.gunPitch&&MathHelper.wrapDegrees(yaw)==howitzer.rotationYaw;
	}

	public float[] getAnglePrediction(Vec3d posTurret, Vec3d posTarget, Vec3d motion)
	{
		Vec3d vv = posTurret.subtract(posTarget).add(motion).normalize();
		float yy = (float)((Math.atan2(vv.x, vv.z)*180D)/3.1415927410125732D);
		float pp = Math.round(Utils.calculateBallisticAngle(
				posTurret.distanceTo(posTarget.add(motion))+10
				, (posTarget.y+motion.y)-posTurret.y,
				IIContent.itemAmmoMortar.getDefaultVelocity(),
				EntityBullet.GRAVITY*3.4f,
				1f-EntityBullet.DRAG));

		return new float[]{MathHelper.wrapDegrees(180-yy), 90-pp};
	}
}
