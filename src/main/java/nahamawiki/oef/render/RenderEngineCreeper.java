package nahamawiki.oef.render;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import nahamawiki.oef.entity.EntityEngineCreeper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import takumicraft.Takumi.TakumiCraftCore;

/*
 * テクスチャへのResourceLocationを設定する.
 */

@SideOnly(Side.CLIENT)
public class RenderEngineCreeper extends RenderLiving {
	private static final ResourceLocation texture = new ResourceLocation("oef:textures/models/eng_creeper.png");
	private static final ResourceLocation texture_armor = new ResourceLocation("textures/entity/creeper/creeper_armor.png");

	/** The creeper model. */
	private final ModelBase creeperModel = new ModelCreeper(2.0F);

	public RenderEngineCreeper() {
		super(new ModelCreeper(), 0.5F);
	}

	/**
	 * Updates creeper scale in prerender callback
	 */
	protected void updateCreeperScale(EntityEngineCreeper par1EntityCreeper, float par2) {
		float f1 = par1EntityCreeper.getCreeperFlashIntensity(par2);
		float f2 = 1.0F + MathHelper.sin(f1 * 100.0F) * f1 * 0.01F;

		if (f1 < 0.0F) {
			f1 = 0.0F;
		}

		if (f1 > 1.0F) {
			f1 = 1.0F;
		}

		f1 *= f1;
		f1 *= f1;
		float f3 = (1.0F + f1 * 0.4F) * f2;
		float f4 = (1.0F + f1 * 0.1F) / f2;
		GL11.glScalef(f3, f4, f3);
	}

	/**
	 * Updates color multiplier based on creeper state called by getColorMultiplier
	 */
	protected int updateCreeperColorMultiplier(EntityEngineCreeper par1EntityCreeper, float par2, float par3) {
		float f2 = par1EntityCreeper.getCreeperFlashIntensity(par3);

		if ((int) (f2 * 10.0F) % 2 == 0) {
			return 0;
		} else {
			int i = (int) (f2 * 0.2F * 255.0F);

			if (i < 0) {
				i = 0;
			}

			if (i > 255) {
				i = 255;
			}

			short short1 = 255;
			short short2 = 255;
			short short3 = 255;
			return i << 24 | short1 << 16 | short2 << 8 | short3;
		}
	}

	/**
	 * A method used to render a creeper's powered form as a pass model.
	 */
	protected int renderCreeperPassModel(EntityEngineCreeper par1EntityCreeper, int par2, float par3) {
		if (par1EntityCreeper.getPowered()) {
			if (par1EntityCreeper.isInvisible()) {
				GL11.glDepthMask(false);
			} else {
				GL11.glDepthMask(true);
			}

			if (par2 == 1) {
				float f1 = par1EntityCreeper.ticksExisted + par3;
				this.bindTexture(texture_armor);
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				float f2 = f1 * 0.01F;
				float f3 = f1 * 0.01F;
				GL11.glTranslatef(f2, f3, 0.0F);
				this.setRenderPassModel(this.creeperModel);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_BLEND);
				float f4 = 0.5F;
				GL11.glColor4f(f4, f4, f4, 1.0F);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
				return 1;
			}

			if (par2 == 2) {
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}

		return -1;
	}

	protected int func_77061_b(EntityEngineCreeper par1EntityCreeper, int par2, float par3) {
		return -1;
	}

	protected ResourceLocation func_110829_a(EntityEngineCreeper par1EntityCreeper) {
		return texture;
	}

	/**
	 * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
	 * entityLiving, partialTickTime
	 */
	@Override
	protected void preRenderCallback(EntityLivingBase par1EntityLiving, float par2) {
		this.updateCreeperScale((EntityEngineCreeper) par1EntityLiving, par2);
	}

	/**
	 * Returns an ARGB int color back. Args: entityLiving, EngineBrightness, partialTickTime
	 */
	@Override
	protected int getColorMultiplier(EntityLivingBase par1EntityLiving, float par2, float par3) {
		return this.updateCreeperColorMultiplier((EntityEngineCreeper) par1EntityLiving, par2, par3);
	}

	/**
	 * Queries whether should render the specified pass or not.
	 */
	@Override
	protected int shouldRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3) {
		return this.renderCreeperPassModel((EntityEngineCreeper) par1EntityLiving, par2, par3);
	}

	@Override
	protected int inheritRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3) {
		return this.func_77061_b((EntityEngineCreeper) par1EntityLiving, par2, par3);
	}

	private static final ResourceLocation texture2 = new ResourceLocation("oef:textures/models/eng_creeper_.png");

	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity) {
		boolean flg = false;
		if (((EntityLiving) par1Entity).getCustomNameTag().startsWith(StatCollector.translateToLocal("TakumiCraft.newYear")) || ((EntityLiving) par1Entity).getCustomNameTag().startsWith("Kimono")) {
			flg = true;
		}
		if (TakumiCraftCore.TCDate(par1Entity.worldObj, flg) == 1) {
			return texture2;
		} else {
			return texture;
		}
	}

}