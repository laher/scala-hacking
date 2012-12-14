package akka.mailer

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/** Taken from https://github.com/typesafehub/akka-first-tutorial-scala.g8 */
object Generate extends App {

  getData(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)

  sealed trait PiMessage
  case object Calculate extends PiMessage
  case class Work(start: Int, nrOfElements: Int) extends PiMessage
  case class EmailData(value: String) extends PiMessage
  case class Email(text: String, duration: Duration)

  class PullData extends Actor {

    def genRandomEmail(): String = {
      var str= "blah blah"
      str
    }

    def receive = {
      case Work(start, nrOfElements) =>
        sender ! EmailData(genRandomEmail()) // perform the work
    }
  }
  
  class ApplyTemplate extends Actor {
    
    def applyTemplate(data : String, template : String) : String = {
      return null
    }
    
    def receive = {
      case Work(start, nrOfElements) =>
        sender ! EmailData(genRandomEmail()) // perform the work
    }
    
  }
  
  

  class Master(nrOfWorkers: Int,
    nrOfMessages: Int,
    nrOfElements: Int,
    listener: ActorRef) extends Actor {

    var text: String = _
    var nrOfResults: Int = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[PullData].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

    def receive = {
      case Calculate =>
        for (i <- 0 until nrOfMessages)
          workerRouter ! Work(i * nrOfElements, nrOfElements)
      case EmailData(value) =>
        text += value
        nrOfResults += 1
        if (nrOfResults == nrOfMessages) {
          listener ! Email(text, duration = (System.currentTimeMillis - start).millis)
          context.stop(self)
        }
    }
  }

  class Listener extends Actor {
    def receive = {
      case Email(text, duration) =>
        println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format(text, duration))
        context.system.shutdown()
    }
  }

  def getData(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val system = ActorSystem("EmailSystem")

    // create the result listener, which will print the result and 
    // shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements, listener)),
      name = "master")

    // start the calculation
    master ! Calculate

  }
}