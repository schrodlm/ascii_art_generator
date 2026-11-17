package asciiart.core.filter

import asciiart.core.image.Image

class IdentityFilter() extends Filter[Image]
{
  override def apply(image: Image): Image = image
}
