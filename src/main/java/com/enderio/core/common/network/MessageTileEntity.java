package com.enderio.core.common.network;

import java.lang.reflect.TypeVariable;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.google.common.reflect.TypeToken;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MessageTileEntity<T extends TileEntity> implements IMessage {

  private long pos;

  protected MessageTileEntity() {
  }

  protected MessageTileEntity(@Nonnull T tile) {
    pos = tile.getPos().toLong();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(pos);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    pos = buf.readLong();
  }

  public @Nonnull BlockPos getPos() {
    return BlockPos.fromLong(pos);
  }

  @SuppressWarnings("unchecked")
  protected T getTileEntity(World worldObj) {
    // Sanity check, and prevent malicious packets from loading chunks
    if (worldObj == null || !worldObj.isBlockLoaded(getPos())) {
      return null;
    }
    TileEntity te = worldObj.getTileEntity(getPos());
    if (te == null) {
      return null;
    }
    @SuppressWarnings("rawtypes")
    final Class<? extends MessageTileEntity> ourClass = getClass();
    @SuppressWarnings("rawtypes")
    final TypeVariable<Class<MessageTileEntity>>[] typeParameters = MessageTileEntity.class.getTypeParameters();
    if (typeParameters.length > 0) {
      @SuppressWarnings("rawtypes")
      final TypeVariable<Class<MessageTileEntity>> typeParam0 = typeParameters[0];
      if (typeParam0 != null) {
        TypeToken<?> teType = TypeToken.of(ourClass).resolveType(typeParam0);
        final Class<? extends TileEntity> teClass = te.getClass();
        if (teType.isAssignableFrom(teClass)) {
          return (T) te;
        }
      }
    }
    return null;
  }

  protected @Nonnull World getWorld(MessageContext ctx) {
    if (ctx.side == Side.SERVER) {
      return ctx.getServerHandler().player.world;
    } else {
      final World clientWorld = EnderCore.proxy.getClientWorld();
      if (clientWorld == null) {
        throw new NullPointerException("Recieved network packet ouside any world!");
      }
      return clientWorld;
    }
  }
}
