package com.sportsmanager.ui;

import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.volleyball.VolleyballPlayer;
import com.sportsmanager.volleyball.VolleyballPosition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LineupAutoPick {

    private LineupAutoPick() {
    }

    public static List<IPlayer> pickBestLineup(ITeam team, GameSession session) {
        int size = session.getSport().getStartingLineupSize();
        List<IPlayer> pool = team.getMatchDaySquad().stream()
                .filter(p -> !p.isInjured())
                .sorted(Comparator.comparingInt(IPlayer::getOverallRating).reversed())
                .toList();

        String sport = session.getSport().getName();
        if ("Football".equalsIgnoreCase(sport)) {
            return pickFootball(pool, size);
        }
        if ("Volleyball".equalsIgnoreCase(sport)) {
            return pickVolleyball(pool, size);
        }
        return pool.size() >= size ? new ArrayList<>(pool.subList(0, size)) : new ArrayList<>(pool);
    }

    public static boolean isLineupReady(ITeam team, GameSession session) {
        List<IPlayer> starters = team.getStartingEleven();
        int need = session.getSport().getStartingLineupSize();
        if (starters == null || starters.size() != need) {
            return false;
        }
        List<IPlayer> matchDay = team.getMatchDaySquad();
        for (IPlayer p : starters) {
            if (p.isInjured()) {
                return false;
            }
            if (!matchDay.contains(p)) {
                return false;
            }
        }
        return passesSportRules(starters, session);
    }

    private static boolean passesSportRules(List<IPlayer> starters, GameSession session) {
        String sport = session.getSport().getName();
        if ("Football".equalsIgnoreCase(sport)) {
            long gks = starters.stream()
                    .filter(p -> p instanceof FootballPlayer fp
                            && fp.getFootballPosition() == FootballPosition.GK)
                    .count();
            return gks == 1;
        }
        if ("Volleyball".equalsIgnoreCase(sport)) {
            long setters = starters.stream()
                    .filter(p -> p instanceof VolleyballPlayer vp
                            && vp.getVolleyballPosition() == VolleyballPosition.SETTER)
                    .count();
            long liberos = starters.stream()
                    .filter(p -> p instanceof VolleyballPlayer vp
                            && vp.getVolleyballPosition() == VolleyballPosition.LIBERO)
                    .count();
            return setters >= 1 && liberos <= 1;
        }
        return true;
    }

    private static List<IPlayer> pickFootball(List<IPlayer> pool, int size) {
        List<IPlayer> picked = new ArrayList<>();
        IPlayer bestGk = pool.stream()
                .filter(p -> p instanceof FootballPlayer fp
                        && fp.getFootballPosition() == FootballPosition.GK)
                .findFirst()
                .orElse(null);
        if (bestGk != null) {
            picked.add(bestGk);
        }
        for (IPlayer p : pool) {
            if (picked.size() >= size) {
                break;
            }
            if (p == bestGk) {
                continue;
            }
            if (p instanceof FootballPlayer fp && fp.getFootballPosition() == FootballPosition.GK) {
                continue;
            }
            picked.add(p);
        }
        return picked;
    }

    private static List<IPlayer> pickVolleyball(List<IPlayer> pool, int size) {
        List<IPlayer> picked = new ArrayList<>();
        IPlayer bestSetter = pool.stream()
                .filter(p -> p instanceof VolleyballPlayer vp
                        && vp.getVolleyballPosition() == VolleyballPosition.SETTER)
                .findFirst()
                .orElse(null);
        if (bestSetter != null) {
            picked.add(bestSetter);
        }
        int liberoCount = 0;
        for (IPlayer p : pool) {
            if (picked.size() >= size) {
                break;
            }
            if (p == bestSetter) {
                continue;
            }
            boolean isLibero = p instanceof VolleyballPlayer vp
                    && vp.getVolleyballPosition() == VolleyballPosition.LIBERO;
            if (isLibero && liberoCount >= 1) {
                continue;
            }
            if (isLibero) {
                liberoCount++;
            }
            picked.add(p);
        }
        return picked;
    }
}
