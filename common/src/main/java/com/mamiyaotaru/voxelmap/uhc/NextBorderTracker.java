package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class NextBorderTracker {

    private static double centerX = 0;
    private static double centerZ = 0;
    private static double size = -1; // -1 = ไม่มี border ถัดไป

    public static double getCenterX() { return centerX; }
    public static double getCenterZ() { return centerZ; }
    public static double getSize() { return size; }
    public static boolean hasNextBorder() { return size > 0; }

    public record NextBorderPayload(double cx, double cz, double sz) implements CustomPacketPayload {
        public static final Type<NextBorderPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "next_border"));

        public static final StreamCodec<RegistryFriendlyByteBuf, NextBorderPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> new NextBorderPayload(buf.readDouble(), buf.readDouble(), buf.readDouble())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void clear() {
        size = -1;
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(NextBorderPayload.TYPE, NextBorderPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                NextBorderPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    centerX = payload.cx();
                    centerZ = payload.cz();
                    size    = payload.sz();
                })
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    centerX = 0;
                    centerZ = 0;
                    size = -1;
                }
        );
    }
}