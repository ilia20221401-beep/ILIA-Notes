package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isGridView: Boolean = true
)

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote: StateFlow<Note?> = _editingNote.asStateFlow()

    private val _notePendingDeletion = MutableStateFlow<Note?>(null)
    val notePendingDeletion: StateFlow<Note?> = _notePendingDeletion.asStateFlow()

    var recentlyDeletedNote: Note? = null
        private set

    val uiState: StateFlow<NotesUiState> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedCategory,
        _isGridView
    ) { allNotes, query, category, isGrid ->
        val filtered = allNotes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory = category == null || note.category == category
            matchesQuery && matchesCategory
        }
        NotesUiState(
            notes = filtered,
            totalCount = allNotes.size,
            searchQuery = query,
            selectedCategory = category,
            isGridView = isGrid
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleLayoutMode() {
        _isGridView.value = !_isGridView.value
    }

    fun openCreateNote() {
        _editingNote.value = null
        _showAddEditDialog.value = true
    }

    fun openEditNote(note: Note) {
        _editingNote.value = note
        _showAddEditDialog.value = true
    }

    fun dismissAddEditDialog() {
        _showAddEditDialog.value = false
        _editingNote.value = null
    }

    fun saveNote(
        title: String,
        content: String,
        category: String,
        colorHex: Long,
        isPinned: Boolean
    ) {
        val currentEditing = _editingNote.value
        viewModelScope.launch {
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateNote(updated)
            } else {
                val newNote = Note(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNote(newNote)
            }
            dismissAddEditDialog()
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun requestDeleteNote(note: Note) {
        _notePendingDeletion.value = note
    }

    fun dismissDeleteDialog() {
        _notePendingDeletion.value = null
    }

    fun confirmDeleteNote(onDeleted: (Note) -> Unit) {
        val note = _notePendingDeletion.value ?: return
        recentlyDeletedNote = note
        _notePendingDeletion.value = null
        viewModelScope.launch {
            repository.deleteNote(note)
            onDeleted(note)
        }
    }

    fun undoDeleteNote() {
        val noteToRestore = recentlyDeletedNote ?: return
        viewModelScope.launch {
            repository.insertNote(noteToRestore)
            recentlyDeletedNote = null
        }
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
