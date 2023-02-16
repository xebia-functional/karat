package karat.examples.gh_alerts

import karat.*
import karat.ast.*
import karat.reflection.*

interface User
interface Repo
interface Event

@abstract sealed interface SubscriptionsMessage {
  val repo: Repo
}
interface StartListen: SubscriptionsMessage
interface StopListen: SubscriptionsMessage

context(ReflectedModule) val KSet<SubscriptionsMessage>.subsRepo: KSet<Repo>
  get() = this / SubscriptionsMessage::repo

interface EventsMessage {
  val repo: Repo
  val event: Event
}

context(ReflectedModule) val KSet<EventsMessage>.eventsRepo: KSet<Repo>
  get() = this / EventsMessage::repo
context(ReflectedModule) val KSet<EventsMessage>.eventsEvent: KSet<Event>
  get() = this / EventsMessage::event

interface NotificationsMessage {
  val users: Set<User>
  val repo: Repo
  val event: Event
}

context(ReflectedModule) val KSet<NotificationsMessage>.notsUsers: KSet<User>
  get() = (this / NotificationsMessage::users).flatten
context(ReflectedModule) val KSet<NotificationsMessage>.notsRepo: KSet<Repo>
  get() = this / NotificationsMessage::repo
context(ReflectedModule) val KSet<NotificationsMessage>.notsEvent: KSet<Event>
  get() = this / NotificationsMessage::event
