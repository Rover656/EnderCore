package com.enderio.core.common;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.Log;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockEnder extends Block {

  protected final Class<? extends TileEntityEnder> teClass;
  protected final String name;

  protected BlockEnder(String name, Class<? extends TileEntityEnder> teClass) {
    this(name, teClass, new Material(MapColor.ironColor));
  }

  protected BlockEnder(String name, Class<? extends TileEntityEnder> teClass, Material mat) {
    super(mat);
    this.teClass = teClass;
    this.name = name;
    setHardness(0.5F);
    setBlockName(name);
    setStepSound(Block.soundTypeMetal);
    setHarvestLevel("pickaxe", 0);
  }

  protected void init() {
    GameRegistry.registerBlock(this, name);
    if (teClass != null) {
      GameRegistry.registerTileEntity(teClass, name + "TileEntity");
    }
  }

  @Override
  public boolean hasTileEntity(int metadata) {
    return teClass != null;
  }

  @Override
  public TileEntity createTileEntity(World world, int metadata) {
    if (teClass != null) {
      try {
        TileEntityEnder te = teClass.newInstance();
        te.init();
        return te;
      } catch (Exception e) {
        Log.error("Could not create tile entity for block " + name + " for class " + teClass);
      }
    }
    return null;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iIconRegister) {
    blockIcon = iIconRegister.registerIcon("enderio:" + name);
  }

  /* Subclass Helpers */

  @Override
  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7, float par8, float par9) {

    TileEntity te = world.getTileEntity(x, y, z);

    if (entityPlayer.isSneaking()) {
      return false;
    }

    if (te instanceof ITankAccess) {
      if (FluidUtil.fillInternalTankFromPlayerHandItem(world, x, y, z, entityPlayer, (ITankAccess) te)) {
        return true;
      }
      if (FluidUtil.fillPlayerHandItemFromInternalTank(world, x, y, z, entityPlayer, (ITankAccess) te)) {
        return true;
      }
    }

    return openGui(world, x, y, z, entityPlayer, side);
  }

  protected boolean shouldWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
    return true;
  }

  protected boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
    return false;
  }

  public boolean doNormalDrops(World world, int x, int y, int z) {
    return true;
  }

  @Override
  public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(world, player, x, y, z, willHarvest);
  }

  @Override
  public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
    super.harvestBlock(world, player, x, y, z, meta);
    world.setBlockToAir(x, y, z);
  }

  @Override
  public final ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
    if (doNormalDrops(world, x, y, z)) {
      return super.getDrops(world, x, y, z, metadata, fortune);
    }
    return Lists.newArrayList(getNBTDrop(world, x, y, z, (TileEntityEnder) world.getTileEntity(x, y, z)));
  }

  public ItemStack getNBTDrop(World world, int x, int y, int z, TileEntityEnder te) {
    int meta = damageDropped(te.getBlockMetadata());
    ItemStack itemStack = new ItemStack(this, 1, meta);
    processDrop(world, x, y, z, te, itemStack);
    return itemStack;
  }

  protected void processDrop(World world, int x, int y, int z, @Nullable TileEntityEnder te, ItemStack drop) {
  }

  protected TileEntityEnder getTileEntityEio(World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(x, y, z);
    if (teClass.isInstance(te)) {
      return (TileEntityEnder) te;
    }
    return null;
  }

  protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval) {
    TileEntityEnder te = getTileEntityEio(world, x, y, z);
    if (te == null) {
      return world.getTotalWorldTime() % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval);
    }
  }

  protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval, int offset) {
    TileEntityEnder te = getTileEntityEio(world, x, y, z);
    if (te == null) {
      return (world.getTotalWorldTime() + offset) % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval, offset);
    }
  }
}
