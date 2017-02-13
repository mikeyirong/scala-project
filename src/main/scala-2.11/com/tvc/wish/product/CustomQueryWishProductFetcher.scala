package com.tvc.wish.product

import org.apache.commons.httpclient.NameValuePair
import tcrawler.InternetIO
import tcrawler.JavaVirtualMachine._
import java.net.URLEncoder
import tcrawler.JQSupport
import tcrawler.HtmlContentFormatter
import tcrawler.ContentFormatter
import org.w3c.dom.Document
import tcrawler.LoggingSupport
import javax.script.ScriptEngineManager
import javax.script.ScriptContext
import scala.collection.JavaConversions._
object CustomQueryWishProductFetcher extends App with LoggingSupport with WishProductAdvanceFeaturesProvider with JQSupport {
  System.getProperties.put("classic", "custom")
  var query = "query".<::("phone accessories")
  var initialize: () => Unit = () => try {
    var bin = InternetIO.fromUrl("https://www.wish.com/search/" + URLEncoder.encode(query))
    var doc = ContentFormatter.apply(bin.getBytes)("text/html").format().asInstanceOf[Document]
    $(doc).select("script").filter(_.text.contains("pageParams['distinct_buckets']")).foreach(x => {
      var engine = new ScriptEngineManager().getEngineByExtension("js")
      var bindings = engine.createBindings()
      engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
      var repository = new java.util.ArrayList[String]()
      bindings.put("repository", repository)
      engine.eval("var pageParams = {};");
      engine.eval(x.text)
      engine.eval("var t= pageParams['orig_feed_items'];for(var i=0;i<t.length;i++) {repository.add(t[i].id)}")

      ActorsPool.retrieveActor ! fetch_task("https://www.wish.com/api/search", mkHeaders,
        repository.map(x => new NameValuePair("last_cids[]", x)).toList.:::(
          List(
            /* */
            new NameValuePair("transform", "true"),
            /* */
            new NameValuePair("_buckets", ""),
            /* */
            new NameValuePair("_experiments", ""),
            /* */
            new NameValuePair("query", query),
            /* */
            new NameValuePair("start", "25"))).toArray)
    })
  } catch {
    case e: Throwable => {
      logger.error("Initialize custom query with {} faild,system will be retry", query)
      initialize()
    }
  }

  initialize()
}