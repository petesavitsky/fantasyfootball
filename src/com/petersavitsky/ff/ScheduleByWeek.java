package com.petersavitsky.ff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScheduleByWeek {

	private final Map<Integer, Week> weeks = new HashMap<>();
	private final int numberOfWeeks;
	private final int firstMatchupByWeek;
	private final Set<Team> teams = new HashSet<>();
	private final Set<Matchup> allPossibleMatchups = new HashSet<>();
	private final Map<Team, Map<Integer,Matchup>> matchupsByTeam = new HashMap<>();
	private final Map<Matchup, Integer> matchupCount = new HashMap<>();
	
	public ScheduleByWeek(int numberOfWeeks, int firstMatchupByWeek, Set<Team> teams) {
		this.numberOfWeeks = numberOfWeeks;
		this.firstMatchupByWeek = firstMatchupByWeek;
		this.teams.addAll(teams);
		createMatchupCombinations(teams);
	}
	
	private List<Matchup> createMatchupCombinations(Set<Team> teams) {
		List<Team> teamsList = new ArrayList<>(teams);
		for (Team team : teamsList) {
			matchupsByTeam.put(team, new HashMap<>());
		}
		List<Matchup> matchups = new ArrayList<>();
		for (int i = 0; i < teams.size() - 1; i++) {
			for (int j = i+1; j < teams.size(); j++) {
				Matchup matchup = new Matchup(teamsList.get(i), teamsList.get(j));
				matchups.add(matchup);
			}
		}
		return matchups;
	}
	
	public boolean isWeekPossibilityValid(WeekPossibility weekPossibility, int weekNumber) {
		if (weeks.get(weekNumber) != null) {
			return false;
		}
		
		if (weeks.get(weekNumber - 1) != null) {
			Week lastWeek = weeks.get(weekNumber - 1);
			for (Matchup matchup : weekPossibility.getMatchups()) {
				if (lastWeek.getMatchups().contains(matchup)) {
					return false;
				}
			}
		}
		
		int maxMatchups = (numberOfWeeks / teams.size()) + (numberOfWeeks % teams.size() > 0 ? 1 : 0);
		for (Matchup matchup : weekPossibility.getMatchups()) {
			if (matchupCount.containsKey(matchup) && (matchupCount.get(matchup) >= maxMatchups)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isScheduleValid() {
		//TODO clean this up to use rule classes
		
		// make sure that all matchups happen at least once by a given week
		Set<Matchup> firstMatchupsRemaining = new HashSet<>(allPossibleMatchups);
		for (int i = 1; i <= firstMatchupByWeek; i++) {
			Week currentWeek = weeks.get(i);
			if (currentWeek == null) {
				return false;
			}
			for (Matchup matchup : currentWeek.getMatchups()) {
				firstMatchupsRemaining.remove(matchup);
			}
		}
		if (!firstMatchupsRemaining.isEmpty()) {
			return false;
		}
		
		// make sure that every scheduled week is accounted for
		// also check that there aren't any duplicate matchups 
		// in back to back weeks
		Map<Matchup, Integer> matchupCount = new HashMap<>();
		for (int i = 1; i <= numberOfWeeks; i++) {
			if (weeks.get(i) == null) {
				return false;
			}
			for (Matchup matchup : weeks.get(i).getMatchups()) {
				Integer numMatchups = matchupCount.get(matchup);
				if (numMatchups == null) {
					matchupCount.put(matchup, 1);
				} else {
					matchupCount.put(matchup, numMatchups + 1);
				}
			}
			if (i == 1) {
				continue;
			}
			Week lastWeek = weeks.get(i - 1);
			Week thisWeek = weeks.get(i);
			if (lastWeek == null || thisWeek == null) {
				return false;
			}
			for (Matchup matchup : thisWeek.getMatchups()) {
				if (lastWeek.getMatchups().contains(matchup)) {
					return false;
				}
			}
		}
		
		int maxMatchups = (numberOfWeeks / teams.size()) + (numberOfWeeks % teams.size() > 0 ? 1 : 0);
		for (Integer numMatchups : matchupCount.values()) {
			if (numMatchups > maxMatchups) {
				return false;
			}
		}
			
		
		return true;
	}
	
	public Map<Team, Map<Integer,Matchup>> getMatchupsByTeam() {
		return matchupsByTeam;
	}
	
	public void addWeekPossibility(WeekPossibility possibleAdd, int weekNumber) {
		Week week = new Week(weekNumber);
		for (Matchup matchup : possibleAdd.getMatchups()) {
			week.addMatchup(matchup);
			weeks.put(weekNumber, week);
			Integer numMatchups = matchupCount.get(matchup);
			if (numMatchups == null) {
				matchupCount.put(matchup, 1);
			} else {
				matchupCount.put(matchup, numMatchups + 1);
			}
			for (Team team : matchup.getTeams()) {
				matchupsByTeam.get(team).put(weekNumber, matchup);
			}
		}
	}
}
