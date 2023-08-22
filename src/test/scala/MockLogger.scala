import cats.effect.IO
import org.typelevel.log4cats.Logger

object MockLogger extends Logger[IO] {
  private var logCount: Int = 0
  def count: Int = logCount
  def reset(): Unit = logCount = 0

  override def info(message: => String): IO[Unit] =  IO { logCount += 1 }

  override def info(t: Throwable)(message: => String): IO[Unit] = IO ()
  override def error(t: Throwable)(message: => String): IO[Unit] = IO()
  override def warn(t: Throwable)(message: => String): IO[Unit] = IO()
  override def debug(t: Throwable)(message: => String): IO[Unit] = IO()
  override def trace(t: Throwable)(message: => String): IO[Unit] = IO()
  override def error(message: => String): IO[Unit] = IO()
  override def warn(message: => String): IO[Unit] = IO()
  override def debug(message: => String): IO[Unit] = IO()
  override def trace(message: => String): IO[Unit] = IO()
}