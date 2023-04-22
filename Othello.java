package Othello;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Othello {


	static List<List<Character>> board = new ArrayList<>();

	static char player = 'X';

	public static List<List<Character>> clone(List<List<Character>> Array){
		List<List<Character>> clone = new ArrayList<>(Array.size());
		for (List<Character> list : Array) {
			clone.add(new ArrayList<>(list.size()));
			for (Character element : list) {
				clone.get(clone.size() - 1).add(element);
			}
		}
		return clone;
	}


	public static void initBoard() {
		for(int i = 0; i < 8; i++) {
			List<Character> row = new ArrayList<>();
			for(int j = 0; j < 8; j++) {
				row.add('-');
			}
			board.add(row);
		}
		board.get(3).set(3, 'X');
		board.get(3).set(4, 'O');
		board.get(4).set(3, 'O');
		board.get(4).set(4, 'X');
	}

	public static boolean isValidMove(int x, int y, char player, List<List<Character>> board) {
		if (x < 0 || x >= board.size() || y < 0 || y >= board.get(x).size()) return false;
		if (board.get(x).get(y) != '-') return false;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				int k = 1;
				while (true) {
					int newX = x + i * k;
					int newY = y + j * k;
					if (newX < 0 || newX >= board.size() || newY < 0 || newY >= board.get(newX).size()) break;
					if (board.get(newX).get(newY) == '-') break;
					if (board.get(newX).get(newY) == player) {
						if (k > 1) {
							return true;
						}
						break;
					}
					k++;
				}
			}
		}
		return false;
	}

	public static void playMove(List<List<Character>> map, int x, int y) {
		map.get(x).set(y, player);
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0) continue;
				int k = 1;
				while (true) {
					int newX = x + i * k;
					int newY = y + j * k;
					if (newX < 0 || newX >= map.size() || newY < 0 || newY >= map.get(newX).size()) break;
					if (map.get(newX).get(newY) == '-') break;
					if (map.get(newX).get(newY) == player) {
						if (k > 1) {
							for(int k2 = k-1; k2 > 0; k2--) {
								int newX2 = x + i * k2;
								int newY2 = y + j * k2;
								map.get(newX2).set(newY2, player);
							}
							break;
						}
						break;
					}
					k++;
				}
			}
		}
	}

	public static boolean switchPlayer() {
		char newPlayer = (player == 'X') ? 'O' : 'X';
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				if (isValidMove(i, j, newPlayer, board)) {
					player = newPlayer;
					return true;
				}
			}
		}
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				if (isValidMove(i, j, player, board)) {
					return true;
				}
			}
		}
		return false;
	}

	public static int scoreX(List<List<Character>> board, char moi) {
		int X = 0;
		int O = 0;
		for (List<Character> line : board) {
			for (char c : line) {
				X += (c == 'X') ? 1 : 0;
				O += (c == 'O') ? 1 : 0;
			}
		}
		if (moi == 'X') {
			return X - O;
		}
		else {
			return O - X;
		}
	}

	public static int getEval(List<List<Character>> board, char moi) {
		int playerScore = 0;
		int opponentScore = 0;
		// Calcul de la différence du nombre de coups possibles
		int playerMobility = listDesCoupsPossible(moi, board).size();
		int opponentMobility = listDesCoupsPossible(getOpponent(moi), board).size();
		// Calcule le nombre de mes pièces et celles de l'adversaire
		int playerPieces = 0;
		int opponentPieces = 0;
		int playerPositionValue = 0;
		int opponentPositionValue = 0;

		//si position finale, retourne la difference de score
		if(isGameOver(board)) {
			return scoreX(board, moi)*10000;
		}

		if(canIFfOpponent(board, moi)) return 100000;


		// calcule le score du board
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				if(board.get(i).get(j)==moi) {
					playerPositionValue += getPositionalValue(board,i, j);
					playerPieces += 1;
				}
				else if(board.get(i).get(j)==getOpponent(moi)) {
					opponentPositionValue += getPositionalValue(board,i, j);
					opponentPieces += 1;
				}
			}
		}

		int playerStability = getStability(board,moi);
		int opponentStability = getStability(board,getOpponent(moi));

		// poids déterminés à l'aide de ce review : https://courses.cs.washington.edu/courses/cse573/04au/Project/mini1/RUSSIA/Final_Paper.pdf
		playerScore += playerPositionValue*1000 + playerMobility*50 + playerStability*150 + playerPieces*200;
		opponentScore += opponentPositionValue*1000 + opponentMobility*50 + opponentStability*150 + opponentPieces*200;

		return 200*evalParity(board) + playerScore - opponentScore;
	}
	/*
	    public static int evalBoardMap(List<List<Character>> board , char moi){
	        char oplayer = getOpponent(moi);
	        int[][] priorityBoard = {
	                {200 , -100, 100,  50,  50, 100, -100,  200},
	                {-100, -200, -50, -50, -50, -50, -200, -100},
	                {100 ,  -50, 100,   0,   0, 100,  -50,  100},
	                {50  ,  -50,   0,   0,   0,   0,  -50,   50},
	                {50  ,  -50,   0,   0,   0,   0,  -50,   50},
	                {100 ,  -50, 100,   0,   0, 100,  -50,  100},
	                {-100, -200, -50, -50, -50, -50, -200, -100},
	                {200 , -100, 100,  50,  50, 100, -100,  200}};

	        //if corners are taken W for that 1/4 loses effect
	        if(board.get(0).get(0) != 0){
	            for (int i = 0; i < 3; i++) {
	                for (int j = 0; j <= 3; j++) {
	                    priorityBoard[i][j] = 0;
	                }
	            }
	        }

	        if(board.get(0).get(7) != 0){
	            for (int i = 0; i < 3; i++) {
	                for (int j = 4; j <= 7; j++) {
	                    priorityBoard[i][j] = 0;
	                }
	            }
	        }

	        if(board.get(7).get(0) != 0){
	            for (int i = 5; i < 8; i++) {
	                for (int j = 0; j <= 3; j++) {
	                    priorityBoard[i][j] = 0;
	                }
	            }
	        }

	        if(board.get(7).get(7) != 0){
	            for (int i = 5; i < 8; i++) {
	                for (int j = 4; j <= 7; j++) {
	                    priorityBoard[i][j] = 0;
	                }
	            }
	        }


	        int myScore = 0;
	        int opScore = 0;

	        for (int i = 0; i < 8; i++) {
	            for (int j = 0; j < 8; j++) {
	                if(board.get(i).get(j)==moi) myScore += priorityBoard[i][j];
	                if(board.get(i).get(j)==oplayer) opScore += priorityBoard[i][j];
	            }
	        }

	        return (myScore - opScore) / (myScore + opScore + 1);
	    }
	 */
	public static int evalParity(List<List<Character>> board){
		int remDiscs = 64 - getTotalCount(board);
		return remDiscs % 2 == 0 ? -1 : 1;
	}

	public static int getTotalCount(List<List<Character>> board) {

		int X = 0;
		int O = 0;
		for (List<Character> line : board) {
			for (char c : line) {
				X += (c == 'X') ? 1 : 0;
				O += (c == 'O') ? 1 : 0;
			}
		}
		return X + O;
	}


	public static int getPositionalValue(List<List<Character>> board, int i, int j) {
		// Retourne une valeur supérieure pour les pièces qui sont dans les coins
		if ((i == 0 && j == 0) || (i == 0 && j == 7) || (i == 7 && j == 0) || (i == 7 && j == 7) ) {
			return 1000;
		}
		// Retourne valeur inférieur si position adjacente à un coin et que le coin n'est pas pris par nous	
		else if ((i==1 && j==1) || (i==1 && j==6) || (i==6 && j==1) || (i==6 && j==6)) {
			return -1000;
		}
		else if ((i==1 && j==0 && board.get(0).get(0)=='-') || (i==0 && j==1 && board.get(0).get(0)=='-') 
		|| (i==7 && j==1 && board.get(7).get(0)=='-') || (i==6 && j==0 && board.get(7).get(0)=='-') 
				|| (i==0 && j==6 && board.get(0).get(7)=='-') || (i==1 && j==7 && board.get(0).get(7)=='-') 
				|| (i==7 && j==6 && board.get(7).get(7)=='-') || (i==6 && j==7 && board.get(7).get(7)=='-')) {
			return -500;
		}
		else if (((i == 0 || i == 7) && (j == 2 || j == 5)) || 
				((j == 0 || j == 7) && (i == 2 || i == 5))) {
			return 100;
		}
		else if (((i == 0 || i == 7) && (j == 3 || j == 4)) || 
				((j == 0 || j == 7) && (i == 3 || i == 4))) {
			return 50;
		}
		else if (((i == 1 || i == 6) && (j == 2 || j == 3 || j == 4)) || ((j == 1 || j == 6) && (i == 2 || i == 3 || i == 4))) {
			return -20;
		}
		else {
			return 10;
		}
	}

	// Si il y a un move qui peut faire abandonner 
	// le tour de l'adversaire, on return true
	public static boolean canIFfOpponent(List<List<Character>> board, char moi) {
		int playerMobility = listDesCoupsPossible(moi, board).size();
		int opponentMobility = listDesCoupsPossible(getOpponent(moi), board).size();

		return opponentMobility == 0 && playerMobility != 0;
	}

	public static int getStability(List<List<Character>> board, char player) {
		int playerStability = 0;
		int n = board.size();

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (board.get(i).get(j) == player) {
					int stability = 0;

					// Check horizontal
					boolean leftStable = true;
					boolean rightStable = true;
					for (int k = 0; k < n; k++) {
						if (k < j) {
							if (board.get(i).get(k) != player) {
								leftStable = false;
							}
						} else if (k > j) {
							if (board.get(i).get(k) != player) {
								rightStable = false;
							}
						}
					}
					if (leftStable || rightStable) {
						stability++;
					}

					// Check vertical
					boolean topStable = true;
					boolean bottomStable = true;
					for (int k = 0; k < n; k++) {
						if (board.get(k).get(j) != player) {
							if (k < i) {
								topStable = false;
							}
							else if (k > i) {
								bottomStable = false;

							}
						}
					}
					if (topStable || bottomStable) {
						stability++;
					}

					// Check diagonal (en haut à gauche à en bas à droite)
					boolean topLeftStable = true;
					boolean bottomRightStable = true;
					int k = i - Math.min(i, j);
					int l = j - Math.min(i, j);
					while (k < n && l < n) {
						if (k != i && l != j && board.get(k).get(l) != player) {
							topLeftStable = false;
						}
						k++;
						l++;
					}
					k = i + Math.min(n - i - 1, n - j - 1);
					l = j + Math.min(n - i - 1, n - j - 1);
					while (k >= 0 && l >= 0) {
						if (k != i && l != j && board.get(k).get(l) != player) {

							bottomRightStable = false;
						}
						k--;
						l--;
					}
					if (topLeftStable || bottomRightStable) {
						stability++;
					}

					// Check diagonal (en haut à droite à en bas à gauche)
					boolean topRightStable = true;
					boolean bottomLeftStable = true;
					k = i - Math.min(i, n - j - 1);
					l = j + Math.min(i, n - j - 1);
					while (k < n && l >= 0) {
						if (k != i && l != j && board.get(k).get(l) != player) {
							topRightStable = false;
						}
						k++;
						l--;
					}
					k = i + Math.min(n - i - 1, j);
					l = j - Math.min(n - i - 1, j);
					while (k >= 0 && l < n) {
						if (k != i && l != j && board.get(k).get(l) != player) {
							bottomLeftStable = false;
						}
						k--;
						l++;
					}
					if (topRightStable || bottomLeftStable) {
						stability++;
					}
					playerStability += stability;
				}
			}
		}
		if(playerStability==0) {
			return -10;
		}
		return playerStability;
	}



	public static List< int[] > listDesCoupsPossible(char player, List<List<Character>> board) {
		ArrayList<  int[] > result = new ArrayList<>();
		for (int i = 0; i < board.size(); i++) {
			for (int j = 0; j < board.get(i).size(); j++) {
				if (isValidMove(i, j, player, board)) {
					int [] pair = new int[2];
					pair[0] = i;
					pair[1] = j;

					result.add(pair);
				}
			}
		}
		return result;
	}



	public static int getDepth(List<int[]> actionsPossible) {

		int depth;
		if (!actionsPossible.isEmpty() && actionsPossible.size() < 5) {
			depth = 8;
		} else if (actionsPossible.size() >= 5 && actionsPossible.size() < 7) {
			depth = 7;
		} else {
			depth = 6;
		}


		return depth;
	}

	public static int[] chooseMove(char moi) {
		//variable pour le temps, à ne pas mettre lorsque l'on joue contre l'I.A 
		long startTime = System.currentTimeMillis();

		// liste des moves possibles
		List< int[] > actionsPossible = listDesCoupsPossible(player, board);	
		if(actionsPossible.size()==1) return actionsPossible.get(0);
		// Profondeur de l'alpha Beta
		int theDepth = getDepth(actionsPossible);
		int bestScore = Integer.MIN_VALUE;
		int [] bestMove = {actionsPossible.get(0)[0],actionsPossible.get(0)[1]} ;
		List<List<Character>> clone = clone(board);
		for( int[] action : actionsPossible) {
			playMove(clone, action[0], action[1]);
			switchPlayer();
			if (canIFfOpponent(clone,moi)) {
				return action;
			}
			else if((action[0] == 0 && action[1] == 0) || (action[0] == 0 && action[1] == 7)
					|| (action[0] == 7 && action[1] == 0) || (action[0] == 7 && action[1] == 7)) {
				return action;
			}
			else {
				int score = alphaBeta(clone, theDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, moi, startTime);
				if(score > bestScore) {
					bestScore = score;
					bestMove[0] = action[0];
					bestMove[1] = action[1];
				}

			}
			clone = clone(board);
		}
		return bestMove;
	}

	public static int alphaBeta(List<List<Character>> tempboard, int depth, int alpha, int beta, char moi, long startTime) {

	    List< int[] > actionsPossible = listDesCoupsPossible(player, tempboard);

	    if (depth == 0 || isGameOver(tempboard) || System.currentTimeMillis() - startTime > 3000) {
	        return getEval(tempboard,moi);
	    }

	    if((player == moi && actionsPossible.isEmpty()) || (player != moi && listDesCoupsPossible(getOpponent(moi), tempboard).isEmpty())) {
	        switchPlayer();
	        return alphaBeta(tempboard, depth - 1, alpha, beta, moi, startTime);
	    }

	    if (player==moi) {
	        for (int[] action : actionsPossible) {
	            List<List<Character>> newBoard = clone(tempboard);
	            playMove(newBoard, action[0], action[1]);
	            switchPlayer();
	            int runningMax = alphaBeta(newBoard, depth - 1, alpha, beta, moi, startTime);
	            if (runningMax > alpha) {
	                alpha = runningMax;
	            }
	            if (beta <= alpha) {
	                return alpha; //coupe
	            }
	        }
	        return alpha;
	    }
	    else {
	        for (int[] action : actionsPossible) {
	            List<List<Character>> newBoard = clone(tempboard);
	            playMove(newBoard, action[0], action[1]);
	            switchPlayer();
	            int runningMin = alphaBeta(newBoard, depth - 1, alpha, beta, moi, startTime);
	            if (runningMin < beta) {
	                beta = runningMin;
	            }
	            if (beta <= alpha) {
	                return beta; //coupe
	            }
	        }
	    }

	    return beta;
	}


	public static char getOpponent(char moi) {
		return moi == 'X' ? 'O' : 'X';
	}


	public static boolean isGameOver(List<List<Character>> board) {

		// Vérifie si les deux joueurs ne peuvent plus jouer
		boolean playerCannotMove = listDesCoupsPossible(player ,board).isEmpty();
		boolean opponentCannotMove = listDesCoupsPossible(getOpponent(player),board).isEmpty();
		return playerCannotMove && opponentCannotMove;
	}



	public static void main(String[] args) {

		if (args.length != 1 || (args[0].charAt(0) != 'X' && args[0].charAt(0) != 'O')) {
			System.err.println("Utilisation :");
			System.err.println("java Main [X|O]");
			System.exit(0);
		}
		Scanner scanner = new Scanner(System.in);
		char moi = args[0].charAt(0);
		initBoard();
		while (true) {
			int row;
			int col;

			if(moi == player) {
				List< int[] > actionsPossible = listDesCoupsPossible(player, board);	
				if(actionsPossible.isEmpty()) {
					switchPlayer();
					continue;
				}
				int[] move = chooseMove(moi);
				row = move[0];
				col = move[1];
				player = moi;
				System.out.println(row + "" + col);

			}
			else {
				String coups = scanner.nextLine();
				System.err.println("coups: \"" + coups + "\"");
				row = coups.charAt(0) - '0';
				col = coups.charAt(1) - '0';
			}

			if (isValidMove(row, col, player, board)) {
				playMove(board,row, col);
				if (!switchPlayer()) { 	
					break;
				}
			} else {
				System.err.println("ERREUR: coups " + row + " " + col + " est invalide !");
				scanner.close();
				return;
			}
		}
		scanner.close();
	}
}
