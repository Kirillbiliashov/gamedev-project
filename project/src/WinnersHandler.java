package src;

import java.util.*;
import java.util.stream.Collectors;

public class WinnersHandler extends Handler {
  private int prevAllInSum;
  private List<Player> winners;
  private List<Player> unresolvedPlayers;
  private List<Player> activeUnresolvedPlayers;
  private int strongestHand;
  private int winnerMoney;

  public WinnersHandler(final Player[] players) {
    super(players);
    this.setFields();
  }

  public void handle() {
    final Player winner = winners.get(0);
    if (winner.getBalance() != 0) {
      this.allocSumToWinners(pot);
      return;
    }
    this.winnerMoney = winner.getMoneyInPot();
    final List<Player> loserPlayers = this.getLoserPlayers();
    final int lostAmount = this.getLostAmount(loserPlayers);
    final int activePlayersInPot = this.activePlayersInPotCount();
    final int winSum = (this.winnerMoney - this.prevAllInSum) *
        activePlayersInPot;
    this.allocSumToWinners(winSum + lostAmount);
    if (this.pot == 0) return;
    for (final Player lostPlayer : loserPlayers) lostPlayer.setResolved();
    winner.setResolved();
    this.setFields();
    this.handle();
  }

  private void setFields() {
    this.unresolvedPlayers = Arrays.asList(players).stream()
    .filter(Objects::nonNull)
    .filter(Player::isUnresolved)
    .collect(Collectors.toList());
    this.activeUnresolvedPlayers = unresolvedPlayers.stream()
    .filter(Objects::nonNull)
    .filter(Player::isActive)
    .collect(Collectors.toList());
    this.strongestHand = this.getStrongestHand();
    this.winners = this.getWinners();
    this.prevAllInSum = this.winnerMoney;
  }

  private List<Player> getWinners() {
    return activeUnresolvedPlayers.stream().filter(Objects::nonNull).filter(this::winnersFilter)
        .sorted(this::winnersSort).collect(Collectors.toList());
  }

  private boolean winnersFilter(final Player player) {
    return player.getCombination().ordinal() == strongestHand;
  }

  private int winnersSort(final Player w1, final Player w2) {
    return w1.getMoneyInPot() - w2.getMoneyInPot();
  }

  private List<Player> getLoserPlayers() {
    return unresolvedPlayers.stream()
    .filter(Objects::nonNull).filter(this::losersFilter).collect(Collectors.toList());
  }

  private boolean losersFilter(final Player player) {
    return player.getMoneyInPot() < winnerMoney && !winners.contains(player);
  }

  private int getStrongestHand() {
    return activeUnresolvedPlayers.stream().mapToInt(this::playerCombinationVal)
        .reduce(0, Math::max);
  }

  private int playerCombinationVal(final Player player) {
    return player.getCombination().ordinal();
  }

  private int getLostAmount(final List<Player> players) {
    final int totalLossAmount = players.stream().mapToInt(Player::getMoneyInPot)
        .reduce(0, Math::addExact);
    return totalLossAmount - this.prevAllInSum * players.size();
  }

  private int activePlayersInPotCount() {
    return unresolvedPlayers.stream().filter(this::playerMoneyInPotFilter)
        .toArray().length;
  }

  private boolean playerMoneyInPotFilter(final Player player) {
    return player.getMoneyInPot() >= winnerMoney;
  }

  private void allocSumToWinners(final int winSum) {
    final int winnersSize = this.winners.size();
    for (final Player winner : this.winners) {
      winner.changeBalance(winSum / winnersSize);
    }
    this.pot -= winSum;
  }

  public void setPotSize(final int potSize) {
    this.pot = potSize;
  }
}
