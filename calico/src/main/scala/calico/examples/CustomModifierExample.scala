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

package calico.examples

import calico.html.io.*
import calico.html.io.given
import calico.html.CustomModifier
import cats.effect.*
import org.scalajs.dom

/**
 * Example demonstrating the new CustomModifier trait.
 * This shows how much easier it is to create custom modifiers now.
 */
object CustomModifierExample:

  // Simple custom modifier using the new trait
  case class DataAttribute(value: String) extends CustomModifier[IO, dom.Element]:
    def apply(element: dom.Element): Resource[IO, Unit] =
      Resource.eval(IO.delay(element.setAttribute("data-example", value)))

  // Custom modifier using the sync helper
  case class CustomClass(className: String) extends CustomModifier[IO, dom.Element]:
    def apply(element: dom.Element): Resource[IO, Unit] =
      sync(element)(_.classList.add(className))

  // Complex custom modifier with conditional logic
  case class ConditionalStyle(condition: Boolean, style: String) extends CustomModifier[IO, dom.Element]:
    def apply(element: dom.Element): Resource[IO, Unit] =
      if condition then
        Resource.eval(IO.delay(element.setAttribute("style", style)))
      else
        Resource.eval(IO.delay(element.removeAttribute("style")))

  // Example usage in HTML DSL
  def exampleHtml = 
    div(
      cls := "container",
      DataAttribute("example-value"),
      CustomClass("highlight"),
      ConditionalStyle(true, "background-color: yellow;"),
      "Hello, Custom Modifiers!"
    )

  // This demonstrates the improvement over the old approach:
  // 
  // OLD WAY (verbose):
  // case class OldDataAttribute(value: String)
  // given Modifier[IO, dom.Element, OldDataAttribute] = (attr, elem) =>
  //   Resource.eval(IO.delay(elem.setAttribute("data-old", attr.value)))
  //
  // NEW WAY (simple):
  // case class NewDataAttribute(value: String) extends CustomModifier[IO, dom.Element]:
  //   def apply(element: dom.Element): Resource[IO, Unit] =
  //     Resource.eval(IO.delay(element.setAttribute("data-new", value)))
