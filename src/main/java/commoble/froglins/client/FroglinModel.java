package commoble.froglins.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import commoble.froglins.FroglinEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.6.6
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

public class FroglinModel extends EntityModel<FroglinEntity>
{
	private final ModelRenderer headPivot;
	private final ModelRenderer bodyPivot;
	private final ModelRenderer rightArmPivot;
	private final ModelRenderer rightArmClaws;
	private final ModelRenderer leftArmPivot;
	private final ModelRenderer leftArmClaws;
	private final ModelRenderer rightThighPivot;
	private final ModelRenderer leftThighPivot;

	public FroglinModel()
	{
		this.textureWidth = 64;
		this.textureHeight = 64;

		this.headPivot = new ModelRenderer(this);
		this.headPivot.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.headPivot.setTextureOffset(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, 0.01F, false);

		this.bodyPivot = new ModelRenderer(this);
		this.bodyPivot.setRotationPoint(0.0F, 2.0F, 0.0F);
		this.setRotationAngle(this.bodyPivot, 0.3491F, 0.0F, 0.0F);
		this.bodyPivot.setTextureOffset(16, 16).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 12.0F, 4.0F, 0.0F, false);

		this.rightArmPivot = new ModelRenderer(this);
		this.rightArmPivot.setRotationPoint(-2.5F, 5.0F, 0.0F);
		this.setRotationAngle(this.rightArmPivot, -0.1745F, 0.0F, 0.0F);
		this.rightArmPivot.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, false);

		this.rightArmClaws = new ModelRenderer(this);
		this.rightArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
		this.rightArmPivot.addChild(this.rightArmClaws);
		this.setRotationAngle(this.rightArmClaws, 0.2618F, 0.0F, 0.0F);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.rightArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);

		this.leftArmPivot = new ModelRenderer(this);
		this.leftArmPivot.setRotationPoint(2.5F, 5.0F, 0.0F);
		this.setRotationAngle(this.leftArmPivot, -0.1745F, 0.0F, 0.0F);
		this.leftArmPivot.setTextureOffset(40, 16).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 9.0F, 3.0F, 0.0F, true);

		this.leftArmClaws = new ModelRenderer(this);
		this.leftArmClaws.setRotationPoint(2.5F, 18.0F, 5.0F);
		this.leftArmPivot.addChild(this.leftArmClaws);
		this.setRotationAngle(this.leftArmClaws, 0.2618F, 0.0F, 0.0F);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-1.6F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-2.4F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);
		this.leftArmClaws.setTextureOffset(0, 0).addBox(-3.3F, -12.0F, -3.5F, 0.0F, 4.0F, 1.0F, 0.0F, false);

		this.rightThighPivot = new ModelRenderer(this);
		this.rightThighPivot.setRotationPoint(-2.0F, 12.0F, 4.0F);
		this.rightThighPivot.setTextureOffset(0, 16).addBox(-2.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

		this.leftThighPivot = new ModelRenderer(this);
		this.leftThighPivot.setRotationPoint(2.0F, 12.0F, 4.0F);
		this.leftThighPivot.setTextureOffset(0, 16).addBox(-0.5F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);
	}

	@Override
	public void setRotationAngles(FroglinEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		// previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		this.headPivot.render(matrixStack, buffer, packedLight, packedOverlay);
		this.bodyPivot.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightArmPivot.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftArmPivot.render(matrixStack, buffer, packedLight, packedOverlay);
		this.rightThighPivot.render(matrixStack, buffer, packedLight, packedOverlay);
		this.leftThighPivot.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
	{
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}