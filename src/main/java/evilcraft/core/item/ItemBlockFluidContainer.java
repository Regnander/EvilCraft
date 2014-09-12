package evilcraft.core.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import evilcraft.core.block.IBlockTank;
import evilcraft.core.tileentity.TankInventoryTileEntity;

/**
 * {@link ItemBlock} that can be used for blocks that have a tile entity with a fluid container.
 * The block must implement {@link IBlockTank}.
 * Instances of this will also keep it's tank capacity next to the contents.
 * @author rubensworks
 *
 */
public class ItemBlockFluidContainer extends ItemBlockNBT implements IFluidContainerItem {
    
	private IBlockTank block;
	
    /**
     * Make a new instance.
     * @param block The block instance.
     */
    public ItemBlockFluidContainer(Block block) {
        super(block);
        // Will crash if no valid instance of.
        this.block = (IBlockTank) block;
    }
    
    /**
     * @return The block tank.
     */
    public IBlockTank getBlockTank() {
    	return block;
    }

	@Override
	public FluidStack getFluid(ItemStack container) {
		String key = block.getTankNBTName();
		if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(key)) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(key));
	}

	@Override
	public int getCapacity(ItemStack container) {
		return block.getTankCapacity(container);
	}
	
	/**
     * Set the maximal tank capacity.
     * @param container The item stack.
     * @param capacity The maximal tank capacity in mB.
     */
	public void setCapacity(ItemStack container, int capacity) {
		block.setTankCapacity(container, capacity);
	}
	
	@Override
	protected void readAdditionalInfo(TileEntity tile, ItemStack itemStack) {
		super.readAdditionalInfo(tile, itemStack);
		if(tile instanceof TankInventoryTileEntity) {
			TankInventoryTileEntity tankTile = (TankInventoryTileEntity) tile;
			tankTile.getTank().setCapacity(getCapacity(itemStack));
		}
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		String key = block.getTankNBTName();
		int capacity = getCapacity(container);
		// Implementation from ItemFluidContainer
		if (resource == null)
        {
            return 0;
        }

        if (!doFill)
        {
            if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(key))
            {
                return Math.min(capacity, resource.amount);
            }

            FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(key));

            if (stack == null)
            {
                return Math.min(capacity, resource.amount);
            }

            if (!stack.isFluidEqual(resource))
            {
                return 0;
            }

            return Math.min(capacity - stack.amount, resource.amount);
        }

        if (container.stackTagCompound == null)
        {
            container.stackTagCompound = new NBTTagCompound();
        }

        if (!container.stackTagCompound.hasKey(key))
        {
            NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

            if (capacity < resource.amount)
            {
                fluidTag.setInteger("Amount", capacity);
                container.stackTagCompound.setTag(key, fluidTag);
                return capacity;
            }

            container.stackTagCompound.setTag(key, fluidTag);
            return resource.amount;
        }

        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag(key);
        FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

        if (!stack.isFluidEqual(resource))
        {
            return 0;
        }

        int filled = capacity - stack.amount;
        if (resource.amount < filled)
        {
            stack.amount += resource.amount;
            filled = resource.amount;
        }
        else
        {
            stack.amount = capacity;
        }

        container.stackTagCompound.setTag(key, stack.writeToNBT(fluidTag));
        return filled;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		String key = block.getTankNBTName();
		int capacity = getCapacity(container);
		// Implementation from ItemFluidContainer
		if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(key))
        {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(key));
        if (stack == null)
        {
            return null;
        }

        int currentAmount = stack.amount;
        stack.amount = Math.min(stack.amount, maxDrain);
        if (doDrain)
        {
            if (currentAmount == stack.amount)
            {
                container.stackTagCompound.removeTag(key);

                if (container.stackTagCompound.hasNoTags())
                {
                    container.stackTagCompound = null;
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag(key);
            fluidTag.setInteger("Amount", currentAmount - stack.amount);
            container.stackTagCompound.setTag(key, fluidTag);
        }
        return stack;
	}

}