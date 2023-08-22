import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    for {
      _ <- IO(println("How many playing card decks?"))
      decks <- IO(scala.io.StdIn.readInt())
      _ <- IO.println("Should cards be matched on: 1. Suit, 2. Value, or 3. Both?")
      matchChoice <- IO(scala.io.StdIn.readInt())
      result <- Game.make[IO](decks, matchChoice).play()
      _ <- IO(println(result))
    } yield ()
}
