package red.jackf.lenientdeath.mixins.itemresilience;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.lenientdeath.ItemResilience;
import red.jackf.lenientdeath.LenientDeath;
import red.jackf.lenientdeath.mixinutil.DeathContext;
import red.jackf.lenientdeath.mixinutil.LDDeathContextHolder;
import red.jackf.lenientdeath.mixinutil.LDGroundedPosHolder;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LDGroundedPosHolder, LDDeathContextHolder {
    /**
     * The last position that an entity was on the ground.
     */
    @Unique
    private @Nullable GlobalPos lastGroundedPos = null;
    /**
     * Not serialized. Used to pass damage-related info up until the inventory drop calls
     */
    @Unique
    private @Nullable DeathContext deathContext = null;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    // last grounded pos set/get
    @Override
    public @Nullable GlobalPos lenientdeath$getLastGroundedPosition() {
        return this.lastGroundedPos;
    }

    @Override
    public void lenientdeath$setLastGroundedPosition(@Nullable GlobalPos pos) {
        this.lastGroundedPos = pos;
    }

    // add death source for future calls
    @Inject(method = "die", at = @At("HEAD"))
    private void addDeathContext(DamageSource damageSource, CallbackInfo ci) {
        this.deathContext = new DeathContext(damageSource);
    }

    @Override
    public @Nullable DeathContext lenientdeath$getDeathContext() {
        return deathContext;
    }

    // read grounded position
    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("RETURN"))
    private void loadGroundedPos(ValueInput valueInput, CallbackInfo ci) {
        this.lastGroundedPos = valueInput.read(LAST_GROUNDED_POS, GlobalPos.CODEC)
                .orElse(null);
    }

    // save grounded position
    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("RETURN"))
    private void saveGroundedPos(ValueOutput valueOutput, CallbackInfo ci) {
        if (this.lastGroundedPos != null) {
            valueOutput.store(LAST_GROUNDED_POS, GlobalPos.CODEC, this.lastGroundedPos);
        }
    }
}
