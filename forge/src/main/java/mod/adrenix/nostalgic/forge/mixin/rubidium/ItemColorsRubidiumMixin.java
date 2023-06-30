package mod.adrenix.nostalgic.forge.mixin.rubidium;

import mod.adrenix.nostalgic.util.client.ItemClientUtil;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemColors.class)
public abstract class ItemColorsRubidiumMixin
{
    /* Shadows */

    @Shadow @Final private Map<Holder.Reference<Item>, ItemColor> f_92674_;

    /* Injections */

    /**
     * If Rubidium is installed, this alternative injection will provide a modified color provider. Controlled by old 2d
     * item colors and whether Rubidium is installed.
     */
    @SuppressWarnings("deprecation")
    @Dynamic("Method getColorProvider is added by Rubidium. See: me.jellysquid.mods.sodium.mixin.core.model.MixinItemColors.java")
    @Inject(
        remap = false,
        cancellable = true,
        method = "getColorProvider",
        at = @At("HEAD")
    )
    private void NT$onGetColorProvider(ItemStack itemStack, CallbackInfoReturnable<ItemColor> callback)
    {
        if (ForgeRegistries.ITEMS.getHolder(itemStack.getItem()).isEmpty())
            return;

        Holder.Reference<Item> holder = ForgeRegistries.ITEMS.getHolder(itemStack.getItem())
            .get()
            .value()
            .builtInRegistryHolder();

        ItemColor itemColor = this.f_92674_.get(holder);

        if (ItemClientUtil.isValidColorItem() && itemColor != null)
            callback.setReturnValue((stack, tintIndex) -> ItemClientUtil.getOldColor(itemColor, stack, tintIndex));
    }
}
