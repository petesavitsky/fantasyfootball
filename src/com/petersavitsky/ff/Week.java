package com.petersavitsky.ff;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Week {
	
	private final int weekNumber;
	private final Set<Matchup> matchups = new HashSet<>();
	private final Set<Team> teamsScheduled = new HashSet<>();
	
	public Week(int weekNumber) {
		this.weekNumber = weekNumber;
	}
	
	public boolean isValid(int numTeams) {
		return (teamsScheduled.size() == (numTeams/2)) && (matchups.size() == (numTeams/2));
	}
	
	public boolean addMatchup(Matchup matchup) {
		return matchups.add(matchup);
	}
	
	public boolean removeMatchup(Matchup matchup) {
		return matchups.remove(matchup);
	}

	public int getWeekNumber() {
		return weekNumber;
	}

	public Set<Matchup> getMatchups() {
		return Collections.unmodifiableSet(matchups);
	}
	
	public boolean hasMatchup(Matchup matchup) {
		return matchups.contains(matchup);
	}
	
	public int numberOfMatchups() {
		return matchups.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + weekNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Week other = (Week) obj;
		if (weekNumber != other.weekNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Week [weekNumber=" + weekNumber + ", matchups=" + matchups + "]";
	}
	
	public static class WeekComparatorMostMatchupsFirstThenRandom implements Comparator<Week> {

		@Override
		public int compare(Week week1, Week week2) {
			int difference = week1.numberOfMatchups() - week2.numberOfMatchups();
			if (difference == 0) {
				// 50/50 shot at ordering if 2 elements are the same
				Random random = new Random();
				return 1 - (random.nextInt(2) * 2);
			} else if (difference > 0) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}
	
}
