package org.sosly.witchesandwizards.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import org.sosly.witchesandwizards.client.entity.IceBlockEntity;
import org.sosly.witchesandwizards.client.model.IceBlockModel;

public class IceBlockRenderer extends EntityRenderer<IceBlockEntity> {
    public static final ResourceLocation ICE_TEXTURE = new ResourceLocation("minecraft", "textures/block/ice.png");
    private final IceBlockModel MODEL;

    public IceBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.MODEL = new IceBlockModel(Minecraft.getInstance().getEntityModels().bakeLayer(IceBlockModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(IceBlockEntity pEntity) {
        return ICE_TEXTURE;
    }

    @Override
    public void render(IceBlockEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        RenderType rendertype = RenderType.entityTranslucent(ICE_TEXTURE);
        VertexConsumer consumer = pBuffer.getBuffer(rendertype);
        
        MODEL.renderToBuffer(pMatrixStack, consumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
