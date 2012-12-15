package akka.mailer

import scala.concurrent.duration._
import scala.concurrent.duration.Duration

import akka.actor._
import akka.routing.RoundRobinRouter
import Actor._


/** Taken from https://github.com/typesafehub/akka-first-tutorial-scala.g8 */
object Generate extends App {

  getData(nrOfWorkers = 4, nrOfElements = 4, nrOfMessages = 4)

  sealed trait PiMessage
  case object Calculate extends PiMessage
  case class Work(start: Int, nrOfElements: Int) extends PiMessage
  case class EmailData(value: String) extends PiMessage
  case class Email(text: String, duration: Duration)

  class PullData(onward : ActorRef) extends Actor {
	//var onward : Option[ActorRef]= None
    def genRandomEmail(): String = {
      var str= "blah blah"
      str
    }

    def receive = {
      
      case Work(start, nrOfElements) =>
        println("pullData received Work")
      //  val t= context.actorFor("akka://EmailSystem/user/master/applyTemplate")
      //  println(t.getClass())
      //  println(t.path)
        onward ! EmailData(genRandomEmail()) // perform the work
      case _ =>
        println("pullData received unexpected input")
    }
  }
  
  class ApplyTemplate extends Actor {
	//var onward : Option[ActorRef]= None
    
    def applyTemplate(data : String, template : String) : String = {
      return null
    }
    
    def receive = {
      case Work(start, nrOfElements) =>
        println("ApplyTemplate received Work")
      case EmailData(text) =>
        println("ApplyTemplate received EmailData")
        println(text)
        
        /*
        onward match {
          case Some(x) => x ! EmailData(genRandomEmail()) // perform the work
        } 
        * 
        */
      case _ =>
                println("ApplyTemplate received something else")

    }
    
  }
  


  class Master(nrOfWorkers: Int,
    nrOfMessages: Int,
    nrOfElements: Int,
    listener: ActorRef) extends Actor {

    var text: String = _
    var nrOfResults: Int = _
    val start: Long = System.currentTimeMillis
    val myApplyRouter = context.actorOf(Props[ApplyTemplate].withRouter(RoundRobinRouter(nrOfWorkers)), name = "applyTemplate")
    println(myApplyRouter.path)
    val workerRouter = context.actorOf(
       Props(new PullData(myApplyRouter)).withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
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