package mech.mania.starterpack.strategy;

import mech.mania.starterpack.game.GameState;
import mech.mania.starterpack.game.character.MoveAction;
import mech.mania.starterpack.game.character.action.AbilityAction;
import mech.mania.starterpack.game.character.action.AttackAction;
import mech.mania.starterpack.game.character.action.CharacterClassType;

import java.util.*;

import mech.mania.starterpack.game.util.Position;
import mech.mania.starterpack.game.character.Character;

/**
 * very swag human strategy
 */
public class SwagHumanStrategy extends Strategy {
    private final Random random = new Random();

    @Override
    public Map<CharacterClassType, Integer> decideCharacterClasses(
            List<CharacterClassType> possibleClasses,
            int numToPick,
            int maxPerSameClass
    )
    {
        Map<CharacterClassType, Integer> choices = new HashMap<>();
        System.out.println(maxPerSameClass);
        int normals = 0,
                marksmen = maxPerSameClass,
                tracers = maxPerSameClass,
                builders = 1,
                medics = maxPerSameClass,
                demos = 0;

        choices.put(possibleClasses.get(0), normals);
        choices.put(possibleClasses.get(1), marksmen);
        choices.put(possibleClasses.get(2), tracers);
        choices.put(possibleClasses.get(3), medics);
        choices.put(possibleClasses.get(4), builders);
        choices.put(possibleClasses.get(5), demos);

        return choices;
    }

    private int manhattanDistance(Position a, Position b) {
        return (Math.abs(a.x() - b.x())
                + Math.abs(a.y() - b.y()));
    }

    private int chebyshevDistance(Position a, Position b) {
        return Math.max(
                Math.abs(a.x() - b.x()),
                Math.abs(a.y() - b.y()));
    }

    private boolean zombieInRange(Character human, Character zombie) {
        Position humanPos = human.position();
        Position zombiePos = zombie.position();

        int walkingDistance = manhattanDistance(humanPos, zombiePos);
        int attackingDistance = chebyshevDistance(humanPos, zombiePos);
        int total = walkingDistance + attackingDistance;

        System.out.printf("human %s @ (%d, %d)", human.id(), humanPos.x(), humanPos.y());
        System.out.printf("zombie %s @ (%d, %d)", zombie.id(), zombiePos.x(), zombiePos.y());
        System.out.println("distance: " + total);

        return (total <= 6);
    }

    private List<Character> getZombies(GameState gameState) {
        List<Character> zombies = new ArrayList<>();
        for (Character character : gameState.characters().values()) {
            if (character.isZombie()) {
                zombies.add(character);
                System.out.println(
                        "character at " + character.position().x() + " " + character.position().y() + " is zombie");
            }
        }
        return zombies;
    }

    @Override
    public List<MoveAction> decideMoves(
            Map<String, List<MoveAction>> possibleMoves,
            GameState gameState
    )
    {
        List<MoveAction> choices = new ArrayList<>();

        for (Character character : gameState.characters().values())
        {
            String characterId = character.id();
            System.out.println(character.id());

            Character currentCharacter = gameState.characters().get(characterId);
            Position currentPosition = currentCharacter.position();

            MoveAction goal = new MoveAction(characterId, new Position(currentPosition.x(), currentPosition.y()));
            
            if (possibleMoves.values().contains(goal))
                choices.add(goal);
        }

        return choices;
    }

    @Override
    public List<AttackAction> decideAttacks(
            Map<String, List<AttackAction>> possibleAttacks,
            GameState gameState) {
        List<AttackAction> choices = new ArrayList<>();

        for (Map.Entry<String, List<AttackAction>> entry : possibleAttacks.entrySet()) {
            String characterId = entry.getKey();
            List<AttackAction> attacks = entry.getValue();

            // NOTE: You will have to handle the case where there is no attack to be made,
            // such as when stunned
            if (!attacks.isEmpty()) {
                choices.add(attacks.get(random.nextInt(attacks.size())));
            }
        }

        return choices;
    }

    @Override
    public List<AbilityAction> decideAbilities(
            Map<String, List<AbilityAction>> possibleAbilities,
            GameState gameState) {
        List<AbilityAction> choices = new ArrayList<>();

        for (Map.Entry<String, List<AbilityAction>> entry : possibleAbilities.entrySet()) {
            String characterId = entry.getKey();
            List<AbilityAction> abilities = entry.getValue();

            // NOTE: You will have to handle the case where there is no ability to be made,
            // such as when stunned
            if (!abilities.isEmpty()) {
                choices.add(abilities.get(random.nextInt(abilities.size())));
            }
        }

        return choices;
    }
}