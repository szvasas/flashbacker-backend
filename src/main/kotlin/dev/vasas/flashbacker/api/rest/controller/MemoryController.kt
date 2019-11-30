package dev.vasas.flashbacker.api.rest.controller

import dev.vasas.flashbacker.api.rest.modelassembler.MemoryModelAssembler.toCollectionModel
import dev.vasas.flashbacker.api.rest.modelassembler.MemoryModelAssembler.toModel
import dev.vasas.flashbacker.api.rest.representationmodel.MemoryModel
import dev.vasas.flashbacker.domain.Memory
import dev.vasas.flashbacker.domain.repository.MemoryRepository
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

@RestController
@RequestMapping(path = ["/memories"], produces = [MediaTypes.HAL_JSON_VALUE])
class MemoryController(
        private val memoryRepo: MemoryRepository
) {

    @GetMapping
    fun listMemories(): CollectionModel<MemoryModel> {
        val foundMemories = memoryRepo.findMemoriesForUser("John")
        val result = toCollectionModel(foundMemories)
        val selfRel = linkTo<MemoryController> { listMemories() }.withRel(IanaLinkRelations.SELF)
        result.add(selfRel)
        return result
    }

    @GetMapping("/{id}")
    fun findMemory(@PathVariable id: String): ResponseEntity<MemoryModel?> {
        val foundMemory = memoryRepo.findById(id)
        return if (foundMemory?.userName == "John") {
            ResponseEntity(toModel(foundMemory), HttpStatus.OK)
        } else {
            ResponseEntity<MemoryModel?>(null, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun createNewMemory(@RequestBody newMemoryModel: MemoryModel) {
        val newMemory = Memory(
                id = newMemoryModel.id,
                userName = newMemoryModel.userName,
                location = newMemoryModel.location,
                date = newMemoryModel.date,
                text = newMemoryModel.text
        )

        memoryRepo.save(newMemory)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(@PathVariable id: String) {
        memoryRepo.deleteById(id)
    }
}
