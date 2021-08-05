package commoble.froglins.client;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import commoble.froglins.FroglinEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import net.minecraft.world.item.alchemy.PotionBrewing;

import static commoble.froglins.client.ClientEvents.FROGLIN;

// Made with Blockbench 3.6.6
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

public class FroglinModel extends HumanoidModel<FroglinEntity>

	// new ColdSnapStabberModel<>(p_174304_.bakeLayer(COLDSNAPSTABBER)
	//something similar to this may be the solution the crouched problem


{
	public static FroglinModel BASE;
	public static FroglinModel CROUCHED;

	static ModelPart constructorPart;
//	private final ModelRenderer headPivot;
//	private final ModelRenderer bodyPivot;
//	private final ModelRenderer rightArmPivot;
//	private final ModelRenderer leftArmPivot;
//	private final ModelRenderer rightThighPivot;
//	private final ModelRenderer leftThighPivot;
	private static final String RIGHTARMCLAW = "rightarmclaw";
	private static final String LEFTARMCLAW = "leftarmclaw";
	private final ModelPart rightArmClaws;
	private final ModelPart leftArmClaws;

	public FroglinModel(ModelPart part)
	{
		super(part);
		this.rightArmClaws = part.getChild(RIGHTARMCLAW);
		this.leftArmClaws = part.getChild(LEFTARMCLAW);
		head.setPos(0, 0 ,0);
		body.setPos(0.0F, 2.0F, 0.0F);
		body.setRotation(0.3491F, 0.0F, 0.0F);
		rightArm.setPos(-2.5F, 5.0F, 0.0F);
		rightArm.setRotation(-0.1745F, 0.0F, 0.0F);
		rightArmClaws.setPos(0, 23, 5);
		rightArmClaws.setRotation( 0.0873F, 0.0F, 0.0F); //0.2618

		leftArm.setPos(2.5f, 5.0f, 0.0f);
		leftArm.setRotation(-0.1745F, 0.0F, 0.0F);
		leftArmClaws.setPos(5, 23, 5);
		leftArmClaws.setRotation(0.0873F, 0.0F, 0.0F); //0.2618
		rightLeg.setPos(-2, 12, 4);
		leftLeg.setPos(2, 12, 4);

//		super(scale, 0F, 64, 64);
////		this.textureWidth = 64;
////		this.textureHeight = 64;
//
//		this.head = new ModelPart(this);
//		this.head.setPos(0.0F, 0.0F, 0.0F);
//		this.head.texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, 0.01F, false);
//
//		this.body = new ModelPart(this);
//		this.body.setPos(0.0F, 2.0F, 0.0F);
//		this.setRotationAngle(this.body, 0.3491F, 0.0F, 0.0F);
//		this.body.texOffs(16, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 12.0F, 4.0F, 0.0F, false);
//
//		this.rightArm = new ModelPart(this);
//		this.rightArm.setPos(-2.5F, 5.0F, 0.0F);
//		this.setRotationAngle(this.rightArm, -0.1745F, 0.0F, 0.0F);
//		this.rightArm.texOffs(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, false);
//
//		this.rightArmClaws = new ModelPart(this);
//		this.rightArmClaws.setPos(2.5F, 18.0F, 5.0F);
//		this.rightArm.addChild(this.rightArmClaws);
//		this.setRotationAngle(this.rightArmClaws, 0.2618F, 0.0F, 0.0F);
//		this.rightArmClaws.texOffs(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.rightArmClaws.texOffs(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.rightArmClaws.texOffs(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//
//		this.leftArm = new ModelPart(this);
//		this.leftArm.setPos(2.5F, 5.0F, 0.0F);
//		this.setRotationAngle(this.leftArm, -0.1745F, 0.0F, 0.0F);
//		this.leftArm.texOffs(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, true);
//
//		this.leftArmClaws = new ModelPart(this);
//		this.leftArmClaws.setPos(2.5F, 18.0F, 5.0F);
//		this.leftArm.addChild(this.leftArmClaws);
//		this.setRotationAngle(this.leftArmClaws, 0.2618F, 0.0F, 0.0F);
//		this.leftArmClaws.texOffs(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.leftArmClaws.texOffs(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//		this.leftArmClaws.texOffs(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
//
//		this.rightLeg = new ModelPart(this);
//		this.rightLeg.setPos(-2.0F, 12.0F, 4.0F);
//		this.rightLeg.texOffs(0, 16).addBox(-2.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);
//
//		this.leftLeg = new ModelPart(this);
//		this.leftLeg.setPos(2.0F, 12.0F, 4.0F);
//		this.leftLeg.texOffs(0, 16).addBox(-0.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);



		//ORIGINAL COMMMENT OUT POINT
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

	public static LayerDefinition createLayer(){
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(0.01f);
		partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0,0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, cubeDeformation), PartPose.ZERO);
		partDefinition.addOrReplaceChild("hat", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1, -1, -1, 0, 0, 0), PartPose.ZERO);
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16,16).addBox(-3, 0, -2, 6, 12, 4), PartPose.ZERO);
		partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-1, -1, -2, 2, 9, 3), PartPose.ZERO);
		partDefinition.addOrReplaceChild(RIGHTARMCLAW, CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.6f, -12f, -3.5f, 0f, 4f, 1f)
				.texOffs(0,0).addBox(-2.4f, -12f, -3.5f, 0f, 4f, 1f)
				.texOffs(0,0).addBox(-3.3f, -12f, -3.5f, 0f, 4f, 1f), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-1f, -1f, -2f, 2f, 9f, 3f), PartPose.ZERO);
		partDefinition.addOrReplaceChild(LEFTARMCLAW, CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.6f, -12f, -3.5f, 0f, 4f, 1f)
				.texOffs(0,0).addBox(-2.4f, -12f, -3.5f, 0f, 4f, 1f)
				.texOffs(0,0).addBox(-3.3f, -12f, -3.5f, 0f, 4f, 1f), PartPose.ZERO);
		partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-2.5f, 0f, -1f, 3f, 12f, 3f), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-0.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);


	}


	// the base method sets rotation points to specific positions, which we don't want
	// so we have to override the *entire method*
	@Override
	public void setupAnim(FroglinEntity froglin, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		FroglinModel currentModel = froglin.getPose() == Pose.CROUCHING ? CROUCHED : BASE;
		this.copyBaseBone(currentModel, model -> model.body);
		this.copyBaseBone(currentModel, model -> model.head);
		this.copyBaseBone(currentModel, model -> model.rightArm);
		this.copyBaseBone(currentModel, model -> model.leftArm);
		this.copyBaseBone(currentModel, model -> model.rightLeg);
		this.copyBaseBone(currentModel, model -> model.leftLeg);
		
		boolean hasBeenGliding = froglin.getFallFlyingTicks() > 4;
		boolean isSwimming = froglin.isVisuallySwimming();
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		if (hasBeenGliding)
		{
			this.head.xRot = (-(float) Math.PI / 4F);
		}
		else if (this.swimAmount > 0.0F)
		{
			if (isSwimming)
			{
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float) Math.PI / 4F));
			}
			else
			{
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, headPitch * ((float) Math.PI / 180F));
			}
		}
		else
		{
			this.head.xRot = headPitch * ((float) Math.PI / 180F);
		}

		float glideFactor = 1.0F;
		if (hasBeenGliding)
		{
			glideFactor = (float) froglin.getDeltaMovement().lengthSqr();
			glideFactor = glideFactor / 0.2F;
			glideFactor = glideFactor * glideFactor * glideFactor;
		}

		if (glideFactor < 1.0F)
		{
			glideFactor = 1.0F;
		}

		this.rightArm.xRot += Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / glideFactor;
		this.leftArm.xRot += Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / glideFactor;
		this.rightLeg.xRot = this.rightLeg.xRot + Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / glideFactor;
		this.leftLeg.xRot = this.leftLeg.xRot + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / glideFactor;
		if (this.riding)
		{
			this.rightArm.xRot += (-(float) Math.PI / 5F);
			this.leftArm.xRot += (-(float) Math.PI / 5F);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = ((float) Math.PI / 10F);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (-(float) Math.PI / 10F);
			this.leftLeg.zRot = -0.07853982F;
		}
		
		// this part deals with held items
