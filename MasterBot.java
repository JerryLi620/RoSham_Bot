// Standard Java packages
import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class MasterBot implements RoShamBot {

    private List<Action> myMoveHistory;
    private List<Action> opponentMoveHistory;
    private Action lastOpponentMove = null;
    private Map<String, Map<String, Integer>> scoreTable;

    private static final List<String> strategies = Arrays.asList("random", "ape", "rotation", "pi", "e", "frequency", "history", "pairHistory", "markov", "iocaine");
    private static final List<String> metastrategies = Arrays.asList("m0", "m1", "m2", "m3", "m4");

    private static final Map<Action, List<Action>> beats = new HashMap<>();
    private static final Map<Action, List<Action>> beatenBy = new HashMap<>();

    static {
        beats.put(Action.ROCK, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beats.put(Action.PAPER, Arrays.asList(Action.ROCK, Action.SPOCK));
        beats.put(Action.SCISSORS, Arrays.asList(Action.PAPER, Action.LIZARD));
        beats.put(Action.LIZARD, Arrays.asList(Action.SPOCK, Action.PAPER));
        beats.put(Action.SPOCK, Arrays.asList(Action.SCISSORS, Action.ROCK));

        beatenBy.put(Action.ROCK, Arrays.asList(Action.PAPER, Action.SPOCK));
        beatenBy.put(Action.PAPER, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beatenBy.put(Action.SCISSORS, Arrays.asList(Action.ROCK, Action.SPOCK));
        beatenBy.put(Action.LIZARD, Arrays.asList(Action.ROCK, Action.SCISSORS));
        beatenBy.put(Action.SPOCK, Arrays.asList(Action.PAPER, Action.LIZARD));
    }

    public MasterBot() {
        myMoveHistory = new ArrayList<>();
        opponentMoveHistory = new ArrayList<>();

        scoreTable = new HashMap<>();
        initializeScoreTable();
    }


    public int getRoundsPlayed() {
        return (myMoveHistory != null) ? myMoveHistory.size() : 0;
    }

    private void updateMyHistory(Action move) {
        myMoveHistory.add(move);
    }

    private void updateOpponentHistory(Action move) {
        opponentMoveHistory.add(move);
    }

    public List<Action> getMyMoveHistory() {
        return new ArrayList<>(myMoveHistory); // Copy to prevent external modification
    }

    public List<Action> getOpponentMoveHistory() {
        return new ArrayList<>(opponentMoveHistory); // Copy to prevent external modification
    }

    private void initializeScoreTable() {
        for (String strategy : strategies) {
            Map<String, Integer> scores = new HashMap<>();
            for (String metastrategy : metastrategies) {
                scores.put(metastrategy, 0);
            }
            scoreTable.put(strategy, scores);
        }
    }

    private void updateScoreTable(String strategy, String metastrategy) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            scores.put(metastrategy, score);
        }
    }

    private int getScore(String strategy, String metastrategy) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            return scores.getOrDefault(metastrategy, 0);
        }
        return 0;
    }


    private Action m0(Action move) { 
        // Beat opponents predicted move
        return beatenBy.get(move).get(0);
    }

    private Action m1(Action move) { 
        // Beat m0's counter-strategy
        Action strategy = m0(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        // Check if there's a move that beats both target2 and target3
        if (beatOppResponse1.containsAll(beatOppResponse2)) {
            return beatOppResponse1.get(0); // Return the first move that beats both
        }

        // If no common move is found, return the original move
        return move;
    }

    private Action m2(Action move) { 
        // Beat m0's counter-strategy
        Action strategy = m1(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        // Check if there's a move that beats both target2 and target3
        if (beatOppResponse1.containsAll(beatOppResponse2)) {
            return beatOppResponse1.get(0); // Return the first move that beats both
        }

        // If no common move is found, return the original move
        return move;
    }

    private Action m3(Action move) { 
        // Beat m0's counter-strategy
        Action strategy = m2(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        // Check if there's a move that beats both target2 and target3
        if (beatOppResponse1.containsAll(beatOppResponse2)) {
            return beatOppResponse1.get(0); // Return the first move that beats both
        }

        // If no common move is found, return the original move
        return move;
    }

    private Action m4(Action move) { 
        // Beat m0's counter-strategy
        Action strategy = m3(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        // Check if there's a move that beats both target2 and target3
        if (beatOppResponse1.containsAll(beatOppResponse2)) {
            return beatOppResponse1.get(0); // Return the first move that beats both
        }

        // If no common move is found, return the original move
        return move;
    }


    private Action randomAction() {
        Random random = new Random();
        int index = random.nextInt(Action.values().length);
        return Action.values()[index];
    }

    private Action apePattern() {
        Action nextMove = myMoveHistory.getLast();
        return nextMove;
    }

    private Action rotationPattern() {
        Action[] allActions = Action.values();
        int currentIndex = Arrays.asList(allActions).indexOf(this.lastOpponentMove);
        int nextIndex = (currentIndex + 1) % allActions.length;
        Action nextMove = allActions[nextIndex];

        return nextMove;
    }

    private Action pi() {
        BigDecimal pi = new BigDecimal(Math.PI).setScale(this.getRoundsPlayed() + 1, RoundingMode.DOWN);
        int piDigit = pi.remainder(BigDecimal.ONE).movePointRight(pi.scale()).intValue();

        // Use the modulus of the pi digit to choose from actions
        Action[] allActions = Action.values();
        int actionIndex = piDigit % allActions.length;

        return allActions[actionIndex];
    }

    public Action e() {
        // Calculate the digit of 'e' corresponding to the current move number
        BigDecimal e = new BigDecimal(Math.E, MathContext.DECIMAL128).setScale(this.getRoundsPlayed() + 1, RoundingMode.DOWN);
        int eDigit = e.remainder(BigDecimal.ONE).movePointRight(e.scale()).intValue();

        // Use the modulus of the 'e' digit to choose from actions
        Action[] allActions = Action.values();
        int actionIndex = eDigit % allActions.length;

        return allActions[actionIndex];
    }

    private Action frequencyCounter() {
        // Check if there are previous moves in the opponent's history
        if (!opponentMoveHistory.isEmpty()) {
            Map<Action, Integer> moveCounts = new HashMap<>();
    
            // Count the frequency of each move in the opponent's history
            for (Action opponentMove : opponentMoveHistory) {
                moveCounts.put(opponentMove, moveCounts.getOrDefault(opponentMove, 0) + 1);
            }
    
            // Find the moves with the highest frequency
            List<Action> mostFrequentMoves = new ArrayList<>();
            int maxCount = 0;
            for (Map.Entry<Action, Integer> entry : moveCounts.entrySet()) {
                int count = entry.getValue();
                if (count > maxCount) {
                    mostFrequentMoves.clear();
                    mostFrequentMoves.add(entry.getKey());
                    maxCount = count;
                } else if (count == maxCount) {
                    mostFrequentMoves.add(entry.getKey());
                }
            }
    
            // Break ties randomly
            if (!mostFrequentMoves.isEmpty()) {
                Random random = new Random();
                return mostFrequentMoves.get(random.nextInt(mostFrequentMoves.size()));
            }
        }
        // If there's no history, return a random move
        return randomAction();
    }

    private Action historyMatching() {
        int[] sequenceLengths = {10,9,8,7,6,5,4,3};
    
        for (int length : sequenceLengths) {
            if (opponentMoveHistory.size()-length >= length) {
                List<Action> lastSequence = opponentMoveHistory.subList(opponentMoveHistory.size() - length, opponentMoveHistory.size());
                Map<List<Action>, Map<Action, Integer>> sequenceCounts = new HashMap<>();
                
                // Iterate over all possible sequences of the specified length in the opponent's move history
                for (int i = 0; i <= opponentMoveHistory.size() - length; i++) {
                    List<Action> sequence = opponentMoveHistory.subList(i, i + length);
                    
                    if (sequence.equals(lastSequence)) {
                        Action nextMove = (i + length < opponentMoveHistory.size()) ? opponentMoveHistory.get(i + length) : null;
    
                        // Update the count for the next move following the current sequence
                        sequenceCounts.computeIfAbsent(sequence, k -> new HashMap<>());
                        Map<Action, Integer> nextMoveCounts = sequenceCounts.get(sequence);
                        nextMoveCounts.put(nextMove, nextMoveCounts.getOrDefault(nextMove, 0) + 1);
                    }
                }
    
                // Find the most common next move for the last sequence
                Map<Action, Integer> mostCommonNextMoveCounts = new HashMap<>();
                for (Map<Action, Integer> counts : sequenceCounts.values()) {
                    for (Map.Entry<Action, Integer> entry : counts.entrySet()) {
                        Action nextMove = entry.getKey();
                        int count = entry.getValue();
                        mostCommonNextMoveCounts.put(nextMove, mostCommonNextMoveCounts.getOrDefault(nextMove, 0) + count);
                    }
                }
    
                // Find the most common next move
                Action mostCommonNextMove = null;
                int maxCount = 0;
                for (Map.Entry<Action, Integer> entry : mostCommonNextMoveCounts.entrySet()) {
                    int count = entry.getValue();
                    if (count > maxCount) {
                        mostCommonNextMove = entry.getKey();
                        maxCount = count;
                    }
                }
    
                if (mostCommonNextMove != null) {
                    return mostCommonNextMove;
                }
            }
        }
    
        // If no history match is found, default to random move
        return randomAction();
    }

    private Action pairHistory() {  
        return  Action.ROCK; 
    }

    private Action markovChain() {  
        return  null; 
    }

    private Action iocainePowder() {  
        return  null; 
    }

    private Action findBestMove() {

        if (this.getRoundsPlayed() < 5) {
            return randomAction();
        }
        else {
            return null;
        }
        
    }


    @Override
    public Action getNextMove(Action lastOpponentMove) {
 
        this.lastOpponentMove = lastOpponentMove;
        if (this.getRoundsPlayed() != 0) {
            updateOpponentHistory(lastOpponentMove);
        }
        
        // this.updateScoreTable();

        Action nextMove = findBestMove();
        updateMyHistory(nextMove);

        return nextMove;
    }
}
