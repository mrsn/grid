package lib

import scala.collection.JavaConverters._
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.{Message => SQSMessage, DeleteMessageRequest, ReceiveMessageRequest}

import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.actor.ActorSystem
import scalaz.syntax.id._

import com.gu.mediaservice.lib.json.PlayJsonHelpers._
import org.elasticsearch.action.index.IndexResponse

object MessageConsumer {

  val actorSystem = ActorSystem("MessageConsumer")

  implicit val ctx: ExecutionContext = actorSystem.dispatcher

  def startSchedule(): Unit =
    actorSystem.scheduler.schedule(0 seconds, 5 seconds)(processMessages())

  lazy val client = {
    val client = new AmazonSQSClient(Config.awsCredentials)
    client.setEndpoint("ec2.eu-west-1.amazonaws.com")
    client
  }

  def processMessages(): Unit =
    for (msg <- poll(1)) {
      Logger.info("Processing message...")
      indexImage(msg) |> deleteOnSuccess(msg)
    }

  def deleteOnSuccess(msg: SQSMessage)(f: Future[IndexResponse]): Unit =
    f.onSuccess { case _ => deleteMessage(msg) }

  def poll(max: Int): Seq[SQSMessage] =
    client.receiveMessage(new ReceiveMessageRequest(Config.queueUrl)).getMessages.asScala.toList

  /* The java Future used by the Async SQS client is useless,
     so we just hide the synchronous call in a scala Future. */
  def pollFuture(max: Int): Future[Seq[SQSMessage]] =
    Future(poll(max))

  def extractSNSMessage(sqsMessage: SQSMessage): Option[SNSMessage] =
    Json.fromJson[SNSMessage](Json.parse(sqsMessage.getBody)) <| logParseErrors |> (_.asOpt)

  private def indexImage(sqsMessage: SQSMessage): Future[IndexResponse] = {
    val message = extractSNSMessage(sqsMessage) getOrElse sys.error("Invalid message structure (not via SNS?)")
    if (message.subject != Some("image"))
      sys.error(s"Unrecognised message subject: ${message.subject}")
    val image = message.body
    image \ "id" match {
      case JsString(id) => ElasticSearch.indexImage(id, image)
      case _            => sys.error(s"No id field present in message body: $image")
    }
  }

  private def deleteMessage(message: SQSMessage): Unit =
    client.deleteMessage(new DeleteMessageRequest(Config.queueUrl, message.getReceiptHandle))

}

case class SNSMessage(
  messageType: String,
  messageId: String,
  topicArn: String,
  subject: Option[String],
  body: JsValue
)

object SNSMessage {
  implicit def snsMessageReads: Reads[SNSMessage] =
    (
      (__ \ "Type").read[String] ~
      (__ \ "MessageId").read[String] ~
      (__ \ "TopicArn").read[String] ~
      (__ \ "Subject").readNullable[String] ~
      (__ \ "Message").read[String].map(Json.parse)
    )(SNSMessage(_, _, _, _, _))
}