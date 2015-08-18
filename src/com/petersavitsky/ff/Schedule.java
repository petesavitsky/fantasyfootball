package com.petersavitsky.ff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Schedule {

	private final Map<Integer, Week> matchupsByWeek = new HashMap<>();
	private final Map<Owner, Map<Integer,Matchup>> matchupsByTeam = new HashMap<>();
	private final int matchupsPerWeek;
	private final int numWeeks;
	
	public Schedule(int numberOfWeeks, List<Owner> teams) {
		for (Owner team : teams) {
			Map<Integer,Matchup> teamSpecificMatchupsByWeek = new HashMap<>();
			matchupsByTeam.put(team, teamSpecificMatchupsByWeek);
		}
		numWeeks = numberOfWeeks;
		matchupsPerWeek = teams.size() / 2;
		for (int i = 1; i <= numWeeks; i++) {
			matchupsByWeek.put(i, new Week(i));
		}
	}
	
	public synchronized void addMatchupToSchedule(Matchup matchup, int week) {
		matchupsByWeek.get(week).addMatchup(matchup);
		for (Owner team : matchup.getTeams()) {
			matchupsByTeam.get(team).put(week, matchup);
		}
		System.out.println("Added week " + week + " matchup " + matchup);
	}
	
	public synchronized Week getRandomWeek() {
		Random r = new Random();
		return matchupsByWeek.get(r.nextInt(numWeeks));
	}
	
	public synchronized boolean isMatchupValid(Matchup matchup, int week) {
		
		//is there any space this week?
		if (matchupsByWeek.get(week).getMatchups().size() >= matchupsPerWeek) {
			System.out.println("Week is already full");
			return false;
		}
		// check if owner has matchup this week
		for (Owner team : matchup.getTeams()) {
			if (matchupsByTeam.get(team).containsKey(week)) {
				System.out.println("Owner [" + team + "] already has a matchup in week [" + week + "]");
				return false;
			}
		}
		// check if the same matchup occurs last week or next week
		if (matchupsByWeek.get(week - 1) != null && matchupsByWeek.get(week - 1).hasMatchup(matchup)) {
			System.out.println("Matchup [" + matchup + "] occured last week [" + ( week - 1) + "]");
			return false;
		} else if (matchupsByWeek.get(week + 1) != null && matchupsByWeek.get(week + 1).hasMatchup(matchup)) {
			System.out.println("Matchup [" + matchup + "] is occuring next week [" + ( week + 1) + "]");
			return false;
		}
		
		// matchup is ok
		System.out.println("Matchup [" + matchup + "] is ok for week [" + week + "]");
		return true;
	}
	
	public int getNumWeeks() {
		return numWeeks;
	}

	public Map<Integer, Week> getMatchupsByWeek() {
		return matchupsByWeek;
	}

	public Map<Owner, Map<Integer, Matchup>> getMatchupsByTeam() {
		return matchupsByTeam;
	}
	
	public SortedSet<Week> getWeeks() {
		SortedSet<Week> weeks = new TreeSet<>(new Week.WeekComparatorMostMatchupsFirstThenRandom());
		weeks.addAll(matchupsByWeek.values());
		return weeks;
	}
	
}
