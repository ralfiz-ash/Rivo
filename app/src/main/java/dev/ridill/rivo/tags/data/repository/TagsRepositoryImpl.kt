package dev.ridill.rivo.tags.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.tags.data.local.TagsDao
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import dev.ridill.rivo.tags.data.toTag
import dev.ridill.rivo.tags.data.toTagInfo
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagInfo
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TagsRepositoryImpl(
    private val dao: TagsDao
) : TagsRepository {
    override fun getAllTagsPagingData(
        searchQuery: String,
        limit: Int
    ): Flow<PagingData<Tag>> = Pager(PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)) {
        dao.getAllTagsPaged(
            query = searchQuery,
            limit = limit
        )
    }.flow
        .map { pagingData -> pagingData.map(TagEntity::toTag) }

    override fun getTagInfoPagingData(
        dateRange: Pair<LocalDate, LocalDate>?,
        limit: Int
    ): Flow<PagingData<TagInfo>> = Pager(PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)) {
        dao.getTagAndAggregatePaged(
            startDate = dateRange?.first,
            endDate = dateRange?.second,
            limit = limit
        )
    }.flow
        .map { pagingData -> pagingData.map(TagAndAggregateRelation::toTagInfo) }

    override suspend fun getTagById(id: Long): Tag? = withContext(Dispatchers.IO) {
        dao.getTagById(id)?.toTag()
    }

    override fun getTagsListFlowByIds(ids: Set<Long>): Flow<List<Tag>> =
        dao.getTagsByIdFlow(ids).map { entities -> entities.map(TagEntity::toTag) }

    override suspend fun saveTag(
        id: Long,
        name: String,
        colorCode: Int,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        val entity = TagEntity(
            id = id,
            name = name,
            colorCode = colorCode,
            createdTimestamp = timestamp,
            isExcluded = excluded
        )

        dao.upsert(entity).first()
    }

    override suspend fun deleteTagById(id: Long) = withContext(Dispatchers.IO) {
        dao.untagTransactionsAndDeleteTag(id)
    }

    override suspend fun deleteMultipleTagsByIds(ids: Set<Long>) = withContext(Dispatchers.IO) {
        dao.untagTransactionsAndDeleteTags(ids)
    }

    override suspend fun deleteTagWithTransactions(tagId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTagWithTransactions(tagId)
    }
}