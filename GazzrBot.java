import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GazzrBot implements RoShamBot {

    private static final int DEPTH = 3;
    private final List<Action> moves = Arrays.asList(Action.ROCK, Action.LIZARD, Action.SPOCK, Action.SCISSORS, Action.PAPER);
    private final Map<String, float[]> oppHistDb = new HashMap<>();
    private final Map<String, float[]> myHistDb = new HashMap<>();
    private final Random random = new Random();
    private String myHistory = "";
    private String oppHistory = "";

    @Override
    public Action getNextMove(Action lastOpponentMove) {
        if (lastOpponentMove != null) {
            oppHistory += lastOpponentMove.name().charAt(0);
            myHistory += (myHistory.length() > 0 ? myHistory.charAt(myHistory.length() - 1) : "");
        }

        if (oppHistory.length() < DEPTH) {
            return moves.get(random.nextInt(moves.size()));
        }

        updateDB(myHistory, myHistory.charAt(myHistory.length() - 1), myHistDb);
        updateDB(oppHistory, oppHistory.charAt(oppHistory.length() - 1), oppHistDb);

        Integer myPrediction = predict(myHistory, myHistDb);
        Integer oppPrediction = predict(oppHistory, oppHistDb);

        if (myPrediction == null && oppPrediction == null) {
            return moves.get(random.nextInt(moves.size()));
        }

        Action myNextMove = myPrediction == null ? moves.get(random.nextInt(moves.size())) :
                moves.get(myPrediction);
        Action oppNextMove = oppPrediction == null ? moves.get(random.nextInt(moves.size())) :
                moves.get(oppPrediction);

        return decideMove(myNextMove, oppNextMove);
    }

    private void updateDB(String history, char nextMove, Map<String, float[]> database) {
        if (history.length() < DEPTH) return;
        String lastMoves = history.substring(history.length() - DEPTH);
        int index = moves.indexOf(Action.valueOf(String.valueOf(nextMove)));
        float[] distr = database.getOrDefault(lastMoves, new float[moves.size()]);
        distr[index] += 1.0f;
        database.put(lastMoves, distr);
    }

    private Integer predict(String history, Map<String, float[]> database) {
        if (history.length() < DEPTH) return null;
        String lastMoves = history.substring(history.length() - DEPTH);
        float[] distr = database.get(lastMoves);
        return distr != null ? predictFromArray(distr) : null;
    }

    private Integer predictFromArray(final float[] distr) {
        float total = 0f;
        for (float v : distr) total += v;
        float target = random.nextFloat() * total;
        total = 0f;
        for (int i = 0; i < distr.length; i++) {
            total += distr[i];
            if (total >= target) {
                return i;
            }
        }
        return distr.length - 1;
    }

    private Action decideMove(Action myMove, Action oppMove) {
        // Implement the logic to decide the next move based on the predictions
        // This could involve choosing a move that beats the predicted opponent's move
        // or any other strategy you deem appropriate.
        // For now, let's return a random move.
        return moves.get(random.nextInt(moves.size())); // Placeholder logic
    }
}
