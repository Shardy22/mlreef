package com.mlreef.rest.api.v1.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.mlreef.rest.AccessLevel
import com.mlreef.rest.Account
import com.mlreef.rest.CodeProject
import com.mlreef.rest.DataProcessor
import com.mlreef.rest.DataProcessorInstance
import com.mlreef.rest.DataProcessorType
import com.mlreef.rest.DataProject
import com.mlreef.rest.DataType
import com.mlreef.rest.Experiment
import com.mlreef.rest.FileLocation
import com.mlreef.rest.Group
import com.mlreef.rest.I18N
import com.mlreef.rest.MLProject
import com.mlreef.rest.MetricType
import com.mlreef.rest.ParameterInstance
import com.mlreef.rest.PipelineConfig
import com.mlreef.rest.PipelineInstance
import com.mlreef.rest.PipelineJobInfo
import com.mlreef.rest.ProcessorParameter
import com.mlreef.rest.VisibilityScope
import com.mlreef.rest.config.censor
import com.mlreef.rest.exceptions.RestException
import com.mlreef.rest.exceptions.ValidationException
import com.mlreef.rest.helpers.DataClassWithId
import com.mlreef.rest.helpers.GroupOfUser
import com.mlreef.rest.helpers.ProjectOfUser
import com.mlreef.rest.helpers.UserInGroup
import com.mlreef.rest.helpers.UserInProject
import org.springframework.data.domain.Persistable
import org.springframework.validation.FieldError
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class RestExceptionDto(
    val errorCode: Int,
    val errorName: String,
    val errorMessage: String,
    val time: ZonedDateTime = I18N.dateTime()
) {
    constructor(
        restException: RestException,
        time: ZonedDateTime = I18N.dateTime()
    ) : this(
        restException.errorCode,
        restException.errorName,
        restException.message.orEmpty(),
        time
    )
}

class ValidationFailureDto(
    val errorCode: Int,
    val errorName: String,
    val errorMessage: String,
    val validationErrors: Array<FieldError?>,
    val time: LocalDateTime = LocalDateTime.now()
) {
    constructor(
        restException: ValidationException,
        time: LocalDateTime = LocalDateTime.now()
    ) : this(
        restException.errorCode,
        restException.errorName,
        restException.message.orEmpty(),
        restException.validationErrors,
        time
    )
}


internal fun Group.toDto(): GroupDto =
    GroupDto(
        id = this.id,
        name = this.name
    )


data class UserDto(
    override val id: UUID,
    val username: String,
    val email: String,
    val token: String?,
    val accessToken: String?,
    val refreshToken: String?
) : DataClassWithId {
    fun censor(): UserDto {
        return this.copy(token = token?.censor())
    }
}

