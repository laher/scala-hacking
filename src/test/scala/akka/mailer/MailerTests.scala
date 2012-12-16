package akka.mailer

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import org.scalatest.junit.JUnitRunner
import akka.Pi
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestProbe
import akka.actor.Props

/** Taken from https://github.com/typesafehub/akka-first-tutorial-scala.g8 */
@RunWith(classOf[JUnitRunner])
class MailerTests(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
   with WordSpec with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  import TestKit._
  override def afterAll {
    system.shutdown()
  }

  "GetData" must {
    "get some email data" in {
      
      val testProbe = new TestProbe(system)
      
      val testPullData = system.actorOf(Props(new Generate.PullData(testProbe.ref)))
      
      testPullData ! Generate.Work(1,1)
      
      testProbe.expectMsg(Generate.EmailData("blah bl2ah"))
      
//      Generate.getData(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)
  //    Thread.sleep(1000);
      //g.get must equal(0.0)
      //      actor.calculatePiFor(1, 1) must be(-1.3333333333333333 plusOrMinus 0.0000000001)
    }
  }
}