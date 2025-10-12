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

import calico.html.CustomModifier
import calico.html.io.*
import calico.html.io.given
import cats.effect.*
import munit.*
import org.scalajs.dom

class CustomModifierCompilationTest extends FunSuite:

  test("CustomModifier typeclass instance should be found") {
    case class TestModifier(value: String)
        extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute("data-test", value)))

    // This should compile without issues
    val modifier = TestModifier("test-value")
    val element = dom.document.createElement("div").asInstanceOf[dom.Element]
    
    // Test that the modifier can be applied
    modifier.apply(element).use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-test"), "test-value")
  }

  test("CustomModifier should work with HTML DSL") {
    case class SimpleModifier(value: String)
        extends CustomModifier[IO, dom.Element]:
      def apply(element: dom.Element): Resource[IO, Unit] =
        Resource.eval(IO.delay(element.setAttribute("data-simple", value)))

    // This should compile and work
    val html = div(SimpleModifier("hello"), "Content")
    val element = html.use(_ => IO.unit).unsafeRunSync()
    assertEquals(element.getAttribute("data-simple"), "hello")
  }
