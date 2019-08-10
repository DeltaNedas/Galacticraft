package com.hrznstudio.galacticraft.api.wire;

import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.api.block.entity.ConfigurableElectricMachineBlockEntity;
import com.hrznstudio.galacticraft.api.entity.WireBlockEntity;
import com.hrznstudio.galacticraft.util.WireConnectable;
import io.github.cottonmc.energy.api.EnergyAttributeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 * @see WireNetwork
 */
public class WireUtils {

    private static WireNetwork network;

    public static BlockPos getPosFromDirection(Direction direction, BlockPos pos) {
        if (pos == null || direction == null) return null;
        if (direction == Direction.NORTH) {
            return pos.north();
        } else if (direction == Direction.SOUTH) {
            return pos.south();
        } else if (direction == Direction.EAST) {
            return pos.east();
        } else if (direction == Direction.WEST) {
            return pos.west();
        } else if (direction == Direction.UP) {
            return pos.up();
        } else {
            return pos.down();
        }
    }

    /**
     * Attempts to find a WireNetwork with a certain ID.
     *
     * @param id The ID of the wanted WireNetwork
     * @return The network with the specified ID.
     */
    public static WireNetwork getNetworkFromId(UUID id) {
        network = null;
        WireNetwork.networkMap.forEach((wireNetwork, blockPos) -> {
            if (wireNetwork.getId() == id) network = wireNetwork;
        });
        return network;
    }

    /**
     * Attempts to find a temporary WireNetwork with a certain ID.
     *
     * @param id The ID of the wanted WireNetwork
     * @return The network with the specified ID.
     */
    public static WireNetwork getTempNetworkFromId(UUID id) {
        network = null;
        WireNetwork.networkMap.forEach((wireNetwork, blockPos) -> {
            if (wireNetwork.getId() == id) network = wireNetwork;
        });
        return network;
    }

    /**
     * @param pos   The source block's position.
     * @param world The world the block is located in.
     * @return An array of all the adjacent consumers (BlockEntities that consume energy).
     */
    public static BlockEntity[] getAdjacentConsumers(BlockPos pos, World world) {
        final BlockEntity[] adjacentConnections = new BlockEntity[6];

        for (Direction direction : Direction.values()) {
            BlockPos adjacentBlockPos = getPosFromDirection(direction, pos);
            Block block = world.getBlockState(adjacentBlockPos).getBlock();

            if (block == null) {
                continue;
            }

            if (block instanceof WireConnectable) {
                if (((WireConnectable) block).canWireConnect(world, direction.getOpposite(), pos, adjacentBlockPos) == WireConnectionType.ENERGY_INPUT) {
                    if (world.getBlockEntity(adjacentBlockPos) instanceof ConfigurableElectricMachineBlockEntity) {
                        if (((ConfigurableElectricMachineBlockEntity) world.getBlockEntity(adjacentBlockPos)).active()) {
                            adjacentConnections[direction.getId()] = world.getBlockEntity(getPosFromDirection(direction, pos)); //Don't send energy to blocks that are not enabled
                        }
                    } else {
                        adjacentConnections[direction.getId()] = world.getBlockEntity(getPosFromDirection(direction, pos));
                    }
                }
            } else {
                if (world.getBlockEntity(adjacentBlockPos) != null) {
                    if (world.getBlockEntity(adjacentBlockPos) instanceof EnergyAttributeProvider) {
                        if (((EnergyAttributeProvider) world.getBlockEntity(adjacentBlockPos)).getEnergyAttribute() != null) {
                            if (((EnergyAttributeProvider) world.getBlockEntity(adjacentBlockPos)).getEnergyAttribute().canInsertEnergy()) {
                                adjacentConnections[direction.getId()] = world.getBlockEntity(getPosFromDirection(direction, pos));
                            }
                        }
                    }
                }
            }
        }
        return adjacentConnections;
    }


    /**
     * @param pos   The source block's position.
     * @param world The world the block is located in.
     * @return An array of all the adjacent producers (BlockEntities that produce/generate energy).
     */
    public static BlockEntity[] getAdjacentProducers(BlockPos pos, World world) {

        final BlockEntity[] adjacentConnections = new BlockEntity[6];

        for (Direction direction : Direction.values()) {
            BlockPos adjacentBlockPos = getPosFromDirection(direction, pos);
            Block block = world.getBlockState(adjacentBlockPos).getBlock();

            if (block == null) {
                continue;
            }

            if (block instanceof WireConnectable) {
                if (((WireConnectable) block).canWireConnect(world, direction.getOpposite(), pos, adjacentBlockPos) == WireConnectionType.ENERGY_OUTPUT) {
                    adjacentConnections[direction.getId()] = world.getBlockEntity(getPosFromDirection(direction, pos));
                } else {
                    if (world.getBlockEntity(adjacentBlockPos) != null) {
                        if (world.getBlockEntity(adjacentBlockPos) instanceof EnergyAttributeProvider) {
                            if (((EnergyAttributeProvider) world.getBlockEntity(adjacentBlockPos)).getEnergyAttribute() != null) {
                                if (((EnergyAttributeProvider) world.getBlockEntity(adjacentBlockPos)).getEnergyAttribute().canInsertEnergy()) {
                                    adjacentConnections[direction.getId()] = world.getBlockEntity(getPosFromDirection(direction, pos));
                                }
                            }
                        }
                    }
                }
            }
        }
        return adjacentConnections;
    }

    /**
     * @param pos   The source block's position.
     * @param world The world the block is located in.
     * @return An array of all the adjacent wires.
     */
    public static WireBlockEntity[] getAdjacentWires(BlockPos pos, World world) {
        final WireBlockEntity[] adjacentConnections = new WireBlockEntity[6];

        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = world.getBlockEntity(getPosFromDirection(direction, pos));

            if (blockEntity instanceof WireBlockEntity) {
                if (world.getBlockEntity(pos) instanceof WireBlockEntity) {
                    WireBlockEntity base = (WireBlockEntity)world.getBlockEntity(pos);
                    if (((WireBlockEntity) blockEntity).networkId != base.networkId) {
                        Galacticraft.logger.debug("Converting a wire at {} from the network with the id: {} to {}", blockEntity.getPos(), ((WireBlockEntity) blockEntity).networkId, base.networkId);
                        try {
                            WireUtils.getNetworkFromId(((WireBlockEntity) blockEntity).networkId).wires.forEach(wireBlockEntity -> {
                                wireBlockEntity.networkId = base.networkId;
                                WireUtils.getNetworkFromId(base.networkId).wires.add(base);
                            });
                            WireNetwork.networkMap.remove(WireUtils.getNetworkFromId(((WireBlockEntity) blockEntity).networkId));
                        } catch (NullPointerException ignore) {
                            try {
                                WireNetwork.networkMap_TEMP.remove(blockEntity.getPos());
                                ((WireBlockEntity) blockEntity).networkId = base.networkId;
                                WireUtils.getNetworkFromId(base.networkId).wires.add(((WireBlockEntity) blockEntity));
                            } catch (Exception ignore2) {}
                        }
                        ((WireBlockEntity) blockEntity).networkId = base.networkId;

                    }
                }
                adjacentConnections[direction.getId()] = (WireBlockEntity)blockEntity;
            }
        }
        return adjacentConnections;
    }
}
