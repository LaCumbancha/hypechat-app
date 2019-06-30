package com.example.hypechat.presentation.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.hypechat.R

class JoinTeamDialog: AppCompatDialogFragment() {

    private lateinit var listener: TeamTokenListener
    private var editText: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.join_team_dialog, null)

        builder.setView(view)
            .setTitle("Enter token")
            .setPositiveButton("Ok"){ dialog, which ->
                if (editText?.text.toString().isNotBlank()){
                    val token = editText!!.text.toString()
                    listener.applyToken(token)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
        editText = view?.findViewById(R.id.tokenEditText)

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as TeamTokenListener

        } catch (e: ClassCastException) {

            throw ClassCastException("$context must implement TeamTokenListener")
        }
    }

    interface TeamTokenListener {

        fun applyToken(token: String)
    }
}