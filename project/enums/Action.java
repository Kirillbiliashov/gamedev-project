package enums;

import src.GameSession;
import src.Range;

public enum Action {

  FOLD(new Range(0, GameSession.MAX_FOLD_NUM)),
  CALL(new Range(GameSession.MAX_FOLD_NUM + 1, GameSession.MAX_CALL_NUM)),
  RAISE(new Range(GameSession.MAX_CALL_NUM + 1, GameSession.MAX_RAISE_NUM)),
  CHECK(new Range(GameSession.MAX_RAISE_NUM + 1, GameSession.MAX_CHECK_NUM));

  private Range range;

  private Action(final Range range) {
    this.range = range;
  }

  public Range getRange() {
    return this.range;
  }
}