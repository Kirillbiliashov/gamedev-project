import java.util.*;

public class WinnersHandler extends Handler {
  private int prevAllInSum;

  public WinnersHandler(final Player[] players, final int pot) {
    super(players);
    this.pot = pot;
  }

  public void handle() {
    final List<Player> unresolvedPlayers = Arrays.asList(players).stream().filter(Player::isUnResolved).toList();
    final List<Player> activeUnresolvedPlayers = unresolvedPlayers.stream().filter(Player::isActive).toList();
    final int strongestHand = getStrongestHand(activeUnresolvedPlayers);
    final List<Player> winners = getWinners(activeUnresolvedPlayers, strongestHand);
    final Player winner = winners.get(0);
    if (winner.getBalance() == 0) {
      final int winnerMoney = winner.getMoneyInPot();
      final List<Player> loserPlayers = getLoserPlayers(unresolvedPlayers, winners, winnerMoney);
      final int lostAmount = getLostAmount(loserPlayers);
      final int activePlayersInPot = getActivePlayersInPotSize(unresolvedPlayers, winnerMoney);
      final int winSum = (winnerMoney - prevAllInSum) * activePlayersInPot;
      allocWinSumToWinners(winners, winSum + lostAmount);
      if (pot == 0) return;
      for (final Player lostPlayer : loserPlayers) lostPlayer.setResolved();
      winner.setResolved();
      prevAllInSum = winnerMoney;
      handle();
    } else allocWinSumToWinners(winners, pot);
  }

  private List<Player> getWinners(final List<Player> players, final int strongestHand) {
    return players.stream().filter(player -> player.getCombination().ordinal() == strongestHand)
        .sorted((w1, w2) -> w1.getMoneyInPot() - w2.getMoneyInPot()).toList();
  }

  private List<Player> getLoserPlayers(final List<Player> players, final List<Player> winners, final int winnerMoney) {
    return players.stream().filter(player -> player.getMoneyInPot() < winnerMoney && winners.indexOf(player) == -1)
        .toList();
  }

  private int getStrongestHand(final List<Player> players) {
    return players.stream().mapToInt(player -> player.getCombination().ordinal()).reduce(0, Math::max);
  }

  private int getLostAmount(final List<Player> players) {
    return players.stream().mapToInt(Player::getMoneyInPot).reduce(0, Math::addExact) - prevAllInSum * players.size();
  }

  private int getActivePlayersInPotSize(final List<Player> players, final int winnerMoney) {
    return players.stream().filter(player -> player.getMoneyInPot() >= winnerMoney).toArray().length;
  }

  private void allocWinSumToWinners(final List<Player> winners, final int winSum) {
    final int winnersSize = winners.size();
    for (final Player winner : winners) winner.changeBalance(winSum / winnersSize);
    pot -= winSum;
  }
}
