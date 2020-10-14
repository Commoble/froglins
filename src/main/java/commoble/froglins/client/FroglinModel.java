package commoble.froglins.client;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import commoble.froglins.FroglinEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.6.6
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

public class FroglinModel extends BipedModel<FroglinEntity>
{
	public static final FroglinModel BASE = new FroglinModel(0F);
	
//	private final ModelRenderer headPivot;
//	private final ModelRenderer bodyPivot;
//	private final ModelRenderer rightArmPivot;
//	private final ModelRenderer leftArmPivot;
//	private final ModelRenderer rightThighPivot;
//	private final ModelRenderer leftThighPivot;
	private final ModelRenderer rightArmClaws;
	private final ModelRenderer leftArmClaws;

	public FroglinModel(float scale)
	{
		super(scale, 0F, 64, 64);
//		this.textureWidth = 64;
//		this.textureHeight = 64;

		this.bipedHead = new ModelRenderer(this);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHead.setTextureOffset(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, 0.01F, false);

		this.bipedBody = new ModelRenderer(this);
		this.bipedBody.setRotationPoint(0.0F, 2.0F, 0.0F);
		this.setRotationAngle(this.bipedBody, 0.3491F, 0.0F, 0.0F);
		this.bipedBody.setTextureOffset(16, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 12.0F, 4.0F, 0.0F, false);

		this.bipedRightArm = new ModelRenderer(this);
		this.bipedRightArm.setRotationPoint(-2.5F, 5.0F, 0.0F);
		this.setRotationAngle(this.bipedRightArm, -0.1745F, 0.0F, 0.0F);
		this.bipedRightArm.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, false);

		this.rightArmClaws = new ModelRenderer(this);
		this.rightArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
		this.bipedRightArm.addChild(this.rightArmClaws);
		this.setRotationAngle(this.rightArmClaws, 0.2618F, 0.0F, 0.0F);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);

		this.bipedLeftArm = new ModelRenderer(this);
		this.bipedLeftArm.setRotationPoint(2.5F, 5.0F, 0.0F);
		this.setRotationAngle(this.bipedLeftArm, -0.1745F, 0.0F, 0.0F);
		this.bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, true);

		this.leftArmClaws = new ModelRenderer(this);
		this.leftArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
		this.bipedLeftArm.addChild(this.leftArmClaws);
		this.setRotationAngle(this.leftArmClaws, 0.2618F, 0.0F, 0.0F);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);

		this.bipedRightLeg = new ModelRenderer(this);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 4.0F);
		this.bipedRightLeg.setTextureOffset(0, 16).addBox(-2.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

		this.bipedLeftLeg = new ModelRenderer(this);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 4.0F);
		this.bipedLeftLeg.setTextureOffset(0, 16).addBox(-0.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);

