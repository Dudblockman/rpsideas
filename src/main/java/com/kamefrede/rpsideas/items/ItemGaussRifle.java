package com.kamefrede.rpsideas.items;

import com.kamefrede.rpsideas.entity.EntityGaussPulse;
import com.kamefrede.rpsideas.items.base.RPSItem;
import com.kamefrede.rpsideas.util.helpers.ClientHelpers;
import com.kamefrede.rpsideas.util.helpers.FlowColorsHelper;
import com.kamefrede.rpsideas.util.helpers.IFlowColorAcceptor;
import com.kamefrede.rpsideas.util.helpers.SpellHelpers;
import com.kamefrede.rpsideas.util.libs.RPSItemNames;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.PlayerDataHandler;
import vazkii.psi.common.core.handler.PsiSoundHandler;

import javax.annotation.Nonnull;

public class ItemGaussRifle extends RPSItem implements IItemColorProvider, IFlowColorAcceptor {
    protected ItemGaussRifle() {
        super(RPSItemNames.ITEM_GAUSS_RIFLE);
        setMaxStackSize(1);
    }

    @Override
    public IItemColor getItemColor() {
        return (stack, tintIndex) -> {
            if (tintIndex == 0) {
                ItemStack colorizer = FlowColorsHelper.getColorizer(stack);
                if (colorizer.isEmpty())
                    return 0;
                else
                    return Psi.proxy.getColorizerColor(colorizer).getRGB();
            } else if (tintIndex == 1) {
                return ClientHelpers.pulseColor(0xB87333);
            }

            return -1;
        };
    }


    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        PlayerDataHandler.PlayerData data = SpellHelpers.getPlayerData(playerIn);
        ItemStack ammo = findAmmo(playerIn);
        ItemStack cad = PsiAPI.getPlayerCAD(playerIn);

        if (data == null || cad.isEmpty())
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        if (playerIn.capabilities.isCreativeMode || data.availablePsi > 0) {
            boolean wasEmpty = ammo.isEmpty();
            boolean noneLeft = false;
            if (!playerIn.capabilities.isCreativeMode) {
                if (wasEmpty) {
                    int cadBattery = ((ICAD) cad.getItem()).getStoredPsi(cad);
                    if (data.availablePsi + cadBattery < 625) noneLeft = true;
                    data.deductPsi(625, (int) (3 * playerIn.getCooldownPeriod() + 10 + (noneLeft ? 50 : 0)), true);
                } else {
                    data.deductPsi(200, 10, true);
                    ammo.setCount(ammo.getCount() - 1);
                }
            }
            playerIn.swingArm(handIn);
            EntityGaussPulse.AmmoStatus status;
            if (!wasEmpty) {
                if (playerIn.capabilities.isCreativeMode)
                    status = EntityGaussPulse.AmmoStatus.DEPLETED;
                else
                    status = EntityGaussPulse.AmmoStatus.AMMO;
            } else if (noneLeft)
                status = EntityGaussPulse.AmmoStatus.BLOOD;
            else
                status = EntityGaussPulse.AmmoStatus.NOTAMMO;
            EntityGaussPulse proj = new EntityGaussPulse(worldIn, playerIn, status);
            if (!worldIn.isRemote) worldIn.spawnEntity(proj);
            Vec3d look = playerIn.getLookVec();
            playerIn.motionX -= 0.5 * look.x;
            playerIn.motionY -= 0.25 * look.y;
            playerIn.motionZ -= 0.5 * look.z;
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ,
                    status == EntityGaussPulse.AmmoStatus.BLOOD ? PsiSoundHandler.compileError : PsiSoundHandler.cadShoot,
                    SoundCategory.PLAYERS, 1f, 1f);


            if (!playerIn.capabilities.isCreativeMode)
                playerIn.getCooldownTracker().setCooldown(this, (int) (3 * playerIn.getCooldownPeriod()));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));

    }

    public ItemStack findAmmo(EntityPlayer player) {
        if (player.getHeldItemOffhand().getItem() == RPSItems.gaussBullet) {
            return player.getHeldItem(EnumHand.OFF_HAND);
        } else if (player.getHeldItemMainhand().getItem() == RPSItems.gaussBullet) {
            return player.getHeldItem(EnumHand.MAIN_HAND);
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory() - 1; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == RPSItems.gaussBullet)
                    return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}