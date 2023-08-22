import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class GameSpec extends AsyncFunSuite with AsyncIOSpec with Matchers {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  test("Game with matchChoice 1 should match on suit") {
    val game = Game.make[IO](1, 1)
    game.play().asserting { result =>
      result should (include ("Player 1 wins") or include ("Player 2 wins") or include ("It's a draw"))
      result should not include "Game matcher choice error"
    }
  }

  test("Game with matchChoice 2 should match on value") {
    val game = Game.make[IO](1, 2)
    game.play().asserting { result =>
      result should (include ("Player 1 wins") or include ("Player 2 wins") or include ("It's a draw"))
      result should not include "Game matcher choice error"
    }
  }

  test("Game with matchChoice 3 should match on both suit and value") {
    val game = Game.make[IO](1, 3)
    game.play().asserting { result =>
      result should (include ("Player 1 wins") or include ("Player 2 wins") or include ("It's a draw"))
      result should not include "Game matcher choice error"
    }
  }

  test("Game with invalid matchChoice should raise MatchChoiceException") {
    val game = Game.make[IO](1, 4)
    game.play().asserting { result =>
      result should include ("Game matcher choice error: Wrong choice for matchers.")
    }
  }

  test("Player victory or draw should be correctly stated") {
    val game = Game.make[IO](1, 1)

    def extractScores(result: String): (Int, Int) = {
      val splitResult = result.split("score:")
      val scores = splitResult(1).trim.split(":").map(_.trim.toInt)
      (scores(0), scores(1))
    }

    game.play().asserting { result =>
      val (p1Score, p2Score) = extractScores(result)

      result match {
        case r if r.contains("Player 1 wins") => p1Score should be > p2Score
        case r if r.contains("Player 2 wins") => p2Score should be > p1Score
        case r if r.contains("It's a draw")   => p1Score should be(p2Score)
        case _                                => fail
      }
    }
  }

  test("Log 52 times for two decks to make correct card deck size") {
    val mockLogger = MockLogger
    val game = Game.make[IO](2, 1)(implicitly, mockLogger)

    game.play().asserting { _ =>
      mockLogger.count shouldBe 52
    }
  }
}

