package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChangingActorBehavior.Mom.MomStart

object ChangingActorBehavior extends App{

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "SAD"
  }
  class FussyKid extends Actor{
    import FussyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) => {
        if(state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
      }
    }
  }

//  class StatelessFussyKid extends Actor {
//    import FussyKid._
//    import Mom._
//    override def receive: Receive = happyReceive
//    def happyReceive: Receive = {
//      case Food(VEGETABLE) => context.become(sadReceive,false)
//      case Food(CHOCOLATE) =>
//      case Ask(_) => sender() ! KidAccept
//    }
//    def sadReceive: Receive = {
//      case Food(VEGETABLE) =>
//      case Food(CHOCOLATE) => context.become(happyReceive,false)
//      case Ask(_) => sender() ! KidReject
//    }
//  }
  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
  }
}
  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._
    override def receive: Receive = {
      case MomStart(kidRef) => {
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")}

      case KidAccept => println("Yey, my kid is happy!")
      case KidReject => println("My kid is not happy, but at least he's healthy")
    }
  }

  val system = ActorSystem("changingActorBehaviorDemo")

  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom! MomStart(statelessFussyKid)

  /*
  mom receives MomStart
    kid receives Food(veg) -> kid will change the handler to sad receive
    kid receives Ask(play?) -> kid replies with the sadReceive handler
  mom receives kid reject
   */
  /*
  context.become
    Food(veg) -> stack.push(sadReceive)
    Food(choc) -> become happy receive

    Stack:
    1. happyReceive
    2. sadReceive
    3. happyReceive
   */
  /*
    new behavior
    Food(veg)
    Food(veg)
    Food(choc)
    Food(choc)
    Stack:
      2. happyReceive
   */
}
