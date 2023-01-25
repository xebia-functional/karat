package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*
import karat.ui.visualize

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

sealed interface SubscriptionsAction: StateMachine {
  @initial object Initial : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      no(theSubsService.db)
  }

  @stutter object Stutter : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      stays(theSubsService.db)
  }

  data class Put(val user: KArg<User>, val repo: KArg<Repo>) : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
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
  }

  data class Delete(val user: KArg<User>, val repo: KArg<Repo>) : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
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
  }

  object Event : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
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
}