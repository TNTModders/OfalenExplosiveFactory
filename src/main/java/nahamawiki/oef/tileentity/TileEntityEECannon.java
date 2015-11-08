package nahamawiki.oef.tileentity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import nahamawiki.oef.entity.EntityCannonBlueLaser;
import nahamawiki.oef.entity.EntityCannonBoltLaser;
import nahamawiki.oef.entity.EntityCannonEPLaser;
import nahamawiki.oef.entity.EntityCannonGreenLaser;
import nahamawiki.oef.entity.EntityCannonRedLaser;
import nahamawiki.oef.entity.EntityCannonWhiteLaser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public class TileEntityEECannon extends TileEntityEEMachineBase {

	private int duration;
	public String color = "";
	private String ownName;
	private EntityLivingBase targetEntity;
	public int size;
	/** 左右角 */
	private float rotationYaw = 0;
	private float prevRotationYaw;
	/** 上下角 */
	private float rotationPitch = 0;
	private float prevRotationPitch;
	private boolean isSpawning;

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getOwnPlayer() {
		return this.ownName;
	}

	public void setOwnPlayer(String name) {
		this.ownName = name;
	}

	@Override
	public String[] getState() {
		String color1 = "";
		if (color.length() > 0)
			color1 = StatCollector.translateToLocal("info.color:" + new String(color).toLowerCase());
		return new String[] {
				StatCollector.translateToLocal("info.EEMachineState.name") + StatCollector.translateToLocal(this.getBlockType().getLocalizedName()),
				StatCollector.translateToLocal("info.EEMachineState.capacity") + capacity + " EE",
				StatCollector.translateToLocal("info.EEMachineState.holding") + holdingEE + " EE",
				StatCollector.translateToLocal("info.EEMachineState.crystal") + color1,
				StatCollector.translateToLocal("info.EEMachineState.charged") + size
		};
	}

	@Override
	public byte getLevel(int meta) {
		return 0;
	}

	@Override
	public int getCapacity(int level) {
		return 800;
	}

	@Override
	public void updateMachine() {
		this.angleUpdate();
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		if (duration > 0)
			duration--;
		if (holdingEE < 1 && !getCreeper())
			return;
		EntityPlayer player = null;
		for (Object entity : worldObj.playerEntities) {
			if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getDisplayName().equalsIgnoreCase(getOwnPlayer())) {
				player = (EntityPlayer) entity;
				break;
			}
		}
		if (player == null) {
			return;
		}
		if (this.targetEntity == null || this.targetEntity.isDead) {
			List list = this.worldObj.loadedEntityList;
			Collections.sort(list, new TileEntityEECannon.Sorter(player));
			if (!list.isEmpty()) {
				for (Object entity : list) {
					if (entity instanceof EntityMob) {
						this.targetEntity = (EntityLivingBase) entity;
						break;
					}
				}
			}
		}
		if (this.targetEntity != null && this.targetEntity.isDead) {
			this.targetEntity = null;
		}

		if (duration < 1 && color != null && size > 0 && holdingEE >= 400 && targetEntity != null) {
			duration = 10;
			NBTTagCompound localnbt = new NBTTagCompound();
			if (color.equals("Red")) {
				for (int i = -2; i < 3; i++) {
					EntityCannonRedLaser laser = new EntityCannonRedLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch, i);
					worldObj.spawnEntityInWorld(laser);
				}
			} else if (color.equals("Green")) {
				EntityCannonGreenLaser laser = new EntityCannonGreenLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			} else if (color.equals("Blue")) {
				EntityCannonBlueLaser laser = new EntityCannonBlueLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			} else if (color.equals("White")) {
				for (int i = -2; i < 3; i++) {
					EntityCannonWhiteLaser laser = new EntityCannonWhiteLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch, i);
					worldObj.spawnEntityInWorld(laser);
				}
			}
			isSpawning = true;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			this.size--;
			holdingEE -= 400;
			if (size < 1) {
				this.setColor("");
			}
		}

		if(this.getCreeper() && duration < 1)
		{
			List list = this.worldObj.playerEntities;
			if (!list.isEmpty()) {
				for (Object entity : list) {
					if (entity instanceof EntityPlayer) {
						this.targetEntity = (EntityPlayer) entity;
						break;
					}
				}
			}

			duration = 10;
			Random rand = new Random();
			if(rand.nextBoolean())
			{
				EntityCannonBoltLaser laser = new EntityCannonBoltLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			}
			else
			{
				EntityCannonEPLaser laser = new EntityCannonEPLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			}

			isSpawning = true;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void updateCreepered() {
		// TODO 匠化の実装
	}

	protected void angleUpdate() {
		if (this.targetEntity != null) {
			double x = this.targetEntity.posX - this.xCoord;
			double y = this.targetEntity.posY - this.yCoord;
			double z = this.targetEntity.posZ - this.zCoord;
			double distance = MathHelper.sqrt_double(x * x + z * z);
			float pYaw = (float) (Math.atan2(z, x) * (180.0D / Math.PI)) - 90.0F;
			float pPitch = (float) (-(Math.atan2(y, distance) * (180.0D / Math.PI)));
			this.rotationPitch = MathHelper.wrapAngleTo180_float(this.updateRotation(this.rotationPitch, pPitch, 360f));
			this.rotationYaw = MathHelper.wrapAngleTo180_float(this.updateRotation(this.rotationYaw, pYaw, 360f));

			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	private float updateRotation(float par1, float par2, float par3) {
		float f3 = MathHelper.wrapAngleTo180_float(par2 - par1);

		if (f3 > par3) {
			f3 = -par3;
		}

		if (f3 < -par3) {
			f3 = par3;
		}

		return par1 + f3;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.setColor(nbt.getString("Color"));
		if (nbt.hasKey("Player"))
			this.setOwnPlayer(nbt.getString("Player"));
		duration = nbt.getInteger("Duration");
		size = nbt.getInteger("Size");
		rotationYaw = nbt.getFloat("rotationYaw");
		rotationPitch = nbt.getFloat("rotationPitch");
		if(nbt.getBoolean("isCreeper"))
		{
			this.setCreeper(true);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("Color", this.getColor());
		if (this.getOwnPlayer() != null)
			nbt.setString("Player", this.getOwnPlayer());
		nbt.setInteger("Duration", duration);
		nbt.setInteger("Size", size);
		nbt.setFloat("rotationYaw", rotationYaw);
		nbt.setFloat("rotationPitch", rotationPitch);
		if(getCreeper())
		{
			nbt.setBoolean("isCreeper", getCreeper());
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (isSpawning) {
			nbt.setBoolean("isSpawning", true);
			nbt.setString("color", color);
			isSpawning = false;
		} else {
			nbt.setBoolean("isSpawning", false);
		}
		nbt.setFloat("rotationYaw", rotationYaw);
		nbt.setFloat("rotationPitch", rotationPitch);
		if(getCreeper())
		{
			nbt.setBoolean("isCreeper", getCreeper());
		}
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.func_148857_g();
		rotationYaw = nbt.getFloat("rotationYaw");
		rotationPitch = nbt.getFloat("rotationPitch");
		if(nbt.getBoolean("isCreeper"))
		{
			this.setCreeper(true);
		}
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (nbt.getBoolean("isSpawning")) {
			color = nbt.getString("color");

			if (color.equals("Red")) {
				for (int i = -2; i < 3; i++) {
					EntityCannonRedLaser laser = new EntityCannonRedLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch, i);
					worldObj.spawnEntityInWorld(laser);
				}
			} else if (color.equals("Green")) {
				EntityCannonGreenLaser laser = new EntityCannonGreenLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			} else if (color.equals("Blue")) {
				EntityCannonBlueLaser laser = new EntityCannonBlueLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(laser);
			} else if (color.equals("White")) {
				for (int i = -2; i < 3; i++) {
					EntityCannonWhiteLaser laser = new EntityCannonWhiteLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch, i);
					worldObj.spawnEntityInWorld(laser);
				}
			}
			
			if(this.getCreeper())
			{
				Random rand = new Random();
				if(rand.nextBoolean())
				{
					EntityCannonBoltLaser laser = new EntityCannonBoltLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
					worldObj.spawnEntityInWorld(laser);
				}
				else
				{
					EntityCannonEPLaser laser = new EntityCannonEPLaser(player, worldObj, xCoord + 0.5, yCoord + 0.65, zCoord + 0.5, rotationYaw, rotationPitch);
					worldObj.spawnEntityInWorld(laser);
				}
			}
		}
	}

	public static class Sorter implements Comparator {
		private final Entity theEntity;

		public Sorter(Entity entity) {
			this.theEntity = entity;
		}

		public int compare(Entity entity1, Entity entity2) {
			double d0 = this.theEntity.getDistanceSqToEntity(entity1);
			double d1 = this.theEntity.getDistanceSqToEntity(entity2);
			return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
		}

		@Override
		public int compare(Object par1, Object par2) {
			return this.compare((Entity) par1, (Entity) par2);
		}
	}

	public float getRotationPitch() {
		return rotationPitch;
	}

	public float getPrevRotationPitch() {
		return prevRotationPitch;
	}

	public float getRotationYaw() {
		return rotationYaw;
	}

	public float getPrevRotationYaw() {
		return prevRotationYaw;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		tick++;
		if (level < 0)
			level = this.getLevel(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
		if (capacity < 0)
			capacity = this.getCapacity(level);
		if (worldObj.isRemote)
			return;
		this.updateMachine();
	}
}