//		boolean isRightHanded = entityIn.getPrimaryHand() == HandSide.RIGHT;
//		boolean isHoldingOffhandItem = isRightHanded ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
//		if (isRightHanded != isHoldingOffhandItem)
//		{
//			this.poseLeftArm(entityIn);
//			this.poseRightArm(entityIn);
//		}
//		else
//		{
//			this.poseRightArm(entityIn);
//			this.poseLeftArm(entityIn);
//		}

		this.setupAttackAnimation(froglin, ageInTicks);
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

		AnimationUtils.bobArms(this.rightArm, this.leftArm, ageInTicks);
		if (this.swimAmount > 0.0F)
		{
			float limbSwingMod26 = limbSwing % 26.0F;
			HumanoidArm handside = this.getAttackArm(froglin);
			float rightArmSwing = handside == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float leftArmSwing = handside == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			if (limbSwingMod26 < 14.0F)
			{
				this.leftArm.xRot = this.rotlerpRad(leftArmSwing, this.leftArm.xRot, 0.0F);
				this.rightArm.xRot = Mth.lerp(rightArmSwing, this.rightArm.xRot, 0.0F);
				this.leftArm.yRot = this.rotlerpRad(leftArmSwing, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(rightArmSwing, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(leftArmSwing, this.leftArm.zRot,
					(float) Math.PI + 1.8707964F * this.getSwimArmAngleSquared(limbSwingMod26) / this.getSwimArmAngleSquared(14.0F));
				this.rightArm.zRot = Mth.lerp(rightArmSwing, this.rightArm.zRot,
					(float) Math.PI - 1.8707964F * this.getSwimArmAngleSquared(limbSwingMod26) / this.getSwimArmAngleSquared(14.0F));
			}
			else if (limbSwingMod26 >= 14.0F && limbSwingMod26 < 22.0F)
			{
				float swingLerp = (limbSwingMod26 - 14.0F) / 8.0F;
				this.leftArm.xRot = this.rotlerpRad(leftArmSwing, this.leftArm.xRot, ((float) Math.PI / 2F) * swingLerp);
				this.rightArm.xRot = Mth.lerp(rightArmSwing, this.rightArm.xRot, ((float) Math.PI / 2F) * swingLerp);
				this.leftArm.yRot = this.rotlerpRad(leftArmSwing, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(rightArmSwing, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(leftArmSwing, this.leftArm.zRot, 5.012389F - 1.8707964F * swingLerp);
				this.rightArm.zRot = Mth.lerp(rightArmSwing, this.rightArm.zRot, 1.2707963F + 1.8707964F * swingLerp);
			}
			else if (limbSwingMod26 >= 22.0F && limbSwingMod26 < 26.0F)
			{
				float swingLerp = (limbSwingMod26 - 22.0F) / 4.0F;
				this.leftArm.xRot = this.rotlerpRad(leftArmSwing, this.leftArm.xRot, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * swingLerp);
				this.rightArm.xRot = Mth.lerp(rightArmSwing, this.rightArm.xRot, ((float) Math.PI / 2F) - ((float) Math.PI / 2F) * swingLerp);
				this.leftArm.yRot = this.rotlerpRad(leftArmSwing, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(rightArmSwing, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(leftArmSwing, this.leftArm.zRot, (float) Math.PI);
				this.rightArm.zRot = Mth.lerp(rightArmSwing, this.rightArm.zRot, (float) Math.PI);
			}

			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + (float) Math.PI));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}
	
	// affects arm swing while attacking, I think
	@Override
	protected void setupAttackAnimation(FroglinEntity froglin, float ageInTicks)
	{
		if (!(this.attackTime <= 0.0F))
		{
			HumanoidArm mainHand = this.getAttackArm(froglin);
			ModelPart mainArmRenderer = this.getArm(mainHand);
			float swingProgress = this.attackTime;
			this.body.yRot = Mth.sin(Mth.sqrt(swingProgress) * ((float) Math.PI * 2F)) * 0.2F;
			if (mainHand == HumanoidArm.LEFT)
			{
				this.body.yRot *= -1.0F;
			}

//			this.bipedRightArm.rotationPointZ += MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedRightArm.rotationPointX -= MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedLeftArm.rotationPointZ -= MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
//			this.bipedLeftArm.rotationPointX += MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
			this.rightArm.yRot += this.body.yRot;
			this.leftArm.yRot += this.body.yRot;
			this.leftArm.xRot += this.body.yRot;
			swingProgress = 1.0F - this.attackTime;
			swingProgress = swingProgress * swingProgress * swingProgress;
			swingProgress = 1.0F - swingProgress;
			float swingRadians = Mth.sin(swingProgress * (float) Math.PI);
			float headRadians = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			mainArmRenderer.xRot = (float) (mainArmRenderer.xRot - (swingRadians * 1.2D + headRadians));
			mainArmRenderer.yRot += this.body.yRot * 2.0F;
			mainArmRenderer.zRot += Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
		}
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		this.head.render(matrixStack, buffer, packedLight, packedOverlay);
		this.body.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
	{
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
	
	public void copyBaseBone(FroglinModel modelToCopyFrom, Function<FroglinModel, ModelPart> getter)
	{
		getter.apply(this).copyFrom(getter.apply(modelToCopyFrom));
	}

	// private method from BipedModel
	private float getSwimArmAngleSquared(float limbSwing)
	{
		return -65.0F * limbSwing + limbSwing * limbSwing;
	}
	
	public static class FroglinCrouchedModel extends FroglinModel
	{

		public FroglinCrouchedModel(ModelPart modelPart)
		{
			super(modelPart);

			this.head.setPos(0.0F, 10.0F, 0.0F);
			this.body.setPos(0.0F, 12.0F, 0.0F);
			this.rightArm.setPos(-2.5F, 15.0F, 0.0F);
			this.leftArm.setPos(2.5F, 15.0F, 0.0F);
		}

	}

}