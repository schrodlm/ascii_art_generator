package asciiart.core.filter

/** The direction a FlipFilter mirrors the art in. */
sealed trait Flip

object Flip {

  /** Turns the art upside down. */
  case object Vertical extends Flip

  /** Mirrors the art left to right. */
  case object Horizontal extends Flip
}
