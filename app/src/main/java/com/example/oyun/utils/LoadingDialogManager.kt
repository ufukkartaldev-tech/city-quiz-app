package com.example.oyun.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.example.oyun.databinding.DialogLoadingBinding

/**
 * Yükleme dialog'u yöneticisi
 */
class LoadingDialogManager(context: Context) {
    
    private val dialog: Dialog = Dialog(context)
    private val binding: DialogLoadingBinding
    
    init {
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    
    /**
     * Loading dialog'unu gösterir
     */
    fun show(message: String = "Yükleniyor...") {
        binding.loadingText.text = message
        binding.progressBar.isIndeterminate = true
        binding.progressText.visibility = android.view.View.GONE
        
        if (!dialog.isShowing) {
            dialog.show()
        }
    }
    
    /**
     * İlerleme çubuğu ile gösterir
     */
    fun showProgress(message: String, current: Int, total: Int) {
        binding.loadingText.text = message
        binding.progressBar.isIndeterminate = false
        binding.progressBar.max = total
        binding.progressBar.progress = current
        binding.progressText.visibility = android.view.View.VISIBLE
        binding.progressText.text = "$current / $total"
        
        if (!dialog.isShowing) {
            dialog.show()
        }
    }
    
    /**
     * İlerlemeyi günceller
     */
    fun updateProgress(current: Int, total: Int) {
        binding.progressBar.progress = current
        binding.progressText.text = "$current / $total"
    }
    
    /**
     * Mesajı günceller
     */
    fun updateMessage(message: String) {
        binding.loadingText.text = message
    }
    
    /**
     * Dialog'u kapatır
     */
    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
    
    /**
     * Dialog gösteriliyor mu?
     */
    fun isShowing(): Boolean = dialog.isShowing
}

/**
 * Loading state'leri
 */
sealed class LoadingState {
    object Idle : LoadingState()
    data class Loading(val message: String = "Yükleniyor...") : LoadingState()
    data class Progress(val message: String, val current: Int, val total: Int) : LoadingState()
    object Success : LoadingState()
    data class Error(val error: GameError) : LoadingState()
}
