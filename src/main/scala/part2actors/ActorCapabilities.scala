package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App{

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello, there" // replying to a message
      case message: String => println(s"[$self] I have received message: $message")
      case number: Int => println(s"I have received number: $number")
      case SpecialMessage(content) => println(s"I have received a special message: $content")
      case SendMessageToYourSelf(content) =>
        self! content

      case SayHiTo(ref) => (ref ! "Hi")(self)
      case WirelessPhoneMessage(content,ref) => ref forward (content + "s")
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor],"simpleActor")

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  simpleActor ! "hello!"
  simpleActor ! 34242 // who is the sender?

  // In practice use case classes and case objects
  case class SpecialMessage(content: String)
  simpleActor ! SpecialMessage("some special contents")

  //2 -actors have information about their context and about themselves
  // context.self == `this` in OOP
  case class SendMessageToYourSelf(content: String)
  simpleActor ! SendMessageToYourSelf("I am an actor and I am proud of it")

  //3 - actors can reply to messages

  val alice = system.actorOf(Props[SimpleActor],"Alice")
  val bob = system.actorOf(Props[SimpleActor],"Bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  //4- dead letters
  alice ! "Hi" // reply to "me"

  //5 - forwarding messages
  case class WirelessPhoneMessage(content: String,ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) //no sender
}
