package nl.malienkolders.htm.lib

import java.nio.charset.Charset

object EncodingHelpers {

  implicit val charset = "UTF-8"

  def encodeBase64(s: String)(implicit charset: String) =
    new sun.misc.BASE64Encoder().encode(s.getBytes(Charset.forName(charset)))

  def decodeBase64(s: String)(implicit charset: String) =
    new String(new sun.misc.BASE64Decoder().decodeBuffer(s), charset)

}