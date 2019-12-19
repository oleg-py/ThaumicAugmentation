/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;
import thecodex6824.thaumicaugmentation.api.TAItems;
import thecodex6824.thaumicaugmentation.api.augment.CapabilityAugment;
import thecodex6824.thaumicaugmentation.api.augment.IAugment;
import thecodex6824.thaumicaugmentation.api.augment.builder.IElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.api.impetus.CapabilityImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.IImpetusStorage;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusAPI;
import thecodex6824.thaumicaugmentation.api.impetus.ImpetusStorage;
import thecodex6824.thaumicaugmentation.common.capability.CapabilityProviderElytraHarnessAugment;
import thecodex6824.thaumicaugmentation.common.capability.SimpleCapabilityProviderNoSave;
import thecodex6824.thaumicaugmentation.common.item.prefab.ItemTABase;
import thecodex6824.thaumicaugmentation.common.network.PacketElytraBoost;
import thecodex6824.thaumicaugmentation.common.network.TANetwork;

public class ItemElytraHarnessAugment extends ItemTABase {

    protected static abstract class HarnessAugment implements IElytraHarnessAugment {
        
        protected boolean sync;
        
        @Override
        public boolean canBeAppliedToItem(ItemStack augmentable) {
            return augmentable.getItem() == TAItems.ELYTRA_HARNESS;
        }
        
        public void setSyncNeeded() {
            sync = true;
        }
        
        @Override
        public boolean shouldSync() {
            boolean res = sync;
            sync = false;
            return res;
        }
    }
    
    public ItemElytraHarnessAugment() {
        super("impetus_booster");
        setMaxStackSize(1);
        setHasSubtypes(true);
    }
    
    protected IElytraHarnessAugment createAugmentForStack(ItemStack stack) {
        if (stack.getMetadata() == 0) {
            return new HarnessAugment() {
                
                @Override
                public boolean isCosmetic() {
                    return false;
                }
                
                @Override
                public void onTick(Entity user) {
                    if (user.world.isRemote && user instanceof EntityLivingBase) {
                        EntityLivingBase entity = (EntityLivingBase) user;
                        IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                        if (energy != null && entity.isElytraFlying() && entity.getTicksElytraFlying() >= 10 && ThaumicAugmentation.proxy.isJumpDown()) {
                            // let the server send the updated energy value
                            if (energy.extractEnergy(1, true) == 1) {
                                TANetwork.INSTANCE.sendToServer(new PacketElytraBoost());
                                Vec3d vec3d = entity.getLookVec();
                                entity.motionX += vec3d.x * 0.1 + (vec3d.x * 1.5 - entity.motionX) * 0.5;
                                entity.motionY += vec3d.y * 0.1 + (vec3d.y * 1.5 - entity.motionY) * 0.5;
                                entity.motionZ += vec3d.z * 0.1 + (vec3d.z * 1.5 - entity.motionZ) * 0.5;
                            }
                        }
                    }
                }   
                
                @Override
                public boolean hasAdditionalAugmentTooltip() {
                    return true;
                }
                
                @Override
                public void appendAdditionalAugmentTooltip(List<String> tooltip) {
                    IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
                    if (energy != null) {
                        tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", new TextComponentTranslation(
                                ImpetusAPI.getEnergyAmountDescriptor(energy))).getFormattedText());
                    }
                }
            };
        }
        else {
            return new HarnessAugment() {
                
                @Override
                public boolean isCosmetic() {
                    return false;
                }
                
            };
        }
    }
    
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (stack.getMetadata() == 0) {
            CapabilityProviderElytraHarnessAugment provider = new CapabilityProviderElytraHarnessAugment(
                    createAugmentForStack(stack), new ImpetusStorage(1500, 75, 1, 0) {
                        @Override
                        public long extractEnergy(long maxToExtract, boolean simulate) {
                            long result = super.extractEnergy(maxToExtract, simulate);
                            if (!simulate)
                                ((HarnessAugment) stack.getCapability(CapabilityAugment.AUGMENT, null)).setSyncNeeded();
                        
                            return result;
                        }
                    }
            );
            if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
                provider.deserializeNBT(nbt.getCompoundTag("Parent"));
            
            return provider;
        }
        else {
            SimpleCapabilityProviderNoSave<IAugment> provider =
                    new SimpleCapabilityProviderNoSave<>(createAugmentForStack(stack), CapabilityAugment.AUGMENT);
            if (nbt != null && nbt.hasKey("Parent", NBT.TAG_COMPOUND))
                provider.deserializeNBT(nbt.getCompoundTag("Parent"));
            
            return provider;
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        IImpetusStorage energy = stack.getCapability(CapabilityImpetusStorage.IMPETUS_STORAGE, null);
        if (energy != null) {
            tooltip.add(new TextComponentTranslation("thaumicaugmentation.text.stored_energy", new TextComponentTranslation(
                    ImpetusAPI.getEnergyAmountDescriptor(energy))).getFormattedText());
        }
    }
    
}
