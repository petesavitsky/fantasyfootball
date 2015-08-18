package com.petersavitsky.ff.rules;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.petersavitsky.ff.Matchup;
import com.petersavitsky.ff.MatchupOccurence;
import com.petersavitsky.ff.Owner;
import com.petersavitsky.ff.Schedule;
import com.petersavitsky.ff.Week;

public class SchedulerTwo {

	private static final String[] teamNames = new String[] { "Team 1", "Team 2", "Team 3", "Team 4", "Team 5"
			,"Team 6", "Team 7", "Team 8", "Team 9", "Team 10"};
	// Number of weeks in the season
	private static final int NUM_WEEKS = 14; 
	// Maximum number of head to head matchups between 2 teams
	private static final int MAX_HEAD_TO_HEAD_MATCHUPS = 2;
	// Minimum number of weeks between head to head matchups
	private static final int MIN_WEEKS_BETWEEN_HEAD_TO_HEAD = 1;
	// All first time matchups must be done before week
	private static final int ALL_FIRST_MATCHUPS_BY_WEEK = 11;
	
	private static final String COMMA_DELIMITER = ",";
	
	private static final List<Owner> teams = new ArrayList<>();
	
	private static final int MATCHUPS_PER_WEEK = teamNames.length / 2;
	
	// Let's run this shit
	public static void main(String[] args) throws IOException {
		populateOwners();
		int maxMatchupsBeforeAllTeamsFirstMeeting = ALL_FIRST_MATCHUPS_BY_WEEK * MATCHUPS_PER_WEEK;
		List<Matchup> allMatchups = createMatchupCombinations();
		chooseMatchups(allMatchups, maxMatchupsBeforeAllTeamsFirstMeeting);
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
		List<Matchup> firstMatchups = new ArrayList<>(allMatchups);
		List<Matchup> secondMatchups = new ArrayList<>(allMatchups);
		Collections.shuffle(firstMatchups);
		Collections.shuffle(secondMatchups);
		int elementsFromSecondList = maxMatchupsBeforeFirstMeeting - firstMatchups.size();
		for (int i = 0; i < elementsFromSecondList; i++) {
			Matchup matchupToMove = secondMatchups.remove(i);
			firstMatchups.add(matchupToMove);
		}
		List<MatchupOccurence> matchupCountOne = createMatchupCountMap(firstMatchups);
		List<MatchupOccurence> matchupCountTwo = createMatchupCountMap(secondMatchups);
		Schedule schedule = new Schedule(NUM_WEEKS, teams);
		buildSchedule(schedule, matchupCountOne);
		buildSchedule(schedule, matchupCountTwo);
		return schedule;
	}
	
	private static List<MatchupOccurence> createMatchupCountMap(List<Matchup> matchups) {
		Map<Matchup,MatchupOccurence> matchupCount = new HashMap<>();
		for (Matchup matchup : matchups) {
			if (matchupCount.containsKey(matchup)) {
				matchupCount.get(matchup).incrementNumberOfOccurences();
			} else {
				matchupCount.put(matchup, new MatchupOccurence(matchup));
			}
		}
		List<MatchupOccurence> matchupOccurences = new ArrayList<>();
		matchupOccurences.addAll(matchupCount.values());
		Collections.sort(matchupOccurences, new MatchupOccurence.MatchupOccurenceComparator());
		return matchupOccurences;
	}
	
	private static void buildSchedule(Schedule schedule, Set<MatchupOccurence> matchups) throws IOException {
		// pick random weeks for dupe matchups
		// random fill in other weeks shoot for highest filled weeks first
		for (MatchupOccurence matchupOccurence : matchups) {
			for (int i = 1; i <= matchupOccurence.getNumberOfOccurences(); i++) {
				if (!scheduleMatchup(schedule, matchupOccurence.getMatchup())) {
					System.out.println("Couldn't schedule [" + i + "] time matchup [" + matchupOccurence.getMatchup() + "]");
				}
			}
		}
		System.out.println("Successfully scheduled everybody!");
	}
		
	private static boolean scheduleMatchup(Schedule schedule, Matchup matchup) {
		SortedSet<Week> weeks = schedule.getWeeks();
		for (Week week : weeks) {
			System.out.println("Scheduling for week [" + week + "]");
			if (schedule.isMatchupValid(matchup, week.getWeekNumber())) {
				schedule.addMatchupToSchedule(matchup, week.getWeekNumber());
				return true;
			}
		}
		return false;
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
						fileWriter.append("FAIL");
					}
				} else {
					fileWriter.append("FAIL");
				}
				fileWriter.append(COMMA_DELIMITER);
			}
			fileWriter.append("\n");
		}
		fileWriter.flush();
		fileWriter.close();
	}
	
	
}
