package com.example.hypechat.presentation.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.hypechat.R

class RegeneratePasswordDialog: AppCompatDialogFragment() {

    private lateinit var listener: RegeneratePasswordListener
    private var emailEditText: EditText? = null
    private var recoveryTokenEditText: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.regenerate_password_dialog, null)

        builder.setView(view)
            .setTitle("Regenerate Password")
            .setPositiveButton("Ok"){ dialog, which ->

                if (emailEditText?.text.toString().isNotBlank() && recoveryTokenEditText?.text.toString().isNotBlank()){

                    val email = emailEditText!!.text.toString()
                    val token = recoveryTokenEditText!!.text.toString()
                    listener.applyData(email, token)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }

        emailEditText = view?.findViewById(R.id.emailEditText)
        recoveryTokenEditText = view?.findViewById(R.id.recoveryTokenEditText)

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as RegeneratePasswordListener

        } catch (e: ClassCastException) {

            throw ClassCastException("$context must implement RegeneratePasswordListener")
        }
    }

    interface RegeneratePasswordListener {

        fun applyData(email: String, token: String)
    }
}