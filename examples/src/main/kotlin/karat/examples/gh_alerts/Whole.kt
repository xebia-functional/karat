package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*
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

sealed interface WholeAction: StateMachine {
  @initial object Initial: WholeAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +one(element<Partition<SubscriptionsMessage>>())
      +singleEmptyPartition(element<Whole>() / Whole::subscriptionsTopic)
      +one(element<Partition<EventsMessage>>())
      +singleEmptyPartition(element<Whole>() / Whole::eventsTopic)
      +one(element<Partition<NotificationsMessage>>())
      +singleEmptyPartition(element<Whole>() / Whole::notificationsTopic)
    }
  }
  @stutter object Stutter: WholeAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +unchangedTopic(element<Whole>() / Whole::subscriptionsTopic)
      +unchangedTopic(element<Whole>() / Whole::eventsTopic)
      +unchangedTopic(element<Whole>() / Whole::notificationsTopic)
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
    reflectMachine(transitionSigName = "Action", type<WholeAction>(), type<SubscriptionsAction>())
    run {
      eventually {
        some (theService.subscriptionsTopic)
      }
    }.visualize()
  }
}