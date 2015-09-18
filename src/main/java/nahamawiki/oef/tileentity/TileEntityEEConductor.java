package nahamawiki.oef.tileentity;

import static net.minecraft.util.Facing.*;

import java.util.ArrayList;

import nahamawiki.oef.OEFCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class TileEntityEEConductor extends TileEntityEEMachineBase {

	protected int[] holdingEEArray = new int[6];
	protected ArrayList<Integer> reciver = new ArrayList<Integer>();
	protected ArrayList<Integer> provider = new ArrayList<Integer>();

	public TileEntityEEConductor() {
		super();
	}

	@Override
	public int getMachineType(int side) {
		return 3;
	}

	@Override
	public int reciveEE(int amount, int side) {
		holdingEEArray[side] += amount;
		return 0;
	}

	@Override
	public String[] getState(EntityPlayer player) {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		// if (player instanceof EntityPlayerMP) {
		// Packet packet = this.getDescriptionPacket();
		// if (packet != null) {
		// ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
		// }
		// } else {}
		return new String[] {
				StatCollector.translateToLocal("info.EEMachineState.name") + this.getBlockType().getLocalizedName(),
				StatCollector.translateToLocal("info.EEMachineState.level") + this.getLevel(this.getBlockMetadata()),
				StatCollector.translateToLocal("info.EEMachineState.meta") + this.getBlockMetadata(),
				StatCollector.translateToLocal("info.EEMachineState.holding") + this.holdingEE
		};
	}

	@Override
	public int getLevel(int meta) {
		return 0;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		for (int i = 0; i < 6; i++) {
			nbt.setInteger("holdingEEArray-" + i, holdingEEArray[i]);
		}

		NBTTagCompound localnbt = new NBTTagCompound();
		for (int i = 0; i < provider.size(); i++) {
			localnbt.setInteger(String.valueOf(i), provider.get(i));
		}
		nbt.setInteger("providerSize", provider.size());
		nbt.setTag("provider", localnbt);

		localnbt = new NBTTagCompound();
		for (int i = 0; i < reciver.size(); i++) {
			localnbt.setInteger(String.valueOf(i), reciver.get(i));
		}
		nbt.setInteger("reciverSize", reciver.size());
		nbt.setTag("reciver", localnbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		for (int i = 0; i < 6; i++) {
			holdingEEArray[i] = nbt.getInteger("holdingEEArray-" + i);
		}

		provider.clear();
		NBTTagCompound localnbt = nbt.getCompoundTag("provider");
		for (int i = 0; i < nbt.getInteger("providerSize"); i++) {
			provider.add(localnbt.getInteger(String.valueOf(i)));
		}

		reciver.clear();
		localnbt = nbt.getCompoundTag("reciver");
		for (int i = 0; i < nbt.getInteger("reciverSize"); i++) {
			reciver.add(localnbt.getInteger(String.valueOf(i)));
		}
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote)
			return;
		holdingEE = 0;
		for (int i = 0; i < 6; i++) {
			holdingEE += holdingEEArray[i];
		}
		if (reciver.size() < 1)
			return;
		for (int i = 0; i < 6; i++) {
			if (holdingEEArray[i] < 1)
				continue;
			int sendingEE;
			int reciverNum = reciver.size();
			if (reciver.contains(i)) {
				if (reciverNum < 2)
					continue;
				sendingEE = holdingEEArray[i] / (reciverNum - 1);
				holdingEEArray[i] %= (reciverNum - 1);
			} else {
				sendingEE = holdingEEArray[i] / reciverNum;
				holdingEEArray[i] %= reciverNum;
			}
			if (sendingEE < 1)
				continue;
			for (int j = 0; j < 6; j++) {
				if (reciver.contains(j) && j != i) {
					reciverNum--;
					ITileEntityEEMachine machine = (ITileEntityEEMachine) worldObj.getTileEntity(xCoord + offsetsXForSide[j], yCoord + offsetsYForSide[j], zCoord + offsetsZForSide[j]);
					int surplus = machine.reciveEE(sendingEE, oppositeSide[j]);
					if (surplus < 1)
						continue;
					if (reciverNum < 1) {
						holdingEEArray[i] += surplus;
						continue;
					}
					sendingEE += surplus / reciverNum;
					holdingEEArray[i] += surplus % reciverNum;
				}
			}
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("holdingEE", holdingEE);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.func_148857_g();
		holdingEE = nbt.getInteger("holdingEE");
		OEFCore.logger.info("Set holdingEE : " + holdingEE);
	}

	/** 周囲のブロックを確認する */
	public void updateDirection(World world, int x, int y, int z) {
		provider.clear();
		reciver.clear();
		for (int i = 0; i < 6; i++) {
			TileEntity tileEntity = world.getTileEntity(x + offsetsXForSide[i], y + offsetsYForSide[i], z + offsetsZForSide[i]);
			if (tileEntity != null && tileEntity instanceof ITileEntityEEMachine) {
				ITileEntityEEMachine machine = (ITileEntityEEMachine) tileEntity;
				int type = machine.getMachineType(oppositeSide[i]);
				if ((type & 1) == 1) {
					provider.add(i);
				}
				if ((type & 2) == 2) {
					reciver.add(i);
				}
			}
		}
	}

}
