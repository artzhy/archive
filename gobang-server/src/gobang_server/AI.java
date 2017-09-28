package gobang_server;

import java.text.MessageFormat;
import java.util.*;

public class AI {
    private static final int SIZE = 15;
    private static final int GO_BANG = 1000000;
    private static final int IMMEDIATE_WIN = 100000;
    private static final int[][] DIRECTIONS = new int[][]{
            new int[]{-1, 0},
            new int[]{-1, 1},
            new int[]{0, 1},
            new int[]{1, 1},
            new int[]{1, 0},
            new int[]{1, -1},
            new int[]{0, -1},
            new int[]{-1, -1},
    };
    private static final int[][][] ALL_SCAN_LINES;

    static {
        int[][][] scanLines = new int[4][][];
        scanLines[0] = new int[SIZE][SIZE];
        scanLines[1] = new int[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                scanLines[0][row][col] = packMove(row, col); // direction 6-2
                scanLines[1][col][row] = packMove(row, col); // direction 0-4
            }
        }
        scanLines[2] = new int[2 * SIZE - 1][]; // direction 7-3 (5->1)
        for (int delta = -SIZE + 1; delta <= SIZE - 1; delta++) { // equation: col = row + delta
            int index = delta + SIZE - 1;
            if (delta <= 0) {
                scanLines[2][index] = new int[SIZE + delta];
                for (int col = 0; col < SIZE + delta; col++) {
                    scanLines[2][index][col] = packMove(col - delta, col);
                }
            } else {
                scanLines[2][index] = new int[SIZE - delta];
                for (int row = 0; row < SIZE - delta; row++) {
                    scanLines[2][index][row] = packMove(row, row + delta);
                }
            }
        }
        scanLines[3] = new int[2 * SIZE - 1][]; // direction 5-1 (7->3)
        for (int sum = 0; sum < 2 * SIZE - 1; sum++) {
            // index is the same as sum
            if (sum <= SIZE - 1) {
                scanLines[3][sum] = new int[sum + 1];
                for (int col = 0; col <= sum; col++) {
                    scanLines[3][sum][col] = packMove(sum - col, col);
                }
            } else {
                scanLines[3][sum] = new int[2 * SIZE - 1 - sum];
                for (int t = 0; t < 2 * SIZE - 1 - sum; t++) {
                    int row = SIZE - 1 - t;
                    scanLines[3][sum][t] = packMove(row, sum - row);
                }
            }
        }
        ALL_SCAN_LINES = scanLines;
    }

    private static void printBoard(int[] black, int[] white) {
        System.out.println("#################");
        for (int row = 0; row < SIZE; row++) {
            System.out.print("#");
            for (int col = 0; col < SIZE; col++) {
                if ((black[row] & (1 << col)) != 0) {
                    System.out.print('X');
                } else if ((white[row] & (1 << col)) != 0) {
                    System.out.print('O');
                } else {
                    System.out.print(' ');
                }
            }
            System.out.println("#");
        }
        System.out.println("#################");
    }

    private static int caseCount = 0;

    // 注意坐标系的转换
    // pieces的坐标用的是[x, y]
    // black[SIZE]和white[SIZE]用的是[row, col]坐标
    // move = (row << 4) | col
    // 返回值用的是[x, y]坐标
    public static List<Integer> analyze(String userSide, List<List<String>> pieces) {
        System.out.println("====================");
        int[] black = new int[SIZE];
        int[] white = new int[SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String piece = pieces.get(col).get(row);
                if (piece == null) {
                    continue;
                }
                switch (piece) {
                    case "WHITE":
                        togglePiece(white, row, col);
                        break;
                    case "BLACK":
                        togglePiece(black, row, col);
                        break;
                    default:
                        throw new RuntimeException("Unknown piece color - " + piece);
                }
            }
        }

        caseCount = 0; // 统计一下分析了多少种情况
        long startTime = System.nanoTime();
        Evaluation blackEvaluation = getEvaluation(black, white);
        Evaluation whiteEvaluation = getEvaluation(white, black);
        System.out.println(blackEvaluation);
        System.out.println(whiteEvaluation);
        int bestMove = minimax(userSide.equals("BLACK"), black, white, 4, blackEvaluation, whiteEvaluation);

        // todo 以下输出用于调试
        System.out.println("Case Count: " + caseCount);
        System.out.println("Time Elapsed: " + ((System.nanoTime() - startTime) / 1000000) + "ms");
        // 注意坐标转换 [x, y]
        return Arrays.asList(getCol(bestMove), getRow(bestMove));
    }

    private static int minimax(boolean wantMax, int[] black, int[] white, int depth,
                               Evaluation blackEvaluation, Evaluation whiteEvaluation) {
        int[] userPieces = wantMax ? black : white;
        List<Integer> bestMoves = new ArrayList<>();
        int bestExpectation = wantMax ? -IMMEDIATE_WIN : IMMEDIATE_WIN;
        Map<Integer, Evaluation> blackEvaluationMap = new HashMap<>();
        Map<Integer, Evaluation> whiteEvaluationMap = new HashMap<>();
        List<Integer> moves = getMoves(wantMax, black, white, blackEvaluation, whiteEvaluation,
                blackEvaluationMap, whiteEvaluationMap);
        for (int move : moves) {
            int row = getRow(move);
            int col = getCol(move);
            togglePiece(userPieces, row, col);

            Evaluation nextBlackEvaluation = blackEvaluationMap.get(move);
            Evaluation nextWhiteEvaluation = whiteEvaluationMap.get(move);

            int nextBestExpectation = backtrack(!wantMax, black, white, depth - 1,
                    nextBlackEvaluation, nextWhiteEvaluation,
                    -IMMEDIATE_WIN, IMMEDIATE_WIN);

            togglePiece(userPieces, row, col);

            if ((wantMax && nextBestExpectation > bestExpectation)
                    || (!wantMax && nextBestExpectation < bestExpectation)) {
                bestMoves.clear();
                bestMoves.add(move);
                bestExpectation = nextBestExpectation;
            } else if (nextBestExpectation == bestExpectation) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.isEmpty()) {
            return moves.get(0); // 即将要输的情况, 返回当前最佳move
        }
        System.out.println(bestMoves.size() + " moves");
        return bestMoves.get(new Random().nextInt(bestMoves.size()));
    }

    private static int backtrack(boolean wantMax, int[] black, int[] white, int depth,
                                 Evaluation blackEvaluation, Evaluation whiteEvaluation,
                                 int alpha, int beta) {
        int expectation = blackEvaluation.getScore() - whiteEvaluation.getScore();
        // todo 算杀
        // 基本情况: depth为0 或 黑子或白子已经连成5颗
        if (depth == 0 || blackEvaluation.gobang || whiteEvaluation.gobang) {
            caseCount++; // 统计一下分析了多少种情况
            return expectation;
        }

        // 回溯情况
        int[] userPieces = wantMax ? black : white;
        int bestExpectation = wantMax ? alpha : beta;
        Map<Integer, Evaluation> blackEvaluationMap = new HashMap<>();
        Map<Integer, Evaluation> whiteEvaluationMap = new HashMap<>();

        List<Integer> moves = getMoves(wantMax, black, white,
                blackEvaluation, whiteEvaluation,
                blackEvaluationMap, whiteEvaluationMap);
        for (int move : moves) {
            if (wantMax && bestExpectation >= beta || !wantMax && bestExpectation <= alpha) {
                break; // alpha-beta pruning.
            }
            int row = getRow(move);
            int col = getCol(move);
            // 下一个棋子
            togglePiece(userPieces, row, col);

            Evaluation nextBlackEvaluation = blackEvaluationMap.get(move);
            Evaluation nextWhiteEvaluation = whiteEvaluationMap.get(move);

            int nextBestExpectation = wantMax ?
                    backtrack(false, black, white, depth - 1,
                            nextBlackEvaluation, nextWhiteEvaluation, bestExpectation, beta) :
                    backtrack(true, black, white, depth - 1,
                            nextBlackEvaluation, nextWhiteEvaluation, alpha, bestExpectation);

            // 恢复userPieces
            togglePiece(userPieces, row, col);

            // 更新bestExpectation
            if (wantMax && nextBestExpectation > bestExpectation
                    || !wantMax && nextBestExpectation < bestExpectation) {
                bestExpectation = nextBestExpectation;
            }
        }
        return bestExpectation;
    }

    private static Evaluation getEvaluation(int[] side, int[] opponent) {
        Evaluation result = new Evaluation();
        for (int[][] scanLines : ALL_SCAN_LINES) {
            for (int[] line : scanLines) {
                boolean oneDead = true;
                int streak = 0;
                for (int move : line) {
                    int row = getRow(move);
                    int col = getCol(move);
                    if (hasPiece(side, row, col)) {
                        streak++;
                    } else if (hasPiece(opponent, row, col)) {
                        if (oneDead) {
                            result.addDoubleDead(streak);
                        } else {
                            result.addDead(streak);
                        }
                        streak = 0;
                        oneDead = true;
                    } else { // empty
                        if (oneDead) {
                            result.addDead(streak);
                        } else {
                            result.addLive(streak);
                        }
                        streak = 0;
                        oneDead = false;
                    }
                }
                // 处理line最后的streak
                if (oneDead) {
                    result.addDoubleDead(streak);
                } else {
                    result.addDead(streak);
                }
            }
        }
        return result;
    }

    /**
     * 判断点[tryRow, tryCol]附近是否有其他棋子
     *
     * @param black  黑子状态
     * @param white  白子状态
     * @param tryRow 点所在的行坐标
     * @param tryCol 所在的列坐标
     * @return 是否有其他棋子
     */
    private static boolean hasNearbyPiece(int[] black, int[] white, int tryRow, int tryCol) {
        int left = Math.max(tryCol - 2, 0);
        int right = Math.min(tryCol + 2, SIZE - 1);
        int top = Math.max(tryRow - 2, 0);
        int bottom = Math.min(tryRow + 2, SIZE - 1);
        for (int row = top; row <= bottom; row++) {
            if (hasAnyPiece(black, row, left, right) || hasAnyPiece(white, row, left, right)) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> getMoves(
            boolean wantMax, int[] black, int[] white,
            Evaluation blackEvaluation, Evaluation whiteEvaluation,
            Map<Integer, Evaluation> blackEvaluationMap, Map<Integer, Evaluation> whiteEvaluationMap) {
        int[] userPieces = wantMax ? black : white;

        List<Integer> moves = new ArrayList<>(SIZE * SIZE);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!hasPiece(black, row, col) && !hasPiece(white, row, col)
                        && hasNearbyPiece(black, white, row, col)) {
                    int move = packMove(row, col);
                    moves.add(move);
                    togglePiece(userPieces, row, col);

                    Evaluation nextBlackEvaluation = getNextEvaluation(wantMax, black, white, row, col, blackEvaluation);
                    blackEvaluationMap.put(move, nextBlackEvaluation);
                    Evaluation nextWhiteEvaluation = getNextEvaluation(!wantMax, white, black, row, col, whiteEvaluation);
                    whiteEvaluationMap.put(move, nextWhiteEvaluation);

                    togglePiece(userPieces, row, col); // 复原棋盘
                }
            }
        }
        moves.sort((move1, move2) -> {
            int expectation1 = blackEvaluationMap.get(move1).getScore() - whiteEvaluationMap.get(move1).getScore();
            int expectation2 = blackEvaluationMap.get(move2).getScore() - whiteEvaluationMap.get(move2).getScore();
            int diff = expectation1 - expectation2;
            return wantMax ? -diff : diff;
        });
        return moves;
    }

    /**
     * 根据最后一步棋子, 来计算某一方变化后的evaluation
     *
     * @param useGain    最后一步是己方还是对方走的棋
     * @param side       己方棋子状态
     * @param opposite   对方棋子状态
     * @param row        最后一步棋子的位置
     * @param col        最后一步棋子的位置
     * @param evaluation 下这一步棋子之前的evaluation
     * @return 新的evaluation
     */
    private static Evaluation getNextEvaluation(boolean useGain, int[] side, int[] opposite,
                                                int row, int col, Evaluation evaluation) {
        // useGain为true表示最后一步是"己方"下的; useGain为false表示最后一步是"对方"下的
        int[] count = new int[8];
        boolean[] live = new boolean[8];
        calculateCountAndLive(side, opposite, row, col, count, live);
        Evaluation nextEvaluation = new Evaluation(evaluation); // 这里使用了new. 复制了一份Evaluation对象.
        return useGain ? applyGain(nextEvaluation, count, live)
                : applyPain(nextEvaluation, count, live);
    }

    private static Evaluation applyGain(Evaluation evaluation, int[] count, boolean[] live) {
        for (int dir = 0; dir < 4; dir++) {
            int opposite = dir + 4;
            int c1 = count[dir];
            int c2 = count[opposite];
            if (!live[dir] && !live[opposite]) {
                evaluation.removeDead(c1)
                        .removeDead(c2)
                        .addDoubleDead(c1 + 1 + c2);
            } else if (live[dir] && live[opposite]) {
                evaluation.removeLive(c1)
                        .removeLive(c2)
                        .addLive(c1 + 1 + c2);
            } else if (live[dir] && !live[opposite]) {
                evaluation.removeLive(c1)
                        .removeDead(c2)
                        .addDead(c1 + 1 + c2);
            } else { // !live[dir] && live[opposite]
                evaluation.removeDead(c1)
                        .removeLive(c2)
                        .addDead(c1 + 1 + c2);
            }
        }
        return evaluation;
    }

    private static Evaluation applyPain(Evaluation evaluation, int[] count, boolean[] live) {
        for (int dir = 0; dir < 4; dir++) {
            int opposite = dir + 4;
            int c1 = count[dir];
            int c2 = count[opposite];
            if (!live[dir] && !live[opposite]) {
                evaluation.removeDead(c1)
                        .removeDead(c2);
            } else if (live[dir] && live[opposite]) {
                evaluation.removeLive(c1)
                        .addDead(c1)
                        .removeLive(c2)
                        .addDead(c2);
            } else if (live[dir] && !live[opposite]) {
                evaluation.removeLive(c1)
                        .addDead(c1)
                        .removeDead(c2);
            } else { // !live[dir] && live[opposite]
                evaluation.removeDead(c1)
                        .removeLive(c2)
                        .addDead(c2);
            }
        }
        return evaluation;
    }

    /**
     * 计算从点[startRow, startCol]出发的8个方向的情况
     * 方向对应的下标如下: (X表示起始点)
     * <pre>
     * _________
     * | 7 0 1 |
     * | 6 X 2 |
     * | 5 4 3 |
     * --------
     * </pre>
     * <p> 注意: count数组中的元素一开始不一定为0 </p>
     *
     * @param side     己方的棋子状态
     * @param opposite 对方的棋子状态
     * @param startRow 出发点的行坐标
     * @param startCol 出发点的列坐标
     * @param count    计算结果将存放的数组
     * @param live     计算结果将存放的数组
     */
    private static void calculateCountAndLive(int[] side, int[] opposite, int startRow, int startCol,
                                              int[] count, boolean[] live) {
        for (int dir = 0; dir < 8; dir++) {
            count[dir] = 0;
            int dx = DIRECTIONS[dir][0];
            int dy = DIRECTIONS[dir][1];
            int row = startRow + dx;
            int col = startCol + dy;
            while (row >= 0 && row < SIZE && col >= 0 && col < SIZE && hasPiece(side, row, col)) {
                row += dx;
                col += dy;
                count[dir]++;
            }
            live[dir] = row >= 0 && row < SIZE && col >= 0 && col < SIZE && !hasPiece(opposite, row, col);
        }
    }

    private static boolean hasPiece(int[] pieces, int row, int col) {
        return (pieces[row] & (1 << col)) != 0;
    }

    /**
     * 判断row[left...right](both inclusive)中是否至少有一个棋子
     */
    private static boolean hasAnyPiece(int[] pieces, int row, int left, int right) {
        int end = right + 1;
        return ((pieces[row] >> left) & (0x7fff >>> (SIZE - end + left))) != 0;
    }

    private static void togglePiece(int[] pieces, int row, int col) {
        pieces[row] ^= (1 << col);
    }

    private static int packMove(int row, int col) {
        return (row << 4) | col;
    }

    private static int getRow(int move) {
        return move >> 4;
    }

    private static int getCol(int move) {
        return move & 0xf;
    }

    private static class Evaluation {
        boolean gobang;
        int[] live = new int[5];
        int[] dead = new int[5];

        Evaluation() {
        }

        Evaluation(Evaluation evaluation) {
            this.gobang = evaluation.gobang;
            System.arraycopy(evaluation.live, 0, this.live, 0, 5);
            System.arraycopy(evaluation.dead, 0, this.dead, 0, 5);
        }

        int getScore() {
            if (gobang) {
                return GO_BANG;
            }
            int liveScore = live[1] + live[2] * 10 + live[3] * 100 + live[4] * 1000;
            int deadScore = dead[2] + dead[3] * 10 + dead[4] * 100;
            return liveScore + deadScore;
        }

        Evaluation addLive(int n) {
            if (n >= 5) {
                gobang = true;
            } else if (n > 0) {
                live[n]++;
            }
            return this;
        }

        Evaluation removeLive(int n) {
            if (n >= 5) {
                gobang = false;
            } else if (n > 0) {
                live[n]--;
            }
            return this;
        }

        Evaluation addDead(int n) {
            if (n >= 5) {
                gobang = true;
            } else if (n > 0) {
                dead[n]++;
            }
            return this;
        }

        void addDoubleDead(int n) {
            if (n >= 5) {
                gobang = true;
            }
        }

        Evaluation removeDead(int n) {
            if (n >= 5) {
                gobang = false;
            } else if (n > 0) {
                dead[n]--;
            }
            return this;
        }

        boolean hasLive4() {
            return live[4] >= 1;
        }

        boolean hasDoubleLive3() {
            return live[3] >= 2;
        }

        boolean couldLeadToWin() {
            return gobang || hasLive4() || hasDoubleLive3();
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Evaluation{");
            if (gobang) {
                builder.append("gobang");
            } else {
                for (int t = 4; t >= 1; t--) {
                    if (live[t] > 0) {
                        builder.append(MessageFormat.format("live{0}({1}) ", t, live[t]));
                    }
                }
                for (int t = 4; t >= 1; t--) {
                    if (dead[t] > 0) {
                        builder.append(MessageFormat.format("dead{0}({1}) ", t, dead[t]));
                    }
                }
            }
            builder.append("}");
            return builder.toString();
        }
    }
}
