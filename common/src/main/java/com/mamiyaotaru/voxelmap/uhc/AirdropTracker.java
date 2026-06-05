package com.mamiyaotaru.voxelmap.uhc;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AirdropTracker {

    public record AirdropPoint(int x, int z) {}
    private static final List<AirdropPoint> airdropPoints = new CopyOnWriteArrayList<>();
    public static List<AirdropPoint> getAirdropPoints() { return airdropPoints; }

    public record AirdropPayload(int x, int z) implements CustomPacketPayload {
        public static final Type<AirdropPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("dmsuhc", "airdrop"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AirdropPayload> CODEC = StreamCodec.of((buf, payload) -> {}, buf -> new AirdropPayload(buf.readInt(), buf.readInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay() .register(AirdropPayload.TYPE, AirdropPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver( AirdropPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    airdropPoints.add(new AirdropPoint(payload.x(), payload.z()));
                    System.out.println("[AirdropTracker] Airdrop at X:" + payload.x() + " Z:" + payload.z());
                })
        );

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> {
                    airdropPoints.clear();
                    System.out.println("[AirdropTracker] Cleared airdrop points on disconnect");
                }
        );
    }
}