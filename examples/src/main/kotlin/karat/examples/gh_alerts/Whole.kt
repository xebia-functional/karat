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
  val subscriptionsService: SubscriptionsService,
  val eventsService: EventsService
) {
  companion object {
    fun InstanceFact<Whole>.linkedTopics(): KFormula = and {
      + (self / Whole::subscriptionsService / SubscriptionsService::subscriptionsTopic `==` self / Whole::subscriptionsTopic)
      + (self / Whole::subscriptionsService / SubscriptionsService::eventsTopic `==` self / Whole::eventsTopic)
      + (self / Whole::subscriptionsService / SubscriptionsService::notificationsTopic `==` self / Whole::notificationsTopic)
      + (self / Whole::eventsService / EventsService::subscriptionsTopic `==` self / Whole::subscriptionsTopic)
      + (self / Whole::eventsService / EventsService::eventsTopic `==` self / Whole::eventsTopic)
    }
  }
}

context(ReflectedModule) class WholeActions: StateMachineDefinition {
  override fun init(): KFormula = and {
    +singleEmptyPartition(element<Whole>() / Whole::subscriptionsTopic)
    +singleEmptyPartition(element<Whole>() / Whole::eventsTopic)
    +singleEmptyPartition(element<Whole>() / Whole::notificationsTopic)
  }

  override fun stutter(): KFormula = and {
    +unchangedTopic(element<Whole>() / Whole::subscriptionsTopic)
    +unchangedTopic(element<Whole>() / Whole::eventsTopic)
    +unchangedTopic(element<Whole>() / Whole::notificationsTopic)
  }

  @stutterFor(SubscriptionsActions::class)
  fun stutterSubscription(): KFormula =
    Constants.TRUE

  @stutterFor(EventsActions::class)
  fun stutterEvent(): KFormula =
    unchangedTopic(element<Whole>() / Whole::notificationsTopic)
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
      type<SubscriptionsService>(), type<EventsService>(),
      type<Whole>()
    )
    facts {
      +one(element<Partition<SubscriptionsMessage>>())
      +one(element<Partition<EventsMessage>>())
      +one(element<Partition<NotificationsMessage>>())
    }
    reflectMachineFromMethods(WholeActions(), SubscriptionsActions(), EventsActions())
    run(overall = 10, steps = 2 .. 4, scopes = listOf(exactly<User>(1), exactly<Repo>(1))) {
      eventually {
        // some(element<Whole>() / Whole::eventsService / EventsService::reposToListen)
        some(theSubsService.db)
        // Constants.TRUE
      }
    }.visualize()
  }
}