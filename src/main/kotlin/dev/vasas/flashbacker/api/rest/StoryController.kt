package dev.vasas.flashbacker.api.rest

import dev.vasas.flashbacker.api.rest.StoryModelAssembler.toCollectionModel
import dev.vasas.flashbacker.api.rest.StoryModelAssembler.toModel
import dev.vasas.flashbacker.api.rest.StoryModel.Companion.collectionRelationName
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryRepository
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.time.DateTimeException
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping(path = ["/$collectionRelationName"], produces = [MediaTypes.HAL_JSON_VALUE])
@CrossOrigin(origins = ["http://localhost:8080", "https://flashbacker-qa.vasas.dev", "https://flashbacker.vasas.dev"])
class StoryController(
        private val idGenerator: () -> String = { UUID.randomUUID().toString() },
        private val storyRepo: StoryRepository
) {

    @GetMapping
    fun listStories(principal: Principal): CollectionModel<StoryModel> {
        val foundStories = storyRepo.findStoriesForUser(principal.name)
        val result = toCollectionModel(foundStories)
        val selfRel = linkTo<StoryController> { listStories(principal) }.withRel(IanaLinkRelations.SELF)
        result.add(selfRel)
        return result
    }

    @GetMapping("/{year}/{month}/{dayOfMonth}/{id}")
    fun findStory(
            principal: Principal,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable dayOfMonth: Int,
            @PathVariable id: String
    ): ResponseEntity<StoryModel?> {
        val dateHappened = parseDateHappened(year, month, dayOfMonth)

        val foundStory = storyRepo.findByUserDateHappenedStoryId(principal.name, dateHappened, id)
        return if (foundStory != null) {
            ResponseEntity(toModel(foundStory), HttpStatus.OK)
        } else {
            ResponseEntity<StoryModel?>(null, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewStory(principal: Principal, @RequestBody newStoryModel: StoryModel) {
        val newStory = Story(
                id = idGenerator(),
                userId = principal.name,
                location = newStoryModel.location,
                dateHappened = newStoryModel.dateHappened,
                text = newStoryModel.text
        )

        storyRepo.save(newStory)
    }

    @DeleteMapping("/{year}/{month}/{dayOfMonth}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStory(
            principal: Principal,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable dayOfMonth: Int,
            @PathVariable id: String
    ) {
        val dateHappened = parseDateHappened(year, month, dayOfMonth)
        storyRepo.deleteByUserDateHappenedStoryId(principal.name, dateHappened, id)
    }

    private fun parseDateHappened(year: Int, month: Int, dayOfMonth: Int): LocalDate {
        return try {
            return LocalDate.of(year, month, dayOfMonth)
        } catch (e: DateTimeException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
        }
    }
}
