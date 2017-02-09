package com.tvc.wish.product

import java.util.Date

import scala.beans.BeanProperty

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import tcrawler.Fetchable
import javax.persistence.CascadeType

@Entity
@Table(name = "wish_product")
class WishProductInfo extends Fetchable {
  @Id
  @BeanProperty
  var id: Int = _
  @BeanProperty
  var product_id: String = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var small_picture: String = _

  @BeanProperty
  var keywords: String = _
  
  @BeanProperty
  var shop_name: String = _

  @BeanProperty
  var feed_tile_text: String = _

  @BeanProperty
  var bought_num: Int = _

  @BeanProperty
  var rating_star: Double = _

  @BeanProperty
  var rating_num: Int = _

  @BeanProperty
  var fetch_at: String = _

  @OneToMany(targetEntity = classOf[WishProductVariationInfo], cascade = Array[CascadeType](CascadeType.ALL))
  @BeanProperty
  var variations: java.util.List[WishProductVariationInfo] = new java.util.ArrayList[WishProductVariationInfo]()
  def getIdentifier() = id + ""
}