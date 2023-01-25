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

sealed interface WholeAction: StateMachine {
  @initial object Initial: WholeAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +singleEmptyPartition(element<Whole>() / Whole::subscriptionsTopic)
      +singleEmptyPartition(element<Whole>() / Whole::eventsTopic)
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

  @stutterFor(SubscriptionsAction::class) object StutterSubscription: WholeAction {
    context(ReflectedModule) override fun execute(): KFormula = Constants.TRUE
  }

  @stutterFor(EventsAction::class) object StutterEvent: WholeAction {
    context(ReflectedModule) override fun execute(): KFormula =
      unchangedTopic(element<Whole>() / Whole::notificationsTopic)
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
      type<SubscriptionsService>(), type<EventsService>(),
      type<Whole>()
    )
    facts {
      +one(element<Partition<SubscriptionsMessage>>())
      +one(element<Partition<EventsMessage>>())
      +one(element<Partition<NotificationsMessage>>())
    }
    reflectMachine(transitionSigName = "Action", type<WholeAction>(), type<SubscriptionsAction>(), type<EventsAction>())
    run(overall = 10, steps = 2 .. 4, scopes = listOf(exactly<User>(1), exactly<Repo>(1))) {
      eventually {
        // some(element<Whole>() / Whole::eventsService / EventsService::reposToListen)
        some(theSubsService.db)
        // Constants.TRUE
      }
    }.visualize()
  }
}