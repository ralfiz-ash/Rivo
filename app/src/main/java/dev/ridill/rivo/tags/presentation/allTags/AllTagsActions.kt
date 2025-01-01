package dev.ridill.rivo.tags.presentation.allTags

interface AllTagsActions {
    fun onSearchQueryChange(value: String)
    fun onTagLongPress(id: Long)
    fun onTagSelectionChange(id: Long)
    fun onMultiSelectionModeDismiss()
    fun onDeleteTagsClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
}