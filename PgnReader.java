import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PgnReader {

    /**
     * Find the tagName tag pair in a PGN game and return its value.
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm
     *
     * @param tagName the name of the tag whose value you want
     * @param game a `String` containing the PGN text of a chess game
     * @return the value in the named tag pair
     */
    public static String tagValue(String tagName, String game) {
        String[] splitGame = splitString(game, "\n");
        int i = 0;
        while (!splitGame[i].isEmpty()) {
            String[] splitTagName = splitString(splitGame[i], " \"");
            String tag = splitTagName[0];
            String value = splitTagName[1];
            tag = tag.substring(1, tag.length());
            value = value.substring(0, value.length() - 2);
            if (tag.equals(tagName)) {
                return value;
            }
            i++;
        }
        return "NOT GIVEN";
    }

    /**
     * Play out the moves in game and return a String with the game's
     * final position in Forsyth-Edwards Notation (FEN).
     *
     * @see http://www.saremba.de/chessgml/standards/pgn/pgn-complete.htm#c16.1
     *
     * @param game a `Strring` containing a PGN-formatted chess game or opening
     * @return the game's final position in FEN.
     */
    public static String finalPosition(String game) {
        String[][] gameBoard = generateBoard();
        String[] moves = listMoves(game);

        for (int i = 0; i < moves.length; i++) {
            if (i % 2 == 0) {
                String[] parsedInfo = parseMove(moves[i], "white");

                if (parsedInfo[0].equals("King Castle white")) {
                    String[] kingMove = {"e1", "g1"};
                    String[] rookMove = {"h1", "f1"};
                    gameBoard = executeMove(gameBoard, kingMove, "K",
                    parsedInfo);
                    gameBoard = executeMove(gameBoard, rookMove, "R",
                    parsedInfo);
                } else if (parsedInfo[0].equals("Queen Castle white")) {
                    String[] kingMove = {"e1", "c1"};
                    String[] rookMove = {"a1", "d1"};
                    gameBoard = executeMove(gameBoard, kingMove, "K",
                    parsedInfo);
                    gameBoard = executeMove(gameBoard, rookMove, "R",
                    parsedInfo);
                } else {
                    String[] startAndEnd = determineMove(parsedInfo, gameBoard);
                    gameBoard = executeMove(gameBoard, startAndEnd,
                    parsedInfo[0], parsedInfo);
                }
            } else {
                String[] parsedInfo = parseMove(moves[i], "black");

                if (parsedInfo[0].equals("King Castle black")) {
                    String[] kingMove = {"e8", "g8"};
                    String[] rookMove = {"h8", "f8"};
                    gameBoard = executeMove(gameBoard, kingMove, "k",
                    parsedInfo);
                    gameBoard = executeMove(gameBoard, rookMove, "r",
                    parsedInfo);
                } else if (parsedInfo[0].equals("Queen Castle black")) {
                    String[] kingMove = {"e8", "c8"};
                    String[] rookMove = {"a8", "d8"};
                    gameBoard = executeMove(gameBoard, kingMove, "k",
                    parsedInfo);
                    gameBoard = executeMove(gameBoard, rookMove, "r",
                    parsedInfo);
                } else {
                    String[] startAndEnd = determineMove(parsedInfo, gameBoard);
                    gameBoard = executeMove(gameBoard, startAndEnd,
                    parsedInfo[0], parsedInfo);
                }
            }
        }

        String board = convertBoard(gameBoard);
        return board;
    }

    /**
     * Reads the file named by path and returns its content as a String.
     *
     * @param path the relative or abolute path of the file to read
     * @return a String containing the content of the file
     */
    public static String fileContent(String path) {
        Path file = Paths.get(path);
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Add the \n that's removed by readline()
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            System.exit(1);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String game = fileContent(args[0]);
        System.out.format("Event: %s%n", tagValue("Event", game));
        System.out.format("Site: %s%n", tagValue("Site", game));
        System.out.format("Date: %s%n", tagValue("Date", game));
        System.out.format("Round: %s%n", tagValue("Round", game));
        System.out.format("White: %s%n", tagValue("White", game));
        System.out.format("Black: %s%n", tagValue("Black", game));
        System.out.format("Result: %s%n", tagValue("Result", game));
        System.out.println("Final Position:");
        System.out.println(finalPosition(game));
    }


    public static String[][] generateBoard() {
        String[][] gameBoard = new String[8][8];
        String[] firstRow = "rnbqkbnr".split("");
        String[] secondRow = "pppppppp".split("");
        String[] middleRow = "11111111".split("");
        String[] seventhRow = "PPPPPPPP".split("");
        String[] eigthRow = "RNBQKBNR".split("");

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0) {
                    gameBoard[i][j] = firstRow[j];
                } else if (i == 1) {
                    gameBoard[i][j] = secondRow[j];
                } else if (i == 6) {
                    gameBoard[i][j] = seventhRow[j];
                } else if (i == 7) {
                    gameBoard[i][j] = eigthRow[j];
                } else {
                    gameBoard[i][j] = middleRow[j];
                }
            }
        }
        return gameBoard;
    }

    public static String convertBoard(String[][] gameBoard) {
        String[] rows = new String[8];
        String str;
        String board = "";

        for (int i = 0; i < 8; i++) {
            str = String.join("", gameBoard[i]);
            rows[i] = new String(str);
        }

        for (int i = 0; i < 8; i++) {
            String row = rows[i];
            int count = 0;
            String tempRow = "";

            for (int j = 0; j < 8; j++) {
                String letter = row.substring(j, j + 1);
                if (letter.equals("1")) {
                    count++;
                } else {
                    tempRow += String.valueOf(count);
                    tempRow += letter;
                    count = 0;
                }
            }
            tempRow += String.valueOf(count);
            tempRow = tempRow.replaceAll("0", "");
            if (i != 7) {
                tempRow += "/";
            }
            board += tempRow;
        }

        return board;
    }

    public static String[] listMoves(String game) {
        String[] messyMoves = splitString(game, "\\d+\\.");
        int numberOfMoves = messyMoves.length * 2 - 2;
        String[] cleanMoves = new String[numberOfMoves];
        int j = 0;

        for (int i = 1; i < messyMoves.length; i++) {
            String[] movePair = splitString(messyMoves[i], " ");
            if (movePair.length == 3) {
                cleanMoves[j] = movePair[1];
                cleanMoves[j] = cleanMoves[j].replaceAll("\\s", "");
                cleanMoves[j + 1] = movePair[2];
                cleanMoves[j + 1] = cleanMoves[j + 1].replaceAll("\\s", "");
                j += 2;
            } else {
                cleanMoves[j] = movePair[1];
                cleanMoves[j] = cleanMoves[j].replaceAll("\\s", "");
            }
        }

        if (cleanMoves[cleanMoves.length - 1] == null) {
            String[] newCleanMoves = new String[cleanMoves.length - 1];
            for (int i = 0; i < newCleanMoves.length; i++) {
                newCleanMoves[i] = cleanMoves[i];
            }
            return newCleanMoves;
        }


        return cleanMoves;
    }

    public static String[] parseMove(String move, String color) {
        // {piece, destination, captured?, check/checkmate?, disambiguation,
        // promoted piece}
        String[] parsedInfo = {"p", "", "", "", "", ""};
        String piece = "";
        if (color.equals("white")) {
            if (move.equals("O-O")) {
                return new String[] {"King Castle white"};
            } else if (move.equals("O-O-O")) {
                return new String[] {"Queen Castle white"};
            }
        } else {
            if (move.equals("O-O")) {
                return new String[] {"King Castle black"};
            } else if (move.equals("O-O-O")) {
                return new String[] {"Queen Castle black"};
            }
        }

        if (color.equals("white")) {
            parsedInfo[0] = "P";
        }

        move = move.replaceAll("\\!", "");
        move = move.replaceAll("\\?", "");

        if (move.substring(move.length() - 1).equals("+")) {
            parsedInfo[3] = "check";
            move = move.replaceAll("\\+", "");
        } else if (move.substring(move.length() - 1).equals("#")) {
            parsedInfo[3] = "mate";
            move = move.replace("#", "");
        }

        if (move.contains("x")) {
            parsedInfo[2] = "yes";
            move = move.replace("x", "");
        }

        if (move.contains("=")) {
            int i = move.indexOf("=");
            String promote = move.substring(i, i + 2);
            String promotedPiece = promote.substring(1, 2);
            move = move.replace(promote, "");
            parsedInfo[5] = promotedPiece;
        }

        String destination = move.substring(move.length() - 2, move.length());
        parsedInfo[1] = destination;
        move = move.replace(destination, "");

        if (!move.isEmpty()) {
            char pieceChar = move.charAt(0);
            if (Character.isUpperCase(pieceChar)) {
                move = move.replace(move.substring(0, 1), "");
                if (color.equals("white")) {
                    piece = Character.toString(pieceChar);
                } else {
                    piece = Character.toString(
                    Character.toLowerCase(pieceChar));
                }
                parsedInfo[0] = piece;
            }
        }

        String remainder = move;
        parsedInfo[4] = remainder;

        return parsedInfo;
    }

    public static String[][] executeMove(String[][] gameBoard,
            String[] startAndEnd, String piece, String[] parsedInfo) {
        String start = startAndEnd[0];
        String destination = startAndEnd[1];
        char startFile = start.charAt(0);
        int startRank = Integer.parseInt(Character.toString(start.charAt(1)));
        char destinationFile = destination.charAt(0);
        int destinationRank = Integer.parseInt(
            Character.toString(destination.charAt(1)));
        int startRow = 8 - startRank;
        int endRow = 8 - destinationRank;
        int startCol = startFile - 97;
        int endCol = destinationFile - 97;
        char pieceChar = piece.charAt(0);
        boolean white = Character.isUpperCase(pieceChar);
        String captured = "";
        String promotedPiece = "";

        if (parsedInfo.length > 1) {
            captured = parsedInfo[2];
            promotedPiece = parsedInfo[5];
            if (captured.equals("yes")
                && gameBoard[endRow][endCol].equals("1")) {
                if (white) {
                    gameBoard[endRow + 1][endCol] = "1";
                } else {
                    gameBoard[endRow - 1][endCol] = "1";
                }
            }
        }

        if (!promotedPiece.isEmpty()) {
            gameBoard[endRow][endCol] = promotedPiece;
            gameBoard[startRow][startCol] = "1";
        } else {
            gameBoard[endRow][endCol] = piece;
            gameBoard[startRow][startCol] = "1";
        }
        return gameBoard;
    }

    public static String[] determineMove(String[] parsedInfo,
            String[][] gameBoard) {
        String piece = parsedInfo[0];
        String destination = parsedInfo[1];
        char destinationFile = destination.charAt(0);
        char destinationRank = destination.charAt(1);
        String captured = parsedInfo[2];
        String check = parsedInfo[3];
        String disambiguation = parsedInfo[4];
        String promotion = parsedInfo[5];
        String[] startAndEnd = {"", destination};
        String file = "";
        String rank = "";
        String start = "";

        if (!disambiguation.isEmpty() && disambiguation.length() == 1) {
            char fileOrRank = disambiguation.toCharArray()[0];
            if (piece.equals("P")) {
                file = disambiguation;
                rank = Character.toString(
                Character.toChars(destinationRank - 1)[0]);
                startAndEnd[0] = file + rank;
                return startAndEnd;
            } else if (piece.equals("p")) {
                file = disambiguation;
                rank = Character.toString(
                Character.toChars(destinationRank + 1)[0]);
                startAndEnd[0] = file + rank;
                return startAndEnd;
            } else if (Character.isDigit(fileOrRank)) {
                for (int j = 0; j < 8; j++) {
                    char fileChar = Character.toChars(j + 97)[0];
                    int rankNum = Character.getNumericValue(fileOrRank);
                    rank = Integer.toString(rankNum);
                    start = Character.toString(fileChar) + rank;
                    if (isValidMove(gameBoard, start, destination,
                        gameBoard[8 - rankNum][j])
                        && gameBoard[8 - rankNum][j].equals(piece)) {
                        startAndEnd[0] = start;
                        return startAndEnd;
                    }
                }
            } else {
                for (int i = 0; i < 8; i++) {
                    rank = Integer.toString(8 - i);
                    file = Character.toString(fileOrRank);
                    int fileNum = Character.getNumericValue(
                        (char) (fileOrRank - 49));
                    start = file + rank;

                    if (isValidMove(gameBoard, start, destination,
                        gameBoard[i][fileNum])
                        && gameBoard[i][fileNum].equals(piece)) {
                        startAndEnd[0] = start;
                        return startAndEnd;
                    }
                }
            }

        } else if (!disambiguation.isEmpty() && disambiguation.length() == 2) {
            startAndEnd[0] = disambiguation;
            return startAndEnd;
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char rankChar = Character.toChars(j + 97)[0];
                file = Integer.toString(8 - i);
                start = Character.toString(rankChar) + file;
                if (isValidMove(gameBoard, start, destination,
                    gameBoard[i][j]) && gameBoard[i][j].equals(piece)) {
                    startAndEnd[0] = start;
                    return startAndEnd;
                }
            }
        }

        return startAndEnd;
    }

    public static boolean isValidMove(String[][] gameBoard, String start,
            String destination, String piece) {
        char startFile = start.charAt(0);
        int startRank = Integer.parseInt(Character.toString(start.charAt(1)));
        char destinationFile = destination.charAt(0);
        int destinationRank = Integer.parseInt(
            Character.toString(destination.charAt(1)));

        int startRow = 8 - startRank;
        int endRow = 8 - destinationRank;
        int startCol = startFile - 97;
        int endCol = destinationFile - 97;

        if (piece.equals("P") && startCol == endCol) {
            if (((destinationRank - startRank) == 2
                && gameBoard[startRow - 1][startCol].equals("1"))
                || (destinationRank - startRank == 1
                && !gameBoard[startRow][startCol].equals("1"))) {
                return true;
            }
            return false;
        } else if (piece.equals("p") && startCol == endCol) {
            if (((destinationRank - startRank) == -2
                && gameBoard[startRow + 1][startCol].equals("1"))
                || (destinationRank - startRank == -1
                && !gameBoard[startRow][startCol].equals("1"))) {
                return true;
            }
        }

        if (piece.equals("R") || piece.equals("r")) {
            if (startFile == destinationFile) {
                return checkColumn(gameBoard, startRank, destinationRank,
                startCol);
            } else if (startRank == destinationRank) {
                return checkRow(gameBoard, startCol, endCol, startRow);
            } else {
                return false;
            }
        }

        if (piece.equals("N") || piece.equals("n")) {
            if ((abs(startCol - endCol) == 2
                && abs(startRow - endRow) == 1)
                || (abs(startCol - endCol) == 1
                && abs(startRow - endRow) == 2)) {
                return true;
            }
        }

        if (piece.equals("B") || piece.equals("b")) {
            return checkDiagonal(gameBoard, startCol, endCol, startRow, endRow);
        }

        if (piece.equals("Q") || piece.equals("q")) {
            if (checkColumn(gameBoard, startRank, destinationRank, startCol)
                || checkRow(gameBoard, startCol, endCol, startRow)
                || checkDiagonal(gameBoard, startCol, endCol, startRow,
                endRow)) {
                return true;
            }
        }

        if (piece.equals("K") || piece.equals("k")) {
            return true;
        }

        return false;
    }

    public static boolean checkColumn(String[][] gameBoard, int startRank,
            int destinationRank, int startCol) {
        int rankDiff = destinationRank - startRank;
        if (rankDiff > 0) {
            for (int i = startRank + 1; i < destinationRank; i++) {
                if (!gameBoard[8 - i][startCol].equals("1")) {
                    return false;
                }
            }
            return true;
        } else {
            for (int i = startRank - 1; i > destinationRank; i--) {
                if (!gameBoard[8 - i][startCol].equals("1")) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean checkRow(String[][] gameBoard, int startCol,
            int endCol, int startRow) {
        int fileDiff = endCol - startCol;
        if (fileDiff > 0) {
            for (int i = startCol + 1; i < endCol; i++) {
                if (!gameBoard[startRow][i].equals("1")) {
                    return false;
                }
            }
            return true;
        } else {
            for (int i = startCol - 1; i > endCol; i--) {
                if (!gameBoard[startRow][i].equals("1")) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean checkDiagonal(String[][] gameBoard, int startCol,
            int endCol, int startRow, int endRow) {
        int rankDiff = endRow - startRow;
        int fileDiff = endCol - startCol;
        if (abs(fileDiff) == abs(rankDiff)) {
            if (rankDiff > 0 && fileDiff > 0) {
                for (int i = 1; i < abs(rankDiff); i++) {
                    if (!gameBoard[startRow + 1][startCol + 1].equals("1")) {
                        return false;
                    }
                }
            } else if (rankDiff > 0 && fileDiff < 0) {
                for (int i = 1; i < abs(rankDiff); i++) {
                    if (!gameBoard[startRow + 1][startCol - 1].equals("1")) {
                        return false;
                    }
                }
            } else if (rankDiff < 0 && fileDiff > 0) {
                for (int i = 1; i < abs(rankDiff); i++) {
                    if (!gameBoard[startRow - 1][startCol + 1].equals("1")) {
                        return false;
                    }
                }
            } else if (rankDiff < 0 && fileDiff < 0) {
                for (int i = 1; i < abs(rankDiff); i++) {
                    if (!gameBoard[startRow - 1][startCol - 1].equals("1")) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static String[] splitString(String game, String delimiter) {
        String[] splitGame = game.split(delimiter);
        return splitGame;
    }

    public static int abs(int num) {
        if (num > 0) {
            return num;
        } else {
            return -1 * num;
        }
    }
}
