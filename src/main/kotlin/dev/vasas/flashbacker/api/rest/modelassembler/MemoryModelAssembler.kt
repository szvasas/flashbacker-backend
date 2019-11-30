package dev.vasas.flashbacker.api.rest.modelassembler

import dev.vasas.flashbacker.api.rest.controller.MemoryController
import dev.vasas.flashbacker.api.rest.representationmodel.MemoryModel
import dev.vasas.flashbacker.domain.Memory
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport

object MemoryModelAssembler : RepresentationModelAssemblerSupport<Memory, MemoryModel>(
        MemoryController::class.java,
        MemoryModel::class.java
) {

    override fun instantiateModel(entity: Memory): MemoryModel {
        return MemoryModel(
                id = entity.id,
                userName = entity.userName,
                location = entity.location,
                date = entity.date,
                text = entity.text
        )
    }

    override fun toModel(memory: Memory): MemoryModel {
        return createModelWithId(memory.id, memory)
    }
}
