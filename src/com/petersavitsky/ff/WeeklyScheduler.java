package com.petersavitsky.ff;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class WeeklyScheduler {

	private static final String COMMA_DELIMITER = ",";
	private static final String[] teamNames = new String[] { "Team 1", "Team 2", "team 3", 
			"team 4", "team 5", "team 6", "team 7", "team 8", "team 9", "team 10"};
	private static final int NUMBER_OF_WEEKS = 14;
	private static final int WEEK_DEADLINE = 11;
	private static final List<Team> TEAMS = new ArrayList<>();
	private static ScheduleByWeek schedule;
	
	public static void main(String[] args) throws IOException {
		populateOwners();
		makeSchedule();
		validateSchedule();
		printSchedule();
	}
	
	private static void validateSchedule() {
		if (!schedule.isScheduleValid()) {
			throw new RuntimeException("Invalid schedule");
		}
	}
	
	private static List<WeekPossibility> rotate() {
		List<Team> teams1 = new ArrayList<>();
		List<Team> teams2 = new ArrayList<>();
		Random r = new Random();
		for (Team team : TEAMS) {
			if (r.nextInt(2) == 0 && teams1.size() < TEAMS.size() / 2 || teams2.size() == TEAMS.size()/2) {
				teams1.add(team);
			} else {
				teams2.add(team);
			}
		}
		Collections.shuffle(teams1);
		Collections.shuffle(teams2);
		return createSchedule(teams1, teams2);
	}
	
	private static List<WeekPossibility> createSchedule(List<Team> teams1, List<Team> teams2) {
		List<WeekPossibility> weeks = new ArrayList<>();
		for (int i = 1; i <= NUMBER_OF_WEEKS; i++) {
			WeekPossibility week = new WeekPossibility();
			for (int j = 0; j < teams1.size() && j < teams2.size(); j++) {
				Matchup matchup = new Matchup(teams1.get(j), teams2.get(j));
				week.addMatchup(matchup);
			}
			weeks.add(week);
			Team team1Shift = teams1.remove(1);
			Team team2Shift = teams2.remove(teams2.size() - 1);
			teams1.add(teams1.size(), team2Shift);
			teams2.add(0, team1Shift);
		}
		return weeks;
	}
	
	private static void populateOwners() {
		for (String teamName : teamNames) {
			Team team = new Team(teamName);
			TEAMS.add(team);
		}
	}
	
	private static void makeSchedule() throws IOException {
		schedule = new ScheduleByWeek(NUMBER_OF_WEEKS, WEEK_DEADLINE, new HashSet<>(TEAMS));
		List<WeekPossibility> firstSet = rotate();
		List<WeekPossibility> secondSet = rotate();
		System.out.println("first set " + firstSet);
		System.out.println("second set " + secondSet);
		int scheduledWeek = 1;
		int availableBonusWeeks = 2;
		Random random = new Random();
		while (scheduledWeek <= NUMBER_OF_WEEKS) {
			int bonusDecider = random.nextInt(2);
			if (bonusDecider == 1) {
				if (scheduledWeek > 1 && availableBonusWeeks > 0 || scheduledWeek > 11) {
					if(attemptToSchedule(secondSet, scheduledWeek)) {
						System.out.println("Scheduling bonus week [" + scheduledWeek + "]");
						scheduledWeek++;
						availableBonusWeeks--;
					}
				}
			} else {
				if(attemptToSchedule(firstSet, scheduledWeek)) {
					System.out.println("Scheduling normal week [" + scheduledWeek + "]");
					scheduledWeek++;
				}
			}
		}
		printSchedule();
	}
	
	private static boolean attemptToSchedule(List<WeekPossibility> weeks, int weekNumber) {
		Iterator<WeekPossibility> iter = weeks.iterator();
		while (iter.hasNext()) {
			WeekPossibility possibility = iter.next();
			if (schedule.isWeekPossibilityValid(possibility, weekNumber)) {
				schedule.addWeekPossibility(possibility, weekNumber);
				iter.remove();
				return true;
			}
		}
		return false;
	}
	
	private static void printSchedule() throws IOException {
		FileWriter fileWriter = new FileWriter("/Users/petersavitsky/fantasySchedule.csv");
		fileWriter.append("Owner");
		for (int i = 0; i < NUMBER_OF_WEEKS; i++) {
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("Week " + (i+1));
		}
		fileWriter.append("\n");
		for (Entry<Team, Map<Integer, Matchup>> entry : schedule.getMatchupsByTeam().entrySet()) {
			fileWriter.append(entry.getKey().getTeamName()).append(COMMA_DELIMITER);
			for(int i = 1; i <= entry.getValue().size(); i++) {
				Matchup matchup = entry.getValue().get(i);
				if (matchup != null) {
					Team opponent = matchup.getOpponent(entry.getKey());
					if (opponent != null) {
						fileWriter.append(opponent.getTeamName());
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
