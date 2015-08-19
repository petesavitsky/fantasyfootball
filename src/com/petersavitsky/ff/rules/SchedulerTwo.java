package com.petersavitsky.ff.rules;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import com.petersavitsky.ff.Matchup;
import com.petersavitsky.ff.Owner;
import com.petersavitsky.ff.Schedule;
import com.petersavitsky.ff.Week;

public class SchedulerTwo {

	private static final String[] teamNames = new String[] { "Srev","Jack","Park","Krive","Mica",
			"GBaby","Josh","Afsh","Luca","Pete"};
	/*{ "Team 1", "Team 2", "Team 3", "Team 4", "Team 5"
			,"Team 6", "Team 7", "Team 8", "Team 9", "Team 10"};*/
	// Number of weeks in the season
	private static final int NUM_WEEKS = 14; 
	// Maximum number of head to head matchups between 2 teams
	private static final int MAX_HEAD_TO_HEAD_MATCHUPS = 2;
	// Minimum number of weeks between head to head matchups
	private static final int MIN_WEEKS_BETWEEN_HEAD_TO_HEAD = 1;
	// All first time matchups must be done before week
	private static final int ALL_FIRST_MATCHUPS_BY_WEEK = 11;
	
	
	
	private static final List<Owner> teams = new ArrayList<>();
	
	private static final int MATCHUPS_PER_WEEK = teamNames.length / 2;
	
	private static int maxAttempts = 4;
	
	// Let's run this shit
	public static void main(String[] args) throws IOException {
		populateOwners();
		int maxMatchupsBeforeAllTeamsFirstMeeting = ALL_FIRST_MATCHUPS_BY_WEEK * MATCHUPS_PER_WEEK;
		List<Matchup> allMatchups = createMatchupCombinations();
		Schedule schedule = chooseMatchups(allMatchups, maxMatchupsBeforeAllTeamsFirstMeeting);
		printSchedule(schedule);
	}
	
	private static void populateOwners() {
		for (String teamName : teamNames) {
			Owner team = new Owner(teamName);
			teams.add(team);
		}
	}
	
	private static List<Matchup> createMatchupCombinations() {
		List<Matchup> matchups = new ArrayList<>();
		for (int i = 0; i < teams.size() - 1; i++) {
			for (int j = i+1; j < teams.size(); j++) {
				Matchup matchup = new Matchup(teams.get(i), teams.get(j));
				matchups.add(matchup);
			}
		}
		return matchups;
	}
	
	private static Schedule chooseMatchups(List<Matchup> allMatchups, int maxMatchupsBeforeFirstMeeting) throws IOException {
		LinkedList<Matchup> firstMatchups = new LinkedList<>(allMatchups);
		LinkedList<Matchup> secondMatchups = new LinkedList<>(allMatchups);
		Collections.shuffle(firstMatchups);
		Collections.shuffle(secondMatchups);
		int elementsFromSecondList = maxMatchupsBeforeFirstMeeting - firstMatchups.size();
		for (int i = 0; i < elementsFromSecondList; i++) {
			Matchup matchupToMove = secondMatchups.remove(i);
			firstMatchups.add(matchupToMove);
		}
		Collections.shuffle(firstMatchups);
		Collections.shuffle(secondMatchups);
		Schedule schedule = new Schedule(NUM_WEEKS, teams);
		System.out.println("First matchups " + firstMatchups.size());
		System.out.println("Second matchups " + secondMatchups.size());
		boolean firstMatchupsResult = buildSchedule(schedule, firstMatchups, 11, 1);
		System.out.println("First matchups success " + firstMatchupsResult);
		
		System.out.println("Second matchups success " + buildSchedule(schedule, secondMatchups, 14, 1));
		return schedule;
	}
	
	private static boolean buildSchedule(Schedule schedule, LinkedList<Matchup> matchups, int lastWeek, int depth) throws IOException {
		Matchup currentMatchup = null;
		SortedSet<Week> weeks = schedule.getWeeks();
		LinkedList<Matchup> traversedMatchups = new LinkedList<>();
		boolean success = false;
		do {
			currentMatchup = matchups.removeFirst();
			LinkedList<Matchup> remainingMatchups = new LinkedList<>(matchups);
			remainingMatchups.addAll(traversedMatchups);
			for (Week week : weeks) {
				LinkedList<Matchup> weeklyRemainingMatchups = new LinkedList<>(remainingMatchups);
				if (week.getWeekNumber() > lastWeek) {
					continue;
				}
				if (schedule.isMatchupValid(currentMatchup, week.getWeekNumber())) {
					schedule.addMatchupToSchedule(currentMatchup, week.getWeekNumber());
					System.out.println("Adding matchup [" + schedule.getNumMatchupsScheduled() + "]");
					success = buildSchedule(schedule, weeklyRemainingMatchups, lastWeek, 1);
					if (!success) {
						System.out.println("Removing matchup [" + schedule.getNumMatchupsScheduled() + "]");
						schedule.removeMatchupFromSchedule(currentMatchup, week.getWeekNumber());
					} else {
						return true;
					}
				}
			}
			traversedMatchups.add(currentMatchup);
			depth++;
			if (maxAttempts <= depth) {
				System.out.println("Hit max attempts");
			}
		} while (!success && !matchups.isEmpty() && depth < maxAttempts);
		return success;
	}
	
	public static void printSchedule(Schedule schedule) throws IOException {
		FileWriter fileWriter = new FileWriter("/Users/petersavitsky/fantasySchedule.csv");
		fileWriter.append("Owner");
		for (int i = 0; i < schedule.getNumWeeks(); i++) {
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Week " + (i+1));
		}
		fileWriter.append("\n");
		for (Entry<Owner, Map<Integer, Matchup>> entry : schedule.getMatchupsByTeam().entrySet()) {
			fileWriter.append(entry.getKey().getOwnerName()).append(COMMA_DELIMITER);
			for(int i = 1; i <= entry.getValue().size(); i++) {
				Matchup matchup = entry.getValue().get(i);
				if (matchup != null) {
					Owner opponent = matchup.getOpponent(entry.getKey());
					if (opponent != null) {
						fileWriter.append(opponent.getOwnerName());
					} else {
						fileWriter.append("no_opponent");
					}
				} else {
					fileWriter.append("no_matchup");
				}
				fileWriter.append(COMMA_DELIMITER);
			}
			fileWriter.append("\n");
		}
		fileWriter.flush();
		fileWriter.close();
	}
	
	
}
