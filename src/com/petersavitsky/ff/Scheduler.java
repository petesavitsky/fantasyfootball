package com.petersavitsky.ff;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Generates a random schedule for head to head matchups
 * observing the given constraints
 * 
 * @author peter savitsky
 * @since 2014-08-18
 */
public class Scheduler {

	// owners x week
	static int[][] schedule = new int[10][14];
	// Map owners' IDs to names
	static Map<Integer,String> owners;
	// List of owners in the league
	// In the order that they finished last year
	static String[] ownersArray = new String[] { "Srev","Jack","Park","Krive","Mica",
		"GBaby","Josh","Afsh","Luca","Pete"};
	// Number of weeks in the season
	static int NUM_WEEKS = 14; 
	// Maximum number of head to head matchups between 2 teams
	static int MAX_HEAD_TO_HEAD_MATCHUPS = 2;
	// Minimum number of weeks between head to head matchups
	static int MIN_WEEKS_BETWEEN_HEAD_TO_HEAD = 1;
	// All first time matchups must be done before week
	static int ALL_FIRST_MATCHUPS_BY_WEEK = 11;
	// Random number generator
	static Random r = new Random();
	
	static FileWriter fileWriter;
	
	// Let's run this shit
	public static void main(String[] args) throws IOException {
		int overallAttempts = 0;
		resetSchedule();
		owners = createOwners();
		// loop through each owner for scheduling
		for (int week = 0; week < NUM_WEEKS; week++) {
			if (overallAttempts > 50000) {
				System.out.println("Game Over after [" + overallAttempts + "] attempts. Please play again.");
				break;
			}
			System.out.println("Matchups for Week " + (week + 1));
			// loop through each week for current owner for scheduling
			List<Integer> ownerList = new ArrayList<Integer>();
			ownerList.addAll(owners.keySet());
			// we've found a good matchup, let's put it on the calendar for 
			// both owners
			Set<Matchup> matchups;
			int attempts = 0;
			do {
				matchups = chooseMatchups(ownerList,week);
				attempts++;
			} while (matchups.size() < owners.size()/2 && attempts < 50);
			if (matchups.size() == owners.size()/2) {
				// it's a good week, let's put it in the books
				int counter = 0;
				for (Matchup matchup : matchups) {
					counter++;
					schedule[matchup.team1][week] = matchup.team2;
					schedule[matchup.team2][week] = matchup.team1;
					System.out.println("Matchup " + counter + " is: " + owners.get(matchup.team1) + " vs " + owners.get(matchup.team2));
				}
			} else {
				System.out.println("***********************************************");
				System.out.println("***********************************************");
				System.out.println("Shit be fucked for week " + week + "]. Starting over.");
				resetSchedule();
				System.out.println("***********************************************");
				System.out.println("***********************************************");
				week = -1;
				overallAttempts++;
				continue;
			}
			if (week == NUM_WEEKS -1 ) {
				for (int i = 0; i < schedule.length; i++) {
					Set<Integer> intSet = new HashSet<Integer>();
					for (int j = 0; j < schedule[i].length; j++) {
						intSet.add(schedule[i][j]);
					}
					if (intSet.size() != 9) {
						System.out.println("Schedule is fucked for " + owners.get(i));
						resetSchedule();
						week = -1;
						break;
					}
				}
			}
		}
		printSchedule();
	}
	
	public static void resetSchedule() {
		// because we're using 0 as an owner id, the default values of 
		// 0 in the array cause scheduling to fail. so default every value in the
		// array to -1. UGH.
		for (int i = 0; i < schedule.length; i++) {
			int[] weeksArray = schedule[i];
			for (int j = 0; j < weeksArray.length; j++) {
				schedule[i][j] = -1;
			}
		}
	}
	
