package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class NextBorderTracker {

    private static double overworldCenterX = 0, overworldCenterZ = 0, overworldSize = -1;
    private static double netherCenterX = 0, netherCenterZ = 0, netherSize = -1;

    private static String normalizeDim(String dim) {
        return dim.contains("nether") ? "nether" : "overworld";
    }

    public static boolean hasNextBorder(String dim) {
        return normalizeDim(dim).equals("nether") ? netherSize > 0 : overworldSize > 0;
    }

    public static double getCenterX(String dim) {
        return normalizeDim(dim).equals("nether") ? netherCenterX : overworldCenterX;
    }

    public static double getCenterZ(String dim) {
        return normalizeDim(dim).equals("nether") ? netherCenterZ : overworldCenterZ;
    }

    public static double getSize(String dim) {
        return normalizeDim(dim).equals("nether") ? netherSize : overworldSize;
    }

    public static void clear() {
        overworldSize = -1;
        netherSize = -1;
    }

    public record NextBorderPayload(double cx, double cz, double sz, String dim) implements CustomPacketPayload {
        public static final Type<NextBorderPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "next_border"));

        public static final StreamCodec<RegistryFriendlyByteBuf, NextBorderPayload> CODEC =
                StreamCodec.of(
                        (buf, payload) -> {},
                        buf -> new NextBorderPayload(
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.isReadable() ? buf.readUtf() : "overworld"
                        )
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay()
                .register(NextBorderPayload.TYPE, NextBorderPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(
                NextBorderPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    if (normalizeDim(payload.dim()).equals("nether")) {
                        netherCenterX = payload.cx();
                        netherCenterZ = payload.cz();
                        netherSize    = payload.sz();
                    } else {
                        overworldCenterX = payload.cx();
                        overworldCenterZ = payload.cz();
                        overworldSize    = payload.sz();
                    }
                    System.out.println("[NextBorderTracker] Received: cx=" + payload.cx() + " cz=" + payload.cz() + " size=" + payload.sz() + " dim=" + payload.dim());
                })
        );

        ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    overworldSize = -1;
                    netherSize    = -1;
                    overworldCenterX = overworldCenterZ = 0;
                    netherCenterX    = netherCenterZ    = 0;
                }
        );
    }
}