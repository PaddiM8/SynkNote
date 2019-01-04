package org.synknote.models

data class ActionsReturn(var token: String, var newestActionId: String, var actions: ArrayList<ActionData>)