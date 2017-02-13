package com.tvc.wish.product

import tcrawler.LoggingSupport
import tcrawler.InternetIO
import org.apache.commons.httpclient.Header

/**
 * Advancement feature provider
 */
trait WishProductAdvanceFeaturesProvider extends LoggingSupport {
  /**
   * Returns xsrf code from wish cookie
   */
  def getXsrfCode: String = try {
    var xsrf = ""
    InternetIO.rawfromUrl("https://www.wish.com", Array[Header](), cookie => {
      if (cookie.getName.equals("_xsrf")) xsrf = cookie.getValue
    })
    xsrf
  } catch {
    case e: Throwable => {
      logger.error("Loading xsrf code faild, fetcher will retry", e)
      getXsrfCode
    }
  }

  private def mkCookie(xsrf: String) = System.getProperties.getProperty("http.cookie") match {
    case cookie: String => new Header("Cookie", cookie + ";_xsrf=" + xsrf)
    case _ => throw new IllegalArgumentException("http.cookie must be configured")
  }
  /**
   * Make http headers
   */
  def mkHeaders = {
    val xsrf = getXsrfCode
    Array[Header](
      /* */
      new Header("X-XSRFToken", xsrf),
      /* */
      //new Header("Cookie", "483523447467-vn30vyys6yo1z; _xsrf=" + xsrf + "; sweeper_session=\"YjhjZjcxNjEtYmFkYy00ODAzLThkNjctNzVkMmJiNDlmNDMwMjAxNy0wMi0xMyAwMzozMDowOC41NDM5OTk=|1486956608|2f8576daaf62b7b3a890406197531318a473487f\"; __utmt=1; __utma=96128154.495241984.1483523445.1486691714.1486956560.20; __utmb=96128154.6.10.1486956560; __utmc=96128154; __utmz=96128154.1486621915.18.4.utmcsr=wiki.mindcenter.cn|utmccn=(referral)|utmcmd=referral|utmcct=/index.php/WISH%E4%BA%A7%E5%93%81%E8%B0%83%E7%A0%94%E6%8A%A5%E5%91%8A; bsid=8e6573d35d764e2d83a0d48ccc4c9e89; IR_EV=1486958348070%7C4953%7C0%7C1486956669650; sweeper_uuid=7b4b60827b5e45739dbb8c65952dcdf2"),
      mkCookie(xsrf),
      /* */
      new Header("Origin", "https://www.wish.com"),

      /* */
      new Header("Referer", "https://www.wish.com"),

      /* */
      new Header("X-Requested-With", "XMLHttpRequest") /* End */ )
  }
}