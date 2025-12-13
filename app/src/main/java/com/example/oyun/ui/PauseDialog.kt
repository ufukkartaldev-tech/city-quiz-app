package com.example.oyun.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.oyun.databinding.DialogPauseBinding // KROKİYİ TANIYORUZ

class PauseDialog : DialogFragment() {

    // Fragment'larda binding'i bu şekilde yönetmek en sağlamıdır.
    private var _binding: DialogPauseBinding? = null
    private val binding get() = _binding!! // Bu satır, null kontrolünden kurtarır.

    private var onResumeListener: (() -> Unit)? = null
    private var onRestartListener: (() -> Unit)? = null
    private var onExitListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // KROKİYİ BURADA ELİMİZE ALIYORUZ
        _binding = DialogPauseBinding.inflate(inflater, container, false)
        return binding.root // Dükkânı krokiyle açıyoruz
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ARTIK findViewById YOK! DİREKT KROKİDEN...
        binding.btnContinue.setOnClickListener {
            onResumeListener?.invoke()
            dismiss()
        }

        binding.btnRestart.setOnClickListener {
            onRestartListener?.invoke()
            dismiss()
        }

        binding.btnExit.setOnClickListener {
            onExitListener?.invoke()
            dismiss()
        }
    }

    fun setListeners(
        onResume: () -> Unit,
        onRestart: () -> Unit,
        onExit: () -> Unit
    ) {
        onResumeListener = onResume
        onRestartListener = onRestart
        onExitListener = onExit
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // İşimiz bitince krokiyi masadan kaldırıyoruz ki hafızada yer kaplamasın.
        _binding = null
    }
}
