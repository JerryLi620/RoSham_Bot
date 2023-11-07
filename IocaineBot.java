import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class IocaineBot implements RoShamBot {
    int MEMORY_SIZE = 30;
    HashMap<String, Integer> strats = new HashMap<String, Integer>();
    HashMap<Action, List<Action>> beaten_by = new HashMap<Action, List<Action>>();
    HashMap<Action, List<Action>> beats = new HashMap<Action, List<Action>>();
    ArrayList<Action> opp_memory;
    ArrayList<Action> own_memory;
    int count = 0;

    public IocaineBot() {
        beats.put(Action.ROCK, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beats.put(Action.PAPER, Arrays.asList(Action.ROCK, Action.SPOCK));
        beats.put(Action.SCISSORS, Arrays.asList(Action.PAPER, Action.LIZARD));
        beats.put(Action.LIZARD, Arrays.asList(Action.SPOCK, Action.PAPER));
        beats.put(Action.SPOCK, Arrays.asList(Action.SCISSORS, Action.ROCK));

        beaten_by.put(Action.ROCK, Arrays.asList(Action.PAPER, Action.SPOCK));
        beaten_by.put(Action.PAPER, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beaten_by.put(Action.SCISSORS, Arrays.asList(Action.ROCK, Action.SPOCK));
        beaten_by.put(Action.LIZARD, Arrays.asList(Action.ROCK, Action.SCISSORS));
        beaten_by.put(Action.SPOCK, Arrays.asList(Action.PAPER, Action.LIZARD));

        strats.put("p0", 0);
        strats.put("p1", 0);
        strats.put("p2", 0);
        strats.put("p3", 0);
        strats.put("p4", 0);
        strats.put("p4", 0);
        strats.put("p_prime_0", 0);
        strats.put("p_prime_1", 0);
        strats.put("p_prime_2", 0);
        strats.put("p_prime_3", 0);
        strats.put("p_prime_4", 0);

        opp_memory = new ArrayList<Action>();
        own_memory = new ArrayList<Action>();

        // Init with ROCK
        for (int i = 0; i < MEMORY_SIZE; i++) {
            opp_memory.add(Action.ROCK);
            own_memory.add(Action.ROCK);
        }
    }

    Action p0(Action a) { // Beat opponents move
        return beaten_by.get(a).get((int) Math.random() * beaten_by.size());
    }

    Action p1(Action a) { // Beat p0
        return beaten_by.get(p0(a)).get((int) Math.random() * beaten_by.size());
    }

    Action p2(Action a) { // Beat p1
        return beats.get(p1(a)).get((int) Math.random() * beaten_by.size());
    }

    Action p3(Action a) { // Beat p2
        return beats.get(p2(a)).get((int) Math.random() * beaten_by.size());
    }

    Action p4(Action a) { // Beat p3
        return beats.get(p3(a)).get((int) Math.random() * beaten_by.size());
    }

    // Implementing the P' strategies by considering the counter to what P
    // strategies would play
    Action p_prime_0(Action a) {
        Action p0Move = p0(a);
        List<Action> counters = beaten_by.get(p0Move);
        return counters.get((int) (Math.random() * counters.size()));
    }

    Action p_prime_1(Action a) {
        Action p1Move = p1(a);
        List<Action> counters = beaten_by.get(p1Move);
        return counters.get((int) (Math.random() * counters.size()));
    }

    Action p_prime_2(Action a) {
        Action p2Move = p2(a);
        List<Action> counters = beaten_by.get(p2Move);
        return counters.get((int) (Math.random() * counters.size()));
    }

    Action p_prime_3(Action a) {
        Action p3Move = p3(a);
        List<Action> counters = beaten_by.get(p3Move);
        return counters.get((int) (Math.random() * counters.size()));
    }

    Action p_prime_4(Action a) {
        Action p4Move = p4(a);
        List<Action> counters = beaten_by.get(p4Move);
        return counters.get((int) (Math.random() * counters.size()));
    }

    private void updateStrategyScores(Action lastOpponentMove, Action strategyMove, String strategyName) {
        if (beats.get(strategyMove).contains(lastOpponentMove)) {
            // This strategy would have won
            strats.put(strategyName, strats.get(strategyName) + 2);
        } else if (beaten_by.get(lastOpponentMove).contains(strategyMove)) {
            // This strategy would have lost
            strats.put(strategyName, strats.get(strategyName) - 1);
        } else {
            // Ties also decrease the score
            strats.put(strategyName, strats.get(strategyName) - 1);
        }

    }

    private String selectBestStrategy() {
        int maxScore = Integer.MIN_VALUE;
        List<String> bestStrategies = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : strats.entrySet()) {
            if (entry.getValue() > maxScore) {
                bestStrategies.clear();
                maxScore = entry.getValue();
                bestStrategies.add(entry.getKey());
            } else if (entry.getValue() == maxScore) {
                bestStrategies.add(entry.getKey());
            }
        }

        // In case of a tie, pick a random strategy from the best ones
        return bestStrategies.get((int) (Math.random() * bestStrategies.size()));
    }

    private Action executeStrategy(String strategyName, Action oppMode) {
        switch (strategyName) {
            case "p0":
                return p0(oppMode);
            case "p1":
                return p1(oppMode);
            case "p2":
                return p2(oppMode);
            case "p3":
                return p3(oppMode);
            case "p4":
                return p4(oppMode);
            case "p_prime_0":
                return p_prime_0(oppMode);
            case "p_prime_1":
                return p_prime_1(oppMode);
            case "p_prime_2":
                return p_prime_2(oppMode);
            case "p_prime_3":
                return p_prime_3(oppMode);
            case "p_prime_4":
                return p_prime_4(oppMode);
            default:
                // If it's not one of the known strategies, default to a random move
                return Action.values()[(int) (Math.random() * Action.values().length)];
        }
    }

    public Action getNextMove(Action lastOpponentMove) {
        // Update memories
        if (count < MEMORY_SIZE) {
            opp_memory.add(lastOpponentMove);
            own_memory.add(lastOpponentMove); // Placeholder for own move
        } else {
            opp_memory.set(count % MEMORY_SIZE, lastOpponentMove);
            own_memory.set(count % MEMORY_SIZE, lastOpponentMove); // Placeholder for own move
        }
        count++;

        // Calculate mode for opponent
        Action oppMode = mode(opp_memory);

        // Run strategies
        Action p0Move = p0(oppMode);
        Action p1Move = p1(oppMode);
        Action p2Move = p2(oppMode);
        Action p_prime_0Move = p_prime_0(oppMode);
        Action p_prime_1Move = p_prime_1(oppMode);
        Action p_prime_2Move = p_prime_2(oppMode);
        Action p_prime_3Move = p_prime_3(oppMode);
        Action p_prime_4Move = p_prime_4(oppMode);

        // Update strategy scores
        updateStrategyScores(lastOpponentMove, p0Move, "p0");
        updateStrategyScores(lastOpponentMove, p1Move, "p1");
        updateStrategyScores(lastOpponentMove, p2Move, "p2");
        updateStrategyScores(lastOpponentMove, p_prime_0Move, "p_prime_0");
        updateStrategyScores(lastOpponentMove, p_prime_1Move, "p_prime_1");
        updateStrategyScores(lastOpponentMove, p_prime_2Move, "p_prime_2");
        updateStrategyScores(lastOpponentMove, p_prime_3Move, "p_prime_3");
        updateStrategyScores(lastOpponentMove, p_prime_4Move, "p_prime_4");

        // Select the best strategy
        String bestStrategy = selectBestStrategy();

        // Execute the best strategy
        Action nextMove = executeStrategy(bestStrategy, oppMode);

        // Record the move you're actually making
        own_memory.set(count % MEMORY_SIZE, nextMove);

        return nextMove;
    }

    public Action mode(ArrayList<Action> array) {
        HashMap<Action, Integer> frequencyMap = new HashMap<>();
        Action mode = null;
        int maxFrequency = 0;

        for (Action action : array) {
            int frequency = frequencyMap.getOrDefault(action, 0) + 1;
            frequencyMap.put(action, frequency);

            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                mode = action;
            }
        }
        return mode;
    }
}