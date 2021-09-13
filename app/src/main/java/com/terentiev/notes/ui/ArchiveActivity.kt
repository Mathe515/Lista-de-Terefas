package com.terentiev.notes.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.terentiev.notes.R
import com.terentiev.notes.data.NoteRecord
import com.terentiev.notes.utils.ItemSwipeCallback

import kotlinx.android.synthetic.main.activity_archive.*
import kotlinx.android.synthetic.main.content_archive.*

class ArchiveActivity : AppCompatActivity(), NoteListAdapter.TodoEvents {


    private lateinit var noteViewModel: NoteViewModel // classe do responsável por chamar opçoes para uma anotação(salvar, deletar, atualizar ...)
    private lateinit var noteAdapter: NoteListAdapter // adaptador de listagem de anotações(controla tudo da listage)
    private lateinit var searchView: SearchView // barra de pesquisa


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive) // selecionando layout que vai ser utilizado(arquivo xml)
        setSupportActionBar(toolbar) // barra de menu superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // rv_archive = RecyclerView => componente nativo que lista itens
        rv_archive.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteListAdapter(this)
        rv_archive.adapter = noteAdapter // definindo adaptador que vai definir o comportamento da listagem
        ItemTouchHelper(
            ItemSwipeCallback(
                noteAdapter,
                applicationContext,
                true
            )
        ).attachToRecyclerView(rv_archive)

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        noteViewModel.getArchivedNotes().observe(this, Observer { noteAdapter.setAllTodos(it)})
    }

    // quando selecionar a opção deletar item
    override fun onItemDeleted(note: NoteRecord, position: Int) {
        noteViewModel.deleteNote(note)
        // dizendo pra o adaptador q tem alterações na lista
        noteAdapter.notifyItemRemoved(position)
        // mensagem de desfazer do canto inferior
        val snackbar = Snackbar.make(container_archive, R.string.deleted, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.undo) { undoDelete(note, position) }
        snackbar.show()
    }

    // quando selecionar a opção cancelar deletar item
    private fun undoDelete(note: NoteRecord, position: Int) {
        noteViewModel.salvarNota(note)
        noteAdapter.notifyItemInserted(position)
    }

    //desfazer desarquivar
    private fun undoUnarchive(note: NoteRecord, position: Int) {
        noteViewModel.archiveNote(note)
        noteAdapter.notifyItemInserted(position)
    }

    override fun onViewClicked(note: NoteRecord) {
        // fazer nada
    }

    override fun onItemUnarchived(note: NoteRecord, position: Int) {
        noteViewModel.unarchiveNote(note)
        noteAdapter.notifyItemRemoved(position)
        val snackbar = Snackbar.make(container_archive, R.string.unarchived, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.undo) { undoUnarchive(note, position) }
        snackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_archive, menu)
        searchView = menu?.findItem(R.id.search_archived_item)?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                noteAdapter.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                noteAdapter.filter.filter(newText)
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.search_archived_item -> true
            R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        resetSearchView()
        super.onBackPressed()
    }

    private fun resetSearchView() {
        if (!searchView.isIconified) {
            searchView.isIconified = true
            return
        }
    }
}
