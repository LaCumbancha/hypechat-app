package com.example.hypechat.presentation.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.hypechat.R
import kotlinx.android.synthetic.main.team_invitation_dialog.*

class TeamInvitationDialog: AppCompatDialogFragment() {

    private lateinit var listener: TeamInvitationListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.team_invitation_dialog, null)

        builder.setView(view)
            .setTitle("Invitation")
            .setPositiveButton("Ok"){ dialog, which ->
                val email = teamInvitationEditText.text.toString()
                listener.applyEmail(email)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }

        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as TeamInvitationListener

        } catch (e: ClassCastException) {

            throw ClassCastException("$context must implement TeamInvitationListener")
        }
    }

    interface TeamInvitationListener {

        fun applyEmail(email: String)
    }
}