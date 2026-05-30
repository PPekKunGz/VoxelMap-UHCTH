package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerTracker {

    public record TrackedPlayer(String name, double x, double y, double z, java.util.UUID uuid, String dimension) {}

    private static final List<TrackedPlayer> players = new CopyOnWriteArrayList<>();

    public static List<TrackedPlayer> getPlayers() {
        return players;
    }

    public record PlayerListPayload(List<TrackedPlayer> list) implements CustomPacketPayload {
        public static final Type<PlayerListPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "players"));

        public static final StreamCodec<RegistryFriendlyByteBuf, PlayerListPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> {
                            int count = buf.readInt();
                            List<TrackedPlayer> result = new ArrayList<>();
                            for (int i = 0; i < count; i++) {
                                String name = buf.readUtf();
                                double x = buf.readDouble();
                                double y = buf.readDouble();
                                double z = buf.readDouble();
                                long msb = buf.readLong();
                                long lsb = buf.readLong();
                                java.util.UUID uuid = new java.util.UUID(msb, lsb);
                                String dimension = buf.isReadable() ? buf.readUtf() : "minecraft:overworld";
                                result.add(new TrackedPlayer(name, x, y, z, uuid, dimension));
                            }
                            return new PlayerListPayload(result);
                        }
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(PlayerListPayload.TYPE, PlayerListPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                PlayerListPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    players.clear();
                    players.addAll(payload.list());
                })
        );
    }
}