/*
 *   Arcane Additions Copyright (c)  2022, Kevin Kragenbrink <kevin@writh.net>
 *           This program comes with ABSOLUTELY NO WARRANTY; for details see <https://www.gnu.org/licenses/gpl-3.0.html>.
 *           This is free software, and you are welcome to redistribute it under certain
 *           conditions; detailed at https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.sosly.arcaneadditions.events.spells;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.arcaneadditions.ArcaneAdditions;
import org.sosly.arcaneadditions.client.entity.EntityRegistry;
import org.sosly.arcaneadditions.client.entity.IceBlockEntity;
import org.sosly.arcaneadditions.effects.EffectRegistry;
import org.sosly.arcaneadditions.effects.beneficial.IceBlockEffect;
import org.sosly.arcaneadditions.utils.World;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = ArcaneAdditions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IceBlockEvents {
    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onBreakingBlock(PlayerEvent.BreakSpeed event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (event.getEntity().level.isClientSide()) return;

        runOnEffect(event, (instance, entity) -> {
            event.setCanceled(true);
            event.setAmount(0f);
            event.setResult(Event.Result.DENY);
        });
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onHarvest(PlayerEvent.HarvestCheck event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        handleRestrictedActions(event);
    }

    @SubscribeEvent
    public static void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        runOnEffect(event, (instance, entity) -> {
            // Only player entities have their potion effects synced.  We need to sync non-player entities, too, so that we
            // are able to render the effect in the overworld.
            if (!(entity instanceof Player)) {
                LivingEntity levelEntity = (LivingEntity) World.getLevelEntity(entity);
                if (levelEntity != null) {
                    levelEntity.forceAddEffect(instance, entity);
                }
            }
            entity.setNoActionTime(Integer.MAX_VALUE);
            entity.setTicksFrozen(Integer.MAX_VALUE);
        });
    }

    @SubscribeEvent
    public static void onPotionExpired(PotionEvent.PotionExpiryEvent event) {
        runOnEffect(event, (instance, entity) -> {
            // Only player entities have their potion effects synced.  We need to sync non-player entities, too, so that we
            // are able to render the effect in the overworld.
            if (!(entity instanceof Player)) {
                LivingEntity levelEntity = (LivingEntity)World.getLevelEntity(entity);
                if (levelEntity != null) {
                    levelEntity.removeEffectNoUpdate(instance.getEffect());
                }
            }
            entity.setNoActionTime(0);
            entity.setTicksFrozen(0);

            MobEffectInstance cooldown = new MobEffectInstance(EffectRegistry.ICE_BLOCK_EXHAUSTION.get(), 600, 0);
            entity.addEffect(cooldown);
        });
    }

    @SubscribeEvent
    public static void onPotionRemoved(PotionEvent.PotionRemoveEvent event) {
        runOnEffect(event, (instance, entity) -> {
            // Only player entities have their potion effects synced.  We need to sync non-player entities, too, so that we
            // are able to render the effect in the overworld.
            if (!(entity instanceof Player)) {
                LivingEntity levelEntity = (LivingEntity)World.getLevelEntity(entity);
                if (levelEntity != null) {
                    levelEntity.removeEffectNoUpdate(instance.getEffect());
                }
            }
            entity.setNoActionTime(0);
            entity.setTicksFrozen(0);

            MobEffectInstance cooldown = new MobEffectInstance(EffectRegistry.ICE_BLOCK_EXHAUSTION.get(), 600, 0);
            entity.addEffect(cooldown);
        });
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        handleRestrictedActions(event);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <E extends LivingEntity, M extends EntityModel<E>> void onPostRenderLiving(RenderLivingEvent.Post<E, M> event) {
        runOnEffect(event, (instance, entity) -> {
            IceBlockEntity ice = new IceBlockEntity(EntityRegistry.ICE_BLOCK.get(), entity.getLevel());
            PoseStack stack = event.getPoseStack();
            EntityDimensions dim = entity.getDimensions(entity.getPose());
            stack.pushPose();
            stack.scale(dim.width, dim.height/3.0f, dim.width);
            Minecraft.getInstance().getEntityRenderDispatcher().render(ice, -0.5f, 0, -0.5f, 0, event.getPartialTick(), stack, event.getMultiBufferSource(), event.getPackedLight());
            stack.popPose();
        });
    }

    private static void handleRestrictedActions(LivingEvent event) {
        runOnEffect(event, (instance, entity) -> {
            if (event.isCancelable()) event.setCanceled(true);
            if (event instanceof PlayerInteractEvent) event.setResult(Event.Result.DENY);
            if (event instanceof PlayerEvent.HarvestCheck) ((PlayerEvent.HarvestCheck)event).setCanHarvest(false);
            if (event instanceof LivingEvent.LivingJumpEvent) {
                entity.hasImpulse = false;
                entity.setDeltaMovement(0d, -2000d, 0d);
            }
        });
    }

    private static void runOnEffect(Event event, EffectRegistry.ILivingMobEffectInstanceHandler handler) {
        LivingEntity entity;

        if (event instanceof LivingEvent) {
            entity = (LivingEntity)((LivingEvent)event).getEntity();
        } else if (event instanceof RenderLivingEvent livingEvent) {
            entity = livingEvent.getEntity();
        } else {
            return; // not sure how we got here but let's bail out just in case.
        }

        if (event instanceof PotionEvent.PotionAddedEvent) {
            MobEffectInstance instance = ((PotionEvent)event).getPotionEffect();
            if (instance != null) {
                MobEffect effect = instance.getEffect();
                if (effect instanceof IceBlockEffect) {
                    EffectRegistry.handle(handler, instance, entity);
                }
            }
        } else {
            Collection<MobEffectInstance> effects = entity.getActiveEffects();
            for (MobEffectInstance instance : effects) {
                MobEffect effect = instance.getEffect();
                if (effect instanceof IceBlockEffect) {
                    EffectRegistry.handle(handler, instance, entity);
                }
            }
        }
    }
}