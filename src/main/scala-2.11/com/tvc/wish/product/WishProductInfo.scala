package com.tvc.wish.product

import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import scala.beans.BeanProperty
import javax.persistence.Column
import tcrawler.Fetchable

@Entity
@Table(name = "wish_product")
class WishProductInfo extends Fetchable {
  @Id
  @BeanProperty
  var id: String = _

  @BeanProperty
  var small_picture: String = _

  @BeanProperty
  var feed_tile_text: String = _

  /** 销量 */
  @BeanProperty
  var bought_num: Int = _

  /** 产品打分 */
  @BeanProperty
  var rating_star: Double = _

  /** 评论数 */
  @BeanProperty
  var rating_num: Int = _

  def getIdentifier() = id
}