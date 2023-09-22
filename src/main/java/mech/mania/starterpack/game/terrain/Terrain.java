package mech.mania.starterpack.game.terrain;

import mech.mania.starterpack.game.util.Position;

public record Terrain(String id, Position position, int health, boolean canAttackThrough) {}