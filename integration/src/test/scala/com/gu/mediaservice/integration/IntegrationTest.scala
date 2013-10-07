package com.gu.mediaservice
package integration

import scala.concurrent.duration._

import org.scalatest.FlatSpec
import scalaz.syntax.bind._
import play.api.libs.json.JsString


class IntegrationTest extends FlatSpec with TestHarness {

  lazy val config = Discovery.discoverConfig("media-service-TEST") getOrElse sys.error("Could not find stack")

  "An image posted to the loader" should "become visible in the Media API" in {
    await(15.seconds) {

      loadImage("honeybee", resourceAsFile("/images/honeybee.jpg")) >>
      retrying("get image", 5, 3.seconds)(getImage("honeybee")) >>
      deleteIndex

    }
  }

  it should "retain IPTC metadata when retrieved from the Media API" in {

    val response = await(15.seconds) {
      loadImage("gallery", resourceAsFile("/images/gallery.jpg")) >>
      retrying("get image", 5, 3.seconds)(getImage("gallery")) <<
      deleteIndex
    }
    val metadata = response.json \ "metadata"

    assert(metadata \ "credit" == JsString("AFP/Getty Images"))
    assert(metadata \ "byline" == JsString("GERARD JULIEN"))

  }

}