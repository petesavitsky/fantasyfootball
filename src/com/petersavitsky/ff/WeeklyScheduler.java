package com.petersavitsky.ff;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class WeeklyScheduler {

	private static final String COMMA_DELIMITER = ",";
	private static final String[] teamNames = new String[] { "Srev","Jack","Park","Krive","Mica",
			"GBaby","Josh","Afsh","Luca","Pete"};
	private static final int MATCHUPS_PER_WEEK = 5;
	private static final int NUMBER_OF_WEEKS = 14;
	private static final int WEEK_DEADLINE = 11;
	private static final int MAX_DUPLICATES_BEFORE_WEEK_DEADLINE = 10;
	private static final List<Owner> TEAMS = new ArrayList<>();
	private static final Set<WeekPossibility> WEEK_POSSIBILITY_SET = new HashSet<>();
	private static final List<WeekPossibility> WEEK_POSSIBILITY_LIST = new ArrayList<>();
	private static final List<Matchup> MATCHUPS = new ArrayList<>();
	private static Schedule schedule;
	
	public static void main(String[] args) throws IOException {
		populateOwners();
		createMatchups();
		shuffleMatchups();
		createWeekPossibilities();
		chooseWeeksOrder();
		printSchedule();
		System.out.println("Created [" + WEEK_POSSIBILITY_SET.size() + "] valid week combinations");
	}
	
	private static void populateOwners() {
		for (String teamName : teamNames) {
			Owner team = new Owner(teamName);
			TEAMS.add(team);
		}
		schedule = new Schedule(NUMBER_OF_WEEKS, TEAMS);
	}
	
	private static void createMatchups() {
		for (int i = 0; i < TEAMS.size() - 1; i++) {
			for (int j = i+1; j < TEAMS.size(); j++) {
				Matchup matchup = new Matchup(TEAMS.get(i), TEAMS.get(j));
				MATCHUPS.add(matchup);
			}
		}
	}
	
	private static void shuffleMatchups() {
		Collections.shuffle(MATCHUPS);
	}
	
	private static void createWeekPossibilities() {
		WeekPossibility week = new WeekPossibility();
		createWeek(MATCHUPS, week, 0);
		WEEK_POSSIBILITY_LIST.addAll(WEEK_POSSIBILITY_SET);
	}
	
	private static void createWeek(List<Matchup> matchups, WeekPossibility week, int depth) {
		if (depth >= MATCHUPS_PER_WEEK) {
			WEEK_POSSIBILITY_SET.add(week);
		}
		for (int i = 0; i < matchups.size(); i++) {
			WeekPossibility nextPossibility = new WeekPossibility(week);
			if (nextPossibility.isValid(matchups.get(i))) {
				nextPossibility.addMatchup(matchups.get(i));
				createWeek(matchups, nextPossibility, depth + 1);
			} else {
				continue;
			}
		}
	}
	
	private static void chooseWeeksOrder() {
		for (int i = 1; i <= NUMBER_OF_WEEKS; i++) {
			int weeksChecked = 0;
			Collections.shuffle(WEEK_POSSIBILITY_LIST);
			for (WeekPossibility weekPossibility : WEEK_POSSIBILITY_LIST) {
				System.out.println("Week [" + i + "] attempt [" + weeksChecked++ + "]");
				if (isWeekOkToSchedule(weekPossibility, i)) {
					for (Matchup matchup : weekPossibility.getMatchups()) {
						schedule.addMatchupToSchedule(matchup, i);
					}
					break;
				}
			}
		}
	}
	
	private static boolean isWeekOkToSchedule(WeekPossibility weekPossibility, int week) {
		for (Matchup matchup : weekPossibility.getMatchups()) {
			if (!schedule.isMatchupValid(matchup, week)) {
				return false;
			}
		}
		return true;
	}
	
	private static void printSchedule() throws IOException {
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
