package mech.mania.starterpack.game.character;

import mech.mania.starterpack.game.character.action.CharacterClassType;
import mech.mania.starterpack.game.util.Position;

public record Character(
        String id,
        Position position,
        boolean isZombie,
        CharacterClassType classType,
        int health,
        boolean isStunned
) {}
