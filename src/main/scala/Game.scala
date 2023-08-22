import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeError, toTraverseOps}
import org.typelevel.log4cats.Logger
import cats.syntax.flatMap._
import cats.syntax.functor._

case class Card(suit: String, value: String)

case class MatchChoiceException(msg: String) extends Exception(msg)

trait Game[F[_]] {
  def play(): F[String]
}

final private class LiveGame[F[_] : Sync](decks: Int, matchChoice: Int)(implicit F: Logger[F]) extends Game[F] {
  private val suits: Seq[String] = List("Spade", "Diamond", "Club", "Heart")
  private val values: Seq[String] = List("Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace")

  private val deck: Seq[Card] = for {
    suit <- suits
    value <- values
  } yield Card(suit, value)

  private val shuffledDeck: List[Card] = scala.util.Random.shuffle(List.fill(decks)(deck).flatten)

  private def matchCriteria(): F[(Card, Card) => Boolean] = matchChoice match {
    case 1 => Sync[F].pure((card1: Card, card2: Card) => card1.suit == card2.suit)
    case 2 => Sync[F].pure((card1: Card, card2: Card) => card1.value == card2.value)
    case 3 => Sync[F].pure((card1: Card, card2: Card) => card1.value == card2.value && card1.suit == card2.suit)
    case _ => Sync[F].raiseError(MatchChoiceException("Wrong choice for matchers."))
  }

  override def play(): F[String] = {
    val playerStacks: (List[Card], List[Card]) = shuffledDeck.splitAt(shuffledDeck.length / 2)

    var p1Score = 0
    var p2Score = 0

    matchCriteria().flatMap { criteriaFunction =>
      val gameLogic = playerStacks._1.zip(playerStacks._2).traverse {
        case (card1, card2) =>
          if (criteriaFunction(card1, card2)) {
            if (scala.util.Random.nextBoolean()) {
              F.info(s"MATCH! Player 1 win a SNAP. $card1 and $card2") >>
                Sync[F].pure(p1Score += 1)
            } else {
              F.info(s"Match! Player 2 win a SNAP. $card1 and $card2") >>
                Sync[F].pure(p2Score += 1)
            }
          } else F.info(s"No match: $card1 and $card2")
      }

      gameLogic.map { _ =>
        if (p1Score > p2Score) s"Player 1 wins, score: $p1Score : $p2Score"
        else if (p2Score > p1Score) s"Player 2 wins, score: $p1Score : $p2Score"
        else s"It's a draw, score: $p1Score : $p2Score"
      }
    } handleError {
      case e: MatchChoiceException => s"Game matcher choice error: ${e.msg}"
      case e: Throwable => s"Unknown error: ${e.getMessage}"
    }
  }
}

object Game {
  def make[F[_] : Sync : Logger](decks: Int, matchChoice: Int): Game[F] =
    new LiveGame[F](decks, matchChoice)
}
