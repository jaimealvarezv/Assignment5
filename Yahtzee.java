/*
 * File: Yahtzee.java
 * ------------------
 * This program will play the Yahtzee game.
 * Author: Jaime Alvarez
 * Assignment 5.
 */

import acm.io.*;
import acm.util.*;
import acm.program.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		setupGame();
		playGame();
	}
 
	/**
	 * Gets the player names and display its name, according to the number of
	 * players
	 */

	private void setupGame() {

		// Reads the player number
		setupPlayerNumber();

		// Reads the player names.
		setupPlayerNames();

		// Set up an array of scores to store the player's points
		setupPlayerScores();

	}

	/**
	 * Reads the player number and its names
	 * 
	 */

	private void setupPlayerNumber() {
		String windowErrorTitle = "Error";
		String maxPlayerMsg = "El maximo de Jugadores permitidos es "
				+ MAX_PLAYERS;

		// Read the player's names according to the number of players.
		do {
			nPlayers = dialog.readInt("Enter number of players");

			// If the number of players is not possible show a message to
			// the user.
			if (nPlayers > MAX_PLAYERS) {
				JOptionPane.showMessageDialog(rootPane, maxPlayerMsg,
						windowErrorTitle, JOptionPane.ERROR_MESSAGE);
			}

		} while (nPlayers > MAX_PLAYERS);

	}

	/**
	 * Reads the player's names
	 */

	private void setupPlayerNames() {
		// Read the players Names
		playerNames = new String[nPlayers];

		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}

	}

	/**
	 * Creates an array of scores, one for each player
	 */

	private void setupPlayerScores() {

		// YahtzeeScore yahtzeeScore = new YahtzeeScore();

		for (int i = 0; i < nPlayers; i++) {
			int[] scores = new int[TOTAL + 1];

			for (int j = 1; j < scores.length; j++) {
				scores[j] = NO_VALUE;
			}

			playersScoreList.add(scores);
		}

	}

	/**
	 * Plays the game
	 */

	private void playGame() {

		// Display the game score sheet
		display = new YahtzeeDisplay(getGCanvas(), playerNames);

		for (int currentGameRound = 1; currentGameRound <= MAX_GAME_ROUNDS; currentGameRound++) {

			for (currentPlayer = 1; currentPlayer <= nPlayers; currentPlayer++) {

				processPlayerTurn();

				for (int playerRounds = 1; playerRounds <= MAX_PLAYER_ROUNDS - 1; playerRounds++) {
					processPlayerRound(playerRounds);
				}

				processCategory();
				updateTotals();

			}
		}

		updateMiddleTotals();
		showWinner();
	}

	/**
	 * Process Current player turn
	 */

	private void processPlayerTurn() {

		String currentPlayerName = playerNames[currentPlayer - 1];
		String turnMsg = currentPlayerName
				+ "'s turn. Click the \"Roll Dice\" button to roll dice. ";

		// Display current player turn message
		display.printMessage(turnMsg);

		// Wait for current player roll dices.
		display.waitForPlayerToClickRoll(currentPlayer);

		// Rolls dice
		currentDiceResult = rollDice();

		// Displays roll dice result
		display.displayDice(currentDiceResult);

	}

	/**
	 * Process the player round
	 * 
	 * @param playerRounds
	 *            The current player round.
	 */

	private void processPlayerRound(int playerRounds) {

		String rollAgainMsg = "Select the dice you wish to re-roll and click \"Roll Again\". "
				+ "Rolls Again avalaible: "
				+ (MAX_PLAYER_ROUNDS - playerRounds);

		// Prints the turn message
		display.printMessage(rollAgainMsg);

		display.waitForPlayerToSelectDice();

		for (int i = 0; i < N_DICE; i++) {
			if (display.isDieSelected(i)) {
				currentDiceResult[i] = rgen.nextInt(1, 6);
			}
		}

		// Displays roll dice result
		display.displayDice(currentDiceResult);

	}

	/**
	 * Manage the Category selected by user
	 */

	private void processCategory() {

		String currentPlayerName = playerNames[currentPlayer - 1];

		// Display current player turn message
		display.printMessage(currentPlayerName	+ ", Please select a category for this roll. ");

		// Repeat select a new category until it wasn't selected
		do {
			// Wait the user to select a category
			currentCategory = display.waitForPlayerToSelectCategory();

			// Display an error message about the category that was already
			// taken
			display.printMessage("You already picked that category. Please choose another category.");

		} while (categoryWasSelected());

		int score = getCategoryScore();
		// Display the current score into the score board
		display.updateScorecard(currentCategory, currentPlayer, score);

		storeScore(score);

	}

	/**
	 * Stores the Category score and recalculates the new score based on the
	 * results of the last player roll. 
	 */

	private void updateTotals() {

		int total = 0;

		int[] refElement = playersScoreList.get(currentPlayer - 1);

		for (int i = 1; i < TOTAL; i++) {

			// Calculate the total score
			if ((refElement[i] != NO_VALUE) && (i != UPPER_SCORE)
					&& (i != LOWER_SCORE)) {
				total += refElement[i];
			}
		}

		// Shows the grand-total for the current player
		refElement[TOTAL] = total;
		display.updateScorecard(TOTAL, currentPlayer, total);

	}

	/**
	 * Recalculates the middle totals and shows them in the score board
	 * 
	 */

	private void updateMiddleTotals() {

		for (int player = 1; player <= nPlayers; player++) {

			int[] refElement = playersScoreList.get(player - 1);

			int total = 0;
			int bonus = 0;

			int upperScore = 0;
			int lowerScore = 0;

			for (int i = 1; i < TOTAL; i++) {

				// Calculate the total score
				if ((refElement[i] != NO_VALUE) && (i != UPPER_SCORE)
						&& (i != LOWER_SCORE)) {

					total += refElement[i];

					// Calculate the upper score
					if (i < UPPER_SCORE) {
						upperScore += refElement[i];
					}

					// Calculate the lower score
					if ((i >= THREE_OF_A_KIND) && (i <= CHANCE)) {
						lowerScore += refElement[i];
					}

				}
			}

			// Show Middle totals if required

			refElement[UPPER_SCORE] = upperScore;
			display.updateScorecard(UPPER_SCORE, player, upperScore);

			if (upperScore > MIN_SCORE_FOR_BONUS) {
				bonus = UPPER_SCORE_BONUS;
				display.updateScorecard(UPPER_BONUS, player, bonus);
			} else {
				display.updateScorecard(UPPER_BONUS, player, 0);
			}

			refElement[LOWER_SCORE] = lowerScore;
			display.updateScorecard(LOWER_SCORE, player, lowerScore);

			// Shows the grand-total for the current player
			refElement[TOTAL] = total + bonus;
			display.updateScorecard(TOTAL, player, total + bonus);

		}

	}

	/**
	 * Displays the name of the winner of the game.
	 */
 
	private void showWinner() {

		int winner = 0;
		int winnerPoints = 0;

		for (int player = 1; player <= nPlayers; player++) {

			int[] refElement = playersScoreList.get(player - 1);
			if (refElement[TOTAL] > winnerPoints) {
				winnerPoints = refElement[TOTAL];
				winner = player;
			}

		}

		display.printMessage("The winner is: "	+ playerNames[winner - 1] + ". Total score: " + winnerPoints);

	}

	/**
	 * Update the new score stored into the array list
	 * 
	 * @param score
	 *            The new score.
	 */

	private void storeScore(int score) {
		// Gets the score array inside the array List
		int[] refElement = playersScoreList.get(currentPlayer - 1);

		// Assign the new score to the Category
		refElement[currentCategory] = score;

		// Update the new score into the array list
		playersScoreList.set(currentPlayer - 1, refElement);
	}

	/**
	 * Checks if the category was previously selected
	 * 
	 * @return True if the category was selected before else false
	 */

	private boolean categoryWasSelected() {

		int[] refElement = playersScoreList.get(currentPlayer - 1);
		return refElement[currentCategory] >= 0;
	}

	/**
	 * Calculate the score according to the category
	 * @return The score
	 */
 
	private int getCategoryScore() {
		int score = 0;

		switch (currentCategory) {

		case ONES:
			// The score will be the sum of the ONES
			if (checkCategory(currentDiceResult, ONES)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}

			break;

		case TWOS:
			// The score will be the sum of the TWOS
			if (checkCategory(currentDiceResult, TWOS)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}
			break;

		case THREES:
			// The score will be the sum of the THREES
			if (checkCategory(currentDiceResult, THREES)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}
			break;

		case FOURS:
			// The score will be the sum of the FOURS
			if (checkCategory(currentDiceResult, FOURS)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}
			break;

		case FIVES:
			// The score will be the sum of the FIVES
			if (checkCategory(currentDiceResult, FIVES)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}
			break;

		case SIXES:

			// The score will be the sum of the SIXES
			if (checkCategory(currentDiceResult, SIXES)) {
				for (int i = 0; i < N_DICE; i++) {
					if (currentDiceResult[i] == currentCategory) {
						score += currentDiceResult[i];
					}
				}
			}
			break;

		case THREE_OF_A_KIND:

			// The score will be the sum of the dice
			if (checkCategory(currentDiceResult, THREE_OF_A_KIND)) {
				for (int i = 0; i < N_DICE; i++) {
					score += currentDiceResult[i];
				}
			}
			break;

		case FOUR_OF_A_KIND:

			// The score will be the sum of the dice
			if (checkCategory(currentDiceResult, FOUR_OF_A_KIND)) {
				for (int i = 0; i < N_DICE; i++) {
					score += currentDiceResult[i];
				}
			}
			break;

		case CHANCE:
			// The score will be the sum of the dice
			if (checkCategory(currentDiceResult, CHANCE)) {
				for (int i = 0; i < N_DICE; i++) {
					score += currentDiceResult[i];
				}
			}
			break;

		case FULL_HOUSE:
			// The score will be  FULL_HOUSE_SCORE
			if (checkCategory(currentDiceResult, FULL_HOUSE)) {
				score = FULL_HOUSE_SCORE;
			}
			break;

		case SMALL_STRAIGHT:
			// The score will be  SMALL_STRAIGHT
			if (checkCategory(currentDiceResult, SMALL_STRAIGHT)) {
				score = SMALL_STRAIGHT_SCORE;
			}
			break;

		case LARGE_STRAIGHT:
			// The score will be  LARGE_STRAIGHT
			if (checkCategory(currentDiceResult, LARGE_STRAIGHT)) {
				score = LARGE_STRAIGHT_SCORE;
			}
			break;

		case YAHTZEE:
			// The score will be  YAHTZEE
			if (checkCategory(currentDiceResult, YAHTZEE)) {
				score = YAHTZEE_SCORE;
			}
			break;

		}

		return score;
	}

  
	/**
	 * Checks if the rolled dice math with the game categories
	 * 
	 * @param dice
	 *            The current dice result.
	 * @return true if the rolled dice math with the category, else false
	 */
	private boolean checkCategory(int[] dice, int category) {

		boolean result = false;

		// Define string for each value of dice

		int ones = 0;
		int twos = 0;
		int threes = 0;
		int fours = 0;
		int fives = 0;
		int sixes = 0;

		// Count the number of values to check the category
		for (int i = 0; i < dice.length; i++) {

			switch (dice[i]) {
			case 1:
				ones++;
				break;

			case 2:
				twos++;
				break;

			case 3:
				threes++;
				break;

			case 4:
				fours++;
				break;

			case 5:
				fives++;
				break;

			case 6:
				sixes++;
				break;

			}
		}

		switch (category) {

		case ONES:
		case TWOS:
		case THREES:
		case FOURS:
		case FIVES:
		case SIXES:
		case CHANCE:
			result = true;
			break;

		case THREE_OF_A_KIND:
			// Only 3 dice are the same
			result = (ones == 3) || (twos == 3)	|| (threes == 3) || (fours == 3) || (fives == 3) || (sixes == 3);
			break;

		case FOUR_OF_A_KIND:
			// Four dice are the same
			result = (ones == 4) || (twos == 4)	|| (threes == 4) || (fours == 4) || (fives == 4) || (sixes == 4);
			break;

		case FULL_HOUSE:
			// Four dice are the same plus two with other equal values 
			result = ((ones == 3) || (twos == 3) || (threes == 3) || (fours == 3) || (fives == 3) || (sixes == 3))
				  && ((ones == 2) || (twos == 2) || (threes == 2) || (fours == 2) || (fives == 2) || (sixes == 2));
			break;

		case SMALL_STRAIGHT:
			// Four dice are consecutive
			result = (
					 (   (ones == 1) && (twos == 1)	&& (threes == 1)  && (fours == 1)  )  
		          || (   (twos == 1) && (threes == 1)  && (fours == 1) && (fives == 1) ) 
		          || (   (threes == 1) || (fours == 1)	|| (fives == 1) || (sixes == 1))
		    );
			break;

		case LARGE_STRAIGHT:
			// Five dice are consecutive
			result = (((ones == 1) && (twos == 1) && (threes == 1) && (fours == 1) && (fives == 1)) 
				   || ((twos == 1) || (threes == 1) || (fours == 1) || (fives == 1) || (sixes == 1)));
			break;

		case YAHTZEE:
			// All dice have the same value
			result = (dice[0] == dice[1] && dice[1] == dice[2]	&& dice[2] == dice[3] && dice[3] == dice[4]);
			break;

		}

		return result;

	}
	 
	/**
	 * Generates five die results and stores them into an array
	 * 
	 * @return The array with five dice values.
	 */

	private int[] rollDice() {
		int[] currentDiceResult = new int[5];
		for (int i = 0; i < 5; i++) {
			currentDiceResult[i] = rgen.nextInt(1, 6);
		}
		return currentDiceResult;
	}

	/* Private instance variables */

	private int nPlayers = 0;
	private String[] playerNames;

	private int[] currentDiceResult;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

	private int currentPlayer;
	private int currentCategory;
	private IODialog dialog = getDialog();

	private ArrayList<int[]> playersScoreList = new ArrayList<int[]>();

}
