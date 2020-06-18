package com.enderio.core.api.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.network.EnderPacketHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IProgressTile {

  float getProgress();

  /**
   * Client-only. Called to set clientside progress for syncing/rendering purposes.
   *
   * @param progress
   *          The % progress.
   */
  @OnlyIn(Dist.CLIENT)
  void setProgress(float progress);

  @Nonnull
  TileEntity getTileEntity();

  @Nonnull
  IMessage getProgressPacket();


  public static @Nullable Packet<?> getProgressPacket(Object o) {
    if (o instanceof IProgressTile) {
      return EnderPacketHandler.INSTANCE.getPacketFrom(((IProgressTile) o).getProgressPacket());
    }
    return null;
  }

}
