package dev.ridill.rivo.folders.presentation.folderDetails

import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel

interface FolderDetailsActions {
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteFolderOnlyClick()
    fun onDeleteFolderAndTransactionsClick()
    fun onTransactionSwipeToDismiss(transaction: TransactionListItemUIModel.TransactionItem)
}