package com.petersavitsky.ff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Schedule {

	private final Map<Integer, Week> matchupsByWeek = new HashMap<>();
	private final Map<Owner, Map<Integer,Matchup>> matchupsByTeam = new HashMap<>();
	private final Map<Matchup, Integer> matchupCount = new HashMap<>();
	private final Map<Owner, Map<Matchup, Integer>> firstMeetingMatchupCount = new HashMap<>();
	//private final Set<Matchup> allPossibleMatchups = new HashSet<>();
	private final int matchupsPerWeek;
	private final int numWeeks;
	private final int firstMeetingByWeek = 11;
	private final int maxOccurencesOfMatchup;
	private final int maxNumOfMaxOccurences;
	private final int firstMeetingMaxOccurencesOfMatchup;
	private final int firstMeetingMaxNumOfMaxDuplicatesPerTeam;
	
	public Schedule(int numberOfWeeks, List<Owner> teams) {
		for (Owner team : teams) {
			Map<Integer,Matchup> teamSpecificMatchupsByWeek = new HashMap<>();
			matchupsByTeam.put(team, teamSpecificMatchupsByWeek);
			Map<Matchup, Integer> teamMatchupCount = new HashMap<>();
			firstMeetingMatchupCount.put(team, teamMatchupCount);
		}
		//this.allPossibleMatchups.addAll(allPossibleMatchups);
		numWeeks = numberOfWeeks;
		matchupsPerWeek = teams.size() / 2;
		for (int i = 1; i <= numWeeks; i++) {
			matchupsByWeek.put(i, new Week(i));
		}
		// note this assumes an even number of teams
		int matchupCombinations = matchupsPerWeek * (teams.size() - 1);
		int maxNumOfMaxOccurencesModulo = (numWeeks * matchupsPerWeek) % matchupCombinations;
		if (maxNumOfMaxOccurencesModulo == 0) {
			maxOccurencesOfMatchup = numWeeks / teams.size();
			maxNumOfMaxOccurences = teams.size();
		} else {
			maxOccurencesOfMatchup = (numWeeks / teams.size()) + 1;
			maxNumOfMaxOccurences = maxNumOfMaxOccurencesModulo;
		}
		
		/*
		 * Same thing but for first meetings
		 */
		// note this assumes an even number of teams
		int firstMeetingMaxNumOfMaxOccurencesModulo = (firstMeetingByWeek * matchupsPerWeek) % matchupCombinations;
		if (firstMeetingMaxNumOfMaxOccurencesModulo == 0) {
			firstMeetingMaxOccurencesOfMatchup = numWeeks / teams.size();
			firstMeetingMaxNumOfMaxDuplicatesPerTeam = teams.size();
		} else {
			firstMeetingMaxOccurencesOfMatchup = (numWeeks / teams.size()) + 1;
			firstMeetingMaxNumOfMaxDuplicatesPerTeam = firstMeetingMaxNumOfMaxOccurencesModulo / matchupsPerWeek;
		}
	}
	
	public synchronized void addMatchupToSchedule(Matchup matchup, int week) {
		matchupsByWeek.get(week).addMatchup(matchup);
		for (Owner team : matchup.getTeams()) {
			matchupsByTeam.get(team).put(week, matchup);
			Integer numFirstMeetingDuplicates = firstMeetingMatchupCount.get(team).get(matchup);
			if (numFirstMeetingDuplicates == null) {
				firstMeetingMatchupCount.get(team).put(matchup, 1);
			} else {
				firstMeetingMatchupCount.get(team).put(matchup, numFirstMeetingDuplicates + 1);
			}
		}
		Integer occurences = matchupCount.get(matchup);
		if (occurences == null) {
			matchupCount.put(matchup, 1);
		} else {
			matchupCount.put(matchup, occurences + 1);
		}
	}
	
	public synchronized boolean isMatchupValid(Matchup matchup, int week) {
		
		/*
		 * is there any space this week?
		 */
		if (matchupsByWeek.get(week).getMatchups().size() >= matchupsPerWeek) {
			return false;
		}
		
		/*
		 *  check if owner has matchup this week
		 */
		for (Owner team : matchup.getTeams()) {
			if (matchupsByTeam.get(team).containsKey(week)) {
				return false;
			}
		}
		
		/*
		 *  check if the same matchup occurs last week or next week
		 */
		if (matchupsByWeek.get(week - 1) != null && matchupsByWeek.get(week - 1).hasMatchup(matchup)) {
			return false;
		} else if (matchupsByWeek.get(week + 1) != null && matchupsByWeek.get(week + 1).hasMatchup(matchup)) {
			return false;
		}
		
		/*
		 *  this matchup has occured too many times
		 */
		if (matchupCount.get(matchup) != null && matchupCount.get(matchup) >= maxOccurencesOfMatchup) {
			return false;
		}
		
		/*
		 *  has the max number of max matchup occurences been hit?
		 */
		int maxOccurences = 0;
		for (Integer occurences : matchupCount.values()) {
			if (occurences >= maxOccurencesOfMatchup) {
				maxOccurences++;
			}
		}
		if (maxOccurences >= maxNumOfMaxOccurences) {
			return false;
		}
		
		/*
		 * Here's our 'Everybody love everybody' before week 11 rule
		 */
		int firstMeetingMaxDuplicates = 0;
		for (Owner team : matchup.getTeams()) {
			Map<Matchup, Integer> teamMatchupCount = firstMeetingMatchupCount.get(team);
			for (Integer occurences : teamMatchupCount.values()) {
				if (occurences >= firstMeetingMaxOccurencesOfMatchup) {
					firstMeetingMaxDuplicates++;
				}
			}
			if (firstMeetingMaxDuplicates >= firstMeetingMaxNumOfMaxDuplicatesPerTeam) {
				return false;
			}
		}
		
		/*
		 *  matchup is ok
		 */
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
	
	public int getNumMatchupsScheduled() {
		int matchups = 0;
		for (Week week : matchupsByWeek.values()) {
			matchups += week.numberOfMatchups();
		}
		return matchups;
	}
	
}
