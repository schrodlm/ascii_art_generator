package asciiart.cli

import scala.util.Try

/** Asks the terminal the program runs in for its size. */
object Terminal {

  /**
   * The terminal's width in columns, or None when there is no terminal to ask
   * (no controlling terminal, or a system without `stty`).
   */
  def columns(): Option[Int] =
    Try {
      // Reads /dev/tty directly, so it works even when standard input is redirected.
      val process = new ProcessBuilder("sh", "-c", "stty size < /dev/tty").redirectErrorStream(true).start()
      val answer = new String(process.getInputStream.readAllBytes())
      if (process.waitFor() == 0) columnsFromSize(answer) else None
    }.toOption.flatten

  /** The columns from the answer of `stty size`, which is "rows columns". */
  def columnsFromSize(answer: String): Option[Int] =
    answer.trim.split(" ") match {
      case Array(_, columns) => columns.toIntOption.filter(_ > 0)
      case _ => None
    }
}
