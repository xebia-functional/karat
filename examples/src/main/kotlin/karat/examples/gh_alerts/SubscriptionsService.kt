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

context(ReflectedModule) val theService: KSig<SubscriptionsService>
  get() = element<SubscriptionsService>()
context(ReflectedModule) val KSet<SubscriptionsService>.db: KRelation<User, Repo>
  get() = (this / SubscriptionsService::database).asRelationSet
context(ReflectedModule) val KSet<SubscriptionsService>.subscriptionsTopic: KSet<Topic<SubscriptionsMessage>>
  get() = this / SubscriptionsService::subscriptionsTopic
context(ReflectedModule) val KSet<SubscriptionsService>.eventsTopic: KSet<Topic<EventsMessage>>
  get() = this / SubscriptionsService::eventsTopic
context(ReflectedModule) val KSet<SubscriptionsService>.notificationsTopic: KSet<Topic<NotificationsMessage>>
  get() = this / SubscriptionsService::notificationsTopic

sealed interface SubscriptionsAction: StateMachine {
  @initial object Initial : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      no(theService.db)
  }

  @stutter object Stutter : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula =
      stays(theService.db)
  }

  data class Put(val user: KArg<User>, val repo: KArg<Repo>) : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +(next(theService.db) `==` current(theService.db) + (user to repo))
      +forOne<StartListen> { m ->
        and {
          +(m.subsRepo `==` repo)
          +theService.subscriptionsTopic.send(m)
        }
      }
      +unchangedTopic(theService.eventsTopic)
      +unchangedTopic(theService.notificationsTopic)
    }
  }

  data class Delete(val user: KArg<User>, val repo: KArg<Repo>) : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +(next(theService.db) `==` current(theService.db) - (user to repo))
      +forNo<User> { u -> u `in` theService.db[u] }.ifThen(
        ifTrue = forOne<StopListen> { m ->
          and {
            +(m.subsRepo `==` repo)
            +theService.subscriptionsTopic.send(m)
          }
        },
        ifFalse = unchangedTopic(theService.subscriptionsTopic)
      )
      +unchangedTopic(theService.eventsTopic)
      +unchangedTopic(theService.notificationsTopic)
    }
  }

  object Event : SubscriptionsAction {
    context(ReflectedModule) override fun execute(): KFormula = and {
      +stays(theService.db)
      +forSome<EventsMessage> { e ->
        and {
          +theService.eventsTopic.consume(e)
          +forOne<NotificationsMessage> { n ->
            and {
              +(n.notsUsers `==` set<User>().suchThat { u -> e.eventsRepo `in` theService.db[u] })
              +(n.notsRepo `==` e.eventsRepo)
              +(n.notsEvent `==` e.eventsEvent)
              +theService.notificationsTopic.send(n)
            }
          }
        }
      }
    }
  }
}