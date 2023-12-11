package com.cinemamod.bukkit.theater;

import com.cinemamod.bukkit.CinemaModPlugin;
import com.cinemamod.bukkit.theater.screen.Screen;

public class PermsTheater extends Theater {
    public PermsTheater(CinemaModPlugin cinemaModPlugin, String id, String name, boolean hidden, Screen screen, TheaterRegion theaterRegion) {
        super(cinemaModPlugin, id, name, hidden, screen, theaterRegion);
    }
}
