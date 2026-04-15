package red.jackf.lenientdeath.restoreinventory;

import com.mojang.serialization.DataResult;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import red.jackf.lenientdeath.LenientDeath;

import java.time.Instant;
import java.util.Optional;

public record DeathRecord(Inventory inventory,
                          Optional<TrinketsRecord> trinketsInventory,
                          Instant timeOfDeath,
                          Component deathMessage,
                          GlobalPos location,
                          int experience) {
    private static final String INVENTORY = "Inventory";
    private static final String TRINKETS_INVENTORY = "TrinketsInventory";
    private static final String TIME_OF_DEATH = "TimeOfDeath";
    private static final String DEATH_MESSAGE = "DeathMessage";
    private static final String LOCATION = "Location";
    private static final String EXPERIENCE = "Experience";

    public static DataResult<DeathRecord> fromTag(ServerPlayer player, CompoundTag tag) {
        return DataResult.error(() -> "DeathRecord loading is not yet migrated to 1.21.10 APIs");
    }

    public CompoundTag toTag(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();

        // TODO migrate inventory serialization for 1.21.10
        this.trinketsInventory.ifPresent(record -> encodeTrinket(tag, record));
        tag.put(TIME_OF_DEATH, ExtraCodecs.INSTANT_ISO8601.encodeStart(NbtOps.INSTANCE, this.timeOfDeath).result().orElseThrow());
        tag.putString(DEATH_MESSAGE, this.deathMessage.getString());
        tag.put(LOCATION, GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, this.location).result().orElseThrow());
        tag.put(EXPERIENCE, IntTag.valueOf(this.experience));

        return tag;
    }

    private static void encodeTrinket(CompoundTag tag, TrinketsRecord trinketsRecord) {
        TrinketsRecord.CODEC.encodeStart(NbtOps.INSTANCE, trinketsRecord)
                .ifSuccess(trinkets -> tag.put(TRINKETS_INVENTORY, trinkets))
                .ifError(err -> LenientDeath.LOGGER.error("Error saving trinkets inventory: {}", err.message()));
    }
}
