package com.tvc.wish.product

import org.apache.commons.httpclient.NameValuePair

import akka.actor.actorRef2Scala
import tcrawler.SimpleFetcher

/**
 * Wish product feature
 */
object WishProductFetcher extends App with WishProductAdvanceFeaturesProvider {
  ActorsPool.retrieveActor ! fetch_task("https://www.wish.com/api/feed/get-filtered-feed", mkHeaders, Array[NameValuePair]())
}