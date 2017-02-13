package com.tvc.wish.product

import org.apache.commons.httpclient.NameValuePair

import javax.persistence.Entity
import javax.persistence.Table

abstract class http_body_classic(mapping: Map[String, String]) {
  def createHttpBody: Array[NameValuePair] = mapping.map(item => new NameValuePair(item._1, item._2)).toArray
}

case class simple_pagination(page: Int, mapping: Map[String, String]) extends http_body_classic(mapping) {
  override def createHttpBody: Array[NameValuePair] =
    super.createHttpBody.toList.+:(new NameValuePair("offset", page + "")).toArray
}

case class wish_product_detail(product: WishProductInfo, mapping: Map[String, String]) extends http_body_classic(mapping) {
  override def createHttpBody: Array[NameValuePair] =
    super.createHttpBody.toList.+:(new NameValuePair("cid", product.product_id)).toArray
}

case class custom_pagination(page: Int, origins: List[String], mapping: Map[String, String]) extends http_body_classic(mapping) {
  override def createHttpBody: Array[NameValuePair] = {
    var list = super.createHttpBody.toList
    origins.map(x => new NameValuePair("last_cids[]", x)).foreach(x => list = list.::(x))
    list.toArray
  }
}

case class mapping_builder(classic: Any) {
  def create_mapping(fn: PartialFunction[Any, Map[String, String]]) = fn(classic)
}

case class custom_search(offset: Int, origins: List[String])

object HttpBodyFactory {
  def createHttpBody(r: Any)(mapping: Map[String, String]): Array[NameValuePair] = (r match {
    case page: Int => simple_pagination(page, mapping)
    case product: WishProductInfo => wish_product_detail(product, mapping)
    case custom_search(page, origins) => custom_pagination(page, origins, mapping)
  }).createHttpBody

  def createHttpBody(classic: Any, r: Any): Array[NameValuePair] = createHttpBody(r)(mapping_builder(classic).create_mapping({
    case "latest_list" => List("count" -> "25",
      /* */ "request_id" -> "tabbed_feed_latest",
      /* */ "request_categories" -> "false",
      /* */ "_buckets" -> "",
      /* */ "_experiments" -> "").toMap
    case "latest_detail" => List("related_contest_count" -> "9",
      /* */ "include_related_creator" -> "false",
      /* */ "request_sizing_chart_info" -> "true",
      /* */ "_buckets" -> "",
      /* */ "_experiments" -> "").toMap
    case "customize_list" :: (query :: (start :: origins)) => List("transform" -> "true",
      /* query */ "query" -> query.asInstanceOf[String],
      /* */ "_buckets" -> "",
      /* */ "start" -> start.toString,
      /* */ "_experiments" -> "").toMap
  }))
}