package com.tvc.wish.product

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.UUID

private[product] object ActorsPool {
  private[this] val system = ActorSystem("MySystem")

  def retrieveActor = System.getProperty("classic") match {
    case "custom" => system.actorOf(Props[CustomWishProductFetcherActor].withDispatcher("fetch-dispatcher"), name = UUID.randomUUID().toString)
    case _ => system.actorOf(Props[LatestWishProductFetcherActor].withDispatcher("fetch-dispatcher"), name = UUID.randomUUID().toString)
  }

  def context = system
}