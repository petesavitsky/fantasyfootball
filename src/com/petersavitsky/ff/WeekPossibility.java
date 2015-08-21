package com.petersavitsky.ff;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WeekPossibility {
	
	private final Set<Matchup> matchups = new HashSet<>();
	private final Set<Team> teamsScheduled = new HashSet<>();
	boolean isValid = true;
	
	public WeekPossibility() { /* */}
	
	public WeekPossibility(WeekPossibility copy) {
		this.matchups.addAll(copy.getMatchups());
		this.teamsScheduled.addAll(copy.getTeamsScheduled());
	}
	
	public boolean isValid(Matchup matchup) {
		boolean valid = !matchups.contains(matchup);
		for (Team team : matchup.getTeams()) {
			valid = !teamsScheduled.contains(team) && valid;
		}
		valid = matchup.getTeams().size() == 2 && valid;
		return valid;
	}
	
	public boolean addMatchup(Matchup matchup) {
		boolean teamsAdded = true;
		for (Team team : matchup.getTeams()) {
			teamsAdded = teamsScheduled.add(team) && teamsAdded;
		}
		boolean matchupAdded = matchups.add(matchup);
		isValid = matchupAdded && teamsAdded && isValid;
		return matchupAdded;
	}
	
	public boolean removeMatchup(Matchup matchup) {
		teamsScheduled.removeAll(matchup.getTeams());
		return matchups.remove(matchup);
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

	public Set<Team> getTeamsScheduled() {
		return teamsScheduled;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isValid ? 1231 : 1237);
		result = prime * result + ((matchups == null) ? 0 : matchups.hashCode());
		result = prime * result + ((teamsScheduled == null) ? 0 : teamsScheduled.hashCode());
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
		WeekPossibility other = (WeekPossibility) obj;
		if (isValid != other.isValid)
			return false;
		if (matchups == null) {
			if (other.matchups != null)
				return false;
		} else if (!matchups.equals(other.matchups))
			return false;
		if (teamsScheduled == null) {
			if (other.teamsScheduled != null)
				return false;
		} else if (!teamsScheduled.equals(other.teamsScheduled))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Week [" + matchups + "]";
	}
	
	public static class WeekComparatorMostMatchupsFirstThenRandom implements Comparator<WeekPossibility> {

		@Override
		public int compare(WeekPossibility week1, WeekPossibility week2) {
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
