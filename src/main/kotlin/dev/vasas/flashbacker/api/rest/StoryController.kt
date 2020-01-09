package dev.vasas.flashbacker.api.rest

import dev.vasas.flashbacker.api.rest.StoryModel.Companion.collectionRelationName
import dev.vasas.flashbacker.api.rest.StoryModel.Companion.lastProcessedDateParamName
import dev.vasas.flashbacker.api.rest.StoryModel.Companion.lastProcessedIdParamName
import dev.vasas.flashbacker.api.rest.StoryModel.Companion.limitParamName
import dev.vasas.flashbacker.api.rest.StoryModelAssembler.toCollectionModel
import dev.vasas.flashbacker.api.rest.StoryModelAssembler.toModel
import dev.vasas.flashbacker.domain.Page
import dev.vasas.flashbacker.domain.PageRequest
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryKey
import dev.vasas.flashbacker.domain.StoryRepository
import org.slf4j.LoggerFactory
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.IanaLinkRelations.NEXT
import org.springframework.hateoas.IanaLinkRelations.SELF
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@RestController
@RequestMapping(path = ["/$collectionRelationName"], produces = [MediaTypes.HAL_JSON_VALUE])
@CrossOrigin(origins = ["http://localhost:8080", "https://flashbacker-qa.vasas.dev", "https://flashbacker.vasas.dev"])
class StoryController(
        private val idGenerator: () -> String = { UUID.randomUUID().toString() },
        private val timestampGenerator: () -> ZonedDateTime = { ZonedDateTime.now(ZoneId.of("UTC")) },
        private val storyRepo: StoryRepository
) {

    private val logger = LoggerFactory.getLogger(StoryController::class.java)

    companion object {
        private const val DEFAULT_PAGE_LIMIT = 10
    }

    @GetMapping
    fun listStories(
            principal: Principal,
            @RequestParam(required = false, name = lastProcessedDateParamName) lastProcessedDate: String? = null,
            @RequestParam(required = false, name = lastProcessedIdParamName) lastProcessedId: String? = null,
            @RequestParam(required = false, name = limitParamName) limit: Int? = null
    ): CollectionModel<StoryModel> {
        logger.info("Listing stories for user: ${principal.name}.")
        val pageRequest = createPageRequest(principal, lastProcessedDate, lastProcessedId, limit)
        logger.info("Using page request: $pageRequest.")

        val currentPage = storyRepo.findStoriesForUser(principal.name, pageRequest)
        val result = toCollectionModel(currentPage.content)

        result.add(buildLinks(principal, lastProcessedDate, lastProcessedId, limit, currentPage))

        logger.info("Returning result of size: ${result.content.size}.")
        return result
    }

    private fun buildLinks(
            principal: Principal,
            lastProcessedDate: String?,
            lastProcessedId: String?,
            limit: Int?,
            currentPage: Page<Story>
    ): List<Link> {
        val result = mutableListOf<Link>()

        result.add(buildLinkToListStories(SELF, principal, lastProcessedDate, lastProcessedId, limit))
        if (currentPage.hasNext) {
            val lastProcessed = currentPage.content.last()

            val nextPageRel = buildLinkToListStories(NEXT, principal, lastProcessed.dateHappened.toString(), lastProcessed.id, limit)
            result.add(nextPageRel)
        }
        return result
    }

    private fun createPageRequest(
            principal: Principal,
            lastProcessedDate: String?,
            lastProcessedId: String?,
            limit: Int?
    ): PageRequest<StoryKey> {
        val lastProcessedKey = if (lastProcessedDate != null && lastProcessedId != null) {
            StoryKey(
                    id = lastProcessedId,
                    dateHappened = LocalDate.parse(lastProcessedDate),
                    userId = principal.name
            )
        } else {
            null
        }
        val requestLimit = limit ?: DEFAULT_PAGE_LIMIT

        return PageRequest(requestLimit, lastProcessedKey)
    }

    private fun buildLinkToListStories(
            linkRelation: LinkRelation,
            principal: Principal,
            lastProcessedDate: String?,
            lastProcessedId: String?,
            limit: Int?
    ): Link {
        return linkTo<StoryController> { listStories(principal, lastProcessedDate, lastProcessedId, limit) }
                .withRel(linkRelation)
                .expand(mapOf(
                        lastProcessedDateParamName to lastProcessedDate,
                        lastProcessedIdParamName to lastProcessedId,
                        limitParamName to limit
                ))
    }

    @GetMapping("/{year}/{month}/{dayOfMonth}/{id}")
    fun findStory(
            principal: Principal,
            @PathVariable year: Int,
            @PathVariable month: Int,
            @PathVariable dayOfMonth: Int,
            @PathVariable id: String
    ): ResponseEntity<StoryModel?> {
        logger.info("Retrieving story for user: ${principal.name}, date: $year-$month-$dayOfMonth and id: $id.")
        val dateHappened = parseDateHappened(year, month, dayOfMonth)

        val foundStory = storyRepo.findByKey(StoryKey(
                id = id,
                userId = principal.name,
                dateHappened = dateHappened
        ))
        return if (foundStory != null) {
            logger.info("Story is found.")
            ResponseEntity(toModel(foundStory), HttpStatus.OK)
        } else {
            logger.info("Story is not found.")
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
                timestampCreated = timestampGenerator(),
                text = newStoryModel.text
        )
        logger.info("Creating a new story for user: ${principal.name} with id: ${newStory.id}.")

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
        logger.info("Deleting story for user: ${principal.name}, date: $year-$month-$dayOfMonth and id: $id.")

        val dateHappened = parseDateHappened(year, month, dayOfMonth)
        storyRepo.deleteByKey(StoryKey(
                id = id,
                userId = principal.name,
                dateHappened = dateHappened
        ))
    }

    private fun parseDateHappened(year: Int, month: Int, dayOfMonth: Int): LocalDate {
        return try {
            return LocalDate.of(year, month, dayOfMonth)
        } catch (e: DateTimeException) {
            logger.warn("Date parsing has failed: ", e)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
        }
    }
}
