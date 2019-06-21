package com.example.hypechat.presentation.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.hypechat.R

class AddForbiddenWordDialog: AppCompatDialogFragment() {

    private lateinit var listener: ForbiddenWordListener
    private var editText: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.add_word_dialog, null)

        builder.setView(view)
            .setTitle("Add word")
            .setPositiveButton("Ok"){ dialog, which ->
                if (editText?.text.toString().isNotBlank()){
                    val word = editText!!.text.toString()
                    listener.addWord(word)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
        editText = view?.findViewById(R.id.forbiddenWordEditText)

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ForbiddenWordListener

        } catch (e: ClassCastException) {

            throw ClassCastException("$context must implement ForbiddenWordListener")
        }
    }

    interface ForbiddenWordListener {

        fun addWord(word: String)
    }
}