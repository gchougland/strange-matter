package com.hexvane.strangematter.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.EchoingShadowEntity;
import com.hexvane.strangematter.entity.EnergeticRiftEntity;
import com.hexvane.strangematter.entity.GravityAnomalyEntity;
import com.hexvane.strangematter.entity.TemporalBloomEntity;
import com.hexvane.strangematter.entity.ThoughtwellEntity;
import com.hexvane.strangematter.entity.WarpGateAnomalyEntity;

import javax.annotation.Nonnull;

/**
 * An invisible marker block used during world generation to defer entity spawning
 * to the main server thread.
 *
 * IMPORTANT: This marker MUST NOT use a block entity. Block entity NBT can be persisted with an AIR block state in
 * some generation/unload orderings, producing log spam:
 * "Tried to load a DUMMY block entity ... but found minecraft:air".
 *
 * Instead, we encode the anomaly type in block state and use a scheduled block tick to spawn the entity.
 */
public class AnomalySpawnerMarkerBlock extends Block {
    public enum SpawnType implements StringRepresentable {
        GRAVITY("gravity"),
        TEMPORAL("temporal"),
        ENERGETIC("energetic"),
        SHADOW("shadow"),
        COGNITIVE("cognitive"),
        WARP_GATE("warp_gate");

        private final String serializedName;

        SpawnType(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    public static final EnumProperty<SpawnType> TYPE = EnumProperty.create("type", SpawnType.class);
    public static final IntegerProperty ATTEMPTS = IntegerProperty.create("attempts", 0, 3);
    private static final int MAX_ATTEMPTS = 3;
    
    public AnomalySpawnerMarkerBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            // NOTE: Do NOT mark this block as "air".
            // During worldgen we place this into proto-chunks. If the block is considered air, the blockstate can be
            // optimized away as vanilla air while the block-entity NBT is still written, causing:
            // "Tried to load a DUMMY block entity ... but found ... minecraft:air"
            .noCollission() // Behave like air for collision/pathing
            .strength(-1.0F, 3600000.0F) // Unbreakable
            .noLootTable() // No drops
            .noOcclusion() // No occlusion culling
        );

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(TYPE, SpawnType.GRAVITY)
            .setValue(ATTEMPTS, 0));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No shape - completely invisible
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No collision
    }
    
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // No visual shape
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true; // Allow light to pass through
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false; // Don't occlude light
    }
    
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // Full brightness - makes it blend with air
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, ATTEMPTS);
    }

    @Override
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        int attempts = state.getValue(ATTEMPTS);
        if (attempts >= MAX_ATTEMPTS) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        boolean spawned = spawnEntity(level, pos, state.getValue(TYPE));
        if (spawned) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            // Retry next tick, but cap attempts so we don't leave these around forever.
            level.setBlock(pos, state.setValue(ATTEMPTS, attempts + 1), 3);
            level.scheduleTick(pos, this, 1);
        }
    }

    private boolean spawnEntity(ServerLevel level, BlockPos pos, SpawnType type) {
        Entity entity = switch (type) {
            case GRAVITY -> new GravityAnomalyEntity(StrangeMatterMod.GRAVITY_ANOMALY.get(), level);
            case TEMPORAL -> new TemporalBloomEntity(StrangeMatterMod.TEMPORAL_BLOOM.get(), level);
            case ENERGETIC -> new EnergeticRiftEntity(StrangeMatterMod.ENERGETIC_RIFT.get(), level);
            case SHADOW -> new EchoingShadowEntity(StrangeMatterMod.ECHOING_SHADOW.get(), level);
            case COGNITIVE -> new ThoughtwellEntity(StrangeMatterMod.THOUGHTWELL.get(), level);
            case WARP_GATE -> {
                WarpGateAnomalyEntity warpGate = new WarpGateAnomalyEntity(StrangeMatterMod.WARP_GATE_ANOMALY_ENTITY.get(), level);
                warpGate.setActive(true);
                yield warpGate;
            }
        };

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        return level.addFreshEntity(entity);
    }
}

