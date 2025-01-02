package dev.ridill.rivo.folders.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateView
import dev.ridill.rivo.folders.data.toFolder
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.Folder
import dev.ridill.rivo.folders.domain.model.FolderUIModel
import dev.ridill.rivo.folders.domain.repository.FolderListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FolderListRepositoryImpl(
    private val folderDao: FolderDao
) : FolderListRepository {
    override fun getFolderAndAggregatesPaged(): Flow<PagingData<FolderUIModel>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { folderDao.getFolderAndAggregatesPaged() }
    ).flow
        .map { it.map(FolderAndAggregateView::toFolderDetails) }
        .map { pagingData ->
            pagingData.map { FolderUIModel.FolderListItem(it) }
        }
        .map { pagingData ->
            pagingData
                .insertSeparators<FolderUIModel.FolderListItem, FolderUIModel>
                { before, after ->
                    if (before?.folderDetails?.aggregateType != after?.folderDetails?.aggregateType)
                        after?.folderDetails?.aggregateType
                            ?.let { FolderUIModel.AggregateTypeSeparator(it) }
                    else null
                }
        }

    override fun getFoldersListPaged(searchQuery: String): Flow<PagingData<Folder>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { folderDao.getFoldersPaged(searchQuery) }
    ).flow
        .map { it.map(FolderEntity::toFolder) }
}