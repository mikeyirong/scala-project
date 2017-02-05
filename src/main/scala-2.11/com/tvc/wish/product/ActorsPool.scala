package com.tvc.wish.product

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.UUID

private[product] object ActorsPool {
  private[this] val system = ActorSystem("MySystem")
  private[this] val actor = system.actorOf(Props[WishProductFetcherActor], name = "wishActor")

  def retrieveActor = system.actorOf(Props[WishProductFetcherActor], name = UUID.randomUUID().toString)

  def context = system
}