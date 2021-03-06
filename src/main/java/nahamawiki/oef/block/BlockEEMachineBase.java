package nahamawiki.oef.block;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import nahamawiki.oef.core.OEFItemCore;
import nahamawiki.oef.tileentity.ITileEntityEEMachine;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import takumicraft.Takumi.TakumiCraftCore;

public abstract class BlockEEMachineBase extends BlockOEFBase implements ITileEntityProvider {

	public BlockEEMachineBase() {
		super();
		this.setHardness(5.0F);
		this.setResistance(6000000.0F);
		this.setHarvestLevel("pickaxe", 3);
		this.setStepSound(soundTypeMetal);
	}

	@Override
	public abstract TileEntity createNewTileEntity(World world, int meta);

	/** ブロックが右クリックされた時の処理。 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float posX, float posY, float posZ) {
		if (player.getHeldItem() != null) {
			int item = 0;
			if (player.getHeldItem().getItem() == TakumiCraftCore.CreeperRod)
				item = 1;
			if (player.getHeldItem().isItemEqual(OEFItemCore.EEPliers))
				item = 2;
			if (item == 0)
				return false;
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity != null && tileEntity instanceof ITileEntityEEMachine) {
				ITileEntityEEMachine machine = (ITileEntityEEMachine) tileEntity;
				if (item == 1) {
					machine.setCreeper(true);
				} else if (item == 2) {
					machine.setCreeper(false);
				}
				return true;
			}
		}
		return false;
	}

	/** クリエイティブタブにブロックを登録する処理。 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		for (int i = 0; i < 4; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	/** ドロップアイテムのメタデータを返す。 */
	@Override
	public int damageDropped(int meta) {
		return meta & 3;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	/** ブロックの硬度を返す。 */
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		// 匠化していたら破壊されないようにする。
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof ITileEntityEEMachine) {
			ITileEntityEEMachine machine = (ITileEntityEEMachine) tileEntity;
			if (machine.getCreeper())
				return -1;
		}
		return this.blockHardness;
	}

	/** ブロックが爆破された時の処理。 */
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		// 匠化していたら破壊されないようにする。
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity != null && tileEntity instanceof ITileEntityEEMachine) {
			ITileEntityEEMachine machine = (ITileEntityEEMachine) tileEntity;
			if (machine.getCreeper())
				return;
		}
		super.onBlockExploded(world, x, y, z, explosion);
	}

}
