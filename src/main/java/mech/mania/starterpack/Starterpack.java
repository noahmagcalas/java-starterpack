package mech.mania.starterpack;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mech.mania.starterpack.game.GameState;
import mech.mania.starterpack.game.character.MoveAction;
import mech.mania.starterpack.game.character.action.AbilityAction;
import mech.mania.starterpack.game.character.action.AttackAction;
import mech.mania.starterpack.game.character.action.CharacterClassType;
import mech.mania.starterpack.network.Client;
import mech.mania.starterpack.network.ReceivedMessage;
import mech.mania.starterpack.strategy.Strategy;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static mech.mania.starterpack.strategy.ChooseStrategy.chooseStrategy;

public class Starterpack {
    private static final String rawDebugEnv = System.getenv("DEBUG");
    private static final boolean DEBUG = rawDebugEnv != null && (rawDebugEnv.equals("1") || rawDebugEnv.equalsIgnoreCase("true"));

    private static final String MAIN_USAGE = """
            Usage: java -jar starterpack.jar [command]

            Command can be one of:
            run - runs your bot against an opponent
            serve - serves your bot on a port""";
    private static final String RUN_USAGE = """
            Usage: java -jar starterpack.jar run [opponent]

            Opponent can be one of: self, humanComputer, zombieComputer""";
    private static final String SERVE_USAGE = """
            Usage: java -jar starterpack.jar serve [port]

            Port is the port to serve on""";
    private enum RunOpponent {
        SELF("self"),
        HUMAN_COMPUTER("humanComputer"),
        ZOMBIE_COMPUTER("zombieComputer");

        private final String value;

        RunOpponent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static void run(RunOpponent opponent) {
        System.out.printf("Running against %s...\n", opponent);
        throw new RuntimeException("Not yet implemented");
    }