//		this.headPivot = new ModelRenderer(this);
//		this.headPivot.setRotationPoint(0.0F, 0.0F, 0.0F);
//		this.headPivot.setTextureOffset(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, 0.01F, false);
//
//		this.bodyPivot = new ModelRenderer(this);
//		this.bodyPivot.setRotationPoint(0.0F, 2.0F, 0.0F);
//		this.setRotationAngle(this.bodyPivot, 0.3491F, 0.0F, 0.0F);
//		this.bodyPivot.setTextureOffset(16, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 12.0F, 4.0F, 0.0F, false);
//
//		this.rightArmPivot = new ModelRenderer(this);
//		this.rightArmPivot.setRotationPoint(-2.5F, 5.0F, 0.0F);
//		this.setRotationAngle(this.rightArmPivot, -0.1745F, 0.0F, 0.0F);
//		this.rightArmPivot.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, false);
//
//		this.rightArmClaws = new ModelRenderer(this);
//		this.rightArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
//		this.rightArmPivot.addChild(this.rightArmClaws);
//		this.setRotationAngle(this.rightArmClaws, 0.2618F, 0.0F, 0.0F);
//		this.rightArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.rightArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.rightArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//
//		this.leftArmPivot = new ModelRenderer(this);
//		this.leftArmPivot.setRotationPoint(2.5F, 5.0F, 0.0F);
//		this.setRotationAngle(this.leftArmPivot, -0.1745F, 0.0F, 0.0F);
//		this.leftArmPivot.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, true);
//
//		this.leftArmClaws = new ModelRenderer(this);
//		this.leftArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
//		this.leftArmPivot.addChild(this.leftArmClaws);
//		this.setRotationAngle(this.leftArmClaws, 0.2618F, 0.0F, 0.0F);
//		this.leftArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.leftArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.leftArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//
//		this.rightThighPivot = new ModelRenderer(this);
//		this.rightThighPivot.setRotationPoint(-2.0F, 12.0F, 4.0F);
//		this.rightThighPivot.setTextureOffset(0, 16).addBox(-2.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);
//
//		this.leftThighPivot = new ModelRenderer(this);
//		this.leftThighPivot.setRotationPoint(2.0F, 12.0F, 4.0F);
//		this.leftThighPivot.setTextureOffset(0, 16).addBox(-0.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);
	}

	// the base method sets rotation points to specific positions, which we don't want
	// so we have to override the *entire method*
	@Override
	public void setRotationAngles(FroglinEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.copyBaseBone(model -> model.bipedBody);
		this.copyBaseBone(model -> model.bipedHead);
		this.copyBaseBone(model -> model.bipedRightArm);
		this.copyBaseBone(model -> model.bipedLeftArm);
		this.copyBaseBone(model -> model.bipedRightLeg);
		this.copyBaseBone(model -> model.bipedLeftLeg);
		
		boolean hasBeenGliding = entityIn.getTicksElytraFlying() > 4;
		boolean isSwimming = entityIn.isActualySwimming();
		this.bipedHead.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
		if (hasBeenGliding)
		{
			this.bipedHead.rotateAngleX = (-(float) Math.PI / 4F);
		}
		else if (this.swimAnimation > 0.0F)
		{
			if (isSwimming)
			{
				this.bipedHead.rotateAngleX = this.rotLerpRad(this.swimAnimation, this.bipedHead.rotateAngleX, (-(float) Math.PI / 4F));
			}
			else
			{
				this.bipedHead.rotateAngleX = this.rotLerpRad(this.swimAnimation, this.bipedHead.rotateAngleX, headPitch * ((float) Math.PI / 180F));
			}
		}
		else
		{
			this.bipedHead.rotateAngleX = headPitch * ((float) Math.PI / 180F);
		}

		float glideFactor = 1.0F;
		if (hasBeenGliding)
		{
			glideFactor = (float) entityIn.getMotion().lengthSquared();
			glideFactor = glideFactor / 0.2F;
			glideFactor = glideFactor * glideFactor * glideFactor;
		}

		if (glideFactor < 1.0F)
		{
			glideFactor = 1.0F;
		}

		this.bipedRightArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / glideFactor;
		this.bipedLeftArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / glideFactor;
		this.bipedRightLeg.rotateAngleX = BASE.bipedRightLeg.rotateAngleX + MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / glideFactor;
		this.bipedLeftLeg.rotateAngleX = BASE.bipedLeftLeg.rotateAngleX + MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / glideFactor;
		if (this.isSitting)
		{
			this.bipedRightArm.rotateAngleX += (-(float) Math.PI / 5F);
			this.bipedLeftArm.rotateAngleX += (-(float) Math.PI / 5F);
			this.bipedRightLeg.rotateAngleX = -1.4137167F;
			this.bipedRightLeg.rotateAngleY = ((float) Math.PI / 10F);
			this.bipedRightLeg.rotateAngleZ = 0.07853982F;
			this.bipedLeftLeg.rotateAngleX = -1.4137167F;
			this.bipedLeftLeg.rotateAngleY = (-(float) Math.PI / 10F);
			this.bipedLeftLeg.rotateAngleZ = -0.07853982F;
		}
		
		// this part deals with held items
//		boolean isRightHanded = entityIn.getPrimaryHand() == HandSide.RIGHT;
//		boolean isHoldingOffhandItem = isRightHanded ? this.leftArmPose.func_241657_a_() : this.rightArmPose.func_241657_a_();
//		if (isRightHanded != isHoldingOffhandItem)
//		{
//			this.func_241655_c_(entityIn);
//			this.func_241654_b_(entityIn);
//		}
//		else
//		{
//			this.func_241654_b_(entityIn);
//			this.func_241655_c_(entityIn);
//		}

		this.func_230486_a_(entityIn, ageInTicks);
//		if (this.isSneak)
//		{
//			this.bipedBody.rotateAngleX = 0.5F;
//			this.bipedRightArm.rotateAngleX += 0.4F;
//			this.bipedLeftArm.rotateAngleX += 0.4F;
//			this.bipedRightLeg.rotationPointZ = 4.0F;
//			this.bipedLeftLeg.rotationPointZ = 4.0F;
//			this.bipedRightLeg.rotationPointY = 12.2F;
//			this.bipedLeftLeg.rotationPointY = 12.2F;
//			this.bipedHead.rotationPointY = 4.2F;
//			this.bipedBody.rotationPointY = 3.2F;
//			this.bipedLeftArm.rotationPointY = 5.2F;
//			this.bipedRightArm.rotationPointY = 5.2F;
//		}

		ModelHelper.func_239101_a_(this.bipedRightArm, this.bipedLeftArm, ageInTicks);
		if (this.swimAnimation > 0.0F)
		{
			float limbSwingMod26 = limbSwing % 26.0F;
			HandSide handside = this.getMainHand(entityIn);
			float rightArmSwing = handside == HandSide.RIGHT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
			float leftArmSwing = handside == HandSide.LEFT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
			if (limbSwingMod26 < 14.0F)
			{
				this.bipedLeftArm.rotateAngleX = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleX, 0.0F);
				this.bipedRightArm.rotateAngleX = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleX, 0.0F);
				this.bipedLeftArm.rotateAngleY = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
				this.bipedRightArm.rotateAngleY = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleY, (float) Math.PI);
				this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleZ,
					(float) Math.PI + 1.8707964F * this.getSwimArmAngleSquared(limbSwingMod26) / this.getSwimArmAngleSquared(14.0F));
				this.bipedRightArm.rotateAngleZ = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleZ,
					(float) Math.PI - 1.8707964F * this.getSwimArmAngleSquared(limbSwingMod26) / this.getSwimArmAngleSquared(14.0F));
			}
			else if (limbSwingMod26 >= 14.0F && limbSwingMod26 < 22.0F)
			{
				float swingLerp = (limbSwingMod26 - 14.0F) / 8.0F;
				this.bipedLeftArm.rotateAngleX = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2F) * swingLerp);
				this.bipedRightArm.rotateAngleX = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2F) * swingLerp);
				this.bipedLeftArm.rotateAngleY = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
				this.bipedRightArm.rotateAngleY = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleY, (float) Math.PI);
				this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleZ, 5.012389F - 1.8707964F * swingLerp);
				this.bipedRightArm.rotateAngleZ = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleZ, 1.2707963F + 1.8707964F * swingLerp);
			}
			else if (limbSwingMod26 >= 22.0F && limbSwingMod26 < 26.0F)
			{
				float swingLerp = (limbSwingMod26 - 22.0F) / 4.0F;
				this.bipedLeftArm.rotateAngleX = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleX, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * swingLerp);
				this.bipedRightArm.rotateAngleX = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleX, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * swingLerp);
				this.bipedLeftArm.rotateAngleY = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleY, (float) Math.PI);
				this.bipedRightArm.rotateAngleY = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleY, (float) Math.PI);
				this.bipedLeftArm.rotateAngleZ = this.rotLerpRad(leftArmSwing, this.bipedLeftArm.rotateAngleZ, (float) Math.PI);
				this.bipedRightArm.rotateAngleZ = MathHelper.lerp(rightArmSwing, this.bipedRightArm.rotateAngleZ, (float) Math.PI);
			}

			this.bipedLeftLeg.rotateAngleX = MathHelper.lerp(this.swimAnimation, this.bipedLeftLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float) Math.PI));
			this.bipedRightLeg.rotateAngleX = MathHelper.lerp(this.swimAnimation, this.bipedRightLeg.rotateAngleX, 0.3F * MathHelper.cos(limbSwing * 0.33333334F));
		}

		this.bipedHeadwear.copyModelAngles(this.bipedHead);
	}
	
	// affects arm swing while attacking, I think
	@Override
	protected void func_230486_a_(FroglinEntity froglin, float ageInTicks)
	{
		if (!(this.swingProgress <= 0.0F))
		{
			HandSide mainHand = this.getMainHand(froglin);
			ModelRenderer mainArmRenderer = this.getArmForSide(mainHand);
			float swingProgress = this.swingProgress;
			this.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float) Math.PI * 2F)) * 0.2F;
			if (mainHand == HandSide.LEFT)
			{
				this.bipedBody.rotateAngleY *= -1.0F;
			}

