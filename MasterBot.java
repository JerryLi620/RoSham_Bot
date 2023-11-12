import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;


public class MasterBot implements RoShamBot {

    //Instance variables to keep track of
    private List<Action> myMoveHistory;
    private List<Action> opponentMoveHistory;
    private Action lastOpponentMove = null;
    public Map<String, Map<String, Action>> recommendedMoves;
    public Map<String, Map<String, Integer>> scoreTable; //TODO: Change to private later

    //Class variables
    private static final List<String> strategies = Arrays.asList("random", "ape", "rotation", "frequency", "history", "pairHistory"); //match with implemented strategy methods
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

        recommendedMoves = new HashMap<>();

        scoreTable = new HashMap<>();
        initializeScoreTable();
    }


    //General bot methods begin from here
    private int getRoundsPlayed() {
        return (myMoveHistory != null) ? myMoveHistory.size() : 0;
    }

    private void rememberRecommendedMove(String strategy, String metastrategy, Action recommendedMove) {
        recommendedMoves.computeIfAbsent(strategy, k -> new HashMap<>()).put(metastrategy, recommendedMove);
    }

    public Action getRecommendedMove(String strategy, String metastrategy) {
        return recommendedMoves.getOrDefault(strategy, Collections.emptyMap()).get(metastrategy);
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
        return new ArrayList<>(opponentMoveHistory); 
    }

    private Action getBotMove(String strategy, String metastrategy) {
        Action oppMove;
        switch (strategy) {
            case "random":
                oppMove = randomAction();
                break;
            case "ape":
                oppMove = apePattern();
                break;
            case "rotation":
                oppMove = rotationPattern();
                break;
            case "pi":
                oppMove = pi();
                break;
            case "e":
                oppMove = e();
                break;
            case "frequency":
                oppMove = frequencyCounter();
                break;
            case "history":
                oppMove = historyMatching();
                break;
            case "pairHistory":
                oppMove = pairHistory();
                break;
            case "markov":
                oppMove = markovChain();
                break;
            default:
                // Handle other metastrategies if needed
                oppMove = randomAction();
        }

        switch (metastrategy) {
            case "m0":
                return m0(oppMove);
            case "m1":
                return m1(oppMove);
            case "m2":
                return m2(oppMove);
            case "m3":
                return m3(oppMove);
            case "m4":
                return m4(oppMove);
            default:
                // Handle other metastrategies if needed
                return randomAction();
        }
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

    private void addScore(String strategy, String metastrategy, int points) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            int currentScore = scores.getOrDefault(metastrategy, 0);
            scores.put(metastrategy, currentScore + points);
        }
    }

    private void updateScoreTable() { //TODO: change scoring system to introduce locality
        for (String strategy : strategies) {
            for (String metastrategy : metastrategies) {
                Action botMove = getRecommendedMove(strategy, metastrategy);
    
                if (beats.get(botMove).contains(lastOpponentMove)) {
                    // Bot wins against opponent's last move
                    addScore(strategy, metastrategy, 2);
                } else if (beatenBy.get(botMove).contains(lastOpponentMove)) {
                    // Bot loses against opponent's last move
                    addScore(strategy, metastrategy, -2);
                } else {
                    // It's a draw
                    addScore(strategy, metastrategy, -1);
                }
            }
        }
    }

    private int getScore(String strategy, String metastrategy) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            return scores.getOrDefault(metastrategy, 0);
        }
        return 0;
    }


    //Metastrategy methods begin from here
    private Action findCommonMove(List<Action> list1, List<Action> list2) {
        for (Action commonAction : list1) {
            if (list2.contains(commonAction)) {
                return commonAction;
            }
        }
        return null; // No common move found
    }

    private Action m0(Action move) { 
        // Beat opponents predicted move naively
        return beatenBy.get(move).get(0); //TODO: Break tie strategically
    }

    private Action m1(Action move) { 
        // Beat m0's counter-strategy
        Action strategy = m0(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);
    
        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);
    
        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);
    
        // If a common move is found, return it; otherwise, return the original move
        return (commonMove != null) ? commonMove : move;
    }
    

    private Action m2(Action move) { 
        Action strategy = m1(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);
    
        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);
    
        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);
    
        return (commonMove != null) ? commonMove : move;
    }

    private Action m3(Action move) { 
        Action strategy = m2(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);
    
        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);
    
        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);
    
        return (commonMove != null) ? commonMove : move;
    }

    private Action m4(Action move) { 
        Action strategy = m3(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);
    
        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);
    
        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);
    
        return (commonMove != null) ? commonMove : move;
    }


    //Strategy methods begin from here
    private Action randomAction() {
        Random random = new Random();
        int index = random.nextInt(Action.values().length);
        return Action.values()[index];
    }

    private Action apePattern() {
        if (!myMoveHistory.isEmpty()) {
            return myMoveHistory.get(myMoveHistory.size() - 1);
        } else {
            // Handle the case when move history is empty
            return randomAction(); 
        }
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

    private Action historyMatching() { //TODO: Revisit design to improve performance
        int[] sequenceLengths = {10,9,8,7,6,5,4,3}; //Check longest sequence downwards
        Map<List<Action>, Map<Action, Integer>> sequenceCounts = new HashMap<>();

        for (int length : sequenceLengths) {
            if (opponentMoveHistory.size() >= length) {
                List<Action> lastSequence = opponentMoveHistory.subList(opponentMoveHistory.size() - length, opponentMoveHistory.size());
                
                // Iterate over all possible sequences of the specified length in the opponent's move history
                for (int i = 0; i < opponentMoveHistory.size() - length; i++) {
                    List<Action> sequence = opponentMoveHistory.subList(i, i + length);
                    
                    if (sequence.equals(lastSequence)) {
                        Action nextMove = opponentMoveHistory.get(i + length);
    
                        // Update the count for the next move following the current sequence
                        sequenceCounts.computeIfAbsent(sequence, k -> new HashMap<>());
                        Map<Action, Integer> nextMoveCounts = sequenceCounts.get(sequence);
                        nextMoveCounts.put(nextMove, nextMoveCounts.getOrDefault(nextMove, 0) + 1);
                    }
                }
            }
        }

        // Naively record total number of each move occurances corresponding to some match
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
    
        // If no history match is found, default to random move
        return randomAction();
    }

    private Action pairHistory() { //TODO: Revisit design to improve performance
        int sequenceLength = 3; // Adjust the sequence length
    
        if (opponentMoveHistory.size() >= sequenceLength && myMoveHistory.size() >= sequenceLength) {
            List<Action> lastOpponentSequence = opponentMoveHistory.subList(opponentMoveHistory.size() - sequenceLength, opponentMoveHistory.size());
            List<Action> lastMySequence = myMoveHistory.subList(myMoveHistory.size() - sequenceLength, myMoveHistory.size());
    
            Map<Action, Integer> nextMoveCounts = new HashMap<>();
    
            for (int i = 0; i <= opponentMoveHistory.size() - sequenceLength; i++) {
                List<Action> opponentSequence = opponentMoveHistory.subList(i, i + sequenceLength);
                List<Action> mySequence = myMoveHistory.subList(i, i + sequenceLength);
    
                if (opponentSequence.equals(lastOpponentSequence) && mySequence.equals(lastMySequence)) {
                    // If the sequences match, update the count for the next move the opponent used last time
                    int nextMoveIndex = i + sequenceLength;
                    if (nextMoveIndex < opponentMoveHistory.size()) {
                        Action nextMove = opponentMoveHistory.get(nextMoveIndex);
                        nextMoveCounts.put(nextMove, nextMoveCounts.getOrDefault(nextMove, 0) + 1);
                    }
                }
            }
    
            // Find the most common next move
            List<Action> mostCommonNextMoves = new ArrayList<>();
            int maxCount = 0;
    
            for (Map.Entry<Action, Integer> entry : nextMoveCounts.entrySet()) {
                int count = entry.getValue();
                if (count > maxCount) {
                    mostCommonNextMoves.clear();
                    mostCommonNextMoves.add(entry.getKey());
                    maxCount = count;
                } else if (count == maxCount) {
                    mostCommonNextMoves.add(entry.getKey());
                }
            }
    
            // Break ties randomly
            if (!mostCommonNextMoves.isEmpty()) {
                Random random = new Random();
                return mostCommonNextMoves.get(random.nextInt(mostCommonNextMoves.size()));
            }
        }
        // If no pair history match is found, default to random move
        return randomAction();
    }

    private Action markovChain() {  //TODO: Implement this method
        return  randomAction(); 
    }


    //main function methods begin from here
    private Action findBestMove() {
        for (String strategy : strategies) {
            for (String metastrategy : metastrategies) {
                //Store the moves recommended by each strategy-meta pair this round
                Action botMove = getBotMove(strategy, metastrategy);
                rememberRecommendedMove(strategy, metastrategy, botMove);
            }
        }

        //Begin by playing random moves
        if (getRoundsPlayed() < 5) {
            return randomAction();
        }

        String bestStrategy = null;
        String bestMeta = null;
        int maxScore = Integer.MIN_VALUE;

        for (String strategy : strategies) {
            for (String metastrategy : metastrategies) {
                int score = getScore(strategy, metastrategy);

                if (score > maxScore) {
                    // Update if a higher score is found
                    maxScore = score;
                    bestStrategy = strategy;
                    bestMeta = metastrategy;
                } else if (score == maxScore) { //TODO: Consider better implementation
                    // Break ties randomly
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        bestStrategy = strategy;
                        bestMeta = metastrategy;
                    }
                }
            }
        }
        // Choose the move based on the best strategy and metastrategy
        if (bestStrategy != null && bestMeta != null) {
            return getBotMove(bestStrategy, bestMeta);
        } else {
            // If no strategy or metastrategy is found, return a random move
            return randomAction();
        }
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {
 
        this.lastOpponentMove = lastOpponentMove;
        if (getRoundsPlayed() != 0) {
            updateOpponentHistory(lastOpponentMove);
            updateScoreTable();
        }
        
        Action nextMove = findBestMove();
        updateMyHistory(nextMove);

        return nextMove;
    }
}
