package com.example.hypechat.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hypechat.R
import com.example.hypechat.data.local.AppPreferences
import com.example.hypechat.data.model.ForbiddenWordRow
import com.example.hypechat.data.model.rest.response.ForbiddenWord
import com.example.hypechat.data.repository.HypechatRepository
import com.example.hypechat.data.rest.utils.ServerStatus
import com.example.hypechat.presentation.utils.AddForbiddenWordDialog
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_forbidden_words.*

class ForbiddenWordsActivity : AppCompatActivity(), AddForbiddenWordDialog.ForbiddenWordListener {

    private val adapter = GroupAdapter<ViewHolder>()
    private var wordList = ArrayList<ForbiddenWordRow>()
    private val TAG = "Forbidden Words"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forbidden_words)

        setSupportActionBar(toolbarForbiddenWords)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        AppPreferences.init(this)

        forbiddenWordsRecyclerView.layoutManager = LinearLayoutManager(this)
        forbiddenWordsRecyclerView.adapter = adapter
        forbiddenWordsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        getForbiddenWords()
        setAdapterOnItemClickListener()
    }

    private fun getForbiddenWords(){

        forbiddenWordsProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().getForbiddenWords(teamId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.LIST.status -> initializeList(it.words)
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.CHAT_NOT_FOUND.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "getForbiddenWords:failure")
                errorOccurred(null)
                forbiddenWordsProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun initializeList(words: List<ForbiddenWord>){

        for (word in words){
            wordList.add(ForbiddenWordRow(word))
        }
        adapter.addAll(wordList)
        Log.d(TAG, "getForbiddenWords:success")
        forbiddenWordsProgressBar.visibility = View.INVISIBLE
    }

    private fun setAdapterOnItemClickListener(){

        adapter.setOnItemClickListener { item, view ->
            val forbiddenWordRow = item as ForbiddenWordRow
            deleteWord(forbiddenWordRow.forbiddenWord)
        }
    }

    private fun deleteWord(word: ForbiddenWord){

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Remove")
        builder.setMessage("Are you sure you want to remove the word ${word.word} from the list?")

        builder.setPositiveButton("Yes"){ dialog, which ->
            dialog.dismiss()
            remove(word.id)
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun remove(wordId: Int){

        forbiddenWordsProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().deleteForbiddenWord(teamId, wordId){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.REMOVED.status -> {
                        forbiddenWordsProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        adapter.removeAll(wordList)
                        wordList.clear()
                        getForbiddenWords()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "deleteForbiddenWord:failure")
                errorOccurred(null)
                forbiddenWordsProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    fun addForbiddenWord(view: View){

        val addForbiddenWordDialog = AddForbiddenWordDialog()
        addForbiddenWordDialog.show(supportFragmentManager, TAG)
    }

    override fun addWord(word: String) {

        forbiddenWordsProgressBar.visibility = View.VISIBLE
        val teamId = AppPreferences.getTeamId()

        HypechatRepository().addForbiddenWord(teamId, word){ response ->

            response?.let {

                when (it.status){
                    ServerStatus.ADDED.status -> {
                        forbiddenWordsProgressBar.visibility = View.INVISIBLE
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        adapter.removeAll(wordList)
                        wordList.clear()
                        getForbiddenWords()
                    }
                    ServerStatus.WRONG_TOKEN.status -> errorOccurred(it.message)
                    ServerStatus.ERROR.status -> errorOccurred(it.message)
                }
            }
            if (response == null){
                Log.w(TAG, "addForbiddenWord:failure")
                errorOccurred(null)
                forbiddenWordsProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onBackPressed() {

        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        android.R.id.home -> {
            onBackPressed()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun errorOccurred(error: String?){

        forbiddenWordsProgressBar.visibility = View.INVISIBLE

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Error")
        var msg = "There was a problem during the process. Please, try again."
        error?.let {
            msg = it
        }
        builder.setMessage(msg)

        builder.setPositiveButton("Ok"){ dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}
