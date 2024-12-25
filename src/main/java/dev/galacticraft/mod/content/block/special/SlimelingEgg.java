/*
 * Copyright (c) 2019-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.content.block.special;


import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.mod.content.GCEntityTypes;
import dev.galacticraft.mod.content.block.entity.SlimelingEggBlockEntity;
import dev.galacticraft.mod.content.item.GCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlimelingEgg extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<SlimelingEgg> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            EggColor.CODEC.fieldOf("color").forGetter(slimelingEgg -> slimelingEgg.eggColor)
    ).apply(instance, SlimelingEgg::new));
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
    public static final BooleanProperty CRACKED = BooleanProperty.create("cracked");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.625, 0.75);

    private final EggColor eggColor;

    public SlimelingEgg(Properties properties, EggColor eggColor) {
        super(properties);
        this.eggColor = eggColor;
        this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false).setValue(CRACKED, false).setValue(HATCH, 0));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully()) {
            this.destroyEgg(level, state, pos, entity, 100);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!(entity instanceof Zombie)) {
            this.destroyEgg(level, state, pos, entity, 3);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(CRACKED)) {
            if (!this.isReadyToHatch(state)) {
                level.playSound(null, pos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
                level.setBlock(pos, state.setValue(HATCH, this.getHatchLevel(state) + 1), 2);
            } else {
                level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);

                if (level.getBlockEntity(pos) instanceof SlimelingEggBlockEntity slimelingEgg && slimelingEgg.ownerUUID != null) {
                    var slimeling = GCEntityTypes.SLIMELING.create(level);
                    var vec3 = pos.getCenter();
                    slimeling.setOwnerUUID(slimelingEgg.ownerUUID);
                    slimeling.setTame(true, true);
                    slimeling.setColor(this.eggColor.byColor());
                    slimeling.setHealth(20.0F);
                    slimeling.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(random.nextFloat() * 360.0F), 0.0F);

                    level.addFreshEntity(slimeling);
                }
                level.destroyBlock(pos, false);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(CRACKED)) {
            level.setBlock(pos, state.setValue(CRACKED, true), Block.UPDATE_ALL);

            if (level.getBlockEntity(pos) instanceof SlimelingEggBlockEntity slimelingEgg) {
                slimelingEgg.ownerUUID = player.getUUID();
            }

            var boostBlock = hatchBoost(level, pos);
            if (!level.isClientSide() && boostBlock) {
                level.levelEvent(LevelEvent.PARTICLES_EGG_CRACK, pos, 0);
            }

            var ticks = boostBlock ? 12000 : 24000;
            var j = ticks / 3;
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
            level.scheduleTick(pos, this, j + level.random.nextInt(300));

            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (tool.is(GCItems.DESH_PICKAXE)) {
            var newStickyDeshPick = new ItemStack(GCItems.STICKY_DESH_PICKAXE);
//            newStickyDeshPick.setTag(tool.getTag());TODO
            player.setItemInHand(player.getUsedItemHand(), newStickyDeshPick);
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor world, BlockPos pos, BlockPos posFrom) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, direction, newState, world, pos, posFrom);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SlimelingEggBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CRACKED, HATCH, WATERLOGGED);
    }

    public static boolean hatchBoost(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.SLIME_BLOCK);//TODO Hatch boost block tag?
    }

    private void destroyEgg(Level level, BlockState state, BlockPos pos, Entity entity, int chance) {
        if (this.canDestroyEgg(level, entity)) {
            if (!level.isClientSide() && level.random.nextInt(chance) == 0 && state.is(this)) {
                level.destroyBlock(pos, false);
                level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            }
        }
    }

    private boolean canDestroyEgg(Level level, Entity entity) {
        if (entity instanceof Bat) {
            return false;
        } else if (!(entity instanceof LivingEntity)) {
            return false;
        } else {
            return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
    }

    public int getHatchLevel(BlockState state) {
        return state.getValue(HATCH);
    }

    private boolean isReadyToHatch(BlockState state) {
        return this.getHatchLevel(state) == 2;
    }

    public enum EggColor implements StringRepresentable {
        RED("red"),
        BLUE("blue"),
        YELLOW("yellow");

        public static final Codec<EggColor> CODEC = StringRepresentable.fromEnum(EggColor::values);

        private final String name;

        public static final Vector3f RED_COLOR = new Vector3f(1.0f, 0.0f, 0.0f);
        public static final Vector3f BLUE_COLOR = new Vector3f(0.0f, 0.0f, 1.0f);
        public static final Vector3f YELLOW_COLOR = new Vector3f(1.0f, 1.0f, 0.0f);

        EggColor(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Vector3f byColor() {
            return switch (this) {
                case RED -> RED_COLOR;
                case BLUE -> BLUE_COLOR;
                case YELLOW -> YELLOW_COLOR;
            };
        }

        public static Vector3f getRandomColor(RandomSource randomSource) {
            return switch (randomSource.nextInt(3)) {
                case 0 -> RED_COLOR;
                case 1 -> BLUE_COLOR;
                case 2 -> YELLOW_COLOR;
                default -> new Vector3f();
            };
        }
    }
}