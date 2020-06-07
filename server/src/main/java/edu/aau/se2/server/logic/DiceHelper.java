package edu.aau.se2.server.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.aau.se2.server.data.Player;

public interface DiceHelper {

    /**
     * Generates a random permutation of all Player ids in players list.
     *
     * @param players Players to create permutation for.
     * @return List of random permutation of all Player ids.
     */
    static List<Integer> getRandomTurnOrder(List<Player> players) {
        ArrayList<Integer> startingOrder = new ArrayList<>();
        for (Player p : players) {
            startingOrder.add(p.getUid());
        }
        Collections.shuffle(startingOrder);
        return startingOrder;
    }

    /**
     * Calculates the lost armies for attacker and defender
     * based on the dicing results
     * @param attackerResults   dicing results of the attacker
     * @param defenderResults   dicing results of the defender
     * @return on the index 0 returns the attacker results and on the index 1 returns the defender results
     */
    static int[] getArmiesLost(List<Integer> attackerResults, List<Integer> defenderResults) {
        int[] armiesLost = new int[2];
        attackerResults.sort(Integer::compareTo);
        Collections.sort(attackerResults, Collections.reverseOrder());
        defenderResults.sort(Integer::compareTo);
        Collections.sort(defenderResults, Collections.reverseOrder());

        for (int i = 0; i < Math.min(attackerResults.size(), defenderResults.size()); i++) {
            if (attackerResults.get(i) <= defenderResults.get(i)) {
                armiesLost[0]++;
            } else {
                armiesLost[1]++;
            }
        }return armiesLost;
    }
}
