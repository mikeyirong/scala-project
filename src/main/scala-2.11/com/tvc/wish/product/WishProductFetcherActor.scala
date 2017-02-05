package com.tvc.wish.product

import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.NameValuePair

import akka.actor.Actor
import tcrawler.LoggingSupport
import tcrawler.SimpleFetcher
import tcrawler.ContentFormatter
import com.alibaba.fastjson.JSON
import scala.collection.JavaConversions._
import com.alibaba.fastjson.JSONObject
import com.tvc.be.db.PersistenceFactory

case class fetch_task(url: String, headers: Array[Header], body: Array[NameValuePair])
case class fetch_response(url: String, headers: Array[Header], body: Array[NameValuePair], bin: Array[Byte])
case class merge_entity(entity: WishProductInfo)
class WishProductFetcherActor extends SimpleFetcher[WishProductInfo] with Actor with LoggingSupport with WishProductAdvanceFeaturesProvider {
  val URL = "https://www.wish.com/api/feed/get-filtered-feed"
  var ebean = PersistenceFactory.load("classpath:dbconfig.properties").getEbeanServer

  def receive = {
    case fetch_task(url, headers, body) => {
      this.fetch_post(url)(headers)(bin => {
        ActorsPool.retrieveActor ! fetch_response(url, headers, body, bin)
        ActorsPool.context.stop(self)
        List[WishProductInfo]()
      })(x => false)(x => List[WishProductInfo]())(body)
    }
    case fetch_response(url, headers, body, bin) => try {
      var json = JSON.parseObject(new String(bin)).getJSONObject("data")
      var next_offset = json.getInteger("next_offset")

      json.getJSONArray("products").map(_.asInstanceOf[JSONObject]).map(js => {
        val entity = new WishProductInfo()
        entity.small_picture = js.getString("small_picture")
        entity.feed_tile_text = js.getString("feed_tile_text")
        entity.id = js.getString("id")
        entity
      }).toList.foreach(x => ActorsPool.retrieveActor ! merge_entity(x))

      if (next_offset != null)
        /* next page */
        ActorsPool.retrieveActor ! fetch_task(URL, mkHeaders, Array[NameValuePair]( /* */
          new NameValuePair("count", "25"),
          /* */
          new NameValuePair("offset", next_offset + ""),
          /* */
          new NameValuePair("request_id", "tabbed_feed_latest"),
          /* */
          new NameValuePair("request_categories", "false"),

          /* */
          new NameValuePair("_buckets", ""),

          /* */
          new NameValuePair("_experiments", "")))
    } catch {
      case _: Throwable => ActorsPool.retrieveActor ! fetch_task(url, headers, body)
    } finally {
      ActorsPool.context.stop(self)
    }
    case merge_entity(entity) => {
      this.fetch_post("https://www.wish.com/api/product/get")(mkHeaders)(bin => {
        try {
          var json = JSON.parseObject(new String(bin)).getJSONObject("data").getJSONObject("contest")
          logger.info("Loading product details information")
          var ratingNode = json.getJSONObject("product_rating")
          entity.rating_num = ratingNode.getInteger("rating_count")
          entity.rating_star = ratingNode.getDouble("rating")
          entity.bought_num = json.getInteger("num_bought")

          ebean.find(classOf[WishProductInfo]).where.eq("id", entity.id).findUnique match {
            case null => ebean.save(entity)
            case _ => ebean.update(entity)
          }
        } catch {
          case _: Throwable => ActorsPool.retrieveActor ! merge_entity(entity)
        } finally {
          ActorsPool.context.stop(self)
        }
        List[WishProductInfo]()
      })(x => false)(x => List[WishProductInfo]())(Array[NameValuePair](
        /* cid */
        new NameValuePair("cid", entity.id),
        /* related_contest_count */
        new NameValuePair("related_contest_count", "9"),

        /* include_related_creator */
        new NameValuePair("include_related_creator", "false"),

        /* request_sizing_chart_info */
        new NameValuePair("request_sizing_chart_info", "true"),

        /* _buckets */
        new NameValuePair("_buckets", ""),

        /* _experiments */
        new NameValuePair("_experiments", "")))
    }
  }
}