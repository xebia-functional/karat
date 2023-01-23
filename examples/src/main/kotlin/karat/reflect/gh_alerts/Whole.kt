package karat.reflect.gh_alerts

import karat.*
import karat.ast.*
import karat.ui.visualize

@one data class Whole (
  val subscriptionsTopic: Topic<SubscriptionsMessage>,
  val eventsTopic: Topic<EventsMessage>,
  val notificationsTopic: Topic<NotificationsMessage>,
  // there's only one, but it's still handy to have it here
  val subscriptionsService: SubscriptionsService
) {
  companion object {
    fun InstanceFact<Whole>.linkedTopics(): KFormula = and {
      + (self / Whole::subscriptionsService / SubscriptionsService::subscriptionsTopic `==` self / Whole::subscriptionsTopic)
      + (self / Whole::subscriptionsService / SubscriptionsService::eventsTopic `==` self / Whole::eventsTopic)
      + (self / Whole::subscriptionsService / SubscriptionsService::notificationsTopic `==` self / Whole::notificationsTopic)
    }
  }
}

fun main() {
  execute {
    reflect(reflectAll = true,
      type<User>(), type<Repo>(), type<Event>(),
      type<SubscriptionsMessage>(), type<StartListen>(), type<StopListen>(),
      type<EventsMessage>(), type<NotificationsMessage>(),
      type<Topic<SubscriptionsMessage>>(), type<Partition<SubscriptionsMessage>>(),
      type<Topic<EventsMessage>>(), type<Partition<EventsMessage>>(),
      type<Topic<NotificationsMessage>>(), type<Partition<NotificationsMessage>>(),
      type<SubscriptionsService>(),
      type<Whole>()
    )
    reflectMachine<SubscriptionsAction>()
    facts {
      + singleEmptyPartition(element<Whole>() / Whole::subscriptionsTopic)
      + singleEmptyPartition(element<Whole>() / Whole::eventsTopic)
      + singleEmptyPartition(element<Whole>() / Whole::notificationsTopic)
    }
    run {
      eventually {
        some (theService.subscriptionsTopic)
      }
    }.visualize()
  }
}