//			this.bipedRightArm.rotationPointZ += MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedRightArm.rotationPointX -= MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedLeftArm.rotationPointZ -= MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedLeftArm.rotationPointX += MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
			this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY;
			this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY;
			this.bipedLeftArm.rotateAngleX += this.bipedBody.rotateAngleY;
			swingProgress = 1.0F - this.swingProgress;
			swingProgress = swingProgress * swingProgress * swingProgress;
			swingProgress = 1.0F - swingProgress;
			float swingRadians = MathHelper.sin(swingProgress * (float) Math.PI);
			float headRadians = MathHelper.sin(this.swingProgress * (float) Math.PI) * -(this.bipedHead.rotateAngleX - 0.7F) * 0.75F;
			mainArmRenderer.rotateAngleX = (float) (mainArmRenderer.rotateAngleX - (swingRadians * 1.2D + headRadians));
			mainArmRenderer.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
			mainArmRenderer.rotateAngleZ += MathHelper.sin(this.swingProgress * (float) Math.PI) * -0.4F;
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		this.bipedHead.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bipedBody.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bipedRightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bipedLeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bipedRightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bipedLeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
	
	public void copyBaseBone(Function<FroglinModel, ModelRenderer> getter)
	{
		getter.apply(this).copyModelAngles(getter.apply(BASE));
	}

	// private method from BipedModel
	private float getSwimArmAngleSquared(float limbSwing)
	{
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}
}