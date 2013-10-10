package nl.malienkolders.htm.admin.lib.exporter

import java.io.PrintWriter
import nl.malienkolders.htm.lib.model.Tournament
import nl.malienkolders.htm.admin.lib.FightExporter

object CsvFightExporter extends FightExporter {

  def doExport {
    implicit def longToString(l: Long) = l.toString()
    implicit def intToString(i: Int) = i.toString()

    val ts = Tournament.findAll.toList
    val out = new PrintWriter("export.csv", "UTF-8")
    val separator = ","
    out.println(List("Tournament", "Round", "Poule", "RedId", "RedName", "BlueId", "BlueName", "RedPoint", "RedAfterblow", "BluePoint", "BlueAfterblow", "DoubleHits", "ExchangeCount", "Duration").mkString(separator))
    for {
      t <- ts
      r <- t.rounds
      p <- r.pools
      f <- p.fights
      red <- f.fighterA.obj
      blue <- f.fighterB.obj
      s <- Some(f.currentScore)
    } {
      out.println(
        List[String](
          t.name.is,
          r.name.is,
          p.order.is,
          red.externalId.is,
          red.name.is,
          blue.externalId.is,
          blue.name.is,
          s.a,
          s.aAfter,
          s.b,
          s.bAfter,
          s.double,
          s.exchangeCount,
          f.netDuration.is).map(_.replace("\"", "\"\"")).map("\"" + _ + "\"").mkString(separator))
    }
    out.close()
  }

  
}