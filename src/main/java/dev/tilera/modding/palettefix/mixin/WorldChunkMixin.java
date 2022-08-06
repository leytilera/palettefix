package dev.tilera.modding.palettefix.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk {

    @Final
    @Shadow
    World world;

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    /**
     * @author tilera
     * @reason Prevent game from crashing
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.world.isDebugWorld()) {
            BlockState blockState = null;
            if (j == 60) {
                blockState = Blocks.BARRIER.getDefaultState();
            }

            if (j == 70) {
                blockState = DebugChunkGenerator.getBlockState(i, k);
            }

            return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
        } else {
            try {
                int l = this.getSectionIndex(j);
                if (l >= 0 && l < this.sectionArray.length) {
                    ChunkSection chunkSection = this.sectionArray[l];
                    if (!chunkSection.isEmpty()) {
                        return chunkSection.getBlockState(i & 15, j & 15, k & 15);
                    }
                }

                return Blocks.AIR.getDefaultState();
            } catch (EntryMissingException e) {
                e.printStackTrace();
                return Blocks.AIR.getDefaultState();
            } catch (Throwable var8) {
                CrashReport crashReport = CrashReport.create(var8, "Getting block state");
                CrashReportSection crashReportSection = crashReport.addElement("Block being got");
                crashReportSection.add("Location", () -> {
                    return CrashReportSection.createPositionString((WorldChunk)(Object) this, i, j, k);
                });
                throw new CrashException(crashReport);
            }
        }
    }

}
