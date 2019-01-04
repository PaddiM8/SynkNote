package org.synknote.models

import org.synknote.misc.NoteActionTypes

data class ActionData(var actionType: Int, var subject: String, var value: String)