	public static Set<Matchup> chooseMatchups(List<Integer> ownerSet, int week) {
		int counter = 0;
		Set<Integer> scheduledTeams = new HashSet<Integer>();
		Set<Matchup> matchups = new HashSet<Matchup>();
		// Here is where the random-ness happens
		// This is re-random-ized for every week
		Collections.shuffle(ownerSet);
		int leftPosition = 0;
		int rightPosition = leftPosition + 1;
		while (leftPosition < ownerSet.size() && rightPosition < ownerSet.size() && matchups.size() < owners.size()/2) {
			int team1 = ownerSet.get(leftPosition);
			int team2 = ownerSet.get(rightPosition);
			if (isOkToSchedule(team1,team2,week) && !scheduledTeams.contains(team1) && !scheduledTeams.contains(team2) && team1 != team2) {
				matchups.add(new Matchup(team1,team2));
				scheduledTeams.add(team1);
				scheduledTeams.add(team2);
				rightPosition = ++leftPosition;
				rightPosition++;
				counter++;
				continue;
			}
			rightPosition++;
			if (rightPosition >= ownerSet.size()) {
				leftPosition++;
				rightPosition=0;
			}
			if (team1 != team2) {
				counter++;
			}
		}
		System.out.println("Went through [" + counter + "] iterations");
		return matchups;
	}
	
	public static boolean isOkToSchedule(int owner, int opponent, int week) {
		return checkRules(owner,opponent,week) && checkRules(opponent,owner,week);
	}
	
	public static boolean checkRules(int owner, int opponent, int week) {
		
		int numHeadToHead = 0;
		int[] ownersSchedule = schedule[owner];
		List<Integer> headToHeadWeeks = new ArrayList<Integer>();
		for (int i = 0; i < ownersSchedule.length; i++) {
			if (ownersSchedule[i] == opponent) {
				numHeadToHead++; 
				headToHeadWeeks.add(i);
			}
		}
		// check to make sure we don't have too many head to head matchups
		if (numHeadToHead >= MAX_HEAD_TO_HEAD_MATCHUPS) {
			// we've already hit the maximum number of head to head matchups between these teams
			return false;
		}
		// check to make sure the matchups don't happen too closely to each other
		for (Integer matchupWeek : headToHeadWeeks) {
			if (matchupWeek > week && (matchupWeek - week) <= MIN_WEEKS_BETWEEN_HEAD_TO_HEAD) {
				// head to head matchups are too close together
				return false;
			}
			else if (matchupWeek < week && (week - matchupWeek) <= MIN_WEEKS_BETWEEN_HEAD_TO_HEAD) {
				// again, head to head matchup weeks are too close together
				return false;
			}
			else if (matchupWeek == week) {
				// teams are already playing each other this week, obviously no good
				return false;
			}
		}
		
		//check to make sure all owners play all other owners before a given week
		if (week == ALL_FIRST_MATCHUPS_BY_WEEK) {
			Set<Integer> ownersScheduledAgainst = new HashSet<>();
			for (int scheduledOpponent : schedule[owner]) {
				ownersScheduledAgainst.add(scheduledOpponent);
			}
			if(ownersScheduledAgainst.size() < ownersArray.length) {
				return false;
			}
		}
		
		// we've made it through all the restrictions,
		// this matchup is a go! Game on!
		return true;
	}
	
	public static Map<Integer,String> createOwners() {
		Map<Integer,String> owners = new HashMap<Integer,String>();
		for (int i = 0; i < ownersArray.length; i++) {
			owners.put(i, ownersArray[i]);
		}
		return owners;
	}

	public static void printSchedule() throws IOException {
		fileWriter = new FileWriter("/Users/petersavitsky/fantasySchedule.csv");
		write("Owner");
		for (int i = 0; i < NUM_WEEKS; i++) {
			write("Week " + (i+1));
		}
		writeNewLine();
		for (int i = 0; i < schedule.length; i++) {
			write(owners.get(i));
			int[] opponents = schedule[i];
			for (int j = 0; j < opponents.length; j++) {
				write(owners.get(opponents[j]));
			}
			writeNewLine();
		}
		fileWriter.flush();
		fileWriter.close();
	}
	
	private static void write(String tabbedField) throws IOException {
		System.out.print(tabbedField + "\t");
		fileWriter.append(tabbedField + ",");
	}
	
	private static void writeNewLine() throws IOException {
		System.out.print("\n");
		fileWriter.append("\n");
	}
}