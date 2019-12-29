package dev.vasas.flashbacker.api.rest

import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(path = ["/"], produces = [MediaTypes.HAL_JSON_VALUE])
@CrossOrigin(origins = ["http://localhost:8080"])
class ApiController {

    class ApiModel : RepresentationModel<ApiModel>()

    @GetMapping
    fun availableEndpoints(): ResponseEntity<ApiModel> {
        val result = ApiModel()
        result.add(linkTo<ApiController> { availableEndpoints() }.withSelfRel())
        result.add(linkTo<StoryController> { listStories(AnyPrincipal()) }.withRel(StoryModel.collectionRelationName))
        return ResponseEntity(result, HttpStatus.OK)
    }

    /**
     * A dummy Principal implementation which is used in linkTo invocation only.
     */
    private class AnyPrincipal : Principal {
        override fun getName(): String {
            return ""
        }
    }

}
