package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*

@one interface SubscriptionsService {
  var database: Map<User, Set<Repo>>
  val subscriptionsTopic: Topic<SubscriptionsMessage>
  val eventsTopic: Topic<EventsMessage>
  val notificationsTopic: Topic<NotificationsMessage>
}

context(ReflectedModule) val theSubsService: KSig<SubscriptionsService>
  get() = element<SubscriptionsService>()
context(ReflectedModule) val KSet<SubscriptionsService>.db: KRelation<User, Repo>
  get() = (this / SubscriptionsService::database).asRelationSet
context(ReflectedModule) val KSet<SubscriptionsService>.subsSubscriptions: KSet<Topic<SubscriptionsMessage>>
  get() = this / SubscriptionsService::subscriptionsTopic
context(ReflectedModule) val KSet<SubscriptionsService>.subsEvents: KSet<Topic<EventsMessage>>
  get() = this / SubscriptionsService::eventsTopic
context(ReflectedModule) val KSet<SubscriptionsService>.subsNotifications: KSet<Topic<NotificationsMessage>>
  get() = this / SubscriptionsService::notificationsTopic

context(ReflectedModule) class SubscriptionsActions: StateMachineDefinition {
  override fun init(): KFormula = no(theSubsService.db)
  override fun stutter(): KFormula = stays(theSubsService.db)

  fun put(user: KArg<User>, repo: KArg<Repo>): KFormula = and {
    +(next(theSubsService.db) `==` current(theSubsService.db) + (user to repo))
    +forOne<StartListen> { m ->
      and {
        +(m.subsRepo `==` repo)
        +theSubsService.subsSubscriptions.send(m)
      }
    }
    +unchangedTopic(theSubsService.subsEvents)
    +unchangedTopic(theSubsService.subsNotifications)
  }

  fun delete(user: KArg<User>, repo: KArg<Repo>): KFormula = and {
    +(next(theSubsService.db) `==` current(theSubsService.db) - (user to repo))
    +forNo<User> { u -> u `in` theSubsService.db[u] }.ifThen(
      ifTrue = forOne<StopListen> { m ->
        and {
          +(m.subsRepo `==` repo)
          +theSubsService.subsSubscriptions.send(m)
        }
      },
      ifFalse = unchangedTopic(theSubsService.subsSubscriptions)
    )
    +unchangedTopic(theSubsService.subsEvents)
    +unchangedTopic(theSubsService.subsNotifications)
  }

  fun event() = and {
    +stays(theSubsService.db)
    +forSome<EventsMessage> { e ->
      and {
        +theSubsService.subsEvents.consume(e)
        +forOne<NotificationsMessage> { n ->
          and {
            +(n.notsUsers `==` set<User>().suchThat { u -> e.eventsRepo `in` theSubsService.db[u] })
            +(n.notsRepo `==` e.eventsRepo)
            +(n.notsEvent `==` e.eventsEvent)
            +theSubsService.subsNotifications.send(n)
          }
        }
      }
    }
  }
}