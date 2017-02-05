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
      logger.error("Loading xsrf code faild, fetcher will retry",e)
      getXsrfCode
    }
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
      new Header("Cookie", "IR_PI=1483523447467-vn30vyys6yo1z; _xsrf=" + xsrf + "; sweeper_session=\"MTNiMjEwYTEtODA3ZC00ZTZmLWE0NzMtZjk5NmZjOWFiMGExMjAxNy0wMi0wNCAwMTo0NjowOC40MjQyNzg=|1486172768|ca295780038086462e1e0c4908585eabed413178\"; __utmt=1; __utma=96128154.495241984.1483523445.1486258675.1486261019.9; __utmb=96128154.1.10.1486261019; __utmc=96128154; __utmz=96128154.1484812743.4.3.utmcsr=wish.malllib.com|utmccn=(referral)|utmcmd=referral|utmcct=/wish/t/index; bsid=b90edef69a5b4a94b09076f4c6e06393; IR_EV=1486261022528%7C4953%7C0%7C1486261022528; sweeper_uuid=c2fcffe1f7814613ae620aa9901f67da"),

      /* */
      new Header("Origin", "https://www.wish.com"),

      /* */
      new Header("Referer", "https://www.wish.com"),

      /* */
      new Header("X-Requested-With", "XMLHttpRequest") /* End */ )
  }
}