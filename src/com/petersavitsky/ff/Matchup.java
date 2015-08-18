package com.petersavitsky.ff;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Matchup {
	
	private final SortedSet<Owner> teams;
	
	public Matchup(Owner team1, Owner team2) {
		SortedSet<Owner> teamSet = new TreeSet<>(new OwnerComparator());
		teamSet.add(team1);
		teamSet.add(team2);
		teams = Collections.unmodifiableSortedSet(teamSet);
	}

	public Set<Owner> getTeams() {
		return teams;
	}
	
	public Owner getOpponent(Owner team) {
		for (Owner opponent : teams) {
			if (!opponent.equals(team)) {
				return opponent;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Matchup [teams=" + teams + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((teams == null) ? 0 : teams.hashCode());
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
		Matchup other = (Matchup) obj;
		if (teams == null) {
			if (other.teams != null)
				return false;
		} else if (!teams.equals(other.teams))
			return false;
		return true;
	}
	
	private static class OwnerComparator implements Comparator<Owner> {

		// should always be alphabetically sorted by owner name
		@Override
		public int compare(Owner team1, Owner team2) {
			return team1.getOwnerName().compareTo(team2.getOwnerName());
		}
		
	}
	
}