    private static void serve(int port) throws IOException {
        System.out.printf("Connecting to server on port %d...\n", port);

        Client client = new Client(port);

        client.connect();

        System.out.println("Connected to server on port " + port);

        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            String rawReceived = client.read();

            if (rawReceived != null && !rawReceived.isEmpty()) {
                try {
                    ReceivedMessage receivedMessage = objectMapper.readValue(rawReceived, ReceivedMessage.class);
                    boolean isZombie = receivedMessage.isZombie();
                    String phase = receivedMessage.phase();
                    ObjectNode message = receivedMessage.message();
                    int turn = message.get("turn").asInt();

                    GameState gameState = null;

                    if (!phase.equals("CHOOSE_CLASSES") && !phase.equals("FINISH")) {
                        gameState = objectMapper.treeToValue(message.get(""), GameState.class);
                    }

                    Strategy strategy = null;
                    if (!phase.equals("FINISH")) {
                        if (DEBUG) {
                            System.out.printf("[TURN %d]: Getting your bot's response to %s phase...\n", turn, phase);
                        }
                        strategy = chooseStrategy(isZombie);
                    }

                    if (phase.equals("CHOOSE_CLASSES")) {
                        List<CharacterClassType> possibleClasses = objectMapper.readValue(
                                objectMapper.treeAsTokens(message.get("choices")),
                                new TypeReference<>() {}
                        );
                        int numToPick = message.get("numToPick").asInt();
                        int maxPerSameClass = message.get("maxPerSameClass").asInt();

                        Map<CharacterClassType, Integer> output = strategy.decideCharacterClasses(possibleClasses, numToPick, maxPerSameClass);

                        if (output == null) {
                            throw new RuntimeException("Your decideCharacterClasses strategy returned nothing (null)!");
                        }

                        String response = objectMapper.writeValueAsString(output);

                        client.write(response);
                    } else if (phase.equals("MOVE")) {
                        Map<String, List<MoveAction>> possibleMoves = objectMapper.readValue(
                                objectMapper.treeAsTokens(message.get("possibleMoves")),
                                new TypeReference<>() {}
                        );

                        List<MoveAction> output = strategy.decideMoves(possibleMoves, gameState);

                        if (output == null) {
                            throw new RuntimeException("Your decideMoves strategy returned nothing (null)!");
                        }

                        String response = objectMapper.writeValueAsString(output);

                        client.write(response);
                    } else if ("ATTACK".equals(phase)) {
                        Map<String, List<AttackAction>> possibleAttacks = objectMapper.readValue(
                                objectMapper.treeAsTokens(message.get("possibleAttacks")),
                                new TypeReference<>() {}
                        );
                        List<AttackAction> output = strategy.decideAttacks(possibleAttacks, gameState);

                        if (output == null) {
                            throw new RuntimeException("Your decideAttacks strategy returned nothing (null)!");
                        }

                        String response = objectMapper.writeValueAsString(output);

                        client.write(response);
                    } else if (phase.equals("ABILITY")) {
                        Map<String, List<AbilityAction>> possibleAbilities = objectMapper.readValue(
                                objectMapper.treeAsTokens(message.get("possibleAbilities")),
                                new TypeReference<>() {}
                        );

                        List<AbilityAction> output = strategy.decideAbilities(possibleAbilities, gameState);

                        if (output == null) {
                            throw new RuntimeException("Your decideAbilities strategy returned nothing (null)!");
                        }

                        String response = objectMapper.writeValueAsString(output);

                        client.write(response);
                    } else if ("FINISH".equals(phase)) {
                        int humansScore = message.get("scores").get("humans").asInt();
                        int zombiesScore = message.get("scores").get("zombies").asInt();
                        int humansLeft = message.get("stats").get("humansLeft").asInt();
                        int zombiesLeft = message.get("stats").get("zombiesLeft").asInt();
                        int gameTurn = message.get("stats").get("turns").asInt();
                        List<String> errors = objectMapper.convertValue(
                                message.get("errors").get(isZombie ? "zombieErrors" : "humanErrors"), new TypeReference<>() {}
                        );

                        String formattedErrorsMessage = (errors != null && !errors.isEmpty())
                                ? "Your bot had " + errors.size() + " errors:\n" + String.join("\n", errors)
                                : "Your bot had no errors.";

                        System.out.println("\n" + formattedErrorsMessage + "\n\n"
                                + "Finished game on turn " + gameTurn + " with " + humansLeft + " humans and " + zombiesLeft + " zombies.\n"
                                + "Score: " + humansScore + "-" + zombiesScore + " (H-Z). You were the " + (isZombie ? "zombies" : "humans") + ".");
                        break;
                    } else {
                        throw new RuntimeException("Unknown phase type " + phase);
                    }

                    if (DEBUG) {
                        System.out.printf("[TURN %d]: Sent response to %s phase to server!\n", turn, phase);
                    }
                } catch (Exception e) {
                    System.err.println("Something went wrong running your bot: " + e.getMessage());
                    e.printStackTrace(System.err);
                    client.write("null");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println(MAIN_USAGE);
            System.exit(1);
        }

        String command = args[0];

        if (command.equals("run")) {
            if (args.length != 2) {
                System.err.println(RUN_USAGE);
                System.exit(1);
            }

            String sOpponent = args[1];

            RunOpponent opponent = null;

            for (RunOpponent possible : RunOpponent.values()) {
                if (possible.getValue().equals(sOpponent)) {
                    opponent = possible;
                    break;
                }
            }

            if (opponent == null) {
                System.err.printf("Invalid opponent %s\n", opponent);
                System.err.println(RUN_USAGE);
                System.exit(1);
            }

            run(opponent);
        } else if (command.equals("serve")) {
            if (args.length != 2) {
                System.err.println(SERVE_USAGE);
                System.exit(1);
            }

            Integer port = null;

            try {
                port = Integer.decode(args[1]);
            } catch (Exception e) {
                // Do nothing
            }

            if (port == null) {
                System.err.printf("Invalid port %d\n", port);
                System.err.println(SERVE_USAGE);
                System.exit(1);
            }

            serve(port);
        } else {
            System.err.printf("Unknown command %s\n", command);
            System.err.println(MAIN_USAGE);
            System.exit(1);
        }

//        Option portOption = Option.builder()
//                .longOpt("port")
//                .desc("Port for the 'serve' command")
//                .hasArg()
//                .argName("port")
//                .type(Integer.class)
//                .build();
//
//        options.addOption(commandOption);
//        options.addOption(opponentOption);
//        options.addOption(portOption);
//
//        CommandLineParser parser = new DefaultParser();
//
//        try {
//            CommandLine cmd = parser.parse(options, args);
//
//            String command = cmd.getOptionValue("command");
//
//            if (command.equals("run")) {
//                String opponent = cmd.getOptionValue("opponent");
//                run(RunOpponent.valueOf(opponent.toUpperCase()));
//            } else if (command.equals("serve")) {
//                int port = Integer.parseInt(cmd.getOptionValue("port"));
//                serve(port);
//            } else {
//                System.err.println("Unknown command: " + command);
//                printHelp(options);
//            }
//        } catch (ParseException e) {
//            System.err.println("Error parsing command-line arguments: " + e.getMessage());
//            printHelp(options);
//        }
    }
}
