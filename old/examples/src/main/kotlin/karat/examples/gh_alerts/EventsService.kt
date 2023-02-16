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

context(ReflectedModule) class EventsActions: StateMachineDefinition {
  override fun init(): KFormula = no(theEventsService.reposToListen)
  override fun stutter(): KFormula = stays(theEventsService.reposToListen)

  fun receiveStartListen(msg: KArg<StartListen>): KFormula = and {
    + theEventsService.evsSubscriptions.consume(msg)
    + (next(theEventsService.reposToListen) `==` (theEventsService.reposToListen + msg.subsRepo))
    + unchangedTopic(theEventsService.evsEvents)
  }
  fun receiveStopListen(msg: KArg<StopListen>): KFormula = and {
    + theEventsService.evsSubscriptions.consume(msg)
    + (next(theEventsService.reposToListen) `==` (theEventsService.reposToListen - msg.subsRepo))
    + unchangedTopic(theEventsService.evsEvents)
  }
  fun github(repo: KArg<Repo>, event: KArg<Event>) = and {
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