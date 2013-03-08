/*
 * Copyright 2013 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.heikoseeberger.gabbler

import akka.actor.{ Actor, ActorRef }
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.HttpServiceActor
import spray.routing.authentication.BasicAuth

class GabblerService(gabblerHub: ActorRef) extends Actor with HttpServiceActor {

  import GabblerHub._
  import SprayJsonSupport._

  def receive = runRoute(
    // format: OFF
    authenticate(BasicAuth("gabbler"))(user =>
      path("")(
        getFromResource(s"web/index.html")
      ) ~
      pathPrefix("api")(
        pathPrefix("messages")(
          get(requestContext =>
            gabblerHub ! GetMessages(user.username, requestContext)
          ) ~
          post(
            entity(as[InboundMessage]) { message => requestContext =>
              gabblerHub ! Message(user.username, message.text)
              requestContext.complete(StatusCodes.NoContent)
            }
          )
        )
      ) ~
      path(Rest)(path =>
        getFromResource(s"web/$path")
      )
    )
  // format: ON
  )
}