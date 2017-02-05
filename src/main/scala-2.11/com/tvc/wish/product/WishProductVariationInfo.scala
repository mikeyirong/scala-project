package com.tvc.wish.product

import scala.beans.BeanProperty

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.CascadeType

@Entity
@Table(name = "wish_product_variation")
class WishProductVariationInfo {
  @BeanProperty
  @Id
  var id: Int = _

  @BeanProperty
  var variation_id: String = _

  @BeanProperty
  var original_price: String = _

  @ManyToOne(targetEntity = classOf[WishProductInfo], cascade = Array[CascadeType](CascadeType.ALL))
  @BeanProperty
  var wish_product: WishProductInfo = _
  @BeanProperty
  var shipping_price_country_code: String = _

  @BeanProperty
  var is_fulfill_by_wish: Boolean = _

  @BeanProperty
  var is_fulfill_by_wlc: Boolean = _

  @BeanProperty
  var color: String = _

  @BeanProperty
  var size_ordering: String = _

  @BeanProperty
  var min_fulfillment_time: String = _

  @BeanProperty
  var max_shipping_time: String = _

  @BeanProperty
  var price: String = _

  @BeanProperty
  var inventory: String = _
}