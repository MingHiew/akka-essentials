package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActors extends App{

  object Parent {
    case class createChild(name: String)
    case class tellChild(msg: String)
  }

  class Parent extends Actor {
    import Parent._
    var child: ActorRef = null
    override def receive: Receive = {
      case createChild(name) => {
        println(s"${self.path} creating child")
        val chilRef = context.actorOf(Props[Child],name)
        context.become(withChild(chilRef))
      }

    }

    def withChild(childRef: ActorRef):Receive = {
      case tellChild(msg) =>
        if(childRef != null) childRef forward msg
    }
  }

  class Child extends Actor {
    def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }
  import  Parent._
  val system = ActorSystem("ParentChildDemo")
  val parent = system.actorOf(Props[Parent],"parent")
  parent ! createChild("child")
  parent ! tellChild("Hey kid")

  // actor hiearchies
  // parent -> child -> grandChild
  //        -> child2

  /*
  Guardian actors (top-level)
  -/system = system guardian
  -/user = user-level guardian
  -/ = the root guardian
   */

  /**
    * Actor selection
    */
  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  /**
    * Danger!
    *
    * NEVER PASS MUTABLE ACTOR STATE, OR THE `THIS` REFERENCE, TO CHILD ACTORS.
    *
    * NEVER IN YOUR LIFE
    */

  object NaiveBankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object IntializeAccount
  }
  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0
    override def receive: Receive = {
      case IntializeAccount => {
        val creditCardRef = context.actorOf(Props[CreditCard],"card")
        creditCardRef ! AttachToAccount(this)
      }
      case Deposit(funds) => {
        deposit(funds)
      }
      case Withdraw(funds) => {
        withdraw(funds)
      }
    }
    def deposit(funds: Int) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }
    def withdraw(funds: Int) = {
      println(s"${self.path} withdrawing $funds from $amount")

      amount -= funds
    }

  }


  object CreditCard {
    case class AttachToAccount(bankAccount: NaiveBankAccount) //!
    case object CheckStatus
  }
  class CreditCard extends Actor {
    import CreditCard._
    override def receive: Receive = {
      case AttachToAccount(account) => context.become(attachedTo(account))
    }
    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        {
          println(s"${self.path} your message has been processed")
          account.withdraw(1)//
        }
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccountRef = system.actorOf(Props[NaiveBankAccount],"account")
  bankAccountRef ! IntializeAccount
  bankAccountRef ! Deposit(100)

  Thread.sleep(1000)
  val ccSelection = system.actorSelection("/user/account/card")
  ccSelection ! CheckStatus

  // WRONG!!
}
