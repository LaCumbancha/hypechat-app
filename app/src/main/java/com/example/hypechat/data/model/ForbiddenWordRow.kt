package com.example.hypechat.data.model

import com.example.hypechat.R
import com.example.hypechat.data.model.rest.response.ForbiddenWord
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.forbidden_word_row.view.*

class ForbiddenWordRow(val forbiddenWord: ForbiddenWord): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.forbidden_word_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.forbiddenWordTextView.text = forbiddenWord.word
    }

}