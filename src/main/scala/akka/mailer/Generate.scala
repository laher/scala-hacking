package akka.mailer

import scala.concurrent.duration._
import scala.concurrent.duration.Duration

import akka.actor._
import akka.routing.RoundRobinRouter
import Actor._


/** Taken from https://github.com/typesafehub/akka-first-tutorial-scala.g8 */
object Generate extends App {

  go( nrOfPullDataWorkers=12, nrOfApplyTemplateWorkers=8, nrOfSendEmailWorkers=12, nrOfUpdateDatabaseWorkers=8, 
      nrOfMessages = 100, template = "To:{to}\n\n Subject:Welcome\n\n Dear {name},\n\nwelcome to Akka\n\nAll the Best")

  case class Query(country: String)
  case class GetRow(country: String, counter : Long)
  case class EmailData(id: Long, address: String, name: String)
  case class RenderedEmail(id: Long, email: String)
  case class SendResult(id: Long, success : Boolean)
  
  case class Report(sent: Integer, failed : Integer, duration: Duration)

  class PullData(onward : ActorRef) extends Actor {
    def genEmail(q : GetRow): EmailData = {
      Thread.sleep(100)
      EmailData(q.counter, "a@b.c","Mr Jones")
    }
    def receive = {
      case q : GetRow =>
        println("pullData received Work")
        onward ! genEmail(q) // perform the work
      case x : AnyRef =>
        println("Warning: pullData received unexpected input", x)
    }
  }

  class ApplyTemplate(onward : ActorRef, template: String) extends Actor {
    def receive = {
      case EmailData(id, to, name) =>
        println("ApplyTemplate received EmailData")
        Thread.sleep(20)
        onward ! RenderedEmail(id, template.replace("{name}", name).replace("{to}", to))
      case _ =>
        println("Warning: ApplyTemplate received something else")
   }
  }
  
  class SendEmail(onward : ActorRef) extends Actor {
    def sendMail(email : RenderedEmail) : SendResult = {
      Thread.sleep(100)
      SendResult(email.id, true)
    }
    def receive = {
      case s : RenderedEmail =>
         println("SendEmail received RenderedMail")
        onward ! sendMail(s)
      case _ =>
        println("Warning: SendEmail received something else")
    }
  }
  
  class UpdateDatabase(onward : ActorRef) extends Actor {
    def updateDatabase(s : SendResult) : SendResult = {
      Thread.sleep(100)
      s
    }
     def receive = {
      case s : SendResult =>
        onward ! updateDatabase(s)
      case _ =>
        println("Warning: SendEmail received something else")
    }
  }

  class Master(nrOfPullDataWorkers: Int,
    nrOfApplyTemplateWorkers : Int,
    nrOfSendEmailWorkers: Int,
    nrOfUpdateDatabaseWorkers: Int,
    nrOfMessages: Int,
    template: String) extends Actor {
	
    val start: Long = System.currentTimeMillis
     // create the result listener, which will print the result and
    // shutdown the system
    val listener = context.actorOf(Props(new Listener(start, nrOfMessages)), name = "listener")
    
    val updateDatabaseRouter = context.actorOf(Props(creator = { () => new UpdateDatabase(listener) }).withRouter(RoundRobinRouter(nrOfUpdateDatabaseWorkers)), name = "updateDatabase")
    val sendEmailRouter = context.actorOf(Props(creator = { () => new SendEmail(updateDatabaseRouter) }).withRouter(RoundRobinRouter(nrOfSendEmailWorkers)), name = "sendEmail")
    val applyTemplateRouter = context.actorOf(Props(creator = { () => new ApplyTemplate(sendEmailRouter, template ) }).withRouter(RoundRobinRouter(nrOfApplyTemplateWorkers)), name = "applyTemplate")
    val pullDataRouter = context.actorOf(Props(creator = { () => new PullData(applyTemplateRouter) }).withRouter(RoundRobinRouter(nrOfPullDataWorkers)), name = "pullDataRouter")

    def receive = {
      case Query(country) =>
        for (i <- 0 until nrOfMessages)
          pullDataRouter ! GetRow(country, i)
    }
  }

  class Listener(start: Long, nrOfMessages: Int) extends Actor {
    var nrOfResults: Int = _
    var failures: Int = _
    
    def receive = {
      case s : SendResult =>
        nrOfResults += 1
        if(! s.success) {
          failures += 1
        }
        if (nrOfResults == nrOfMessages) {
          println("\n\n\tSent: \t\t%d\n\tFailed: \t%s\n\tDuration: %s".format(nrOfResults, failures, (System.currentTimeMillis - start).millis))
          context.system.shutdown()
          context.stop(self)
        }
        
    }
  }

  def go(nrOfPullDataWorkers: Int,
    nrOfApplyTemplateWorkers : Int,
    nrOfSendEmailWorkers: Int,
    nrOfUpdateDatabaseWorkers: Int, nrOfMessages: Int, template: String) {
    // Create an Akka system
    val system = ActorSystem("EmailSystem")

    // create the master
    val master = system.actorOf(Props(new Master(
      nrOfPullDataWorkers, nrOfApplyTemplateWorkers, nrOfSendEmailWorkers, nrOfUpdateDatabaseWorkers, nrOfMessages, template)),
      name = "master")

    // start the calculation
    master ! Query("nz")

  }
}
