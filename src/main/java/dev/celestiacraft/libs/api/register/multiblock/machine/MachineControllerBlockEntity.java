package dev.celestiacraft.libs.api.register.multiblock.machine;

import dev.celestiacraft.libs.api.register.multiblock.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.api.IMultiblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class MachineControllerBlockEntity extends ControllerBlockEntity {
	private static final String INVENTORY_KEY = "Inventory";
	private static final String FLUIDS_KEY = "Fluids";
	private static final String LEGACY_FLUID_KEY = "Fluid";
	private static final String ENERGY_KEY = "Energy";

	private final boolean controllerItemIOEnabled;
	private final boolean controllerFluidIOEnabled;
	private final boolean controllerEnergyIOEnabled;
	private final boolean internalItemStorageEnabled;
	private final boolean internalFluidStorageEnabled;
	private final boolean internalEnergyStorageEnabled;
	private final int configuredItemSlots;
	private final FluidSlots[] configuredFluidSlots;
	private final int configuredEnergyCapacity;

	@Nullable
	private final ItemStackHandler itemStorage;
	@Nullable
	private final List<FluidStack> fluidStorage;

	private int energyStored;

	private LazyOptional<IItemHandler> itemCapability = LazyOptional.empty();
	private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.empty();
	private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.empty();

	public MachineControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Supplier<IMultiblock> structure) {
		super(type, pos, state, structure);

		this.controllerItemIOEnabled = supportsControllerItemIO();
		this.controllerFluidIOEnabled = supportsControllerFluidIO();
		this.controllerEnergyIOEnabled = supportsControllerEnergyIO();

		this.configuredItemSlots = Math.max(0, getItemSlots());
		this.configuredFluidSlots = sanitizeFluidSlots(getFluidSlots());
		this.configuredEnergyCapacity = Math.max(0, getEnergyCapacity());

		this.internalItemStorageEnabled = useInternalItemStorage() && configuredItemSlots > 0;
		this.internalFluidStorageEnabled = useInternalFluidStorage() && configuredFluidSlots.length > 0;
		this.internalEnergyStorageEnabled = useInternalEnergyStorage() && configuredEnergyCapacity > 0;

		this.itemStorage = internalItemStorageEnabled ? createInternalItemStorage() : null;
		this.fluidStorage = internalFluidStorageEnabled ? createInternalFluidStorage() : null;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		rebuildCapabilities();
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		itemCapability.invalidate();
		fluidCapability.invalidate();
		energyCapability.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		rebuildCapabilities();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
		if (capability == ForgeCapabilities.ITEM_HANDLER && itemCapability.isPresent()) {
			return canExposeItemCapability(side) ? itemCapability.cast() : LazyOptional.empty();
		}

		if (capability == ForgeCapabilities.FLUID_HANDLER && fluidCapability.isPresent()) {
			return canExposeFluidCapability(side) ? fluidCapability.cast() : LazyOptional.empty();
		}

		if (capability == ForgeCapabilities.ENERGY && energyCapability.isPresent()) {
			return canExposeEnergyCapability(side) ? energyCapability.cast() : LazyOptional.empty();
		}

		return super.getCapability(capability, side);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);

		if (itemStorage != null) {
			tag.put(INVENTORY_KEY, itemStorage.serializeNBT());
		}

		if (fluidStorage != null && !fluidStorage.isEmpty()) {
			ListTag fluids = new ListTag();
			for (FluidStack stack : fluidStorage) {
				fluids.add(stack.writeToNBT(new CompoundTag()));
			}
			tag.put(FLUIDS_KEY, fluids);

			if (fluidStorage.size() == 1) {
				tag.put(LEGACY_FLUID_KEY, fluidStorage.get(0).writeToNBT(new CompoundTag()));
			}
		}

		if (internalEnergyStorageEnabled) {
			tag.putInt(ENERGY_KEY, energyStored);
		}
	}

	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);

		if (itemStorage != null && tag.contains(INVENTORY_KEY, Tag.TAG_COMPOUND)) {
			itemStorage.deserializeNBT(tag.getCompound(INVENTORY_KEY));
		}

		if (fluidStorage != null) {
			for (int i = 0; i < fluidStorage.size(); i++) {
				fluidStorage.set(i, FluidStack.EMPTY);
			}

			if (tag.contains(FLUIDS_KEY, Tag.TAG_LIST)) {
				ListTag fluids = tag.getList(FLUIDS_KEY, Tag.TAG_COMPOUND);
				for (int i = 0; i < fluidStorage.size() && i < fluids.size(); i++) {
					fluidStorage.set(i, FluidStack.loadFluidStackFromNBT(fluids.getCompound(i)));
				}
			} else if (fluidStorage.size() == 1 && tag.contains(LEGACY_FLUID_KEY, Tag.TAG_COMPOUND)) {
				fluidStorage.set(0, FluidStack.loadFluidStackFromNBT(tag.getCompound(LEGACY_FLUID_KEY)));
			}
		}

		if (internalEnergyStorageEnabled) {
			energyStored = clampEnergy(tag.getInt(ENERGY_KEY));
		}
	}

	@Override
	public @NotNull CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	protected final boolean canWork(MultiblockContext<? extends MachineControllerBlockEntity> context) {
		return isMachineStructureValid() && getWorkCondition(context).pass();
	}

	protected final boolean prepareRecipeTick(MultiblockContext<? extends MachineControllerBlockEntity> context) {
		if (context.isClient() || !isMachineStructureValid()) {
			return false;
		}

		WorkConditionResult result = getWorkCondition(context);
		if (!result.pass()) {
			onWorkConditionFailed(context, result);
			return false;
		}

		return true;
	}

	protected final WorkConditionResult getCurrentWorkCondition() {
		if (level == null) {
			return WorkConditionResult.fail("level_unavailable");
		}

		return getWorkCondition(MultiblockContext.of(this));
	}

	protected final boolean isMachineStructureValid() {
		return isStructureValid() && hasRequiredIOCounts();
	}

	protected final int countMatchedBlocks(Block block) {
		if (level == null || !isStructureValid()) {
			return 0;
		}

		return getMultiblockHandler().findBlock(block).size();
	}

	protected final <T extends BlockEntity> List<T> findMatchedBlockEntities(Block block, Class<T> type) {
		if (level == null || !isStructureValid()) {
			return Collections.emptyList();
		}

		List<T> result = new ArrayList<>();
		for (BlockPos pos : getMultiblockHandler().findBlock(block)) {
			BlockEntity entity = level.getBlockEntity(pos);
			if (type.isInstance(entity)) {
				result.add(type.cast(entity));
			}
		}
		return result;
	}

	@Nullable
	protected final <T extends BlockEntity> T findFirstMatchedBlockEntity(Block block, Class<T> type) {
		List<T> entities = findMatchedBlockEntities(block, type);
		return entities.isEmpty() ? null : entities.get(0);
	}

	protected final List<IOBlockEntity> findMatchedIOBlockEntities() {
		return findMatchedIOBlockEntities(IOBlockEntity.class);
	}

	protected final <T extends IOBlockEntity> List<T> findMatchedIOBlockEntities(Class<T> type) {
		if (level == null || !isStructureValid()) {
			return Collections.emptyList();
		}

		List<T> result = new ArrayList<>();
		for (BlockPos pos : getMultiblockHandler().findFilterBlock((state) -> true)) {
			BlockEntity entity = level.getBlockEntity(pos);
			if (type.isInstance(entity)) {
				T ioBlockEntity = type.cast(entity);
				if (ioBlockEntity.isControllerAllowed(this)) {
					result.add(ioBlockEntity);
				}
			}
		}
		return result;
	}

	@Nullable
	protected final IOBlockEntity findFirstMatchedIOBlockEntity() {
		return findFirstMatchedIOBlockEntity(IOBlockEntity.class);
	}

	@Nullable
	protected final <T extends IOBlockEntity> T findFirstMatchedIOBlockEntity(Class<T> type) {
		List<T> entities = findMatchedIOBlockEntities(type);
		return entities.isEmpty() ? null : entities.get(0);
	}

	protected final int countMatchedIOBlockEntities() {
		return findMatchedIOBlockEntities().size();
	}

	protected final int countMatchedItemIOBlockEntities() {
		return countMatchedAllowedIOBlockEntities(IOBlockEntity::hasItemHandler);
	}

	protected final int countMatchedFluidIOBlockEntities() {
		return countMatchedAllowedIOBlockEntities(IOBlockEntity::hasFluidHandler);
	}

	protected final int countMatchedEnergyIOBlockEntities() {
		return countMatchedAllowedIOBlockEntities(IOBlockEntity::hasEnergyStorage);
	}

	@Nullable
	protected final IOBlockEntity findFirstMatchedItemIOBlockEntity() {
		return findFirstMatchedAllowedIOBlockEntity(IOBlockEntity::hasItemHandler);
	}

	@Nullable
	protected final IOBlockEntity findFirstMatchedFluidIOBlockEntity() {
		return findFirstMatchedAllowedIOBlockEntity(IOBlockEntity::hasFluidHandler);
	}

	@Nullable
	protected final IOBlockEntity findFirstMatchedEnergyIOBlockEntity() {
		return findFirstMatchedAllowedIOBlockEntity(IOBlockEntity::hasEnergyStorage);
	}

	@Nullable
	protected final IItemHandler findFirstMatchedItemHandler() {
		IOBlockEntity ioBlockEntity = findFirstMatchedItemIOBlockEntity();
		return ioBlockEntity == null ? null : ioBlockEntity.getItemHandler();
	}

	@Nullable
	protected final IFluidHandler findFirstMatchedFluidHandler() {
		IOBlockEntity ioBlockEntity = findFirstMatchedFluidIOBlockEntity();
		return ioBlockEntity == null ? null : ioBlockEntity.getFluidHandler();
	}

	@Nullable
	protected final IEnergyStorage findFirstMatchedEnergyStorage() {
		IOBlockEntity ioBlockEntity = findFirstMatchedEnergyIOBlockEntity();
		return ioBlockEntity == null ? null : ioBlockEntity.getEnergyStorage();
	}

	protected final int getStoredEnergy() {
		return energyStored;
	}

	protected final int getConfiguredEnergyCapacity() {
		return configuredEnergyCapacity;
	}

	@NotNull
	protected final ItemStack getStoredItem(int slot) {
		if (itemStorage == null || !isValidItemSlot(slot)) {
			return ItemStack.EMPTY;
		}

		return itemStorage.getStackInSlot(slot);
	}

	protected final ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
		if (itemStorage == null || !isValidItemSlot(slot)) {
			return stack;
		}

		return itemStorage.insertItem(slot, stack, simulate);
	}

	protected final ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
		if (itemStorage == null || !isValidItemSlot(slot)) {
			return ItemStack.EMPTY;
		}

		return itemStorage.extractItem(slot, amount, simulate);
	}

	@NotNull
	protected final FluidStack getStoredFluid(int tank) {
		if (fluidStorage == null || !isValidFluidTank(tank)) {
			return FluidStack.EMPTY;
		}

		return fluidStorage.get(tank).copy();
	}

	protected final int fillFluidInternal(int tank, FluidStack stack, IFluidHandler.FluidAction action) {
		return fillFluidSlot(tank, stack, action, false);
	}

	@NotNull
	protected final FluidStack drainFluidInternal(int tank, int amount, IFluidHandler.FluidAction action) {
		return drainFluidSlot(tank, amount, action, false);
	}

	protected final int receiveEnergyInternal(int maxReceive, boolean simulate) {
		if (!internalEnergyStorageEnabled || maxReceive <= 0) {
			return 0;
		}

		int received = Math.min(configuredEnergyCapacity - energyStored, maxReceive);
		if (!simulate && received > 0) {
			energyStored += received;
			setChanged();
		}

		return received;
	}

	protected final int extractEnergyInternal(int maxExtract, boolean simulate) {
		if (!internalEnergyStorageEnabled || maxExtract <= 0) {
			return 0;
		}

		int extracted = Math.min(energyStored, maxExtract);
		if (!simulate && extracted > 0) {
			energyStored -= extracted;
			setChanged();
		}

		return extracted;
	}

	protected boolean supportsControllerItemIO() {
		return configuredItemSlotsHint() > 0;
	}

	protected boolean supportsControllerFluidIO() {
		return configuredFluidSlotsHint().length > 0;
	}

	protected boolean supportsControllerEnergyIO() {
		return configuredEnergyCapacityHint() > 0;
	}

	protected boolean useInternalItemStorage() {
		return configuredItemSlotsHint() > 0;
	}

	protected boolean useInternalFluidStorage() {
		return configuredFluidSlotsHint().length > 0;
	}

	protected boolean useInternalEnergyStorage() {
		return configuredEnergyCapacityHint() > 0;
	}

	protected boolean canExposeItemCapability(@Nullable Direction side) {
		return isMachineStructureValid();
	}

	protected boolean canExposeFluidCapability(@Nullable Direction side) {
		return isMachineStructureValid();
	}

	protected boolean canExposeEnergyCapability(@Nullable Direction side) {
		return isMachineStructureValid();
	}

	@Nullable
	protected IItemHandler createItemCapability() {
		return controllerItemIOEnabled && itemStorage != null ? new MachineItemHandler() : null;
	}

	@Nullable
	protected IFluidHandler createFluidCapability() {
		return controllerFluidIOEnabled && fluidStorage != null ? new MachineFluidHandler() : null;
	}

	@Nullable
	protected IEnergyStorage createEnergyCapability() {
		return controllerEnergyIOEnabled && internalEnergyStorageEnabled ? new MachineEnergyStorage() : null;
	}

	protected IOMode getItemIO(int slot) {
		return IOMode.BOTH;
	}

	protected IOMode getFluidIO(int slot) {
		return getFluidSlot(slot).mode();
	}

	protected IOMode getEnergyIO(int slot) {
		return IOMode.BOTH;
	}

	protected int getMinItemIO() {
		return 0;
	}

	protected int getMaxItemIO() {
		return Integer.MAX_VALUE;
	}

	protected int getMinFluidIO() {
		return 0;
	}

	protected int getMaxFluidIO() {
		return Integer.MAX_VALUE;
	}

	protected int getMinEnergyIO() {
		return 0;
	}

	protected int getMaxEnergyIO() {
		return Integer.MAX_VALUE;
	}

	protected int getItemSlots() {
		return 0;
	}

	protected FluidSlots[] getFluidSlots() {
		return FluidSlots.EMPTY;
	}

	protected int getEnergyCapacity() {
		return 0;
	}

	protected boolean isItemAllowed(int slot, @NotNull ItemStack stack) {
		return true;
	}

	protected WorkConditionResult getWorkCondition(MultiblockContext<? extends MachineControllerBlockEntity> context) {
		return WorkConditionResult.success();
	}

	protected void onWorkConditionFailed(MultiblockContext<? extends MachineControllerBlockEntity> context, WorkConditionResult result) {
	}

	private int countMatchedAllowedIOBlockEntities(Predicate<IOBlockEntity> predicate) {
		int count = 0;
		for (IOBlockEntity entity : findMatchedIOBlockEntities()) {
			if (predicate.test(entity)) {
				count++;
			}
		}
		return count;
	}

	@Nullable
	private IOBlockEntity findFirstMatchedAllowedIOBlockEntity(Predicate<IOBlockEntity> predicate) {
		for (IOBlockEntity entity : findMatchedIOBlockEntities()) {
			if (predicate.test(entity)) {
				return entity;
			}
		}
		return null;
	}

	private int configuredItemSlotsHint() {
		return Math.max(0, getItemSlots());
	}

	private FluidSlots[] configuredFluidSlotsHint() {
		return sanitizeFluidSlots(getFluidSlots());
	}

	private int configuredEnergyCapacityHint() {
		return Math.max(0, getEnergyCapacity());
	}

	private void rebuildCapabilities() {
		itemCapability.invalidate();
		fluidCapability.invalidate();
		energyCapability.invalidate();

		IItemHandler itemHandler = createItemCapability();
		IFluidHandler fluidHandler = createFluidCapability();
		IEnergyStorage energyHandler = createEnergyCapability();

		itemCapability = itemHandler == null ? LazyOptional.empty() : LazyOptional.of(() -> itemHandler);
		fluidCapability = fluidHandler == null ? LazyOptional.empty() : LazyOptional.of(() -> fluidHandler);
		energyCapability = energyHandler == null ? LazyOptional.empty() : LazyOptional.of(() -> energyHandler);
	}

	private ItemStackHandler createInternalItemStorage() {
		return new ItemStackHandler(configuredItemSlots) {
			@Override
			protected void onContentsChanged(int slot) {
				setChanged();
			}

			@Override
			public boolean isItemValid(int slot, @NotNull ItemStack stack) {
				return isItemAllowed(slot, stack);
			}
		};
	}

	private List<FluidStack> createInternalFluidStorage() {
		List<FluidStack> fluids = new ArrayList<>(configuredFluidSlots.length);
		for (int i = 0; i < configuredFluidSlots.length; i++) {
			fluids.add(FluidStack.EMPTY);
		}
		return fluids;
	}

	private boolean hasRequiredIOCounts() {
		return isCountValid(getActualItemIOCount(), getMinItemIO(), getMaxItemIO())
				&& isCountValid(getActualFluidIOCount(), getMinFluidIO(), getMaxFluidIO())
				&& isCountValid(getActualEnergyIOCount(), getMinEnergyIO(), getMaxEnergyIO());
	}

	protected int getActualItemIOCount() {
		return controllerItemIOEnabled ? 1 : 0;
	}

	protected int getActualFluidIOCount() {
		return controllerFluidIOEnabled ? 1 : 0;
	}

	protected int getActualEnergyIOCount() {
		return controllerEnergyIOEnabled ? 1 : 0;
	}

	private boolean isCountValid(int actual, int min, int max) {
		int normalizedMin = Math.max(0, min);
		int normalizedMax = Math.max(normalizedMin, max);
		return actual >= normalizedMin && actual <= normalizedMax;
	}

	private FluidSlots[] sanitizeFluidSlots(@Nullable FluidSlots[] slots) {
		if (slots == null || slots.length == 0) {
			return FluidSlots.EMPTY;
		}

		FluidSlots[] copy = new FluidSlots[slots.length];
		for (int i = 0; i < slots.length; i++) {
			copy[i] = slots[i];
			if (copy[i] == null) {
				throw new IllegalArgumentException("Fluid slot definition at index " + i + " is null");
			}
		}
		return copy;
	}

	private int clampEnergy(int energy) {
		return Math.max(0, Math.min(configuredEnergyCapacity, energy));
	}

	private boolean isValidItemSlot(int slot) {
		return itemStorage != null && slot >= 0 && slot < itemStorage.getSlots();
	}

	private boolean isValidFluidTank(int tank) {
		return tank >= 0 && tank < configuredFluidSlots.length;
	}

	private FluidSlots getFluidSlot(int tank) {
		if (!isValidFluidTank(tank)) {
			throw new IllegalArgumentException("Invalid fluid tank index: " + tank);
		}
		return configuredFluidSlots[tank];
	}

	private int fillFluidSlot(int tank, FluidStack stack, IFluidHandler.FluidAction action, boolean respectMode) {
		if (fluidStorage == null || !isValidFluidTank(tank) || stack.isEmpty()) {
			return 0;
		}

		FluidSlots slot = getFluidSlot(tank);
		if (respectMode && !getFluidIO(tank).canInsert()) {
			return 0;
		}

		if (!slot.accepts(stack)) {
			return 0;
		}

		FluidStack stored = fluidStorage.get(tank);
		if (!stored.isEmpty() && !stored.isFluidEqual(stack)) {
			return 0;
		}

		int fillable = Math.min(stack.getAmount(), slot.capacity() - stored.getAmount());
		if (fillable <= 0) {
			return 0;
		}

		if (action.execute()) {
			FluidStack updated = stored.isEmpty() ? new FluidStack(stack, fillable) : stored.copy();
			if (!stored.isEmpty()) {
				updated.grow(fillable);
			}
			fluidStorage.set(tank, updated);
			setChanged();
		}

		return fillable;
	}

	private FluidStack drainFluidSlot(int tank, int amount, IFluidHandler.FluidAction action, boolean respectMode) {
		if (fluidStorage == null || !isValidFluidTank(tank) || amount <= 0) {
			return FluidStack.EMPTY;
		}

		if (respectMode && !getFluidIO(tank).canExtract()) {
			return FluidStack.EMPTY;
		}

		FluidStack stored = fluidStorage.get(tank);
		if (stored.isEmpty()) {
			return FluidStack.EMPTY;
		}

		int drained = Math.min(amount, stored.getAmount());
		FluidStack result = new FluidStack(stored, drained);

		if (action.execute()) {
			FluidStack updated = stored.copy();
			updated.shrink(drained);
			fluidStorage.set(tank, updated.isEmpty() ? FluidStack.EMPTY : updated);
			setChanged();
		}

		return result;
	}

	private final class MachineItemHandler implements IItemHandler {
		@Override
		public int getSlots() {
			return itemStorage == null ? 0 : itemStorage.getSlots();
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return itemStorage == null || !isValidItemSlot(slot) ? ItemStack.EMPTY : itemStorage.getStackInSlot(slot);
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (itemStorage == null || !isValidItemSlot(slot) || stack.isEmpty() || !isMachineStructureValid() || !getItemIO(slot).canInsert()) {
				return stack;
			}

			return itemStorage.insertItem(slot, stack, simulate);
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (itemStorage == null || !isValidItemSlot(slot) || !isMachineStructureValid() || !getItemIO(slot).canExtract()) {
				return ItemStack.EMPTY;
			}

			return itemStorage.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return itemStorage == null || !isValidItemSlot(slot) ? 0 : itemStorage.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return itemStorage != null && isValidItemSlot(slot) && getItemIO(slot).canInsert() && itemStorage.isItemValid(slot, stack);
		}
	}

	private final class MachineFluidHandler implements IFluidHandler {
		@Override
		public int getTanks() {
			return configuredFluidSlots.length;
		}

		@Override
		public @NotNull FluidStack getFluidInTank(int tank) {
			return isMachineStructureValid() && isValidFluidTank(tank) ? getStoredFluid(tank) : FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank) {
			return isValidFluidTank(tank) ? getFluidSlot(tank).capacity() : 0;
		}

		@Override
		public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
			if (!isValidFluidTank(tank) || !getFluidIO(tank).canInsert()) {
				return false;
			}

			FluidStack stored = fluidStorage == null ? FluidStack.EMPTY : fluidStorage.get(tank);
			return getFluidSlot(tank).accepts(stack) && (stored.isEmpty() || stored.isFluidEqual(stack));
		}

		@Override
		public int fill(FluidStack stack, FluidAction action) {
			if (!isMachineStructureValid() || stack.isEmpty()) {
				return 0;
			}

			int totalFilled = 0;
			for (int pass = 0; pass < 2 && totalFilled < stack.getAmount(); pass++) {
				for (int tank = 0; tank < configuredFluidSlots.length && totalFilled < stack.getAmount(); tank++) {
					FluidStack stored = fluidStorage == null ? FluidStack.EMPTY : fluidStorage.get(tank);
					boolean matchesPass = pass == 0 ? !stored.isEmpty() && stored.isFluidEqual(stack) : stored.isEmpty();
					if (!matchesPass) {
						continue;
					}

					FluidStack remaining = stack.copy();
					remaining.setAmount(stack.getAmount() - totalFilled);
					totalFilled += fillFluidSlot(tank, remaining, action, true);
				}
			}

			return totalFilled;
		}

		@Override
		public @NotNull FluidStack drain(FluidStack stack, FluidAction action) {
			if (!isMachineStructureValid() || stack.isEmpty()) {
				return FluidStack.EMPTY;
			}

			for (int tank = 0; tank < configuredFluidSlots.length; tank++) {
				FluidStack stored = fluidStorage == null ? FluidStack.EMPTY : fluidStorage.get(tank);
				if (!stored.isEmpty() && stored.isFluidEqual(stack)) {
					return drainFluidSlot(tank, stack.getAmount(), action, true);
				}
			}

			return FluidStack.EMPTY;
		}

		@Override
		public @NotNull FluidStack drain(int amount, FluidAction action) {
			if (!isMachineStructureValid() || amount <= 0) {
				return FluidStack.EMPTY;
			}

			for (int tank = 0; tank < configuredFluidSlots.length; tank++) {
				FluidStack stored = fluidStorage == null ? FluidStack.EMPTY : fluidStorage.get(tank);
				if (!stored.isEmpty()) {
					return drainFluidSlot(tank, amount, action, true);
				}
			}

			return FluidStack.EMPTY;
		}
	}

	private final class MachineEnergyStorage implements IEnergyStorage {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (!isMachineStructureValid() || !getEnergyIO(0).canInsert()) {
				return 0;
			}

			return receiveEnergyInternal(maxReceive, simulate);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!isMachineStructureValid() || !getEnergyIO(0).canExtract()) {
				return 0;
			}

			return extractEnergyInternal(maxExtract, simulate);
		}

		@Override
		public int getEnergyStored() {
			return energyStored;
		}

		@Override
		public int getMaxEnergyStored() {
			return configuredEnergyCapacity;
		}

		@Override
		public boolean canExtract() {
			return isMachineStructureValid() && getEnergyIO(0).canExtract();
		}

		@Override
		public boolean canReceive() {
			return isMachineStructureValid() && getEnergyIO(0).canInsert();
		}
	}
}
