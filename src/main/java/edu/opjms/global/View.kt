package edu.opjms.global

interface View {
    var onNavigateBackRequest: (() -> Unit)?
    fun cleanup()
}