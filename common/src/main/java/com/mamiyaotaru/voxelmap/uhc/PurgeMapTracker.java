package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class PurgeMapTracker {

    public record PurgeMapPayload() implements CustomPacketPayload {
        public static final Type<PurgeMapPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "purge_map"));

        public static final StreamCodec<RegistryFriendlyByteBuf, PurgeMapPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> new PurgeMapPayload()
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(PurgeMapPayload.TYPE, PurgeMapPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                PurgeMapPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    System.out.println("[PurgeMapTracker] Received purge_map packet");
                    RespawnTracker.getRespawnPoints().clear();
                    AirdropTracker.getAirdropPoints().clear();
                    NextBorderTracker.clear();

                    try {
                        var instance = com.mamiyaotaru.voxelmap.VoxelConstants.getVoxelMapInstance();
                        System.out.println("[PurgeMapTracker] VoxelMap instance: " + (instance != null ? "OK" : "NULL"));
                        if (instance != null) {
                            var map = instance.getPersistentMap();
                            System.out.println("[PurgeMapTracker] PersistentMap: " + (map != null ? "OK" : "NULL"));
                            if (map != null) {
                                map.purgeCachedRegions();
                                System.out.println("[PurgeMapTracker] Map cache purged");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[PurgeMapTracker] Error: " + e.getMessage());
                    }
                })
        );
    }
}