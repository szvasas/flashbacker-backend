package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.api.rest.modelassembler.StoryModelAssembler.toCollectionModel
import dev.vasas.flashbacker.api.rest.modelassembler.StoryModelAssembler.toModel
import dev.vasas.flashbacker.api.rest.representationmodel.StoryModel
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.repository.StoryRepository
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping(path = ["/${StoryModel.collectionRelationName}"], produces = [MediaTypes.HAL_JSON_VALUE])
class StoryController(
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

    @GetMapping("/{id}")
    fun findStory(principal: Principal, @PathVariable id: String): ResponseEntity<StoryModel?> {
        val foundStory = storyRepo.findById(id)
        return if (foundStory != null && foundStory.userId == principal.name) {
            ResponseEntity(toModel(foundStory), HttpStatus.OK)
        } else {
            ResponseEntity<StoryModel?>(null, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewStory(principal: Principal, @RequestBody newStoryModel: StoryModel) {
        val newStory = Story(
                id = newStoryModel.id,
                userId = principal.name,
                location = newStoryModel.location,
                dateHappened = newStoryModel.dateHappened,
                text = newStoryModel.text
        )

        storyRepo.save(newStory)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(principal: Principal, @PathVariable id: String) {
        val storedStory = storyRepo.findById(id)
        if (storedStory != null && storedStory.userId == principal.name) {
            storyRepo.deleteById(id)
        }
    }
}
