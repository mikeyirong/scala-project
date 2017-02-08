package com.tvc.wish.product

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.UUID

private[product] object ActorsPool {
  private[this] val system = ActorSystem("MySystem")

  def retrieveActor = system.actorOf(Props[WishProductFetcherActor].withDispatcher("fetch-dispatcher"), name = UUID.randomUUID().toString)

  def context = system
}