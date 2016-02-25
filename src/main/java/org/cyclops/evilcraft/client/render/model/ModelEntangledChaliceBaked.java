package org.cyclops.evilcraft.client.render.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IColoredBakedQuad;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.cyclops.cyclopscore.client.model.DynamicBaseModel;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.evilcraft.block.EntangledChalice;
import org.cyclops.evilcraft.block.EntangledChaliceConfig;
import org.cyclops.evilcraft.block.EntangledChaliceItem;
import org.cyclops.evilcraft.tileentity.TileEntangledChalice;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A baked entangled chalice model.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ModelEntangledChaliceBaked extends DynamicBaseModel implements ISmartItemModel, ISmartBlockModel {

    private final static Map<String, Integer> seeds = Maps.newHashMap();

    public static IBakedModel chaliceModel;
    public static IBakedModel gemsModel;

    private final String id;
    private final FluidStack fluidStack;

    public ModelEntangledChaliceBaked() {
        id = "";
        fluidStack = null;
    }

    public ModelEntangledChaliceBaked(String id, FluidStack fluidStack) {
        this.id = id != null ? id : "";
        this.fluidStack = fluidStack;
    }

    /**
     * Set the color seed of the chalice.
     * @param id Unique id of a chalice group.
     */
    public static int getColorSeed(String id) {
        int gemColor;
        if(seeds.containsKey(id)) {
            gemColor = seeds.get(id);
        } else {
            long res = id.hashCode();
            Random rand = new Random(res);
            gemColor = rand.nextInt(1 << 24) | (255 << 24);
            seeds.put(id, gemColor);
        }
        return gemColor;
    }

    @Override
    public List<BakedQuad> getGeneralQuads() {
        List<BakedQuad> quads = Lists.newLinkedList();

        // Base chalice model
        quads.addAll(chaliceModel.getGeneralQuads());

        // Colored gems
        int color = getColorSeed(this.id);
        for(BakedQuad quad : gemsModel.getGeneralQuads()) {
            int[] data = quad.getVertexData();
            for(int i = 0; i < data.length / 7; i++) {
                data[i * 7 + 3] = color;
            }
            quads.add(new IColoredBakedQuad.ColoredBakedQuad(data, quad.getTintIndex(), quad.getFace()));
        }

        // Fluid
        if(fluidStack != null) {
            quads.addAll(getFluidQuads(fluidStack, TileEntangledChalice.BASE_CAPACITY));
        }

        return quads;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IBakedModel handleItemState(ItemStack itemStack) {
        String id = ((EntangledChaliceItem) itemStack.getItem()).getTankID(itemStack);
        return new ModelEntangledChaliceBaked(id, ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack));
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return chaliceModel.getParticleTexture();
    }

    protected List<BakedQuad> getFluidQuads(FluidStack fluidStack, int capacity) {
        float height = Math.min(0.95F, ((float) fluidStack.amount / (float) capacity)) * 0.1875F + 0.8125F;
        List<BakedQuad> quads = Lists.newArrayList();
        TextureAtlasSprite texture = org.cyclops.cyclopscore.helper.RenderHelpers.getFluidIcon(fluidStack, EnumFacing.UP);
        addBakedQuadRotated(quads, 0.1875F, 0.8125F, 0.1875F, 0.8125F, height, texture, EnumFacing.UP, ROTATION_FIX[EnumFacing.UP.ordinal()]);
        return quads;
    }

    @Override
    public IBakedModel handleBlockState(IBlockState state) {
        String tankId = ((IExtendedBlockState) state).getValue(EntangledChalice.TANK_ID);
        FluidStack fluidStack = BlockHelpers.getSafeBlockStateProperty((IExtendedBlockState) state, EntangledChalice.TANK_FLUID, null);
        if(!EntangledChaliceConfig.staticBlockRendering) {
            fluidStack = null;
        }
        return new ModelEntangledChaliceBaked(tankId, fluidStack);
    }
}
