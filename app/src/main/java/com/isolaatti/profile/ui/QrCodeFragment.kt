package com.isolaatti.profile.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidmads.library.qrgenearator.BuildConfig
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.isolaatti.R
import com.isolaatti.databinding.FragmentQrCodeBinding
import com.isolaatti.utils.UrlGen

class QrCodeFragment : Fragment() {

    private lateinit var binding: FragmentQrCodeBinding
    private val args: QrCodeFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrCodeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateQR(generateUrl())
        binding.usernameText.text ="@${args.profile.uniqueUsername}"

        binding.toolbar.menu.findItem(R.id.share).icon?.setTint(ResourcesCompat.getColor(resources, android.R.color.white, null))

        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.share -> {
                    share()
                    true
                }
                else -> false
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val topPadding = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = topPadding.top)
            insets
        }

    }

    private fun generateUrl(): String {
        return "${com.isolaatti.BuildConfig.backend}/@${args.profile.uniqueUsername}"
    }
    private fun generateQR(text: String) {
        val qrgEncoder = QRGEncoder(text, null, QRGContents.Type.TEXT, 200)

        val bitmap = qrgEncoder.getBitmap(0)

        binding.imageView.setImageBitmap(bitmap)
    }

    private fun share() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/uri"
            putExtra(Intent.EXTRA_TEXT, generateUrl())
        }

        startActivity(Intent.createChooser(intent, "Share profile"))
    }
}