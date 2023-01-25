package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*

@one interface EventsService {
  var reposToListen: Set<Repo>
  val subscriptionsTopic: Topic<SubscriptionsMessage>
  val eventsTopic: Topic<EventsMessage>
}

context(ReflectedModule) val theEventsService: KSig<EventsService>
  get() = element<EventsService>()
context(ReflectedModule) val KSet<EventsService>.reposToListen: KSet<Repo>
  get() = (this / EventsService::reposToListen).flatten
context(ReflectedModule) val KSet<EventsService>.evsSubscriptions: KSet<Topic<SubscriptionsMessage>>
  get() = this / EventsService::subscriptionsTopic
context(ReflectedModule) val KSet<EventsService>.evsEvents: KSet<Topic<EventsMessage>>
  get() = this / EventsService::eventsTopic

sealed interface EventsAction: StateMachine {
  @initial object Initial : EventsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      no(theEventsService.reposToListen)
  }

  @stutter object Stutter : EventsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      stays(theEventsService.reposToListen)
  }

  data class ReceiveStartListen(val msg: KArg<StartListen>): EventsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      + theEventsService.evsSubscriptions.consume(msg)
      + (next(theEventsService.reposToListen) `==` (theEventsService.reposToListen + msg.subsRepo))
      + unchangedTopic(theEventsService.evsEvents)
    }
  }

  data class ReceiveStopListen(val msg: KArg<StopListen>): EventsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      + theEventsService.evsSubscriptions.consume(msg)
      + (next(theEventsService.reposToListen) `==` (theEventsService.reposToListen - msg.subsRepo))
      + unchangedTopic(theEventsService.evsEvents)
    }
  }

  data class GitHub(val repo: KArg<Repo>, val event: KArg<Event>): EventsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      + stays(theEventsService.reposToListen)
      + unchangedTopic(theEventsService.evsSubscriptions)
      + (repo `in` theEventsService.reposToListen).ifThen(
        ifTrue = forSome<EventsMessage> { m -> and {
          + (m.eventsEvent `==` event)
          + (m.eventsRepo `==` repo)
          + theEventsService.evsEvents.send(m)
        } },
        ifFalse = unchangedTopic(theEventsService.evsEvents)
      )
    }
  }
}