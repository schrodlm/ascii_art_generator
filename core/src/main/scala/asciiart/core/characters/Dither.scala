package asciiart.core.characters

/** How braille decides which dots to raise, so that areas of grey look grey on average. */
sealed trait Dither

object Dither {

  /** A dot is raised where its pixel is darker than mid-grey: sharp outlines, but no greys. */
  case object Threshold extends Dither

  /** Each pixel is compared with a repeating 4 by 4 pattern of thresholds: even textures, steady on video. */
  case object Ordered extends Dither

  /**
   * Each dot's rounding error is passed on to the pixels after it (Floyd–Steinberg): the most
   * natural for photos, but on video a small change shifts the dots after it, so they shimmer.
   */
  case object Diffusion extends Dither

  private val byName: Seq[(String, Dither)] = Seq("ordered" -> Ordered, "diffusion" -> Diffusion, "threshold" -> Threshold)

  /** The methods' names, in the order to offer them: the default, ordered, first. */
  val names: Seq[String] = byName.map(_._1)

  /** The method with this name, in any case, if there is one. */
  def named(name: String): Option[Dither] = byName.collectFirst { case (known, dither) if known == name.toLowerCase => dither }
}
