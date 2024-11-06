package dev.ridill.rivo.folders.data.repository

import androidx.paging.PagingData
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.toFolderDetails
import dev.ridill.rivo.folders.domain.model.FolderDetails
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FolderDetailsRepositoryImpl(
    private val dao: FolderDao,
    private val transactionDao: TransactionDao,
    private val transactionRepo: TransactionRepository
) : FolderDetailsRepository {
    override fun getFolderDetailsById(id: Long): Flow<FolderDetails?> = dao
        .getFolderAndAggregateById(id).map { it?.toFolderDetails() }

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

    override suspend fun removeTransactionFromFolderById(transactionId: Long) =
        withContext(Dispatchers.IO) {
            transactionDao.setFolderIdToTransactionsByIds(setOf(transactionId), null)
        }

    override suspend fun addTransactionToFolder(transaction: TransactionListItem) {
        withContext(Dispatchers.IO) {
            transactionDao.upsert(transaction.toEntity())
        }
    }
}