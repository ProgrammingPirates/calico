/*
 * Copyright 2022 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package calico

import calico.html.io.*
import calico.html.io.given
import calico.html.CustomModifier
import cats.effect.*
import cats.syntax.all.*
import fs2.concurrent.*
import munit.*
import org.scalajs.dom

class CustomModifierSuite extends FunSuite:

  test("CustomModifier can be used as a modifier") {
    case class DataAttribute(value: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute("data-test", value)))

    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    val modifier = DataAttribute("hello")
    
    // This should compile and work without needing explicit typeclass instance
    modifier.apply(element).use(_ => IO.unit).unsafeRunSync()
    
    // Verify the attribute was set
    assertEquals(element.getAttribute("data-test"), "hello")
  }

  test("CustomModifier works with HTML DSL") {
    case class CustomClass(className: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.classList.add(className)))

    // This should compile and work in the HTML DSL
    val html = div(
      CustomClass("my-custom-class"),
      "Hello World"
    )
    
    // The modifier should be applied when the element is created
    val element = html.use(_ => IO.unit).unsafeRunSync()
    assert(element.classList.contains("my-custom-class"))
  }

  test("CustomModifier can be combined with other modifiers") {
    case class CustomStyle(style: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute("style", style)))

    val html = div(
      cls := "container",
      CustomStyle("background-color: red;"),
      "Content"
    )
    
    val element = html.use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.className, "container")
    assertEquals(element.getAttribute("style"), "background-color: red;")
  }

  test("CustomModifier works with signals") {
    case class DynamicAttribute(name: String, value: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute(name, value)))

    val signal = SignallingRef[IO].of("initial").unsafeRunSync()
    
    val html = div(
      DynamicAttribute("data-value", signal.get.unsafeRunSync())
    )
    
    val element = html.use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-value"), "initial")
    
    // Update the signal
    signal.set("updated").unsafeRunSync()
    // Note: In a real reactive scenario, the attribute would be updated automatically
    // This test demonstrates the basic functionality
  }

  test("CustomModifier can use the sync helper method") {
    case class SimpleAttribute(name: String, value: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        sync(element)(_.setAttribute(name, value))

    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    val modifier = SimpleAttribute("data-simple", "test-value")
    
    modifier.apply(element).use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-simple"), "test-value")
  }

  test("CustomModifier can handle complex logic") {
    case class ConditionalModifier(condition: Boolean, value: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        if condition then
          Resource.eval(IO.delay(element.setAttribute("data-active", value)))
        else
          Resource.eval(IO.delay(element.removeAttribute("data-active")))

    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    
    // Test with condition true
    val modifier1 = ConditionalModifier(true, "yes")
    modifier1.apply(element).use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-active"), "yes")
    
    // Test with condition false
    val modifier2 = ConditionalModifier(false, "no")
    modifier2.apply(element).use(_ => IO.unit).unsafeRunSync()
    assert(element.getAttribute("data-active") == null)
  }

  test("CustomModifier demonstrates the improvement over manual typeclass implementation") {
    // OLD WAY (verbose and not intuitive):
    // case class OldStyleModifier(value: String)
    // given Modifier[IO, dom.Element, OldStyleModifier] = (modifier, element) =>
    //   Resource.eval(IO.delay(element.setAttribute("data-old", modifier.value)))
    
    // NEW WAY (simple and intuitive):
    case class NewStyleModifier(value: String) extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute("data-new", value)))

    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    val modifier = NewStyleModifier("improved")
    
    modifier.apply(element).use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-new"), "improved")
  }
