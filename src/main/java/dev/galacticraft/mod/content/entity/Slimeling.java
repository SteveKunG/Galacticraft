/*
 * Copyright (c) 2019-2025 Team Galacticraft
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

package dev.galacticraft.mod.content.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import dev.galacticraft.mod.content.GCEntityTypes;
import dev.galacticraft.mod.content.GCSounds;
import dev.galacticraft.mod.content.block.special.slimeling_egg.SlimelingEggColor;
import dev.galacticraft.mod.content.item.GCItems;
import dev.galacticraft.mod.tag.GCItemTags;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Slimeling extends TamableAnimal implements ContainerListener, HasCustomInventoryScreen {
    private static final EntityDataAccessor<Boolean> HAS_BAG = SynchedEntityData.defineId(Slimeling.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> COLOR = SynchedEntityData.defineId(Slimeling.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<ItemStack> FAVORITE_FOOD = SynchedEntityData.defineId(Slimeling.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.defineId(Slimeling.class, EntityDataSerializers.INT);

    public static final String HAS_BAG_TAG = "has_bag";
    public static final String HAT_ITEM_TAG = "hat_item";
    public static final String FAVORITE_FOOD_TAG = "favorite_food";
    public static final String COLOR_TAG = "color";
    public static final String KILL_COUNT_TAG = "kill_count";

    public final int MAX_AGE = 100000;
    protected SimpleContainer inventory;

    public Slimeling(EntityType<? extends Slimeling> entityType, Level level) {
        super(entityType, level);
        this.createInventory();

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new TemptGoal(this, 1.25, stack -> stack.is(Items.SLIME_BALL), false));//TODO Tempt food
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Sludgeling.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_BAG, false);
        builder.define(COLOR, new Vector3f(1.0f, 1.0f, 1.0f));
        builder.define(FAVORITE_FOOD, ItemStack.EMPTY);
        builder.define(KILLS, 0);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();

        if (this.inventory != null) {
            for (var i = 0; i < this.inventory.getContainerSize(); ++i) {
                var itemStack = this.inventory.getItem(i);

                if (!itemStack.isEmpty() && !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    this.spawnAtLocation(itemStack);
                }
            }
        }
    }

    public int getKillCount() {
        return this.entityData.get(KILLS);
    }

    public void setKillCount(int killCount) {
        this.entityData.set(KILLS, killCount);
    }

    public Vector3f getColor() {
        return this.entityData.get(COLOR);
    }

    public void setColor(Vector3f color) {
        this.entityData.set(COLOR, color);
    }

    public ItemStack getFavoriteFood() {
        return this.entityData.get(FAVORITE_FOOD);
    }

    public void setFavoriteFood(ItemStack itemStack) {
        this.entityData.set(FAVORITE_FOOD, itemStack);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        var randomSource = level.getRandom();

        this.setColor(SlimelingEggColor.getRandomColor(randomSource));
        this.setFavoriteFood(this.getRandomFavoriteFood(randomSource));

        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    public ItemStack getRandomFavoriteFood(RandomSource randomSource) {
        return new ItemStack(Util.getRandom(ImmutableList.copyOf(BuiltInRegistries.ITEM.getTagOrEmpty(GCItemTags.SLIMELING_FAVORITE_FOODS)), randomSource).value());
    }

    public boolean hasBag() {
        return this.entityData.get(HAS_BAG);
    }

    public void setHasBag(boolean hasBag) {
        this.entityData.set(HAS_BAG, hasBag);
    }

    public final int getInventorySize() {
        return getInventorySize(this.getInventoryColumns());
    }

    public static int getInventorySize(int columns) {
        return columns * 3 + 1;
    }

    protected void createInventory() {
        var simpleContainer = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());

        if (simpleContainer != null) {
            simpleContainer.removeListener(this);
            var i = Math.min(simpleContainer.getContainerSize(), this.inventory.getContainerSize());

            for (var j = 0; j < i; j++) {
                var itemStack = simpleContainer.getItem(j);

                if (!itemStack.isEmpty()) {
                    this.inventory.setItem(j, itemStack.copy());
                }
            }
        }

        this.inventory.addListener(this);
        this.syncSaddleToClients();
    }

    public float getSlimelingSize() {
        return this.getScale() * 2.0F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean(HAS_BAG_TAG, this.hasBag());

        if (!this.getFavoriteFood().isEmpty()) {
            compound.put(FAVORITE_FOOD_TAG, this.getFavoriteFood().save(this.registryAccess()));
        }

        compound.put(COLOR_TAG, this.newFloatList(this.getColor().x(), this.getColor().y(), this.getColor().z()));
        compound.putInt(KILL_COUNT_TAG, this.getKillCount());

        if (this.hasBag()) {
            var listTag = new ListTag();

            for (var i = 1; i < this.inventory.getContainerSize(); i++) {
                var itemStack = this.inventory.getItem(i);

                if (!itemStack.isEmpty()) {
                    var compoundTag = new CompoundTag();
                    compoundTag.putByte("Slot", (byte) (i - 1));
                    listTag.add(itemStack.save(this.registryAccess(), compoundTag));
                }
            }
            compound.put("Items", listTag);
        }

        if (!this.inventory.getItem(0).isEmpty()) {
            compound.put(HAT_ITEM_TAG, this.inventory.getItem(0).save(this.registryAccess()));
        }
    }

    @Override
    public SlotAccess getSlot(int mappedIndex) {
        if (mappedIndex == 499) {
            return new SlotAccess() {
                @Override
                public ItemStack get() {
                    return Slimeling.this.hasBag() ? new ItemStack(GCItems.SLIMELING_INVENTORY_BAG) : ItemStack.EMPTY;
                }

                @Override
                public boolean set(ItemStack stack) {
                    if (stack.isEmpty()) {
                        if (Slimeling.this.hasBag()) {
                            Slimeling.this.setHasBag(false);
                            Slimeling.this.createInventory();
                        }

                        return true;
                    } else if (stack.is(GCItems.SLIMELING_INVENTORY_BAG)) {
                        if (!Slimeling.this.hasBag()) {
                            Slimeling.this.setHasBag(true);
                            Slimeling.this.createInventory();
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            };
        } else {
            var j = mappedIndex - 500 + 1;
            return j >= 1 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(mappedIndex);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(HAS_BAG_TAG, Tag.TAG_BYTE)) {
            this.setHasBag(compound.getBoolean(HAS_BAG_TAG));
        }

        if (compound.contains(FAVORITE_FOOD_TAG, Tag.TAG_STRING)) {
            this.setFavoriteFood(ItemStack.parse(this.registryAccess(), compound.getCompound(FAVORITE_FOOD_TAG)).orElse(ItemStack.EMPTY));
        }

        this.setKillCount(compound.getInt(KILL_COUNT_TAG));

        var colorListTag = compound.getList(COLOR_TAG, Tag.TAG_FLOAT);
        this.setColor(new Vector3f(colorListTag.getFloat(0), colorListTag.getFloat(1), colorListTag.getFloat(2)));

        this.createInventory();

        if (this.hasBag()) {
            var listTag = compound.getList("Items", Tag.TAG_COMPOUND);

            for (var i = 0; i < listTag.size(); i++) {
                var compoundTag = listTag.getCompound(i);
                var j = compoundTag.getByte("Slot") & 255;

                if (j < this.inventory.getContainerSize() - 1) {
                    this.inventory.setItem(j + 1, ItemStack.parse(this.registryAccess(), compoundTag).orElse(ItemStack.EMPTY));
                }
            }
        }

        if (compound.contains(HAT_ITEM_TAG, Tag.TAG_COMPOUND)) {
            var itemStack = ItemStack.parse(this.registryAccess(), compound.getCompound(HAT_ITEM_TAG)).orElse(ItemStack.EMPTY);

            if (itemStack.is(ItemTags.WOOL_CARPETS)) {
                this.inventory.setItem(0, itemStack);
            }
        }

        this.syncSaddleToClients();
    }

    //    @Override
    //    public void setScaleForAge(boolean par1)
    //    {
    //        this.setScale(this.getSlimelingSize());
    //    }
    //
    //    @Override
    //    public boolean isChild()
    //    {
    //        return this.getAge() / (float) this.MAX_AGE < 0.33F;
    //    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_BLOCK_STEP;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return GCSounds.SLIMELING_DEATH;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
        }
    }

    private double getMaxHealthSlimeling() {
        if (this.isTame()) {
            return 20.001D + 30.0 * ((double) this.age / (double) this.MAX_AGE);
        } else {
            return 8.0D;
        }
    }

    //    @Override
    //    public float getStandingEyeHeight(Pose pose, EntityDimensions dimensions)
    //    {
    //        return dimensions.height * 0.8F;
    //    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            var entity = source.getEntity();
            if (!this.level().isClientSide()) {
                this.setOrderedToSit(false);
            }

            if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.hurt(source, amount);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        var bl = target.hurt(this.damageSources().mobAttack(this), (float) (int) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (bl) {
            //            this.doEnchantDamageEffects(this, target);
        }

        return bl;
    }

    public float getDamage() {
        var i = this.isTame() ? 5 : 2;
        return (float) (i * this.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        var itemStack = player.getItemInHand(interactionHand);
        var item = itemStack.getItem();

        if (this.isTame()) {
            if (this.isOwnedBy(player)) {

                if (player.isSecondaryUseActive()) {
                    this.openCustomInventoryScreen(player);
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                if (itemStack.is(GCItems.SLIMELING_INVENTORY_BAG) && !this.hasBag()) {
                    itemStack.consume(1, player);
                    this.setHasBag(true);
                    this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
                    this.createInventory();
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                if (this.isFood(itemStack)) {
                    itemStack.shrink(1);

                    if (this.random.nextInt(3) == 0) {
                        this.setFavoriteFood(this.getRandomFavoriteFood(this.random));
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                var interactionResult = super.mobInteract(player, interactionHand);

                if (!interactionResult.consumesAction()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return InteractionResult.SUCCESS_NO_ITEM_USED;
                }
            }
        }

        //        if (this.isTame()) {
        //            if (!itemStack.isEmpty()) {
        //                if (ItemStack.isSameItem(itemStack, this.getFavoriteFood())) {
        //                    if (this.isOwnedBy(player)) {
        //                        itemStack.shrink(1);
        //
        //                        if (this.random.nextInt(3) == 0) {
        //                            this.setFavoriteFood(this.getRandomFavoriteFood(this.random));
        //                        }
        //                    } else {
        //                        if (player instanceof ServerPlayer serverPlayer) {
        //                            //                            GCPlayerStats stats = GCPlayerStats.get(player);
        //                            //                            if (stats.getChatCooldown() == 0) {
        //                            //                                player.sendMessage(new TextComponentString(GCCoreUtil.translate("gui.slimeling.chat.wrong_player")));
        //                            //                                stats.setChatCooldown(100);
        //                            //                            }
        //                        }
        //                    }
        //                } else {
        //                    //                    if (this.world.isRemote) {
        //                    //                        MarsModuleClient.openSlimelingGui(this, 0);
        //                    //                    }
        //                }
        //            } else {
        //                //                if (this.world.isRemote) {
        //                //                    MarsModuleClient.openSlimelingGui(this, 0);
        //                //                }
        //            }
        //
        //            return InteractionResult.SUCCESS;
        //        } else if (!itemStack.isEmpty() && this.isFood(itemStack)) {
        //            if (!player.getAbilities().instabuild) {
        //                itemStack.shrink(1);
        //            }
        //
        //            if (this.random.nextInt(3) == 0) {
        //                this.tame(player);
        //                this.navigation.stop();
        //                this.setTarget(null);
        //                this.setOrderedToSit(true);
        //                this.level().broadcastEntityEvent(this, (byte) 7);
        //            } else {
        //                this.level().broadcastEntityEvent(this, (byte) 6);
        //            }
        //
        //            //            if (!this.level().isClientSide()) {
        //            //                if (this.rand.nextInt(3) == 0) {
        //            //                    this.setTamed(true);
        //            //                    this.getNavigator().clearPath();
        //            //                    this.setAttackTarget(null);
        //            //                    this.setSittingAI(true);
        //            //                    this.setHealth(20.0F);
        //            //                    this.setOwnerId(player.getUniqueID());
        //            //                    this.setOwnerUsername(player.getName());
        //            //                    this.playTameEffect(true);
        //            //                    this.world.setEntityState(this, (byte) 7);
        //            //                }
        //            //                else {
        //            //                    this.playTameEffect(false);
        //            //                    this.world.setEntityState(this, (byte) 6);
        //            //                }
        //            //            }
        //
        //            return InteractionResult.SUCCESS;
        //        }

        return super.mobInteract(player, interactionHand);
    }

    public String getOwnerUsername() {
        if (this.getServer() != null) {
            var gameProfileCache = this.getServer().getProfileCache();
            if (gameProfileCache != null) {
                var optional = gameProfileCache.get(this.getOwnerUUID());
                return optional.map(GameProfile::getName).orElse("");
            }
        }
        return "";
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SLIME_BALL);
    }

    @Override
    @Nullable
    public Slimeling getBreedOffspring(ServerLevel level, AgeableMob otherSlimeling) {
        var slimeling = GCEntityTypes.SLIMELING.create(level);
        if (slimeling != null) {
            var uUID = this.getOwnerUUID();
            if (uUID != null) {
                slimeling.setOwnerUUID(uUID);
                slimeling.setTame(true, true);
            }
        }
        return slimeling;
    }

    //    public Slimeling spawnBabyAnimal(EntityAgeable par1EntityAgeable)
    //    {
    //        if (par1EntityAgeable instanceof Slimeling) {
    //            Slimeling otherSlimeling = (Slimeling) par1EntityAgeable;
    //
    //            Vector3 colorParentA = new Vector3(this.getColorRed(), this.getColorGreen(), this.getColorBlue());
    //            Vector3 colorParentB = new Vector3(otherSlimeling.getColorRed(), otherSlimeling.getColorGreen(), otherSlimeling.getColorBlue());
    //            Vector3 newColor = ColorUtil.addColorsRealistically(colorParentA, colorParentB);
    //            newColor.x = Math.max(Math.min(newColor.x, 1.0F), 0);
    //            newColor.y = Math.max(Math.min(newColor.y, 1.0F), 0);
    //            newColor.z = Math.max(Math.min(newColor.z, 1.0F), 0);
    //            Slimeling newSlimeling = new Slimeling(this.world, (float) newColor.x, (float) newColor.y, (float) newColor.z);
    //
    //            UUID s = this.getOwnerId();
    //
    //            if (s != null) {
    //                newSlimeling.setOwnerId(s);
    //                newSlimeling.setTamed(true);
    //            }
    //
    //            return newSlimeling;
    //        }
    //
    //        return null;
    //    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        } else if (!this.isTame()) {
            return false;
        } else if (!(otherAnimal instanceof Slimeling slimeling)) {
            return false;
        } else {
            if (!slimeling.isTame()) {
                return false;
            } else if (slimeling.isInSittingPose()) {
                return false;
            } else {
                return this.isInLove() && slimeling.isInLove();
            }
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof Creeper || target instanceof Ghast) {
            return false;
        } else if (target instanceof Slimeling slimeling) {
            return !slimeling.isTame() || slimeling.getOwner() != owner;
        } else if (target instanceof Player playerTarget && owner instanceof Player playerOwner && !playerOwner.canHarmPlayer(playerTarget)) {
            return false;
        } else if (target instanceof AbstractHorse targetHorse && targetHorse.isTamed()) {
            return false;
        } else {
            return !(target instanceof TamableAnimal tamableAnimal) || !tamableAnimal.isTame();
        }
    }

    @Override
    public float getScale() {
        return this.getAge() / (float) this.MAX_AGE * 0.5F + 0.5F;
    }

    @Override
    public void containerChanged(Container container) {
        var bl = this.hasBag();
        this.syncSaddleToClients();

        if (this.tickCount > 20 && !bl && this.hasBag()) {
            this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
        }
    }

    protected void syncSaddleToClients() {
        if (!this.level().isClientSide()) {
            //            this.setHasBag(!this.inventory.getItem(0).isEmpty());
        }
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide() && this.isTame() && this.isOwnedBy(player)) {
            player.galacticraft$openSlimelingInventory(this, this.inventory);
        }
    }

    public int getInventoryColumns() {
        return this.hasBag() ? 5 : 0;
    }

    public boolean hasInventoryChanged(Container container) {
        return this.inventory != container;
    }
}