fun Account.toUserDto(accessToken: String? = null, refreshToken: String? = null) = UserDto(
    this.id,
    this.username,
    this.email,
    this.bestToken?.token,
    accessToken,
    refreshToken
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInProjectDto(
    override val id: UUID?,
    val userName: String?,
    val email: String?,
    val gitlabId: Long?
) : DataClassWithId

internal fun UserInProjectDto.toDomain() = UserInProject(
    id = this.id,
    userName = this.userName,
    gitlabId = this.gitlabId,
    email = this.email
)

internal fun UserInProject.toDto() = UserInProjectDto(
    id = this.id,
    userName = this.userName,
    gitlabId = this.gitlabId,
    email = this.email
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInGroupDto(
    override val id: UUID,
    val userName: String?,
    val email: String?,
    val gitlabId: Long?,
    val accessLevel: AccessLevel?
) : DataClassWithId

internal fun UserInGroupDto.toDomain() = UserInGroup(
    id = this.id,
    userName = this.userName,
    email = this.email,
    gitlabId = this.gitlabId,
    accessLevel = this.accessLevel
)

internal fun UserInGroup.toDto() = UserInGroupDto(
    id = this.id,
    userName = this.userName,
    email = this.email,
    gitlabId = this.gitlabId,
    accessLevel = this.accessLevel
)


data class GroupDto(
    override val id: UUID,
    val name: String
) : DataClassWithId

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GroupOfUserDto(
    override val id: UUID,
    val name: String,
    val accessLevel: AccessLevel?
) : DataClassWithId

internal fun GroupOfUserDto.toDomain() = GroupOfUser(
    id = this.id,
    name = this.name,
    accessLevel = this.accessLevel
)

internal fun GroupOfUser.toDto() = GroupOfUserDto(
    id = this.id,
    name = this.name,
    accessLevel = this.accessLevel
)


data class LoginRequest(
    val username: String?,
    @get:Email val email: String?,
    @get:NotEmpty val password: String
)

data class RegisterRequest(
    @get:NotEmpty val username: String,
    @get:Email @get:NotEmpty val email: String,
    @get:NotEmpty val password: String,
    @get:NotEmpty val name: String
)

data class ParameterDto(
    @get:NotEmpty val name: String,
    @get:NotEmpty val type: String,
    @get:NotEmpty val required: Boolean = true,
    val order: Int?,
    val defaultValue: String = "",
    val description: String? = null
)

data class ParameterInstanceDto(
    @get:NotEmpty val name: String,
    @get:NotEmpty val value: String,
    val type: String? = null,
    val required: Boolean = true,
    val description: String = ""
)

data class DataProcessorDto(
    val id: UUID,
    val slug: String,
    val name: String? = null,
    val inputDataType: DataType,
    val outputDataType: DataType,
    val type: DataProcessorType,
    val visibilityScope: VisibilityScope,
    val description: String,
    val codeProjectId: UUID?,
    val authorId: UUID?,
    val metricType: MetricType,
    val parameters: List<ParameterDto> = arrayListOf()
)

data class DataProcessorInstanceDto(
    @get:NotEmpty val slug: String,
    @get:Valid val parameters: List<ParameterInstanceDto> = arrayListOf(),
    val id: UUID? = null,
    val name: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PipelineJobInfoDto(
    val id: Long,
    val ref: String,
    val commitSha: String,
    val createdAt: ZonedDateTime? = null,
    val committedAt: ZonedDateTime? = null,
    val startedAt: ZonedDateTime? = null,
    val updatedAt: ZonedDateTime? = null,
    val finishedAt: ZonedDateTime? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProjectOfUserDto(
    override val id: UUID,
    val name: String,
    val accessLevel: AccessLevel?
) : DataClassWithId

abstract class MLProjectDto(
    override val id: UUID?,
    open val slug: String,
    open val url: String,
    open val ownerId: UUID,
    open val gitlabGroup: String,
    open val gitlabProject: String,
    open val gitlabId: Long
) : DataClassWithId

data class DataProjectDto(
    override val id: UUID,
    override val slug: String,
    override val url: String,
    override val ownerId: UUID,
    override val gitlabGroup: String,
    override val gitlabProject: String,
    override val gitlabId: Long,
    val experiments: List<ExperimentDto> = listOf()
) : MLProjectDto(id, slug, url, ownerId, gitlabGroup, gitlabProject, gitlabId)

data class CodeProjectDto(
    override val id: UUID,
    override val slug: String,
    override val url: String,
    override val ownerId: UUID,
    override val gitlabGroup: String,
    override val gitlabProject: String,
    override val gitlabId: Long
) : MLProjectDto(id, slug, url, ownerId, gitlabGroup, gitlabProject, gitlabId)

internal fun ProjectOfUserDto.toDomain() = ProjectOfUser(
    id = this.id,
    name = this.name,
    accessLevel = this.accessLevel
)

class ExperimentDto(
    override val id: UUID,
    val dataProjectId: UUID,
    val dataInstanceId: UUID?,
    val slug: String,
    val name: String,
    @get:NotEmpty val sourceBranch: String,
    @get:NotEmpty val targetBranch: String,
    val status: String,
    val pipelineJobInfo: PipelineJobInfoDto? = null,
    @get:Valid val postProcessing: List<DataProcessorInstanceDto>? = arrayListOf(),
    @get:Valid val processing: DataProcessorInstanceDto? = null,
    val jsonBlob: String = ""
) : DataClassWithId

data class PipelineConfigDto(
    override val id: UUID,
    val pipelineType: String,
    val dataProjectId: UUID,
    val slug: String,
    val name: String,
    @get:NotEmpty val sourceBranch: String,
    val targetBranchPattern: String = "",
    @get:Valid val dataOperations: List<DataProcessorInstanceDto>? = arrayListOf(),
    @get:Valid val inputFiles: List<FileLocationDto>? = arrayListOf()
) : DataClassWithId

data class PipelineInstanceDto(
    override val id: UUID,
    val pipelineType: String,
    val pipelineConfigId: UUID,
    val dataProjectId: UUID,
    val slug: String,
    val name: String,
    @get:NotEmpty val sourceBranch: String,
    val targetBranch: String,
    @get:Valid val dataOperations: List<DataProcessorInstanceDto>? = arrayListOf(),
    @get:Valid val inputFiles: List<FileLocationDto>? = arrayListOf(),
    val number: Int,
    val commit: String? = null,
    val status: String
) : DataClassWithId

data class FileLocationDto(
    val location: String,
    val locationType: String = "PATH"
)

@Suppress("UNCHECKED_CAST")
fun MLProject.toDto(): MLProjectDto {
    val id = (this as? Persistable<UUID>)?.id
    return object : MLProjectDto(
        id,
        this.slug,
        this.url,
        this.ownerId,
        this.gitlabGroup,
        this.gitlabProject,
        this.gitlabId
    ) {}
}

internal fun DataProject.toDto() =
    DataProjectDto(
        id = this.id,
        slug = this.slug,
        url = this.url,
        ownerId = this.ownerId,
        gitlabGroup = this.gitlabGroup,
        gitlabProject = this.gitlabProject,
        gitlabId = this.gitlabId,
        experiments = this.experiments.map(Experiment::toDto)
    )

internal fun CodeProject.toDto() =
    CodeProjectDto(
        id = this.id,
        slug = this.slug,
        url = this.url,
        ownerId = this.ownerId,
        gitlabGroup = this.gitlabGroup,
        gitlabProject = this.gitlabProject,
        gitlabId = this.gitlabId
    )

internal fun Experiment.toDto(): ExperimentDto =
    ExperimentDto(
        id = this.id,
        dataProjectId = this.dataProjectId,
        dataInstanceId = this.dataInstanceId,
        slug = this.slug,
        name = this.name,
        sourceBranch = this.sourceBranch,
        targetBranch = this.targetBranch,
        status = this.status.name,
        jsonBlob = this.jsonBlob,
        pipelineJobInfo = this.pipelineJobInfo?.toDto(),
        postProcessing = this.postProcessing.map(DataProcessorInstance::toDto),
        processing = this.getProcessor()?.toDto()
    )


internal fun PipelineJobInfo.toDto(): PipelineJobInfoDto =
    PipelineJobInfoDto(
        this.gitlabId,
        this.ref,
        this.commitSha,
        this.committedAt,
        this.createdAt,
        this.updatedAt,
        this.startedAt,
        this.finishedAt
    )

internal fun PipelineConfig.toDto(): PipelineConfigDto =
    PipelineConfigDto(
        id = this.id,
        slug = this.slug,
        name = this.name,
        pipelineType = this.pipelineType.name,
        dataProjectId = this.dataProjectId,
        sourceBranch = this.sourceBranch,
        targetBranchPattern = this.targetBranchPattern,
        inputFiles = this.inputFiles.map(FileLocation::toDto),
        dataOperations = this.dataOperations.map(DataProcessorInstance::toDto)
    )

internal fun PipelineInstance.toDto(): PipelineInstanceDto =
    PipelineInstanceDto(
        id = this.id,
        dataProjectId = this.dataProjectId,
        pipelineConfigId = this.pipelineConfigId,
        slug = this.slug,
        name = this.name,
        pipelineType = this.pipelineType.name,
        sourceBranch = this.sourceBranch,
        targetBranch = this.targetBranch,
        inputFiles = this.inputFiles.map(FileLocation::toDto),
        dataOperations = this.dataOperations.map(DataProcessorInstance::toDto),
        number = this.number,
        commit = this.commit,
        status = this.status.name
    )

internal fun FileLocation.toDto(): FileLocationDto =
    FileLocationDto(
        location = this.location,
        locationType = this.locationType.name
    )

internal fun DataProcessorInstance.toDto(): DataProcessorInstanceDto =
    DataProcessorInstanceDto(
        id = this.id,
        slug = this.slug,
        name = this.name,
        parameters = this.parameterInstances.map(ParameterInstance::toDto)
    )

internal fun ParameterInstance.toDto(): ParameterInstanceDto =
    ParameterInstanceDto(
        name = this.processorParameter.name,
        value = this.value,
        type = this.processorParameter.type.name,
        description = this.processorParameter.description ?: "",
        required = this.processorParameter.required
    )

internal fun DataProcessor.toDto(): DataProcessorDto =
    DataProcessorDto(
        id = this.id,
        slug = this.slug,
        name = this.name,
        description = this.description,
        type = this.type,
        metricType = metricSchema.metricType,
        authorId = this.author?.id,
        inputDataType = this.inputDataType,
        outputDataType = this.outputDataType,
        visibilityScope = this.visibilityScope,
        codeProjectId = this.codeProjectId,
        parameters = this.parameters.map(ProcessorParameter::toDto)
    )


internal fun ProcessorParameter.toDto(): ParameterDto =
    ParameterDto(
        name = this.name,
        type = this.type.name,
        required = this.required,
        defaultValue = this.defaultValue,
        order = this.order,
        description = this.description
    )


