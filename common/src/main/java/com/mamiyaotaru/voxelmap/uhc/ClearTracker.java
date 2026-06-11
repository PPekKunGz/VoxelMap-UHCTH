package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ClearTracker {

    public record ClearPayload() implements CustomPacketPayload {
        public static final Type<ClearPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "clear"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClearPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> new ClearPayload()
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(ClearPayload.TYPE, ClearPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                ClearPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    PlayerTracker.getPlayers().clear();
                    RespawnTracker.getRespawnPoints().clear();
                    AirdropTracker.getAirdropPoints().clear();
                    NextBorderTracker.clear();
                })
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    PlayerTracker.getPlayers().clear();
                }
        );
    }
}