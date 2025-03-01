package dev.ridill.rivo.folders.presentation.folderDetails

interface FolderDetailsActions {
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteFolderOnlyClick()
    fun onDeleteFolderAndTransactionsClick()
    fun onTransactionSwipeActionRevealed()
    fun onRemoveTransactionFromFolderClick(id: Long)
}