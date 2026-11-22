package asciiart.core.filter

/** The direction a FlipFilter mirrors the picture in. */
sealed trait Flip

object Flip {

  /** Turns the picture upside down. */
  case object Vertical extends Flip

  /** Mirrors the picture left to right. */
  case object Horizontal extends Flip
}
