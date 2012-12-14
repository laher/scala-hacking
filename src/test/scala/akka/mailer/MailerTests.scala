package akka.mailer

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import org.scalatest.junit.JUnitRunner
import akka.Pi

/** Taken from https://github.com/typesafehub/akka-first-tutorial-scala.g8 */
@RunWith(classOf[JUnitRunner])
class MailerTests extends WordSpec with MustMatchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()

  override def afterAll {
    system.shutdown()
  }

  "GetData" must {
    "get some email data" in {
      Generate.getData(nrOfWorkers = 4, nrOfElements = 10000, nrOfMessages = 10000)
      Thread.sleep(1000);
      //g.get must equal(0.0)
//      actor.calculatePiFor(1, 1) must be(-1.3333333333333333 plusOrMinus 0.0000000001)
    }
  }
}