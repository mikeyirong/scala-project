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
      mkCookie(xsrf),
      /* */
      new Header("Origin", "https://www.wish.com"),

      /* */
      new Header("Referer", "https://www.wish.com"),

      /* */
      new Header("X-Requested-With", "XMLHttpRequest") /* End */ )
  }
}