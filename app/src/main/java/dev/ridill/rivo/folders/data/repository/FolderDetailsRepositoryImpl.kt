package dev.ridill.rivo.folders.data.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class FolderDetailsRepositoryImpl(
    private val dao: FolderDao,
    private val transactionDao: TransactionDao,
    private val transactionRepo: TransactionRepository,
    private val animPreferencesManager: AnimPreferencesManager
) : FolderDetailsRepository {
    override fun getFolderDetailsById(id: Long): Flow<FolderDetails?> = dao
        .getFolderAndAggregateById(id).mapLatest { it?.toFolderDetails() }

    override fun getTransactionsInFolderPaged(
        folderId: Long
    ): Flow<PagingData<TransactionListItemUIModel>> = transactionRepo
        .getDateSeparatedTransactions(folderId = folderId)

    override suspend fun addTransactionsToFolderByIds(folderId: Long, transactionIds: Set<Long>) =
        withContext(Dispatchers.IO) {
            transactionDao.setFolderIdToTransactionsByIds(
                ids = transactionIds,
                folderId = folderId
            )
        }

    override suspend fun deleteFolderById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFolderOnlyById(id)
    }

    override suspend fun deleteFolderWithTransactions(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFolderAndTransactionsById(id)
    }

    override suspend fun removeTransactionFromFolderById(
        transactionId: Long
    ) = withContext(Dispatchers.IO) {
        transactionDao.setFolderIdToTransactionsByIds(
            ids = setOf(transactionId),
            folderId = null
        )
    }

    override suspend fun addTransactionToFolder(
        txId: Long,
        folderId: Long
    ) = withContext(Dispatchers.IO) {
        transactionDao.setFolderIdToTransactionsByIds(
            ids = setOf(txId),
            folderId = folderId
        )
    }

    override fun shouldShowActionPreview(): Flow<Boolean> = animPreferencesManager.preferences
        .mapLatest { it.showTxInFolderItemActionPreview }
        .distinctUntilChanged()

    override suspend fun disableActionPreview() =
        animPreferencesManager.disableTxInFolderItemActionPreview()
}