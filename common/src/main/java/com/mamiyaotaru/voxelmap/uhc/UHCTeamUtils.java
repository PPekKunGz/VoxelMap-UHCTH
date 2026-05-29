package com.mamiyaotaru.voxelmap.uhc;

import net.minecraft.client.Minecraft;

public final class UHCTeamUtils {
    private static final java.util.Set<String> DEV_TEAMS = java.util.Set.of("Admin", "Spectator", "Dimension");

    public static boolean isVisiblePlayer(String otherPlayerName) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return false;

        var scoreboard = mc.level.getScoreboard();
        String myName = mc.player.getName().getString();

        var myTeam = scoreboard.getPlayersTeam(myName);
        var otherTeam = scoreboard.getPlayersTeam(otherPlayerName);

        String myTeamName = myTeam != null ? myTeam.getName() : "";
        String otherTeamName = otherTeam != null ? otherTeam.getName() : "";

        boolean iAmDev = DEV_TEAMS.stream().anyMatch(t -> t.equalsIgnoreCase(myTeamName));

        if (iAmDev) {
            if (DEV_TEAMS.stream().anyMatch(t -> t.equalsIgnoreCase(otherTeamName))) return false;
            return !myTeamName.equalsIgnoreCase(otherTeamName);
        } else {
            if (myTeamName.isEmpty() || otherTeamName.isEmpty()) return false;
            return myTeamName.equalsIgnoreCase(otherTeamName);
        }
    }
}