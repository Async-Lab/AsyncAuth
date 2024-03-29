package club.asyncraft.asyncauth.server.event;

import club.asyncraft.asyncauth.AsyncAuth;
import club.asyncraft.asyncauth.common.network.ClientInitializeMessage;
import club.asyncraft.asyncauth.common.network.CommonPacketManager;
import club.asyncraft.asyncauth.common.util.MessageUtils;
import club.asyncraft.asyncauth.server.PlayerManager;
import club.asyncraft.asyncauth.server.util.i18n.TranslationContext;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.StringUtils;

@Mod.EventBusSubscriber(modid = AsyncAuth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class PlayerEventHandler {

/*
    public static void onPlayerLoginClientSide(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.getServer() != null) {
            return;
        }
    }
*/

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        PlayerManager.subscribeUnLoginPlayer(((ServerPlayerEntity) player));
        CommonPacketManager.clientInitializeChannel.sendTo(new ClientInitializeMessage(true, TranslationContext.clientMessage), ((ServerPlayerEntity) player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerManager.logoutPlayer((ServerPlayerEntity) event.getPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerMove(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        if (!PlayerManager.hasLogin(player)) {
            player.teleportTo(player.xOld, player.yOld, player.zOld);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerChat(ServerChatEvent event) {
        String prefix = StringUtils.split(event.getMessage())[0];
        if (StringUtils.isEmpty(prefix)) {
            return;
        }
        if (MessageUtils.isRegisterPrefix(prefix) || MessageUtils.isLoginPrefix(prefix)) {
            return;
        }
        ServerPlayerEntity player = event.getPlayer();
        verify(player, event, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = ((PlayerEntity) event.getEntity());
            verify(player, event, false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerCommand(CommandEvent event) {
        Entity entity = event.getParseResults().getContext().getSource().getEntity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            verify(player,event,true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            verify(player, event, false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerBreak(BlockEvent.BreakEvent event) {
        verify(event.getPlayer(), event, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        verify(event.getPlayer(), event, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        verify(event.getPlayer(), event, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickEmpty event) {
        verify(event.getPlayer(), event, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        verify(event.getPlayer(), event, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        verify(event.getPlayer(), event, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamaged(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof PlayerEntity) {
            verify(((PlayerEntity) entity),event,false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDrop(ItemTossEvent event) {
        ServerPlayerEntity player = ((ServerPlayerEntity) event.getPlayer());
        if (!PlayerManager.hasLogin(player)) {
            ItemStack item = event.getEntityItem().getItem();
            player.addItem(item);
            event.setCanceled(true);
        }
    }

    private static void verify(PlayerEntity player, Event event, boolean sendMsg) {
        if (!PlayerManager.hasLogin(player)) {
            event.setCanceled(true);
            if (sendMsg) {
                sendUnLoginMessage(player);
            }
        }
    }

    private static void sendUnLoginMessage(PlayerEntity player) {
        MessageUtils.sendConfigMessageOnServer(player, "login.un_login_info");
    }

}
