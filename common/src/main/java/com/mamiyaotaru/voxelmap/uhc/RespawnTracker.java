package com.mamiyaotaru.voxelmap.uhc;

import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RespawnTracker {

    public record TrackerRespawn(double x, double z) {}

    private static final List<TrackerRespawn> respawnPoints = new CopyOnWriteArrayList<>();

    public static List<TrackerRespawn> getRespawnPoints() {
        return respawnPoints;
    }

    public record RespawnPayload(List<TrackerRespawn> list) implements CustomPacketPayload {
        public static final Type<RespawnPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "respawn"));

        public static final StreamCodec<RegistryFriendlyByteBuf, RespawnPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> {
                            int count = buf.readInt();
                            List<TrackerRespawn> result = new ArrayList<>();
                            for (int i = 0; i < count; i++) {
                                double x = buf.readDouble();
                                double z = buf.readDouble();
                                result.add(new TrackerRespawn(x, z));
                            }
                            return new RespawnPayload(result);
                        }
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(RespawnPayload.TYPE, RespawnPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                RespawnPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    respawnPoints.clear();
                    respawnPoints.addAll(payload.list());

                    System.out.println("[RespawnTracker] Received " + payload.list().size() + " respawn points:");
//                    for (TrackerRespawn point : payload.list()) {
//                        System.out.println("  - x: " + point.x() + ", z: " + point.z());
//                    }
                })
        );

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    respawnPoints.clear();
                    System.out.println("[RespawnTracker] Cleared respawn points on disconnect");
                }
        );
    }
}