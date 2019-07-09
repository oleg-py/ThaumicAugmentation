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

package thecodex6824.thaumicaugmentation.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import thaumcraft.api.casters.FocusPackage;

/**
 * Event fired when a cast is successfully executed. This may not be fired for all casters - 
 * it is only guaranteed for Thaumic Augmentation casters.
 * @author TheCodex6824
 */
@Cancelable
public class SuccessfulCastEvent extends CastEvent {
    
    public SuccessfulCastEvent(EntityLivingBase castingEntity, ItemStack casterStack, FocusPackage castPackage) {
        super(castingEntity, casterStack, castPackage);
    }
    
}