package evilcraft.entities.tileentities.tickaction;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import evilcraft.api.entities.tileentitites.TankInventoryTileEntity;
import evilcraft.api.entities.tileentitites.TickingTankInventoryTileEntity;
import evilcraft.api.entities.tileentitites.tickaction.ITickAction;

/**
 * {@link ITickAction} for emptying fluid containers in a tank.
 * @author rubensworks
 *
 * @param <T> {@link TickingTankInventoryTileEntity} to drain to.
 */
public class EmptyFluidContainerInTankTickAction<T extends TickingTankInventoryTileEntity<T>> extends EmptyInTankTickAction<T> {

    @Override
    public void onTick(T tile, ItemStack itemStack, int slot, int tick) {
        ItemStack containerStack = tile.getInventory().getStackInSlot(slot);
        FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(containerStack);
        if(fluidStack != null) {
        	IFluidContainerItem container = (IFluidContainerItem) containerStack.getItem();
            fluidStack.amount = Math.min(MB_PER_TICK, fluidStack.amount);
            int filled = tile.getTank().fill(fluidStack, true);
            container.drain(containerStack, filled, true);
        }
    }
    
    @Override
    public int getRequiredTicks(T tile, int slot) {
        return getRequiredTicks(tile, tile.getInventory().getStackInSlot(slot));
    }
    
    /**
     * Get the required ticks for a given item.
     * @param tile The {@link TileEntity} to drain to.
     * @param itemStack The item to get the required ticks for.
     * @return The required ticks.
     */
    public static int getRequiredTicks(TankInventoryTileEntity tile, ItemStack itemStack) {
        IFluidContainerItem container = (IFluidContainerItem) itemStack.getItem();
        int amount = 0;
        if(container.getFluid(itemStack) != null)
            amount = container.getFluid(itemStack).amount;
        int capacity = Math.min(container.getCapacity(itemStack), tile.getTank().getFluidAmount());
        return (capacity - amount) / MB_PER_TICK;
    }
    
    @Override
    public boolean canTick(T tile, ItemStack itemStack, int slot, int tick) {
        boolean emptyContainer = false;
        ItemStack containerStack = tile.getInventory().getStackInSlot(slot);
        IFluidContainerItem container = (IFluidContainerItem) containerStack.getItem();
        if(container.getFluid(containerStack) != null) {
            FluidStack fluidStack = container.getFluid(containerStack);
            if(fluidStack.amount <= 0)
                emptyContainer = true;
        } else emptyContainer = true;
        return super.canTick(tile, itemStack, slot, tick) && !emptyContainer;
    }

}
