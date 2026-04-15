package red.jackf.lenientdeath.mixins.perplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.lenientdeath.LenientDeath;
import red.jackf.lenientdeath.mixinutil.LDPerPlayer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements LDPerPlayer {
    @Unique
    private boolean perPlayerEnabledForMe = LenientDeath.CONFIG.instance().perPlayer.defaultEnabledForPlayer;

    public ServerPlayerMixin(
            Level level,
            BlockPos pos,
            float yRot,
            GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public boolean lenientdeath$isPerPlayerEnabled() {
        return perPlayerEnabledForMe;
    }

    @Override
    public void lenientdeath$setPerPlayerEnabled(boolean newValue) {
        this.perPlayerEnabledForMe = newValue;
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("RETURN"))
    private void lenientdeath$getModData(ValueInput valueInput, CallbackInfo ci) {
        this.perPlayerEnabledForMe = valueInput.getBooleanOr(PER_PLAYER_TAG_KEY, this.perPlayerEnabledForMe);
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("RETURN"))
    private void lenientdeath$addModData(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.putBoolean(PER_PLAYER_TAG_KEY, this.perPlayerEnabledForMe);
    }
}
