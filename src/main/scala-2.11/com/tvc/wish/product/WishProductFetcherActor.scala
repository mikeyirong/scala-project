package com.tvc.wish.product

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.JavaConversions._

import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.NameValuePair

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tvc.be.db.PersistenceFactory

import akka.actor.Actor
import tcrawler.LoggingSupport
import tcrawler.SimpleFetcher

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
        entity.product_id = js.getString("id")
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
          entity.fetch_at = new SimpleDateFormat("yyyy-MM-dd").format(new Date)
          entity.name = json.getString("name")
          entity.keywords = json.getString("keywords")
          //          entity.price = json.getString("price")
          //          entity.inventory = json.getString("inventory")
          try {
            entity.variations = json.getJSONObject("commerce_product_info").getJSONArray("variations").map(_.asInstanceOf[JSONObject]).map(x => {
              var variation = new WishProductVariationInfo
              variation.variation_id = x.getString("variation_id")
              variation.color = x.getString("color")
              variation.is_fulfill_by_wish = x.getBoolean("is_fulfill_by_wish")
              variation.is_fulfill_by_wlc = x.getBoolean("is_fulfill_by_wlc")
              variation.max_shipping_time = x.getString("max_shipping_time")
              variation.min_fulfillment_time = x.getString("min_fulfillment_time")
              variation.original_price = x.getString("original_price")
              variation.price = x.getString("price")
              variation.shipping_price_country_code = x.getString("shipping_price_country_code")
              variation.size_ordering = x.getString("size_ordering")
              variation.wish_product = entity
              variation.inventory = x.getString("inventory")
              variation
            }).toList
          } catch {
            case e: Throwable => e.printStackTrace()
          }

          ebean.find(classOf[WishProductInfo]).where.eq("fetch_at", new SimpleDateFormat("yyyy-MM-dd").format(new Date)).eq("product_id", entity.product_id).findUnique match {
            case null => {
              logger.info("SAVING...")
              ebean.save(entity)
            }
            case _ => {
              logger.info("Updating...")
              ebean.update(entity)
            }
          }
        } catch {
          case _: Throwable => ActorsPool.retrieveActor ! merge_entity(entity)
        } finally {
          ActorsPool.context.stop(self)
        }
        List[WishProductInfo]()
      })(x => false)(x => List[WishProductInfo]())(Array[NameValuePair](
        /* cid */
        new NameValuePair("cid", entity.product_id),
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